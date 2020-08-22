package com.sw.SWAPI.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.CmdRunner;
import com.mks.api.Session;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.websocket.server.PathParam;
import java.io.*;
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
@PropertySource(value = {"classpath:sw.properties"})
public class AlmController {

//    @Autowired
//    private IntegrityFactory integrityFactory;

    private static final Log log = LogFactory.getLog(AlmController.class);
    //    private MKSCommand mks = new MKSCommand();
    @Autowired
    private CacheManager cacheManager;

//    @Value("${filePath}")
//    private String filePath;

    String docID = "";//文档id
    String tm_id = "";//变更创建的id
    String filePath = "C:\\\\SWFile";
    Map<String, String> swid_id = new HashMap<>();
    Cache cache = null;
    @Value("${host}")
    private String host;

    @Value("${port}")
    private int port;

    @Value("${loginName}")
    private String loginName;

    @Value("${passWord}")
    private String passWord;
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
            System.out.println("开始链接：");
            MKSCommand mks = new MKSCommand();
            mks.initMksCommand(host, port, loginName, passWord);
//            mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
            allUsers = mks.getAllUsers(Arrays.asList("fullname", "name", "Email"));
            System.out.println("断开链接：");
            mks.close(host, port, loginName);
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
//        MKSCommand mks = new MKSCommand();
//        mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");

        List<User> allUsers = new ArrayList<User>();
        try {
            MKSCommand mks = new MKSCommand();
            System.out.println("开始链接：");
            mks.initMksCommand(host, port, loginName, passWord);
            allUsers = mks.getProjectDynaUsers(project, dynamicGroups);
            System.out.println("断开链接：");
            mks.close(host, port, loginName);
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
    @RequestMapping(value = "/getAllProjects", method = RequestMethod.GET)
    public JSONArray getAllProject() {
        System.out.println("-------------查询所用项目-------------");

        List<Project> allUsers = new ArrayList<Project>();
        try {
            MKSCommand mks = new MKSCommand();
            mks.initMksCommand(host, port, loginName, passWord);
//            mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
            allUsers = mks.getAllprojects(Arrays.asList("backingIssueID", "name"));
        } catch (APIException e) {
            log.info("error: " + "查询所有project错误！" + e.getMessage());
            e.printStackTrace();
        }

//        mks.close(host,port,loginName);

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

//
//    @RequestMapping(value = "/getAllUsersByProject", method = RequestMethod.POST)
//    public JSONArray getAllUsers(@RequestBody JSONObject jsonData) {
//        System.out.println("-------------根据项目查询用户-------------");
//        String project = IsNull(jsonData.get("project"));
//        log.info("project-----" + project);
//
//        List<User> allUsers = new ArrayList<User>();
//        try {
//            allUsers = mks.getProjects(project);
//        } catch (APIException e) {
//            log.info("error: " + "查询所有用户错误！" + e.getMessage());
//            e.printStackTrace();
//        }
//
//        JSONArray jsonArray = new JSONArray();
//        for (int i = 0; i < allUsers.size(); i++) {
//            JSONObject jsonObject = new JSONObject();
//            User user = allUsers.get(i);
//            jsonObject.put("userName", user.getUserName());
//            jsonObject.put("login_ID", user.getLogin_ID());
//            jsonObject.put("email", user.getEmail());
//            jsonArray.add(jsonObject);
//        }
//        log.info("查询成功！" + jsonArray);
//        return jsonArray;
//    }

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
        System.out.println("-------------数据下发-------------");
        swid_id = new HashMap<>();
        docID = ""; //文档id
        System.out.println("参数信息：");
        log.info(jsonData);
        cache = cacheManager.getCache("orgCodeFindAll");

        String item_Owner = IsNull(jsonData.get("item_Owner"));//SystemWeaver需求最后撰写人
        String end = jsonData.get("end").toString();//结尾标记，标识本次文档数据传输完毕

        //移动到父id下面的位置 参数 first  last  before:name  after:name
//        String insertLocation = IsNull(jsonData.get("insertLocation"));
        JSONArray jsonResult = new JSONArray();
        String resultStr = "";
        if ("true".equals(end)) {  //所有参数存入缓存
            cache.put(new Element(IsNull(jsonData.get("SW_SID")), jsonData));
            List<String> list = cache.getKeys();
            //实现排序方法
            Collections.sort(list, Comparator.comparingInt(String::length).thenComparing(Comparator.comparing(String::toLowerCase, Comparator.reverseOrder())).
                    thenComparing(Comparator.reverseOrder()));

            MKSCommand mks = new MKSCommand();
            mks.initMksCommand(host, port, loginName, passWord);
            boolean xglt = false; //新增后修改文档状态评审中
            String xgyg = "";//新增后修改文档状态时用户
            System.out.println("总共---------------" + list.size() + "数据，开始下发！");
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject1 = (JSONObject) cache.get(list.get(i)).getObjectValue();
                String action_Type = IsNull(jsonObject1.get("action_Type"));
                xgyg = IsNull(jsonObject1.get("Assigned_User"));
                if ("add".equals(action_Type)) {
                    resultStr = AddDoc(jsonObject1, mks);
                    xglt = true;
                } else if ("update".equals(action_Type)) {
                    resultStr = UpDoc(jsonObject1, mks);
                } else if ("delete".equals(action_Type)) {
                    resultStr = DelDoc(jsonObject1, mks);
                } else if ("move".equals(action_Type)) {
                    resultStr = MoveDoc(jsonObject1, mks);
                }
            }
            //新增后修改状态为评审 in approve
            if (xglt) {
                try {
                    log.info("评审人---" + xgyg);
                    String[] arr = mks.getStaticGroup("VCU");
                    if (Arrays.asList(arr).contains(xgyg)) {
                        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
                        dataMap.put("State", "In Review");
                        dataMap.put("Assigned User", xgyg);
                        mks.editIssue(resultStr, dataMap, new HashMap<String, String>());
                    } else {
                        Map<String, String> dataMap1 = new HashMap<String, String>();//普通字段
                        dataMap1.put("State", "In Review");
                        dataMap1.put("Assigned User", xgyg);
                        mks.editIssue(resultStr, dataMap1, new HashMap<String, String>());

                        Map<String, String> dataMap2 = new HashMap<String, String>();//普通字段
                        dataMap2.put("State", "Audit");
                        mks.editIssue(resultStr, dataMap2, new HashMap<String, String>());

                        Map<String, String> dataMap3 = new HashMap<String, String>();//普通字段
                        dataMap3.put("State", "in approve");

                        mks.editIssue(resultStr, dataMap3, new HashMap<String, String>());

                    }
                } catch (APIException e) {
                    e.printStackTrace();
                }
                log.info("into: " + "创建文档后修改状态");
                System.out.println("断开链接：");
                mks.close(host, port, loginName);
                System.out.println("清理缓存！");
                cache.removeAll();
            }
        } else {
            cache.put(new Element(IsNull(jsonData.get("SW_SID")), jsonData));
            System.out.println(ResultJson("data", ""));
            return ResultJson("data", "");
        }
        System.out.println("清理缓存！");
        cache.removeAll();
        System.out.println("返回信息：");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", resultStr));
        System.out.println(jsonObject);
        return jsonObject;
    }

