package com.databox.entity.enums;

public enum ShareReportEnum {
    PENDING(0, "待处理"),
    BAN(1, "已处理(封禁)"),
    NORMAL(2, "已处理(正常)");

    private Integer status;
    private String desc;

    ShareReportEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    // 修复了这里的返回值和遍历目标
    public static ShareReportEnum getByStatus(Integer status) {
        for (ShareReportEnum reportEnum : ShareReportEnum.values()) {
            if (reportEnum.getStatus().equals(status)) {
                return reportEnum;
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