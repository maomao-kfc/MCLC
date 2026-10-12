package com.youlai.boot.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Druid 数据源配置（手动配置，兼容 Spring Boot 4）
 * <p>
 * 由于 druid-spring-boot-3-starter 的自动配置类引用了
 * Spring Boot 4 中已移除的 DataSourceAutoConfiguration，
 * 因此改用纯 druid 依赖 + 手动配置方式。
 * <p>
 * 注意：Druid 的监控页面(StatViewServlet/WebStatFilter)
 * 依赖 javax.servlet，与 Spring Boot 4(jakarta.servlet)不兼容，
 * 如需监控功能请使用 Spring Boot Actuator 或其他方案。
 */
@Configuration
public class DruidConfig {

    /**
     * 配置 Druid 数据源，绑定 spring.datasource 下的属性
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }
}