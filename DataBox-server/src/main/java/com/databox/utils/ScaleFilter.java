package com.databox.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class ScaleFilter {
    public static void createCoverForVideo(File sourceFile, Integer width, File targetFile){
        try{
            String cmd = "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, width, targetFile.getAbsoluteFile()), false);
        }catch (Exception e){
            log.error("生成缩略图失败", e);
        }
    }

    public static Boolean createThumbnailWidthFFmpeg(File file, int thumbnailWidth, File targetFile, Boolean delSource){
        try{
            BufferedImage src = ImageIO.read(file);
            int sorceW = src.getWidth();
            int sorceH = src.getHeight();
            // 小于指定高度不压缩
            if(sorceH < thumbnailWidth){
                return false;
            }
            compressImage(file, thumbnailWidth, targetFile, delSource);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // 图片压缩为缩略图
    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource){
        try{
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, targetFile.getAbsoluteFile()), false);
            if(delSource){
                FileUtils.forceDelete(sourceFile);
            }
        }catch (Exception e){
            log.error("生成缩略图失败", e);
        }
    }
}
