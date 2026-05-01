package com.dtsolution.auth.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    private String empNo;
    private String dept;
    private String position;
    private String phone;
    private LocalDateTime updatedAt;
}