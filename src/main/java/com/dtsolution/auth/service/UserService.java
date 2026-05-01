package com.dtsolution.auth.service;

import com.dtsolution.auth.domain.User;
import com.dtsolution.auth.dto.AuthDto;
import com.dtsolution.auth.mapper.UserMapper;
import com.dtsolution.auth.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthDto.SignupRequest dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userMapper.existsByEmpNo(dto.getEmpNo())) {
            throw new IllegalArgumentException("이미 등록된 사번입니다.");
        }
        if (userMapper.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        userMapper.insertUser(User.builder()
                .empNo(dto.getEmpNo())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .email(dto.getEmail())
                .build());

        userProfileMapper.insertEmptyProfile(dto.getEmpNo());
    }
}