package com.databox.service;

import com.databox.entity.dto.SessionWebUserDto;

public interface AiDocSummaryService {
    /**
     * 生成并获取文档摘要
     */
    String generateSummary(SessionWebUserDto webUserDto, String fileId);
}