package com.databox.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.config.AppConfig;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.UploadResultDto;
import com.databox.entity.dto.UserSpaceDto;
import com.databox.entity.enums.*;
import com.databox.entity.po.UserInfo;
import com.databox.entity.query.UserInfoQuery;
import com.databox.exception.BusinessException;
import com.databox.mappers.UserInfoMapper;
import com.databox.service.UserInfoService;
import com.databox.utils.DateUtil;
import com.databox.utils.ProcessUtils;
import com.databox.utils.ScaleFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.po.FileInfo;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.query.SimplePage;
import com.databox.mappers.FileInfoMapper;
import com.databox.service.FileInfoService;
import com.databox.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息记录表 业务接口实现
 */
@Service("fileInfoService")
@Slf4j
public class FileInfoServiceImpl implements FileInfoService {

	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	@Lazy
	private FileInfoServiceImpl fileInfoService;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<FileInfo> findListByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<FileInfo> list = this.findListByParam(param);
		PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<FileInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<FileInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(FileInfo bean, FileInfoQuery param) {
		StringTools.checkParam(param);
		return this.fileInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(FileInfoQuery param) {
		StringTools.checkParam(param);
		return this.fileInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据FileIdAndUserId获取对象
	 */
	@Override
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
	}

	/**
	 * 根据FileIdAndUserId修改
	 */
	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
	}

