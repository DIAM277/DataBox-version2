package com.databox.mappers;

import com.databox.entity.po.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件信息记录表 数据库操作接口
 */
public interface FileInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据FileIdAndUserId更新
	 */
	Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId);


	/**
	 * 根据FileIdAndUserId删除
	 */
	Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


	/**
	 * 根据FileIdAndUserId获取对象
	 */
	T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

	/**
	 * 获取用户已使用空间
	 *
	 * @param userId
	 * @return
	 */
	Long selectUseSpace(@Param("userId") String userId);

	/**
	 * 更新文件状态
	 *
	 * @param fileId
	 * @param userId
	 * @param t
	 * @param oldStatus
	 */
	void updateFileStatusWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") T t, @Param("oldStatus") Integer oldStatus);

	/**
	 * 批量更新文件删除标记
	 *
	 * @param fileInfo
	 * @param userId
	 * @param filePidList
	 */
	void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
								@Param("userId") String userId,
								@Param("filePidList") List<String> filePidList,
								@Param("fileIdList") List<String> fileIdList,
								@Param("oldDelFlag") Integer delFlag);

	/**
	 * 彻底删除文件
	 * @param userId
	 * @param filePidList
	 * @param fileIdList
	 * @param oldDelFlag
	 */
	void delFileBatch(@Param("userId") String userId,
					  @Param("filePidList")List<String> filePidList,
					  @Param("fileIdList") List<String> fileIdList,
					  @Param("oldDelFlag")Integer oldDelFlag);

	/**
	 * 根据用户id删除文件
	 * @param userId
	 */
	void deleteFileByUserId(@Param("userId") String userId);

	/**
	 * 更新文件引用计数
	 * @param fileMd5
	 * @param count
	 */
	Integer updateReferenceCount(@Param("fileMd5") String fileMd5, @Param("count") Integer count);

	/**
	 * 根据文件MD5获取文件信息
	 * @param fileMd5
	 * @return
	 */
	T selectByFileMd5(@Param("fileMd5") String fileMd5);
}
