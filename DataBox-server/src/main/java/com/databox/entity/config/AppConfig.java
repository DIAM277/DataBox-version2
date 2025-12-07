package com.databox.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
@Data
public class AppConfig {

    @Value("${spring.mail.username:}")
    private String sendUserName;

    @Value("${admin.emails}")
    private String adminEmails;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${qq.app.id:}")
    private String qqAppId;

    @Value("${qq.app.key:}")
    private String qqAppKey;

    @Value("${qq.url.authorization:}")
    private String qqUrlAuthorization;

    @Value("${qq.url.access.token:}")
    private String qqUrlAccessToken;

    @Value("${qq.url.openid:}")
    private String qqUrlOpenid;

    @Value("${qq.url.user.info:}")
    private String qqUrlUserinfo;

    @Value("${qq.url.redirect:}")
    private String qqUrlRedirect;
}
