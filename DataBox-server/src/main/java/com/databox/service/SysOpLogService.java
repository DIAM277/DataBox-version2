package com.databox.service;

import java.util.List;

import com.databox.entity.query.SysOpLogQuery;
import com.databox.entity.po.SysOpLog;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 用户操作日志表 业务接口
 */
public interface SysOpLogService {

	/**
	 * 根据条件查询列表
	 */
	List<SysOpLog> findListByParam(SysOpLogQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SysOpLogQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SysOpLog> findListByPage(SysOpLogQuery param);

	/**
	 * 新增
	 */
	Integer add(SysOpLog bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SysOpLog> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SysOpLog> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SysOpLog bean,SysOpLogQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SysOpLogQuery param);

	/**
	 * 根据LogId查询对象
	 */
	SysOpLog getSysOpLogByLogId(Integer logId);


	/**
	 * 根据LogId修改
	 */
	Integer updateSysOpLogByLogId(SysOpLog bean,Integer logId);


	/**
	 * 根据LogId删除
	 */
	Integer deleteSysOpLogByLogId(Integer logId);

}