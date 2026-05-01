package com.dtsolution.auth.mapper;

import com.dtsolution.auth.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    User findByEmpNo(@Param("empNo") String empNo);

    boolean existsByEmpNo(@Param("empNo") String empNo);

    boolean existsByEmail(@Param("email") String email);

    void insertUser(User user);

    void resetFailCount(@Param("empNo") String empNo,
                        @Param("now") LocalDateTime now);

    void incrementFailCount(@Param("empNo") String empNo);

    void lockAccount(@Param("empNo") String empNo,
                     @Param("lockedUntil") LocalDateTime lockedUntil);
}