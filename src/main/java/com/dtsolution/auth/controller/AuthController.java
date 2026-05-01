package com.dtsolution.auth.controller;

import com.dtsolution.auth.dto.AuthDto;
import com.dtsolution.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            String msg = switch (error) {
                case "locked"   -> "로그인 시도 5회 초과 - 30분간 계정이 잠겼습니다.";
                case "disabled" -> "비활성화된 계정입니다. 관리자에게 문의하세요.";
                default         -> "사번 또는 비밀번호가 올바르지 않습니다.";
            };
            model.addAttribute("errorMsg", msg);
        }
        if (logout != null) model.addAttribute("logoutMsg", "정상적으로 로그아웃되었습니다.");
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupForm", new AuthDto.SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") AuthDto.SignupRequest form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "signup";
        try {
            userService.signup(form);
            redirectAttributes.addFlashAttribute("successMsg", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("empNo", user.getUsername());
        return "home";
    }

    @GetMapping("/")
    public String root() { return "redirect:/home"; }
}