package com.databox.service;

import java.util.List;

import com.databox.entity.query.ShareReportQuery;
import com.databox.entity.po.ShareReport;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 分享违规举报表 业务接口
 */
public interface ShareReportService {

	/**
	 * 根据条件查询列表
	 */
	List<ShareReport> findListByParam(ShareReportQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ShareReportQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ShareReport> findListByPage(ShareReportQuery param);

	/**
	 * 新增
	 */
	Integer add(ShareReport bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ShareReport> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ShareReport> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ShareReport bean,ShareReportQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ShareReportQuery param);

	/**
	 * 根据ReportId查询对象
	 */
	ShareReport getShareReportByReportId(Integer reportId);


	/**
	 * 根据ReportId修改
	 */
	Integer updateShareReportByReportId(ShareReport bean,Integer reportId);


	/**
	 * 根据ReportId删除
	 */
	Integer deleteShareReportByReportId(Integer reportId);

	/**
	 * 提交举报信息
	 * @param shareId
	 * @param fileId
	 * @param userId
	 * @param ip
	 * @param reason
	 */
    void submitReport(String shareId, String fileId, String userId, String ip, String reason);

	/**
	 * 管理员处理举报信息
	 * @param reportId
	 * @param status
	 */
	void processReport(Integer reportId, Integer status);
}