package com.sw.SWAPI.util;

import static com.sw.SWAPI.util.Obj.IsNull;
import static com.sw.SWAPI.util.Obj.verification;
import static com.sw.SWAPI.util.ResultJson.ResultStr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.controller.AlmController;


public class IntegrityUtil {

	public static final Log log = LogFactory.getLog(IntegrityUtil.class);

	private MKSCommand mks;

	/** 文件临时路径 */
	private String filePath = "C:\\\\Program Files\\\\Integrity\\\\ILMServer12\\\\data\\\\tmp";

	/**
	 * 处理文档
	 * @return
	 */
	public String dealDoc(JSONObject docJSON){
		String action_Type = docJSON.getString("action_Type");
		String docId = null;
    	if("add".equals(action_Type)){//
    		String doc_SW_SID = docJSON.getString("SW_SID");
    		String doc_SW_ID = docJSON.getString("SW_ID");
            String issue_Type = docJSON.getString("issue_Type");
    		List<Map<String,String>> docList = null;
    		try {
    			docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, null);
			} catch (APIException e) {
				log.info("查询数据：" + APIExceptionUtil.getMsg(e));
			}
    		log.info("判断文档是否存在"+docList.size());
            log.info("判断文档是否存在"+docList);
    		if(docList == null || docList.isEmpty() || docList.size()==0){
    			docId = createDoc(docJSON, mks);//当前未创建，创建新的文档
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
		return docId;
	}
	
	/**
	 * 处理条目数据
	 * 
	 * @param contentObj
	 * @param SWIDMap
	 *            // 存放 SWSID - ALMID
	 * @param SWMap
	 * @param SWDealMap
	 * @param SWJSONMap
	 * @param docId
	 * @param mks
	 * @param branch
	 * @param newDoc
	 * @param branchDeleIssueList
	 */
	private void dealContentJson(JSONObject contentObj, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			Map<String, Boolean> SWDealMap, Map<String, JSONObject> SWJSONMap, String docId, MKSCommand mks,
			boolean branch, boolean newDoc, List<String> branchDeleIssueList) {
		String SWSID = IsNull(contentObj.get(Constants.SW_SID_FIELD));// 当前JSON的ID
		log.info("SW_SID 处理中- " + SWSID);
		if (SWDealMap.get(SWSID)) {
			log.info("SWSID - " + SWSID + " == 数据已经处理过，跳过本次循环");
			return;
		}
		String beforeId = IsNull(contentObj.get("Before_ID"));
		log.info("beforeId - " + beforeId + " ");
		if (null != beforeId && !"".equals(beforeId)) {// BeforeID存在时，防止Before未处理。获取Before判断并处理
			log.info("beforeId - 处理 " + SWDealMap.get(beforeId) + " ");
			if (SWDealMap.get(beforeId) != null && !SWDealMap.get(beforeId)) {
				log.info("beforeId - " + beforeId + " == 数据未处理，优先执行Before ID处理");
				dealContentJson(SWJSONMap.get(beforeId), SWIDMap, SWMap, SWDealMap, SWJSONMap, docId, mks, branch,
						newDoc, branchDeleIssueList);
				SWJSONMap.remove(beforeId);// 处理完成，将SWJSONMap置空。
				SWDealMap.put(beforeId, true);// 更新SWSID对应处理结果
			}
		}
		/** 分支创建，调用 */
		if (branch) {
			String Old_SWSID = IsNull(contentObj.get(Constants.SW_SID_FIELD));
			if (SWIDMap.get(SWSID) != null) {// 如果在原结构有此数据，则移除。不做处理
				branchDeleIssueList.remove(SWIDMap.get(SWSID));
				// 同时直接将原数据进行 Move & Update
				MoveDoc(contentObj, SWIDMap, SWMap, docId, mks);
			} else if (SWIDMap.get(Old_SWSID) != null) {// 如果在原结构有此数据，则移除。不做处理
				branchDeleIssueList.remove(SWIDMap.get(Old_SWSID));
				// 同时直接将原数据进行 Move & Update
				MoveDoc(contentObj, SWIDMap, SWMap, docId, mks);
			} else {
				// 在原结构中找不到响相应数据，新增。
				AddEntry(contentObj, SWIDMap, SWMap, docId, mks);
			}
		} else {
			String action_Type = IsNull(contentObj.get("action_Type"));
			if ("add".equals(action_Type)) {// 创建分支，可能会选择历史数据进行分支。此时直接复用的分支进行更新
				AddEntry(contentObj, SWIDMap, SWMap, docId, mks);
			} else if ("update".equals(action_Type) && !newDoc) {// 新建文档，不执行更新操作
				UpDoc(contentObj, SWIDMap, SWMap, docId, mks);
			} else if ("delete".equals(action_Type) && !newDoc) {// 新建文档，不执行删除操作
				DelDoc(contentObj, docId, mks);
			} else if (("move".equals(action_Type) || "update/move".equals(action_Type)) && !newDoc) {// 新建文档，不执行移动操作
				MoveDoc(contentObj, SWIDMap, SWMap, docId, mks);
			}
		}

		SWJSONMap.remove(SWSID);// 处理完成，将SWJSONMap置空。
		SWDealMap.put(SWSID, true);// 更新SWSID对应处理结果
	}

	// 条目移动
	public String MoveDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			String docId, MKSCommand mks) {
		log.info("-------------移动文档-----------");
		String oldSWSID = jsonData.getString("Old_SW_SID");// 旧ID
		if (oldSWSID != null && !"".equals(oldSWSID)) {// OLD SW_SID不为空时，才需要移动
			String project = jsonData.getString("Project");// 文档id
			String docType = jsonData.getString("issue_Type");
			String issueType = docType.substring(0, docType.lastIndexOf(" "));
			String id = SWIDMap.get(oldSWSID);// 复用时，可以通过SW_SID - ALM ID
												// Map获取到数据
			if (id == null) {// 获取不到，使用查询
				id = mks.getIssueBySWID("SW_SID", IsNull(jsonData.get("Old_SW_SID")), project, issueType, "ID");
			}
			log.info("需要移动的sw_sid -" + IsNull(jsonData.get("Old_SW_SID")) + ";id:" + id);
			// 获取文档id
			String Parent_ID = IsNull(jsonData.get("Parent_ID"));
			String alm_parent_ID = "";
			log.info("Parent_ID>>>SW_SID-------" + Parent_ID);
			if ("".equals(Parent_ID) || "null".equals(Parent_ID)) {
				log.info("父级为文档id-------" + docId);
				alm_parent_ID = docId;
			} else {
				alm_parent_ID = SWIDMap.get(Parent_ID);
				log.info("map中查询已经创建的父id-------" + alm_parent_ID);
				log.info("同批次数据-------" + alm_parent_ID);
				if (alm_parent_ID == null || "".equals(alm_parent_ID) || "null".equals(alm_parent_ID)) {
					alm_parent_ID = mks.getIssueBySWID("SW_SID", Parent_ID, project, issueType, "ID");
					SWIDMap.put(Parent_ID, alm_parent_ID);
					log.info("Parent_ID-------" + Parent_ID);
					log.info("alm_parent_ID-------" + alm_parent_ID);
				}
			}
			String insertLocation = "";
			String Before_ID = IsNull(jsonData.get("Before_ID"));// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
			log.info("需要移动到前面的id -" + IsNull(jsonData.get("Before_ID")) + ";id:" + Before_ID);
			if (!"".equals(Before_ID)) {
				String alm_bef_ID = null;
				if (SWIDMap != null) {// SWID_ID MAP保存的有SW_SID <>
										// ALM_ID对应关系，直接从Map获取
					alm_bef_ID = SWIDMap.get(Before_ID);
				}
				if (alm_bef_ID == null) {// 当从Map中获取不到时，查询
					alm_bef_ID = mks.getIssueBySWID("SW_SID", Before_ID, project, issueType, "ID");
					SWIDMap.put(Before_ID, alm_bef_ID);
				}
				log.info("插入位置：id -" + Before_ID + ";ALM - id:" + alm_bef_ID);
				insertLocation = "after:" + alm_bef_ID;
			} else {
				insertLocation = "first";
			}
			try {
				log.info("需要移动的aml_id(" + id + "),移动的aml_parentID(" + alm_parent_ID + ")：移动的具体位置id(" + insertLocation
						+ ")");
				mks.movecontent(alm_parent_ID, insertLocation, id);
				log.info("将id： (" + id + ")移动到 -" + alm_parent_ID);
			} catch (APIException e) {
				log.info("error: " + "移动条目出错！" + APIExceptionUtil.getMsg(e));
				log.info("清理缓存！");
				String docUUID = jsonData.getString("DOC_UUID");
				if (docUUID != null && !"".equals(docUUID)) {
					cache.remove(docUUID);
				}
				throw new MsgArgumentException("201", "移动条目出错 " + APIExceptionUtil.getMsg(e));
			}
			log.info("移动后更新SW_SID");
		}
		return UpDoc(jsonData, SWIDMap, SWMap, docId, mks);
	}

