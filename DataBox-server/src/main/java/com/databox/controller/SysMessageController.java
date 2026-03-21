package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.query.SysMessageQuery;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.SysMessageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/message")
public class SysMessageController extends ABaseController {

	@Resource
	private SysMessageService sysMessageService;

	/**
	 * 获取消息列表
	 */
	@RequestMapping("/loadDataList")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO loadDataList(HttpSession session, SysMessageQuery query) {
		SessionWebUserDto userDto = getUserInfoFromSession(session);
		query.setUserId(userDto.getUserId());
		query.setOrderBy("create_time desc"); // 按时间倒序
		PaginationResultVO result = sysMessageService.findListByPage(query);
		return getSuccessResponseVO(result);
	}

	/**
	 * 获取未读消息数量 (用于渲染小红点)
	 */
	@RequestMapping("/getUnreadCount")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getUnreadCount(HttpSession session) {
		SessionWebUserDto userDto = getUserInfoFromSession(session);
		SysMessageQuery query = new SysMessageQuery();
		query.setUserId(userDto.getUserId());
		query.setReadStatus(0); // 0表示未读
		Integer count = sysMessageService.findCountByParam(query);
		return getSuccessResponseVO(count);
	}

	/**
	 * 标记消息为已读
	 */
	@RequestMapping("/markAsRead")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO markAsRead(HttpSession session, @VerifyParam(required = true) String messageIds) {
		SessionWebUserDto userDto = getUserInfoFromSession(session);
		sysMessageService.markAsRead(messageIds, userDto.getUserId());
		return getSuccessResponseVO(null);
	}
}