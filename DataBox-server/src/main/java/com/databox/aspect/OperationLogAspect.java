package com.databox.aspect;

import com.databox.annotation.OpLog;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.po.FileInfo;
import com.databox.entity.po.SysOpLog;
import com.databox.entity.query.FileInfoQuery;
import com.databox.service.FileInfoService;
import com.databox.service.SysOpLogService;
import com.databox.utils.StringTools;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Aspect
public class OperationLogAspect {

    @Resource
    private SysOpLogService sysOpLogService;

    @Resource
    private FileInfoService fileInfoService;

    // 定义切点
    @Pointcut("@annotation(com.databox.annotation.OpLog)")
    public void logPointCut() {
    }

    // 对正常返回结果的拦截
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        // 记录操作日志
        handleLog(joinPoint, null);
    }

    // 对异常的拦截
    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        // 记录操作日志
        handleLog(joinPoint, e);
    }

    // 逻辑核心
    private void handleLog(JoinPoint joinPoint, Exception e){
        try{
            // 获取当前请求的request来获取session
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession();

            // 获取当前登陆的用户
            SessionWebUserDto webUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
            // 获取切入点方法和注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OpLog opLog = method.getAnnotation(OpLog.class);
            if(opLog == null) {
                return;
            }

            // 封装日志对象
            SysOpLog log = new SysOpLog();
            log.setModule(opLog.module());
            log.setAction(opLog.action());
            log.setCreateTime(new Date());

            // 填充用户信息
            if(webUserDto != null) {
                log.setUserId(webUserDto.getUserId());
                log.setUserName(webUserDto.getUserName());
            }

            // 处理状态和结果信息
            if (e != null) {
                log.setStatus(0); // 失败
                // 截取异常信息防止超长报错
                String errorMsg = e.getMessage();
                log.setResultMsg(errorMsg != null ? (errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg) : "未知异常");
            } else {
                log.setStatus(1); // 成功
                log.setResultMsg("操作成功");
            }

            // 尝试获取请求中的文件ID参数
            String fileIds = request.getParameter("fileIds");
            if(StringTools.isEmpty(fileIds)){
                fileIds = request.getParameter("fileId");
            }

            if(!StringTools.isEmpty(fileIds)) {
                // 根据逗号拆分
                String[] fileIdArray = fileIds.split(",");
                FileInfoQuery query = new FileInfoQuery();
                query.setFileIdArray(fileIdArray);

                // 预防查出不同人的同ID文件，携带当前用户ID
                if(webUserDto != null) {
                    query.setUserId(webUserDto.getUserId());
                }
                // 调用批量查询文件接口
                List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);

                if(fileInfoList != null && !fileInfoList.isEmpty()){
                    // 提取出所有文件名并用逗号拼接
                    String fileName = fileInfoList.stream()
                            .map(FileInfo::getFileName)
                            .collect(Collectors.joining(", "));
                    String detailMsg = "操作文件：[" + fileName + "]";
                    // 防止文件名过长导致插入错误(截取前490个字符)
                    log.setDetail(detailMsg.length() > 490 ? detailMsg.substring(0, 490) : detailMsg);
                }else {
                    log.setDetail("操作对象ID" + fileIds);
                }
            }

            // 异步保存，不阻塞主线程
            CompletableFuture.runAsync(() -> {
                sysOpLogService.add(log);
            });
        }catch (Exception ex){
            // 记录日志失败，不影响正常业务逻辑
            ex.printStackTrace();
        }
    }
}
