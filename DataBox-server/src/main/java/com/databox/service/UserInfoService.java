package com.databox.service;

import java.util.List;

import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.query.UserInfoQuery;
import com.databox.entity.po.UserInfo;
import com.databox.entity.vo.PaginationResultVO;


/**
 * 用户信息表 业务接口
 */
public interface UserInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<UserInfo> findListByParam(UserInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(UserInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserInfo bean,UserInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserInfoQuery param);

	/**
	 * 根据UserId查询对象
	 */
	UserInfo getUserInfoByUserId(String userId);


	/**
	 * 根据UserId修改
	 */
	Integer updateUserInfoByUserId(UserInfo bean,String userId);


	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoByUserId(String userId);


	/**
	 * 根据Email查询对象
	 */
	UserInfo getUserInfoByEmail(String email);


	/**
	 * 根据Email修改
	 */
	Integer updateUserInfoByEmail(UserInfo bean,String email);


	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoByEmail(String email);


	/**
	 * 根据QqOpenId查询对象
	 */
	UserInfo getUserInfoByQqOpenId(String qqOpenId);


	/**
	 * 根据QqOpenId修改
	 */
	Integer updateUserInfoByQqOpenId(UserInfo bean,String qqOpenId);


	/**
	 * 根据QqOpenId删除
	 */
	Integer deleteUserInfoByQqOpenId(String qqOpenId);


	/**
	 * 根据UserName查询对象
	 */
	UserInfo getUserInfoByUserName(String userName);


	/**
	 * 根据UserName修改
	 */
	Integer updateUserInfoByUserName(UserInfo bean,String userName);


	/**
	 * 根据UserName删除
	 */
	Integer deleteUserInfoByUserName(String userName);

	/**
	 * 注册
	 * @param email
	 * @param userName
	 * @param password
	 * @param emailCode
	 */
	void register(String email, String userName, String password, String emailCode);

	/**
	 * 登录
	 * @return
	 */
	SessionWebUserDto login(String email, String password);

	/**
	 * 修改密码
	 * @param email
	 * @param password
	 * @param emailCode
	 */
	void resetPassword(String email, String password, String emailCode);

	/**
	 * qq登录
	 * @param code
	 * @return
	 */
	SessionWebUserDto qqLogin(String code);

	/**
	 * 修改用户状态
	 * @param userId
	 * @param status
	 */
	void updateUserStatus(String userId, Integer status);

	/**
	 * 更新用户空间
	 * @param userId
	 * @param changeSpace
	 */
	void updateUserSpace(String userId, Long changeSpace);

	/**
	 * 设置用户总空间
	 * @param userId
	 * @param totalSpace
	 */
	void setUserTotalSpace(String userId, Long totalSpace);


}