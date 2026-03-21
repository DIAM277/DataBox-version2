package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.databox.entity.enums.DateTimePatternEnum;
import com.databox.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 系统消息通知表
 */
public class SysMessage implements Serializable {


	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * 接收消息的用户ID
	 */
	private String userId;

	/**
	 * 消息标题
	 */
	private String title;

	/**
	 * 消息正文内容
	 */
	private String content;

	/**
	 * 阅读状态 0:未读 1:已读
	 */
	private Integer readStatus;

	/**
	 * 发送时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setMessageId(Integer messageId){
		this.messageId = messageId;
	}

	public Integer getMessageId(){
		return this.messageId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setReadStatus(Integer readStatus){
		this.readStatus = readStatus;
	}

	public Integer getReadStatus(){
		return this.readStatus;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "消息ID:"+(messageId == null ? "空" : messageId)+"，接收消息的用户ID:"+(userId == null ? "空" : userId)+"，消息标题:"+(title == null ? "空" : title)+"，消息正文内容:"+(content == null ? "空" : content)+"，阅读状态 0:未读 1:已读:"+(readStatus == null ? "空" : readStatus)+"，发送时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
