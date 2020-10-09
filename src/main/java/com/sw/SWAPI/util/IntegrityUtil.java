package com.sw.SWAPI.util;

import static com.sw.SWAPI.util.ResultJson.ResultStr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.mks.api.Command;
import com.mks.api.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import sun.misc.BASE64Decoder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.controller.AlmController;


public class IntegrityUtil {

    public static final Log log = LogFactory.getLog(IntegrityUtil.class);

    private MKSCommand mks = new MKSCommand();

    private ConvertRTFToHtml rthToHTML = new ConvertRTFToHtml();

    public static final Properties SWBASICHolder = new Properties();
    private static InputStream is = null;
    public static String SW_TOKEN = null;
    public static String SW_HOST = null;
    public static String URL = null;
    private static CloseableHttpClient client = null;
    /**
     * 文件临时路径
     */
    private String filePath = "C:\\\\Program Files\\\\Integrity\\\\ILMServer12\\\\data\\\\tmp";

    @SuppressWarnings("deprecation")
    public JSONObject dealData(List<JSONObject> listData) throws APIException {
        log.info("开始实际导入数据");
        JSONObject jsonInfo = new JSONObject();
        String docId = null;
        try {
            List<JSONObject> contentsList = new ArrayList<>(listData.size());
            JSONObject docJSON = sortContainsAndGetDoc(listData, contentsList);
            log.info("条目数据排序完成");
            Map<String, JSONObject> SWJSONMap = new HashMap<String, JSONObject>(contentsList.size() * 4 / 3);
            Map<String, Boolean> SWDealMap = new HashMap<String, Boolean>(contentsList.size() * 4 / 3);
            Map<String, List<String>> SWMap = new HashMap<String, List<String>>(contentsList.size() * 4 / 3);// 记录SW_ID对应的ALMID
            for (JSONObject obj : contentsList) {
                String swSid = obj.getString(Constants.SW_SID_FIELD);
                SWJSONMap.put(swSid, obj);// 通过Map将每个JSON记录
                SWDealMap.put(swSid, false);// 通过Map记录SWSID对应数据是否处理。防止排序失败导致数据处理异常
            }
            // mks.initMksCommand(host, port, loginName, passWord);
            boolean newDoc = false; // 判断是否是新增文档
            boolean branch = false;// 判断是否是复用文档
            Map<String, String> SWSIDMap = new HashMap<>();// 存放已保存ID
            String action_Type = docJSON.getString("action_Type");
            String project = docJSON.getString("Project");// 创建到的目标项目，通过分别查询
            String doc_SW_SID = docJSON.getString("SW_SID");
            String docIssueId = docJSON.getString("issue_id");
            String doc_SW_ID = docJSON.getString("SW_ID");
            String issue_Type = AnalysisXML.getAlmType(docJSON.getString("issue_Type"));
            String curState = null;//原始文档状态，判断Component Requirement Specification Document在Published时 也要能修改数据，同时触发钉钉通知
            String targetState = AnalysisXML.getTypeTargetState(issue_Type);
            String synCount = null;
            long startDoc = System.currentTimeMillis();
            jsonInfo.put("Project", project);
            jsonInfo.put("Action", "CreationFail");
            jsonInfo.put("D_Issue_ID", docIssueId);//SW_SID
            jsonInfo.put("D_Item_ID", doc_SW_ID);//SW_ID
            if ("add".equals(action_Type)) {//
                List<Map<String, String>> docList = null;
                docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, null);
                long endDocQuery = System.currentTimeMillis();
                if (docList == null || docList.isEmpty() || docList.size() == 0) {
                    docId = createDoc(docJSON, SWSIDMap, mks);// 当前未创建，创建新的文档
                    long endDocCreate = System.currentTimeMillis();
                    log.info("创建文档" + docList.size() + "|| 花费时间：" + (endDocCreate - endDocQuery));
                } else {
                    branch = true;
                    Map<String, String> origInfo = null;
                    for (Map<String, String> docInfo : docList) {
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
                        long endDocCreate = System.currentTimeMillis();
                        log.info("创建文档分支" + docList.size() + "|| 花费时间：" + (endDocCreate - endDocQuery));
                    } catch (APIException e) {
                        docJSON = null;
                        docId = null;
                        action_Type = null;
                        log.error("208 - 创建分支错误! " + APIExceptionUtil.getMsg(e));
                        return jsonInfo;
                    }
                }
                newDoc = true;
            } else if ("update".equals(action_Type)) {
                doc_SW_SID = docJSON.getString("Old_SW_SID");
                List<Map<String, String>> docList = null;
                long endDocQuery = 0;
                docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
                endDocQuery = System.currentTimeMillis();
                log.error("查询数据花费时间" + (endDocQuery - startDoc));

                Map<String, String> docInfo = docList.get(0);
                curState = docInfo.get("State");
                // Component Requirement Specification Document判断时，如果已经处于Published状态，则允许更新
                if (curState.equals(targetState) && Constants.DOC_PUBLISHED_STATE.equals(curState)) {
                    synCount = docInfo.get("SWR Synchronize Count");
                }
                docId = docInfo.get("ID");
                updateDoc(docInfo, docJSON, false, true);
                long endDocUpdate = System.currentTimeMillis();
                log.info("创建文档" + docList.size() + "|| 花费时间：" + (endDocUpdate - endDocQuery));
            }
            /** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理 */
            List<String> branchDeleIssueList = null;// 记录复用文档后，需要移除的数据
            if (branch) {
                try {
                    long branchStart = System.currentTimeMillis();
                    branchDeleIssueList = mks.getDocContents(docId, SWSIDMap);
                    long branchEnd = System.currentTimeMillis();
                    log.info("分支查询数据为： " + Arrays.asList(branchDeleIssueList));
                    log.info("分支条目查询|| 花费时间：" + (branchEnd - branchStart));
                } catch (APIException e1) {
                    SWSIDMap = null;
                    contentsList = null;
                    docJSON = null;
                    SWJSONMap = null;
                    SWDealMap = null;
                    SWMap = null;
                    docId = null;
                    log.error("209 - 查询分支文档数据失败! " + APIExceptionUtil.getMsg(e1));
                    return jsonInfo;
                }
            }
            /** 如果是分支创建，文档分支创建成功后，需要将SW_SID-ALMID查询出来，进行处理 */
            /** 处理数据1 */
            long contentStart = System.currentTimeMillis();
            for (int i = 0; i < contentsList.size(); i++) {
                try {
                    dealContentJson(contentsList.get(i), SWSIDMap, SWMap, SWDealMap, SWJSONMap, docId, branch, newDoc,
                            branchDeleIssueList);
                } catch (MsgArgumentException e) {
                    log.error("清理缓存！");
                    return jsonInfo;
                } catch (APIException e) {
                    log.error("处理数据失败 201 - 处理数据失败! " + APIExceptionUtil.getMsg(e));
                    return jsonInfo;
                }
            }
            long contentEnd = System.currentTimeMillis();
            log.info(" 条目处理总花费  花费时间：" + (contentEnd - contentStart));
            /** 处理追溯3 */
            for (int i = 0; i < contentsList.size(); i++) {
                JSONObject contentObj = contentsList.get(i);
                try {
                    dealRelationship(contentObj, SWSIDMap, SWMap);
                    contentObj = null;// 处理完成，将此数据设置null
                } catch (APIException e) {
                    contentObj = null;// 处理完成，将此数据设置null
                    return jsonInfo;
                }
            }
            long tranceEnd = System.currentTimeMillis();
            log.info(" 追溯处理总花费  花费时间：" + (tranceEnd - contentEnd));
            /** 如果分支创建完成，需要删除结构的，进行结构删除。 */
            if (branchDeleIssueList != null) {
                for (String issueId : branchDeleIssueList) {
                    try {
                        mks.removecontent(issueId);
                        mks.deleteissue(issueId);
                    } catch (APIException e) {
                        SWSIDMap = null;
                        contentsList = null;
                        docJSON = null;
                        SWJSONMap = null;
                        SWDealMap = null;
                        SWMap = null;
                        docId = null;
                        log.error("210 - 删除条目失败! " + APIExceptionUtil.getMsg(e));
                        return jsonInfo;
                    }
                }
            }
            long deleteEnd = System.currentTimeMillis();
            log.info(" 删除多余条目处理总花费  花费时间：" + (deleteEnd - tranceEnd));
            /** 如果分支创建完成，需要删除结构的，进行结构删除。 */
            SWDealMap = null;// 置空，回收对象
            SWJSONMap = null;// 置空，回收对象
            SWSIDMap = null;// 置空，回收对象
            contentsList = null;// 置空，回收对象
            // 新增后修改状态为评审 in approve
            log.info("数据下发完成 开始修改状态 Doc_id : " + docId);
            String assignedUser = docJSON.getString("Assigned_User");
            try {
                Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
                log.info("curState: " + curState + "||targetState:" + targetState + "||synCount:" + synCount);
                if (curState != null && curState.equals(targetState) && targetState.equals(Constants.DOC_PUBLISHED_STATE)) {
                    //当前状态等于目标状态 且目标状态为Published时，允许更新
                    Integer synCountInt = synCount != null ? Integer.valueOf(synCount) : 0;
                    synCountInt++;
                    dataMap.put("SWR Synchronize Count", String.valueOf(synCountInt));
                } else {
                    dataMap.put("State", targetState);
                }
                dataMap.put("Assigned User", assignedUser);
                mks.editIssue(docId, dataMap, new HashMap<String, String>());
            } catch (APIException e) {
                log.error("211 - 修改文档状态失败! " + APIExceptionUtil.getMsg(e));
                return jsonInfo;
            }
            long editEnd = System.currentTimeMillis();
            log.info(" 变更文档状态  花费时间：" + (editEnd - deleteEnd));
            try {
                String label = "Autobaseline:from SWR :" + doc_SW_ID;
                log.info("基线标题===" + label + "|基线文档id===" + docId);
                mks.createBaseLine(label, docId);// 自动创建基线信息
                long labelEnd = System.currentTimeMillis();
                log.info(" 自动添加文档基线  花费时间：" + (labelEnd - editEnd));
            } catch (APIException e) {
                // TODO Auto-generated catch block
                SWSIDMap = null;
                contentsList = null;
                docJSON = null;
                SWJSONMap = null;
                SWDealMap = null;
                SWMap = null;
                docId = null;
                log.error("210 - 文档基线创建失败! " + APIExceptionUtil.getMsg(e));
                return jsonInfo;
            }
        } catch (Exception e) {
            log.info("数据处理错误");
            return jsonInfo;
        }
        jsonInfo.put("Action", "CreationPass");
        jsonInfo.put("Doc_ID", docId);
        return jsonInfo;
    }

    public String checkData(List<JSONObject> listData) {
        List<JSONObject> contentsList = new ArrayList<>(listData.size());
        JSONObject docJSON = sortContainsAndGetDoc(listData, contentsList);

        String action_Type = docJSON.getString("action_Type");
        String project = docJSON.getString("Project");
        String Document_Short_Title = docJSON.getString("item_name");
        String doc_SW_SID = docJSON.getString("SW_SID");
        String issue_Type = AnalysisXML.getAlmType(docJSON.getString("issue_Type"));
        String targetState = AnalysisXML.getTypeTargetState(issue_Type);
        List<Map<String, String>> docList = null;
        String verificationDoc = verification(docJSON);
        if (!"success".equals(verificationDoc)) {
            return verificationDoc;
        }
        if ("add".equals(action_Type)) {
            try {
                docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, null);
                if (docList != null && docList.size() > 0) {
                    long count = docList.stream().filter(info -> project.equals(info.get("Project"))).count();
                    if (count > 0) {
                        return "206 - Document has created in project: [" + project + "]!";
                    }
                }
                //验证名称重复
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Project", project);
                jsonObject.put("Type", issue_Type);
                jsonObject.put("Document Short Title", Document_Short_Title);
                String infoName = mks.checkIdAndName(jsonObject);
                if (!"success".equals(infoName)) {
                    return infoName;
                }
            } catch (Exception e) {
                return "207 - 根据SW_SID查询错误，请联系管理员!";
            }
        }
        if ("update".equals(action_Type)) {
            try {
                docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);

                if (docList == null || docList.isEmpty()) {
                    return "204 - Document hadn't create。Please check you action type!";
                } else {
                    String curState = docList.get(0).get("State");
                    log.info("更新状态判断：curState = " + curState + "||targetState = " + targetState);
                    if (Constants.DOC_PUBLISHED_STATE.equals(curState)) {
                        log.info("Published更新判断 " + !curState.equals(targetState));
                        if (!curState.equals(targetState)) {
                            // 如果目标状态为Published，那么 当前状态与目标状态都一致时，允许更新
                            return "205 - Document now is in reivew, can not update!";
                        }
                    } else if (!Constants.DOC_INIT_STATE.equals(curState)) {
                        return "205 - Document now is in reivew or published, can not update!";
                    }

                }
            } catch (APIException e) {
                return "207 - 根据SW_SID查询错误，请联系管理员!";
            }
        }

        //验证条目
        for (JSONObject issueJSON : contentsList) {
            String verificationIssue = verification(issueJSON);
            if (!"success".equals(verificationIssue)) {
                return verificationIssue;
            }
            String issue_action_Type = issueJSON.getString("action_Type");
            String SW_SID = issueJSON.getString("SW_SID");
            String Old_SW_SID = issueJSON.getString("Old_SW_SID");
            String issueProject = issueJSON.getString("Project");
            String issueType = issue_Type.substring(0, issue_Type.lastIndexOf(" "));
            if ("add".equals(issue_action_Type)) {
                Map<String, String> issueMap =
                        mks.searchOrigIssue(Arrays.asList("ID", "Document ID", "SW_SID", "Project"),
                                SW_SID, issueType, issueProject);
                if (issueMap != null) {
                    return "已经存在的条目id： " + SW_SID + "---alm_ID:" + issueMap.get("ID");
                }
            }
            if ("update".equals(issue_action_Type)) {
                String id = mks.getIssueBySWID("SW_SID", Old_SW_SID, issueProject, issueType, "ID");
                if (id == null || "".equals(id)) {
                    return "通过SW_SID查询不到需要update的ALM数据: " + Old_SW_SID;
                }
            }
            if ("delete".equals(issue_action_Type)) {
                String id = mks.getIssueBySWID("SW_SID", SW_SID, issueProject, issueType, "ID");
                if (id == null || "".equals(id)) {
                    return "通过SW_SID查询不到需要delete的ALM数据: " + SW_SID;
                }
            }
            if ("move".equals(issue_action_Type)) {
                if (Old_SW_SID != null && !"".equals(Old_SW_SID)) {
                    String id = mks.getIssueBySWID("SW_SID", SW_SID, issueProject, issueType, "ID");
                    if (id == null || "".equals(id)) {
                        return "通过SW_SID查询不到需要move的ALM数据: " + SW_SID;
                    }
                } else {
                    return "OLD SW_SID为空" + Old_SW_SID;
                }
            }
        }
        return "success";
    }

    public String verification(JSONObject jsonObject) {
        if (jsonObject.get("Project") == null) {
            System.out.println("Project不能为null " + jsonObject.get("SW_SID"));
            return "Project不能为null" + jsonObject.get("SW_SID");
        }
        if (jsonObject.get("SW_SID") == null) {
            System.out.println("SW_SID不能为null " + jsonObject.get("SW_SID"));
            return "SW_SID不能为null" + jsonObject.get("SW_SID");
        }
        if (jsonObject.get("issue_Type") == null) {
            System.out.println("issue_Type不能为null " + jsonObject.get("SW_SID"));
            return "issue_Type不能为null" + jsonObject.get("SW_SID");
        }
        if (jsonObject.get("SW_ID") == null) {
            System.out.println("SW_ID不能为null " + jsonObject.get("SW_SID"));
            return "SW_ID不能为null" + jsonObject.get("SW_SID");
        }
        if (jsonObject.get("issue_id") == null) {
            System.out.println("issue_id不能为null " + jsonObject.get("SW_SID"));
            return "issue_id不能为null" + jsonObject.get("SW_SID");
        }
        if (jsonObject.get("item_name") == null) {
            System.out.println("item_name不能为null " + jsonObject.get("SW_SID"));
            return "item_name不能为null" + jsonObject.get("SW_SID");
        }
        return "success";
    }

    public void executionSychSW(JSONObject data) {
        loadSWConfig();
        data.put("Access_Token", SW_TOKEN);
        log.info("链接地址：" + SW_HOST + "//" + URL);
        HttpPost httpPost = new HttpPost(SW_HOST + "//" + URL);
        if (client == null) {
            try {
                getConnection();
            } catch (Exception e) {
                log.error("获取链接失败：" + e);
            }
        }
        log.info("data:" + data.toJSONString());
        setDataToEntity(data.toString(), httpPost);
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            log.info("反馈数据状态：" + response.getStatusLine().getStatusCode());
            BasicResponseHandler hander = new BasicResponseHandler();
            log.info("反馈数据信息：" + hander.handleResponse(response));
        } catch (Exception e) {
            log.error("数据发送失败：" + e);
        }
        log.info("数据发送成功");
    }

    public void setDataToEntity(String data, HttpEntityEnclosingRequestBase http) {
        http.setHeader("Authorization", "Access_Token=" + SW_TOKEN);
        http.setHeader("Access_Token", SW_TOKEN);
        http.setHeader("Content-type", "application/json");
        StringEntity entity = new StringEntity(data, "UTF-8");
        entity.setContentType("application/json");
        http.setEntity(entity);
    }

    public static CloseableHttpClient getConnection() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        // 配置同时支持 HTTP 和 HTPPS
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
        // 初始化连接管理器
        PoolingHttpClientConnectionManager poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 将最大连接数增加到200，实际项目最好从配置文件中读取这个值
        poolConnManager.setMaxTotal(200);
        // 设置最大路由
        poolConnManager.setDefaultMaxPerRoute(2);
        // 根据默认超时限制初始化requestConfig
        int socketTimeout = 90000;
        int connectTimeout = 90000;
        int connectionRequestTimeout = 90000;
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
        // 初始化httpClient
