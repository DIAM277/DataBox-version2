package com.databox.service;

import java.util.List;

import com.databox.entity.dto.SessionShareDto;
import com.databox.entity.query.FileShareQuery;
import com.databox.entity.po.FileShare;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 文件分享信息 业务接口
 */
public interface FileShareService {

	/**
	 * 根据条件查询列表
	 */
	List<FileShare> findListByParam(FileShareQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(FileShareQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<FileShare> findListByPage(FileShareQuery param);

	/**
	 * 新增
	 */
	Integer add(FileShare bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<FileShare> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<FileShare> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(FileShare bean,FileShareQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(FileShareQuery param);

	/**
	 * 根据ShareId查询对象
	 */
	FileShare getFileShareByShareId(String shareId);


	/**
	 * 根据ShareId修改
	 */
	Integer updateFileShareByShareId(FileShare bean,String shareId);


	/**
	 * 根据ShareId删除
	 */
	Integer deleteFileShareByShareId(String shareId);

	/**
	 * 保存分享信息
	 * @param fileShare
	 */
	void saveShare(FileShare fileShare);

	/**
	 * 批量取消分享
	 * @param shareArray
	 * @param userId
	 */
	void deleteFileShareBatch(String[] shareArray, String userId);

	/**
	 * 校验文件提取码
	 * @param shareId
	 * @param code
	 * @return
	 */
	SessionShareDto checkShareCode(String shareId, String code);
}