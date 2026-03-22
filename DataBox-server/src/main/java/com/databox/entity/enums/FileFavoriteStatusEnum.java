package com.databox.entity.enums;

public enum FileFavoriteStatusEnum {
    NORMAL(0, "未收藏"),
    COLLECTION(1, "已收藏");

    private Integer status;
    private String desc;

    FileFavoriteStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static FileFavoriteStatusEnum fileFavoriteStatusEnum(Integer status) {
        for (FileFavoriteStatusEnum fileFavoriteStatusEnum : FileFavoriteStatusEnum.values()) {
            if (fileFavoriteStatusEnum.getStatus().equals(status)) {
                return fileFavoriteStatusEnum;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
