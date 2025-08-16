package org.example.riskwarningsystembackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于配置跨域资源共享(CORS)等相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 允许跨域访问的源站地址数组，从配置文件中读取
     */
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * 配置跨域资源共享(CORS)映射规则
     *
     * @param registry CORS注册器，用于添加跨域配置映射
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置/api/**路径下的接口支持跨域访问，允许指定源站、HTTP方法和请求头
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
