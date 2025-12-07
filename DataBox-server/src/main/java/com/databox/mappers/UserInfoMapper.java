package com.databox.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户信息表 数据库操作接口
 */
public interface UserInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据UserId更新
	 */
	 Integer updateByUserId(@Param("bean") T t,@Param("userId") String userId);


	/**
	 * 根据UserId删除
	 */
	 Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * 根据UserId获取对象
	 */
	 T selectByUserId(@Param("userId") String userId);


	/**
	 * 根据Email更新
	 */
	 Integer updateByEmail(@Param("bean") T t,@Param("email") String email);


	/**
	 * 根据Email删除
	 */
	 Integer deleteByEmail(@Param("email") String email);


	/**
	 * 根据Email获取对象
	 */
	 T selectByEmail(@Param("email") String email);


	/**
	 * 根据QqOpenId更新
	 */
	 Integer updateByQqOpenId(@Param("bean") T t,@Param("qqOpenId") String qqOpenId);


	/**
	 * 根据QqOpenId删除
	 */
	 Integer deleteByQqOpenId(@Param("qqOpenId") String qqOpenId);


	/**
	 * 根据QqOpenId获取对象
	 */
	 T selectByQqOpenId(@Param("qqOpenId") String qqOpenId);


	/**
	 * 根据UserName更新
	 */
	 Integer updateByUserName(@Param("bean") T t,@Param("userName") String userName);


	/**
	 * 根据UserName删除
	 */
	 Integer deleteByUserName(@Param("userName") String userName);


	/**
	 * 根据UserName获取对象
	 */
	 T selectByUserName(@Param("userName") String userName);

	/**
	 * 更新用户空间使用情况
	 * @param userId
	 * @param usedSpace
	 * @param totalSpace
	 * @return
	 */
	Integer updateUserSpace(@Param("userId") String userId, @Param("usedSpace") Long usedSpace, @Param("totalSpace") Long totalSpace);

	/**
	 * 设置用户总空间
	 * @param userId
	 * @param totalSpace
	 * @return
	 */
	Integer setUserTotalSpace(@Param("userId") String userId, @Param("totalSpace") Long totalSpace);
}
