package com.taskflow.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String department;
    private String lastLoginTime;
    private String createdAt;
}