    //变更反馈增删改条目
    @RequestMapping(value = "/changeAction", method = RequestMethod.POST)
    public JSONObject changeAction1(@RequestBody JSONObject jsonData) {
        log.info(jsonData);
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host, port, loginName, passWord);
        String action_Type = IsNull(jsonData.get("action_Type"));//创建、更新、删除或移动
        String SW_SID = IsNull(jsonData.get("SW_SID"));//创建、更新、删除或移动
        String issue_Type = IsNull(jsonData.get("issue_Type"));//创建、更新、删除或移动
        String Parent_ID = IsNull(jsonData.get("Parent_ID"));
        String resultStr = "";
        docID = "";
        //创建文档需要的参数
        if (action_Type.equals("add")) {
            docID = mks.getDocIdsByType("SW_SID","entry_"+Parent_ID,"ID");
            resultStr =AddDoc(jsonData, mks);
            //添加变更单追溯关系
//            String ALM_CO_ID = IsNull(jsonData.get("ALM_CO_ID"));
//            try {
//                mks.addRelationships(ALM_CO_ID, "Authorizes Changes To", tm_id);
//            } catch (APIException e) {
//                log.info("error: " + "变更添加条目出错！" + e.getMessage());
//                e.printStackTrace();
//            }
        } else if (action_Type.equals("update")) {
            resultStr =UpDoc(jsonData, mks);
        } else if (action_Type.equals("delete")) {
            resultStr =DelDoc(jsonData, mks);
        } else if (action_Type.equals("move")) {
            resultStr =MoveDoc(jsonData, mks);
        }
        mks.close(host,port,loginName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", resultStr));
        System.out.println(jsonObject);
        return jsonObject;
    }

