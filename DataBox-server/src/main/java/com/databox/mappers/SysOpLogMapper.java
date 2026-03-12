package com.databox.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户操作日志表 数据库操作接口
 */
public interface SysOpLogMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据LogId更新
	 */
	 Integer updateByLogId(@Param("bean") T t,@Param("logId") Integer logId);


	/**
	 * 根据LogId删除
	 */
	 Integer deleteByLogId(@Param("logId") Integer logId);


	/**
	 * 根据LogId获取对象
	 */
	 T selectByLogId(@Param("logId") Integer logId);


}
