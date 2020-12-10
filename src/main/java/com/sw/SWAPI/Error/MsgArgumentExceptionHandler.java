package com.sw.SWAPI.Error;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 异常信息处理
 */
@ControllerAdvice
public class MsgArgumentExceptionHandler {

    @ExceptionHandler(value = MsgArgumentException.class)
    @ResponseBody
    public JSONObject errorHandler(MsgArgumentException e) {
        e.printStackTrace();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message",e.getMessage());
        jsonObject.put("status",e.getStatus());
        return  jsonObject;
    }
}