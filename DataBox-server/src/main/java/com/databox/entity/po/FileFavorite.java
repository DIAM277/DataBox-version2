package com.databox.entity.po;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户文件收藏表
 */
@Data
public class FileFavorite implements Serializable {
    private Integer favoriteId;
    private String userId;
    private String fileId;
    private Date createTime;
}