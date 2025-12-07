package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.component.RedisComponent;
import com.databox.entity.dto.SysSettingDto;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.query.UserInfoQuery;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.entity.vo.UserInfoVO;
import com.databox.service.FileInfoService;
import com.databox.service.UserInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends CommonFileController{

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取系统设置
     * @return
     */
    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO getSysSetting(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

    /**
     * 保存系统设置
     * @return
     */
    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO saveSysSetting(@VerifyParam(required = true) String registerEmailTitle,
                                     @VerifyParam(required = true) String registerEmailContent,
                                     @VerifyParam(required = true) Integer userInitUseSpace){
        SysSettingDto sysSettingDto = new SysSettingDto();
        sysSettingDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingDto.setRegisterEmailContent(registerEmailContent);
        sysSettingDto.setUserInitUseSpace(userInitUseSpace);
        redisComponent.saveSysSettingDto(sysSettingDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取用户列表
     * @param userInfoQuery
     * @return
     */
    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery, String sortField, String sortOrder){
        if(sortField == null || sortField.isEmpty()){
            userInfoQuery.setOrderBy("create_time desc");
        } else {
            // 根据前端传入的排序字段和排序方向构建orderBy
            String dbField = convertToDbField(sortField);
            String direction = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
            userInfoQuery.setOrderBy(dbField + " " + direction);
        }
        PaginationResultVO resultVO = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, UserInfoVO.class));
    }


    /**
     * 更新用户状态
     * @param userId
     * @param status
     * @return
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer status){
        userInfoService.updateUserStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    /**
     * 修改用户空间
     * @param userId
     * @param totalSpace 新的总空间大小（GB）
     * @return
     */
    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId,
                                      @VerifyParam(required = true) Long totalSpace){
        userInfoService.setUserTotalSpace(userId, totalSpace);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取所有文件
     * @param query
     * @return
     */
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery query,String sortField, String sortOrder){
        if(sortField == null || sortField.isEmpty()){
            query.setOrderBy("create_time desc");
        } else {
            // 根据前端传入的排序字段和排序方向构建orderBy
            String dbField = convertToDbField(sortField);
            String direction = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
            query.setOrderBy(dbField + " " + direction);
        }
        query.setQueryUserName(true);
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(result);
    }

    private String convertToDbField(String frontField) {
        switch (frontField) {
            case "fileName":
                return "file_name";
            case "fileSize":
                return "file_size";
            case "fileType":
                // 对于文件类型排序，先按照文件夹类型排序，再按照文件类型排序
                return "folder_type desc, file_type";
            case "createTime":
                return "create_time";
            case "recoveryTime":
                return "recovery_time";
            case "userName":
                return "user_name";
            case "email":
                return "email";
            case "lastLoginTime":
                return "last_login_time";
            case "status":
                return "status";
            default:
                return "create_time";
        }
    }

    /**
     * 获取当前目录信息
     * @param path
     * @return
     */
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO getFolderInfo(@VerifyParam(required = true) String path){
        return super.getFolderInfo(path, null);
    }

    /**
     * 获取文件信息
     * @param userId
     * @param fileId
     */
    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public void getFile(HttpServletResponse response,
                        @PathVariable("userId") String userId,
                        @PathVariable("fileId") String fileId){
        super.getFile(response, fileId, userId);
    }

    /**
     * 获取视频信息
     * @param response
     * @param userId
     * @param fileId
     */
    @RequestMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public void getVideo(HttpServletResponse response,
                         @PathVariable("userId") String userId,
                         @PathVariable("fileId") String fileId){
        super.getFile(response, fileId, userId);
    }

    /**
     * 创建下载链接
     * @param userId
     * @param fileId
     */
    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO createDownloadUrl(@PathVariable("userId") String userId, @PathVariable("fileId") String fileId){
        return super.createDownloadUrl(fileId, userId);
    }

    /**
     * 下载文件
     * @param request
     * @param response
     * @param code
     * @throws Exception
     */
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable("code") String code) throws Exception {
        super.download(request, response, code);
    }

    /**
     * 删除文件
     * @param fileIdAndUserIds
     * @return
     */
    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO delFile(@VerifyParam(required = true) String fileIdAndUserIds){
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        for (String fileIdAndUserId : fileIdAndUserIdArray){
            String[] itemArray = fileIdAndUserId.split("_");
            fileInfoService.delFileBatch(itemArray[0], itemArray[1], true);
        }
        return getSuccessResponseVO(null);
    }
}
