package com.databox.controller;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.enums.FileDelFlagEnum;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.vo.FileInfoVO;
import com.databox.entity.vo.PaginationResultVO;
import com.databox.entity.vo.ResponseVO;
import com.databox.service.FileInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@RestController("recycleController")
@RequestMapping("/recycle")
public class RecycleController extends ABaseController{

    @Resource
    private FileInfoService fileInfoService;

    /**
     * 加载回收站列表
     * @param session
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycleList(HttpSession session, Integer pageNo, Integer pageSize,String sortField, String sortOrder){
        FileInfoQuery query = new FileInfoQuery();
        query.setPageSize(pageSize);
        query.setPageNo(pageNo);
        query.setUserId(getUserInfoFromSession(session).getUserId());
        // 设置默认排序
        if(sortField == null || sortField.isEmpty()){
            query.setOrderBy("recovery_time desc");
        } else {
            // 根据前端传入的排序字段和排序方向构建orderBy
            String dbField = convertToDbField(sortField);
            String direction = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
            query.setOrderBy(dbField + " " + direction);
        }
        query.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    private String convertToDbField(String frontField) {
        switch (frontField) {
            case "fileName":
                return "file_name";
            case "fileSize":
                return "file_size";
            case "recoveryTime":
                return "recovery_time";
            default:
                return "recovery_time";
        }
    }

    /**
     * 恢复回收站文件
     * @param session
     * @param fileIds
     * @return
     */
    @RequestMapping("/recoverFile")
    @GlobalInterceptor
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.recoverFileBatch(webUserDto.getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 彻底删除文件
     * @param session
     * @param fileIds
     * @return
     */
    @RequestMapping("/delFile")
    @GlobalInterceptor
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.delFileBatch(webUserDto.getUserId(), fileIds, false);
        return getSuccessResponseVO(null);
    }
}