	/**
	 * 更新文档SW_ID、SW_SID
	 * 
	 * @param jsonData
	 * @param mks
	 */
	public void updateDoc(String docId, JSONObject jsonData) {
		log.info("-------------修改文档-----------");
		String SW_SID = IsNull(jsonData.get("SW_SID"));
		String issue_Type = IsNull(jsonData.get("issue_Type"));// 创建文档类型或创建条目类型(创建时必须)
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		Map<String, String> docdataMap = new HashMap<String, String>();// 普通字段
		Map<String, String> docmap = new AnalysisXML().resultFile(issue_Type);
		for (String key : docmap.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key) || key == null || "".equals(key))
				continue;// 跳过不更新
			String strKey = IsNull(jsonData.get(key));
			if (!strKey.equals("")) {
				if (key.equals("SW_ID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else if (key.equals("SW_SID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else {
					docdataMap.put(docmap.get(key), strKey);
				}
			}
		}
		// rtf

		try {
			// docdataMap.put("Assigned User", "admin");
			log.info("issue_id ====== :" + IsNull(jsonData.get("issue_id")));
			mks.editIssue(docId, docdataMap, null);
			// swid_id.put("doc_" + SW_SID, doc_Id);
			log.info("更新文档成功：" + docId + " SW_SID=" + SW_SID);
		} catch (APIException e) {
			log.info("error: " + "更新文档出错！" + APIExceptionUtil.getMsg(e));
			log.info("清理缓存！");
			
			throw new MsgArgumentException("201", "更新文档出错 " + APIExceptionUtil.getMsg(e));
		}
	}

