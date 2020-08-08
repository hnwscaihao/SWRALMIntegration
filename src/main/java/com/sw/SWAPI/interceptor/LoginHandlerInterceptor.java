//package com.sw.SWAPI.interceptor;
//
//import com.sw.SWAPI.Error.MsgArgumentException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.PrintWriter;
//
//@Component
//public class LoginHandlerInterceptor implements HandlerInterceptor {
//
//    @Value("${token}")
//    private String token;
//
//    //目标方法执行之前
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String longinToken =request.getHeader("access_Token");
//        System.out.println("登录token："+longinToken + " ==== " + "token："+token);
//        if(token.equals(longinToken)){
//            return true;
//        }else {
////            PrintWriter out =  response.getWriter();
////            out.append("token Validation failed!");
//            throw new MsgArgumentException("201","token Validation failed!");
////            return false;
//        }
//
//    }
//
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
//
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//
//    }
//}