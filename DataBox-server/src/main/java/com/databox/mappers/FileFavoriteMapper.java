package com.databox.mappers;

import com.databox.entity.po.FileFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileFavoriteMapper {

    FileFavorite selectByUserIdAndFileId(@Param("userId") String userId, @Param("fileId") String fileId);

    int insert(FileFavorite fileFavorite);

    int deleteByUserIdAndFileId(@Param("userId") String userId, @Param("fileId") String fileId);

    // 查询这些ID中有哪些已经被收藏了
    List<String> selectFavoritedFileIds(@Param("userId") String userId, @Param("fileIdArray") String[] fileIdArray);

    // 批量取消收藏
    int deleteBatch(@Param("userId") String userId, @Param("fileIdArray") String[] fileIdArray);

    // 批量加入收藏
    int insertBatch(@Param("list") List<FileFavorite> list);

}