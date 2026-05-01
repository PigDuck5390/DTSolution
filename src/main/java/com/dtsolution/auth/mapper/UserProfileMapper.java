package com.dtsolution.auth.mapper;

import com.dtsolution.auth.domain.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    void insertEmptyProfile(@Param("empNo") String empNo);

    UserProfile findByEmpNo(@Param("empNo") String empNo);
}