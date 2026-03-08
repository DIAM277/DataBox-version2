package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户登录日志表
 */
public class UserLoginLog implements Serializable {


	/**
	 * 日志ID（主键）
	 */
	private Integer logId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 登录IP地址
	 */
	private String loginIp;

	/**
	 * IP归属地（如：中国-广东-深圳）
	 */
	private String loginLocation;

	/**
	 * 登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date loginTime;


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

	public void setLoginIp(String loginIp){
		this.loginIp = loginIp;
	}

	public String getLoginIp(){
		return this.loginIp;
	}

	public void setLoginLocation(String loginLocation){
		this.loginLocation = loginLocation;
	}

	public String getLoginLocation(){
		return this.loginLocation;
	}

	public void setLoginTime(Date loginTime){
		this.loginTime = loginTime;
	}

	public Date getLoginTime(){
		return this.loginTime;
	}

	@Override
	public String toString (){
		return "日志ID（主键）:"+(logId == null ? "空" : logId)+"，用户ID:"+(userId == null ? "空" : userId)+"，登录IP地址:"+(loginIp == null ? "空" : loginIp)+"，IP归属地（如：中国-广东-深圳）:"+(loginLocation == null ? "空" : loginLocation)+"，登录时间:"+(loginTime == null ? "空" : DateUtil.format(loginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
