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

	// @Autowired
	// private IntegrityFactory integrityFactory;

	public static final Log log = LogFactory.getLog(AlmController.class);
	// private MKSCommand mks = new MKSCommand();
	@Autowired
	private CacheManager cacheManager;

	// @Value("${filePath}")
	// private String filePath;

	String filePath = "C:\\\\Program Files\\\\Integrity\\\\ILMServer12\\\\data\\\\tmp";
	// String filePath = "C:\\\\SWFile";
	Cache cache = null;
	// @Value("${host}")
	// private String host;
	//
	// @Value("${port}")
	// private int port;
	//
	// @Value("${loginName}")
	// private String loginName;
	//
	@Value("${token}")
	private String token;

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
			if (mks == null) {
				mks = new MKSCommand();
			}
			allUsers = mks.getAllUsers(Arrays.asList("fullname", "name", "Email"));
		} catch (APIException e) {
			log.info("error: " + "查询所有用户错误！" + e.getMessage());
			e.printStackTrace();
		}

		// mks.close(host,port,loginName);

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
		getToken(jsonData.getString("Access_Token"));
		String project = jsonData.getString("project");
		String type = jsonData.getString("type");// 根据类型判断获取的动态组，Component获取Review
													// Committee Leader，其他获取
		List<String> dynamicGroups = new ArrayList<String>();
		if ("Component requirements Specification Document".equals(type)) {// Component到In
																			// Review,查询Review
																			// Committee
																			// Leader
			dynamicGroups.add("Review Committee DG");
			dynamicGroups.add("Review Committee Leader DG");
		} else {// 其他到In Approve，查询Project Manager DG
			dynamicGroups.add("Project Manager DG");
		}

		log.info("project-----" + project);
		// MKSCommand mks = new MKSCommand();
		// mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
		if (mks == null) {
			mks = new MKSCommand();
		}
		List<User> allUsers = new ArrayList<User>();
		try {
			log.info("开始链接：");
			// mks.initMksCommand(host, port, loginName, passWord);
			allUsers = mks.getProjectDynaUsers(project, dynamicGroups);
			// log.info("断开链接：");
			// mks.close(host, port, loginName);
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
		getToken(jsonData.getString("Access_Token"));
		log.info("-------------查询所用项目-------------");
		if (mks == null) {
			mks = new MKSCommand();
		}
		List<Project> allUsers = new ArrayList<Project>();
		try {
			// mks.initMksCommand(host, port, loginName, passWord);
			// mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
			allUsers = mks.getAllprojects(Arrays.asList("backingIssueID", "name"));
		} catch (APIException e) {
			log.info("error: " + "查询所有project错误！" + e.getMessage());
			e.printStackTrace();
		}

		// mks.close(host,port,loginName);

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

	@SuppressWarnings("unchecked")
	/**
	 * @Description
	 * @Author liuxiaoguang
	 * @Date 2020/7/22 10:02
	 * @Param [jsonData]
	 * @Return com.alibaba.fastjson.JSONObject
	 * @Exception 创建 修改 删除 移动文档条目
	 */
	@RequestMapping(value = "/releaseData", method = RequestMethod.POST)
	public JSONObject createDocument(@RequestBody JSONObject jsonData) {
		getToken(jsonData.getString("Access_Token"));
		log.info("-------------数据下发-------------");
		// docID = ""; //文档id
		if (mks == null) {
			mks = new MKSCommand();
		}
		String docId = "";
		log.info("参数信息：");
		log.info(jsonData);
		cache = cacheManager.getCache("orgCodeFindAll");
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("-------------docUUID + " + docUUID);
		String end = jsonData.get("end").toString();// 结尾标记，标识本次文档数据传输完毕

		// 移动到父id下面的位置 参数 first last before:name after:name
		// String insertLocation = jsonData.get("insertLocation"));
		if ("true".equals(end)) { // 所有参数存入缓存
			Map<String, String> SWSIDMap = new HashMap<>();// 存放已保存ID
			String project = jsonData.get("Project").toString();// 创建到的目标项目，通过分别查询
																// docId、docId +
																// Project，判断是否需要创建分支
			List<JSONObject> listData = null;// 保存排序后条目数据
			Element docEle = cache.get(docUUID);
			if (docEle == null) {
				listData = new ArrayList<>();
			} else
				listData = (List<JSONObject>) docEle.getObjectValue();
			listData.add(jsonData);

			List<JSONObject> contentsList = new ArrayList<>(listData.size());
			// 实现排序方法
			JSONObject docJSON = sortContainsAndGetDoc(listData, contentsList);
			// Collections.sort(list,
			// Comparator.comparingInt(String::length).thenComparing(Comparator.comparing(String::toLowerCase,
			// Comparator.reverseOrder())).
			// thenComparing(Comparator.reverseOrder()));
			log.info("排序后 List:");
			listData = null;// 置空，回收对象
			Map<String, JSONObject> SWJSONMap = new HashMap<String, JSONObject>(contentsList.size() * 4 / 3);
			Map<String, Boolean> SWDealMap = new HashMap<String, Boolean>(contentsList.size() * 4 / 3);
			Map<String, List<String>> SWMap = new HashMap<String, List<String>>(contentsList.size() * 4 / 3);// 记录SW_ID对应的ALMID
			for (JSONObject obj : contentsList) {
				String swSid = obj.getString(Constants.SW_SID_FIELD);
				SWJSONMap.put(swSid, obj);// 通过Map将每个JSON记录
				SWDealMap.put(swSid, false);// 通过Map记录SWSID对应数据是否处理。防止排序失败导致数据处理异常
				log.info(obj);
			}
			// mks.initMksCommand(host, port, loginName, passWord);
			boolean newDoc = false; // 判断是否是新增文档
			boolean branch = false;// 判断是否是复用文档
			String doc_SW_SID = null;
			String doc_SW_ID = null;
			log.info("总共---------------" + contentsList.size() + "数据，开始下发！");
			if (docJSON != null) {// 处理文档。每次进入都要更新SW_SID和SW_ID
				/**
				 * 判断： 1. 通过判断操作类型，add时，可能存在创建分支的情况。如果SW_SID未创建文档，则创建新文档，否则创建分支
				 * 2. update时，则通过old_SW_SID更新文档
				 */
				String action_Type = docJSON.getString("action_Type");
				doc_SW_SID = docJSON.getString("SW_SID");
				doc_SW_ID = docJSON.getString("SW_ID");
				String issue_Type = docJSON.getString("issue_Type");
				if ("add".equals(action_Type)) {//
					List<Map<String, String>> docList = null;
					try {
						docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, null);
					} catch (APIException e) {
						log.info("查询数据：" + APIExceptionUtil.getMsg(e));
						SWSIDMap = null;
						contentsList = null;
						docJSON = null;
						SWJSONMap = null;
						SWDealMap = null;
						SWMap = null;
						docId = null;
						issue_Type = null;
						action_Type = null;
						throw new MsgArgumentException("207", "根据SW_SID查询错误，请联系管理员!");
					}
					log.info("判断文档是否存在" + docList.size());
					log.info("判断文档是否存在" + docList);
					if (docList == null || docList.isEmpty() || docList.size() == 0) {
						docId = createDoc(docJSON, SWSIDMap, mks);// 当前未创建，创建新的文档
					} else {
						branch = true;
						Map<String, String> origInfo = null;
						for (Map<String, String> docInfo : docList) {
							if (project.equals(docInfo.get("Project"))) {// 如果目标项目已经存在数据，不创建
								throw new MsgArgumentException("206",
										"Document has created in project: [" + project + "]!");
							}
							if (origInfo == null) {
								origInfo = docInfo;
							} else {
								String createdDate = docInfo.get("Created Date");
								String orCreatedDate = origInfo.get("Created Date");
								Date target = new Date(createdDate);
								Date orgi = new Date(orCreatedDate);
								if (target.before(orgi)) {// 比对，获取最开始的一条数据
									origInfo = docInfo;
								}
							}
						}
						try {
							docId = mks.branchDocument(origInfo.get("ID"), project);
							/** 复用后更新文档SW_ID、SW_SID、ISSUEID等信息 */
							origInfo.put("ID", docId);
							updateDoc(origInfo, docJSON, false, true);
							/** 复用后更新文档SW_ID、SW_SID、ISSUEID等信息 */
							log.info("分支创建成功 ：" + project + " | " + docId);
						} catch (APIException e) {
							log.info("分支创建失败 ：" + APIExceptionUtil.getMsg(e));
							SWSIDMap = null;
							contentsList = null;
							docJSON = null;
							SWJSONMap = null;
							SWDealMap = null;
							SWMap = null;
							docId = null;
							issue_Type = null;
							action_Type = null;
							throw new MsgArgumentException("208", "创建分支错误! " + APIExceptionUtil.getMsg(e));
						}
					}
					newDoc = true;
				} else if ("update".equals(action_Type)) {
					doc_SW_SID = docJSON.getString("Old_SW_SID");
					List<Map<String, String>> docList = null;
					try {
						docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
					} catch (APIException e) {
						log.info("查询数据：" + APIExceptionUtil.getMsg(e));
					}
					if (docList == null || docList.isEmpty()) {// 更新判断文档是否存在
						throw new MsgArgumentException("204", "Document hadn't create。Please check you action type!");
					} else {
						Map<String, String> docInfo = docList.get(0);
						String curState = docInfo.get("State");
						if (!Constants.DOC_INIT_STATE.equals(curState)) {// 文档状态判断
							throw new MsgArgumentException("205",
									"Document now is in reivew or published, can not update!");
						}
						docId = docInfo.get("ID");
						updateDoc(docInfo, docJSON, false, true);
					}
				}
				SWSIDMap.put(doc_SW_SID, docId);
			}
			String assignedUser = docJSON.getString("Assigned_User");
			docJSON = null;// 文档数据回收
			/** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理 */
			List<String> branchDeleIssueList = null;// 记录复用文档后，需要移除的数据
			if (branch) {
				try {
					branchDeleIssueList = mks.getDocContents(docId, SWSIDMap);
					log.info("分支查询数据为： " + Arrays.asList(branchDeleIssueList));
					log.info("分支查询数据为： " + JSON.toJSONString(SWSIDMap));
				} catch (APIException e1) {
					log.info("分支文档条目查询失败");
					SWSIDMap = null;
					contentsList = null;
					docJSON = null;
					SWJSONMap = null;
					SWDealMap = null;
					SWMap = null;
					docId = null;
					throw new MsgArgumentException("209", "查询分支文档数据失败! " + APIExceptionUtil.getMsg(e1));
				}
			}
			/** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理 */
			/** 处理数据1 */
			for (int i = 0; i < contentsList.size(); i++) {
				try {
					dealContentJson(contentsList.get(i), SWSIDMap, SWMap, SWDealMap, SWJSONMap, docId, mks, branch, newDoc,
							branchDeleIssueList);
				} catch (MsgArgumentException e) {
					log.info("清理缓存！");
					cache.remove(docUUID);
					deleteTmpFile();
					throw e;
				} catch (APIException e) {
					log.info("清理缓存！");
					cache.remove(docUUID);
					deleteTmpFile();
					log.info("处理数据失败 ： " + APIExceptionUtil.getMsg(e));
					throw new MsgArgumentException("201", "处理数据失败! " + APIExceptionUtil.getMsg(e));
				}
			}
			/** 处理追溯3 */
			for (int i = 0; i < contentsList.size(); i++) {
				JSONObject contentObj = contentsList.get(i);
				try {
					dealRelationship(contentObj, SWSIDMap, SWMap, mks);
					contentObj = null;// 处理完成，将此数据设置null
				} catch (APIException e) {
					contentObj = null;// 处理完成，将此数据设置null
					cache.remove(docUUID);
					/** 2释放word处理进程 并删除临时文件*/
					deleteTmpFile();
					/** 2释放word处理进程 并删除临时文件 */
					throw new MsgArgumentException("201", "处理数据失败! " + APIExceptionUtil.getMsg(e));
				}
				
			}
			/** 如果分支创建完成，需要删除结构的，进行结构删除。 */
			if (branchDeleIssueList != null) {
				for (String issueId : branchDeleIssueList) {
					try {
						mks.removecontent(issueId);
						mks.deleteissue(issueId);
					} catch (APIException e) {
						log.info("删除分支 复用条目 失败-" + issueId + " | 失败原因：" + APIExceptionUtil.getMsg(e));
						log.info("分支文档条目查询失败");
						SWSIDMap = null;
						contentsList = null;
						docJSON = null;
						SWJSONMap = null;
						SWDealMap = null;
						SWMap = null;
						docId = null;
						/** 2释放word处理进程 并删除临时文件*/
						deleteTmpFile();
						/** 2释放word处理进程 并删除临时文件 */
						throw new MsgArgumentException("210", "删除条目失败! " + APIExceptionUtil.getMsg(e));
					}
				}
			}
			/** 如果分支创建完成，需要删除结构的，进行结构删除。 */
			SWDealMap = null;// 置空，回收对象
			SWJSONMap = null;// 置空，回收对象
			SWSIDMap = null;// 置空，回收对象
			contentsList = null;// 置空，回收对象
			// 新增后修改状态为评审 in approve
			docUUID = jsonData.getString(Constants.DOC_UUID);
			log.info("数据下发完成 开始修改状态 Doc_id : " + docId);
			try {
				log.info("评审人---" + assignedUser);
				String doctype = jsonData.getString("issue_Type");
				log.info("判断文档是否是coment==" + doctype);
				if ("Component Requirement Specification Documnet".equals(doctype)) {
					String[] arr = mks.getStaticGroup("VCU");
					if (Arrays.asList(arr).contains(assignedUser)) {
						log.info("评审人在VCU组");
						Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
						dataMap.put("State", "In Review");
						dataMap.put("Assigned User", assignedUser);
						mks.editIssue(docId, dataMap, new HashMap<String, String>());
						log.info("into: " + "修改状态In Review");
					}
				} else {
					Map<String, String> dataMap3 = new HashMap<String, String>();// 普通字段
					dataMap3.put("State", "in approve");
					dataMap3.put("Assigned User", assignedUser);
					mks.editIssue(docId, dataMap3, new HashMap<String, String>());
					log.info("into: " + "修改状态in approve");
				}
			} catch (APIException e) {
				log.info("修改状态出错 ： " + e.getMessage());
				log.info("清理缓存！" + docUUID);
				cache.remove(docUUID);
				/** 2释放word处理进程 并删除临时文件*/
				deleteTmpFile();
				/** 2释放word处理进程 并删除临时文件 */
				e.printStackTrace();
			}
			try {
				String label = "Autobaseline:from SWR :" + doc_SW_ID;
				log.info("基线标题===" + label + "|基线文档id===" + docId);
				mks.createBaseLine(label, docId);// 自动创建基线信息
			} catch (APIException e) {
				// TODO Auto-generated catch block
				log.info("创建基线错误！" + e.getMessage());
				log.info("分支文档条目查询失败");
				SWSIDMap = null;
				contentsList = null;
				docJSON = null;
				SWJSONMap = null;
				SWDealMap = null;
				SWMap = null;
				docId = null;
				cache.remove(docUUID);
				/** 2释放word处理进程 并删除临时文件*/
				deleteTmpFile();
				/** 2释放word处理进程 并删除临时文件 */
				throw new MsgArgumentException("210", "文档基线创建失败! " + APIExceptionUtil.getMsg(e));
			}
			/** 2释放word处理进程 并删除临时文件*/
			deleteTmpFile();
			/** 2释放word处理进程 并删除临时文件 */
			log.info("执行完成，清理缓存！");
			cache.remove(docUUID);
		} else {
			Element docEle = cache.get(docUUID);
			List<JSONObject> docContentList = null;
			if (docEle == null) {
				docContentList = new ArrayList<>();
			} else
				docContentList = (List<JSONObject>) docEle.getObjectValue();
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
	 * @throws APIException 
	 * @throws MsgArgumentException 
	 */
	private void dealContentJson(JSONObject contentObj, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			Map<String, Boolean> SWDealMap, Map<String, JSONObject> SWJSONMap, String docId, MKSCommand mks,
			boolean branch, boolean newDoc, List<String> branchDeleIssueList) throws MsgArgumentException, APIException {
		String SWSID = contentObj.getString(Constants.SW_SID_FIELD);// 当前JSON的ID
		log.info("SW_SID 处理中- " + SWSID);
		if (SWDealMap.get(SWSID)) {
			log.info("SWSID - " + SWSID + " == 数据已经处理过，跳过本次循环");
			return;
		}
		String beforeId = contentObj.getString("Before_ID");
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
			String Old_SWSID = contentObj.getString(Constants.SW_SID_FIELD);
			if (SWIDMap.get(SWSID) != null) {// 如果在原结构有此数据，则移除。不做处理
				branchDeleIssueList.remove(SWIDMap.get(SWSID));
				// 同时直接将原数据进行 Move & Update
				MoveDoc(contentObj, SWIDMap, SWMap, docId, false, true, null);
			} else if (SWIDMap.get(Old_SWSID) != null) {// 如果在原结构有此数据，则移除。不做处理
				branchDeleIssueList.remove(SWIDMap.get(Old_SWSID));
				// 同时直接将原数据进行 Move & Update
				MoveDoc(contentObj, SWIDMap, SWMap, docId , false, true, null);
			} else {
				// 在原结构中找不到响相应数据，新增。
				AddEntry(contentObj, SWIDMap, SWMap, docId, false, true);
			}
		} else {
			String action_Type = contentObj.getString("action_Type");
			if ("add".equals(action_Type)) {// 创建分支，可能会选择历史数据进行分支。此时直接复用的分支进行更新
				AddEntry(contentObj, SWIDMap, SWMap, docId, false, true);
			} else if ("update".equals(action_Type) && !newDoc) {// 新建文档，不执行更新操作
				UpDoc(contentObj, SWIDMap, SWMap, docId, false, true, null);
			} else if ("delete".equals(action_Type) && !newDoc) {// 新建文档，不执行删除操作
				DelDoc(contentObj, docId, false, true);
			} else if (("move".equals(action_Type) || "update/move".equals(action_Type)) && !newDoc) {// 新建文档，不执行移动操作
				MoveDoc(contentObj, SWIDMap, SWMap, docId, false, true, null);
			}
		}

		SWJSONMap.remove(SWSID);// 处理完成，将SWJSONMap置空。
		SWDealMap.put(SWSID, true);// 更新SWSID对应处理结果
	}

	/**
	 * 将 DOC JSON查询出来，并将条目 JSON排序
	 * 
	 * @param contains
	 * @return
	 */
	private JSONObject sortContainsAndGetDoc(List<JSONObject> contains, List<JSONObject> result) {
		JSONObject docJson = null;
		// 1 将所有的 条目 JSON获取出来
		for (JSONObject obj : contains) {
			String category = obj.getString(Constants.CATEGORY);
			if (Constants.DOC_CATEGORY.equalsIgnoreCase(category)) {// 获取当前文档JSON
				docJson = obj;
			} else// 添加条目JSON
				result.add(obj);
		}
		// 2 排序条目，按 parent_id, before_id排序
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
				if (sid1Len == sid2Len) {// 长度相同，说明在同一级
					if (parent1.equals(parent2)) {// 同一个父级下，对比是否有一个是另一个的在另一个之前。不在同一个父级下，不对比
						if ("".equals(before1)) {// 为空在最前
							return -1;
						} else if ("".equals(before2)) {// 为空在最前
							return 1;
						} else if (sw_sid2.equals(before1)) {// 1 before 2
							return 1;
						} else if (sw_sid1.equals(before2)) {// 2 before 1
							return -1;
						}
					}
				} else {// SW_SID长度不一致时，短的在前，判断一个是否是另一个的Parent
					return sid1Len - sid2Len;
				}
				return 0;
			}
		});

		return docJson;
	}

	// 根据条目id获取文档id
	public String getDocID(String e_id, String docId, MKSCommand mks) {
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

	// 变更反馈增删改条目
	@RequestMapping(value = "/changeAction", method = RequestMethod.POST)
	public JSONObject changeAction1(@RequestBody JSONObject jsonData) {
		getToken(jsonData.getString("Access_Token"));
		log.info(jsonData);
		if(mks == null)
			mks = new MKSCommand();
		// mks.initMksCommand(host, port, loginName, passWord);
		String category = jsonData.getString("Category");
		log.info("变更反馈 - " + category);
		String docId = null;
		String resultStr = null;
		/** 判断变更关联对象，如果是文档，全更新；如果是条目，当前条目全更新，其他条目部分更新*/
		List<String> relatedIssueId = null;
		Boolean docChange = false;
		try {
			String authoresChange = mks.searchById(Arrays.asList(jsonData.getString("ALM_CO_ID")), Arrays.asList("Authorizes Changes To")).get(0).get("Authorizes Changes To");
			relatedIssueId = Arrays.asList(authoresChange.split(","));
			String typeCate = mks.searchById(relatedIssueId, Arrays.asList("Category")).get(0).get("Category");
			if("Document".equals(typeCate)){//关联的变更数据类型
				docChange = true;
			}
		} catch (APIException e1) {
			log.info("通过Change Order判断更新数据");
		}
		/** 判断变更关联对象*/
		if ("Document".equalsIgnoreCase(category)) {// 文档处理
			/** 变更更新文档数据 */
			String doc_SW_SID = jsonData.getString("Old_SW_SID");
			List<Map<String, String>> docList = null;
			String issue_Type = jsonData.getString("issue_Type");
			String project = jsonData.getString("Project");
			try {
				docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
			} catch (APIException e) {
				log.info("查询数据：" + APIExceptionUtil.getMsg(e));
			}
			if (docList != null && !docList.isEmpty()) {// 更新判断文档是否存在
				Map<String, String> docInfo = docList.get(0);
				docId = docInfo.get("ID");
				updateDoc(docInfo, jsonData, docChange, false);
			} else {
				throw new MsgArgumentException("204",
						"Can not find Document ,Document Structure ID : " + doc_SW_SID + "!");
			}
		} else {
			/** 变更更新条目数据 */
			Map<String, String> SWSIDMap = new HashMap<>();
			String action_Type = jsonData.getString("action_Type");// 创建、更新、删除或移动
			// 创建文档需要的参数
			
			try {
				log.info("变更类型 ： "+ docChange + "变更数据" + Arrays.asList(relatedIssueId));
				if (action_Type.equals("add")) {
					resultStr = AddEntry(jsonData, SWSIDMap, null, docId, docChange, false );
				} else if (action_Type.equals("update")) {
					resultStr = UpDoc(jsonData, SWSIDMap, null, docId, docChange, false, relatedIssueId);
				} else if (action_Type.equals("delete")) {
					resultStr = DelDoc(jsonData, docId, docChange, false);
				} else if (action_Type.equals("move")) {
					resultStr = MoveDoc(jsonData, SWSIDMap, null, docId, docChange, false, relatedIssueId);
				}
				deleteTmpFile();
				dealRelationship(jsonData, SWSIDMap, null, mks);
				docId = mks.getTypeById(resultStr, "Document ID");
				log.info("返回的根据条目查询的文档id:" + docId);
			} catch (MsgArgumentException e) {
				deleteTmpFile();
				throw e;
			} catch (APIException e) {
				deleteTmpFile();
				log.info("变更 处理失败：" + APIExceptionUtil.getMsg(e));
				e.printStackTrace();
			}
		}

		/** 当数据处理完毕后，修改变更单 */
		String end = jsonData.getString("end");// 结尾标记，标识本次文档数据传输完毕
		if ("true".equals(end)) {
			try {
				updateChangeInfo(jsonData.getString("ALM_CO_ID"), mks);
			} catch (APIException e) {
				log.info("变更单处理失败，失败原因：" + APIExceptionUtil.getMsg(e));
			} // 更新变更信息
		}
		// mks.close(host,port,loginName);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("data", ResultJson("DOC_ID", docId));
		log.info(jsonObject);
		return jsonObject;
	}

	// 文档新增
	public String createDoc(JSONObject jsonData, Map<String, String> SWIDMap, MKSCommand mks) {
		log.info("-------------新增文档-----------");
		verification(jsonData);
		String issue_Type = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
		String SW_SID = jsonData.getString("SW_SID");
		String project = jsonData.getString("Project");
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("创建SW_SID : " + SW_SID);
		// 先判断是否创建过
		String docType = new AnalysisXML().resultType(issue_Type);
		Map<String, String> docdataMap = new HashMap<String, String>();// 普通字段
		Map<String, String> docmap = new AnalysisXML().resultFile(issue_Type);
		for (String key : docmap.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key) || key == null || "".equals(key))
				continue;// 跳过不更新
			String strKey = jsonData.getString(key);
			if (strKey != null && !"".equals(strKey)) {
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
			log.info("issue_id ====== :" + jsonData.getString("issue_id"));
			String doc_Id = mks.createDocument(docType, docdataMap, null);
			SWIDMap.put(SW_SID, doc_Id);
			// swid_id.put("doc_" + SW_SID, doc_Id);
			log.info("创建的文档id： " + doc_Id);
			return doc_Id;
		} catch (APIException e) {
			log.info("error: " + "创建文档出错！" + APIExceptionUtil.getMsg(e));
			log.info("清理缓存！");
			if (docUUID != null && !"".equals(docUUID)) {
				cache.remove(docUUID);
			}
			throw new MsgArgumentException("201", "创建文档出错 " + APIExceptionUtil.getMsg(e));
		}
	}

	/**
	 * 更新文档SW_ID、SW_SID
	 * 
	 * @param jsonData
	 * @param mks
	 */
	public void updateDoc(Map<String, String> docInfo, JSONObject jsonData, boolean docChange, boolean dealDoc) {
		log.info("-------------修改文档-----------");
		String SW_SID = jsonData.getString("SW_SID");
		String docId = docInfo.get("ID");
		String issue_Type = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		Map<String, String> docdataMap = new HashMap<String, String>();// 普通字段
		Map<String, String> docmap = new AnalysisXML().resultFile(issue_Type);
		for (String key : docmap.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key) || key == null || "".equals(key))
				continue;// 跳过不更新
			String strKey = jsonData.getString(key);
			if (strKey != null && !"".equals(strKey)) {
				if (key.equals("SW_ID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else if (key.equals("SW_SID")) {
					docdataMap.put(docmap.get(key), strKey);
				} else {
					if(dealDoc || docChange)
						docdataMap.put(docmap.get(key), strKey);
				}
			}
		}
		// rtf

		try {
			// docdataMap.put("Assigned User", "admin");
			log.info("issue_id ====== :" + jsonData.getString("issue_id"));
			mks.editIssue(docId, docdataMap, null);
			// swid_id.put("doc_" + SW_SID, doc_Id);
			log.info("更新文档成功：" + docId + " SW_SID=" + SW_SID);
		} catch (APIException e) {
			log.info("error: " + "更新文档出错！" + APIExceptionUtil.getMsg(e));
			log.info("清理缓存！");
			if (docUUID != null && !"".equals(docUUID)) {
				cache.remove(docUUID);
			}
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
	 * @throws APIException 
	 * @throws MsgArgumentException 
	 */
	public String AddEntry(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			String docId, boolean docChange, boolean dealDoc) throws MsgArgumentException, APIException {
		log.info("-------------新增条目-----------");
		verification(jsonData);
		String docType = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
		String SW_SID = jsonData.getString("SW_SID");
		String SW_ID = jsonData.getString("SW_ID");
		String project = jsonData.getString("Project");
		String docUUID = jsonData.getString(Constants.DOC_UUID);
		log.info("创建SW_SID : " + SW_SID);
		String issueType = docType.substring(0, docType.lastIndexOf(" "));
		/** 设置位置信息 */
		String Parent_ID = jsonData.getString("Parent_ID");
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
		String Before_ID = jsonData.getString("Before_ID");// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
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
				UpDoc(jsonData, SWIDMap, SWMap, docId, docChange, dealDoc, null);
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
				String strKey = jsonData.getString(key);
				if (strKey != null && !"".equals(strKey)) {
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
			String header = new AnalysisXML().resultCategory(issueType, jsonData.getString("Category"));
			if ("".equals(header)) {
				header = "Heading";
			}
			dataMap.put("Category", header);
			// rtf
			String Text1 = jsonData.getString("issue_text");
			if (Text1 != null && !"".equals(Text1)) {
				dataMap.put("Text", rtfString(Text1));
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
						uploadAttachments(j, issueId, mks);
					}
				}
			} catch (APIException e) {
				log.info("error: " + "创建文档条目出错！" + APIExceptionUtil.getMsg(e));
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

	// 文档条目修改
	public String UpDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap, String docId 
			, boolean docChange, boolean dealDoc, List<String> changeList) throws MsgArgumentException,APIException{
		log.info("-------------修改条目-----------");
		String doc_Type = jsonData.getString("issue_Type");
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

		Map<String, String> map = new AnalysisXML().resultFile(issue_Type);
		for (String key : map.keySet()) {
			if ("Assigned_User".equals(key) || "Assigned User".equals(key))
				continue;// 跳过不更新
			String strKey = jsonData.getString(key);
			if (strKey != null && !"".equals(strKey)) {
				if (key.equals("SW_ID")) {
					dataMap.put(map.get(key), strKey);
				} else if (key.equals("SW_SID")) {
					dataMap.put(map.get(key), strKey);
				} else {
					if( docChange || dealDoc || ( changeList !=null && changeList.contains(id)))//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
						dataMap.put(map.get(key), strKey);
				}
			}
		}
		if( docChange || dealDoc || ( changeList !=null && changeList.contains(id))){//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
			Object Text1 = jsonData.get("issue_text");
			if (Text1 != null && !"".equals(Text1.toString())) {
				String htmlStr = rtfString(Text1.toString());
				richDataMap.put("Text", htmlStr);
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
			if( docChange || dealDoc || ( changeList !=null && changeList.contains(id))){//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
				Object Attachments1 = jsonData.get("Attachments");
				if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
					String[] Attachments2 = (String[]) Attachments1;
					for (int i = 0; i < Attachments2.length; i++) {
						JSONObject j = JSONObject.parseObject(Attachments2[i]);
						uploadAttachments(j, id, mks);
					}
				}
			}
		} catch (APIException e) {
			log.info("error: " + "修改条目出错！" + APIExceptionUtil.getMsg(e));
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

	// 条目删除
	public String DelDoc(JSONObject jsonData, String docId, boolean docChange, boolean dealDoc) throws MsgArgumentException{
		log.info("-------------删除文档-----------");
		String SW_SID = jsonData.getString("SW_SID");// 文档id
		String project = jsonData.getString("Project");// 文档id
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
			throw new MsgArgumentException("201", "删除条目关系出错 " + APIExceptionUtil.getMsg(e));
		}
		try {
			mks.deleteissue(id);
			log.info("删除的条目id： " + id);
		} catch (APIException e) {
			log.info("error: " + "删除条目出错！" );
			throw new MsgArgumentException("201", "删除条目出错 " + APIExceptionUtil.getMsg(e));
		}
		return id;
	}

	// 条目移动
	public String MoveDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
			String docId, boolean docChange, boolean dealDoc, List<String> changeList) throws MsgArgumentException, APIException {
		log.info("-------------移动文档-----------");
		String oldSWSID = jsonData.getString("Old_SW_SID");// 旧ID
		if (oldSWSID != null && !"".equals(oldSWSID)) {// OLD SW_SID不为空时，才需要移动
			String project = jsonData.getString("Project");// 文档id
			String docType = jsonData.getString("issue_Type");
			String issueType = docType.substring(0, docType.lastIndexOf(" "));
			String id = SWIDMap.get(oldSWSID);// 复用时，可以通过SW_SID - ALM ID
												// Map获取到数据
			if (id == null) {// 获取不到，使用查询
				id = mks.getIssueBySWID("SW_SID", jsonData.getString("Old_SW_SID"), project, issueType, "ID");
			}
			log.info("需要移动的sw_sid -" + jsonData.getString("Old_SW_SID") + ";id:" + id);
			// 获取文档id
			String Parent_ID = jsonData.getString("Parent_ID");
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
			String Before_ID = jsonData.getString("Before_ID");// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
			log.info("需要移动到前面的id -" + jsonData.getString("Before_ID") + ";id:" + Before_ID);
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
		return UpDoc(jsonData, SWIDMap, SWMap, docId, docChange, dealDoc, changeList);
	}

	/**
	 * 更新变更单信息
	 * 
	 * @param coID
	 * @throws APIException
	 */
	private void updateChangeInfo(String coID, MKSCommand mks) throws APIException {
		Map<String, String> changeInfo = mks.searchById(Arrays.asList(coID), Arrays.asList("ID", "Created By")).get(0);
		Map<String, String> changeMap = new HashMap<String, String>();
		changeMap.put("State", Constants.CHANGE_VERIFY);
		changeMap.put("Assigned User", changeInfo.get("Created By"));
		mks.editIssue(coID, changeMap, null);
	}

	// 输入流转换html string
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
		conserveFile(str, text);
		ConvertRTFToHtml.RTFToHtml(str, str1);// 本地rtf文件转换为html
		String htmldata = null;// 获取html中元素
		try {
			htmldata = ConvertRTFToHtml.readHtml(str1 + ".htm");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return htmldata;
	}

	// 接受输入流转存本地 编辑上传附件
	public JSONObject uploadAttachments(JSONObject jsonObject, String id, MKSCommand mks) {
		String fj = jsonObject.get("fileContent").toString();
		String fileNmae = jsonObject.get("fileName").toString();
		String fileType = jsonObject.get("fileType").toString();
		String attachmentFile = jsonObject.get("attachmentFile").toString();
		String str = filePath + "\\" + fileNmae + "." + fileType;

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

		conserveFile(str, fj);// 输入流保存到本地
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
	 * byte 转文件 下载到本地
	 *
	 * @param
	 */
	public String conserveFile(String filePath1, String bytes) {
		// byte[] bytes= DatatypeConverter.parseBase64Binary(base64String);
		InputStream inputStream = null;
		InputStream inputStreams = null;
		try {
			String str = new sun.misc.BASE64Encoder().encode(bytes.getBytes("GBK"));
			inputStream = new ByteArrayInputStream(str.getBytes());
			// 进行解码
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] byt = base64Decoder.decodeBuffer(inputStream);
			inputStreams = new ByteArrayInputStream(byt);

			// Files.copy(inputStreams, Paths.get(filePath1));
			ConvertRTFToHtml.sc(inputStreams, filePath1);// 输入流保存到本地
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

	/**
	 * 添加追溯关系
	 * 
	 * @param jsonData
	 * @param SWSIDMap
	 * @param SWMap
	 * @param mks
	 * @throws APIException 
	 */
	public void dealRelationship(JSONObject jsonData, Map<String, String> SWSIDMap, Map<String, List<String>> SWMap,
			MKSCommand mks) throws APIException {
		String Delete_Trace_ID = jsonData.getString("Delete_Trace_ID");
		// SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
		String addTrace_ID = jsonData.getString("Trace_ID");//
		log.info("Delete_Trace_ID 删除 ：" + Delete_Trace_ID);
		log.info("Trace_ID 添加：" + addTrace_ID);
		if ((addTrace_ID == null || "".equals(addTrace_ID))
				&& (Delete_Trace_ID == null || "".equals(Delete_Trace_ID))) {
			return;// 如果追溯关系删除和添加都没有，跳过本条处理
		}
		String SW_SID = jsonData.getString("SW_SID");// SW_SID
		String issueId = SWSIDMap.get(SW_SID);// 条目ID
		log.info("Issue ID ：" + issueId + " || SW_SID" + SW_SID);
		if (issueId == null || "".equals(issueId)) {
			log.info("Issue ID获取不到：" + SW_SID);
			return;// 获取不到issueID
		}
		String doc_Type = jsonData.getString("issue_Type");
		String issue_Type = doc_Type.substring(0, doc_Type.lastIndexOf(" "));
		String project = jsonData.getString("Project");// 所属项目
		/** Modify By Cai Hao, 添加关联关系 */
		log.info("需要添加的 - - - Teace_Id" + addTrace_ID);
		log.info("需要删除的 - - - Delete_Trace_ID" + Delete_Trace_ID);
		editIssueRelationship(issueId, issue_Type, Delete_Trace_ID, addTrace_ID, SWMap, project, mks);
		/** Modify By Cai Hao, 添加关联关系 */
	}

	/**
	 * 添加/删除关联关系
	 *
	 * @param curIssueId
	 * @param deleteIssueStrs
	 * @param addIssueStrs
	 * @return
	 * @throws APIException 
	 */
	public boolean editIssueRelationship(String curIssueId, String curType, String deleteIssueStrs, String addIssueStrs,
			Map<String, List<String>> SWMap, String project, MKSCommand mks) throws APIException {

		Map<String, String> deleRelationMap = null;
		if (!Obj.isEmptyOrNull(deleteIssueStrs)) {/* 拼接删除关系 */
			List<Map<String, String>> deleteIssueList = null;
			log.info("开始删除关联关系" + deleteIssueStrs);
			deleRelationMap = new HashMap<String, String>();
			try {
				deleteIssueList = mks.searchALMIDTypeBySWID(Arrays.asList(deleteIssueStrs.split(",")), null);
			} catch (APIException e) {
				log.info("查找ISSUE失败，失败原因：" + APIExceptionUtil.getMsg(e));
				throw e;
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
		if (!Obj.isEmptyOrNull(addIssueStrs)) {/* 拼接添加关系 */
			List<Map<String, String>> addIssueList = null;
			log.info("开始添加关联关系" + addIssueStrs);
			addRelationMap = new HashMap<String, String>();
			try {
				addIssueList = mks.searchALMIDTypeBySWID(Arrays.asList(addIssueStrs.split(",")), null);
			} catch (APIException e) {
				log.info("查找ISSUE失败，失败原因：" + APIExceptionUtil.getMsg(e));
				throw e;
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
					if (almIdList == null || almIdList.isEmpty()) {
						continue;
					}
					String relationField = AnalysisXML.getRelationshipField(curType, curType);
					log.info("在创建过程中添加追溯。类型：" + curType + ",关系字段：" + relationField);
					String editVal = addRelationMap.get(relationField);
					for (String almId : almIdList) {
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
			throw e;
		}
	}

	/**
	 * token验证
	 */
	public void getToken(String str) {
		if (token.equals(str)) {
			log.info("token验证成功!");
			return;
		} else {
			log.error("token验证失败!");
			throw new MsgArgumentException("201", "token Validation failed!");
		}
	}
	
	/**
	 * 删除临时文件
	 */
	public void deleteTmpFile(){
		File dir = new File(filePath);
		if(dir.exists() && dir.isDirectory()){
			String[] files = dir.list();// 读取目录下的所有目录文件信息
			if(files != null){
				File file = null;
				for(String fileName : files){
					file = new File(fileName);
					try {
						if(file.exists())
							file.delete();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}
	}

	public static void main(String[] str) {
		// MKSCommand mks = new MKSCommand();
		// mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
		//// String isNO = mks.getProjectNameById("22324");
		// String id = mks.getDocIdsByType("SW_ID","85125614","type");
		// mks.getDocIdsByType("SW_SID","entry_x0400000000084B8D","ID");
		// String value = "{\\rtf1 \\ansi \\ansicpg936 \\deff0 \\stshfdbch1
		// \\stshfloch2 \\stshfhich2 \\deflang2052 \\deflangfe2052 {\\fonttbl
		// {\\f0 \\froman \\fcharset0 \\fprq2 {\\*\\panose
		// 02020603050405020304}Times New Roman{\\*\\falt Times New
		// Roman};}{\\f1 \\fnil \\fcharset134 \\fprq0 {\\*\\panose
		// 02010600030101010101}\\'cb\\'ce\\'cc\\'e5{\\*\\falt
		// \\'cb\\'ce\\'cc\\'e5};}{\\f2 \\fswiss \\fcharset0 \\fprq0
		// {\\*\\panose 020f0502020204030204}Calibri{\\*\\falt Calibri};}{\\f3
		// \\fnil \\fcharset2 \\fprq0 {\\*\\panose
		// 05000000000000000000}Wingdings{\\*\\falt
		// Wingdings};}}{\\colortbl;\\red0\\green0\\blue0;\\red128\\green0\\blue0;\\red255\\green0\\blue0;\\red0\\green128\\blue0;\\red128\\green128\\blue0;\\red0\\green255\\blue0;\\red255\\green255\\blue0;\\red0\\green0\\blue128;\\red128\\green0\\blue128;\\red0\\green128\\blue128;\\red128\\green128\\blue128;\\red192\\green192\\blue192;\\red0\\green0\\blue255;\\red255\\green0\\blue255;\\red0\\green255\\blue255;\\red255\\green255\\blue255;}{\\stylesheet
		// {\\qj \\li0 \\ri0 \\nowidctlpar \\aspalpha \\aspnum \\adjustright
		// \\lin0 \\rin0 \\itap0 \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch
		// \\dbch \\af1 \\hich \\af2 \\loch \\f2 \\lang1033 \\langnp1033
		// \\langfe2052 \\langfenp2052 \\snext0 \\sqformat \\spriority0
		// Normal;}{\\*\\cs10 \\rtlch \\ltrch \\snext10 \\ssemihidden
		// \\spriority0 Default Paragraph Font;}}{\\*\\latentstyles
		// \\lsdstimax260 \\lsdlockeddef0 \\lsdsemihiddendef1
		// \\lsdunhideuseddef1 \\lsdqformatdef0 \\lsdprioritydef99
		// {\\lsdlockedexcept \\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1
		// \\lsdpriority0 \\lsdlocked0 Normal;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 1;\\lsdqformat1
		// \\lsdpriority0 \\lsdlocked0 heading 2;\\lsdqformat1 \\lsdpriority0
		// \\lsdlocked0 heading 3;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0
		// heading 4;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading
		// 5;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 6;\\lsdqformat1
		// \\lsdpriority0 \\lsdlocked0 heading 7;\\lsdqformat1 \\lsdpriority0
		// \\lsdlocked0 heading 8;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0
		// heading 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 index 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 toc 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Normal Indent;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 footnote text;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation
		// text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// header;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// footer;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// index heading;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0
		// caption;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// table of figures;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 envelope address;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 envelope return;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footnote
		// reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 annotation reference;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 line number;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 page
		// number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// endnote reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 endnote text;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 table of authorities;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 macro;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toa
		// heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// List Bullet;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 List Number;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List Bullet 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List
		// Bullet 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 List Bullet 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List Number 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List
		// Number 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 List Number 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Title;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Closing;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Signature;\\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Default
		// Paragraph Font;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Body Text;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Body Text Indent;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List
		// Continue;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 List Continue 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 List Continue 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List
		// Continue 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Message Header;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Subtitle;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Salutation;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Date;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Body Text First Indent;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Body Text First Indent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Note
		// Heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Body Text 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Body Text 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Body Text Indent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Block
		// Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Hyperlink;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 FollowedHyperlink;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Strong;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0
		// Emphasis;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Document Map;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Plain Text;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 E-mail
		// Signature;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Normal (Web);\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 HTML Acronym;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML
		// Address;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// HTML Cite;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 HTML Code;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 HTML Definition;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML
		// Keyboard;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 HTML Preformatted;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 HTML Sample;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML
		// Typewriter;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 HTML Variable;\\lsdunhideused0 \\lsdqformat1
		// \\lsdpriority0 \\lsdlocked0 Normal Table;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation
		// subject;\\lsdpriority99 \\lsdlocked0 No List;\\lsdpriority99
		// \\lsdlocked0 1 / a / i;\\lsdpriority99 \\lsdlocked0 1 / 1.1 /
		// 1.1.1;\\lsdpriority99 \\lsdlocked0 Article / Section;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Simple 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Simple 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Classic 1;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Classic 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Classic 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Colorful 1;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Colorful 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Columns 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Columns 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Columns 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Columns 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Grid 1;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Grid 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Table Grid 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Grid 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Grid 6;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid
		// 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Grid 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Table List 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table List 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table List 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// List 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Table List 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table List 7;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table List 8;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// 3D effects 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table 3D effects 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Contemporary;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Elegant;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Table Professional;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Table Subtle 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Subtle 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Web 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0
		// Table Web 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0
		// \\lsdlocked0 Balloon Text;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority0 \\lsdlocked0 Table Grid;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table
		// Theme;\\lsdpriority99 \\lsdlocked0 Placeholder Text;\\lsdpriority99
		// \\lsdlocked0 No Spacing;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority60 \\lsdlocked0 Light Shading;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light
		// List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0
		// Light Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63
		// \\lsdlocked0 Medium Shading 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority64 \\lsdlocked0 Medium Shading 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0
		// Medium List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67
		// \\lsdlocked0 Medium Grid 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority68 \\lsdlocked0 Medium Grid 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark
		// List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72
		// \\lsdlocked0 Colorful List;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority73 \\lsdlocked0 Colorful Grid;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0
		// Light List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62
		// \\lsdlocked0 Light Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0
		// Medium Shading 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 1;\\lsdpriority99
		// \\lsdlocked0 List Paragraph;\\lsdpriority99 \\lsdlocked0
		// Quote;\\lsdpriority99 \\lsdlocked0 Intense Quote;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0
		// Medium Grid 1 Accent 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 1;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark
		// List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71
		// \\lsdlocked0 Colorful Shading Accent 1;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent
		// 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0
		// Colorful Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority60 \\lsdlocked0 Light Shading Accent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0
		// Light Grid Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63
		// \\lsdlocked0 Medium Shading 1 Accent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0
		// Medium List 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0
		// Medium Grid 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading Accent 2;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority72 \\lsdlocked0 Colorful List Accent 2;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent
		// 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0
		// Light Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority61 \\lsdlocked0 Light List Accent 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0
		// Medium Shading 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0
		// Medium List 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0
		// Medium Grid 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority72 \\lsdlocked0 Colorful List Accent 3;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent
		// 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0
		// Light Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority61 \\lsdlocked0 Light List Accent 4;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0
		// Medium Shading 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0
		// Medium List 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 4;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0
		// Medium Grid 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 4;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority72 \\lsdlocked0 Colorful List Accent 4;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent
		// 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0
		// Light Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority61 \\lsdlocked0 Light List Accent 5;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent
		// 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0
		// Medium Shading 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent
		// 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0
		// Medium List 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 5;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent
		// 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0
		// Medium Grid 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 5;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent
		// 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority72 \\lsdlocked0 Colorful List Accent 5;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent
		// 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0
		// Light Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority61 \\lsdlocked0 Light List Accent 6;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent
		// 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0
		// Medium Shading 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent
		// 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0
		// Medium List 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 6;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent
		// 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0
		// Medium Grid 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 6;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent
		// 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0
		// Colorful Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0
		// \\lsdpriority72 \\lsdlocked0 Colorful List Accent 6;\\lsdsemihidden0
		// \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent
		// 6;}}{\\*\\generator WPS Office}{\\info {\\author
		// Administrator}{\\operator \\'c1\\'f5\\'d0\\'a1\\'b9\\'e2}{\\creatim
		// \\yr2020 \\mo8 \\dy12 \\hr8 \\min58 }{\\revtim \\yr2020 \\mo8 \\dy12
		// \\hr8 \\min59 }{\\version1 }{\\nofpages1 }}\\paperw12240
		// \\paperh15840 \\margl1800 \\margr1800 \\margt1440 \\margb1440
		// \\gutter0 \\deftab420 \\ftnbj \\aenddoc \\formshade \\dgmargin
		// \\dghspace180 \\dgvspace156 \\dghorigin1800 \\dgvorigin1440
		// \\dghshow0 \\dgvshow2 \\jcompress1 \\viewkind1 \\viewscale110
		// \\viewscale110 \\splytwnine \\ftnlytwnine \\htmautsp \\useltbaln
		// \\alntblind \\lytcalctblwd \\lyttblrtgr \\lnbrkrule \\nogrowautofit
		// \\nobrkwrptbl \\wrppunct {\\*\\fchars
		// !),.:;?]\\'7d\\'a1\\'a7\\'a1\\'a4\\'a1\\'a6\\'a1\\'a5\\'a8D\\'a1\\'ac\\'a1\\'af\\'a1\\'b1\\'a1\\'ad\\'a1\\'c3\\'a1\\'a2\\'a1\\'a3\\'a1\\'a8\\'a1\\'a9\\'a1\\'b5\\'a1\\'b7\\'a1\\'b9\\'a1\\'bb\\'a1\\'bf\\'a1\\'b3\\'a1\\'bd\\'a3\\'a1\\'a3\\'a2\\'a3\\'a7\\'a3\\'a9\\'a3\\'ac\\'a3\\'ae\\'a3\\'ba\\'a3\\'bb\\'a3\\'bf\\'a3\\'dd\\'a3\\'e0\\'a3\\'fc\\'a3\\'fd\\'a1\\'ab\\'a1\\'e9}{\\*\\lchars
		// ([\\'7b\\'a1\\'a4\\'a1\\'ae\\'a1\\'b0\\'a1\\'b4\\'a1\\'b6\\'a1\\'b8\\'a1\\'ba\\'a1\\'be\\'a1\\'b2\\'a1\\'bc\\'a3\\'a8\\'a3\\'ae\\'a3\\'db\\'a3\\'fb\\'a1\\'ea\\'a3\\'a4}\\fet2
		// {\\*\\ftnsep \\pard \\plain {\\insrsid \\chftnsep \\par
		// }}{\\*\\ftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par
		// }}{\\*\\aftnsep \\pard \\plain {\\insrsid \\chftnsep \\par
		// }}{\\*\\aftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par
		// }}\\sectd \\sbkpage \\pgwsxn11906 \\pghsxn16838 \\marglsxn1800
		// \\margrsxn1800 \\margtsxn1440 \\margbsxn1440 \\guttersxn0
		// \\headery851 \\footery992 \\pgbrdropt32 \\sectlinegrid312
		// \\sectspecifyl \\endnhere \\pard \\plain \\qj \\li0 \\ri0
		// \\nowidctlpar \\aspalpha \\aspnum \\adjustright \\lin0 \\rin0 \\itap0
		// \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\dbch \\af1 \\hich
		// \\af2 \\loch \\af2 \\lang1033 \\langnp1033 \\langfe2052
		// \\langfenp2052 {\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\loch
		// \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033 \\langfe2052
		// \\langfenp2052 Crtg}{\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch
		// \\loch \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033
		// \\langfe2052 \\langfenp2052 \\par }}";
		// new AlmController().conserveFile("11111",value);
		// List<Map<String, String>> s =
		// mks.searchALMIDTypeBySWID(Arrays.asList("entry_x04000000000850E3",
		// "entry_x04000000000850E1"));
	}
}
