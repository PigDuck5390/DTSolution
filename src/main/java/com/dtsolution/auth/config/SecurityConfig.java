package com.dtsolution.auth.config;

import com.dtsolution.auth.security.CustomUserDetailsService;
import com.dtsolution.auth.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginLogService loginLogService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/signup", "/css/**", "/js/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("empNo")
                .passwordParameter("password")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .addLogoutHandler((req, res, auth) -> {
                    if (auth != null) loginLogService.recordLogout(auth.getName());
                })
            )
            .sessionManagement(session -> session
                .maximumSessions(1).maxSessionsPreventsLogin(false)
            )
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
            );

        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            loginLogService.recordSuccess(auth.getName(), getIp(req));
            res.sendRedirect("/home");
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            String empNo = req.getParameter("empNo");
            String errorCode = "error";
            if (ex instanceof LockedException)       errorCode = "locked";
            else if (ex instanceof DisabledException) errorCode = "disabled";
            else loginLogService.recordFailure(empNo, getIp(req), ex.getClass().getSimpleName());
            res.sendRedirect("/login?error=" + errorCode);
        };
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff == null || xff.isEmpty()) ? req.getRemoteAddr() : xff.split(",")[0].trim();
    }
}