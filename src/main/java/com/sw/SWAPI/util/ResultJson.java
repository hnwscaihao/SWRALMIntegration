package com.sw.SWAPI.util;

import com.alibaba.fastjson.JSONObject;

public class ResultJson {

    public static JSONObject ResultJson(JSONObject jsonObject){


        return jsonObject;
    }

    public static JSONObject ResultStr(String name,String str){
        JSONObject resultData = new JSONObject();
        resultData.put("status",200);
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put(name,str);
        jsonObject.put("data",jsonObject1);
        resultData.put("body",jsonObject);
        return resultData;
    }
}
