package com.example.lottery.service;

import com.example.lottery.domain.AuthResponse;
import com.example.lottery.domain.User;
import com.example.lottery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ConcurrentMap<String, Long> tokenStore = new ConcurrentHashMap<String, Long>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(String username, String password) {
        String normalized = normalize(username);
        if (userRepository.findByUsername(normalized).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }
        String hash = passwordEncoder.encode(password);
        User user = new User(normalized, hash, Instant.now());
        userRepository.save(user);
        String token = issueToken(user.getId());
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(String username, String password) {
        String normalized = normalize(username);
        Optional<User> userOpt = userRepository.findByUsername(normalized);
        if (!userOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名或密码错误");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名或密码错误");
        }
        String token = issueToken(user.getId());
        return new AuthResponse(token, user.getUsername());
    }

    public User requireUser(String authHeader) {
        String token = extractToken(authHeader);
        Long userId = tokenStore.get(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"));
    }

    private String issueToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, userId);
        return token;
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        String value = authHeader.trim();
        if (value.toLowerCase().startsWith("bearer ")) {
            return value.substring(7).trim();
        }
        return value;
    }
}
