package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户操作日志表
 */
public class SysOpLog implements Serializable {


	/**
	 * 日志ID
	 */
	private Integer logId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 操作模块（如：文件管理）
	 */
	private String module;

	/**
	 * 操作动作（如：删除文件）
	 */
	private String action;

	/**
	 * 状态 1:成功 0:失败
	 */
	private Integer status;

	/**
	 * 返回信息或异常提示
	 */
	private String resultMsg;

	/**
	 * 操作时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setLogId(Integer logId){
		this.logId = logId;
	}

	public Integer getLogId(){
		return this.logId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return this.userName;
	}

	public void setModule(String module){
		this.module = module;
	}

	public String getModule(){
		return this.module;
	}

	public void setAction(String action){
		this.action = action;
	}

	public String getAction(){
		return this.action;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setResultMsg(String resultMsg){
		this.resultMsg = resultMsg;
	}

	public String getResultMsg(){
		return this.resultMsg;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "日志ID:"+(logId == null ? "空" : logId)+"，用户ID:"+(userId == null ? "空" : userId)+"，用户名:"+(userName == null ? "空" : userName)+"，操作模块（如：文件管理）:"+(module == null ? "空" : module)+"，操作动作（如：删除文件）:"+(action == null ? "空" : action)+"，状态 1:成功 0:失败:"+(status == null ? "空" : status)+"，返回信息或异常提示:"+(resultMsg == null ? "空" : resultMsg)+"，操作时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
