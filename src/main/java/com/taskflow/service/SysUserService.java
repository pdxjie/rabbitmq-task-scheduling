package com.taskflow.service;

import com.taskflow.entity.SysUser;
import com.taskflow.dto.UserProfileDTO;
import com.taskflow.dto.UpdatePasswordDTO;

public interface SysUserService {

    SysUser getUserById(Long id);

    SysUser getUserByUsername(String username);

    UserProfileDTO getUserProfile(String username);

    boolean updateProfile(String username, UserProfileDTO profileDTO);

    boolean updatePassword(String username, UpdatePasswordDTO passwordDTO);

    SysUser login(String username, String password);
}
