package com.databox.service.impl;

import com.databox.component.RedisComponent;
import com.databox.entity.config.AppConfig;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.entity.po.AiDocSummary;
import com.databox.entity.po.FileInfo;
import com.databox.exception.BusinessException;
import com.databox.mappers.AiDocSummaryMapper;
import com.databox.service.AiDocSummaryService;
import com.databox.service.FileInfoService;
import com.databox.utils.JsonUtils;
import com.databox.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("aiDocSummaryService")
public class AiDocSummaryServiceImpl implements AiDocSummaryService {

    @Resource
    private AiDocSummaryMapper aiDocSummaryMapper;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${deepseek.api.url}")
    private String deepseekUrl;

    @Value("${deepseek.api.key}")
    private String deepseekApiKey;

    private static final int MAX_TEXT_LENGTH = 4000; // 最长字符数

    @Override
    public String generateSummary(SessionWebUserDto webUserDto, String fileId) {
        // 获取文件实体和物理路径验证
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, webUserDto.getUserId());
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String fileMd5 = fileInfo.getFileMd5();

        // 查询全站摘要池库 (命中立刻返回)
        AiDocSummary existSummary = aiDocSummaryMapper.selectByFileMd5(fileMd5);
        if (existSummary != null) {
            return existSummary.getSummaryContent();
        }

        // 3. 第二道防线：Redis 每日限额防控 (普通用户限制)
        String todayStr = LocalDate.now().toString();
        String redisCountKey = Constants.REDIS_KEY_AI_SUMMARY_COUNT + todayStr + ":" + webUserDto.getUserId();

        // 对于非管理员用户，执行调用次数限制检查
        if (!webUserDto.getIsAdmin()) {
            Integer limitCount = redisComponent.getSysSettingDto().getUserAiSummaryCount();
            if (limitCount == null)
                limitCount = 5; // 兜底配置

            String currentStr = stringRedisTemplate.opsForValue().get(redisCountKey);
            int currentCount = currentStr == null ? 0 : Integer.parseInt(currentStr);
            if (currentCount >= limitCount) {
                throw new BusinessException("今日 AI 智能摘要调用额度（当前用户上限：" + limitCount + "次）已用尽，请明日重试。");
            }
        }

        // 解析文本与超长安全截断
        String filePathStr = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
        File docFile = new File(filePathStr);
        if (!docFile.exists()) {
            throw new BusinessException("文件物理本体不存在，无法解析");
        }

        String pureText = "";
        try {
            Tika tika = new Tika();
            pureText = tika.parseToString(docFile);
        } catch (Exception e) {
            log.error("Tika 解析文件失败", e);
            throw new BusinessException("文档格式解析失败，目前暂不支持该排版。");
        }

        if (StringTools.isEmpty(pureText)) {
            throw new BusinessException("未从该文档提取到任何有效文本");
        }
        if (pureText.length() > MAX_TEXT_LENGTH) {
            pureText = pureText.substring(0, MAX_TEXT_LENGTH);
        }

        // 调用 DeepSeek OpenAI
        String summaryResult = doCallDeepSeek(pureText);

        // 执行额度自增控制
        if (!webUserDto.getIsAdmin()) {
            Long count = stringRedisTemplate.opsForValue().increment(redisCountKey);
            // 设定在当日晚上 23:59:59 到期
            if (count != null && count == 1) {
                LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
                Duration duration = Duration.between(LocalDateTime.now(), endOfDay);
                stringRedisTemplate.expire(redisCountKey, duration.getSeconds(), TimeUnit.SECONDS);
            }
        }

        // 异步全站缓存入库
        CompletableFuture.runAsync(() -> {
            try {
                AiDocSummary aiDocSummary = new AiDocSummary();
                aiDocSummary.setSummaryId(StringTools.getRandomString(Constants.LENGTH_20));
                aiDocSummary.setFileMd5(fileMd5);
                aiDocSummary.setSummaryContent(summaryResult);
                aiDocSummary.setCreateTime(new Date());
                aiDocSummaryMapper.insert(aiDocSummary);
            } catch (Exception e) {
                log.error("异步保存文档AI摘要失败, md5:{}", fileMd5, e);
            }
        });

        return summaryResult;
    }

    /**
     * 发起 DeepSeek Http 请求引擎核心
     */
    private String doCallDeepSeek(String extractedText) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", "deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一名高效、专业的文档AI助手。请针对用户上传的文本片段，使用简练概括的中文进行主旨总结提取（字数不宜过少，但排版需易读）。");
        messages.add(sysMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "待整理文本如下：\n" + extractedText);
        messages.add(userMsg);

        reqBody.put("messages", messages);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, JsonUtils.convertObj2Json(reqBody));
        Request request = new Request.Builder()
                .url(deepseekUrl)
                .header("Authorization", "Bearer " + deepseekApiKey)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBodyStr = response.body().string();
                Map<String, Object> resMap = JsonUtils.convertJson2Obj(responseBodyStr, Map.class);
                if (resMap != null && resMap.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) resMap.get("choices");
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageObj.get("content");
                }
            } else {
                log.error("DeepSeek 接口异常, 状态码: {}", response.code());
            }
        } catch (Exception e) {
            log.error("请求 DeepSeek 服务发生致命异常", e);
        }
        throw new BusinessException("AI 服务网络拥堵，思考超时，请稍后重试。");
    }
}