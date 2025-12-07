package com.databox.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.config.AppConfig;
import com.databox.entity.dto.QQInfoDto;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.SysSettingDto;
import com.databox.entity.dto.UserSpaceDto;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.entity.enums.UserStatusEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.exception.BusinessException;
import com.databox.mappers.FileInfoMapper;
import com.databox.service.EmailCodeService;
import com.databox.utils.JsonUtils;
import com.databox.utils.OKHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.databox.entity.enums.PageSize;
import com.databox.entity.query.UserInfoQuery;
import com.databox.entity.po.UserInfo;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.UserInfoMapper;
import com.databox.service.UserInfoService;
import com.databox.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户信息表 业务接口实现
 */
@Service("userInfoService")
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private AppConfig appConfig;

	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据QqOpenId获取对象
	 */
	@Override
	public UserInfo getUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.selectByQqOpenId(qqOpenId);
	}

	/**
	 * 根据QqOpenId修改
	 */
	@Override
	public Integer updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId) {
		return this.userInfoMapper.updateByQqOpenId(bean, qqOpenId);
	}

	/**
	 * 根据QqOpenId删除
	 */
	@Override
	public Integer deleteUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.deleteByQqOpenId(qqOpenId);
	}

	/**
	 * 根据UserName获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserName(String userName) {
		return this.userInfoMapper.selectByUserName(userName);
	}

	/**
	 * 根据UserName修改
	 */
	@Override
	public Integer updateUserInfoByUserName(UserInfo bean, String userName) {
		return this.userInfoMapper.updateByUserName(bean, userName);
	}

	/**
	 * 根据UserName删除
	 */
	@Override
	public Integer deleteUserInfoByUserName(String userName) {
		return this.userInfoMapper.deleteByUserName(userName);
	}

	/**
	 * 注册
	 * @param email
	 * @param userName
	 * @param password
	 * @param emailCode
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String userName, String password, String emailCode) {
		// 邮箱唯一
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if(null != userInfo){
			throw new BusinessException("该邮箱已经存在!");
		}
		// 用户名唯一
		UserInfo userNameUser = this.userInfoMapper.selectByUserName(userName);
		if(null != userNameUser){
			throw new BusinessException("该用户名已经存在!");
		}
		//获取系统设置
		SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
		// 校验邮箱验证码
		emailCodeService.checkEmailCode(email, emailCode);
		String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
		userInfo = new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setUserName(userName);
		userInfo.setEmail(email);
		userInfo.setPassword(StringTools.MD5(password));
		userInfo.setCreateTime(new Date());
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setUserSpace(0L);
		userInfo.setTotalSpace(sysSettingDto.getUserInitUseSpace() * Constants.MB);
		this.userInfoMapper.insert(userInfo);
	}

	/**
	 * 登录
	 * @param email
	 * @param password
	 * @return
	 */
	@Override
	public SessionWebUserDto login(String email, String password) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		// 此处无需MD5比对是因为前端传来的数据是已经MD5过的
		if(null == userInfo || !userInfo.getPassword().equals(password)){
			throw new BusinessException("邮箱或密码错误!");
		}
		if(UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())){
			throw new BusinessException("该用户已被禁用!");
		}
		UserInfo updateUserInfo = new UserInfo();
		updateUserInfo.setLastLoginTime(new Date());
		this.userInfoMapper.updateByUserId(updateUserInfo, userInfo.getUserId());

		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setUserName(userInfo.getUserName());
		sessionWebUserDto.setUserId(userInfo.getUserId());
		sessionWebUserDto.setEmail(userInfo.getEmail());
		// 判断是否是管理员
		sessionWebUserDto.setIsAdmin(ArrayUtils.contains(appConfig.getAdminEmails().split(","), email));
		// 用户空间
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		// 获取当前已经使用空间
		Long useSpace = fileInfoMapper.selectUseSpace(userInfo.getUserId());
		userSpaceDto.setUseSpace(useSpace);
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpaceUsed(userInfo.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}

	/**
	 * 修改密码
	 * @param email
	 * @param password
	 * @param emailCode
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPassword(String email, String password, String emailCode) {
		UserInfo userInfo  = this.userInfoMapper.selectByEmail(email);
		if(null == userInfo){
			throw new BusinessException("该邮箱不存在!");
		}
		emailCodeService.checkEmailCode(email, emailCode);
		UserInfo updateUserInfo = new UserInfo();
		updateUserInfo.setPassword(StringTools.MD5(password));
		this.userInfoMapper.updateByEmail(updateUserInfo, email);
	}

	/**
	 * QQ登录
	 * @param code
	 * @return
	 */
	@Override
	public SessionWebUserDto qqLogin(String code) {
		// 通过回调code 获取accessToken
		String accessToken = getQQAccessToken(code);
		// 通过accessToken获取openId
		String qqOpenId = getQQOpenId(accessToken);
		UserInfo user = this.userInfoMapper.selectByQqOpenId(qqOpenId);
		String avatar = null;
		// 第一次使用qq登陆的用户(注册)
		// 通过openId获取用户信息
		if(null == user){
			// 获取QQ用户信息
			QQInfoDto qqInfoDto = getQQUserInfo(accessToken, qqOpenId);
			// 插入用户信息
			user = new UserInfo();
			String userName = qqInfoDto.getNickName();
			userName = userName.length() > Constants.LENGTH_20 ? userName.substring(0, Constants.LENGTH_20) : userName;
			avatar = StringTools.isEmpty(qqInfoDto.getFigureUrl_qq_2()) ? qqInfoDto.getFigureUrl_qq_1() : qqInfoDto.getFigureUrl_qq_2();
			Date curDate = new Date();

			user.setQqOpenId(qqOpenId);
			user.setCreateTime(curDate);
			user.setUserName(userName);
			user.setQqAvatar(avatar);
			user.setUserId(StringTools.getRandomString(Constants.LENGTH_10));
			user.setLastLoginTime(curDate);
			user.setStatus(UserStatusEnum.ENABLE.getStatus());
			user.setUserSpace(0L);
			user.setTotalSpace(redisComponent.getSysSettingDto().getUserInitUseSpace() * Constants.MB);
			this.userInfoMapper.insert(user);
			user = this.userInfoMapper.selectByQqOpenId(qqOpenId);
		} else {
			UserInfo updateInfo = new UserInfo();
			updateInfo.setLastLoginTime(new Date());
			avatar = user.getQqAvatar();
			this.userInfoMapper.updateByQqOpenId(updateInfo, qqOpenId);
		}
		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setUserId(user.getUserId());
		sessionWebUserDto.setUserName(user.getUserName());
		sessionWebUserDto.setAvatar(avatar);
        sessionWebUserDto.setIsAdmin(ArrayUtils.contains(appConfig.getAdminEmails().split(","), user.getEmail() == null ? "" : user.getEmail()));

		UserSpaceDto userSpaceDto = new UserSpaceDto();
		// 获取用户已经使用空间
		Long useSpace = fileInfoMapper.selectUseSpace(user.getUserId());
		userSpaceDto.setUseSpace(useSpace);
		userSpaceDto.setTotalSpace(user.getTotalSpace());
		redisComponent.saveUserSpaceUsed(user.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}

	private String getQQAccessToken(String code) {
		String accessToken = null;
		String url = null;
		try {
			url = String.format(appConfig.getQqUrlAuthorization(), appConfig.getQqUrlOpenid(), appConfig.getQqAppKey(), code, URLEncoder.encode(appConfig.getQqUrlRedirect(), "utf-8"));
		} catch (UnsupportedEncodingException e){
			log.error("获取QQ授权失败(encode)", e);
		}
		String tokenResult = OKHttpUtils.getRequest(url);
		if(tokenResult == null || tokenResult.contains(Constants.VIEW_OBJ_RESULT_KEY)){
			log.error("获取QQToken授权失败");
			throw new BusinessException("获取QQ授权失败");
		}
		String[] params = tokenResult.split("&");
		for (String param : params) {
			if (param.contains("access_token")) {
				accessToken = param.split("=")[1];
				break;
			}
		}
		return accessToken;
	}

	private String getQQOpenId(String accessToken) {
		//  获取QQopenId
		String url = String.format(appConfig.getQqUrlOpenid(), accessToken);
		String openIdResult = OKHttpUtils.getRequest(url);
		String tmpJson = this.getQQResp(openIdResult);
		if(tmpJson == null){
			log.error("获取QQOpenId失败");
			throw new BusinessException("获取QQOpenId失败");
		}
		Map jsonData = JsonUtils.convertJson2Obj(tmpJson, Map.class);
		if(jsonData == null || jsonData.containsKey(Constants.VIEW_OBJ_RESULT_KEY)) {
			log.error("获取QQOpenId失败,jsonData:{}", jsonData);
			throw new BusinessException("获取QQOpenId失败");
		}
		return String.valueOf(jsonData.get("openid"));
	}

	private String getQQResp(String result) {
		if(StringUtils.isNotBlank(result)) {
			int pos = result.indexOf("callback");
			if (pos != -1) {
				int start = result.indexOf("(");
				int end = result.lastIndexOf(")");
				return result.substring(start + 1, end - 1);
			}
		}
		return null;
	}

	private QQInfoDto getQQUserInfo(String accessToken, String qqopenId) throws BusinessException{
		String url = String.format(appConfig.getQqUrlUserinfo(), accessToken, appConfig.getQqAppId(),qqopenId);
		String response = OKHttpUtils.getRequest(url);
		if(StringUtils.isNotBlank(response)){
			QQInfoDto qqInfo = JsonUtils.convertJson2Obj(response, QQInfoDto.class);
			if(qqInfo.getRet() != 0){
				log.error("qqInfo:{}", response);
				throw new BusinessException("获取QQ用户信息失败");
			}
			return qqInfo;
		}
		throw new BusinessException("获取QQ用户信息失败");
	}

	/**
	 * 修改用户状态
	 * @param userId
	 * @param status
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserStatus(String userId, Integer status) {
		UserInfo userInfo = new UserInfo();
		userInfo.setStatus(status);
		// 若用户被禁用 则将用户空间设置为0 删除其存储的文件
		if(UserStatusEnum.DISABLE.getStatus().equals(status)){
			userInfo.setUserSpace(0L);
			fileInfoMapper.deleteFileByUserId(userId);
		}
		userInfoMapper.updateByUserId(userInfo, userId);
	}

	/**
	 * 更新用户使用空间
	 * @param userId
	 * @param useSpace
	 */
	@Override
	public void updateUserSpace(String userId, Long useSpace) {
		Integer count = userInfoMapper.updateUserSpace(userId, useSpace, null);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		UserSpaceDto spaceDto = redisComponent.getUserSpaceUsed(userId);
		spaceDto.setUseSpace(spaceDto.getUseSpace() + useSpace);
		redisComponent.saveUserSpaceUsed(userId, spaceDto);
	}

	/**
	 * 设置用户总空间
	 * @param userId
	 * @param totalSpace 新的总空间大小（GB）
	 */
	@Override
	public void setUserTotalSpace(String userId, Long totalSpace) {
		// 获取用户当前信息
		UserInfo userInfo = userInfoMapper.selectByUserId(userId);
		if (userInfo == null) {
			throw new BusinessException("用户不存在");
		}
		// 计算当前已使用空间（GB）
		Long usedSpaceGB = (long) Math.ceil(userInfo.getUserSpace() * 1.0 / Constants.GB);
		// 验证新空间不小于已使用空间
		if (totalSpace < usedSpaceGB) {
			throw new BusinessException("新空间大小不能小于当前已使用空间(" + usedSpaceGB + "GB)");
		}
		// 计算新的总空间（字节）
		Long newTotalSpace = totalSpace * Constants.GB;
		// 更新用户空间
		this.userInfoMapper.setUserTotalSpace(userId, newTotalSpace);
		redisComponent.resetUserSpaceUsed(userId);
	}
}