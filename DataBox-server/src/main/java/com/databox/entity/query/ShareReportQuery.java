package com.databox.entity.query;

import java.util.Date;


/**
 * 分享违规举报表参数
 */
public class ShareReportQuery extends BaseParam {


	/**
	 * 举报ID
	 */
	private Integer reportId;

	/**
	 * 分享ID
	 */
	private String shareId;

	private String shareIdFuzzy;

	/**
	 * 文件ID
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 举报人ID(未登录为空)
	 */
	private String reportUserId;

	private String reportUserIdFuzzy;

	/**
	 * 举报人IP(防刷必备)
	 */
	private String reportIp;

	private String reportIpFuzzy;

	/**
	 * 举报原因
	 */
	private String reason;

	private String reasonFuzzy;

	/**
	 * 状态 0:待处理 1:已封禁(通过) 2:正常(驳回)
	 */
	private Integer status;

	/**
	 * 举报时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setReportId(Integer reportId){
		this.reportId = reportId;
	}

	public Integer getReportId(){
		return this.reportId;
	}

	public void setShareId(String shareId){
		this.shareId = shareId;
	}

	public String getShareId(){
		return this.shareId;
	}

	public void setShareIdFuzzy(String shareIdFuzzy){
		this.shareIdFuzzy = shareIdFuzzy;
	}

	public String getShareIdFuzzy(){
		return this.shareIdFuzzy;
	}

	public void setFileId(String fileId){
		this.fileId = fileId;
	}

	public String getFileId(){
		return this.fileId;
	}

	public void setFileIdFuzzy(String fileIdFuzzy){
		this.fileIdFuzzy = fileIdFuzzy;
	}

	public String getFileIdFuzzy(){
		return this.fileIdFuzzy;
	}

	public void setReportUserId(String reportUserId){
		this.reportUserId = reportUserId;
	}

	public String getReportUserId(){
		return this.reportUserId;
	}

	public void setReportUserIdFuzzy(String reportUserIdFuzzy){
		this.reportUserIdFuzzy = reportUserIdFuzzy;
	}

	public String getReportUserIdFuzzy(){
		return this.reportUserIdFuzzy;
	}

	public void setReportIp(String reportIp){
		this.reportIp = reportIp;
	}

	public String getReportIp(){
		return this.reportIp;
	}

	public void setReportIpFuzzy(String reportIpFuzzy){
		this.reportIpFuzzy = reportIpFuzzy;
	}

	public String getReportIpFuzzy(){
		return this.reportIpFuzzy;
	}

	public void setReason(String reason){
		this.reason = reason;
	}

	public String getReason(){
		return this.reason;
	}

	public void setReasonFuzzy(String reasonFuzzy){
		this.reasonFuzzy = reasonFuzzy;
	}

	public String getReasonFuzzy(){
		return this.reasonFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}
