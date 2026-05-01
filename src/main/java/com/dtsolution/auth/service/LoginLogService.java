package com.dtsolution.auth.service;

import com.dtsolution.auth.domain.LoginLog;
import com.dtsolution.auth.mapper.LoginLogMapper;
import com.dtsolution.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;
    private final UserMapper userMapper;

    private static final int MAX_FAIL     = 5;
    private static final int LOCK_MINUTES = 30;

    @Transactional
    public void recordSuccess(String empNo, String ip) {
        userMapper.resetFailCount(empNo, LocalDateTime.now());
        loginLogMapper.insertLog(LoginLog.builder()
                .empNo(empNo).ipAddress(ip)
                .success(true).reason("SUCCESS").build());
    }

    @Transactional
    public void recordFailure(String empNo, String ip, String reason) {
        var user = userMapper.findByEmpNo(empNo);
        if (user == null) return;

        userMapper.incrementFailCount(empNo);

        if (user.getFailCount() + 1 >= MAX_FAIL) {
            userMapper.lockAccount(empNo, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }

        loginLogMapper.insertLog(LoginLog.builder()
                .empNo(empNo).ipAddress(ip)
                .success(false).reason(reason).build());
    }

    @Transactional
    public void recordLogout(String empNo) {
        LoginLog log = loginLogMapper.findLatestActiveLog(empNo);
        if (log != null) {
            loginLogMapper.updateLogoutAt(log.getId(), LocalDateTime.now());
        }
    }
}