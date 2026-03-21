package org.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 对所有接口生效
                .allowedOrigins(
                        "http://localhost:8080",      // 前端 8080 端口
                        "http://127.0.0.1:8080",      // 前端 8080 端口的 IP 形式
                        "http://localhost:5173",      // 保留 Vue 默认端口（备用）
                        "http://127.0.0.1:5173"       // 保留 Vue 默认端口的 IP 形式
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许的请求头
                .allowCredentials(true) // 允许携带Cookie（如果需要）
                .maxAge(3600); // 预检请求缓存时间（秒）
    }
}