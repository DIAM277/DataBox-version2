package com.databox.component;

import com.databox.entity.constants.Constants;
import com.databox.entity.dto.DownloadFileDto;
import com.databox.entity.dto.SysSettingDto;
import com.databox.entity.dto.UserSpaceDto;
import com.databox.entity.po.FileInfo;
import com.databox.entity.po.UserInfo;
import com.databox.entity.query.FileInfoQuery;
import com.databox.entity.query.UserInfoQuery;
import com.databox.mappers.FileInfoMapper;
import com.databox.mappers.UserInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 获取系统设置
     * @return
     */
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
        }
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     */
    public void saveSysSettingDto(SysSettingDto sysSettingDto){
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    public void saveUserSpaceUsed(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setx(Constants.REDIS_KEY_USER_SPACE_USED + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRE_TIME_DAY);
    }

    /**
     * 重置用户空间使用量
     * @param userId
     */
    public UserSpaceDto resetUserSpaceUsed(String userId) {
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long userSpace = this.fileInfoMapper.selectUseSpace(userId);
        userSpaceDto.setUseSpace(userSpace);
        UserInfo userInfo = this.userInfoMapper.selectByUserId(userId);
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisUtils.setx(Constants.REDIS_KEY_USER_SPACE_USED + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRE_TIME_DAY);
        return userSpaceDto;
    }

    public UserSpaceDto getUserSpaceUsed(String userId){
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USED + userId);
        if (userSpaceDto == null) {
            userSpaceDto = new UserSpaceDto();
            // 获取当前已经使用空间
            Long useSpace = fileInfoMapper.selectUseSpace(userId);
            userSpaceDto.setUseSpace(useSpace);
            userSpaceDto.setTotalSpace(getSysSettingDto().getUserInitUseSpace() * Constants.MB);
            saveUserSpaceUsed(userId, userSpaceDto);
        }
        return userSpaceDto;
   }

   // 保存临时文件大小
   public void saveFileTempSize(String userId, String fileId, Long fileSize){
        Long currentSize = getFileTempSize(userId, fileId);
        redisUtils.setx(Constants.REDIS_KEY_FILE_TEMP + userId + fileId, currentSize + fileSize, Constants.REDIS_KEY_EXPIRE_TIME_HALF_HOUR);
   }

   // 获取临时文件大小
   public Long getFileTempSize(String userId, String fileId){
       return getFileSizeFromRedis(Constants.REDIS_KEY_FILE_TEMP + userId + fileId);
   }

   public Long getFileSizeFromRedis(String key){
        Object sizeObj = redisUtils.get(key);
        if(sizeObj == null){
            return 0L;
        }
        if(sizeObj instanceof Integer){
            return ((Integer) sizeObj).longValue();
        } else if(sizeObj instanceof Long){
            return (Long) sizeObj;
        }
        return 0L;
   }

    /**
     * 保存下载验证码
     * @param code
     * @param downloadFileDto
     */
   public void saveDownloadCode(String code, DownloadFileDto downloadFileDto){
        redisUtils.setx(Constants.REDIS_KEY_DOWNLOAD + code, downloadFileDto, Constants.REDIS_KEY_EXPIRE_TIME_5MIN);
   }

    /**
     * 获取下载验证码
     * @param code
     * @return
     */
   public DownloadFileDto getDownloadCode(String code){
       return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
   }
}
