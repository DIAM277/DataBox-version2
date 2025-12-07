package com.databox.entity.dto;

import lombok.Data;

@Data
public class SessionWebUserDto {
    private String userName;
    private String userId;
    private Boolean isAdmin;
    private String avatar;
    private String email;
}
