package com.sw.SWAPI.Error;

import com.alibaba.fastjson.JSONObject;
import com.sw.SWAPI.damain.Project;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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