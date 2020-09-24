package com.sw.SWAPI.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.websocket.server.PathParam;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sw.SWAPI.util.Obj.IsNull;
import static com.sw.SWAPI.util.Obj.verification;
import static com.sw.SWAPI.util.ResultJson.*;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value = "/alm")
public class AlmController {

//    @Autowired
//    private IntegrityFactory integrityFactory;

    public static final Log log = LogFactory.getLog(AlmController.class);
    //    private MKSCommand mks = new MKSCommand();
    @Autowired
    private CacheManager cacheManager;

//    @Value("${filePath}")
//    private String filePath;

    
    //    String filePath = "C:\\\\SWFile";
    Cache cache = null;
//    @Value("${host}")
//    private String host;
//
//    @Value("${port}")
//    private int port;
//
//    @Value("${loginName}")
//    private String loginName;
//
    @Value("${token}")
    private String token;

    private IntegrityUtil util = new IntegrityUtil();
    
    private MKSCommand mks;
    /**
     * @Description
     * @Author liuxiaoguang
     * @Date 2020/7/16 15:33
     * @Param []
     * @Return com.alibaba.fastjson.JSONObject
     * @Exception 获取ALM中所有用户信息
     */
    @RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
    public JSONArray getAllUsers() {

        List<User> allUsers = new ArrayList<User>();
        try {
            log.info("开始链接：");
            if(mks == null){
            	mks = new MKSCommand();
            }
            allUsers = mks.getAllUsers(Arrays.asList("fullname", "name", "Email"));
        } catch (APIException e) {
            log.info("error: " + "查询所有用户错误！" + e.getMessage());
            e.printStackTrace();
        }

//        mks.close(host,port,loginName);

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < allUsers.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            User user = allUsers.get(i);
            jsonObject.put("userName", user.getUserName());
            jsonObject.put("login_ID", user.getLogin_ID());
            jsonObject.put("email", user.getEmail());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    @RequestMapping(value = "/getAllUsersByProject", method = RequestMethod.POST)
    public JSONArray getAllUsers(@RequestBody JSONObject jsonData) {
        getToken(IsNull(jsonData.get("Access_Token")));
        String project = IsNull(jsonData.get("project"));
        String type = IsNull(jsonData.get("type"));//根据类型判断获取的动态组，Component获取Review Committee Leader，其他获取
        List<String> dynamicGroups = new ArrayList<String>();
        if ("Component requirements Specification Document".equals(type)) {//Component到In Review,查询Review Committee Leader
            dynamicGroups.add("Review Committee DG");
            dynamicGroups.add("Review Committee Leader DG");
        } else {//其他到In Approve，查询Project Manager DG
            dynamicGroups.add("Project Manager DG");
        }

        log.info("project-----" + project);
        if(mks == null){
        	mks = new MKSCommand();
        }
        List<User> allUsers = new ArrayList<User>();
        try {
            log.info("开始链接：");
            allUsers = mks.getProjectDynaUsers(project, dynamicGroups);
        } catch (APIException e) {
            log.info("error: " + "查询所有用户错误！" + e.getMessage());
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < allUsers.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            User user = allUsers.get(i);
            jsonObject.put("userName", user.getUserName());
            jsonObject.put("login_ID", user.getLogin_ID());
            jsonObject.put("email", user.getEmail());
            jsonArray.add(jsonObject);
        }
        log.info("查询成功！" + jsonArray);
        return jsonArray;
    }

    /**
     * @Description
     * @Author liuxiaoguang
     * @Date 2020/7/17 14:53
     * @Param []
     * @Return com.alibaba.fastjson.JSONObject
     * @Exception 获取ALM中Project列表
     */
    @RequestMapping(value = "/getAllProjects", method = RequestMethod.POST)
    public JSONArray getAllProject(@RequestBody JSONObject jsonData) {
        getToken(IsNull(jsonData.get("Access_Token")));
        log.info("-------------查询所用项目-------------");
        if(mks == null){
        	mks = new MKSCommand();
        }
        List<Project> allUsers = new ArrayList<Project>();
        try {
            allUsers = mks.getAllprojects(Arrays.asList("backingIssueID", "name"));
        } catch (APIException e) {
            log.info("error: " + "查询所有project错误！" + e.getMessage());
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < allUsers.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Project project = allUsers.get(i);
            jsonObject.put("project", project.getProject());
            jsonObject.put("PID", project.getPID());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    
	/**
     * @Description
     * @Author liuxiaoguang
     * @Date 2020/7/22 10:02
     * @Param [jsonData]
     * @Return com.alibaba.fastjson.JSONObject
     * @Exception 创建 修改 删除 移动文档条目
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/releaseData", method = RequestMethod.POST)
    public JSONObject createDocument(@RequestBody JSONObject jsonData) {
        getToken(IsNull(jsonData.get("Access_Token")));
        log.info("-------------数据下发-------------");
		if(mks == null){
        	mks = new MKSCommand();
        }
        String docId = "";
        log.info("参数信息：");
        log.info(jsonData);
        cache = cacheManager.getCache("orgCodeFindAll");
        String docUUID = jsonData.getString(Constants.DOC_UUID);
        log.info("-------------docUUID + "+ docUUID);
        String end = jsonData.get("end").toString();//结尾标记，标识本次文档数据传输完毕
       
        //移动到父id下面的位置 参数 first  last  before:name  after:name
        if ("true".equals(end)) {  //所有参数存入缓存
        	Map<String,String> SWSIDMap = new HashMap<>();//存放已保存ID
        	String project = jsonData.get("Project").toString();//创建到的目标项目，通过分别查询 docId、docId + Project，判断是否需要创建分支
            List<JSONObject> listData = null;//保存排序后条目数据
            Element docEle = cache.get(docUUID);
        	if(docEle == null){
        		listData = new ArrayList<>();
        	}else
        		listData = (List<JSONObject>)docEle.getObjectValue();
            listData.add(jsonData);
            
            List<JSONObject> contentsList = new ArrayList<>(listData.size());
            //实现排序方法
            JSONObject docJSON = sortContainsAndGetDoc(listData,contentsList);
//            Collections.sort(list, Comparator.comparingInt(String::length).thenComparing(Comparator.comparing(String::toLowerCase, Comparator.reverseOrder())).
//                    thenComparing(Comparator.reverseOrder()));
            log.info("排序后 List:"  ) ; 
            listData = null;//置空，回收对象
            Map<String,JSONObject> SWJSONMap = new HashMap<String,JSONObject>(contentsList.size() *4/3); 
            Map<String,Boolean> SWDealMap = new HashMap<String,Boolean>(contentsList.size() *4/3);
            Map<String,List<String>> SWMap = new HashMap<String,List<String>>(contentsList.size() *4/3);//记录SW_ID对应的ALMID
            for(JSONObject obj : contentsList){
            	String swSid = obj.getString(Constants.SW_SID_FIELD);
            	SWJSONMap.put(swSid, obj);// 通过Map将每个JSON记录
            	SWDealMap.put(swSid, false);//通过Map记录SWSID对应数据是否处理。防止排序失败导致数据处理异常
            	log.info(obj);
            }
//            mks.initMksCommand(host, port, loginName, passWord);
            boolean newDoc = false; //判断是否是新增文档
            boolean branch = false;//判断是否是复用文档
            String doc_SW_SID = null;
            String doc_SW_ID = null;
            String assignedUser = IsNull(docJSON.get("Assigned_User"));;//新增后修改文档状态时用户assignedUser = IsNull(contentObj.get("Assigned_User"));
            log.info("总共---------------" + contentsList.size() + "数据，开始下发！");
            if(docJSON != null){//处理文档。每次进入都要更新SW_SID和SW_ID
            	/** 判断：
            	 * 	1. 通过判断操作类型，add时，可能存在创建分支的情况。如果SW_SID未创建文档，则创建新文档，否则创建分支
            	 * 	2. update时，则通过old_SW_SID更新文档
            	 */
            	String action_Type = IsNull(docJSON.get("action_Type"));
            	doc_SW_SID = IsNull(docJSON.get("SW_SID"));
            	doc_SW_ID = IsNull(docJSON.get("SW_ID"));
                String issue_Type = IsNull(docJSON.get("issue_Type"));
            	if("add".equals(action_Type)){//
            		List<Map<String,String>> docList = null;
            		try {
            			docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, null);
					} catch (APIException e) {
						log.info("查询数据：" + APIExceptionUtil.getMsg(e));
					}
            		log.info("判断文档是否存在"+docList.size());
                    log.info("判断文档是否存在"+docList);
            		if(docList == null || docList.isEmpty() || docList.size()==0){
            			docId = createDoc(docJSON,SWSIDMap, mks);//当前未创建，创建新的文档
            		}else{
            			branch = true;
            			Map<String,String> origInfo = null;
            			for(Map<String,String> docInfo : docList){
            				if(project.equals(docInfo.get("Project"))){//如果目标项目已经存在数据，不创建
            					throw new MsgArgumentException("206","Document has created in project: [" +project+ "]!");
            				}
            				if(origInfo == null){
            					origInfo = docInfo;
            				}else{
            					String createdDate = docInfo.get("Created Date");
            					String orCreatedDate = origInfo.get("Created Date");
            					Date target = new Date(createdDate);
            					Date orgi = new Date(orCreatedDate);
            					if(target.before(orgi)){//比对，获取最开始的一条数据
            						origInfo = docInfo;
            					}
            				}
            			}
            			try {
							docId = mks.branchDocument(origInfo.get("ID"), project);
							/** 复用后更新文档SW_ID、SW_SID、ISSUEID等信息*/
							origInfo.put("ID", docId);
							updateDoc( origInfo,docJSON,mks);
							/** 复用后更新文档SW_ID、SW_SID、ISSUEID等信息*/
							log.info("分支创建成功 ："+ project + " | " + docId);
            			} catch (APIException e) {
            				log.info("分支创建失败 ："+ APIExceptionUtil.getMsg(e));
            				SWSIDMap = null;
            				contentsList = null;
            				docJSON = null;
            				SWJSONMap = null;
            				SWDealMap = null;
            				SWMap = null;
							e.printStackTrace();
						}
            		}
            		newDoc = true;
            	}else if("update".equals(action_Type)){
            		doc_SW_SID = IsNull(docJSON.get("Old_SW_SID"));
            		List<Map<String,String>> docList = null;
            		try {
            			docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
					} catch (APIException e) {
						log.info("查询数据：" + APIExceptionUtil.getMsg(e));
					}
            		if(docList == null || docList.isEmpty()){//更新判断文档是否存在
            			throw new MsgArgumentException("204","Document hadn't create。Please check you action type!");
                	}else{
                		Map<String,String> docInfo = docList.get(0);
                		String curState = docInfo.get("State");
                		if(!Constants.DOC_INIT_STATE.equals(curState)){//文档状态判断
                			throw new MsgArgumentException("205","Document now is in reivew or published, can not update!");
                		}
                		docId = docInfo.get("ID");
                        updateDoc( docInfo,docJSON,mks);
                	}
            	}
            	SWSIDMap.put(doc_SW_SID, docId);
            }
            /** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理*/
            List<String> branchDeleIssueList = null;//记录复用文档后，需要移除的数据
            if(branch ){
				try {
					branchDeleIssueList = mks.getDocContents(docId, SWSIDMap);
					log.info("分支查询数据为： " + Arrays.asList(branchDeleIssueList));
					log.info("分支查询数据为： " + JSON.toJSONString(SWSIDMap));
				} catch (APIException e1) {
					log.info("分支文档条目查询失败");
					log.info(APIExceptionUtil.getMsg(e1));
				}
            }	
            /** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理*/
            /** 处理数据1*/
            for (int i = 0; i < contentsList.size(); i++) {
                dealContentJson(contentsList.get(i), SWSIDMap, SWMap, SWDealMap, SWJSONMap, docId, mks, branch, newDoc, branchDeleIssueList);
            }
            /** 2释放word处理进程*/
            ConvertRTFToHtml.releaseWord();
            /** 2释放word处理进程*/
            /** 处理追溯3*/
            for (int i = 0; i < contentsList.size(); i++) {
            	JSONObject contentObj = contentsList.get(i);
            	dealRelationship(contentObj, SWSIDMap, SWMap, mks);
            	contentObj = null;//处理完成，将此数据设置null
            }
            /** 如果分支创建完成，需要删除结构的，进行结构删除。 */
            if(branchDeleIssueList != null ){
            	for(String issueId : branchDeleIssueList){
            		try {
						mks.removecontent(issueId);
						mks.deleteissue(issueId);
					} catch (APIException e) {
						log.info("删除分支 复用条目 失败-" + issueId + " | 失败原因：" + APIExceptionUtil.getMsg(e));
					}
            	}
            }
            /** 如果分支创建完成，需要删除结构的，进行结构删除。 */
            SWDealMap = null;//置空，回收对象
            SWJSONMap = null;//置空，回收对象
            SWSIDMap = null;//置空，回收对象
            contentsList = null;//置空，回收对象
            //新增后修改状态为评审 in approve
            docUUID = jsonData.getString(Constants.DOC_UUID);
            log.info("数据下发完成 开始修改状态 Doc_id : "+docId);
            try {
                log.info("评审人---" + assignedUser);
                String doctype = IsNull(jsonData.get("issue_Type"));
                log.info("判断文档是否是coment==" + doctype);
                if ("Component Requirement Specification Documnet".equals(doctype)) {
                    String[] arr = mks.getStaticGroup("VCU");
                    if (Arrays.asList(arr).contains(assignedUser)) {
                        log.info("评审人在VCU组");
                        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
                        dataMap.put("State", "In Review");
                        dataMap.put("Assigned User", assignedUser);
                        mks.editIssue(docId, dataMap, new HashMap<String, String>());
                        log.info("into: " + "修改状态In Review");
                    }
                } else {
                    Map<String, String> dataMap3 = new HashMap<String, String>();//普通字段
                    dataMap3.put("State", "in approve");
                    dataMap3.put("Assigned User", assignedUser);
                    mks.editIssue(docId, dataMap3, new HashMap<String, String>());
                    log.info("into: " + "修改状态in approve");
                }
            } catch (APIException e) {
                log.info("修改状态出错 ： "+e.getMessage());
                log.info("清理缓存！" + docUUID);
                cache.remove(docUUID);
                e.printStackTrace();
            }
//                log.info("断开链接：");
//                mks.close(host, port, loginName);\
            String label = "Autobaseline:from SWR :" + doc_SW_ID;
            try {
                log.info("基线标题==="+label);
                log.info("基线文档id==="+docId);
				mks.createBaseLine(label, docId);//自动创建基线信息
				log.info("同步完毕，创建自动基线！");
			} catch (APIException e) {
				// TODO Auto-generated catch block
                log.info("创建基线错误！"+e.getMessage());
				e.printStackTrace();
			}
            log.info("执行完成，清理缓存！");
            cache.remove(docUUID);
        } else {
        	Element docEle = cache.get(docUUID);
        	List<JSONObject> docContentList = null;
        	if(docEle == null){
        		docContentList = new ArrayList<>();
        	}else
        		docContentList = (List<JSONObject>)docEle.getObjectValue();
        	docContentList.add(jsonData);
            cache.put(new Element(docUUID, docContentList));
            log.info(cache.get(docUUID).getObjectValue());
            log.info(ResultJson("data", ""));
            return ResultJson("data", "");
        }
        log.info("返回信息：");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", docId));
        log.info(jsonObject);
        return jsonObject;
    }
    
	/**
     * 将 DOC JSON查询出来，并将条目 JSON排序
     * @param contains
     * @return
     */
    private JSONObject sortContainsAndGetDoc(List<JSONObject> contains, List<JSONObject> result){
    	JSONObject docJson = null; 
    	//1 将所有的 条目 JSON获取出来
    	for(JSONObject obj : contains){
    		String category = obj.getString(Constants.CATEGORY);
    		if(Constants.DOC_CATEGORY.equalsIgnoreCase(category)){//获取当前文档JSON
    			docJson = obj;
    		}else//添加条目JSON
    			result.add(obj);
    	}
    	//2 排序条目，按 parent_id, before_id排序
    	Collections.sort(result, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject obj1, JSONObject obj2) {
				String sw_sid1 = obj1.getString(Constants.SW_SID_FIELD);
				int sid1Len = sw_sid1.split(Constants.SW_SID_SPLIT).length;
				String sw_sid2 = obj2.getString(Constants.SW_SID_FIELD);
				int sid2Len = sw_sid2.split(Constants.SW_SID_SPLIT).length;
				String parent1 = obj1.getString(Constants.PARENT_FIELD);
				String parent2 = obj2.getString(Constants.PARENT_FIELD);
				String before1 = obj1.getString(Constants.BEFORE_FIELD);
				String before2 = obj2.getString(Constants.BEFORE_FIELD);
				// SW_SID长度一致时，看是否是同一个 Parent下的，如果是，比较Before_id
				if(sid1Len == sid2Len){// 长度相同，说明在同一级
					if(parent1.equals(parent2)){//同一个父级下，对比是否有一个是另一个的在另一个之前。不在同一个父级下，不对比
						if("".equals(before1)){// 为空在最前
							return -1;
						}else if("".equals(before2)){// 为空在最前
							return 1;
						}else if(sw_sid2.equals(before1)){// 1 before 2
							return 1;
						}else if(sw_sid1.equals(before2)){// 2 before 1
							return -1;
						} 
					}
				}else{// SW_SID长度不一致时，短的在前，判断一个是否是另一个的Parent
					return sid1Len - sid2Len;
				}
				return 0;
			}
    	});
    	
    	return docJson;
    }

    //根据条目id获取文档id
    public String getDocID(String e_id, String docId, MKSCommand mks){
        if (docId == "") {
            log.info("文档条目id--" + e_id);
            try {
                String doc_id = mks.getTypeById(e_id, "Document ID");
                docId = doc_id;
                log.info("返回的根据条目查询的文档id:" + docId);
            } catch (APIException e) {
                e.printStackTrace();
            }
        }
        return docId;
    }

    //变更反馈增删改条目
    @RequestMapping(value = "/changeAction", method = RequestMethod.POST)
    public JSONObject changeAction1(@RequestBody JSONObject jsonData) {
        getToken(IsNull(jsonData.get("Access_Token")));
        log.info(jsonData);
        MKSCommand mks = new MKSCommand();
//        mks.initMksCommand(host, port, loginName, passWord);
        String category = jsonData.getString("Category");
        log.info("变更反馈 - " + category);
        String docId = null;
        String resultStr = null;
        if("Document".equalsIgnoreCase(category)){//文档处理
        	/** 变更更新文档数据 */
        	String doc_SW_SID = jsonData.getString("Old_SW_SID");
    		List<Map<String,String>> docList = null;
    		String issue_Type = jsonData.getString("issue_Type");
    		String project = jsonData.getString("Project");
    		try {
    			docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
			} catch (APIException e) {
				log.info("查询数据：" + APIExceptionUtil.getMsg(e));
			}
    		if(docList != null && !docList.isEmpty()){//更新判断文档是否存在
        		Map<String,String> docInfo = docList.get(0);
        		docId = docInfo.get("ID");
                updateDoc( docInfo,jsonData,mks);
        	}else{
        		throw new MsgArgumentException("204","Can not find Document ,Document Structure ID : " +doc_SW_SID+ "!");
        	}
        }else{
        	/** 变更更新条目数据 */
        	Map<String,String> SWSIDMap = new HashMap<>();
        	String action_Type = IsNull(jsonData.get("action_Type"));//创建、更新、删除或移动
            //创建文档需要的参数
            if (action_Type.equals("add")) {
//                docID = mks.getDocIdsByType("SW_SID","entry_"+Parent_ID,"ID");
                resultStr = AddEntry(jsonData, SWSIDMap, null, docId, mks);
            } else if (action_Type.equals("update")) {
                resultStr = UpDoc(jsonData,SWSIDMap, null, docId, mks);
            } else if (action_Type.equals("delete")) {
                resultStr = DelDoc(jsonData, docId, mks);
            } else if (action_Type.equals("move")) {
                resultStr = MoveDoc(jsonData,SWSIDMap, null, docId, mks);
            }
            dealRelationship(jsonData, SWSIDMap, null, mks);
            try {
                docId = mks.getTypeById(resultStr, "Document ID");
                log.info("返回的根据条目查询的文档id:" + docId);
            } catch (APIException e) {
                e.printStackTrace();
            }
        }
        
        
        /** 当数据处理完毕后，修改变更单*/
        String end = jsonData.getString("end");//结尾标记，标识本次文档数据传输完毕
        if("true".equals(end)){
        	try {
				updateChangeInfo(jsonData.getString("ALM_CO_ID"),mks);
			} catch (APIException e) {
				log.info("变更单处理失败，失败原因：" + APIExceptionUtil.getMsg(e));
			}//更新变更信息
        }
//        mks.close(host,port,loginName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", docId));
        log.info(jsonObject);
        return jsonObject;
    }

   
    public String getIdBySWID(String[] sw_ids, MKSCommand mks) {
        log.info("开始根据SW_ID查询Trace_id");
        String str = "";
        for (int i = 0; i < sw_ids.length; i++) {
            str += sw_ids[i] + ",";
//            List<String> almIds = mks.getDocIdsByType1("SW_ID","entry_"+sw_ids[i],"ID");
//            for(int j=0;j<almIds.size();j++){
//                str += "entry_"+almIds.get(j) + ",";
//            }
        }
        str = str.substring(0, str.length() - 1);
        log.info("查询的teaces ids：" + str);
        return str;
    }

    
    

	// 输入流转换html string
	

    

    /**
     *token验证
     */
    public void getToken(String str){
        if(token.equals(str)){
            log.info("token验证成功!");
            return;
        }else {
            log.error("token验证失败!");
            throw new MsgArgumentException("201","token Validation failed!");
        }
    }

    public static void main(String[] str) {
    }
}
