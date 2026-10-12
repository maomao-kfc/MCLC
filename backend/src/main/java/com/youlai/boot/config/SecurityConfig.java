package com.youlai.boot.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.ArrayUtil;
import com.youlai.boot.config.property.SecurityProperties;
import com.youlai.boot.core.filter.RateLimiterFilter;
import com.youlai.boot.security.filter.CaptchaValidationFilter;
import com.youlai.boot.security.filter.TokenAuthenticationFilter;
import com.youlai.boot.security.handler.MyAccessDeniedHandler;
import com.youlai.boot.security.handler.MyAuthenticationEntryPoint;
import com.youlai.boot.portal.service.RegisterUserService;
import com.youlai.boot.security.provider.EmailAuthenticationProvider;
import com.youlai.boot.security.provider.SmsAuthenticationProvider;
import com.youlai.boot.security.provider.WechatMiniAuthenticationProvider;
import com.youlai.boot.security.token.TokenManager;
import com.youlai.boot.security.service.SysUserDetailsService;
import com.youlai.boot.system.service.ConfigService;
import com.youlai.boot.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// WebSecurityCustomizer 已移除，unsecuredUrls 改用 HttpSecurity.permitAll()
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * Spring Security 核心配置类
 *
 * 职责：
 * 1. 组装SecurityFilterChain：RateLimiterFilter → CaptchaValidationFilter → TokenAuthenticationFilter → Authorization
 * 2. 配置4种AuthenticationProvider：Dao(用户名密码)/Sms(短信验证码)/Email(邮箱验证码)/WechatMini(微信小程序)
 * 3. 定义公开路径(ignoreUrls, 23个)和内部路径(unsecuredUrls, 7个Swagger/Actuator)
 * 4. 配置CORS/CSP/Session策略(stateless)/异常处理(AccessDenied/AuthenticationEntryPoint)
 *
 * 安全架构详见：docs/08-rbac-permission.md
 *
 * @see SecurityProperties ignoreUrls/unsecuredUrls配置
 * @see TokenAuthenticationFilter JWT解析过滤器
 * @see RateLimiterFilter Redis令牌桶限流
 * @see CaptchaValidationFilter 验证码校验(仅/login接口)
 * @author Ray.Hao
 * @since 2023/2/17
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private final TokenManager tokenManager;
    private final UserService userService;
    private final SysUserDetailsService userDetailsService;
    private final RegisterUserService registerUserService;

    private final CodeGenerator codeGenerator;
    private final ConfigService configService;
    private final SecurityProperties securityProperties;

    private final WxMaService wxMaService;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 配置安全过滤链 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requestMatcherRegistry -> {
                            // 配置无需登录即可访问的公开接口（配置文件方式）
                            String[] ignoreUrls = securityProperties.getIgnoreUrls();
                            if (ArrayUtil.isNotEmpty(ignoreUrls)) {
                                requestMatcherRegistry.requestMatchers(ignoreUrls).permitAll();
                            }
                            // 非安全端点路径（Swagger/API文档等），使用permitAll替代WebSecurity.ignoring
                            String[] unsecuredUrls = securityProperties.getUnsecuredUrls();
                            if (ArrayUtil.isNotEmpty(unsecuredUrls)) {
                                requestMatcherRegistry.requestMatchers(unsecuredUrls).permitAll();
                            }
                            // 其他所有请求需登录后访问
                            requestMatcherRegistry.anyRequest().authenticated();
                        }
                )
                .exceptionHandling(configurer ->
                        configurer
                                .authenticationEntryPoint(new MyAuthenticationEntryPoint()) // 未认证异常处理器
                                .accessDeniedHandler(new MyAccessDeniedHandler()) // 无权限访问异常处理器
                )

                // 禁用默认的 Spring Security 特性，适用于前后端分离架构
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态认证，不使用 Session
                )
                .csrf(AbstractHttpConfigurer::disable)      // 禁用 CSRF 防护，前后端分离无需此防护机制
                .formLogin(AbstractHttpConfigurer::disable) // 禁用默认的表单登录功能，前后端分离采用 Token 认证方式
                .httpBasic(AbstractHttpConfigurer::disable) // 禁用 HTTP Basic 认证，避免弹窗式登录
                // 禁用 X-Frame-Options 响应头，允许页面被嵌套到 iframe 中
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 限流过滤器
                .addFilterBefore(new RateLimiterFilter(redisTemplate, configService), UsernamePasswordAuthenticationFilter.class)
                // 验证码校验过滤器
                .addFilterBefore(new CaptchaValidationFilter(redisTemplate, codeGenerator), UsernamePasswordAuthenticationFilter.class)
                // 验证和解析过滤器
                .addFilterBefore(new TokenAuthenticationFilter(tokenManager), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 已移除 WebSecurityCustomizer Bean。
     * <p>
     * 原先使用 web.ignoring() 绕过Spring Security过滤器链（用于Swagger/API文档路径），
     * Spring Security 6.x 不推荐此方式，会产生WARN日志。
     * 现已将 unsecuredUrls 合并到 HttpSecurity.authorizeHttpRequests 的 permitAll() 中，
     * 这些路径仍允许匿名访问，但会经过安全过滤器链（可记录日志、限流等）。
     */

    /**
     * 默认密码认证的 Provider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    /**
     * 短信验证码认证 Provider
     */
    @Bean
    public SmsAuthenticationProvider smsAuthenticationProvider() {
        return new SmsAuthenticationProvider(userService, redisTemplate);
    }

    /**
     * 邮箱验证码认证 Provider
     */
    @Bean
    public EmailAuthenticationProvider emailAuthenticationProvider() {
        return new EmailAuthenticationProvider(registerUserService, redisTemplate);
    }

    /**
     * 微信小程序认证 Provider
     */
    @Bean
    public WechatMiniAuthenticationProvider wechatMiniAuthenticationProvider(
            WxMaService wxMaService,
            SysUserDetailsService sysUserDetailsService
    ) {
        return new WechatMiniAuthenticationProvider(wxMaService, sysUserDetailsService);
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider,
            SmsAuthenticationProvider smsAuthenticationProvider,
            WechatMiniAuthenticationProvider wechatMiniAuthenticationProvider,
            EmailAuthenticationProvider emailAuthenticationProvider
    ) {
        return new ProviderManager(
                daoAuthenticationProvider,
                smsAuthenticationProvider,
                wechatMiniAuthenticationProvider,
                emailAuthenticationProvider
        );
    }

}
