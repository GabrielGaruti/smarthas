package com.smarthas.api.service;

import com.smarthas.api.domain.Role;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.*;
import com.smarthas.api.repository.UserRepository;
import com.smarthas.api.security.JwtService;
import com.smarthas.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Regras de cadastro e login. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("E-mail ja cadastrado", HttpStatus.BAD_REQUEST);
        }
        User user = new User(req.fullName().trim(), email, passwordEncoder.encode(req.password()), Role.USER);
        user = userRepository.save(user);
        return new RegisterResponse("Usuario cadastrado com sucesso", UserResponse.from(user));
    }

    public LoginResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Email ou senha invalidos", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException("Email ou senha invalidos", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(token, UserResponse.from(user));
    }
}
