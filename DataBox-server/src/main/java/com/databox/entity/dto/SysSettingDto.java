package com.databox.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {

    private String registerEmailTitle = "DataBox邮箱验证码";
    private String registerEmailContent = "您的验证码为：%s 。有效期15分钟。";
    private Integer userInitUseSpace = 5;
}
