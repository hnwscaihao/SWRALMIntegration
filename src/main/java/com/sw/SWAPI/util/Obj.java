package com.sw.SWAPI.util;

import com.alibaba.fastjson.JSONObject;
import com.sw.SWAPI.Error.MsgArgumentException;

public class Obj {

    public static String IsNull(Object obj){
        if(obj == null){
            return "";
        }else {
            return obj.toString();
        }
    }
    //数据验证 创建文档必须的字段
    public static void verification(JSONObject jsonObject){
        if(jsonObject.get("Project") == null){
            throw new MsgArgumentException("201","Project不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("Assigned_User") == null){
            throw new MsgArgumentException("201","Assigned_User不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("SW_SID") == null){
            throw new MsgArgumentException("201","SW_SID不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("issue_Type") == null){
            throw new MsgArgumentException("201","issue_Type不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("SW_ID") == null){
            throw new MsgArgumentException("201","SW_ID不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("issue_id") == null){
            throw new MsgArgumentException("201","issue_id不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("item_name") == null){
            throw new MsgArgumentException("201","item_name不能为null "+jsonObject.get("SW_SID"));
        }
    }
}
