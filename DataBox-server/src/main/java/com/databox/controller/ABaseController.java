package com.databox.controller;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionShareDto;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.exception.BusinessException;
import com.databox.utils.CopyTools;
import com.databox.utils.StringTools;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class ABaseController {

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        // 动态获取当前请求的 request 对象以读取 Header，解耦各个 Controller 的方法签名
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception e) {
            // 忽略非web环境调用
        }

        // 使用 RandomAccessFile 进行灵活的指针读取
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
             OutputStream out = response.getOutputStream()) {

            long fileLength = file.length();
            long start = 0;
            long end = fileLength - 1;

            if (request != null) {
                // 告诉前端播放器：我们支持断点续传（拖动进度条）
                response.setHeader("Accept-Ranges", "bytes");
                String range = request.getHeader("Range");
                if (range != null && range.startsWith("bytes=")) {
                    String[] values = range.substring("bytes=".length()).split("-");
                    start = Long.parseLong(values[0]);
                    if (values.length > 1 && !values[1].isEmpty()) {
                        end = Long.parseLong(values[1]);
                    }
                    if (end > fileLength - 1) {
                        end = fileLength - 1;
                    }

                    // HTTP 206 状态表示分段内容
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            long contentLength = end - start + 1;
            response.setHeader("Content-Length", String.valueOf(contentLength));

            // 将读取指针移动到起始位置
            randomAccessFile.seek(start);

            byte[] buffer = new byte[8192];
            long remaining = contentLength;
            int bytesRead;

            // 仅输出本次请求请求的数据块长度
            while (remaining > 0 && (bytesRead = randomAccessFile.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                out.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            out.flush();

        } catch (Exception e) {
            // 当用户拖拽进度条时，前端会主动断开旧连接，抛出属于预期内的管道断裂异常，这里静默处理防止控制台报错刷屏
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Broken pipe") || msg.contains("ClientAbortException") || msg.contains("Connection reset by peer"))) {
                // 正常取消连接，忽略
            } else {
                log.error("读取文件流失败", e);
            }
        }
    }

    protected SessionWebUserDto getUserInfoFromSession(HttpSession session){
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (null == sessionWebUserDto) {
            return null;
        }
        return sessionWebUserDto;
    }

    protected SessionShareDto getSessionShareFromSession(HttpSession session, String shareId){
        SessionShareDto sessionShareDto = (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
        if (null == sessionShareDto) {
            return null;
        }
        return sessionShareDto;
    }

    /**
     * 分页结果转换
     * @param result
     * @param classz
     * @return
     * @param <S>
     * @param <T>
     */
    protected <S,T>PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> classz) {
        PaginationResultVO<T> resultVO = new PaginationResultVO<>();
        resultVO.setList(CopyTools.copyList(result.getList(), classz));
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());
        return resultVO;
    }
}
