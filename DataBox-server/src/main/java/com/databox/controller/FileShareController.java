package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.po.FileShare;
import com.databox.entity.query.FileShareQuery;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.FileShareService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("fileShareController")
@RequestMapping("/share")
public class FileShareController extends ABaseController{

    @Resource
    private FileShareService fileShareService;

    /**
     * 加载分享列表
     * @param session
     * @param query
     * @return
     */
    @RequestMapping("/loadShareList")
    @GlobalInterceptor
    public ResponseVO loadShareList(HttpSession session, FileShareQuery query){
        if(query.getSortField() == null || query.getSortField().isEmpty()){
            query.setOrderBy("share_time desc");
        }else {
            // 根据前端传入的排序字段和排序方向构建orderBy
            String dbField = convertToDbField(query.getSortField());
            String direction = "asc".equalsIgnoreCase(query.getSortOrder()) ? "asc" : "desc";
            query.setOrderBy(dbField + " " + direction);
        }
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        query.setUserId(webUserDto.getUserId());
        query.setQueryFileName(true);
        PaginationResultVO result = fileShareService.findListByPage(query);
        return getSuccessResponseVO(result);
    }

    private String convertToDbField(String frontField){
        switch (frontField){
            case "fileName":
                return "file_name";
            case "shareTime":
                return "share_time";
            case "expireTime":
                return "expire_time";
            case "showCount":
                return "show_count";
            default:
                return "last_update_time";
        }
    }

    /**
     * 分享文件
     * @param session
     * @param fileId
     * @return
     */
    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session,
                                @VerifyParam(required = true) String fileId,
                                @VerifyParam(required = true) Integer validType,
                                String code){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileShare share = new FileShare();
        share.setValidType(validType);
        share.setCode(code);
        share.setFileId(fileId);
        share.setUserId(webUserDto.getUserId());
        fileShareService.saveShare(share);
        return getSuccessResponseVO(share);
    }

    /**
     * 取消分享
     * @param session
     * @param shareIds
     * @return
     */
    @RequestMapping("/cancelShare")
    public ResponseVO cancelShare(HttpSession session,
                                 @VerifyParam(required = true) String shareIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileShareService.deleteFileShareBatch(shareIds.split(","), webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
