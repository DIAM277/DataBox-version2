package com.databox.entity.enums;

import org.apache.commons.lang3.ArrayUtils;


public enum FileTypeEnum {
    VIDEO(FileCategoryEnum.VIDEO, 1, new String[]{".mp4", ".avi", ".mkv", ".wmv", ".mov", ".flv", ".rmvb", ".m4v", ".m3u8"}, "视频"),
    MUSIC(FileCategoryEnum.MUSIC, 2, new String[]{".mp3", ".wav", ".wma", ".mp2", ".flac", ".ape", ".aac", ".cda", ".midi", ".ra"}, "音频"),
    IMAGE(FileCategoryEnum.IMAGE, 3, new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".ico", ".webp", ".dds", ".psd", ".pdt", ".webp", ".xmp", ".svg", ".tiff"}, "图片"),
    PDF(FileCategoryEnum.DOC, 4, new String[]{".pdf"}, "pdf"),
    WORD(FileCategoryEnum.DOC, 5, new String[]{".doc", ".docx"}, "word"),
    EXCEL(FileCategoryEnum.DOC, 6, new String[]{".xls", ".xlsx"}, "excel"),
    TXT(FileCategoryEnum.DOC, 7, new String[]{".txt"}, "txt文本"),
    PROGRAM(FileCategoryEnum.OTHER, 8, new String[]{".h", ".c", ".hpp", ".hxx", ".cpp", ".cc", ".c++", ".cxx", ".m", ".o", ".s", ".dll", ".cs", ".java", ".class", ".js", ".ts", ".css", ".scss", ".vue", ".jsx", ".sql", ".md", ".json", ".html", ".xml"}, "CODE"),
    ZIP(FileCategoryEnum.OTHER, 9, new String[]{".zip", ".rar", ".7z", ".cab", ".arj", ".lzh", ".tar", ".gz", ".ace", ".uue", ".bz", ".jar", ".iso", ".mpq"}, "压缩文件"),
    OTHER(FileCategoryEnum.OTHER, 10, new String[]{}, "其他");

    private FileCategoryEnum category;
    private Integer type;
    private String[] suffix;
    private String desc;

    FileTypeEnum(FileCategoryEnum category, Integer type, String[] suffix, String desc) {
        this.category = category;
        this.type = type;
        this.suffix = suffix;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix){
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (ArrayUtils.contains(fileTypeEnum.getSuffix(), suffix)) {
                return fileTypeEnum;
            }
        }
        return FileTypeEnum.OTHER;
    }

    public static FileTypeEnum getByType(Integer type){
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.getType().equals(type)) {
                return fileTypeEnum;
            }
        }
        return null;
    }

    public FileCategoryEnum getCategory() {
        return category;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public String getDesc() {
        return desc;
    }
}
