package com.databox.mappers;

import com.databox.entity.po.AiDocSummary;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiDocSummaryMapper {

    // 修复映射：显式使用 AS 别名解决下划线转驼峰失效导致的 MyBatis 映射抛出 null 的问题
    @Select("SELECT " +
            "summary_id AS summaryId, " +
            "file_md5 AS fileMd5, " +
            "summary_content AS summaryContent, " +
            "create_time AS createTime " +
            "FROM ai_doc_summary WHERE file_md5 = #{fileMd5} LIMIT 1")
    AiDocSummary selectByFileMd5(@Param("fileMd5") String fileMd5);

    @Insert("INSERT INTO ai_doc_summary(summary_id, file_md5, summary_content, create_time) " +
            "VALUES (#{summaryId}, #{fileMd5}, #{summaryContent}, #{createTime})")
    int insert(AiDocSummary aiDocSummary);
}