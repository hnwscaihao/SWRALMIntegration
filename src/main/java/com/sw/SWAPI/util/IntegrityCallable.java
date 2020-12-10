package com.sw.SWAPI.util;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.controller.AlmController;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 异步执行类
 */
public class IntegrityCallable implements Runnable {

    private List<JSONObject> listData;//需要执行的数据

    public IntegrityCallable(List<JSONObject> listData) {
        this.listData = listData;
    }

    @Override
    public void run() {
        IntegrityUtil util = new IntegrityUtil();
        JSONObject json = new JSONObject();
        try {
            AlmController.log.info("启动线程处理数据");
            json = util.dealData(listData);
        } catch (APIException e) {
            AlmController.log.info("线程处理出现问题：" + APIExceptionUtil.getMsg(e));
            e.printStackTrace();
        } catch (Exception e) {
            AlmController.log.info("线程处理出现问题：" + e.getMessage());
            e.printStackTrace();
        }
        AlmController.log.info("json:" + json);
        util.executionSychSw(json);
    }

    public List<JSONObject> getListData() {
        return listData;
    }

    public void setListData(List<JSONObject> listData) {
        this.listData = listData;
    }

}
