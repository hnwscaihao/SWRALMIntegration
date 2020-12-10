package com.sw.SWAPI.util;

import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItemIterator;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: ALM异常处理类
 */
public class APIExceptionUtil {

	/**
	 * 获取ALM异常信息
	 * @param e
	 * @return String
	 */
	public static String getMsg(APIException e) {
		String msg = e.getMessage();
		Response res = e.getResponse();
		if (res != null) {
			WorkItemIterator wit = res.getWorkItems();
			try {
				while (wit.hasNext()) {
					wit.next();
				}
			} catch (APIException e1) {
				String message = e1.getMessage();
				if (message != null) {
					msg = message;
				}
			}
		}
		return msg;
	}
}
