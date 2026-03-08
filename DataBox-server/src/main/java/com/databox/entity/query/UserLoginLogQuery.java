package com.databox.entity.query;

import java.util.Date;


/**
 * 用户登录日志表参数
 */
public class UserLoginLogQuery extends BaseParam {


	/**
	 * 日志ID（主键）
	 */
	private Integer logId;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 登录IP地址
	 */
	private String loginIp;

	private String loginIpFuzzy;

	/**
	 * IP归属地（如：中国-广东-深圳）
	 */
	private String loginLocation;

	private String loginLocationFuzzy;

	/**
	 * 登录时间
	 */
	private String loginTime;

	private String loginTimeStart;

	private String loginTimeEnd;


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

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
	}

	public void setLoginIp(String loginIp){
		this.loginIp = loginIp;
	}

	public String getLoginIp(){
		return this.loginIp;
	}

	public void setLoginIpFuzzy(String loginIpFuzzy){
		this.loginIpFuzzy = loginIpFuzzy;
	}

	public String getLoginIpFuzzy(){
		return this.loginIpFuzzy;
	}

	public void setLoginLocation(String loginLocation){
		this.loginLocation = loginLocation;
	}

	public String getLoginLocation(){
		return this.loginLocation;
	}

	public void setLoginLocationFuzzy(String loginLocationFuzzy){
		this.loginLocationFuzzy = loginLocationFuzzy;
	}

	public String getLoginLocationFuzzy(){
		return this.loginLocationFuzzy;
	}

	public void setLoginTime(String loginTime){
		this.loginTime = loginTime;
	}

	public String getLoginTime(){
		return this.loginTime;
	}

	public void setLoginTimeStart(String loginTimeStart){
		this.loginTimeStart = loginTimeStart;
	}

	public String getLoginTimeStart(){
		return this.loginTimeStart;
	}
	public void setLoginTimeEnd(String loginTimeEnd){
		this.loginTimeEnd = loginTimeEnd;
	}

	public String getLoginTimeEnd(){
		return this.loginTimeEnd;
	}

}
