package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.SysSettingDto;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.AiDocSummaryService;
import com.databox.utils.DateUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * AI智能文档摘要 Controller
 */
@RestController
@RequestMapping("/ai")
public class AiDocSummaryController extends ABaseController {

    @Resource
    private AiDocSummaryService aiDocSummaryService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 文档智能解析提取核心交互接口
     */
    @RequestMapping("/summary")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO summary(HttpSession session, @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        // 执行流式处理防御
        String summaryContent = aiDocSummaryService.generateSummary(webUserDto, fileId);
        return getSuccessResponseVO(summaryContent);
    }

    /**
     * 获取当前用户剩余的AI额度
     */
    @RequestMapping("/getAiQuota")
    @GlobalInterceptor(checkParams = false)
    public ResponseVO getAiQuota(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto == null) {
            return getSuccessResponseVO(0);
        }

        // 1. 获取系统配置的每日总额度
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        int total = sysSettingDto.getUserAiSummaryCount();

        // 2. 构建当天的 Redis Key (请确保 Constants 中的 Key 名称与你扣减额度时保存的保持一致)
        String dateStr = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String redisKey = Constants.REDIS_KEY_AI_SUMMARY_COUNT + dateStr + ":" + userDto.getUserId();

        // 3. 安全地获取 Redis 中的值：完美解决 0点过后 Key 失效/不存在 的问题
        Object usedObj = redisComponent.get(redisKey);
        int used = 0;
        if (usedObj != null) {
            try {
                // 先转 String 再转 Integer，彻底杜绝 ClassCastException
                used = Integer.parseInt(usedObj.toString());
            } catch (NumberFormatException e) {
                used = 0;
            }
        }

        // 4. 计算剩余次数并返回（增加 Math.max 防止超扣产生负数）
        int remain = total - used;
        return getSuccessResponseVO(Math.max(remain, 0));
    }
}