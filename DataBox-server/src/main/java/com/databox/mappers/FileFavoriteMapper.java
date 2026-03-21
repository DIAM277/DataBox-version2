package com.databox.mappers;

import com.databox.entity.po.FileFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileFavoriteMapper {

    FileFavorite selectByUserIdAndFileId(@Param("userId") String userId, @Param("fileId") String fileId);

    int insert(FileFavorite fileFavorite);

    int deleteByUserIdAndFileId(@Param("userId") String userId, @Param("fileId") String fileId);

}