package com.databox.service.impl;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.config.AppConfig;
import com.databox.entity.dto.SysSettingDto;
import com.databox.entity.po.UserInfo;
import com.databox.entity.query.UserInfoQuery;
import com.databox.exception.BusinessException;
import com.databox.mappers.UserInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.databox.entity.enums.PageSize;
import com.databox.entity.query.EmailCodeQuery;
import com.databox.entity.po.EmailCode;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.EmailCodeMapper;
import com.databox.service.EmailCodeService;
import com.databox.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 邮箱验证码表 业务接口实现
 */
@Slf4j
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

	@Resource
	private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private JavaMailSender javaMailSender;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<EmailCode> findListByParam(EmailCodeQuery param) {
		return this.emailCodeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(EmailCodeQuery param) {
		return this.emailCodeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<EmailCode> list = this.findListByParam(param);
		PaginationResultVO<EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(EmailCode bean) {
		return this.emailCodeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<EmailCode> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<EmailCode> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(EmailCode bean, EmailCodeQuery param) {
		StringTools.checkParam(param);
		return this.emailCodeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(EmailCodeQuery param) {
		StringTools.checkParam(param);
		return this.emailCodeMapper.deleteByParam(param);
	}

	/**
	 * 根据EmailAndCode获取对象
	 */
	@Override
	public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.selectByEmailAndCode(email, code);
	}

	/**
	 * 根据EmailAndCode修改
	 */
	@Override
	public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
		return this.emailCodeMapper.updateByEmailAndCode(bean, email, code);
	}

	/**
	 * 根据EmailAndCode删除
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.deleteByEmailAndCode(email, code);
	}

	/**
	 * 发送邮箱验证码
	 * @param email
	 * @param type
	 */
	@Override
	public void sendEmailCode(String email, Integer type) {
		// 注册验证码
		if(type == Constants.ZERO){
			UserInfo userInfo = userInfoMapper.selectByEmail(email);
			if(null != userInfo){
				throw new RuntimeException("邮箱已经存在!");
			}
		}
		String code = StringTools.getRandomNumber(Constants.CHECK_CODE_LENGTH_6);

		// 发送邮箱验证码
		sendEmailCode(email, code);

		// 重置原有的验证码
		emailCodeMapper.disableEmailCode(email);
		EmailCode emailCode = new EmailCode();
		emailCode.setEmail(email);
		emailCode.setCode(code);
		emailCode.setStatus(Constants.ZERO); //状态未使用
		emailCode.setCreateTime(new Date());
		emailCodeMapper.insert(emailCode);
	}

	// 发送邮箱验证码DO
	private void sendEmailCode(String email, String code){
		try{
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(appConfig.getSendUserName());
			helper.setTo(email);
			SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
			// 设置邮件标题
			helper.setSubject(sysSettingDto.getRegisterEmailTitle());
			// 设置邮件内容
			helper.setText(String.format(sysSettingDto.getRegisterEmailContent(), code));
			// 设置发送时间
			helper.setSentDate(new Date());
			javaMailSender.send(message);
		}catch (Exception e){
			log.error("发送邮件失败", e);
			throw new RuntimeException("发送邮件失败");
		}
	}

	/**
	 * 验证邮箱验证码
	 * @param email
	 * @param code
	 */
	@Override
	public void checkEmailCode(String email, String code) {
		EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
		if(null == emailCode){
			throw new BusinessException("邮箱验证码错误!");
		}
		// 校验验证码是否已使用或者过期
		if(emailCode.getStatus() == 1 || System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15 * 1000 * 60){
			throw new BusinessException("邮箱验证码已失效!");
		}
		emailCodeMapper.disableEmailCode(email);
	}

	/**
	 * 校验邮箱验证码
	 * @param email
	 * @param code
	 * @return
	 */
	@Override
	public boolean verifyEmailCode(String email, String code) {
		EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
		if (emailCode == null) {
			return false;
		}
		// 判断是否已使用或过期（15分钟有效期）
		if (emailCode.getStatus() == Constants.ZERO &&
				System.currentTimeMillis() - emailCode.getCreateTime().getTime() <= Constants.LENGTH_15 * 60 * 1000) {
			// 标记为已使用
			emailCodeMapper.disableEmailCode(email);
			return true;
		}
		return false;
	}
}