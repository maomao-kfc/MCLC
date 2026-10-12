package com.youlai.boot.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serial;
import java.util.Collection;

/**
 * 邮箱验证码认证 Token
 * <p>
 * 用于邮箱验证码登录场景，遵循 Spring Security 认证模型：
 * <ul>
 *   <li>未认证状态：principal 为邮箱，credentials 为验证码</li>
 *   <li>已认证状态：principal 为用户详情，credentials 为 null</li>
 * </ul>
 *
 * @since 2.20.0
 */
public class EmailAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 622L;

    private final Object principal;
    private final Object credentials;

    public EmailAuthenticationToken(String email, String verifyCode) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = email;
        this.credentials = verifyCode;
        setAuthenticated(false);
    }

    public EmailAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        super.setAuthenticated(true);
    }

    public static EmailAuthenticationToken authenticated(Object principal, Collection<? extends GrantedAuthority> authorities) {
        return new EmailAuthenticationToken(principal, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
