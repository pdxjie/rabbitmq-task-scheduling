package com.taskflow.controller;

import com.taskflow.common.Result;
import com.taskflow.dto.UpdatePasswordDTO;
import com.taskflow.dto.UserProfileDTO;
import com.taskflow.entity.SysUser;
import com.taskflow.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserProfileDTO> getProfile(@RequestHeader(value = "username", defaultValue = "admin") String username) {
        UserProfileDTO profile = userService.getUserProfile(username);
        if (profile == null) {
            return Result.error("用户不存在");
        }
        return Result.success(profile);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestHeader(value = "username", defaultValue = "admin") String username,
            @RequestBody UserProfileDTO profileDTO) {
        boolean success = userService.updateProfile(username, profileDTO);
        if (success) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestHeader(value = "username", defaultValue = "admin") String username,
            @RequestBody UpdatePasswordDTO passwordDTO) {
        boolean success = userService.updatePassword(username, passwordDTO);
        if (success) {
            return Result.success();
        }
        return Result.error("密码修改失败，请检查原密码是否正确");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<UserProfileDTO> login(@RequestBody LoginRequest request) {
        SysUser user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        UserProfileDTO profile = userService.getUserProfile(user.getUsername());
        return Result.success(profile);
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
