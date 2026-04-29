package com.cgad.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 *
 * 【知识点】CORS（Cross-Origin Resource Sharing）跨域资源共享：
 *   - 浏览器的同源策略默认禁止跨域请求（协议+域名+端口不同）
 *   - 前端 5173 端口访问后端 8080 端口属于跨域
 *   - 后端需要配置 CORS 允许前端访问
 *
 * 【知识点】开发环境 vs 生产环境：
 *   - 开发环境：Vite dev server 通过 proxy 代理解决跨域，此配置也生效
 *   - 生产环境：前端打包后部署到同域 Nginx，或依赖此 CORS 配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
