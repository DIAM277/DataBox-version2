package com.databox.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统消息枚举（统一管理所有固定通知文案）
 */
@Getter
@AllArgsConstructor
public enum SysMessageEnum {

    /**
     * 1. 存储空间不足提醒
     */
    STORAGE_NOT_ENOUGH("存储空间不足", "您的存储空间已达到阈值，请及时清理文件释放空间"),

    /**
     * 2. 回收站文件30天自动清理提醒
     */
    RECYCLE_AUTO_CLEAN("回收站自动清理", "您的回收站中有 %s 个文件已满30天，系统已自动彻底清理。"),

    /**
     * 3. 管理员系统公告
     */
    ADMIN_NOTICE("系统公告", "管理员发布了新公告：%s"),

    /**
     * 4. 新设备登录通知
     */
    NEW_DEVICE_LOGIN("账号安全提醒", "您的账号于 %s 在 %s 登录，若非本人操作请及时修改密码！"),

    /**
     * 5. 密码修改成功通知
     */
    PASSWORD_CHANGE_SUCCESS("密码修改成功", "账号密码已于 %s 修改，如非本人操作请及时联系管理员"),

    /*
    6. 新用户注册通知
     */
    REGISTER_SUCCESS("欢迎使用DataBox", "欢迎您注册成为DataBox的用户，祝您使用愉快！");


    // 消息标题
    private final String title;
    // 消息内容（支持占位符 %s 动态传参）
    private final String content;

}