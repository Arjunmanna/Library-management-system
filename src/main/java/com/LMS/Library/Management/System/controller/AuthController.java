package com.LMS.Library.Management.System.controller;

import com.LMS.Library.Management.System.exception.UserException;
import com.LMS.Library.Management.System.payload.dto.UserDTO;
import com.LMS.Library.Management.System.payload.request.ForgetPasswordRequest;
import com.LMS.Library.Management.System.payload.request.LoginRequest;
import com.LMS.Library.Management.System.payload.request.ResetPasswordRequest;
import com.LMS.Library.Management.System.payload.response.ApiResponse;
import com.LMS.Library.Management.System.payload.response.AuthResponse;
import com.LMS.Library.Management.System.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandeler(@RequestBody @Valid UserDTO req) throws UserException {
        AuthResponse res = authService.signup(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandeler(@RequestBody @Valid LoginRequest req) throws UserException {
        AuthResponse res = authService.login(req.getEmail(),req.getPassword());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody ForgetPasswordRequest req) throws UserException {
        authService.createPasswordResetToken(req.getEmail());
        ApiResponse res = new ApiResponse("A reset link was sent to your email.",true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest req) throws Exception {
        authService.resetPassword(req.getToken(),req.getPassword());
        ApiResponse res = new ApiResponse("password reset successfully",true);
        return ResponseEntity.ok(res);
    }
}
