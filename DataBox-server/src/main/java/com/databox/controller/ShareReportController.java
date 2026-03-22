package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.enums.ShareReportEnum;
import com.databox.entity.query.ShareReportQuery;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.exception.BusinessException;
import com.databox.service.ShareReportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/shareReport")
public class ShareReportController extends ABaseController {

    @Resource
    private ShareReportService shareReportService;

    /**
     * 获取举报列表（管理员查看）
     */
    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadDataList(ShareReportQuery query) {
        // 默认按状态升序（0待处理排在最前面），再按时间倒序
        query.setOrderBy("status asc, create_time desc");
        PaginationResultVO result = shareReportService.findListByPage(query);
        return getSuccessResponseVO(result);
    }

    /**
     * 处理举报 核心业务接口
     * @param reportId 举报记录ID
     * @param status 1:封禁分享 2:标记为正常
     */
    @RequestMapping("/process")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO process(@VerifyParam(required = true) Integer reportId,
                              @VerifyParam(required = true) Integer status) {
        
        ShareReportEnum reportEnum = ShareReportEnum.getByStatus(status);
        if (reportEnum == null || ShareReportEnum.PENDING.equals(reportEnum)) {
            throw new BusinessException("处理状态参数不合法");
        }

        // 调用 Service 层处理
        shareReportService.processReport(reportId, status);
        
        return getSuccessResponseVO(null);
    }
}