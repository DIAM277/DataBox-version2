package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 分享违规举报表
 */
public class ShareReport implements Serializable {


	/**
	 * 举报ID
	 */
	private Integer reportId;

	/**
	 * 分享ID
	 */
	private String shareId;

	/**
	 * 文件ID
	 */
	private String fileId;

	/**
	 * 举报人ID(未登录为空)
	 */
	private String reportUserId;

	/**
	 * 举报人IP(防刷必备)
	 */
	private String reportIp;

	/**
	 * 举报原因
	 */
	private String reason;

	/**
	 * 状态 0:待处理 1:已封禁(通过) 2:正常(驳回)
	 */
	private Integer status;

	/**
	 * 举报时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


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

	public void setFileId(String fileId){
		this.fileId = fileId;
	}

	public String getFileId(){
		return this.fileId;
	}

	public void setReportUserId(String reportUserId){
		this.reportUserId = reportUserId;
	}

	public String getReportUserId(){
		return this.reportUserId;
	}

	public void setReportIp(String reportIp){
		this.reportIp = reportIp;
	}

	public String getReportIp(){
		return this.reportIp;
	}

	public void setReason(String reason){
		this.reason = reason;
	}

	public String getReason(){
		return this.reason;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "举报ID:"+(reportId == null ? "空" : reportId)+"，分享ID:"+(shareId == null ? "空" : shareId)+"，文件ID:"+(fileId == null ? "空" : fileId)+"，举报人ID(未登录为空):"+(reportUserId == null ? "空" : reportUserId)+"，举报人IP(防刷必备):"+(reportIp == null ? "空" : reportIp)+"，举报原因:"+(reason == null ? "空" : reason)+"，状态 0:待处理 1:已封禁(通过) 2:正常(驳回):"+(status == null ? "空" : status)+"，举报时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
