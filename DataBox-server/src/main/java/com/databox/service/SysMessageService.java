package com.databox.service;

import java.util.List;

import com.databox.entity.query.SysMessageQuery;
import com.databox.entity.po.SysMessage;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 系统消息通知表 业务接口
 */
public interface SysMessageService {

	/**
	 * 根据条件查询列表
	 */
	List<SysMessage> findListByParam(SysMessageQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SysMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SysMessage> findListByPage(SysMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(SysMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SysMessage> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SysMessage> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SysMessage bean,SysMessageQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SysMessageQuery param);

	/**
	 * 根据MessageId查询对象
	 */
	SysMessage getSysMessageByMessageId(Integer messageId);


	/**
	 * 根据MessageId修改
	 */
	Integer updateSysMessageByMessageId(SysMessage bean,Integer messageId);


	/**
	 * 根据MessageId删除
	 */
	Integer deleteSysMessageByMessageId(Integer messageId);

	/**
	 * 发送系统消息
	 */
	void saveMessage(String userId, String title, String content);

	/**
	 * 标记消息为已读
	 */
	void markAsRead(String messageIds, String userId);

}