package com.youlai.boot.portal.controller;

import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.dto.EmailLoginRequest;
import com.youlai.boot.portal.service.RegisterUserService;
import com.youlai.boot.security.model.AuthenticationToken;
import com.youlai.boot.security.model.EmailAuthenticationToken;
import com.youlai.boot.security.token.TokenManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "门户登录接口")
@RestController
@RequestMapping("/api/v1/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {

    private final RegisterUserService registerUserService;
    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;

    @Operation(summary = "发送登录邮箱验证码")
    @PostMapping("/email/code")
    public Result<Void> sendLoginEmailCode(@RequestParam String email) {
        registerUserService.sendLoginEmailCode(email);
        return Result.success();
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/email/login")
    public Result<AuthenticationToken> loginByEmail(@Valid @RequestBody EmailLoginRequest request) {
        EmailAuthenticationToken authToken = new EmailAuthenticationToken(request.getEmail(), request.getCode());

        Authentication authentication = authenticationManager.authenticate(authToken);

        AuthenticationToken token = tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return Result.success(token);
    }
}
