package com.databox.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 分享违规举报表 数据库操作接口
 */
public interface ShareReportMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ReportId更新
	 */
	 Integer updateByReportId(@Param("bean") T t,@Param("reportId") Integer reportId);


	/**
	 * 根据ReportId删除
	 */
	 Integer deleteByReportId(@Param("reportId") Integer reportId);


	/**
	 * 根据ReportId获取对象
	 */
	 T selectByReportId(@Param("reportId") Integer reportId);


}
