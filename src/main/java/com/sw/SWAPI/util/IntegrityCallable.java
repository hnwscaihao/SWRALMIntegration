package com.sw.SWAPI.util;

import java.util.List;
import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.controller.AlmController;

public class IntegrityCallable implements Runnable{
	
	private List<JSONObject> listData ;
	
	public IntegrityCallable(List<JSONObject> listData){
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
		} catch(Exception e) {
			AlmController.log.info("线程处理出现问题：" + e.getMessage());
			e.printStackTrace();
		}
		util.executionSychSW(json);
	}

	public List<JSONObject> getListData() {
		return listData;
	}

	public void setListData(List<JSONObject> listData) {
		this.listData = listData;
	}

}
