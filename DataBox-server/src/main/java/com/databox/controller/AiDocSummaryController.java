package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.AiDocSummaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * AI智能文档摘要 Controller
 */
@RestController
@RequestMapping("/ai")
public class AiDocSummaryController extends ABaseController {

    @Resource
    private AiDocSummaryService aiDocSummaryService;

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
}