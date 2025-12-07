package com.databox.task;

import com.databox.entity.enums.FileDelFlagEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.service.FileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("fileCleanTask")
public class FileCleanTask {

    @Resource
    private FileInfoService fileInfoService;

    /**
     * 定时清理回收站文件
     */
    @Scheduled(fixedRate = 1000 * 60 * 3)
    public void execute() {
        // 查询回收站中的文件信息
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        // 设置查询包含过期时间
        fileInfoQuery.setQueryExpire(true);
        // 获取符合查询条件的文件信息列表
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        // 将文件信息按照用户ID分组
        Map<String, List<FileInfo>> fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        // 遍历每个用户文件列表
        for(Map.Entry<String, List<FileInfo>> entry : fileInfoMap.entrySet()){
            // 提取出当前用户回收站中的文件
            List<String> fileIds = entry.getValue().stream().map(FileInfo::getFileId).collect(Collectors.toList());
            // 批量删除回收站中过期文件
            fileInfoService.delFileBatch(entry.getKey(), String.join(",", fileIds), false);
        }
    }
}
