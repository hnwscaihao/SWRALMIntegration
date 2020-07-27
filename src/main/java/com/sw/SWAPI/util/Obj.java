package com.sw.SWAPI.util;

public class Obj {

    public static String IsNull(Object obj){
        if(obj == null){
            return "";
        }else {
            return obj.toString();
        }
    }
}
