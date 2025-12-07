package com.databox.entity.enums;

public enum VerifyRegexEnum {

    NO("", "不校验"),
    EMAIL("[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}", "邮箱"),
    PASSWORD("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,18}$", "数字+字母，8-18位");


    private String regex;
    private String desc;

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}

