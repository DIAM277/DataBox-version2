package com.databox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.databox.entity.enums.PageSize;
import com.databox.entity.query.UserLoginLogQuery;
import com.databox.entity.po.UserLoginLog;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.UserLoginLogMapper;
import com.databox.service.UserLoginLogService;
import com.databox.utils.StringTools;


/**
 * 用户登录日志表 业务接口实现
 */
@Service("userLoginLogService")
public class UserLoginLogServiceImpl implements UserLoginLogService {

	@Resource
	private UserLoginLogMapper<UserLoginLog, UserLoginLogQuery> userLoginLogMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserLoginLog> findListByParam(UserLoginLogQuery param) {
		return this.userLoginLogMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserLoginLogQuery param) {
		return this.userLoginLogMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserLoginLog> findListByPage(UserLoginLogQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserLoginLog> list = this.findListByParam(param);
		PaginationResultVO<UserLoginLog> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserLoginLog bean) {
		return this.userLoginLogMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserLoginLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userLoginLogMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserLoginLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userLoginLogMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserLoginLog bean, UserLoginLogQuery param) {
		StringTools.checkParam(param);
		return this.userLoginLogMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserLoginLogQuery param) {
		StringTools.checkParam(param);
		return this.userLoginLogMapper.deleteByParam(param);
	}

	/**
	 * 根据LogId获取对象
	 */
	@Override
	public UserLoginLog getUserLoginLogByLogId(Integer logId) {
		return this.userLoginLogMapper.selectByLogId(logId);
	}

	/**
	 * 根据LogId修改
	 */
	@Override
	public Integer updateUserLoginLogByLogId(UserLoginLog bean, Integer logId) {
		return this.userLoginLogMapper.updateByLogId(bean, logId);
	}

	/**
	 * 根据LogId删除
	 */
	@Override
	public Integer deleteUserLoginLogByLogId(Integer logId) {
		return this.userLoginLogMapper.deleteByLogId(logId);
	}

	// 查询用户【上一次】登录记录（按时间倒序，取第一条）
	@Override
	public UserLoginLog getLastLoginLog(String userId) {
		UserLoginLogQuery query = new UserLoginLogQuery();
		query.setUserId(userId);
		query.setOrderBy("login_time desc");
		query.setPageNo(1);
		query.setPageSize(1);
		List<UserLoginLog> loginLogList = this.findListByParam(query);
		return loginLogList.isEmpty() ? null : loginLogList.get(0);
	}
}