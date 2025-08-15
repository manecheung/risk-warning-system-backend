package org.example.riskwarningsystembackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
//
//    /**
//     * 配置静态资源处理器。
//     * 告诉 Spring Boot 从哪里查找前端的静态文件（HTML, CSS, JS 等）。
//     * 在 Dockerfile 中，您将 vue-risk-dashboard/dist 复制到了 /app/static/。
//     * 因此，这里需要指向相对于 JAR 包运行目录的 'static/' 文件夹。
//     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**") // 匹配所有请求路径
//                .addResourceLocations("file:./static/"); // 指向 JAR 包同级目录下的 'static' 文件夹
//    }
//
//    /**
//     * 配置视图控制器，用于处理单页应用的路由回退（History Mode）。
//     * 当浏览器直接访问非根路径（如 /supply-chain）时，Spring Boot 会将请求转发到根路径，
//     * 从而加载 index.html，然后由前端路由接管。
//     */
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        // 处理单层路径，例如 /supply-chain
//        registry.addViewController("/{spring:\\w+}")
//                .setViewName("forward:/");
//
//        // 处理多层嵌套路径，例如 /supply-chain/details
//        registry.addViewController("/**/{spring:\\w+}")
//                .setViewName("forward:/");
//
//        // 处理带有文件扩展名的路径，例如 /somefile.html (如果它不是真正的静态文件)
//        registry.addViewController("/{spring:\\w+}.{spring:\\w+}")
//                .setViewName("forward:/");
//    }
}