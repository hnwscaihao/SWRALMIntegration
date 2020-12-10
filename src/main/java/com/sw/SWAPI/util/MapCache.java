package com.sw.SWAPI.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import net.sf.ehcache.util.concurrent.ConcurrentHashMap;

/** 
 * 线程缓存
 * @author admin
 *
 */
public class MapCache {

	private final static ConcurrentHashMap<String,List<JSONObject>> currentMap = new ConcurrentHashMap<String,List<JSONObject>>();
	
	private final static ConcurrentHashMap<String,Map<String,String>> swSIdMap = new ConcurrentHashMap<String,Map<String,String>>();

	private final static ConcurrentHashMap<String,Map<String,List<String>>> swIdMap = new ConcurrentHashMap<String,Map<String,List<String>>>();
	
	/**
	 * 缓存下发数据
	 * @param key
	 * @param json
	 */
	public static void cacheVal(String key, JSONObject json){
		List<JSONObject> list = currentMap.get(key);
		if(list == null){
			list = new ArrayList<>();
			currentMap.put(key, list);
		}
		list.add(json);
	}
	
	/**
	 * 获取缓存好的数据
	 * @param key
	 * @return
	 */
	public static List<JSONObject> getList(String key){
		if(key != null){
			return currentMap.get(key);
		}
		return new ArrayList<>();
	}
	
	/**
	 * 清除缓存数据
	 * @param key
	 */
	public static void clearCache(String key){
		if(key != null){
			currentMap.remove(key);
		}
	}
	
	/**
	 * 缓存SW_SID - ALMID数据
	 * @param uuid
	 * @param SWSID
	 * @param almId
	 */
	public static void cacheSWSID(String uuid, String SWSID, String almId){
		Map<String,String> idMap = swSIdMap.get(uuid);
		if(idMap == null){
			idMap = new HashMap<String,String>();
			swSIdMap.put(uuid, idMap);
		}
		idMap.put(SWSID, almId);
	}
	
	/**
	 * 获取缓存的ALMID
	 * @param uuid
	 * @param SWSID
	 * @return
	 */
	public static String getCacheALMID(String uuid, String SWSID){
		Map<String,String> idMap = swSIdMap.get(uuid);
		if(idMap != null){
			idMap.get(SWSID);
		}
		return null;
	}
	
	/**
	 * 获取UUID缓存的数据
	 * @param uuid
	 * @param SWSID
	 * @return
	 */
	public static Map<String,String> getSWSIDMap(String uuid){
		Map<String,String> idMap = swSIdMap.get(uuid);
		if(idMap == null){
			idMap = new HashMap<String,String>();
		}
		return idMap;
	}
	
	/**
	 * 清除UUID缓存数据
	 * @param uuid
	 */
	public static void clearSWSIDCache(String uuid){
		if(uuid != null){
			swSIdMap.remove(uuid);
		}
	}
	
	/**
	 * 根据UUID缓存同一批次下发数据的SWID-ALMID
	 * @param uuid
	 * @param SWID
	 * @param almId
	 */
	public static void cacheSWID(String uuid, String SWID, String almId){
		Map<String,List<String>> swMap = swIdMap.get(uuid);
		if(swMap == null){
			swMap = new HashMap<String,List<String>>();
			swIdMap.put(uuid, swMap);
		}
		List<String> swList = swMap.get(SWID);
		if(swList == null){
			swList = new ArrayList<String>();
		}
		swList.add(almId);
	}
	
	/**
	 * 根据UUI的获取数据
	 * @param uuid
	 * @return
	 */
	public static Map<String,List<String>> getSWIDCacheMap(String uuid){
		if(uuid != null){
			return swIdMap.get(uuid);
		}
		return null;
	}
	
	/**
	 * 清除缓存数据
	 * @param uuid
	 */
	public static void clearSWIDCache(String uuid){
		if(uuid != null){
			swIdMap.remove(uuid);
		}
	}
}
