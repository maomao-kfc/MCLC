package com.youlai.boot.security.filter;

import cn.hutool.core.util.StrUtil;
import com.youlai.boot.common.constant.SecurityConstants;
import com.youlai.boot.security.token.TokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Token 认证校验过滤器
 *
 * @author wangtao
 * @since 2025/3/6 16:50
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Token 管理器
     */
    private final TokenManager tokenManager;

    public TokenAuthenticationFilter(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * 校验 Token ，包括验签和是否过期
     * 如果 Token 有效，将 Token 解析为 Authentication 对象，并设置到 Spring Security 上下文中
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (StrUtil.isNotBlank(authorizationHeader)
                    && authorizationHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX)) {

                String rawToken = authorizationHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
                boolean isValidToken = tokenManager.validateToken(rawToken);
                if (isValidToken) {
                    Authentication authentication = tokenManager.parseToken(rawToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
