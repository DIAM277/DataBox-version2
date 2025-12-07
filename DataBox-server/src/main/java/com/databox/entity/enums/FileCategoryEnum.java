package com.databox.entity.enums;

import lombok.Data;

public enum FileCategoryEnum {
    VIDEO(1, "video", "视频"),
    MUSIC(2, "music", "音频"),
    IMAGE(3, "image", "图片"),
    DOC(4, "doc", "文档"),
    OTHER(5, "others", "其他");

    private Integer category;
    private String code;
    private String desc;

    private FileCategoryEnum(Integer category, String code, String desc) {
        this.category = category;
        this.code = code;
        this.desc = desc;
    }

    public static FileCategoryEnum getByCode(String code){
        for (FileCategoryEnum item : FileCategoryEnum.values()){
            if(item.getCode().equals(code)){
                return item;
            }
        }
        return null;
    }

    public Integer getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
