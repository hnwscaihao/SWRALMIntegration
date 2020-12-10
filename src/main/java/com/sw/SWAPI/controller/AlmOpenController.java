package com.sw.SWAPI.controller;


import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.util.MKSCommand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 *  @author: liuxiaoguang
 *  @Date: 2020/7/16 15:28
 *  @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value="/SWR")
public class AlmOpenController {

    private static final Log log = LogFactory.getLog(AlmOpenController.class);


    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/16 15:33
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception   获取ALM中所有用户信息
     */
    @RequestMapping(value="/Issue", method = RequestMethod.POST)
    public JSONObject getAllUsers(@RequestBody JSONObject jsonData){
    String str  = "{\n" +
        "    \"Issue_Back\": [\n" +
        "        {\n" +
        "            \"B_Relations\": [\n" +
        "                {\n" +
        "                    \"B_Item_ID\": \"x0400000000084DA6\",\n" +
        "                    \"B_Structure_ID\": \"x0400000000084DA6\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"B_Issue_ID\": \"574\"\n" +
        "        }\n" +
        "    ],\n" +
        "    \"Status\": \"Status:100\"\n" +
        "}";
        return JSONObject.parseObject(str);
    }

}
