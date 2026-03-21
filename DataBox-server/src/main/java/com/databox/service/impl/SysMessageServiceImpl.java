package com.databox.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.databox.entity.constants.Constants;
import org.springframework.stereotype.Service;

import com.databox.entity.enums.PageSize;
import com.databox.entity.query.SysMessageQuery;
import com.databox.entity.po.SysMessage;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.SysMessageMapper;
import com.databox.service.SysMessageService;
import com.databox.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 系统消息通知表 业务接口实现
 */
@Service("sysMessageService")
public class SysMessageServiceImpl implements SysMessageService {

	@Resource
	private SysMessageMapper<SysMessage, SysMessageQuery> sysMessageMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SysMessage> findListByParam(SysMessageQuery param) {
		return this.sysMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SysMessageQuery param) {
		return this.sysMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SysMessage> findListByPage(SysMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SysMessage> list = this.findListByParam(param);
		PaginationResultVO<SysMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SysMessage bean) {
		return this.sysMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SysMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SysMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SysMessage bean, SysMessageQuery param) {
		StringTools.checkParam(param);
		return this.sysMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SysMessageQuery param) {
		StringTools.checkParam(param);
		return this.sysMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public SysMessage getSysMessageByMessageId(Integer messageId) {
		return this.sysMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateSysMessageByMessageId(SysMessage bean, Integer messageId) {
		return this.sysMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteSysMessageByMessageId(Integer messageId) {
		return this.sysMessageMapper.deleteByMessageId(messageId);
	}

	/**
	 * 发送系统消息
	 * @param userId
	 * @param title
	 * @param content
	 */
	@Override
	public void saveMessage(String userId, String title, String content) {
		SysMessage message = new SysMessage();
		message.setUserId(userId);
		message.setTitle(title);
		message.setContent(content);
		message.setReadStatus(Constants.SYS_MESSAGE_UNREAD); // 0:未读
		message.setCreateTime(new Date());
		this.sysMessageMapper.insert(message);
	}

	/**
	 * 标记消息为已读
	 * @param messageIds
	 * @param userId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markAsRead(String messageIds, String userId) {
		SysMessage updateInfo = new SysMessage();
		updateInfo.setReadStatus(Constants.SYS_MESSAGE_READ); // 1:已读

		SysMessageQuery query = new SysMessageQuery();
		query.setUserId(userId);
		// 如果前端传来的不是 "all"，说明是标记单条/多条
		if (!"all".equalsIgnoreCase(messageIds)) {
			String[] idArray = messageIds.split(",");
			// 将 String 数组转换为 Integer 数组
			Integer[] intArray = new Integer[idArray.length];
			for (int i = 0; i < idArray.length; i++) {
				intArray[i] = Integer.parseInt(idArray[i]);
			}
			query.setMessageIdArray(intArray);
		}
		this.sysMessageMapper.updateByParam(updateInfo, query);
	}
}