	/**
	 * 添加条目
	 * 
	 * @param jsonData
	 * @param SWIDMap
	 * @param SWMap
	 * @param docId
	 * @param mks
	 * @return
	 */
	public String AddEntry(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			String docId, MKSCommand mks) {
		log.info("-------------新增条目-----------");
		verification(jsonData);
		String docType = IsNull(jsonData.get("issue_Type"));// 创建文档类型或创建条目类型(创建时必须)
		String SW_SID = IsNull(jsonData.get("SW_SID"));
		String SW_ID = IsNull(jsonData.get("SW_ID"));
		String project = IsNull(jsonData.get("Project"));
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("创建SW_SID : " + SW_SID);
		String issueType = docType.substring(0, docType.lastIndexOf(" "));
		/** 设置位置信息 */
		String Parent_ID = IsNull(jsonData.get("Parent_ID"));
		String alm_parent_ID = "";
		log.info("Parent_ID>>>SW_SID-------" + Parent_ID);
		if ("".equals(Parent_ID) || "null".equals(Parent_ID)) {
			log.info("父级为文档id-------" + docId);
			alm_parent_ID = docId;
		} else {
			alm_parent_ID = SWIDMap.get(Parent_ID);
			log.info("map中查询已经创建的父id-------" + alm_parent_ID);
			log.info("同批次数据-------" + alm_parent_ID);
			if (alm_parent_ID == null || "".equals(alm_parent_ID) || "null".equals(alm_parent_ID)) {
				alm_parent_ID = mks.getIssueBySWID("SW_SID", Parent_ID, project, issueType, "ID");
				SWIDMap.put(Parent_ID, alm_parent_ID);
				log.info("Parent_ID-------" + Parent_ID);
				log.info("alm_parent_ID-------" + alm_parent_ID);
			}
		}
		String insertLocation = "";
		String Before_ID = IsNull(jsonData.get("Before_ID"));// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
		log.info("插入位置：Before_ID -" + Before_ID);
		if (!"".equals(Before_ID)) {
			if (SWIDMap != null) {// SWID_ID MAP保存的有SW_SID <>
									// ALM_ID对应关系，直接从Map获取
				String alm_bef_ID = null;
				if (SWIDMap != null) {// SWID_ID MAP保存的有SW_SID <>
										// ALM_ID对应关系，直接从Map获取
					alm_bef_ID = SWIDMap.get(Before_ID);
				}
				if (alm_bef_ID == null) {// 当从Map获取不到时，查询
					alm_bef_ID = mks.getIssueBySWID("SW_SID", Before_ID, project, issueType, "ID");
					SWIDMap.put(Before_ID, alm_bef_ID);
				}
				log.info("插入位置：id -" + Before_ID + ";ALM - id:" + alm_bef_ID);
				insertLocation = "after:" + alm_bef_ID;
			}
		} else {
			insertLocation = "first";
		}
		/** 设置位置信息 */
		// 创建文档条目
		// xml配置字段
		// 先判断是否创建过
		String issueId = null;
		Map<String, String> issueMap = mks.searchOrigIssue(Arrays.asList("ID", "Document ID", "SW_SID", "Project"),
				SW_SID, issueType, project);
		if (issueMap != null) {
			/** 不为空时，判断是否处于当前文档下：1. 如果处于当前文档 ,则直接更新 ；2. 如果不处于当前文档，则copy到当前文档 */
			// 1 判断是否处于当前文档
			String issueDocId = issueMap.get("Document ID");
			issueId = issueMap.get("ID");
			log.info("查找到相关数据：");
			if (issueDocId.equals(docId)) {
				UpDoc(jsonData, SWIDMap, SWMap, docId, mks);
			} else {// 2 未处于当前文档下
				try {
					issueId = mks.copyContent(alm_parent_ID, insertLocation, issueMap.get("ID"));
				} catch (APIException e) {
					log.info(SW_SID + "复用失败，所属文档 " + issueDocId + "失败原因：" + APIExceptionUtil.getMsg(e));
					e.printStackTrace();
				}
			}
			if (SWIDMap != null)
				SWIDMap.put(SW_SID, issueId);
			log.info("已经存在的条目id： " + SW_SID + "---alm_ID:" + issueId);
		} else {
			Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
			Map<String, String> richDataMap = new HashMap<String, String>();// 富文本字段
			String entryType1 = new AnalysisXML().resultType(issueType);
			Map<String, String> map = new AnalysisXML().resultFile(issueType);
			for (String key : map.keySet()) {
				String strKey = IsNull(jsonData.get(key));
				if (!strKey.equals("")) {
					if (key.equals("SW_ID")) {
						dataMap.put(map.get(key), strKey);
					} else if (key.equals("SW_SID")) {
						dataMap.put(map.get(key), strKey);
					} else {
						dataMap.put(map.get(key), strKey);
					}
				}
			}
			// String Category = "Requirement";
			String header = new AnalysisXML().resultCategory(issueType, IsNull(jsonData.get("Category")));
			if ("".equals(header)) {
				header = "Heading";
			}
			dataMap.put("Category", header);
			// rtf
			Object Text1 = jsonData.get("issue_text");
			if (Text1 != null && !"".equals(Text1.toString())) {
				String htmlStr = new AlmController().rtfString(Text1.toString());
				dataMap.put("Text", htmlStr);
			}
			try {
				issueId = mks.createContent(alm_parent_ID, insertLocation, dataMap, entryType1);
				log.info("创建的条目id： " + issueId);
				if (SWIDMap != null)
					SWIDMap.put(SW_SID, issueId);
				// 获取文档id
				// 附件
				Object Attachments1 = jsonData.get("Attachments");
				if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
					String[] Attachments2 = (String[]) Attachments1;
					for (int i = 0; i < Attachments2.length; i++) {
						JSONObject j = JSONObject.parseObject(Attachments2[i]);
						new AlmController().uploadAttachments(j, issueId, mks);
					}
				}
			} catch (APIException e) {
				log.info("error: " + "创建文档条目出错！" + APIExceptionUtil.getMsg(e));
				log.info("清理缓存！");
				if (docUUID != null && !"".equals(docUUID)) {
					cache.remove(docUUID);
				}
				throw new MsgArgumentException("201", "创建文档条目出错 " + APIExceptionUtil.getMsg(e));
			}
		}
		if (SWMap != null) {
			List<String> almList = SWMap.get(SW_ID);
			if (almList == null) {
				almList = new ArrayList<>();
				SWMap.put(SW_ID, almList);
			}
			almList.add(issueId);
		}
		return issueId;
	}

	// 条目删除
	public String DelDoc(JSONObject jsonData, String docId, MKSCommand mks) {
		log.info("-------------删除文档-----------");
		String SW_SID = IsNull(jsonData.get("SW_SID"));// 文档id
		String project = IsNull(jsonData.get("Project"));// 文档id
		String id = mks.getIssueBySWID("SW_SID", SW_SID, project, null, "ID");
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("需要删除的sid: " + SW_SID + ",id : " + id);
		try {
			mks.removecontent(id);
		} catch (APIException e) {
			log.info("error: " + "删除条目关系出错！" + e.getMessage());
			log.info("清理缓存！");
			if (docUUID != null && !"".equals(docUUID)) {
				cache.remove(docUUID);
			}
			throw new MsgArgumentException("201", "删除条目关系出错 " + e.getMessage());
		}
		try {
			mks.deleteissue(id);
			log.info("删除的条目id： " + id);
		} catch (APIException e) {
			log.info("error: " + "删除条目出错！" + e.getMessage());
			log.info("清理缓存！");
			if (docUUID != null && !"".equals(docUUID)) {
				cache.remove(docUUID);
			}
			throw new MsgArgumentException("201", "删除条目出错 " + e.getMessage());
		}
		return id;
	}

	/**
	 * 处理富文本信息
	 * 
	 * @param text
	 * @return
	 */
	public String rtfString(String text) {
		String str = filePath + "\\" + new Date().getTime() + ".rtf";
		String str1 = filePath + "\\" + new Date().getTime();

		// 如果文件夹不存在则创建
		if (!new File(filePath).exists() && !new File(filePath).isDirectory()) {
			new File(filePath).mkdir();
		}
		// 没有文件就创建
		if (!new File(str).exists()) {
			try {
				new File(str).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new AlmController().conserveFile(str, text);
		ConvertRTFToHtml.RTFToHtml(str, str1);// 本地rtf文件转换为html
		String htmldata = null;// 获取html中元素
		try {
			htmldata = ConvertRTFToHtml.readHtml(str1 + ".htm");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return htmldata;
	}

	// 文档新增
	public String createDoc(JSONObject jsonData) {
		log.info("-------------新增文档-----------");
		verification(jsonData);
		String issue_Type = IsNull(jsonData.get("issue_Type"));// 创建文档类型或创建条目类型(创建时必须)
		String SW_SID = IsNull(jsonData.get("SW_SID"));
		String project = IsNull(jsonData.get("Project"));
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("创建SW_SID : " + SW_SID);
		// 先判断是否创建过
		String docType = new AnalysisXML().resultType(issue_Type);
		Map<String, String> docdataMap = new HashMap<String, String>();// 普通字段
		Map<String, String> docmap = new AnalysisXML().resultFile(issue_Type);
		for (String key : docmap.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key) || key == null || "".equals(key))
				continue;// 跳过不更新
			String strKey = IsNull(jsonData.get(key));
			if (!strKey.equals("")) {
				if (key.equals("SW_ID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else if (key.equals("SW_SID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else {
					docdataMap.put(docmap.get(key), strKey);
				}
			}
		}
		try {
			// docdataMap.put("Assigned User", "admin");
			log.info("issue_id ====== :" + IsNull(jsonData.get("issue_id")));
			String doc_Id = mks.createDocument(docType, docdataMap, null);
			// swid_id.put("doc_" + SW_SID, doc_Id);
			log.info("创建的文档id： " + doc_Id);
			return doc_Id;
		} catch (APIException e) {
			log.info("error: " + "创建文档出错！" + APIExceptionUtil.getMsg(e));
			log.info("清理缓存！");
			throw new MsgArgumentException("201", "创建文档出错 " + APIExceptionUtil.getMsg(e));
		}
	}

	// 文档条目修改
	public String UpDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap, String docId,
			MKSCommand mks) {
		log.info("-------------修改条目-----------");
		String doc_Type = IsNull(jsonData.get("issue_Type"));
		String issue_Type = doc_Type.substring(0, doc_Type.lastIndexOf(" "));
		Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
		Map<String, String> richDataMap = new HashMap<String, String>();// 富文本字段
		String Old_SW_SID = jsonData.getString("Old_SW_SID");// 原SW_SID
		if (Old_SW_SID == null || "".equals(Old_SW_SID)) {// 只有Branch时，有可能不传OLD_SW_SID
			Old_SW_SID = jsonData.getString("SW_SID");// 使用新的SW_SID查询数据
		}
		String SW_ID = jsonData.getString("SW_ID");// SW_ID
		String project = jsonData.getString("Project");// 文档id
		log.info("修改的sw_sid----------" + Old_SW_SID);
		// 当通过分支创建时，会直接将SW_SID-ALMID存放入MAP，可以直接获取进行更新
		String id = SWIDMap.get(Old_SW_SID);
		if (id == null)
			id = mks.getIssueBySWID("SW_SID", Old_SW_SID, project, issue_Type, "ID");
		// rtf
		if (id == null || "".equals(id)) {
			log.info("通过SW_SID查询不到对应的ALM数据: " + Old_SW_SID);
		}
		Object Text1 = jsonData.get("issue_text");
		if (Text1 != null && !"".equals(Text1.toString())) {
			String htmlStr = new AlmController().rtfString(Text1.toString());
			richDataMap.put("Text", htmlStr);
		}

		Map<String, String> map = new AnalysisXML().resultFile(issue_Type);
		for (String key : map.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key))
				continue;// 跳过不更新
			String strKey = IsNull(jsonData.get(key));
			if (!strKey.equals("")) {
				if (key.equals("SW_ID")) {
					dataMap.put(map.get(key), strKey);
				} else if (key.equals("SW_SID")) {
					dataMap.put(map.get(key), strKey);
				} else {
					dataMap.put(map.get(key), strKey);
				}
			}
		}
		try {
			log.info("需要修改的aml_id----------" + id);
			mks.editIssue(id, dataMap, richDataMap);
			log.info("修改的条目： " + id);
			// 获取文档id
			// getDocID(id, docId, mks); //此查询作用
			// 附件
			String SW_SID = jsonData.getString("SW_SID");// 新SW_SID
			if (SWIDMap != null)
				SWIDMap.put(SW_SID, id);
			Object Attachments1 = jsonData.get("Attachments");
			if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
				String[] Attachments2 = (String[]) Attachments1;
				for (int i = 0; i < Attachments2.length; i++) {
					JSONObject j = JSONObject.parseObject(Attachments2[i]);
					new AlmController().uploadAttachments(j, id, mks);
				}
			}
		} catch (APIException e) {
			log.info("error: " + "修改条目出错！" + APIExceptionUtil.getMsg(e));
			log.info("清理缓存！");
			String docUUID = jsonData.getString("DOC_UUID");
			if (docUUID != null && !"".equals(docUUID)) {
				cache.remove(docUUID);
			}
			throw new MsgArgumentException("201", "修改条目出错 " + APIExceptionUtil.getMsg(e));
		}
		if (SWMap != null) {// 记录ALMID
			List<String> almList = SWMap.get(SW_ID);
			if (almList == null) {
				almList = new ArrayList<>();
				SWMap.put(SW_ID, almList);
			}
			almList.add(id);
		}
		return id;
	}
	
	 /**
     * 添加追溯关系
     * @param jsonData
     * @param SWSIDMap
     * @param SWMap
     * @param mks
     */
    public void dealRelationship(JSONObject jsonData, Map<String,String> SWSIDMap, Map<String,List<String>> SWMap, MKSCommand mks){
         String Delete_Trace_ID = jsonData.getString("Delete_Trace_ID");
         // SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
         String addTrace_ID = jsonData.getString("Trace_ID");//
         log.info("Delete_Trace_ID 删除 ：" + Delete_Trace_ID);         
         log.info("Trace_ID 添加：" + addTrace_ID);         
         if( (addTrace_ID == null || "".equals(addTrace_ID) ) && (Delete_Trace_ID == null || "".equals(Delete_Trace_ID) ) ){
        	 return;//如果追溯关系删除和添加都没有，跳过本条处理
         }
         String SW_SID = IsNull(jsonData.get("SW_SID"));//SW_SID
         String issueId = SWSIDMap.get(SW_SID);//条目ID
         log.info("Issue ID ：" + issueId + " || SW_SID" + SW_SID);
         if(issueId == null || "".equals(issueId)){
        	 log.info("Issue ID获取不到：" + SW_SID);
        	 return;//获取不到issueID
         }
         String doc_Type = IsNull(jsonData.get("issue_Type"));
         String issue_Type = doc_Type.substring(0,doc_Type.lastIndexOf(" "));
         String project = IsNull(jsonData.get("Project"));//所属项目
    	  /** Modify By Cai Hao, 添加关联关系*/
         log.info("需要添加的 - - - Teace_Id" + addTrace_ID);
         log.info("需要删除的 - - - Delete_Trace_ID" + Delete_Trace_ID);
         editIssueRelationship(issueId, issue_Type, Delete_Trace_ID, addTrace_ID, SWMap, project, mks);
        /** Modify By Cai Hao, 添加关联关系*/
    }
    /**
     * 添加/删除关联关系
     *
     * @param curIssueId
     * @param deleteIssueStrs
     * @param addIssueStrs
     * @return
     */
    public boolean editIssueRelationship(String curIssueId, String curType, String deleteIssueStrs, String addIssueStrs, 
                                         Map<String,List<String>> SWMap, String project, MKSCommand mks) {

        Map<String, String> deleRelationMap = null;
        if (!Obj.isEmptyOrNull(deleteIssueStrs)) {/* 拼接删除关系*/
        	List<Map<String, String>> deleteIssueList = null;
            log.info("开始删除关联关系" + deleteIssueStrs);
            deleRelationMap = new HashMap<String, String>();
            try {
            	deleteIssueList = mks.searchALMIDTypeBySWID(Arrays.asList(deleteIssueStrs.split(",")), null);
			} catch (APIException e) {
				log.info("查找ISSUE失败，失败原因：" + APIExceptionUtil.getMsg(e));
			}
            log.info("alm中deleteIssueList===========" + deleteIssueList);
            for (Map<String, String> map : deleteIssueList) {
                String targetType = map.get(Constants.TYPE_FIELD);
                String targetID = map.get(Constants.ID_FIELD);
                String relationField = AnalysisXML.getRelationshipField(curType, targetType);
                log.info("类型：" + curType + ",关系字段：" + relationField);
                String editVal = deleRelationMap.get(relationField);
                if (Obj.isEmptyOrNull(editVal)) {
                    editVal = targetID;
                } else {
                    editVal = editVal + "," + targetID;
                }
                deleRelationMap.put(relationField, editVal);
            }
        }
        Map<String, String> addRelationMap = null;
        if (!Obj.isEmptyOrNull(addIssueStrs)) {/* 拼接添加关系*/
        	List<Map<String, String>> addIssueList = null;
            log.info("开始添加关联关系" + addIssueStrs);
            addRelationMap = new HashMap<String, String>();
            try {
            	addIssueList = mks.searchALMIDTypeBySWID(Arrays.asList(addIssueStrs.split(",")), null);
			} catch (APIException e) {
				log.info("查找ISSUE失败，失败原因：" + APIExceptionUtil.getMsg(e));
			}
            
            log.info("alm中addIssueList===========" + addIssueList);
            for (Map<String, String> map : addIssueList) {
                String targetType = map.get(Constants.TYPE_FIELD);
                String targetID = map.get(Constants.ID_FIELD);
                log.info("关系类型1：" + curType + ",关系类型2：" + targetType);
                String relationField = AnalysisXML.getRelationshipField(curType, targetType);
                log.info("类型：" + curType + ",关系字段：" + relationField);
                String editVal = addRelationMap.get(relationField);
                if (Obj.isEmptyOrNull(editVal)) {
                    editVal = targetID;
                } else {
                    editVal = editVal + "," + targetID;
                }
                addRelationMap.put(relationField, editVal);
            }
			if (SWMap != null) {// 通过MAP保存的ID，只会是同一个类型数据
				for (String swID : addIssueStrs.split(",")) {
					List<String> almIdList = SWMap.get(swID);
					if(almIdList == null || almIdList.isEmpty()){
						continue;
					}
					String relationField = AnalysisXML.getRelationshipField(curType, curType);
					log.info("在创建过程中添加追溯。类型：" + curType + ",关系字段：" + relationField);
					String editVal = addRelationMap.get(relationField);
					for(String almId : almIdList){
						if (Obj.isEmptyOrNull(editVal)) {
							editVal = almId;
						} else {
							editVal = editVal + "," + almId;
						}
					}
					log.info("Trace ALM ID " + editVal);
					addRelationMap.put(relationField, editVal);
				}
			}
        }
        try {
        	mks.editRelationship(curIssueId, deleRelationMap, addRelationMap);
            return true;
        } catch (APIException e) {
            log.info("添加/删除关系失败，失败原因：" + APIExceptionUtil.getMsg(e));
            return false;
        }
    }
    
    /**
     * byte 转文件 下载到本地
     *
     * @param
     */
    public String conserveFile(String filePath1, String bytes) {
//        byte[] bytes= DatatypeConverter.parseBase64Binary(base64String);
        InputStream inputStream = null;
        InputStream inputStreams = null;
        try {
            String str = new sun.misc.BASE64Encoder().encode(bytes.getBytes("GBK"));
            inputStream = new ByteArrayInputStream(str.getBytes());
            // 进行解码
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] byt = base64Decoder.decodeBuffer(inputStream);
            inputStreams = new ByteArrayInputStream(byt);

//            Files.copy(inputStreams, Paths.get(filePath1));
            ConvertRTFToHtml.sc(inputStreams, filePath1);//输入流保存到本地
            return filePath1;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null && inputStreams != null) {
                try {
                    inputStream.close();
                    inputStreams.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
  //接受输入流转存本地 编辑上传附件
    public JSONObject uploadAttachments(JSONObject jsonObject, String id, MKSCommand mks) {
        String fj = jsonObject.get("fileContent").toString();
        String fileNmae = jsonObject.get("fileName").toString();
        String fileType = jsonObject.get("fileType").toString();
        String attachmentFile = jsonObject.get("attachmentFile").toString();
        String str = filePath + "\\" + fileNmae + "." + fileType;

        //如果文件夹不存在则创建
        if (!new File(filePath).exists() && !new File(filePath).isDirectory()) {
            new File(filePath).mkdir();
        }
        //没有文件就创建
        if (!new File(str).exists()) {
            try {
                new File(str).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        conserveFile(str, fj);//输入流保存到本地
        Attachment attachment = new Attachment();
        attachment.setName(fileNmae + "." + fileType);
        attachment.setPath(str);
        try {
            mks.addAttachment(id, attachment, attachmentFile);
            log.info("上传附件成功: " + fileNmae + "." + fileType);
        } catch (APIException e) {
            log.info("上传附件出错: " + id + "(" + e.getMessage() + ")");
            e.printStackTrace();
        }

        return ResultStr("200", "1111");
    }

	/**
	 * 更新变更单信息
	 * 
	 * @param coID
	 * @throws APIException
	 */
	public void updateChangeInfo(String coID, MKSCommand mks) throws APIException {
		Map<String, String> changeInfo = mks.searchById(Arrays.asList(coID), Arrays.asList("ID", "Created By")).get(0);
		Map<String, String> changeMap = new HashMap<String, String>();
		changeMap.put("State", Constants.CHANGE_VERIFY);
		changeMap.put("Assigned User", changeInfo.get("Created By"));
		mks.editIssue(coID, changeMap, null);
	}
}
