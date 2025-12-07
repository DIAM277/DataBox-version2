package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionShareDto;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.enums.FileDelFlagEnum;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.po.FileShare;
import com.databox.entity.po.UserInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.vo.FileInfoVO;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.entity.vo.ShareInfoVO;
import com.databox.exception.BusinessException;
import com.databox.service.FileInfoService;
import com.databox.service.FileShareService;
import com.databox.service.UserInfoService;
import com.databox.utils.CopyTools;
import com.databox.utils.StringTools;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController("webShareController")
@RequestMapping("/showShare")
public class WebShareController extends CommonFileController{
    @Resource
    private FileShareService fileShareService;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取文件分享登录信息
     * @param session
     * @param shareId
     * @return
     */
    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO getShareLoginInfo(HttpSession session, @VerifyParam(required = true) String shareId) {
        SessionShareDto sessionShareDto = getSessionShareFromSession(session, shareId);
        if(null == sessionShareDto){
            return getSuccessResponseVO(null);
        }
        ShareInfoVO shareInfoVO = getShareInfoCommon(shareId);
        // 判断是否当前用户分享的文件
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if(userDto != null && userDto.getUserId().equals(sessionShareDto.getShareUserId())){
            shareInfoVO.setCurrentUser(true);
        } else {
            shareInfoVO.setCurrentUser(false);
        }
        return getSuccessResponseVO(shareInfoVO);
    }

    /**
     * 获取文件分享消息
     * @param shareId
     * @return
     */
    @RequestMapping("/getShareInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO  getShareInfo(@VerifyParam(required = true) String shareId) {
        return getSuccessResponseVO(getShareInfoCommon(shareId));
    }

    /**
     * 获取文件分享消息
     * @param shareId
     * @return
     */
    private ShareInfoVO getShareInfoCommon(String shareId) {
        // 根据分享ID 获取文件分享对象
        FileShare share = fileShareService.getFileShareByShareId(shareId);
        // 检查分享是否存在且未过期，如果不存在或已过期，则抛出异常
        if(null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        // 将文件分享对象转换为分享信息视图对象
        ShareInfoVO shareInfoVO = CopyTools.copy(share, ShareInfoVO.class);
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(share.getFileId(), share.getUserId());
        // 检查文件信息是否存在且未被删除，如果不存在或已被删除，则抛出异常
        if(fileInfo == null || !FileDelFlagEnum.USING.getFlag().equals(fileInfo.getDelFlag())){
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        shareInfoVO.setFileName(fileInfo.getFileName());
        UserInfo userInfo = userInfoService.getUserInfoByUserId(share.getUserId());
        shareInfoVO.setUserName(userInfo.getUserName());
        shareInfoVO.setAvatar(userInfo.getQqAvatar());
        shareInfoVO.setUserId(share.getUserId());
        return shareInfoVO;
    }

    /**
     * 校验文件提取码
     * @param session
     * @param shareId
     * @param code
     * @return
     */
    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,
                                     @VerifyParam(required = true) String shareId,
                                     @VerifyParam(required = true) String code){
        SessionShareDto sessionShareDto = fileShareService.checkShareCode(shareId, code);
        session.setAttribute(Constants.SESSION_SHARE_KEY + shareId, sessionShareDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载文件列表
     * @param session
     * @param shareId
     * @param filePid
     * @return
     */
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO loadFileList(HttpSession session,
                                   @VerifyParam(required = true) String shareId,
                                   String filePid){
        SessionShareDto shareSessionDto = checkShare(session, shareId);
        FileInfoQuery query = new FileInfoQuery();
        if(!StringTools.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)){
            fileInfoService.checkRootFilePid(shareSessionDto.getFileId(), shareSessionDto.getShareUserId(), filePid);
            query.setFilePid(filePid);
        } else {
            query.setFileId(shareSessionDto.getFileId());
        }
        query.setUserId(shareSessionDto.getShareUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnum.USING.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * 检查分享
     * @param session
     * @param shareId
     * @return
     */
    private SessionShareDto checkShare(HttpSession session, String shareId){
        SessionShareDto sessionShareDto = getSessionShareFromSession(session, shareId);
        if(null == sessionShareDto){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(sessionShareDto.getExpireTime() != null && new Date().after(sessionShareDto.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDto;
    }

    /**
     * 获取目录信息
     * @param session
     * @param shareId
     * @param path
     * @return
     */
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                    @VerifyParam(required = true) String path){
        SessionShareDto shareDto = checkShare(session, shareId);
        return super.getFolderInfo(path, shareDto.getShareUserId());
    }

    /**
     * 获取文件信息
     * @param response
     * @param session
     * @param shareId
     * @param fileId
     */
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @PathVariable("shareId") String shareId,
                        @PathVariable("fileId") String fileId){
        SessionShareDto shareDto = checkShare(session, shareId);
        super.getFile(response, fileId, shareDto.getShareUserId());
    }

    /**
     * 获取视频信息
     * @param response
     * @param session
     * @param shareId
     * @param fileId
     */
    @RequestMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getImage(HttpServletResponse response,
                         HttpSession session,
                         @PathVariable("shareId") String shareId,
                         @PathVariable("fileId") String fileId){
        SessionShareDto shareDto = checkShare(session, shareId);
        super.getImage(response, fileId, shareDto.getShareUserId());
    }

    /**
     * 创建下载地址
     * @param session
     * @param shareId
     * @param fileId
     * @return
     */
    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @PathVariable("shareId") String shareId,
                                        @PathVariable("fileId") String fileId){
        SessionShareDto shareDto = checkShare(session, shareId);
        return super.createDownloadUrl(fileId, shareDto.getShareUserId());
    }

    /**
     * 下载
     * @param request
     * @param response
     * @param code
     * @throws Exception
     */
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @VerifyParam(required = true) @PathVariable("code") String code) throws Exception {
        super.download(request, response, code);
    }

    /**
     * 将分享文件保存到我的网盘
     * @param session
     * @param shareId
     * @param shareFileIds
     * @param myFolderId
     */
    @RequestMapping("/saveShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO saveShare(HttpSession session,
                          @VerifyParam(required = true) String shareId,
                          @VerifyParam(required = true) String shareFileIds,
                          @VerifyParam(required = true) String myFolderId) {
        SessionShareDto shareSessionDto = checkShare(session, shareId);
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        if(shareSessionDto.getShareUserId().equals(webUserDto.getUserId())){
            throw new BusinessException("自己分享的文件无法保存到自己的网盘");
        }
        fileInfoService.saveShare(shareSessionDto.getFileId(), shareFileIds, myFolderId, shareSessionDto.getShareUserId(), webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
