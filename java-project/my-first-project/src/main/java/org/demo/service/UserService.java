package org.demo.service;

import org.demo.dao.UserRepository;
import org.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Map<String, Object> register(String username, String password, String email, String nickname) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);
        String normalizedNickname = normalize(nickname);

        validateRegisterParams(normalizedUsername, password, normalizedEmail);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!normalizedEmail.isEmpty() && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(hashPassword(password));
        user.setEmail(normalizedEmail.isEmpty() ? null : normalizedEmail);
        user.setNickname(normalizedNickname.isEmpty() ? normalizedUsername : normalizedNickname);
        user.setToken(generateToken());

        User savedUser = userRepository.save(user);
        return buildAuthPayload(savedUser);
    }

    @Transactional
    public Map<String, Object> login(String username, String password) {
        String normalizedUsername = normalize(username);
        if (normalizedUsername.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!hashPassword(password).equals(user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        user.setToken(generateToken());
        User savedUser = userRepository.save(user);
        return buildAuthPayload(savedUser);
    }

    public Map<String, Object> getUserInfo(String token) {
        User user = getUserByToken(token);
        return buildUserInfo(user);
    }

    @Transactional
    public Map<String, Object> updateUser(String token, String email, String nickname) {
        User user = getUserByToken(token);
        String normalizedEmail = normalize(email);
        String normalizedNickname = normalize(nickname);

        if (!normalizedEmail.isEmpty() && !normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("邮箱已被其他账号使用");
        }

        user.setEmail(normalizedEmail.isEmpty() ? null : normalizedEmail);
        if (!normalizedNickname.isEmpty()) {
            user.setNickname(normalizedNickname);
        }

        User savedUser = userRepository.save(user);
        return buildUserInfo(savedUser);
    }

    @Transactional
    public void changePassword(String token, String oldPassword, String newPassword) {
        User user = getUserByToken(token);
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("请输入正确的旧密码和至少 6 位的新密码");
        }
        if (!hashPassword(oldPassword).equals(user.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }

        user.setPassword(hashPassword(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void logout(String token) {
        User user = getUserByToken(token);
        user.setToken(null);
        userRepository.save(user);
    }

    private void validateRegisterParams(String username, String password, String email) {
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }

    private User getUserByToken(String rawToken) {
        String token = normalizeToken(rawToken);
        if (token.isEmpty()) {
            throw new IllegalArgumentException("未登录或登录已失效");
        }

        return userRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("登录状态无效，请重新登录"));
    }

    private Map<String, Object> buildAuthPayload(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getToken());
        data.put("user", buildUserInfo(user));
        return data;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("nickname", user.getNickname());
        data.put("createdAt", user.getCreatedAt());
        return data;
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeToken(String rawToken) {
        String token = normalize(rawToken);
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("密码加密失败", e);
        }
    }
}