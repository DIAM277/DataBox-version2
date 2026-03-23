package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 分享违规举报表
 */
@Data
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

	/** 冗余显示字段：文件名 */
	private String fileName;
	/** 冗余显示字段：文件封面 */
	private String fileCover;
	/** 冗余显示字段：文件夹类型 (0:文件 1:目录) */
	private Integer folderType;
	/** 冗余显示字段：文件分类 (1:视频 2:音频 3:图片 ...) */
	private Integer fileCategory;
	/** 冗余显示字段：该分享的提取码 */
	private String shareCode;
	/** 真实的文件精细类型 (用于精确渲染各个后缀的图标) */
	private Integer fileType;

	@Override
	public String toString (){
		return "举报ID:"+(reportId == null ? "空" : reportId)+"，分享ID:"+(shareId == null ? "空" : shareId)+"，文件ID:"+(fileId == null ? "空" : fileId)+"，举报人ID(未登录为空):"+(reportUserId == null ? "空" : reportUserId)+"，举报人IP(防刷必备):"+(reportIp == null ? "空" : reportIp)+"，举报原因:"+(reason == null ? "空" : reason)+"，状态 0:待处理 1:已封禁(通过) 2:正常(驳回):"+(status == null ? "空" : status)+"，举报时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
