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
 * 文件分享信息
 */
@Data
public class FileShare implements Serializable {


	/**
	 * 分享ID
	 */
	private String shareId;

	/**
	 * 文件ID
	 */
	private String fileId;

	/**
	 * 分享人ID
	 */
	private String userId;

	/**
	 * 有效期类型 0:1天 1:7天 2:30天 4:永久有效
	 */
	private Integer validType;

	/**
	 * 失效时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

	/**
	 * 分享时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date shareTime;

	/**
	 * 提取码
	 */
	private String code;

	/**
	 * 浏览次数
	 */
	private Integer showCount;

	private String fileName;
	private String fileCover;
	private Integer folderType;
	private Integer fileCategory;
	private Integer fileType;

	@Override
	public String toString (){
		return "分享ID:"+(shareId == null ? "空" : shareId)+"，文件ID:"+(fileId == null ? "空" : fileId)+"，分享人ID:"+(userId == null ? "空" : userId)+"，有效期类型 0:1天 1:7天 2:30天 4:永久有效:"+(validType == null ? "空" : validType)+"，失效时间:"+(expireTime == null ? "空" : DateUtil.format(expireTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，分享时间:"+(shareTime == null ? "空" : DateUtil.format(shareTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，提取码:"+(code == null ? "空" : code)+"，浏览次数:"+(showCount == null ? "空" : showCount);
	}
}
