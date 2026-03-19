package com.databox.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI智能文档摘要实体
 */
@Data
public class AiDocSummary implements Serializable {
    /** 摘要ID */
    private String summaryId;

    /** 文件MD5 */
    private String fileMd5;

    /** 摘要内容 */
    private String summaryContent;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}