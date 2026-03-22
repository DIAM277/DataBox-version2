package com.databox.entity.enums;

public enum SysMessageStatusEnum {
    DISABLE(0, "正常"),
    ENABLE(1, "删除");

    private Integer status;
    private String desc;

    SysMessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static SysMessageStatusEnum getByStatus(Integer status) {
        for (SysMessageStatusEnum sysMessageStatusEnum : SysMessageStatusEnum.values()) {
            if (sysMessageStatusEnum.getStatus().equals(status)) {
                return sysMessageStatusEnum;
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
