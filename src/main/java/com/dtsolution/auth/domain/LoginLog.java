package com.dtsolution.auth.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    private Long id;
    private String empNo;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;
    private String ipAddress;
    private boolean success;
    private String reason;
}