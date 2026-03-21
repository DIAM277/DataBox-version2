package com.databox.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 系统消息通知表参数
 */
@Data
public class SysMessageQuery extends BaseParam {


	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * 接收消息的用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 消息标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 消息正文内容
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 阅读状态 0:未读 1:已读
	 */
	private Integer readStatus;

	/**
	 * 发送时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 消息ID数组，用于批量操作
	 */
	private Integer[] messageIdArray;

}
