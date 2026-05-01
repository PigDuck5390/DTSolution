package com.dtsolution.auth.security;

import com.dtsolution.auth.domain.User;
import com.dtsolution.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String empNo) throws UsernameNotFoundException {
        User user = userMapper.findByEmpNo(empNo);
        if (user == null) {
            throw new UsernameNotFoundException("인증 실패");
        }

        boolean accountNonLocked = user.getLockedUntil() == null
                || user.getLockedUntil().isBefore(LocalDateTime.now());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmpNo())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .disabled(!user.isEnabled())
                .accountLocked(!accountNonLocked)
                .build();
    }
}