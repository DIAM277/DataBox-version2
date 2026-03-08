package com.databox.service;

import java.util.List;

import com.databox.entity.query.UserLoginLogQuery;
import com.databox.entity.po.UserLoginLog;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 用户登录日志表 业务接口
 */
public interface UserLoginLogService {

	/**
	 * 根据条件查询列表
	 */
	List<UserLoginLog> findListByParam(UserLoginLogQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserLoginLogQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserLoginLog> findListByPage(UserLoginLogQuery param);

	/**
	 * 新增
	 */
	Integer add(UserLoginLog bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserLoginLog> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserLoginLog> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserLoginLog bean,UserLoginLogQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserLoginLogQuery param);

	/**
	 * 根据LogId查询对象
	 */
	UserLoginLog getUserLoginLogByLogId(Integer logId);


	/**
	 * 根据LogId修改
	 */
	Integer updateUserLoginLogByLogId(UserLoginLog bean,Integer logId);


	/**
	 * 根据LogId删除
	 */
	Integer deleteUserLoginLogByLogId(Integer logId);

}