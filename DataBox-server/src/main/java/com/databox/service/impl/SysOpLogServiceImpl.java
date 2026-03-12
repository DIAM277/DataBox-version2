package com.databox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.databox.entity.enums.PageSize;
import com.databox.entity.query.SysOpLogQuery;
import com.databox.entity.po.SysOpLog;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.SysOpLogMapper;
import com.databox.service.SysOpLogService;
import com.databox.utils.StringTools;


/**
 * 用户操作日志表 业务接口实现
 */
@Service("sysOpLogService")
public class SysOpLogServiceImpl implements SysOpLogService {

	@Resource
	private SysOpLogMapper<SysOpLog, SysOpLogQuery> sysOpLogMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SysOpLog> findListByParam(SysOpLogQuery param) {
		return this.sysOpLogMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SysOpLogQuery param) {
		return this.sysOpLogMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SysOpLog> findListByPage(SysOpLogQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SysOpLog> list = this.findListByParam(param);
		PaginationResultVO<SysOpLog> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SysOpLog bean) {
		return this.sysOpLogMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SysOpLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysOpLogMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SysOpLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysOpLogMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SysOpLog bean, SysOpLogQuery param) {
		StringTools.checkParam(param);
		return this.sysOpLogMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SysOpLogQuery param) {
		StringTools.checkParam(param);
		return this.sysOpLogMapper.deleteByParam(param);
	}

	/**
	 * 根据LogId获取对象
	 */
	@Override
	public SysOpLog getSysOpLogByLogId(Integer logId) {
		return this.sysOpLogMapper.selectByLogId(logId);
	}

	/**
	 * 根据LogId修改
	 */
	@Override
	public Integer updateSysOpLogByLogId(SysOpLog bean, Integer logId) {
		return this.sysOpLogMapper.updateByLogId(bean, logId);
	}

	/**
	 * 根据LogId删除
	 */
	@Override
	public Integer deleteSysOpLogByLogId(Integer logId) {
		return this.sysOpLogMapper.deleteByLogId(logId);
	}
}