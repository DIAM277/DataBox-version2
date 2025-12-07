package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户信息表
 */
public class UserInfo implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 用户QQ
	 */
	private String qqOpenId;

	/**
	 * QQ头像
	 */
	private String qqAvatar;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 最后登陆时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 用户状态:0禁用 1启用
	 */
	private Integer status;

	/**
	 * 用户使用空间
	 */
	private Long userSpace;

	/**
	 * 总空间
	 */
	private Long totalSpace;


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

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setQqOpenId(String qqOpenId){
		this.qqOpenId = qqOpenId;
	}

	public String getQqOpenId(){
		return this.qqOpenId;
	}

	public void setQqAvatar(String qqAvatar){
		this.qqAvatar = qqAvatar;
	}

	public String getQqAvatar(){
		return this.qqAvatar;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setLastLoginTime(Date lastLoginTime){
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLoginTime(){
		return this.lastLoginTime;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setUserSpace(Long userSpace){
		this.userSpace = userSpace;
	}

	public Long getUserSpace(){
		return this.userSpace;
	}

	public void setTotalSpace(Long totalSpace){
		this.totalSpace = totalSpace;
	}

	public Long getTotalSpace(){
		return this.totalSpace;
	}

	@Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，用户名:"+(userName == null ? "空" : userName)+"，邮箱:"+(email == null ? "空" : email)+"，用户QQ:"+(qqOpenId == null ? "空" : qqOpenId)+"，QQ头像:"+(qqAvatar == null ? "空" : qqAvatar)+"，密码:"+(password == null ? "空" : password)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登陆时间:"+(lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，用户状态:0禁用 1启用:"+(status == null ? "空" : status)+"，用户使用空间:"+(userSpace == null ? "空" : userSpace)+"，总空间:"+(totalSpace == null ? "空" : totalSpace);
	}
}
