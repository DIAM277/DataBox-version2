package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.config.AppConfig;
import com.databox.entity.dto.CreateImageCode;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.UserSpaceDto;
import com.databox.entity.enums.VerifyRegexEnum;
import com.databox.entity.po.UserInfo;
import com.databox.entity.vo.ResponseVO;
import com.databox.exception.BusinessException;
import com.databox.service.EmailCodeService;
import com.databox.service.UserInfoService;
import com.databox.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息表 Controller
 */
@RestController("accountController")
@Slf4j
public class AccountController extends ABaseController {
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json:charset=UTF-8";

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	// 获取网页验证码
	@RequestMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
		CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		String code = vCode.getCode();
		// 类型：0登录注册 1邮箱验证码 默认为0
		if (null == type || type == 0) {
			session.setAttribute(Constants.CHECK_CODE_KEY, code);
		} else {
			session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
		}
		vCode.write(response.getOutputStream());
		log.info("生成验证码：{}", code);
	}

	// 发送邮箱验证码
	@RequestMapping("/sendEmailCode")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO sendEmailCode(HttpSession session,
									@VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
									@VerifyParam(required = true) String checkCode,
									@VerifyParam(required = true) Integer type) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
				throw new BusinessException("图片验证码不正确!");
			}
			emailCodeService.sendEmailCode(email, type);
			return getSuccessResponseVO(null);
		} finally {
			// 重置验证码
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		}
	}

	@RequestMapping("/register")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO register(HttpSession session,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
							   @VerifyParam(required = true) String userName,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, max = 18, min = 8) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确!");
			}
			userInfoService.register(email, userName, password, emailCode);
			return getSuccessResponseVO(null);
		} finally {
			// 重置验证码
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	/**
	 * 登录
	 *
	 * @param session
	 * @param email
	 * @param password
	 * @param checkCode
	 * @return
	 */
	@RequestMapping("/login")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO login(HttpSession session,
							@VerifyParam(required = true) String email,
							@VerifyParam(required = true) String password,
							@VerifyParam(required = true) String checkCode) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确!");
			}
			SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
			session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
			return getSuccessResponseVO(sessionWebUserDto);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	/**
	 * 修改密码
	 *
	 * @param session
	 * @param email
	 * @param password
	 * @param checkCode
	 * @param emailCode
	 * @return
	 */
	@RequestMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO resetPwd(HttpSession session,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, max = 18, min = 8) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确!");
			}
			userInfoService.resetPassword(email, password, emailCode);
			return getSuccessResponseVO(null);
		} finally {
			// 重置验证码
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	/**
	 * 通过邮箱验证码修改密码
	 * @param session
	 * @param emailCode
	 * @param password
	 * @return
	 */
	@RequestMapping("/updatePasswordByEmailCode")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updatePasswordByEmailCode(HttpSession session,
												@VerifyParam(required = true) String emailCode,
												@VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, max = 18, min = 8) String password) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		String email = sessionWebUserDto.getEmail();

		if (!emailCodeService.verifyEmailCode(email, emailCode)) {
			throw new BusinessException("邮箱验证码不正确或已过期");
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setPassword(StringTools.MD5(password));
		userInfoService.updateUserInfoByUserId(userInfo, sessionWebUserDto.getUserId());

		return getSuccessResponseVO(null);
	}


	/**
	 * 获取用户头像
	 *
	 * @param response
	 * @param userId
	 */
	@RequestMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) {
		String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
		File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		// 完整路径
		String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
		File file = new File(avatarPath);
		if (!file.exists()) {//头像文件不存在 输出默认头像
			if (!new File(appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFAULT).exists()) {
				printNoDefaultImage(response);
			}
			avatarPath = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFAULT;
		}
		readFile(response, avatarPath);
		response.setContentType("image/jpg");
	}

	// 输出默认头像提示
	private void printNoDefaultImage(HttpServletResponse response) {
		response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		try (PrintWriter writer = response.getWriter()) {
			writer.print("no default avatar");
			writer.close();
		} catch (Exception e) {
			log.error("输出默认头像失败", e);
		}
	}

	/**
	 * 获取用户空间
	 *
	 * @param session
	 * @return
	 */
	@RequestMapping("/getUseSpace")
	@GlobalInterceptor
	public ResponseVO getUseSpace(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUsed(sessionWebUserDto.getUserId());
		return getSuccessResponseVO(userSpaceDto);
	}

	/**
	 * 退出登录
	 *
	 * @param session
	 * @return
	 */
	@RequestMapping("/logout")
	public ResponseVO logout(HttpSession session) {
		session.invalidate();
		return getSuccessResponseVO(null);
	}

	/**
	 * 上传/修改头像
	 *
	 * @param session
	 * @param avatar
	 * @return
	 */
	@RequestMapping("/updateUserAvatar")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar, String newUserName) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		// 用户名变更时才更新
		if(!webUserDto.getUserName().equals(newUserName)){
			UserInfo userInfo = new UserInfo();
			userInfo.setUserName(newUserName);
			userInfoService.updateUserInfoByUserId(userInfo, webUserDto.getUserId());
		}
		String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
		File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
		if (!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);
		try {
			avatar.transferTo(targetFile);
		} catch (Exception e) {
			log.error("保存用户头像失败", e);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setQqAvatar("");
		userInfoService.updateUserInfoByUserId(userInfo, webUserDto.getUserId());
		webUserDto.setAvatar(null);
		session.setAttribute(Constants.SESSION_KEY, webUserDto);
		return getSuccessResponseVO(null);
	}

	/**
	 * 修改密码
	 *
	 * @param session
	 * @param oldPassword 旧密码
	 * @param newPassword 新密码
	 * @return
	 */
	@RequestMapping("/updatePassword")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updatePassword(HttpSession session,
									 @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, max = 18, min = 8) String oldPassword,
									 @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, max = 18, min = 8) String newPassword) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		// 获取当前用户信息
		UserInfo userInfo = userInfoService.getUserInfoByUserId(sessionWebUserDto.getUserId());
		// 校验旧密码是否正确
		if (!userInfo.getPassword().equals(StringTools.MD5(oldPassword))) {
			throw new BusinessException("旧密码不正确！");
		}
		// 更新新密码
		userInfo.setPassword(StringTools.MD5(newPassword));
		userInfoService.updateUserInfoByUserId(userInfo, sessionWebUserDto.getUserId());
		return getSuccessResponseVO(null);
	}


	/**
	 * qq登录接口
	 * @param session
	 * @param callbackUrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/qqlogin")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO qqlogin(HttpSession session, String callbackUrl) throws UnsupportedEncodingException {
		String state = StringTools.getRandomNumber(Constants.LENGTH_30);
		if (!StringTools.isEmpty(callbackUrl)) {
			session.setAttribute(state, callbackUrl);
		}
		String url = String.format(appConfig.getQqUrlAuthorization(), appConfig.getQqAppId(), URLEncoder.encode(appConfig.getQqUrlRedirect(), "utf-8"), state);
		return getSuccessResponseVO(url);
	}

	/**
	 * QQ登录回调
	 * @param session
	 * @param code
	 * @param state
	 * @return
	 */
	@RequestMapping("/qqlogin/callback")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO qqloginCallback(HttpSession session, @VerifyParam(required = true) String code, @VerifyParam(required = true) String state){
		SessionWebUserDto sessionWebUserDto = userInfoService.qqLogin(code);
		session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
		Map<String, Object> result = new HashMap<>();
		result.put("callbackUrl", session.getAttribute(state));
		result.put("userInfo", sessionWebUserDto);
		return getSuccessResponseVO(result);
	}
}