    //文档，条目新增
    public String AddDoc(JSONObject jsonData, MKSCommand mks) {
        System.out.println("-------------新增文档-----------");
        verification(jsonData);
        String issue_Type = IsNull(jsonData.get("issue_Type"));//创建文档类型或创建条目类型(创建时必须)
        String SW_SID = IsNull(jsonData.get("SW_SID"));
        String DOC_UUID = IsNull(jsonData.get("DOC_UUID"));
        System.out.println("创建SW_SID : " + SW_SID);
        if (docID.equals("")) {  //首次创建条目 先创建文档
            //先判断是否创建过
            String id = mks.getDocIdsByType("SW_SID", "doc_" + SW_SID, "ID");
            if (!id.equals("")) {
//                String isNO = mks.getProjectNameById(id);
                docID = id;
                swid_id.put("doc_" + SW_SID, id);
                log.info("已经存在的文档id： " + SW_SID);
            } else {
                String docType = new AnalysisXML().resultType(issue_Type);
                Map<String, String> docdataMap = new HashMap<String, String>();//普通字段
                Map<String, String> docmap = new AnalysisXML().resultFile(issue_Type);
                for (String key : docmap.keySet()) {
                    String strKey = IsNull(jsonData.get(key));
                    if (!strKey.equals("")) {
                        if (key.equals("SW_ID")) {
                            docdataMap.put(docmap.get(key), "doc_" + strKey);
                        } else if (key.equals("SW_SID")) {
                            docdataMap.put(docmap.get(key), "doc_" + strKey);
                        } else {
                            docdataMap.put(docmap.get(key), strKey);
                        }
                    }
                }
                //rtf
                Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
//                Object Text1 = jsonData.get("issue_text");
//                if(Text1 != null){
//                    String htmlStr = new AlmController().rtfString("doc_"+SW_SID,DOC_UUID);
//                    richDataMap.put("Description",htmlStr);
//                }
                try {
//                    docdataMap.put("Project","/ALM项目组");
                    System.out.println("issue_id ====== :" + IsNull(jsonData.get("issue_id")));
                    String doc_Id = mks.createDocument(docType, docdataMap, richDataMap);
                    docID = doc_Id;
                    log.info("创建的文档id： " + doc_Id);
                } catch (APIException e) {
                    log.info("error: " + "创建文档出错！" + e.getMessage());
                    System.out.println("清理缓存！");
                    cache.removeAll();
                    throw new MsgArgumentException("201", "创建文档出错 " + e.getMessage());
                }
            }
            swid_id.put("doc_" + SW_SID, docID);
        }

        //创建文档条目
        //xml配置字段
        //先判断是否创建过

        String e_id = mks.getDocIdsByType("SW_SID", "entry_" + SW_SID, "ID");
        if (!e_id.equals("")) {
//            String isNO = mks.getProjectNameById(e_id);
            swid_id.put("entry_" + SW_SID, e_id);
            log.info("已经存在的条目id： " + SW_SID);
        } else {
            Map<String, String> dataMap = new HashMap<String, String>();//普通字段
            Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
            String entryType = issue_Type.replace(" Document", "");
            String entryType1 = new AnalysisXML().resultType(entryType);
            Map<String, String> map = new AnalysisXML().resultFile(entryType);
            for (String key : map.keySet()) {
                String strKey = IsNull(jsonData.get(key));
                if (!strKey.equals("")) {
                    if (key.equals("SW_ID")) {
                        dataMap.put(map.get(key), "entry_" + strKey);
                    } else if (key.equals("SW_SID")) {
                        dataMap.put(map.get(key), "entry_" + strKey);
                    } else {
                        dataMap.put(map.get(key), strKey);
                    }

                }
            }
//            String Category = "Heading";
            String Category = "Requirement";
//            String Category = IsNull(jsonData.get("Category")).equals("")?"Requirement":IsNull(jsonData.get("Category"));
            dataMap.put("Category", Category);
//            rtf
            Object Text1 = jsonData.get("issue_text");
            if (Text1 != null) {
                String htmlStr = new AlmController().rtfString(Text1.toString(), "entry_" + SW_SID);
                dataMap.put("Text", htmlStr);
            }

            try {
//                dataMap.put("Project","/ALM项目组");
                String Parent_ID = IsNull(jsonData.get("Parent_ID"));
                System.out.println("Parent_ID>>>SW_SID-------" + IsNull(jsonData.get("Parent_ID")));
                if ("".equals(Parent_ID) || "null".equals(Parent_ID)) {
                    Parent_ID = docID;
                } else {
//                    Parent_ID = mks.getDocIdsByType("SW_SID","entry_"+Parent_ID,"ID");
                    Parent_ID = swid_id.get("entry_" + Parent_ID);
                    System.out.println("Parent_ID-------" + Parent_ID);
                }
                tm_id = mks.createContent(Parent_ID, dataMap, entryType1);
                log.info("创建的条目id： " + tm_id);
                swid_id.put("entry_" + SW_SID, tm_id);
                //附件
                Object Attachments1 = jsonData.get("Attachments");
                if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
                    String[] Attachments2 = (String[]) Attachments1;
                    for (int i = 0; i < Attachments2.length; i++) {
                        JSONObject j = JSONObject.parseObject(Attachments2[i]);
                        new AlmController().test3(j, tm_id, mks);
                    }
                }
            } catch (APIException e) {
                log.info("error: " + "创建文档条目出错！" + e.getMessage());
                System.out.println("清理缓存！");
                cache.removeAll();
                throw new MsgArgumentException("201", "创建文档条目出错 " + e.getMessage());
            }
        }

