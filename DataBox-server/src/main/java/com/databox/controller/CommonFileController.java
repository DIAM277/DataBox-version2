package com.databox.controller;

import com.databox.component.RedisComponent;
import com.databox.entity.constants.Constants;
import com.databox.entity.config.AppConfig;
import com.databox.entity.dto.DownloadFileDto;
import com.databox.entity.enums.FileCategoryEnum;
import com.databox.entity.enums.FileFolderTypeEnum;
import com.databox.entity.enums.FileTypeEnum;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.vo.FolderVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.exception.BusinessException;
import com.databox.service.FileInfoService;
import com.databox.utils.CopyTools;
import com.databox.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.core.DocumentConverter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommonFileController extends ABaseController {
    @Resource
    private AppConfig appConfig;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private DocumentConverter documentConverter;

    /**
     * 读取图片
     * 
     * @param response
     * @param imageFolder
     * @param imageName
     */
    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || !StringTools.pathIsOk(imageFolder)
                || !StringTools.pathIsOk(imageName)) {
            return;
        }
        String imageSuffix = StringTools.getFileSuffix(imageName);
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age = 2592000");
        readFile(response, filePath);
    }

    /**
     * 读取文件
     * 
     * @param response
     * @param fileId
     * @param userId
     */
    protected void getFile(HttpServletResponse response, String fileId, String userId) {
        String filePath = null;
        if (fileId.endsWith(".ts")) {
            String[] tsArray = fileId.split("_");
            String readFileId = tsArray[0];
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(readFileId, userId);
            if (null == fileInfo) {
                return;
            }
            String fileName = fileInfo.getFilePath();
            fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileName;
        } else {
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            // 如果是视频文件
            if (FileCategoryEnum.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileNameNoSuffix + "/"
                        + Constants.M3U8_NAME;
            } else {
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            // ================= LibreOffice 文档转换旁路逻辑 ====================
            String fileExtName = StringTools.getFileSuffix(fileInfo.getFileName());// 获取文件后缀
            // 需要被引擎接管的后缀池
            List<String> officeExts = Arrays.asList(".doc", ".docx", ".ppt", ".pptx");

            if (fileExtName != null && officeExts.contains(fileExtName.toLowerCase())) {
                File pdfCacheFile = new File(file.getParent(), file.getName() + ".pdf");

                try {
                    // 若pdf缓存不存在，则触发引擎进行高保真转换
                    if (!pdfCacheFile.exists()) {
                        log.info("触发 LibreOffice 转换: {} -> {}", file.getName(), pdfCacheFile.getName());
                        documentConverter.convert(file).to(pdfCacheFile).execute();
                    }
                    // 将需要被读取传输的指针，指向生成的 PDF 缓冲文件
                    filePath = pdfCacheFile.getAbsolutePath();
                    // 为了让前端 vue-pdf-embed 组件准确嗅探，显式附加上 PDF 的 Content-Type
                    response.setContentType("application/pdf");
                } catch (Exception e) {
                    log.error("文档转换PDF失败：{}", fileInfo.getFileName(), e);
                    throw new BusinessException("文档转换预览失败，请稍后重试");
                }
            }
        }
        readFile(response, filePath);
    }

    /**
     * 获取文件目录层级信息
     * 
     * @param path
     * @param userId
     * @return
     */
    protected ResponseVO getFolderInfo(String path, String userId) {
        // 获取父级目录
        String[] pathArray = path.split("/");
        FileInfoQuery infoQuery = new FileInfoQuery();
        infoQuery.setUserId(userId);
        infoQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType());
        infoQuery.setFileIdArray(pathArray);
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArray, "\",\"") + "\")";
        infoQuery.setOrderBy(orderBy);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(infoQuery);
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FolderVO.class));
    }

    /**
     * 创建下载链接
     * 
     * @param fileId
     * @param userId
     * @return
     */
    protected ResponseVO createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 不支持直接下载整个文件夹
        if (FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code = StringTools.getRandomString(Constants.LENGTH_50);
        DownloadFileDto fileDto = new DownloadFileDto();
        fileDto.setDownloadCode(code);
        fileDto.setFilePath(fileInfo.getFilePath());
        fileDto.setFileName(fileInfo.getFileName());
        // 保存到redis中
        redisComponent.saveDownloadCode(code, fileDto);
        return getSuccessResponseVO(code);
    }

    /**
     * 下载文件
     * 
     * @param request
     * @param response
     * @param code
     * @throws Exception
     */
    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws Exception {
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if (null == downloadFileDto) {
            return;
        }
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        readFile(response, filePath);
    }

}