	/**
	 * 根据FileIdAndUserId删除
	 */
	@Override
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
	}

	/**
	 * 文件上传
	 * @param webUserDto
	 * @param fileId
	 * @param file
	 * @param fileName
	 * @param filePid
	 * @param fileMd5
	 * @param chunkIndex
	 * @param chunk
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunk) {
		UploadResultDto resultDto = new UploadResultDto();
		Boolean uploadSuccess = true;
		File tempFileFolder = null;
		try{
			if(StringTools.isEmpty(fileId)){
				fileId = StringTools.getRandomString(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date curDate = new Date();
			// 获取用户空间使用情况
			UserSpaceDto spaceDto = this.redisComponent.getUserSpaceUsed(webUserDto.getUserId());
			if(chunkIndex == 0) {
				FileInfoQuery infoQuery = new FileInfoQuery();
				infoQuery.setFileMd5(fileMd5);
				infoQuery.setSimplePage(new SimplePage(0, 1));
				infoQuery.setStatus(FileStatusEnum.USING.getStatus());
				List<FileInfo> dbFileList = this.fileInfoMapper.selectList(infoQuery);
				// 如果数据库中存在该文件，则进行秒传
				if (!dbFileList.isEmpty()) {
					FileInfo dbFile = dbFileList.get(0);
					// 判断文件大小情况
					if (dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
						throw new BusinessException(ResponseCodeEnum.CODE_904);
					}
					dbFile.setFileId(fileId);
					dbFile.setFilePid(filePid);
					dbFile.setUserId(webUserDto.getUserId());
					dbFile.setLastUpdateTime(curDate);
					dbFile.setStatus(FileStatusEnum.USING.getStatus());
					dbFile.setDelFlag(FileDelFlagEnum.USING.getFlag());
					dbFile.setFileMd5(fileMd5);
					// 文件重命名
					fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
					dbFile.setFileName(fileName);
					this.fileInfoMapper.insert(dbFile);
					resultDto.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getCode());
					// 实时更新用户剩余空间
					updateUserSpace(webUserDto, dbFile.getFileSize());
					return resultDto;
				}
			}
			//判断磁盘空间
			Long currentTempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			// 空间大小不足(当前上传文件+当前上传文件临时文件+已使用空间 > 总空间)
			if(file.getSize() + currentTempSize + spaceDto.getUseSpace() > spaceDto.getTotalSpace()){
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}
			// 暂存临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			tempFileFolder = new File(tempFolderName + "/" + currentUserFolderName);
			if(!tempFileFolder.exists()){
				tempFileFolder.mkdirs();
			}
			File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
			file.transferTo(newFile);
			// 保存临时大小
			redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());
			if(chunkIndex < chunk - 1){
				resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
				return resultDto;
			}
			// 最后一个分片上传完成 记录数据库 异步合并分片
			String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM.getPattern());
			String fileSuffix = StringTools.getFileSuffix(fileName);
			// 真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			// 根据文件后缀获取文件类型
			FileTypeEnum fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			// 文件自动重命名
			fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFileId(fileId);
			fileInfo.setUserId(webUserDto.getUserId());
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month + "/" + realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(curDate);
			fileInfo.setLastUpdateTime(curDate);
			fileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnum.getType());
			fileInfo.setStatus(FileStatusEnum.TRANSFER.getStatus());
			fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
			fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
			this.fileInfoMapper.insert(fileInfo);

			// 获取文件总空间
			Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			updateUserSpace(webUserDto, totalSize);
			resultDto.setStatus(UploadStatusEnum.UPLOAD_FINISH.getCode());

			// 添加事务监听器
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit(){
					fileInfoService.transferFile(fileInfo.getFileId(), webUserDto);
				}
			});

			return resultDto;
		}catch (BusinessException e){
			log.error("文件上传失败", e);
			uploadSuccess = false;
			throw e;
		} catch (Exception e){
			log.error("文件上传失败", e);
			uploadSuccess = false;
		}finally {
			if(!uploadSuccess && tempFileFolder != null){
				try {
					FileUtils.deleteDirectory(tempFileFolder);
				} catch (IOException e) {
					log.error("删除临时文件失败", e);
				}
			}
		}
		return resultDto;
	}

	//文件重命名方法
	private String autoRename(String filePid, String userId, String fileName){
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		fileInfoQuery.setFileName(fileName);
		Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
		// 根据上述条件查询符合条件的文件 如果查询到的结果大于0 即说明存在与当前上传文件相同的文件存在 进行重命名处理
		if(count > 0){
			fileName = StringTools.rename(fileName);
		}
		return fileName;
	}

	// 更新用户空间使用情况
	private void updateUserSpace(SessionWebUserDto webUserDto, Long useSpace){
		Integer count = userInfoMapper.updateUserSpace(webUserDto.getUserId(), useSpace,null);
		if(count == 0){
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		UserSpaceDto spaceDto = redisComponent.getUserSpaceUsed(webUserDto.getUserId());
		spaceDto.setUseSpace(spaceDto.getUseSpace() + useSpace);
		redisComponent.saveUserSpaceUsed(webUserDto.getUserId(), spaceDto);
	}

	// 文件转码
	@Async
	public void transferFile(String fileId, SessionWebUserDto webUserDto){
		Boolean transferSuccess = true;
		String targetFilePath = null;
		String cover = null;
		FileTypeEnum  fileTypeEnum = null;
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDto.getUserId());
		try{
			if(fileInfo == null || !FileStatusEnum.TRANSFER.getStatus().equals(fileInfo.getStatus())){
				return;
			}
			// 临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			File fileFolder = new File(tempFolderName + "/" + currentUserFolderName);
			String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
			String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM.getPattern());
			// 目标目录
			String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
			File targetFileFolder = new File(targetFolderName + "/" + month);
			if(!targetFileFolder.exists()){
				targetFileFolder.mkdirs();
			}
			// 真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			targetFilePath  = targetFileFolder.getPath() + "/" + realFileName;
			// 合并文件
			union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
			// 视频文件切割并生成缩略图 图片文件仅生成缩略图
			fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			if(FileTypeEnum.VIDEO.equals(fileTypeEnum)){
				cutFileForVideo(fileId, targetFilePath);
				// 生成视频缩略图
				cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
				String coverPath = targetFolderName + "/" + cover;
				ScaleFilter.createCoverForVideo(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
			} else if(FileTypeEnum.IMAGE.equals(fileTypeEnum)){
				// 生成缩略图
				cover = month + "/" + realFileName.replace(".", "_.");
				String coverPath = targetFolderName + "/" + cover;
				Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
				if(!created){
					FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
				}
			}
		}catch (Exception e){
			log.error("文件转码失败,文件:{}, userId:{}", fileId, webUserDto.getUserId(),e);
			transferSuccess = false;
		}finally {
			// 更新文件状态
			FileInfo updateInfo = new FileInfo();
			updateInfo.setFileSize(new File(targetFilePath).length());
			updateInfo.setFileCover(cover);
			updateInfo.setStatus(transferSuccess ? FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFER_FAIL.getStatus());
			fileInfoMapper.updateFileStatusWithOldStatus(fileId, webUserDto.getUserId(), updateInfo, FileStatusEnum.TRANSFER.getStatus());
		}
	}

	/**
	 * 合并分片
	 * @param dirPath
	 * @param toFilePath
	 * @param fileName
	 * @param delSource
	 */
	private void union(String dirPath, String toFilePath, String fileName, Boolean delSource){
		File dir = new File(dirPath);
		if(!dir.exists()){
			throw new BusinessException("目录不存在，请检查！");
		}
		File[] fileList = dir.listFiles();
		File targetFile = new File(toFilePath);
		RandomAccessFile writeFile = null;
		try{
			writeFile = new RandomAccessFile(targetFile, "rw");
			byte[] b = new byte[1024 * 10];
			for(int i = 0; i < fileList.length; i++){
				int len = -1;
				File chunkFile = new File(dirPath + "/" + i);
				RandomAccessFile readFile = null;
				try{
					readFile = new RandomAccessFile(chunkFile, "r");
					while ((len = readFile.read(b)) != -1){
						writeFile.write(b, 0, len);
					}
				}catch (Exception e){
					log.error("合并文件失败", e);
					throw new BusinessException("合并文件失败");
				}finally {
					readFile.close();
				}
			}
		}catch (Exception e){
			log.error("合并文件{}失败", fileName, e);
			throw new BusinessException("合并文件" + fileName + "失败");
		}finally {
			if(null != writeFile){
				try{
					writeFile.close();
				}catch (IOException e){
					log.error("关闭文件失败", e);
				}
			}
			if(delSource && dir.exists()){
				try{
					FileUtils.deleteDirectory(dir);
				}catch (IOException e){
					log.error("删除源文件失败", e);
				}
			}
		}
	}

	/**
	 * 视频分割
	 * @param fileId
	 * @param videoFilePath
	 */
	private void cutFileForVideo(String fileId, String videoFilePath){
		// 创建同名的切片目录
		File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
		if(!tsFolder.exists()){
			tsFolder.mkdir();
		}
		final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
		final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
		String tsPath = tsFolder + "/" + Constants.TS_NAME;
		// 生成TS文件
		String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
		ProcessUtils.executeCommand(cmd, false);
		// 生成索引文件.m3u8 和 切片.ts文件
		cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
		ProcessUtils.executeCommand(cmd, false);
		// 删除index.ts文件
		File tsFile = new File(tsPath);
		if(tsFile.exists()){
			tsFile.delete();
		}
	}

	/**
	 * 新建文件夹
	 * @param filePid
	 * @param userId
	 * @param folderName
	 * @return
	 */
	@Override
	public FileInfo newFolder(String filePid, String userId, String folderName) {
		checkFileName(filePid, userId, folderName, FileFolderTypeEnum.FOLDER.getType());
		Date curDate = new Date();
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
		fileInfo.setUserId(userId);
		fileInfo.setFilePid(filePid);
		fileInfo.setFileName(folderName);
		fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setStatus(FileStatusEnum.USING.getStatus());
		fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
		this.fileInfoMapper.insert(fileInfo);
		return fileInfo;
	}

	/**
	 * 校验文件名是否重复
	 * @param fileName
	 */
	private void checkFileName(String filePid, String userId, String fileName, Integer folderType){
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFolderType(folderType);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		Integer count = fileInfoMapper.selectCount(fileInfoQuery);
		if(count > 0){
			throw new BusinessException("当前目录下已存在同名文件，请修改文件名");
		}
	}

	/**
	 * 文件重命名
	 * @param fileId
	 * @param userId
	 * @param fileName
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileInfo reName(String fileId, String userId, String fileName) {
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
		if(null == fileInfo){
			throw new BusinessException("文件不存在");
		}
		String filePid = fileInfo.getFilePid();
		checkFileName(filePid, userId, fileName, fileInfo.getFolderType());
		// 获取文件后缀
		if(FileFolderTypeEnum.FILE.getType().equals(fileInfo.getFolderType())){
			fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
		}
		Date curDate = new Date();
		FileInfo dbInfo = new FileInfo();
		dbInfo.setFileName(fileName);
		dbInfo.setLastUpdateTime(curDate);
		this.fileInfoMapper.updateByFileIdAndUserId(dbInfo, fileId, userId);
		// 检查文件夹下文件名重复
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		Integer count = fileInfoMapper.selectCount(fileInfoQuery);
		if(count > 1){
			throw new BusinessException("文件名" + fileName + "已存在，请修改文件名");
		}
		fileInfo.setFileName(fileName);
		fileInfo.setLastUpdateTime(curDate);
		return fileInfo;
	}

	/**
	 * 文件移动到指定文件夹
	 * @param fileIds
	 * @param filePid
	 * @param userId
	 */
	@Override
	public void changeFileFolder(String fileIds, String filePid, String userId) {
		// 源文件不能移动到目标文件下
		if(fileIds.equals(filePid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(!Constants.ZERO_STR.equals(filePid)){
			FileInfo fileInfo = this.getFileInfoByFileIdAndUserId(filePid, userId);
			if(fileInfo == null || !FileDelFlagEnum.USING.getFlag().equals(fileInfo.getDelFlag())){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setFileId(filePid);
		query.setUserId(userId);
		List<FileInfo> dbFileList = this.findListByParam(query);
		Map<String, FileInfo> dbFileNameMap = dbFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) ->data2));
		// 查询选中文件
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		List<FileInfo> selectFileList = this.findListByParam(query);
		// 所选文件重命名
		for(FileInfo item : selectFileList){
			FileInfo rootFileInfo = dbFileNameMap.get(item.getFileName());
			// 新目录下已经存在同名文件 还原重命名
			FileInfo updateInfo = new FileInfo();
			if(rootFileInfo != null){
				String fileName = StringTools.rename(item.getFileName());
				updateInfo.setFileName(fileName);
			}
			updateInfo.setFilePid(filePid);
			this.fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
		}
	}

	/**
	 * 批量删除文件到回收站
	 * @param userId
	 * @param fileIds
	 */
	@Override
	public void removeFile2RecycleBinBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		if(fileInfoList.isEmpty()){
			return;
		}
		List<String> delFilePidList = new ArrayList<>();
		for(FileInfo fileInfo : fileInfoList){
			findAllSubFolderFileList(delFilePidList, userId, fileInfo.getFileId(), FileDelFlagEnum.USING.getFlag());
		}
		if(!delFilePidList.isEmpty()){
			FileInfo updateInfo = new FileInfo();
			updateInfo.setDelFlag(FileDelFlagEnum.DEL.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, delFilePidList, null, FileDelFlagEnum.USING.getFlag());
		}
		// 将选中的文件更新状态为回收站
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setRecoveryTime(new Date());
		fileInfo.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnum.USING.getFlag());
	}

	/**
	 * 递归查找所有子文件夹文件
	 * @param fileIdList
	 * @param userId
	 * @param fileId
	 * @param delFlag
	 */
	private void findAllSubFolderFileList(List<String> fileIdList, String userId, String fileId, Integer delFlag){
		fileIdList.add(fileId);
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(fileId);
		query.setDelFlag(delFlag);
		query.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		for(FileInfo fileInfo : fileInfoList){
			findAllSubFolderFileList(fileIdList, userId, fileInfo.getFileId(), delFlag);
		}
	}

	/**
	 * 批量恢复文件
	 * @param userId
	 * @param fileIds
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverFileBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		// 创建文件查询对象
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
		// 查询待恢复的文件列表
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		List<String> delFileSubFileFolderFileList = new ArrayList<>();
		// 遍历文件列表 查找所有子文件夹下的文件
		for(FileInfo fileInfo : fileInfoList){
			if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
				findAllSubFolderFileList(delFileSubFileFolderFileList, userId, fileInfo.getFileId(), FileDelFlagEnum.DEL.getFlag());
			}
		}
		// 查询所有根目录文件
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		query.setFilePid(Constants.ZERO_STR);
		List<FileInfo> allRootFileList = this.findListByParam(query);
		// 将根目录文件列表转换为Map 便于后续查找
		Map<String, FileInfo> rootFileMap = allRootFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) ->data2));
		// 查询所有所选文件 将目录下所有删除的文件更新为使用中
		if(!delFileSubFileFolderFileList.isEmpty()){
			FileInfo fileInfo = new FileInfo();
			fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, delFileSubFileFolderFileList, null, FileDelFlagEnum.DEL.getFlag());
		}
		// 将所选要恢复的文件恢复为正常使用状态 且添加到根目录
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
		fileInfo.setFilePid(Constants.ZERO_STR);
		fileInfo.setLastUpdateTime(new Date());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnum.RECYCLE.getFlag());
		// 将所选要恢复的文件重命名
		for(FileInfo item : fileInfoList){
			FileInfo rootFileInfo = rootFileMap.get(item.getFileName());
			// 新目录下已经存在同名文件 还原重命名
			if(rootFileInfo != null){
				String fileName = StringTools.rename(item.getFileName());
				FileInfo updateInfo = new FileInfo();
				updateInfo.setFileName(fileName);
				this.fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
			}
		}
	}

	/**
	 * 彻底删除文件
	 * @param userId
	 * @param fileIds
	 * @param adminOp
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileBatch(String userId, String fileIds, Boolean adminOp) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);

		// 收集需要检查引用计数的文件MD5
		Map<String, List<FileInfo>> md5FileMap = new HashMap<>();

		List<String> delFileSubFileFolderFileList = new ArrayList<>();
		// 找到所选文件子目录文件ID
		for(FileInfo fileInfo : fileInfoList){
			if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
				findAllSubFolderFileList(delFileSubFileFolderFileList, userId, fileInfo.getFileId(), FileDelFlagEnum.DEL.getFlag(), md5FileMap);
			} else if (!StringTools.isEmpty(fileInfo.getFileMd5())) {
				// 如果是文件，收集MD5
				md5FileMap.computeIfAbsent(fileInfo.getFileMd5(), k -> new ArrayList<>()).add(fileInfo);
			}
		}
		// 处理物理文件删除
		for (Map.Entry<String, List<FileInfo>> entry : md5FileMap.entrySet()) {
			String fileMd5 = entry.getKey();
			List<FileInfo> files = entry.getValue();
			// 查询当前引用计数
			FileInfo fileInfo = (FileInfo) this.fileInfoMapper.selectByFileMd5(fileMd5);
			// 对每个文件的MD5减少引用计数
			this.fileInfoMapper.updateReferenceCount(fileMd5, -files.size());

			// 如果引用计数为0或为null，则删除物理文件
			if (fileInfo != null && (fileInfo.getReferenceCount() == null || fileInfo.getReferenceCount() <= 0)) {
				// 获取文件路径
				String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
				File file = new File(filePath);
				if (file.exists()) {
					// 删除物理文件
					boolean deleted = file.delete();
					if (!deleted) {
						log.error("物理文件删除失败: {}", filePath);
					} else {
						log.info("物理文件已删除: {}", filePath);
					}
				}

				// 如果是视频文件，还需要删除转码后的文件夹
				if (FileCategoryEnum.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
					String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
					String videoFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileNameNoSuffix;
					File videoFolder = new File(videoFolderPath);
					if (videoFolder.exists() && videoFolder.isDirectory()) {
						try {
							FileUtils.deleteDirectory(videoFolder);
							log.info("视频转码文件夹已删除: {}", videoFolderPath);
						} catch (IOException e) {
							log.error("视频转码文件夹删除失败: {}", videoFolderPath, e);
						}
					}
				}
			}
		}

		// 删除所选文件 子目录文件
		if(!delFileSubFileFolderFileList.isEmpty()){
			this.fileInfoMapper.delFileBatch(userId, delFileSubFileFolderFileList, null, adminOp ? null : FileDelFlagEnum.DEL.getFlag());
		}
		// 删除所选文件
		this.fileInfoMapper.delFileBatch(userId, null, Arrays.asList(fileIdArray), adminOp ? null : FileDelFlagEnum.RECYCLE.getFlag());

		// 更新用户空间使用量
		Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
		UserInfo userInfo = new UserInfo();
		userInfo.setUserSpace(useSpace);
		this.userInfoMapper.updateByUserId(userInfo, userId);

		// 更新redis缓存信息
		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUsed(userId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpaceUsed(userId, userSpaceDto);
	}

	/**
	 * 递归查找所有子文件夹文件
	 * @param delFileSubFileFolderFileList
	 * @param userId
	 * @param fileId
	 * @param delFlag
	 * @param md5FileMap 收集需要检查引用计数的文件MD5
	 */
	private void findAllSubFolderFileList(List<String> delFileSubFileFolderFileList, String userId, String fileId, Integer delFlag, Map<String, List<FileInfo>> md5FileMap) {
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(fileId);
		query.setDelFlag(delFlag);
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		for (FileInfo fileInfo : fileInfoList) {
			if (FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) {
				findAllSubFolderFileList(delFileSubFileFolderFileList, userId, fileInfo.getFileId(), delFlag, md5FileMap);
			} else {
				delFileSubFileFolderFileList.add(fileInfo.getFileId());
				// 收集文件MD5
				if (!StringTools.isEmpty(fileInfo.getFileMd5())) {
					md5FileMap.computeIfAbsent(fileInfo.getFileMd5(), k -> new ArrayList<>()).add(fileInfo);
				}
			}
		}
	}




	/**
	 * 验证根目录文件pid
	 * @param rootFilePid
	 * @param userId
	 * @param fileId
	 */
	@Override
	public void checkRootFilePid(String rootFilePid, String userId, String fileId) {
		if(StringTools.isEmpty(fileId)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(rootFilePid.equals(fileId)){
			return ;
		}
		checkFilePid(rootFilePid, fileId, userId);
	}

	/**
	 * 递归验证文件pid
	 * @param rootFilePid
	 * @param fileId
	 * @param userId
	 */
	private void checkFilePid(String rootFilePid, String fileId, String userId){
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
		if(fileInfo == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(Constants.ZERO_STR.equals(fileInfo.getFilePid())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(fileInfo.getFilePid().equals(rootFilePid)){
			return ;
		}
		checkFilePid(rootFilePid, fileInfo.getFilePid(), userId);
	}

	/**
	 * 保存分享的文件到自己的网盘
	 * @param shareRootFilePid
	 * @param shareFileIds
	 * @param myFolderId
	 * @param shareUserId
	 * @param currentUserId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId) {
		String[] shareFileIdArray = shareFileIds.split(",");
		// 目标文件列表
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(currentUserId);
		fileInfoQuery.setFilePid(myFolderId);
		List<FileInfo> currentFileList = this.fileInfoMapper.selectList(fileInfoQuery);
		Map<String, FileInfo> currentFileMap = currentFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) ->data2));

		// 选择的文件
		fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(shareUserId);
		fileInfoQuery.setFileIdArray(shareFileIdArray);
		List<FileInfo> shareFileList = this.fileInfoMapper.selectList(fileInfoQuery);
		// 重命名选择的文件
		List<FileInfo> copyFileList = new ArrayList<>();
		Date curDate = new Date();

		// 收集所有需要增加引用计数的文件MD5
		Set<String> fileMd5Set = new HashSet<>();

		for(FileInfo item : shareFileList){
			FileInfo haveFile = currentFileMap.get(item.getFileName());
			if(haveFile != null){
				item.setFileName(StringTools.rename(item.getFileName()));
			}
			findAllSubFile(copyFileList, item, shareUserId, currentUserId, curDate, myFolderId, fileMd5Set);
		}
		this.fileInfoMapper.insertBatch(copyFileList);
		// 更新引用计数
		for (String fileMd5 : fileMd5Set) {
			if (!StringTools.isEmpty(fileMd5)) {
				int result = this.fileInfoMapper.updateReferenceCount(fileMd5, 1);
			}
		}
		// 计算新增文件的总大小并更新用户空间
		Long totalFileSize = 0L;
		for (FileInfo fileInfo : copyFileList) {
			if (fileInfo.getFileSize() != null) {
				totalFileSize += fileInfo.getFileSize();
			}
		}
		// 更新用户空间
		if (totalFileSize > 0) {
			userInfoService.updateUserSpace(currentUserId, totalFileSize);
		}
	}

	/**
	 * 递归查询子文件
	 * @param copyFileList
	 * @param fileInfo
	 * @param sourceUserId
	 * @param currentUserId
	 * @param curDate
	 * @param newFilePid
	 * @param fileMd5Set 收集需要增加引用计数的文件MD5
	 */
	private void findAllSubFile(List<FileInfo> copyFileList, FileInfo fileInfo, String sourceUserId, String currentUserId, Date curDate, String newFilePid, Set<String> fileMd5Set){
		String sourceFileId = fileInfo.getFileId();
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setFilePid(newFilePid);
		fileInfo.setUserId(currentUserId);
		String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
		fileInfo.setFileId(newFileId);

		// 如果是文件（非文件夹），则收集MD5用于增加引用计数
		if (FileFolderTypeEnum.FILE.getType().equals(fileInfo.getFolderType()) && !StringTools.isEmpty(fileInfo.getFileMd5())) {
			fileMd5Set.add(fileInfo.getFileMd5());
		}

		copyFileList.add(fileInfo);
		if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
			FileInfoQuery query = new FileInfoQuery();
			query.setFilePid(sourceFileId);
			query.setUserId(sourceUserId);
			List<FileInfo> sourceFileList = this.fileInfoMapper.selectList(query);
			for(FileInfo item : sourceFileList){
				findAllSubFile(copyFileList, item, sourceUserId, currentUserId, curDate, newFileId, fileMd5Set);
			}
		}
	}

	/**
	 * 获取当前文件夹下的全部图片文件
	 * @param userId
	 * @param filePid
	 * @return
	 */
	@Override
	public List<FileInfo> getFolderImage(String userId, String filePid) {
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(filePid);
		query.setFolderType(FileFolderTypeEnum.FILE.getType());
		query.setFileCategory(FileCategoryEnum.IMAGE.getCategory());
		query.setStatus(FileStatusEnum.USING.getStatus());
		List<FileInfo> imageList = this.fileInfoMapper.selectList(query);
		return imageList;
	}
}