package com.databox.service;

import java.util.List;

import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.UploadResultDto;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.po.FileInfo;
import com.databox.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 文件信息记录表 业务接口
 */
public interface FileInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<FileInfo> findListByParam(FileInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(FileInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(FileInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<FileInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<FileInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(FileInfo bean,FileInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(FileInfoQuery param);

	/**
	 * 根据FileIdAndUserId查询对象
	 */
	FileInfo getFileInfoByFileIdAndUserId(String fileId,String userId);


	/**
	 * 根据FileIdAndUserId修改
	 */
	Integer updateFileInfoByFileIdAndUserId(FileInfo bean,String fileId,String userId);


	/**
	 * 根据FileIdAndUserId删除
	 */
	Integer deleteFileInfoByFileIdAndUserId(String fileId,String userId);

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
	 */
	UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunk);

	/**
	 * 新建目录
	 * @param filePid
	 * @param userId
	 * @param folderName
	 * @return
	 */
    FileInfo newFolder(String filePid, String userId, String folderName);

	/**
	 * 文件重命名
	 * @param fileId
	 * @param fileName
	 * @return
	 */
	FileInfo reName(String fileId, String userId, String fileName);

	/**
	 * 文件移动
	 * @param fileIds
	 * @param filePid
	 * @param userId
	 */
	void changeFileFolder(String fileIds, String filePid, String userId);

	/**
	 * 批量删除
	 * @param userId
	 * @param fileIds
	 */
	void removeFile2RecycleBinBatch(String userId, String fileIds);

	/**
	 * 批量恢复回收站文件
	 * @param userId
	 * @param fileIds
	 */
	void recoverFileBatch(String userId, String fileIds);

	/**
	 * 彻底删除文件
	 * @param userId
	 * @param fileIds
	 * @param adminOp
	 */
	void delFileBatch(String userId, String fileIds, Boolean adminOp);

	/**
	 * 检查根目录
	 * @param rootFilePid
	 * @param userId
	 * @param fileId
	 */
	void checkRootFilePid(String rootFilePid, String userId, String fileId);

	/**
	 * 保存分享的文件到自己的网盘
	 * @param shareRootFilePid
	 * @param shareFileIds
	 * @param myFolderId
	 * @param shareUserId
	 * @param currentUserId
	 */
	void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId);

	/**
	 * 获取文件夹下的全部图片
	 * @param userId
	 * @param filePid
	 * @return
	 */
	List<FileInfo> getFolderImage(String userId, String filePid);
}