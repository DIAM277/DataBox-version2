package com.databox.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 文件信息记录表参数
 */
@Data
public class FileInfoQuery extends BaseParam {


	/**
	 * 文件编号
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 文件所有者
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 文件MD5值
	 */
	private String fileMd5;

	private String fileMd5Fuzzy;

	/**
	 * 文件父级别ID
	 */
	private String filePid;

	private String filePidFuzzy;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件名称
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 文件缩略图
	 */
	private String fileCover;

	private String fileCoverFuzzy;

	/**
	 * 文件路径
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 文件创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 最近更新时间
	 */
	private String lastUpdateTime;

	private String lastUpdateTimeStart;

	private String lastUpdateTimeEnd;

	/**
	 * 类型；0文件，1目录
	 */
	private Integer folderType;

	/**
	 * 文件分类:1：视频 2：音频 3：图片 4：文档 5：其他
	 */
	private Integer fileCategory;

	/**
	 * 文件类型:1：视频 2：音频 3：图片 4：pdf 5：doc 6:excel 7:txt 8:code 9:zip 10:其他
	 */
	private Integer fileType;

	/**
	 * 文件状态;1:转码失败  2：转码成功
	 */
	private Integer status;

	/**
	 * 加入回收站时间
	 */
	private Date recoveryTime;

	private String recoveryTimeStart;

	private String recoveryTimeEnd;

	/**
	 * 删除标记 0：删除 1：回收站 2：正常
	 */
	private Integer delFlag;

	private String[] fileIdArray;

	private String[] excludeFileIdArray;
	private Boolean queryUserName;

	private Boolean queryExpire;

	private String sortField;
	private String sortOrder;

	private Integer referenceCount;

}
