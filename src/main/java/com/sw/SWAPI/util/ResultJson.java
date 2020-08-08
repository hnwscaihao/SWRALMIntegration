package com.sw.SWAPI.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ResultJson {

    public static JSONObject ResultJson(String str1,String str2){
        JSONObject resultData = new JSONObject();
        resultData.put(str1,str2);
        return resultData;
    }

    public static JSONObject ResultJsonAry(JSONArray jSONArray){
        JSONObject resultData = new JSONObject();
        resultData.put("status",200);
        resultData.put("data",jSONArray);
        return resultData;
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
