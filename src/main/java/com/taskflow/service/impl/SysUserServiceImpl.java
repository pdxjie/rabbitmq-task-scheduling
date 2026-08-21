package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.UpdatePasswordDTO;
import com.taskflow.dto.UserProfileDTO;
import com.taskflow.entity.SysUser;
import com.taskflow.mapper.SysUserMapper;
import com.taskflow.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public SysUser getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserProfileDTO getUserProfile(String username) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        dto.setDepartment("技术部"); // 暂时硬编码，后续可以从其他表关联

        if (user.getLastLoginTime() != null) {
            dto.setLastLoginTime(user.getLastLoginTime().format(FORMATTER));
        }
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().format(FORMATTER));
        }

        return dto;
    }

    @Override
    public boolean updateProfile(String username, UserProfileDTO profileDTO) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            return false;
        }

        if (profileDTO.getNickname() != null) {
            user.setNickname(profileDTO.getNickname());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updatePassword(String username, UpdatePasswordDTO passwordDTO) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            return false;
        }

        // 验证旧密码
        String oldPasswordMd5 = DigestUtils.md5DigestAsHex(passwordDTO.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!oldPasswordMd5.equals(user.getPassword())) {
            return false;
        }

        // 更新新密码
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(passwordDTO.getNewPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(newPasswordMd5);

        return userMapper.updateById(user) > 0;
    }

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            return null;
        }

        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!passwordMd5.equals(user.getPassword())) {
            return null;
        }

        return user;
    }
}