//		this.url = LoadPropertyPlaceholderConfigurer.getContextProperty("atlas.url") ;
        client = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(poolConnManager)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
        if (poolConnManager != null && poolConnManager.getTotalStats() != null) {
            log.info("Get jira client pool " + poolConnManager.getTotalStats().toString());
        }
        return client;
    }

    private void loadSWConfig() {
        try {
            if (is == null) {
                InputStream is = IntegrityUtil.class.getClassLoader().getSystemResourceAsStream("sw.properties");
                SWBASICHolder.load(is);
                SW_TOKEN = SWBASICHolder.getProperty("SW_TOKEN");
                SW_HOST = SWBASICHolder.getProperty("SW_HOST");
                URL = SWBASICHolder.getProperty("SW_URL");
                log.info("加载配置SWR文件");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 处理条目数据
     *
     * @param contentObj
     * @param SWIDMap             // 存放 SWSID - ALMID
     * @param SWMap
     * @param SWDealMap
     * @param SWJSONMap
     * @param docId
     * @param
     * @param branch
     * @param newDoc
     * @param branchDeleIssueList
     */
    private void dealContentJson(JSONObject contentObj, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
                                 Map<String, Boolean> SWDealMap, Map<String, JSONObject> SWJSONMap, String docId,
                                 boolean branch, boolean newDoc, List<String> branchDeleIssueList) throws MsgArgumentException, APIException {
        String SWSID = contentObj.getString(Constants.SW_SID_FIELD);// 当前JSON的ID
        if (SWDealMap.get(SWSID)) {
            log.info("SWSID - " + SWSID + " == 数据已经处理过，跳过本次循环");
            return;
        }
        String beforeId = contentObj.getString("Before_ID");
//		log.info("beforeId - " + beforeId + " ");
        if (null != beforeId && !"".equals(beforeId)) {// BeforeID存在时，防止Before未处理。获取Before判断并处理
//			log.info("beforeId - 处理 " + SWDealMap.get(beforeId) + " ");
            if (SWDealMap.get(beforeId) != null && !SWDealMap.get(beforeId)) {
//				log.info("beforeId - " + beforeId + " == 数据未处理，优先执行Before ID处理");
                dealContentJson(SWJSONMap.get(beforeId), SWIDMap, SWMap, SWDealMap, SWJSONMap, docId, branch,
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
                MoveDoc(contentObj, SWIDMap, SWMap, docId, false, true, null);
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

    // 条目移动
    public String MoveDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
                          String docId, boolean docChange, boolean dealDoc, List<String> changeList) throws MsgArgumentException, APIException {
        String oldSWSID = jsonData.getString("Old_SW_SID");// 旧ID
        if (oldSWSID != null && !"".equals(oldSWSID)) {// OLD SW_SID不为空时，才需要移动
            String project = jsonData.getString("Project");// 文档id
            String docType = jsonData.getString("issue_Type");
            String uuid = jsonData.getString("DOC_UUID");
            String issueType = docType.substring(0, docType.lastIndexOf(" "));
            String id = SWIDMap.get(oldSWSID);// 复用时，可以通过SW_SID - ALM ID
            // Map获取到数据
            long beginDeal = System.currentTimeMillis();
            if (id == null) {// 获取不到，使用查询
                id = mks.getIssueBySWID("SW_SID", jsonData.getString("Old_SW_SID"), project, issueType, "ID");
            }
            log.info("需要移动的sw_sid -" + jsonData.getString("Old_SW_SID") + ";id:" + id);
            // 获取文档id
            String Parent_ID = jsonData.getString("Parent_ID");
            String alm_parent_ID = "";
//			log.info("Parent_ID>>>SW_SID-------" + Parent_ID);
            if ("".equals(Parent_ID) || "null".equals(Parent_ID)) {
//				log.info("父级为文档id-------" + docId);
                alm_parent_ID = docId;
            } else {
                if (dealDoc)
                    alm_parent_ID = SWIDMap.get(Parent_ID);
                else
                    alm_parent_ID = MapCache.getCacheALMID(uuid, Parent_ID);
//				log.info("map中查询已经创建的父id-------" + alm_parent_ID);
                log.info("同批次数据-------" + alm_parent_ID);
                if (alm_parent_ID == null || "".equals(alm_parent_ID) || "null".equals(alm_parent_ID)) {
                    alm_parent_ID = mks.getIssueBySWID("SW_SID", Parent_ID, project, issueType, "ID");
                    if (dealDoc && SWIDMap != null) {
                        SWIDMap.put(Parent_ID, alm_parent_ID);
                    } else {
                        MapCache.cacheSWSID(uuid, Parent_ID, alm_parent_ID);
                    }
//					log.info("Parent_ID-------" + Parent_ID);
//					log.info("alm_parent_ID-------" + alm_parent_ID);
                }
            }
            long searPar = System.currentTimeMillis();
            log.info("移动条目，查询父节点----花费：" + (searPar - beginDeal));
            String insertLocation = "";
            String Before_ID = jsonData.getString("Before_ID");// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
//			log.info("需要移动到前面的id -" + jsonData.getString("Before_ID") + ";id:" + Before_ID);
            if (!"".equals(Before_ID)) {
                String alm_bef_ID = null;
                if (dealDoc)
                    alm_bef_ID = SWIDMap.get(Before_ID);
                else
                    alm_bef_ID = MapCache.getCacheALMID(uuid, Parent_ID);
                if (alm_bef_ID == null) {// 当从Map中获取不到时，查询
                    alm_bef_ID = mks.getIssueBySWID("SW_SID", Before_ID, project, issueType, "ID");
                    if (dealDoc && SWIDMap != null) {
                        SWIDMap.put(Before_ID, alm_bef_ID);
                    } else {
                        MapCache.cacheSWSID(uuid, Before_ID, alm_bef_ID);
                    }
                }
//				log.info("插入位置：id -" + Before_ID + ";ALM - id:" + alm_bef_ID);
                insertLocation = "after:" + alm_bef_ID;
            } else {
                insertLocation = "first";
            }
            long searBefo = System.currentTimeMillis();
            log.info("移动条目，查询前节点----花费：" + (searBefo - searPar));
            try {
//				log.info("需要移动的aml_id(" + id + "),移动的aml_parentID(" + alm_parent_ID + ")：移动的具体位置id(" + insertLocation
//						+ ")");
                mks.movecontent(alm_parent_ID, insertLocation, id);
                long endMove = System.currentTimeMillis();
                log.info("将id： (" + id + ")移动到 -" + alm_parent_ID + " 移动条目，查询前节点----花费： " + (endMove - searBefo));
            } catch (APIException e) {
                log.error("error: " + "移动条目出错！" + APIExceptionUtil.getMsg(e));
                log.error("清理缓存！");
                throw new MsgArgumentException("201", "移动条目出错 " + APIExceptionUtil.getMsg(e));
            }
            long endDeal = System.currentTimeMillis();
            log.info("移动条目总----花费：" + (endDeal - beginDeal));
        }
        return UpDoc(jsonData, SWIDMap, SWMap, docId, docChange, dealDoc, changeList);
    }

    /**
     * 更新文档SW_ID、SW_SID
     *
     * @param jsonData
     * @param
     */
    public void updateDoc(Map<String, String> docInfo, JSONObject jsonData, boolean docChange, boolean dealDoc) {
//		log.info("-------------修改文档-----------");
        String SW_SID = jsonData.getString("SW_SID");
        String docId = docInfo.get("ID");
        String issue_Type = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
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
                    if (dealDoc || docChange)
                        docdataMap.put(docmap.get(key), strKey);
                }
            }
        }
        try {
            mks.editIssue(docId, docdataMap, null);
            JSONArray attachList = jsonData.getJSONArray("Attachments");
            log.info("处理附件开始 附件大小：" + attachList.size());
            if (attachList != null && !attachList.isEmpty()) {
                for (int i = 0; i < attachList.size(); i++) {
                    JSONObject att = attachList.getJSONObject(i);
                    log.info("处理第 " + i + " 个附件");
                    uploadAttachments(att, docId);
                }
            }
            log.info("更新文档成功：" + docId + " SW_SID=" + SW_SID);
        } catch (APIException e) {
            log.error("error: " + "更新文档出错！" + APIExceptionUtil.getMsg(e));
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
     * @param
     * @return
     */
    @SuppressWarnings("unused")
    public String AddEntry(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap,
                           String docId, boolean docChange, boolean dealDoc) throws MsgArgumentException, APIException {
        log.info("-------------新增条目-----------");
        verification(jsonData);
        String docType = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
        String SW_SID = jsonData.getString("SW_SID");
        String SW_ID = jsonData.getString("SW_ID");
        String project = jsonData.getString("Project");
        String uuid = jsonData.getString("DOC_UUID");
//		log.info("创建SW_SID : " + SW_SID);
        String issueType = docType.substring(0, docType.lastIndexOf(" "));
        /** 设置位置信息 */
        String Parent_ID = jsonData.getString("Parent_ID");
        String alm_parent_ID = "";
        String dataTime = null;
//		log.info("Parent_ID>>>SW_SID-------" + Parent_ID);
        long beginDeal = System.currentTimeMillis();
        if ("".equals(Parent_ID) || "null".equals(Parent_ID)) {
//			log.info("父级为文档id-------" + docId);
            alm_parent_ID = docId;
        } else {
            if (dealDoc)
                alm_parent_ID = SWIDMap.get(Parent_ID);
            else {
                alm_parent_ID = MapCache.getCacheALMID(uuid, Parent_ID);
            }
            log.info("map中查询已经创建的父id-------" + alm_parent_ID);
//			log.info("同批次数据-------" + alm_parent_ID);
            if (alm_parent_ID == null || "".equals(alm_parent_ID) || "null".equals(alm_parent_ID)) {
                alm_parent_ID = mks.getIssueBySWID("SW_SID", Parent_ID, project, issueType, "ID");
                if (dealDoc && SWIDMap != null) {
                    SWIDMap.put(Parent_ID, alm_parent_ID);
                } else {
                    MapCache.cacheSWSID(uuid, Parent_ID, alm_parent_ID);
                }
                log.info("查询alm_parent_ID-------" + alm_parent_ID);
            }
        }
        long searPar = System.currentTimeMillis();
        log.info("查询父节点----花费：" + (searPar - beginDeal));
        String insertLocation = "";
        String Before_ID = jsonData.getString("Before_ID");// SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
//		log.info("插入位置：Before_ID -" + Before_ID);
        if (!"".equals(Before_ID)) {
            if (SWIDMap != null) {// SWID_ID MAP保存的有SW_SID <>
                // ALM_ID对应关系，直接从Map获取
                String alm_bef_ID = null;
                if (SWIDMap != null) {// SWID_ID MAP保存的有SW_SID <> ALM_ID对应关系，直接从Map获取
                    alm_bef_ID = SWIDMap.get(Before_ID);
                } else
                    alm_bef_ID = MapCache.getCacheALMID(uuid, Before_ID);
                if (alm_bef_ID == null) {// 当从Map获取不到时，查询
                    alm_bef_ID = mks.getIssueBySWID("SW_SID", Before_ID, project, issueType, "ID");
                    if (dealDoc && SWIDMap != null) {
                        SWIDMap.put(Before_ID, alm_bef_ID);
                    } else {
                        MapCache.cacheSWSID(uuid, Before_ID, alm_bef_ID);
                    }
                }
//				log.info("插入位置：id -" + Before_ID + ";ALM - id:" + alm_bef_ID);
                insertLocation = "after:" + alm_bef_ID;
            }
        } else {
            insertLocation = "first";
        }
        long searBef = System.currentTimeMillis();
        log.info("查询前节点----花费：" + (searBef - searPar));
        /** 设置位置信息 */
        // 创建文档条目
        // xml配置字段
        // 先判断是否创建过
        String issueId = null;
        Map<String, String> issueMap = mks.searchOrigIssue(Arrays.asList("ID", "Document ID", "SW_SID", "Project"),
                SW_SID, issueType, project);
        long searThis = System.currentTimeMillis();
        log.info("查询此节点是否创建----花费：" + (searThis - searBef));
        if (issueMap != null) {
            /** 不为空时，判断是否处于当前文档下：1. 如果处于当前文档 ,则直接更新 ；2. 如果不处于当前文档，则copy到当前文档 */
            // 1 判断是否处于当前文档
            String issueDocId = issueMap.get("Document ID");
            issueId = issueMap.get("ID");
            if (issueDocId.equals(docId)) {
                UpDoc(jsonData, SWIDMap, SWMap, docId, docChange, dealDoc, null);
            } else {// 2 未处于当前文档下
                try {
                    issueId = mks.copyContent(alm_parent_ID, insertLocation, issueMap.get("ID"));
                    if (SWIDMap != null)
                        SWIDMap.put(SW_SID, issueId);
                    UpDoc(jsonData, SWIDMap, SWMap, docId, docChange, dealDoc, null);
                } catch (APIException e) {
                    log.error(SW_SID + "复用失败，所属文档 " + issueDocId + "失败原因：" + APIExceptionUtil.getMsg(e));
                    e.printStackTrace();
                }
            }

            log.error("已经存在的条目id： " + SW_SID + "---alm_ID:" + issueId);
        } else {
            Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
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
            List<Attachment> listAtt = null;
            if (Text1 != null && !"".equals(Text1)) {
                try {
                    dataTime = String.valueOf(new Date().getTime());
                    listAtt = new ArrayList<>();
                    dataMap.put("Text", rtfString(dataTime, Text1, listAtt));
                    log.info(dataTime + " 附件有:" + Arrays.asList(listAtt));
                } catch (Exception e) {
                    log.info("富文本转换失败:" + e.getMessage());
                    /** 2释放word处理进程 并删除临时文件*/
                    deleteTmpFile(dataTime);
                    /** 2释放word处理进程 并删除临时文件 */
                }
            }
            long dealText = System.currentTimeMillis();
            log.info("处理富文本----花费：" + (dealText - searThis));
            try {
                issueId = mks.createContent(alm_parent_ID, insertLocation, dataMap, entryType1);
                log.info("创建的条目id： " + issueId);
                if (SWIDMap != null)
                    SWIDMap.put(SW_SID, issueId);
                // 获取文档id
                long create = System.currentTimeMillis();
                log.info("创建条目----花费：" + (create - dealText));
                try {
                    JSONArray attachList = jsonData.getJSONArray("Attachments");
                    log.info("处理附件开始 附件大小：" + attachList.size());
                    if (attachList != null && !attachList.isEmpty()) {
                        for (int i = 0; i < attachList.size(); i++) {
                            JSONObject att = attachList.getJSONObject(i);
                            log.info("处理第 " + i + " 个附件");
                            uploadAttachments(att, issueId);
                        }
                    }

                    /** 处理富文本所带图片*/
                    if (listAtt != null && !listAtt.isEmpty()) {
                        for (Attachment att : listAtt) {
                            mks.addAttachment(issueId, att, Constants.TEXT_ATTACHMENT);
                        }
                    }
                    long upload = System.currentTimeMillis();
                    log.info("上传附件----花费：" + (upload - create));
                } catch (APIException e) {
                    log.error("附件处理失败:" + APIExceptionUtil.getMsg(e));
                }
                if (dataTime != null) {
                    deleteTmpFile(dataTime);
                }
            } catch (APIException e) {
                /** 2释放word处理进程 并删除临时文件*/
                deleteTmpFile(dataTime);
                /** 2释放word处理进程 并删除临时文件 */
                log.error("error: " + "创建文档条目出错！" + APIExceptionUtil.getMsg(e));
                throw new MsgArgumentException("201", "创建文档条目出错 " + APIExceptionUtil.getMsg(e));
            }
        }
        if (dealDoc) {
            if (SWMap != null) {
                List<String> almList = SWMap.get(SW_ID);
                if (almList == null) {
                    almList = new ArrayList<>();
                    SWMap.put(SW_ID, almList);
                }
                almList.add(issueId);
            }
        } else {
            MapCache.cacheSWID(uuid, SW_ID, issueId);
            MapCache.cacheSWSID(uuid, SW_SID, issueId);
            log.info("Change Cache : " + uuid + " | " + SW_ID + " | " + issueId);
            log.info("SWSID_MAP : " + JSONObject.toJSONString(MapCache.getSWSIDMap(uuid)));
        }
        long endDeal = System.currentTimeMillis();
        log.info("创建条目----花费：" + (endDeal - beginDeal));
        return issueId;
    }

    // 条目删除
    public String DelDoc(JSONObject jsonData, String docId, boolean docChange, boolean dealDoc) throws MsgArgumentException {
//		log.info("-------------删除文档-----------");
        String SW_SID = jsonData.getString("SW_SID");// 文档id
        String project = jsonData.getString("Project");// 文档id
        String id = mks.getIssueBySWID("SW_SID", SW_SID, project, null, "ID");
        log.info("需要删除的sid: " + SW_SID + ",id : " + id);
        long beginDeal = System.currentTimeMillis();
        try {
            mks.removecontent(id);
        } catch (APIException e) {
            log.error("error: " + "删除条目关系出错！" + e.getMessage());
            log.error("清理缓存！");
            throw new MsgArgumentException("201", "删除条目关系出错 " + APIExceptionUtil.getMsg(e));
        }
        try {
            mks.deleteissue(id);
        } catch (APIException e) {
            log.error("error: " + "删除条目出错！");
            throw new MsgArgumentException("201", "删除条目出错 " + APIExceptionUtil.getMsg(e));
        }
        long endDeal = System.currentTimeMillis();
        log.info("删除条目----花费：" + (endDeal - beginDeal));
        return id;
    }

    /**
     * 处理富文本信息
     *
     * @param text
     * @return
     */
    public String rtfString(String dataTime, String text, List<Attachment> listAtt) {
        String str = filePath + "\\" + dataTime + ".rtf";
        String str1 = filePath + "\\" + dataTime;
        String imgFolderPath = str1 + ".files";

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
        long beginTime = System.currentTimeMillis();
        conserveFile(str, text);
        long endTime = System.currentTimeMillis();
        log.info("下载富文本到服务器 - 花费：" + (endTime - beginTime));
        rthToHTML.RTFToHtml(str, str1);// 本地rtf文件转换为html
        long endHtm = System.currentTimeMillis();
        log.info("rft转换为html - 花费：" + (endHtm - endTime));
        String htmldata = null;// 获取html中元素
        try {
            htmldata = rthToHTML.readHtml(str1 + ".htm");
            File imgFolder = new File(imgFolderPath);
            File[] listFiles = imgFolder.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    String absolutePath = file.getAbsolutePath();
                    String name = file.getName();
                    Attachment att = new Attachment();
                    att.setName(name);
                    att.setPath(absolutePath);
                    listAtt.add(att);
                }
            }
            long endRead = System.currentTimeMillis();
            log.info("本地转换图片和转换富文本 图片连接 - 花费：" + (endRead - endHtm));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmldata;
    }

    // 文档新增
    public String createDoc(JSONObject jsonData, Map<String, String> SWIDMap, MKSCommand mks) {
        log.info("-------------新增文档-----------");
        verification(jsonData);
        String issue_Type = jsonData.getString("issue_Type");// 创建文档类型或创建条目类型(创建时必须)
        String SW_SID = jsonData.getString("SW_SID");
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

        try {
            // docdataMap.put("Assigned User", "admin");
//			log.info("issue_id ====== :" + jsonData.getString("issue_id"));
            String doc_Id = mks.createDocument(docType, docdataMap, null);
            JSONArray attachList = jsonData.getJSONArray("Attachments");
            log.info("处理附件开始 附件大小：" + attachList.size());
            if (attachList != null && !attachList.isEmpty()) {
                for (int i = 0; i < attachList.size(); i++) {
                    JSONObject att = attachList.getJSONObject(i);
                    log.info("处理第 " + i + " 个附件");
                    uploadAttachments(att, doc_Id);
                }
            }
            SWIDMap.put(SW_SID, doc_Id);
            // swid_id.put("doc_" + SW_SID, doc_Id);
            log.info("创建的文档id： " + doc_Id);
            return doc_Id;
        } catch (APIException e) {
            log.error("error: " + "创建文档出错！" + APIExceptionUtil.getMsg(e));
            throw new MsgArgumentException("201", "创建文档出错 " + APIExceptionUtil.getMsg(e));
        }
    }

    // 文档条目修改
    public String UpDoc(JSONObject jsonData, Map<String, String> SWIDMap, Map<String, List<String>> SWMap, String docId
            , boolean docChange, boolean dealDoc, List<String> changeList) throws MsgArgumentException, APIException {
        log.info("-------------修改条目-----------");
        String doc_Type = jsonData.getString("issue_Type");
        String issue_Type = doc_Type.substring(0, doc_Type.lastIndexOf(" "));
        Map<String, String> dataMap = new HashMap<String, String>();// 普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();// 富文本字段
        String Old_SW_SID = jsonData.getString("Old_SW_SID");// 原SW_SID
        String SW_SID = jsonData.getString("SW_SID");// 新SW_SID
        if (Old_SW_SID == null || "".equals(Old_SW_SID)) {// 只有Branch时，有可能不传OLD_SW_SID
            Old_SW_SID = SW_SID;// 使用新的SW_SID查询数据
        }
        String SW_ID = jsonData.getString("SW_ID");// SW_ID
        String project = jsonData.getString("Project");// 文档id
        String uuid = jsonData.getString("DOC_UUID");// 文档id
//		log.info("修改的sw_sid----------" + Old_SW_SID);
        // 当通过分支创建时，会直接将SW_SID-ALMID存放入MAP，可以直接获取进行更新
        long beginDeal = System.currentTimeMillis();
        String id = null;
        String dataTime = null;
        if (SWIDMap != null)
            id = SWIDMap.get(Old_SW_SID);
        if (id == null)
            id = mks.getIssueBySWID("SW_SID", Old_SW_SID, project, issue_Type, "ID");
        // rtf
        if (id == null || "".equals(id)) {
            log.info("通过SW_SID查询不到对应的ALM数据: " + Old_SW_SID);
        }
        long upQuery = System.currentTimeMillis();
        log.info("更新条目查询----花费：" + (upQuery - beginDeal));
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
                    if (docChange || dealDoc || (changeList != null && changeList.contains(id)))//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
                        dataMap.put(map.get(key), strKey);
                }
            }
        }
        List<Attachment> listAtt = null;
        if (docChange || dealDoc || (changeList != null && changeList.contains(id))) {//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
            String Text1 = jsonData.getString("issue_text");
            if (Text1 != null && !"".equals(Text1)) {
                try {
                    dataTime = String.valueOf(new Date().getTime());
                    listAtt = new ArrayList<>();
                    richDataMap.put("Text", rtfString(dataTime, Text1, listAtt));
                    log.info("附件有:" + Arrays.asList(listAtt));
                } catch (Exception e) {
                    log.info("富文本转换失败:" + e.getMessage());
                    /** 2释放word处理进程 并删除临时文件*/
                    deleteTmpFile(dataTime);
                    /** 2释放word处理进程 并删除临时文件 */
                }
            }
        }
        long dealText = System.currentTimeMillis();
        log.info("更新条目处理富文本----花费：" + (dealText - upQuery));
        try {
//			log.info("需要修改的aml_id----------" + id);
            mks.editIssue(id, dataMap, richDataMap);
            log.info("修改的条目： " + id);
            // 获取文档id
            // getDocID(id, docId, mks); //此查询作用
            // 附件
            if (SWIDMap != null)
                SWIDMap.put(SW_SID, id);
            long update = System.currentTimeMillis();
            log.info("更新条目更新----花费：" + (update - dealText));
            try {
                if (docChange || dealDoc || (changeList != null && changeList.contains(id))) {//文档变更，或者创建新文档，或者变更单关联有本条数据，允许变更
                    Object Attachments1 = jsonData.get("Attachments");
                    if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
                        String[] Attachments2 = (String[]) Attachments1;
                        for (int i = 0; i < Attachments2.length; i++) {
                            JSONObject j = JSONObject.parseObject(Attachments2[i]);
                            uploadAttachments(j, id);
                        }
                    }
                }
                /** 处理富文本所带图片*/
                if (listAtt != null && !listAtt.isEmpty()) {
                    for (Attachment att : listAtt) {
                        mks.addAttachment(id, att, Constants.TEXT_ATTACHMENT);
                    }
                }
                long upload = System.currentTimeMillis();
                log.info("更新条目上传附件----花费：" + (upload - update));
            } catch (APIException e) {
                log.error("附件处理失败：" + APIExceptionUtil.getMsg(e));
            }
            if (!dealDoc) {//为false时，是变更修改
                MapCache.cacheSWSID(uuid, SW_SID, id);
                log.info("Change Cache : " + uuid + " | " + SW_ID + " | " + id);
                log.info("SWSID_MAP : " + JSONObject.toJSONString(MapCache.getSWSIDMap(uuid)));
            }
            if (dataTime != null) {
                deleteTmpFile(dataTime);
            }
        } catch (APIException e) {
            /** 2释放word处理进程 并删除临时文件*/
            deleteTmpFile(dataTime);
            /** 2释放word处理进程 并删除临时文件 */
            log.error("error: " + "修改条目出错！" + APIExceptionUtil.getMsg(e));
            throw new MsgArgumentException("201", "修改条目出错 " + APIExceptionUtil.getMsg(e));
        }
        if (dealDoc) {
            if (SWMap != null) {
                List<String> almList = SWMap.get(SW_ID);
                if (almList == null) {
                    almList = new ArrayList<>();
                    SWMap.put(SW_ID, almList);
                }
                almList.add(id);
            }
        } else {
            MapCache.cacheSWID(uuid, SW_ID, id);
            MapCache.cacheSWSID(uuid, SW_SID, id);
            log.info("Change Cache : " + uuid + " | " + SW_ID + " | " + id);
            log.info("SWSID_MAP : " + JSONObject.toJSONString(MapCache.getSWSIDMap(uuid)));
        }
        long endDeal = System.currentTimeMillis();
        log.info("更新条目----花费：" + (endDeal - beginDeal));
        return id;
    }

    /**
     * 添加追溯关系
     *
     * @param jsonData
     * @param SWSIDMap
     * @param SWMap
     * @param
     */
    public void dealRelationship(JSONObject jsonData, Map<String, String> SWSIDMap, Map<String, List<String>> SWMap
    ) throws APIException {
        String Delete_Trace_ID = jsonData.getString("Delete_Trace_ID");
        String addTrace_ID = jsonData.getString("Trace_ID");//
//		log.info("Delete_Trace_ID 删除 ：" + Delete_Trace_ID);
//		log.info("Trace_ID 添加：" + addTrace_ID);
        if ((addTrace_ID == null || "".equals(addTrace_ID))
                && (Delete_Trace_ID == null || "".equals(Delete_Trace_ID))) {
            return;// 如果追溯关系删除和添加都没有，跳过本条处理
        }
        String SW_SID = jsonData.getString("SW_SID");// SW_SID
        String issueId = SWSIDMap.get(SW_SID);// 条目ID
//		log.info("Issue ID ：" + issueId + " || SW_SID" + SW_SID);
        if (issueId == null || "".equals(issueId)) {
            log.info("Issue ID获取不到：" + SW_SID);
            return;// 获取不到issueID
        }
        String doc_Type = jsonData.getString("issue_Type");
        String issue_Type = doc_Type.substring(0, doc_Type.lastIndexOf(" "));
        String project = jsonData.getString("Project");// 所属项目
        /** Modify By Cai Hao, 添加关联关系 */
//		log.info("需要添加的 - - - Teace_Id" + addTrace_ID);
//		log.info("需要删除的 - - - Delete_Trace_ID" + Delete_Trace_ID);
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
     */
    public boolean editIssueRelationship(String curIssueId, String curType, String deleteIssueStrs, String addIssueStrs,
                                         Map<String, List<String>> SWMap, String project, MKSCommand mks) {

        Map<String, String> deleRelationMap = null;
        long beginDeal = System.currentTimeMillis();
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
        long dealDelete = System.currentTimeMillis();
        log.info("查找删除追溯关系数据----花费：" + (dealDelete - beginDeal));
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
        long dealAdd = System.currentTimeMillis();
        log.info("查找新增追溯关系数据----花费：" + (dealAdd - dealDelete));
        try {
            mks.editRelationship(curIssueId, deleRelationMap, addRelationMap);
            long endDeal = System.currentTimeMillis();
            log.info("添加追溯关系总----花费：" + (endDeal - beginDeal));
            return true;
        } catch (APIException e) {
            long endDeal = System.currentTimeMillis();
            log.info("添加追溯关系总----花费：" + (endDeal - beginDeal));
            log.info("添加/删除关系失败，失败原因：" + APIExceptionUtil.getMsg(e));
            return false;
        }

    }

    /**
     * byte 转文件 下载到本地
     *
     * @param
     */
    @SuppressWarnings("restriction")
    public String conserveFile(String filePath1, String bytes) {
//        byte[] bytes= DatatypeConverter.parseBase64Binary(base64String);
        InputStream inputStream = null;
        InputStream inputStreams = null;
        try {
            log.info("下载附件到本地 转码 GBK");
            String str = new sun.misc.BASE64Encoder().encode(bytes.getBytes("GBK"));
            inputStream = new ByteArrayInputStream(str.getBytes());
            // 进行解码
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] byt = base64Decoder.decodeBuffer(inputStream);
            inputStreams = new ByteArrayInputStream(byt);

//            Files.copy(inputStreams, Paths.get(filePath1));
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
     * byte 转文件 下载到本地
     *
     * @param
     */
    @SuppressWarnings("restriction")
    public String conserveAttachmentFile(String filePath1, String bytes) {
//        byte[] bytes= DatatypeConverter.parseBase64Binary(base64String);
        InputStream inputStream = null;
        try {
            log.info("下载附件到本地 ");
            // 进行解码
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] byt = base64Decoder.decodeBuffer(bytes);
            inputStream = new ByteArrayInputStream(byt);
            ConvertRTFToHtml.sc(inputStream, filePath1);// 输入流保存到本地
            return filePath1;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //接受输入流转存本地 编辑上传附件
    public JSONObject uploadAttachments(JSONObject jsonObject, String id) {
//    	log.info("附件内容：" + jsonObject);
        String fj = jsonObject.getString("fileContent");
        String fileName = jsonObject.getString("filename");
        String fileType = jsonObject.getString("filetype");

//        String attachmentFile = jsonObject.get("attachmentFile").toString();
        String attPath = filePath + File.separator + fileName + "." + fileType;
        if (fileName.endsWith("." + fileType)) {// 判断是否名称已经包含有类型
            attPath = filePath + File.separator + fileName;
        }
        log.info(id + "的附件名：" + fileName + "附件类型：" + fileType + "|附件路径：" + attPath);
        //如果文件夹不存在则创建
        if (!new File(filePath).exists() && !new File(filePath).isDirectory()) {
            new File(filePath).mkdir();
        }
        File attFile = new File(attPath);
        //没有文件就创建
        if (!attFile.exists()) {
            try {
                attFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("下载附件到本地");
        conserveAttachmentFile(attPath, fj);//输入流保存到本地
        Attachment attachment = new Attachment();
        attachment.setName(fileName.endsWith("." + fileType) ? fileName : fileName + "." + fileType);
        attachment.setPath(attPath);
        try {
            log.info("开始上传附件");
            mks.addAttachment(id, attachment, Constants.ATTACHMENT);
            log.info("上传附件成功: " + fileName + "." + fileType);
        } catch (APIException e) {
            log.info("上传附件出错: " + id + "(" + e.getMessage() + ")");
            e.printStackTrace();
        }
        attFile.delete();//删除附件
        return ResultStr("200", "1111");
    }

    /**
     * 变更执行
     *
     * @param jsonData
     * @return
     */
    public String changeExecution(JSONObject jsonData) {
        String category = jsonData.getString("Category");
        String action_Type = jsonData.getString("action_Type");// 创建、更新、删除或移动
        log.info("变更反馈 - " + action_Type);
        log.info("jsonData - " + jsonData);
        String uuid = jsonData.getString("DOC_UUID");
        String docId = null;
        String resultStr = null;
        /** 判断变更关联对象，如果是文档，全更新；如果是条目，当前条目全更新，其他条目部分更新 */
        List<String> relatedIssueId = null;
        Boolean docChange = false;
        try {
            if (!"delete".equals(action_Type)) {//等于Null说明是删除
                String authoresChange = mks
                        .searchById(Arrays.asList(jsonData.getString("ALM_CO_ID")), Arrays.asList("Authorizes Changes To"))
                        .get(0).get("Authorizes Changes To");
                relatedIssueId = Arrays.asList(authoresChange.split(","));
                String issueType = mks.searchById(relatedIssueId, Arrays.asList("Type")).get(0).get("Type");
                if (issueType != null && (issueType.endsWith("Document") || issueType.endsWith("Suite"))) {// 关联的变更数据类型
                    docChange = true;
                }
            }
        } catch (APIException e1) {
            log.error("通过Change Order判断更新数据失败：" + APIExceptionUtil.getMsg(e1));
        }
        /** 判断变更关联对象 */
        if ("Document".equalsIgnoreCase(category)) {// 文档处理
            /** 变更更新文档数据 */
            String doc_SW_SID = jsonData.getString("Old_SW_SID");
            List<Map<String, String>> docList = null;
            String issue_Type = jsonData.getString("issue_Type");
            String project = jsonData.getString("Project");
            try {
                docList = mks.queryDocByQuery(doc_SW_SID, issue_Type, project);
            } catch (APIException e) {
                log.error("查询数据失败：" + APIExceptionUtil.getMsg(e));
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

            // 创建文档需要的参数
            try {
                if ("add".equals(action_Type)) {
                    resultStr = AddEntry(jsonData, null, null, docId, docChange, false);
                } else if ("update".equals(action_Type)) {
                    resultStr = UpDoc(jsonData, null, null, docId, docChange, false, relatedIssueId);
                } else if ("delete".equals(action_Type)) {
                    resultStr = DelDoc(jsonData, docId, docChange, false);
                } else if ("move".equals(action_Type)) {
                    resultStr = MoveDoc(jsonData, null, null, docId, docChange, false, relatedIssueId);
                }
                dealRelationship(jsonData, MapCache.getSWSIDMap(uuid), MapCache.getSWIDCacheMap(uuid));
                docId = mks.getTypeById(resultStr, "Document ID");
                log.info("返回的根据条目查询的文档id:" + docId);
            } catch (MsgArgumentException e) {
                MapCache.clearSWSIDCache(uuid);
                throw e;
            } catch (APIException e) {
                MapCache.clearSWSIDCache(uuid);
                log.error("变更 处理失败：" + APIExceptionUtil.getMsg(e));
                e.printStackTrace();
            }
        }

        /** 当数据处理完毕后，修改变更单 */
        String end = jsonData.getString("end");// 结尾标记，标识本次文档数据传输完毕
        if ("true".equals(end)) {
            try {
                MapCache.clearSWSIDCache(uuid);
                MapCache.clearSWIDCache(uuid);
                updateChangeInfo(jsonData.getString("ALM_CO_ID"), mks);
            } catch (APIException e) {
                log.error("变更单处理失败，失败原因：" + APIExceptionUtil.getMsg(e));
            } // 更新变更信息
        }
        return docId;
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

    /**
     * 删除数据
     *
     * @param dataTime
     */
    @SuppressWarnings("null")
    public void deleteTmpFile(String dataTime) {
        if (dataTime == null) {
            String rtfPath = filePath + File.separator + dataTime + ".rtf";
            String htmPath = filePath + File.separator + dataTime + ".htm";
            String imgFolderPath = filePath + "\\" + dataTime + ".files";
            File rtfFile = new File(rtfPath);
            File htmFile = new File(htmPath);
            File folderFile = new File(imgFolderPath);
            if (rtfFile.exists()) {
                try {
                    rtfFile.delete();
                } catch (Exception e) {
                    log.error("rtf文件不存在：" + dataTime);
                }
            }
            if (htmFile.exists()) {
                try {
                    htmFile.delete();
                } catch (Exception e) {
                    log.error("htm文件不存在：" + dataTime);
                }
            }
            if (folderFile.exists() && folderFile.isDirectory()) {
                File[] files = folderFile.listFiles();
                if (files == null && files.length > 0) {
                    for (File file : files) {
                        try {
                            file.delete();
                        } catch (Exception e) {
                            log.error("rft文件不存在：" + dataTime);
                        }
                    }
                }
            }
        }
    }
}
