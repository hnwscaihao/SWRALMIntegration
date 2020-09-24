//package com.sw.SWAPI.interceptor;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class MyMvcConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private LoginHandlerInterceptor loginHandlerInterceptor;
//
//    public  void addInterceptors(InterceptorRegistry registry){
//        registry.addInterceptor(loginHandlerInterceptor).addPathPatterns("/**");
//    }
//
//}