package com.databox.entity.query;

import java.util.Date;


/**
 * 用户文件收藏表参数
 */
public class FileFavoriteQuery extends BaseParam {


	/**
	 * 收藏ID
	 */
	private Integer favoriteId;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 被收藏的文件ID
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 收藏时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setFavoriteId(Integer favoriteId){
		this.favoriteId = favoriteId;
	}

	public Integer getFavoriteId(){
		return this.favoriteId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
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
