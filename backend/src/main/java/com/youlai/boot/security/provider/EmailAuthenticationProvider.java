package com.youlai.boot.security.provider;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.service.RegisterUserService;
import com.youlai.boot.security.exception.CaptchaValidationException;
import com.youlai.boot.security.model.EmailAuthenticationToken;
import com.youlai.boot.security.model.SysUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

/**
 * 邮箱验证码认证 Provider
 * <p>
 * 实现 Spring Security 的 {@link AuthenticationProvider} 接口，处理邮箱验证码登录认证。
 * <p>
 * 认证流程：
 * <ol>
 *   <li>根据邮箱从 register_user_information 表查询用户</li>
 *   <li>校验邮箱验证码（与 Redis 缓存比对）</li>
 *   <li>验证成功后删除验证码，防止重复使用</li>
 *   <li>返回已认证的 Authentication</li>
 * </ol>
 *
 * @see EmailAuthenticationToken
 * @see AuthenticationProvider
 */
@Slf4j
public class EmailAuthenticationProvider implements AuthenticationProvider {

    private final RegisterUserService registerUserService;
    private final RedisTemplate<String, Object> redisTemplate;

    public EmailAuthenticationProvider(RegisterUserService registerUserService, RedisTemplate<String, Object> redisTemplate) {
        this.registerUserService = registerUserService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = ((String) authentication.getPrincipal()).trim().toLowerCase();
        String inputVerifyCode = (String) authentication.getCredentials();

        if (StrUtil.isBlank(email)) {
            log.warn("邮箱验证码登录失败：邮箱为空");
            throw new CaptchaValidationException("邮箱不能为空");
        }
        if (StrUtil.isBlank(inputVerifyCode)) {
            log.warn("邮箱验证码登录失败：验证码为空，邮箱={}", email);
            throw new CaptchaValidationException("验证码不能为空");
        }

        RegisterUserInformation registerUser = registerUserService.getOne(
                new LambdaQueryWrapper<RegisterUserInformation>()
                        .eq(RegisterUserInformation::getEmail, email)
        );

        if (registerUser == null) {
            log.warn("邮箱验证码登录失败：用户不存在，邮箱={}", email);
            throw new CaptchaValidationException("该邮箱未注册");
        }

        String cacheKey = StrUtil.format(RedisConstants.Captcha.EMAIL_LOGIN_CODE, email);
        String cachedVerifyCode = (String) redisTemplate.opsForValue().get(cacheKey);

        if (cachedVerifyCode == null) {
            log.warn("邮箱验证码登录失败：验证码已过期，邮箱={}", email);
            throw new CaptchaValidationException("验证码已过期，请重新获取");
        }

        if (!StrUtil.equals(inputVerifyCode, cachedVerifyCode)) {
            log.warn("邮箱验证码登录失败：验证码错误，邮箱={}", email);
            throw new CaptchaValidationException("验证码错误");
        }

        redisTemplate.delete(cacheKey);

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        SysUserDetails userDetails = new SysUserDetails();
        userDetails.setUserId(registerUser.getId());
        userDetails.setUsername(registerUser.getEmail());
        userDetails.setEnabled(true);
        userDetails.setDeptId(null);
        userDetails.setPassword(null);
        userDetails.setAuthorities(authorities);
        userDetails.setDataScopes(null);

        log.info("邮箱验证码登录成功：邮箱={}, userId={}", email, registerUser.getId());

        return EmailAuthenticationToken.authenticated(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
