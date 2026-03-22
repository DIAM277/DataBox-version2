package com.databox.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.enums.ShareReportEnum;
import com.databox.exception.BusinessException;
import com.databox.service.FileShareService;
import org.springframework.stereotype.Service;

import com.databox.entity.enums.PageSize;
import com.databox.entity.query.ShareReportQuery;
import com.databox.entity.po.ShareReport;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.ShareReportMapper;
import com.databox.service.ShareReportService;
import com.databox.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 分享违规举报表 业务接口实现
 */
@Service("shareReportService")
public class ShareReportServiceImpl implements ShareReportService {

	@Resource
	private ShareReportMapper<ShareReport, ShareReportQuery> shareReportMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private FileShareService fileShareService;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ShareReport> findListByParam(ShareReportQuery param) {
		return this.shareReportMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ShareReportQuery param) {
		return this.shareReportMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ShareReport> findListByPage(ShareReportQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ShareReport> list = this.findListByParam(param);
		PaginationResultVO<ShareReport> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ShareReport bean) {
		return this.shareReportMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ShareReport> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.shareReportMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ShareReport> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.shareReportMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ShareReport bean, ShareReportQuery param) {
		StringTools.checkParam(param);
		return this.shareReportMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ShareReportQuery param) {
		StringTools.checkParam(param);
		return this.shareReportMapper.deleteByParam(param);
	}

	/**
	 * 根据ReportId获取对象
	 */
	@Override
	public ShareReport getShareReportByReportId(Integer reportId) {
		return this.shareReportMapper.selectByReportId(reportId);
	}

	/**
	 * 根据ReportId修改
	 */
	@Override
	public Integer updateShareReportByReportId(ShareReport bean, Integer reportId) {
		return this.shareReportMapper.updateByReportId(bean, reportId);
	}

	/**
	 * 根据ReportId删除
	 */
	@Override
	public Integer deleteShareReportByReportId(Integer reportId) {
		return this.shareReportMapper.deleteByReportId(reportId);
	}

	/**
	 * 用户提交举报信息
	 * @param shareId
	 * @param fileId
	 * @param userId
	 * @param ip
	 * @param reason
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submitReport(String shareId, String fileId, String userId, String ip, String reason) {
		// 1. 未登录用户的 24 小时绝对限流
		if (userId == null) {
			String ipLimitKey = Constants.REDIS_KEY_REPORT_IP_LIMIT + shareId + ":" + fileId + ":" + ip;
			if (redisComponent.hasKey(ipLimitKey)) {
				// 提示信息
				throw new BusinessException("您在24小时内已对该文件提交过举报，请勿频繁操作");
			}
		}

		// 2. 查询是否存在「待处理(0)」的举报
		ShareReportQuery query = new ShareReportQuery();
		query.setShareId(shareId);
		query.setFileId(fileId);
		query.setStatus(ShareReportEnum.PENDING.getStatus()); // 仅查待处理0
		if (userId != null) {
			query.setReportUserId(userId);
		} else {
			query.setReportIp(ip);
			query.setReportUserId("guest"); // 标明是游客
		}

		Integer pendingCount = this.shareReportMapper.selectCount(query);
		if (pendingCount > 0) {
			throw new BusinessException("您已提交过违规举报，管理员正在快马加鞭审核中，请耐心等待~");
		}

		// 3. 通过所有校验，入库
		ShareReport report = new ShareReport();
		report.setShareId(shareId);
		report.setFileId(fileId);
		report.setReportUserId(userId != null ? userId : "guest");
		report.setReportIp(ip);
		report.setReason(reason);
		report.setStatus(ShareReportEnum.PENDING.getStatus()); // 待处理
		report.setCreateTime(new Date());
		this.shareReportMapper.insert(report);

		// 4. 未登录用户，写入 Redis 24 小时限制
		if (userId == null) {
			String ipLimitKey = Constants.REDIS_KEY_REPORT_IP_LIMIT + shareId + ":" + fileId + ":" + ip;
			// 设置 24 小时过期 (24 * 60 * 60 秒)
			redisComponent.setex(ipLimitKey, "1", Constants.REDIS_KEY_EXPIRE_TIME_DAY);
		}
	}

	/**
	 * 管理员处理举报信息
	 * @param reportId
	 * @param status
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void processReport(Integer reportId, Integer status) {
		// 1. 查询举报记录
		ShareReport report = this.shareReportMapper.selectByReportId(reportId);
		if (report == null) {
			throw new BusinessException("举报记录不存在");
		}
		if (!ShareReportEnum.PENDING.getStatus().equals(report.getStatus())) {
			throw new BusinessException("该举报已处理，请勿重复操作");
		}

		// 2. 更新举报表状态
		ShareReport updateInfo = new ShareReport();
		updateInfo.setStatus(status);
		this.shareReportMapper.updateByReportId(updateInfo, reportId);

		// 3. 业务联动：如果管理员判定违规并封禁 (status = 1)
		if (ShareReportEnum.BAN.getStatus().equals(status)) {
			try {
				// 直接删除该分享记录（强行失效）
				fileShareService.deleteFileShareBatch(new String[]{report.getShareId()}, null);
				// TODO：调用 sysMessageService 发一条消息通知分享人：您的分享因违规已被强制取消。
			} catch (Exception e) {
				// 防止由于分享记录已经被原主人主动删除而导致的异常，仅打印日志
				System.out.println("删除违规分享失败（可能已被原分享者删除）: " + e.getMessage());
			}
		}
	}
}