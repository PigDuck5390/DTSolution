package com.dtsolution.auth.mapper;

import com.dtsolution.auth.domain.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface LoginLogMapper {

    void insertLog(LoginLog log);

    LoginLog findLatestActiveLog(@Param("empNo") String empNo);

    void updateLogoutAt(@Param("id") Long id,
                        @Param("logoutAt") LocalDateTime logoutAt);
}