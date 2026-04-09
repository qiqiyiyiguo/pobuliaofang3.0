package org.demo.controller;

import org.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = userService.register(
                    params.get("username"),
                    params.get("password"),
                    params.get("email"),
                    params.get("nickname")
            );
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("data", data);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = userService.login(
                    params.get("username"),
                    params.get("password")
            );
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", data);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = userService.getUserInfo(authorization);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
        } catch (IllegalArgumentException e) {
            result.put("code", 401);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = userService.updateUser(
                    authorization,
                    params.get("email"),
                    params.get("nickname")
            );
            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", data);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.changePassword(
                    authorization,
                    params.get("oldPassword"),
                    params.get("newPassword")
            );
            result.put("code", 200);
            result.put("message", "修改成功");
            result.put("data", null);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.logout(authorization);
            result.put("code", 200);
            result.put("message", "退出成功");
            result.put("data", null);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }
        return result;
    }
}