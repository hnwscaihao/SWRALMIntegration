package com.sw.SWAPI.util;

import com.alibaba.fastjson.JSONObject;
import com.sw.SWAPI.Error.MsgArgumentException;

public class Obj {

    public static boolean isEmptyOrNull(String str){
    	if(str == null){
            return true;
        }else if("".equals(str)){
            return true;
        }
    	return false;
    }
    
    //数据验证 创建文档必须的字段
    public static void verification(JSONObject jsonObject){
        if(jsonObject.get("Project") == null){
            System.out.println("Project不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","Project不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("SW_SID") == null){
            System.out.println("SW_SID不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","SW_SID不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("issue_Type") == null){
            System.out.println("issue_Type不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","issue_Type不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("SW_ID") == null){
            System.out.println("SW_ID不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","SW_ID不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("issue_id") == null){
            System.out.println("issue_id不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","issue_id不能为null "+jsonObject.get("SW_SID"));
        }
        if(jsonObject.get("item_name") == null){
            System.out.println("item_name不能为null "+jsonObject.get("SW_SID"));
            throw new MsgArgumentException("201","item_name不能为null "+jsonObject.get("SW_SID"));
        }
    }

}
