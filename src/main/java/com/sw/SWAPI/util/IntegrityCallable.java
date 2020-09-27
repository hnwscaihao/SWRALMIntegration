package com.sw.SWAPI.util;

import java.util.List;
import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;

public class IntegrityCallable implements Runnable{
	
	private List<JSONObject> listData ;
	
	public IntegrityCallable(List<JSONObject> listData){
		this.listData = listData;
	}
	
	
	@Override
	public void run() {
		IntegrityUtil util = new IntegrityUtil();
		try {
			util.dealData(listData);
		} catch (APIException e) {
			e.printStackTrace();
		}
	}

	public List<JSONObject> getListData() {
		return listData;
	}

	public void setListData(List<JSONObject> listData) {
		this.listData = listData;
	}

}
