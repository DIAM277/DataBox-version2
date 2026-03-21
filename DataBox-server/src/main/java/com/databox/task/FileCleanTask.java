package com.databox.task;

import com.databox.entity.enums.FileDelFlagEnum;
import com.databox.entity.enums.SysMessageEnum;
import com.databox.entity.po.FileInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.service.FileInfoService;
import com.databox.service.SysMessageService;
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

    @Resource
    private SysMessageService sysMessageService;

    /**
     * 定时清理回收站文件
     */
    @Scheduled(fixedRate = 1000 * 60 * 3)
    public void execute() {

        try{
            // 查询回收站中的文件信息
            FileInfoQuery fileInfoQuery = new FileInfoQuery();
            fileInfoQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
            // 设置查询包含过期时间
            fileInfoQuery.setQueryExpire(true);
            // 获取符合查询条件的文件信息列表
            List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
            if(fileInfoList == null || fileInfoList.isEmpty()){
                return;
            }

            // 将文件信息按照用户ID分组
            Map<String, List<FileInfo>> fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
            // 遍历每个用户文件列表
            for(Map.Entry<String, List<FileInfo>> entry : fileInfoMap.entrySet()){
                String userId = entry.getKey();
                List<FileInfo> userCleanFile = entry.getValue();
                // 记录清理文件的数量
                int cleanFileCount = userCleanFile.size();
                // 提取出当前用户回收站中的文件
                List<String> fileIds = entry.getValue().stream().map(FileInfo::getFileId).collect(Collectors.toList());
                // 批量删除回收站中过期文件
                fileInfoService.delFileBatch(entry.getKey(), String.join(",", fileIds), false);
                // 发送彻底删除回收站文件系统消息
                try{
                    sysMessageService.saveMessage(userId, SysMessageEnum.RECYCLE_AUTO_CLEAN, cleanFileCount);
                } catch (Exception e){
                    // 发送消息失败不影响文件清理结果，记录日志后继续执行
                    e.printStackTrace();
                }
            }
        } catch (Exception e){
            // 记录日志，定时任务执行异常不影响系统正常运行
            e.printStackTrace();
        }
    }
}
