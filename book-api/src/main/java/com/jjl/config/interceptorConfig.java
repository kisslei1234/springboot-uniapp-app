package com.jjl.config;

import com.jjl.intercepter.PassportIntercepter;
import com.jjl.intercepter.UserTokenIntercepter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class interceptorConfig implements WebMvcConfigurer {
    @Bean
    public PassportIntercepter passportIntercepter() {
        return new PassportIntercepter();
    }
    @Bean
    public UserTokenIntercepter userTokenIntercepter(){return new UserTokenIntercepter();}
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportIntercepter())
                .addPathPatterns("/passport/getSMSCode");
        registry.addInterceptor(userTokenIntercepter())
                .addPathPatterns("/userInfo/modifyImage")
                .addPathPatterns("/userInfo/modifyUserInfo");
    }
}