        return docID;
    }

    //文档条目修改
    public String UpDoc(JSONObject jsonData, MKSCommand mks) {
        System.out.println("-------------修改文档-----------");
        String issue_Type = IsNull(jsonData.get("issue_Type"));
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        String Old_SW_SID = IsNull(jsonData.get("Old_SW_SID"));//文档id
        System.out.println("修改的sw_sid----------" + Old_SW_SID);
        String id = mks.getDocIdsByType("SW_SID", "entry_" + Old_SW_SID, "ID");

        //rtf
        Object Text1 = jsonData.get("issue_text");
        if (Text1 != null) {
            String htmlStr = new AlmController().rtfString(Text1.toString(), "entry_" + Old_SW_SID);
            richDataMap.put("Text", htmlStr);
        }

        String entryType = issue_Type.replace(" Document", "");
        Map<String, String> map = new AnalysisXML().resultFile(entryType);
        for (String key : map.keySet()) {
            String strKey = IsNull(jsonData.get(key));
            if (!strKey.equals("")) {
                if (key.equals("SW_ID")) {
                    dataMap.put(map.get(key), "entry_" + strKey);
                } else if (key.equals("SW_SID")) {
                    dataMap.put(map.get(key), "entry_" + strKey);
                } else {
                    dataMap.put(map.get(key), strKey);
                }
            }
        }
        try {
            System.out.println("需要修改的aml_id----------" + id);
            mks.editIssue(id, dataMap, richDataMap);
            log.info("修改的文档： " + id);
            //附件
            Object Attachments1 = jsonData.get("Attachments");
            if (Attachments1 != null && !Attachments1.toString().equals("[]")) {
                String[] Attachments2 = (String[]) Attachments1;
                for (int i = 0; i < Attachments2.length; i++) {
                    JSONObject j = JSONObject.parseObject(Attachments2[i]);
                    new AlmController().test3(j, id, mks);
                }
            }
        } catch (APIException e) {
            log.info("error: " + "修改文档出错！" + e.getMessage());
            System.out.println("清理缓存！");
            cache.removeAll();
            throw new MsgArgumentException("201", "修改文档出错 " + e.getMessage());
        }
        return id;
    }

    //条目删除
    public String DelDoc(JSONObject jsonData, MKSCommand mks) {
        System.out.println("-------------删除文档-----------");
        String SW_SID = IsNull(jsonData.get("SW_SID"));//文档id
        String id = mks.getDocIdsByType("SW_SID", "entry_" + SW_SID, "ID");
        try {
            mks.removecontent(id);
        } catch (APIException e) {
            log.info("error: " + "删除条目关系出错！" + e.getMessage());
            System.out.println("清理缓存！");
            cache.removeAll();
            throw new MsgArgumentException("201", "删除条目关系出错 " + e.getMessage());
        }
        try {
            mks.deleteissue(id);
            log.info("删除的条目id： " + id);
        } catch (APIException e) {
            log.info("error: " + "删除条目出错！" + e.getMessage());
            System.out.println("清理缓存！");
            cache.removeAll();
            throw new MsgArgumentException("201", "删除条目出错 " + e.getMessage());
        }
        return id;
    }

    //条目移动
    public String MoveDoc(JSONObject jsonData, MKSCommand mks) {
        System.out.println("-------------移动文档-----------");
        String SW_ID = IsNull(jsonData.get("SW_ID"));//文档id
        String SW_SID = IsNull(jsonData.get("SW_SID"));//文档id
        String id = mks.getDocIdsByType("SW_SID", "entry_" + IsNull(jsonData.get("Old_SW_SID")), "ID");
        //在已经建立的追溯关系中删除 12223,12234
        String Delete_Trace_ID = IsNull(jsonData.get("Delete_Trace_ID"));
        // SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
        String Trace_ID = IsNull(jsonData.get("Trace_ID"));//
        String parentID = IsNull(jsonData.get("Parent_ID"));//SystemWeaver中父节点唯一标识(创建移动时必须)
        parentID = mks.getDocIdsByType("SW_SID", "entry_" + parentID, "ID");
        String ids = IsNull(jsonData.get("ids"));//需要移动的id 多个空格 1 2 3

        String Before_ID = IsNull(jsonData.get("Before_ID"));//SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        Before_ID = mks.getDocIdsByType("SW_SID", "entry_" + Before_ID, "ID");
        String After_ID = IsNull(jsonData.get("After_ID"));//SystemWeaver中后节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        After_ID = mks.getDocIdsByType("SW_SID", "entry_" + After_ID, "ID");
        String insertLocation = "";
        if (!Before_ID.equals("")) {
            insertLocation = "before:" + Before_ID;
        } else if (!After_ID.equals("")) {
            insertLocation = "after:" + After_ID;
        } else {
            insertLocation = "last";
        }
        try {
            System.out.println("需要移动的aml_id(" + id + "),移动的aml_parentID(" + parentID + ")：移动的具体位置id(" + insertLocation + ")");
            mks.movecontent(parentID, insertLocation, id);
            log.info("将id： (" + id + ")移动到 -" + parentID);
        } catch (APIException e) {
            log.info("error: " + "移动条目出错！" + e.getMessage());
            System.out.println("清理缓存！");
            cache.removeAll();
            throw new MsgArgumentException("201", "移动条目出错 " + e.getMessage());
        }
        System.out.println("移动后更新SW_SID");
        UpDoc(jsonData, mks);
        return parentID;
//        return ResultJson("ids","将id： ("+ids + ")移动到 -" + parentID);
    }

    /**
     * @Description
     * @Author liuxiaoguang
     * @Date 2020/8/3 15:42
     * @Param [jsonData]
     * @Return com.alibaba.fastjson.JSONObject
     * @Exception 评审/变更
     */
    @RequestMapping(value = "/Issue", method = RequestMethod.POST)
    public JSONObject Issue(@RequestBody JSONObject jsonData) {
        String Issue_Type = IsNull(jsonData.get("Issue_Type"));

        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host, port, loginName, passWord);

        JSONArray jsonResult = new JSONArray();
        if ("Item_Review".equals(Issue_Type)) {
            JSONObject jsonAdd = entryReview(jsonData, mks);
            jsonResult.add(jsonAdd);
        } else if ("Doc_Review".equals(Issue_Type)) {
            JSONObject jsonAdd = docReview(jsonData, mks);
            jsonResult.add(jsonAdd);
        } else if ("Item_Change".equals(Issue_Type)) {
            JSONObject jsonAdd = entryChange(jsonData, mks);
            jsonResult.add(jsonAdd);
        } else if ("Doc_Change".equals(Issue_Type)) {
            JSONObject jsonAdd = docChange(jsonData, mks);
            jsonResult.add(jsonAdd);
        }
        mks.close(host, port, loginName);
        return ResultJsonAry(jsonResult);
    }

    //文档评审
    public JSONObject docReview(JSONObject jsonData, MKSCommand mks) {
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        String Project = IsNull(jsonData.get("Project"));
        String D_Issue_ID = IsNull(jsonData.get("D_Issue_ID"));
        String D_Issue_Status = IsNull(jsonData.get("D_Issue_Status"));//Review/QA/PM/Released	反馈文档评审Issue状态
        String I_Issue_ID = IsNull(jsonData.get("I_Issue_ID"));//Review/QA/PM/Released	反馈文档评审Issue状态
        String Issue_Status = IsNull(jsonData.get("Issue_Status"));
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String Structure_ID = IsNull(jsonData.get("Structure_ID"));
        String Reviewer_ID = IsNull(jsonData.get("Reviewer_ID"));
        String Reviewer = IsNull(jsonData.get("Reviewer"));
        String Comment = IsNull(jsonData.get("Comment"));
        String[] Contents = (String[]) jsonData.get("Contents");//包含多个变更单及评审意见的信息
        String[] Relations = (String[]) jsonData.get("Relations");//包含于变更及评审相关的Issue
        if ("Approved".equals(Issue_Status)) {
            dataMap.put("state", "Published");
        } else {
            dataMap.put("state", "open");
        }
        dataMap.put("Comment", Comment);

        try {
            mks.editIssue(Item_ID, dataMap, richDataMap);
            log.info("文档评审 ： " + Item_ID);
        } catch (APIException e) {
            log.info("error: " + "文档评审出错！" + e.getMessage());

            throw new MsgArgumentException("201", "文档评审出错 " + e.getMessage());
        }
        return ResultJson("ids", Item_ID);
    }

    //条目评审
    public JSONObject entryReview(JSONObject jsonData, MKSCommand mks) {
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        String Reviewer_Engineer = IsNull(jsonData.get("Reviewer Engineer"));
        String Reviewer = IsNull(jsonData.get("Reviewer"));
        String Review_Result = IsNull(jsonData.get("Review Result"));

        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String Comment = IsNull(jsonData.get("Comment"));

        dataMap.put("Comment", Comment);
        dataMap.put(Reviewer_Engineer, Reviewer);//评审工程师和名字
        dataMap.put(Reviewer_Engineer + " Review Result", Review_Result);

        try {
            mks.editIssue(Item_ID, dataMap, richDataMap);
            log.info("文档评审 ： " + Item_ID);
        } catch (APIException e) {
            log.info("error: " + "文档评审出错！" + e.getMessage());
            throw new MsgArgumentException("201", "文档评审出错 " + e.getMessage());
        }
        return ResultJson("ids", Item_ID);
    }

    //文档变更
    public JSONObject docChange(JSONObject jsonData, MKSCommand mks) {
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String str = "";
        //创建变更请求时必须参数
        String docType = new AnalysisXML().resultType("Change Request");
        Map<String, String> map = new AnalysisXML().resultFile("Change Request");
        for (String key : map.keySet()) {
            String strKey = IsNull(jsonData.get(key));
            if (!strKey.equals("")) {
                dataMap.put(map.get(key), strKey);
            }
        }
        try {
            str = mks.createIssue(docType, dataMap, richDataMap);
            log.info("创建的变更请求id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更请求出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更出错 " + e.getMessage());
        }
        //添加文档的追溯关系
        try {
            mks.addRelationships(Item_ID, "Spawns", str);
            log.info("创建变更请求追溯关系成功 ：" + Item_ID + ">" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更请求追溯关系出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更请求追溯关系出错 " + e.getMessage());
        }
        //创建changeOrder
        String changeOrderId = addChangeOrder(jsonData, mks);
        //添加文档的追溯关系
        try {
            mks.addRelationships(str, "Spawns", changeOrderId);
            log.info("创建变更单追溯关系成功 ：" + str + ">" + changeOrderId);
        } catch (APIException e) {
            log.info("error: " + "创建变更单追溯关系出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更单追溯关系出错 " + e.getMessage());
        }
        return ResultJson("ids", str + ">" + changeOrderId);
    }

    //条目变更
    public JSONObject entryChange(JSONObject jsonData, MKSCommand mks) {
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String str = "";
        //创建时必须参数
        String docType = new AnalysisXML().resultType("Change Request");
        Map<String, String> map = new AnalysisXML().resultFile("Change Request");
        for (String key : map.keySet()) {
            String strKey = IsNull(jsonData.get(key));
            if (!strKey.equals("")) {
                dataMap.put(map.get(key), strKey);
            }
        }
        try {
            str = mks.createIssue(docType, dataMap, richDataMap);
            log.info("创建的变更id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更出错 " + e.getMessage());
        }
        //添加文档的追溯关系
        try {
            mks.addRelationships(Item_ID, "Spawns", str);
            log.info("创建追溯关系成功 ：" + Item_ID + ">" + str);
        } catch (APIException e) {
            log.info("error: " + "创建追溯关系出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建追溯关系出错 " + e.getMessage());
        }
        //创建changeOrder
        String changeOrderId = addChangeOrder(jsonData, mks);
        //添加changeOrder的追溯关系
        try {
            mks.addRelationships(str, "Spawns", changeOrderId);
            log.info("创建变更单追溯关系成功 ：" + str + ">" + changeOrderId);
        } catch (APIException e) {
            log.info("error: " + "创建变更单追溯关系出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更单追溯关系出错 " + e.getMessage());
        }
        return ResultJson("ids", str + ">" + changeOrderId);
    }

    public String addChangeOrder(JSONObject jsonData, MKSCommand mks) {
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        Map<String, String> dataMap = new HashMap<String, String>();//普通字段
        Map<String, String> richDataMap = new HashMap<String, String>();//富文本字段
        dataMap.put("project", IsNull(jsonData.get("project")));
        String str = "";
        try {
            str = mks.createIssue("Change Order", dataMap, richDataMap);
            log.info("创建的变更单id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更单出错！" + e.getMessage());
            throw new MsgArgumentException("201", "创建变更单出错 " + e.getMessage());
        }
        return str;
    }

    //测试
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(@PathParam("str1") String str1) {
        Cache cache = cacheManager.getCache("orgCodeFindAll");
        cache.remove("swid");
        cache.put(new Element("swid", str1));
//        JSONObject str = (JSONObject)cache.get(str1).getObjectValue();
        String str = cache.get("swid").getObjectValue().toString();
//
        return str;
    }

    //测试
    @RequestMapping(value = "/test2", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public JSONObject test2(@PathParam("text") InputStream text) {
        System.out.println(text);
        return ResultStr("msg", "1");
//        String str = filePath + "\\123456.rtf";
//        String str1 = filePath + "\\123456";
//
//
//        //如果文件夹不存在则创建
//        if  (!new File(filePath) .exists()  && !new File(filePath) .isDirectory())
//        {
//            new File(filePath) .mkdir();
//        }
//        //没有文件就创建
//        if(!new File(str).exists()){
//            try {
//                new File(str).createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        new ConvertRTFToHtml().sc(text,str);//输入流保存到本地
//        new ConvertRTFToHtml().RTFToHtml(str,str1);//本地rtf文件转换为html
//        String htmldata = null;//获取html中元素
//        try {
//            htmldata = new ConvertRTFToHtml().readHtml(str1+".htm");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Cache cache = cacheManager.getCache("orgCodeFindAll");
//        cache.put(new Element("swid",htmldata));
//        return ResultStr("msg","1");
    }

    //输入流转换html string
    public String rtfString(String text, String sid) {
        String str = filePath + "\\" + new Date().getTime() + ".rtf";
        String str1 = filePath + "\\" + new Date().getTime();


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
        new AlmController().conserveFile(str, text);
//        new ConvertRTFToHtml().sc(text,str);//输入流保存到本地
        new ConvertRTFToHtml().RTFToHtml(str, str1);//本地rtf文件转换为html
        String htmldata = null;//获取html中元素
        try {
            htmldata = new ConvertRTFToHtml().readHtml(str1 + ".htm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmldata;
    }

    //接受输入流转存本地 编辑上传附件
    public JSONObject test3(JSONObject jsonObject, String id, MKSCommand mks) {
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

        new AlmController().conserveFile(fj, str);//输入流保存到本地
        Attachment attachment = new Attachment();
        attachment.setName(fileNmae + "." + fileType);
        attachment.setPath(str);
        try {
            mks.addAttachment(id, attachment, attachmentFile);
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
            new ConvertRTFToHtml().sc(inputStreams, filePath1);//输入流保存到本地
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

    public static void main(String[] str) {
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
////        String isNO = mks.getProjectNameById("22324");
//        String id = mks.getDocIdsByType("SW_ID","85125614","type");
//        mks.getDocIdsByType("SW_SID","entry_x0400000000084B8D","ID");
//        String value = "{\\rtf1 \\ansi \\ansicpg936 \\deff0 \\stshfdbch1 \\stshfloch2 \\stshfhich2 \\deflang2052 \\deflangfe2052 {\\fonttbl {\\f0 \\froman \\fcharset0 \\fprq2 {\\*\\panose 02020603050405020304}Times New Roman{\\*\\falt Times New Roman};}{\\f1 \\fnil \\fcharset134 \\fprq0 {\\*\\panose 02010600030101010101}\\'cb\\'ce\\'cc\\'e5{\\*\\falt \\'cb\\'ce\\'cc\\'e5};}{\\f2 \\fswiss \\fcharset0 \\fprq0 {\\*\\panose 020f0502020204030204}Calibri{\\*\\falt Calibri};}{\\f3 \\fnil \\fcharset2 \\fprq0 {\\*\\panose 05000000000000000000}Wingdings{\\*\\falt Wingdings};}}{\\colortbl;\\red0\\green0\\blue0;\\red128\\green0\\blue0;\\red255\\green0\\blue0;\\red0\\green128\\blue0;\\red128\\green128\\blue0;\\red0\\green255\\blue0;\\red255\\green255\\blue0;\\red0\\green0\\blue128;\\red128\\green0\\blue128;\\red0\\green128\\blue128;\\red128\\green128\\blue128;\\red192\\green192\\blue192;\\red0\\green0\\blue255;\\red255\\green0\\blue255;\\red0\\green255\\blue255;\\red255\\green255\\blue255;}{\\stylesheet {\\qj \\li0 \\ri0 \\nowidctlpar \\aspalpha \\aspnum \\adjustright \\lin0 \\rin0 \\itap0 \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\dbch \\af1 \\hich \\af2 \\loch \\f2 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 \\snext0 \\sqformat \\spriority0 Normal;}{\\*\\cs10 \\rtlch \\ltrch \\snext10 \\ssemihidden \\spriority0 Default Paragraph Font;}}{\\*\\latentstyles \\lsdstimax260 \\lsdlockeddef0 \\lsdsemihiddendef1 \\lsdunhideuseddef1 \\lsdqformatdef0 \\lsdprioritydef99 {\\lsdlockedexcept \\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Normal;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 1;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 2;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 3;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 4;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 5;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 6;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 7;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 8;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Normal Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footnote text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 header;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footer;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index heading;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 caption;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 table of figures;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 envelope address;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 envelope return;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footnote reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 line number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 page number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 endnote reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 endnote text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 table of authorities;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 macro;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toa heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Title;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Closing;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Signature;\\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Default Paragraph Font;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Message Header;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Subtitle;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Salutation;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Date;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text First Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text First Indent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Note Heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Block Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Hyperlink;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 FollowedHyperlink;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Strong;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Emphasis;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Document Map;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Plain Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 E-mail Signature;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Normal (Web);\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Acronym;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Address;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Cite;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Code;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Definition;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Keyboard;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Preformatted;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Sample;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Typewriter;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Variable;\\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Normal Table;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation subject;\\lsdpriority99 \\lsdlocked0 No List;\\lsdpriority99 \\lsdlocked0 1 / a / i;\\lsdpriority99 \\lsdlocked0 1 / 1.1 / 1.1.1;\\lsdpriority99 \\lsdlocked0 Article / Section;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Contemporary;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Elegant;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Professional;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Subtle 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Subtle 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Balloon Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Theme;\\lsdpriority99 \\lsdlocked0 Placeholder Text;\\lsdpriority99 \\lsdlocked0 No Spacing;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 1;\\lsdpriority99 \\lsdlocked0 List Paragraph;\\lsdpriority99 \\lsdlocked0 Quote;\\lsdpriority99 \\lsdlocked0 Intense Quote;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 6;}}{\\*\\generator WPS Office}{\\info {\\author Administrator}{\\operator \\'c1\\'f5\\'d0\\'a1\\'b9\\'e2}{\\creatim \\yr2020 \\mo8 \\dy12 \\hr8 \\min58 }{\\revtim \\yr2020 \\mo8 \\dy12 \\hr8 \\min59 }{\\version1 }{\\nofpages1 }}\\paperw12240 \\paperh15840 \\margl1800 \\margr1800 \\margt1440 \\margb1440 \\gutter0 \\deftab420 \\ftnbj \\aenddoc \\formshade \\dgmargin \\dghspace180 \\dgvspace156 \\dghorigin1800 \\dgvorigin1440 \\dghshow0 \\dgvshow2 \\jcompress1 \\viewkind1 \\viewscale110 \\viewscale110 \\splytwnine \\ftnlytwnine \\htmautsp \\useltbaln \\alntblind \\lytcalctblwd \\lyttblrtgr \\lnbrkrule \\nogrowautofit \\nobrkwrptbl \\wrppunct {\\*\\fchars !),.:;?]\\'7d\\'a1\\'a7\\'a1\\'a4\\'a1\\'a6\\'a1\\'a5\\'a8D\\'a1\\'ac\\'a1\\'af\\'a1\\'b1\\'a1\\'ad\\'a1\\'c3\\'a1\\'a2\\'a1\\'a3\\'a1\\'a8\\'a1\\'a9\\'a1\\'b5\\'a1\\'b7\\'a1\\'b9\\'a1\\'bb\\'a1\\'bf\\'a1\\'b3\\'a1\\'bd\\'a3\\'a1\\'a3\\'a2\\'a3\\'a7\\'a3\\'a9\\'a3\\'ac\\'a3\\'ae\\'a3\\'ba\\'a3\\'bb\\'a3\\'bf\\'a3\\'dd\\'a3\\'e0\\'a3\\'fc\\'a3\\'fd\\'a1\\'ab\\'a1\\'e9}{\\*\\lchars ([\\'7b\\'a1\\'a4\\'a1\\'ae\\'a1\\'b0\\'a1\\'b4\\'a1\\'b6\\'a1\\'b8\\'a1\\'ba\\'a1\\'be\\'a1\\'b2\\'a1\\'bc\\'a3\\'a8\\'a3\\'ae\\'a3\\'db\\'a3\\'fb\\'a1\\'ea\\'a3\\'a4}\\fet2 {\\*\\ftnsep \\pard \\plain {\\insrsid \\chftnsep \\par }}{\\*\\ftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par }}{\\*\\aftnsep \\pard \\plain {\\insrsid \\chftnsep \\par }}{\\*\\aftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par }}\\sectd \\sbkpage \\pgwsxn11906 \\pghsxn16838 \\marglsxn1800 \\margrsxn1800 \\margtsxn1440 \\margbsxn1440 \\guttersxn0 \\headery851 \\footery992 \\pgbrdropt32 \\sectlinegrid312 \\sectspecifyl \\endnhere \\pard \\plain \\qj \\li0 \\ri0 \\nowidctlpar \\aspalpha \\aspnum \\adjustright \\lin0 \\rin0 \\itap0 \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\dbch \\af1 \\hich \\af2 \\loch \\af2 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 {\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\loch \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 Crtg}{\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\loch \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 \\par }}";
//        new AlmController().conserveFile("11111",value);
//        mks.getStaticGroup("VCU");
        try {
            mks.getProjects("/aaaaa");
        } catch (APIException e) {
            e.printStackTrace();
        }
    }

}
