package com.dtsolution.auth.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class User {

    private Long id;
    private String empNo;
    private String password;
    private String name;
    private String email;
    private String role;
    private boolean enabled;
    private int failCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}