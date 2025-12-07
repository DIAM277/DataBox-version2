package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.dto.UploadResultDto;
import com.databox.entity.enums.FileCategoryEnum;
import com.databox.entity.enums.FileDelFlagEnum;
import com.databox.entity.enums.FileFolderTypeEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.vo.FileInfoVO;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.FileInfoService;
import com.databox.utils.CopyTools;
import com.databox.utils.StringTools;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 文件信息记录表 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends CommonFileController{

	@Resource
	private FileInfoService fileInfoService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category){
		FileCategoryEnum categoryEnum = FileCategoryEnum.getByCode(category);
		if(null != categoryEnum){
			query.setFileCategory(categoryEnum.getCategory());
		}
		query.setUserId(getUserInfoFromSession(session).getUserId());

		// 设置默认排序
		if(query.getSortField() == null || query.getSortField().isEmpty()){
			query.setOrderBy("last_update_time desc");
		} else {
			// 根据前端传入的排序字段和排序方向构建orderBy
			String dbField = convertToDbField(query.getSortField());
			String direction = "asc".equalsIgnoreCase(query.getSortOrder()) ? "asc" : "desc";
			query.setOrderBy(dbField + " " + direction);
		}
		query.setDelFlag(FileDelFlagEnum.USING.getFlag());
		PaginationResultVO result = fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
	}

	// 将前端字段名转换为数据库字段名
	private String convertToDbField(String frontField) {
		switch (frontField) {
			case "fileName":
				return "file_name";
			case "fileSize":
				return "file_size";
			case "fileType":
				// 对于文件类型排序，先按照文件夹类型排序，再按照文件类型排序
				return "folder_type desc, file_type";
			case "lastUpdateTime":
				return "last_update_time";
			default:
				return "last_update_time";
		}
	}

	/**
	 * 文件上传
	 * @param session
	 * @param fileId
	 * @param file
	 * @param fileName
	 * @param filePid
	 * @param fileMd5
	 * @param chunkIndex
	 * @param chunks
	 * @return
	 */
	@RequestMapping("/uploadFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO uploadFile(HttpSession session,
								 String fileId,
								 MultipartFile file,
								 @VerifyParam(required = true) String fileName,
								 @VerifyParam(required = true) String filePid,
								 @VerifyParam(required = true) String fileMd5,
								 @VerifyParam(required = true) Integer chunkIndex,
								 @VerifyParam(required = true) Integer chunks){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		UploadResultDto result = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
		return getSuccessResponseVO(result);
	}

	/**
	 * 获取图片
	 * @param response
	 * @param imageFolder
	 * @param imageName
	 */
	@RequestMapping("/getImage/{imageFolder}/{imageName}")
	//@GlobalInterceptor(checkParams = true)
	public void getImage(HttpServletResponse response, @PathVariable("imageFolder")String imageFolder, @PathVariable("imageName") String imageName){
		super.getImage(response, imageFolder, imageName);
	}

	/**
	 * 获取视频信息
	 * @param response
	 * @param session
	 * @param fileId
	 */
	@RequestMapping("/ts/getVideoInfo/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId")String fileId){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}

	/**
	 * 获取文件信息
	 * @param response
	 * @param session
	 * @param fileId
	 */
	@RequestMapping("/getFile/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId")String fileId){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}

	/**
	 * 新建目录
	 */
	@RequestMapping("/newFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO newFolder(HttpSession session,
								@VerifyParam(required = true) String filePid,
								@VerifyParam(required = true) String fileName){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		FileInfo fileInfo = fileInfoService.newFolder(filePid, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
	}

	/**
	 * 获取文件目录层级信息
	 * @param session
	 * @param path
	 * @return
	 */
	@RequestMapping("/getFolderInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderInfo(HttpSession session,
									@VerifyParam(required = true) String path){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		return super.getFolderInfo(path, webUserDto.getUserId());
	}

	/**
	 * 文件重命名
	 * @param session
	 * @param fileId
	 * @param fileName
	 * @return
	 */
	@RequestMapping("/rename")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO rename(HttpSession session,
							 @VerifyParam(required = true) String fileId,
							 @VerifyParam(required = true) String fileName){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		FileInfo fileInfo = fileInfoService.reName(fileId, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
	}

	/**
	 * 获取所有目录
	 * @param session
	 * @param filePid
	 * @param currentFileIds
	 * @return
	 */
	@RequestMapping("/loadAllFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO loadAllFolder(HttpSession session,
							 @VerifyParam(required = true) String filePid, String currentFileIds){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		FileInfoQuery infoQuery = new FileInfoQuery();
		infoQuery.setUserId(webUserDto.getUserId());
		infoQuery.setFilePid(filePid);
		infoQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		// 排除当前目录
		if(!StringTools.isEmpty(currentFileIds)){
			infoQuery.setExcludeFileIdArray(currentFileIds.split(","));
		}
		infoQuery.setDelFlag(FileDelFlagEnum.USING.getFlag());
		infoQuery.setOrderBy("create_time desc");
		List<FileInfo> fileInfoList = fileInfoService.findListByParam(infoQuery);
		return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
	}

	/**
	 * 移动文件到指定目录
	 * @param session
	 * @param fileIds
	 * @param filePid
	 * @return
	 */
	@RequestMapping("/changeFileFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO changeFileFolder(HttpSession session,
									  @VerifyParam(required = true) String fileIds,
									  @VerifyParam(required = true) String filePid){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		fileInfoService.changeFileFolder(fileIds, filePid, webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}

	/**
	 * 生成下载地址
	 * @param session
	 * @param fileId
	 * @return
	 */
	@RequestMapping("/createDownloadUrl/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO createDownloadUrl(HttpSession session,@VerifyParam(required = true) @PathVariable("fileId")String fileId){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		return super.createDownloadUrl(fileId, getUserInfoFromSession(session).getUserId());
	}

	/**
	 * 下载文件
	 * @param response
	 * @param code
	 * @return
	 */
	@RequestMapping("/download/{code}")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public void download(HttpServletRequest request, HttpServletResponse response,
							   @VerifyParam(required = true) @PathVariable("code")String code) throws Exception {
		super.download(request, response, code);
	}

	/**
	 * 删除文件
	 * @param session
	 * @param fileIds
	 * @return
	 */
	@RequestMapping("/delFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		fileInfoService.removeFile2RecycleBinBatch(webUserDto.getUserId(), fileIds);
		return getSuccessResponseVO(null);
	}

	/**
	 * 获取当前目录的全部图片
	 */
	@RequestMapping("/getFolderImage")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderImage(HttpSession session, @VerifyParam(required = true) String filePid){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		List<FileInfo> imageList = fileInfoService.getFolderImage(webUserDto.getUserId(), filePid);
		return getSuccessResponseVO(CopyTools.copyList(imageList, FileInfoVO.class));
	}


}