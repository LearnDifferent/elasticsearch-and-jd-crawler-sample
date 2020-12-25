package com.example.escrawlerjd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射资源
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 使用 search 路径访问 index.html
        registry.addViewController("/search").setViewName("/index.html");
    }

    /**
     * 跨域相关配置
     *
     * @param registry 跨域相关注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .maxAge(36000)
                .allowCredentials(true);
    }
}