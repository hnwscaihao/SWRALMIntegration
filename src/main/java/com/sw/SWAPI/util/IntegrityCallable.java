package com.sw.SWAPI.util;

import java.util.List;
import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;

public class IntegrityCallable implements Callable<String>{
	
	private List<JSONObject> listData ;
	
	public IntegrityCallable(List<JSONObject> listData){
		this.listData = listData;
	}
	
	
	@Override
	public String call() throws APIException, MsgArgumentException, Exception {
		IntegrityUtil util = new IntegrityUtil();
		//return util.dealData(listData);
		return null;
	}

	public List<JSONObject> getListData() {
		return listData;
	}

	public void setListData(List<JSONObject> listData) {
		this.listData = listData;
	}

}
