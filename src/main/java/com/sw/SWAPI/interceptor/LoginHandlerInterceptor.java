//package com.sw.SWAPI.interceptor;
//
//import com.alibaba.fastjson.JSONObject;
//import com.sw.SWAPI.Error.MsgArgumentException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
//@Component
//@PropertySource(value = {"classpath:integrity.properties"})
//public class LoginHandlerInterceptor implements HandlerInterceptor {
//
//    @Value("${token}")
//    private String token;
//
//    //目标方法执行之前
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String longinToken ="";
////        String longinToken =request.getHeader("Access_Token");
//        try {
//            BufferedReader streamReader = new BufferedReader( new InputStreamReader(request.getInputStream(), "UTF-8"));
//            StringBuilder responseStrBuilder = new StringBuilder();
//            String inputStr;
//            while ((inputStr = streamReader.readLine()) != null)
//                responseStrBuilder.append(inputStr);
//
//            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());
//            longinToken = jsonObject.get("Access_Token").toString();
////            System.out.println(longinToken);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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