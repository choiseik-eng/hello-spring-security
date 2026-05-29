package kr.ac.hansung.controller;

import kr.ac.hansung.dto.UserDto;
import kr.ac.hansung.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        // 기존 코드 유지
        model.addAttribute("user", new UserDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupProcess(@ModelAttribute("user") UserDto dto, Model model) {
        // 기존 회원가입 중복 체크 및 흐름 유지
        if (userService.existsByEmail(dto.getEmail())) {
            model.addAttribute("emailExists", true);
            return "signup";
        }
        userService.signup(dto);
        return "redirect:/login?registered";
    }

    // 과제 필수 요구사항: 비밀번호 변경 화면 이동 매핑 추가
    @GetMapping("/user/change-password")
    public String changePasswordForm() {
        return "user/change-password";
    }

    // 과제 필수 요구사항: 비밀번호 변경 처리 POST 매핑 추가 (더티 체킹 연동)
    @PostMapping("/user/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model,
                                 RedirectAttributes ra) {

        // 1차 검증: 입력한 두 새 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "user/change-password";
        }

        try {
            // 시큐리티 컨텍스트에서 로그인한 사람의 이메일(Username)을 꺼내 서비스에 전달
            userService.changePassword(userDetails.getUsername(), currentPassword, newPassword);

            // 변경 성공 시 리다이렉트 후 일회성 메시지 전달
            ra.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/home";

        } catch (IllegalArgumentException e) {
            // 현재 비밀번호가 틀리는 등 예외 발생 시 에러 메시지를 들고 화면으로 복귀
            model.addAttribute("error", e.getMessage());
            return "user/change-password";
        }
    }
}