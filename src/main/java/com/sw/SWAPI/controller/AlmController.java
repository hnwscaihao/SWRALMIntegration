package com.sw.SWAPI.controller;


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
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.Cache.ValueWrapper;

import javax.websocket.server.PathParam;
import java.io.*;
import java.util.*;

import static com.sw.SWAPI.util.Obj.IsNull;
import static com.sw.SWAPI.util.ResultJson.*;

/**
 *  @author: liuxiaoguang
 *  @Date: 2020/7/16 15:28
 *  @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value="/alm")
public class AlmController {

    private static final Log log = LogFactory.getLog(AlmController.class);

    @Autowired
    private CacheManager cacheManager;

    @Value("${host}")
    private String host;

    @Value("${port}")
    private int port;

    @Value("${loginName}")
    private String loginName;

    @Value("${passWord}")
    private String passWord;

    @Value("${filePath}")
    private String filePath;

    String docID = "";//文档id

    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/16 15:33
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception   获取ALM中所有用户信息
     */
    @RequestMapping(value="/getAllUsers", method = RequestMethod.GET)
    public JSONArray getAllUsers(){
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        List<User> allUsers = new ArrayList<>();
        try {
            allUsers = mks.getAllUsers(Arrays.asList("fullname","name","Email"));
        } catch (APIException e) {
            log.info("error: " + "查询所有用户错误！"+e.getMessage());
            e.printStackTrace();
        }

        mks.close(host,port,loginName);

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<allUsers.size();i++){
            JSONObject jsonObject = new JSONObject();
            User user = allUsers.get(i);
            jsonObject.put("userName",user.getUserName());
            jsonObject.put("login_ID",user.getLogin_ID());
            jsonObject.put("email",user.getEmail());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/17 14:53
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception 获取ALM中Project列表
     */
    @RequestMapping(value="/getAllProjects", method = RequestMethod.GET)
    public JSONArray getAllProject(){
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        List<Project> allUsers = new ArrayList<>();
        try {
            allUsers = mks.getAllprojects(Arrays.asList("backingIssueID","name"));
        } catch (APIException e) {
            log.info("error: " + "查询所有project错误！" + e.getMessage());
            e.printStackTrace();
        }

        mks.close(host,port,loginName);

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<allUsers.size();i++){
            JSONObject jsonObject = new JSONObject();
            Project project = allUsers.get(i);
            jsonObject.put("project",project.getProject());
            jsonObject.put("PID",project.getPID());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }


    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/22 10:02
     * @Param  [jsonData]
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception  创建 修改 删除 移动文档条目
     */
    @RequestMapping(value="/releaseData", method = RequestMethod.POST)
    public JSONObject createDocument(@RequestBody JSONObject jsonData){
        docID = ""; //文档id
        System.out.println("参数信息：");
        System.out.println(jsonData);
        Cache cache = cacheManager.getCache("orgCodeFindAll");

         String item_Owner = IsNull(jsonData.get("item_Owner"));//SystemWeaver需求最后撰写人
         String end = IsNull(jsonData.get("End"));//结尾标记，标识本次文档数据传输完毕

        //移动到父id下面的位置 参数 first  last  before:name  after:name
//        String insertLocation = IsNull(jsonData.get("insertLocation"));
        JSONArray jsonResult = new JSONArray();
        if(!end.equals("true")){  //所有参数存入缓存
            cache.put(new Element(IsNull(jsonData.get("DOC_UUID")),jsonData));
            return  ResultStr("msg","正在缓存文件！");
        }else {
            cache.put(new Element(IsNull(jsonData.get("DOC_UUID")),jsonData));
            List<String> list =  cache.getKeys();
            MKSCommand mks = new MKSCommand();
            mks.initMksCommand(host,port,loginName,passWord);
            for (int i=0;i<list.size();i++){
                JSONObject jsonObject1 = (JSONObject)cache.get(list.get(i)).getObjectValue();
                String action_Type = IsNull(jsonData.get("action_Type"));
                if("add".equals(action_Type)){
                    JSONObject jsonAdd = AddDoc(jsonObject1,mks);
                    jsonResult.add(jsonAdd);
                }else if("update".equals(action_Type)){
                    JSONObject jsonAdd = UpDoc(jsonObject1,mks);
                    jsonResult.add(jsonAdd);
                }else if("delete".equals(action_Type)){
                    JSONObject jsonAdd = DelDoc(jsonObject1,mks);
                    jsonResult.add(jsonAdd);
                }else if("move".equals(action_Type)){
                    JSONObject jsonAdd = MoveDoc(jsonObject1,mks);
                    jsonResult.add(jsonAdd);
                }
            }
            mks.close(host,port,loginName);
            cache.removeAll();
        }
        System.out.println("返回信息：");
        System.out.println(jsonResult);
        return ResultJsonAry(jsonResult);
    }

    //文档，条目新增
    public JSONObject AddDoc(JSONObject jsonData,MKSCommand mks){
        String issue_Type = IsNull(jsonData.get("Issue_Type"));//创建文档类型或创建条目类型(创建时必须)
        String SWID = IsNull(jsonData.get("SW_ID"));
        String DOC_UUID = IsNull(jsonData.get("DOC_UUID"));

        if(docID.equals("")){  //首次创建条目 先创建文档
            //先判断是否创建过
            String id = mks.getDocIdsByType("SW_ID","doc_"+SWID,"ID");
            String isNO = mks.getProjectNameById(id);
            if(!isNO.equals("")){
                docID = id;
                log.info("已经存在的文档id： "+SWID);
            }else {
                String docType = new AnalysisXML().resultType(issue_Type);
                Map<String,String> docdataMap = new HashMap<>();//普通字段
                Map<String,String> docmap =  new AnalysisXML().resultFile(issue_Type);
                for(String key : docmap.keySet()){
                    String strKey = IsNull(jsonData.get(key));
                    if(!strKey.equals("")){
                        if(key.equals("SW_ID")){
                            docdataMap.put(docmap.get(key),"doc_"+strKey);
                        }else if(key.equals("SW_SID")){
                            docdataMap.put(docmap.get(key),"doc_"+strKey);
                        }else{
                            docdataMap.put(docmap.get(key),strKey);
                        }
                    }
                }
                //rtf
                Object Text1 = jsonData.get("issue_text");
                Map<String,String> richDataMap = new HashMap<>();//富文本字段
                if(Text1 != null){
                    InputStream text = (InputStream)Text1;
                    String htmlStr = new AlmController().rtfString(text,DOC_UUID);
                    richDataMap.put("Description",htmlStr);
                }
                try {
                    String doc_Id =   mks.createDocument(docType,docdataMap,richDataMap);
                    docID = doc_Id;
                    log.info("创建的文档id： "+doc_Id);
                } catch (APIException e) {
                    log.info("error: " + "创建文档出错！"+ e.getMessage());
                    throw new MsgArgumentException("201","创建文档出错 "+ e.getMessage());
                }
            }
        }
        //创建文档条目
        //xml配置字段
        //先判断是否创建过
        String e_id = mks.getDocIdsByType("SW_ID",SWID,"ID");
        String isNO = mks.getProjectNameById(e_id);
        if(!isNO.equals("")){
            log.info("已经存在的条目id： "+SWID);
        }else {
            Map<String,String> dataMap = new HashMap<>();//普通字段
            Map<String,String> richDataMap = new HashMap<>();//富文本字段
            String entryType = issue_Type.replace(" Document", "");
            String entryType1 = new AnalysisXML().resultType(entryType);
            Map<String,String> map =  new AnalysisXML().resultFile(entryType);
            for(String key : map.keySet()){
                String strKey = IsNull(jsonData.get(key));
                if(!strKey.equals("")){
                    dataMap.put(map.get(key),strKey);
                }
            }
            String Category = IsNull(jsonData.get("Category")).equals("")?"Requirement":IsNull(jsonData.get("Category"));
            dataMap.put("Category",Category);
            //rtf
            Object Text1 = jsonData.get("issue_text");
            if(Text1 != null){
                InputStream text = (InputStream)Text1;
                String htmlStr = new AlmController().rtfString(text,DOC_UUID);
                richDataMap.put("Description",htmlStr);
            }

            try {
                String tm_id = mks.createcontent(entryType1,docID,dataMap,richDataMap);
                log.info("创建的条目id： "+tm_id);
                //附件
                Object Attachments1 =  jsonData.get("Attachments");
                if(Attachments1 != null){
                    String[] Attachments2 = (String[])Attachments1;
                    for(int i=0;i<Attachments2.length;i++){
                        JSONObject j = JSONObject.parseObject(Attachments2[i]);
                        new AlmController().test3(j,tm_id,mks);
                    }
                }
            } catch (APIException e) {
                log.info("error: " + "创建文档出错！"+ e.getMessage());
                throw new MsgArgumentException("201","创建文档出错 "+ e.getMessage());
            }
        }
        return ResultJson("ids",docID);
    }
    //文档条目修改
    public JSONObject UpDoc(JSONObject jsonData,MKSCommand mks){
        String issue_Type = IsNull(jsonData.get("issue_Type"));
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        String SW_ID = IsNull(jsonData.get("SW_ID"));//文档id
        String id = mks.getDocIdsByType("SW_ID",SW_ID,"ID");
        String DOC_UUID = IsNull(jsonData.get("DOC_UUID"));

        //rtf
        Object Text1 = jsonData.get("Text");
        if(Text1 != null){
            InputStream text = (InputStream)Text1;
            String htmlStr = new AlmController().rtfString(text,DOC_UUID);
            richDataMap.put("Text",htmlStr);
        }

        Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
        for(String key : map.keySet()){
            String strKey = IsNull(jsonData.get(key));
            if(!strKey.equals("")){
                dataMap.put(map.get(key),strKey);
            }
        }
        try {
            mks.editIssue(id,dataMap,richDataMap);
            log.info("修改的文档： "+id);
            //附件
            Object Attachments1 =  jsonData.get("Attachments");
            if(Attachments1 != null){
                String[] Attachments2 = (String[])Attachments1;
                for(int i=0;i<Attachments2.length;i++){
                    JSONObject j = JSONObject.parseObject(Attachments2[i]);
                    new AlmController().test3(j,id,mks);
                }
            }
        } catch (APIException e) {
            log.info("error: " + "修改文档出错！"+ e.getMessage());
            throw new MsgArgumentException("201","修改文档出错 "+ e.getMessage());
        }
        return ResultJson("ids",id);
    }
    //条目删除
    public JSONObject DelDoc(JSONObject jsonData,MKSCommand mks){
        String SW_ID = IsNull(jsonData.get("SW_ID"));//文档id
        String id = mks.getDocIdsByType("SW_ID",SW_ID,"ID");
        try {
            mks.removecontent(id);
        } catch (APIException e) {
            log.info("error: " + "删除条目关系出错！"+ e.getMessage());
            throw new MsgArgumentException("201","删除条目关系出错 "+ e.getMessage());
        }
        try {
            mks.deleteissue(id);
            log.info("删除的条目id： "+id);
        } catch (APIException e) {
            log.info("error: " + "删除条目出错！"+ e.getMessage());
            throw new MsgArgumentException("201","删除条目出错 "+ e.getMessage());
        }
        return ResultJson("ids",id);
    }
    //条目移动
    public JSONObject MoveDoc(JSONObject jsonData,MKSCommand mks){
        String SW_ID = IsNull(jsonData.get("SW_ID"));//文档id
        String id = mks.getDocIdsByType("SW_ID",SW_ID,"ID");
        //在已经建立的追溯关系中删除 12223,12234
        String Delete_Trace_ID = IsNull(jsonData.get("Delete_Trace_ID"));
        // SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
        String Trace_ID = IsNull(jsonData.get("Trace_ID"));//
        String parentID = IsNull(jsonData.get("Parent_ID"));//SystemWeaver中父节点唯一标识(创建移动时必须)
        String ids = IsNull(jsonData.get("ids"));//需要移动的id 多个空格 1 2 3

        String Before_ID = IsNull(jsonData.get("Before_ID"));//SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        String After_ID = IsNull(jsonData.get("After_ID"));//SystemWeaver中后节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        String insertLocation = "";
        if(!Before_ID.equals("")){
            insertLocation = "before:"+Before_ID;
        }else if(!After_ID.equals("")){
            insertLocation = "after:"+After_ID;
        }else {
            insertLocation = "last";
        }
        try {
            mks.movecontent(parentID,insertLocation,ids);
            log.info("将id： ("+ids + ")移动到 -" + parentID);
        } catch (APIException e) {
            log.info("error: " + "移动条目出错！"+ e.getMessage());
            throw new MsgArgumentException("201","移动条目出错 "+ e.getMessage());
        }
        return ResultJson("ids","将id： ("+ids + ")移动到 -" + parentID);
    }

    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/8/3 15:42
     * @Param  [jsonData]
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception  评审/变更
     */
    @RequestMapping(value="/Issue", method = RequestMethod.POST)
    public JSONObject Issue(@RequestBody JSONObject jsonData){
        String Issue_Type = IsNull(jsonData.get("Issue_Type"));

        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        JSONArray jsonResult = new JSONArray();
        if("Item_Review".equals(Issue_Type)){
            JSONObject jsonAdd = entryReview(jsonData,mks);
            jsonResult.add(jsonAdd);
        }else if("Doc_Review".equals(Issue_Type)){
            JSONObject jsonAdd = docReview(jsonData,mks);
            jsonResult.add(jsonAdd);
        }else if("Item_Change".equals(Issue_Type)){
            JSONObject jsonAdd = entryChange(jsonData,mks);
            jsonResult.add(jsonAdd);
        }else if("Doc_Change".equals(Issue_Type)){
            JSONObject jsonAdd = docChange(jsonData,mks);
            jsonResult.add(jsonAdd);
        }
        mks.close(host,port,loginName);
        return ResultJsonAry(jsonResult);
    }

    //文档评审
    public JSONObject docReview(JSONObject jsonData,MKSCommand mks){
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
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
        String[] Contents = (String[])jsonData.get("Contents");//包含多个变更单及评审意见的信息
        String[] Relations = (String[])jsonData.get("Relations");//包含于变更及评审相关的Issue
        if("Approved".equals(Issue_Status)){
            dataMap.put("state","Published");
        }else {
            dataMap.put("state","open");
        }
        dataMap.put("Comment",Comment);

        try {
            mks.editIssue(Item_ID,dataMap,richDataMap);
            log.info("文档评审 ： "+Item_ID);
        } catch (APIException e) {
            log.info("error: " + "文档评审出错！"+ e.getMessage());
            throw new MsgArgumentException("201","文档评审出错 "+ e.getMessage());
        }
        return ResultJson("ids",Item_ID);
    }
    //条目评审
    public JSONObject entryReview(JSONObject jsonData,MKSCommand mks){
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        String Reviewer_Engineer = IsNull(jsonData.get("Reviewer Engineer"));
        String Reviewer = IsNull(jsonData.get("Reviewer"));
        String Review_Result = IsNull(jsonData.get("Review Result"));

        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String Comment = IsNull(jsonData.get("Comment"));

        dataMap.put("Comment",Comment);
        dataMap.put(Reviewer_Engineer,Reviewer);//评审工程师和名字
        dataMap.put(Reviewer_Engineer+" Review Result",Review_Result);

        try {
            mks.editIssue(Item_ID,dataMap,richDataMap);
            log.info("文档评审 ： "+Item_ID);
        } catch (APIException e) {
            log.info("error: " + "文档评审出错！"+ e.getMessage());
            throw new MsgArgumentException("201","文档评审出错 "+ e.getMessage());
        }
        return ResultJson("ids",Item_ID);
    }
    //文档变更
    public JSONObject docChange(JSONObject jsonData,MKSCommand mks){
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String str = "";
        //创建变更请求时必须参数
        String docType = new AnalysisXML().resultType("Change Request");
        Map<String,String> map =  new AnalysisXML().resultFile("Change Request");
        for(String key : map.keySet()){
            String strKey = IsNull(jsonData.get(key));
            if(!strKey.equals("")){
                dataMap.put(map.get(key),strKey);
            }
        }
        try {
            str = mks.createIssue(docType,dataMap,richDataMap);
            log.info("创建的变更请求id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更请求出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更出错 "+ e.getMessage());
        }
        //添加文档的追溯关系
        try {
            mks.addRelationships(Item_ID,"Spawns",str);
            log.info("创建变更请求追溯关系成功 ：" + Item_ID+">"+str);
        } catch (APIException e) {
            log.info("error: " + "创建变更请求追溯关系出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更请求追溯关系出错 "+ e.getMessage());
        }
        //创建changeOrder
        String changeOrderId = addChangeOrder(jsonData,mks);
        //添加文档的追溯关系
        try {
            mks.addRelationships(str,"Spawns",changeOrderId);
            log.info("创建变更单追溯关系成功 ：" + str+">"+changeOrderId);
        } catch (APIException e) {
            log.info("error: " + "创建变更单追溯关系出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更单追溯关系出错 "+ e.getMessage());
        }
        return ResultJson("ids",str+">"+changeOrderId);
    }
    //条目变更
    public JSONObject entryChange(JSONObject jsonData,MKSCommand mks){
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        String str = "";
        //创建时必须参数
        String docType = new AnalysisXML().resultType("Change Request");
        Map<String,String> map =  new AnalysisXML().resultFile("Change Request");
        for(String key : map.keySet()){
            String strKey = IsNull(jsonData.get(key));
            if(!strKey.equals("")){
                dataMap.put(map.get(key),strKey);
            }
        }
        try {
            str = mks.createIssue(docType,dataMap,richDataMap);
            log.info("创建的变更id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更出错 "+ e.getMessage());
        }
        //添加文档的追溯关系
        try {
            mks.addRelationships(Item_ID,"Spawns",str);
            log.info("创建追溯关系成功 ：" + Item_ID+">"+str);
        } catch (APIException e) {
            log.info("error: " + "创建追溯关系出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建追溯关系出错 "+ e.getMessage());
        }
        //创建changeOrder
        String changeOrderId = addChangeOrder(jsonData,mks);
        //添加changeOrder的追溯关系
        try {
            mks.addRelationships(str,"Spawns",changeOrderId);
            log.info("创建变更单追溯关系成功 ：" + str+">"+changeOrderId);
        } catch (APIException e) {
            log.info("error: " + "创建变更单追溯关系出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更单追溯关系出错 "+ e.getMessage());
        }
        return ResultJson("ids",str+">"+changeOrderId);
    }

    public String addChangeOrder(JSONObject jsonData,MKSCommand mks){
        String Item_ID = IsNull(jsonData.get("Item_ID"));
        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        dataMap.put("project",IsNull(jsonData.get("project")));
        String str = "";
        try {
            str = mks.createIssue("Change Order",dataMap,richDataMap);
            log.info("创建的变更单id ：" + str);
        } catch (APIException e) {
            log.info("error: " + "创建变更单出错！"+ e.getMessage());
            throw new MsgArgumentException("201","创建变更单出错 "+ e.getMessage());
        }
        return str;
    }

    //变更增删改查
    @RequestMapping(value="/changeAction", method = RequestMethod.POST)
    public JSONObject changeAction(@RequestBody JSONObject jsonData){
        JSONObject resultData = new JSONObject();
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        String issue_Type = IsNull(jsonData.get("issue_Type"));//创建文档类型或创建条目类型(创建时必须)
        String action_Type = IsNull(jsonData.get("action_Type"));//创建、更新、删除或移动
        String Parent_ID = IsNull(jsonData.get("Parent_ID"));//SystemWeaver中父节点唯一标识(创建移动时必须)
        //未传递设置默认值Requirement
        String Category = IsNull(jsonData.get("Category"))==""?"Requirement":IsNull(jsonData.get("Category"));
        String Before_ID = IsNull(jsonData.get("Before_ID"));//SystemWeaver中前节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        String After_ID = IsNull(jsonData.get("After_ID"));//SystemWeaver中后节点唯一标识，用以定位数据在系统中位值(创建移动时必须)
        //在已经建立的追溯关系中删除 12223,12234
        String Delete_Trace_ID = IsNull(jsonData.get("Delete_Trace_ID"));
        // SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
        String Trace_ID = IsNull(jsonData.get("Trace_ID"));//
        String Attachments = IsNull(jsonData.get("Attachments"));//传递附件
        String Comment = IsNull(jsonData.get("Comment"));//评论
        String id = IsNull(jsonData.get("id"));//变更id
        String Summary = IsNull(jsonData.get("Summary"));//变更标题
        String parentID = IsNull(jsonData.get("parentID"));//移动到父id下面
        //移动到父id下面的位置 参数 first  last  before:name  after:name
        String insertLocation = IsNull(jsonData.get("insertLocation"));
        String ids = IsNull(jsonData.get("ids"));//需要移动的id 多个空格 1 2 3

        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段
        //创建文档需要的参数
        if(action_Type.equals("add")){
            //创建时必须参数
            String docType = new AnalysisXML().resultType(issue_Type);
            Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
            for(String key : map.keySet()){
                String strKey = IsNull(jsonData.get(key));
                if(!strKey.equals("")){
                    dataMap.put(map.get(key),strKey);
                }
            }
            try {
                String str = mks.createIssue(docType,dataMap,richDataMap);
                log.info("创建的变更id ：" + str);
                resultData = ResultStr("DOC_ID",str);
            } catch (APIException e) {
                log.info("error: " + "创建变更出错！"+ e.getMessage());
                throw new MsgArgumentException("201","创建变更出错 "+ e.getMessage());
            }
        }else if(action_Type.equals("update")){
            Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
            for(String key : map.keySet()){
                String strKey = IsNull(jsonData.get(key));
                if(!strKey.equals("")){
                    dataMap.put(map.get(key),strKey);
                }
            }

            try {
                mks.editIssue(id,dataMap,richDataMap);
                log.info("修改变更id ：" + id);
                resultData = ResultStr("msg","Success");
            } catch (APIException e) {
                log.info("error: " + "修改变更出错！"+ e.getMessage());
                throw new MsgArgumentException("201","修改变更出错 "+ e.getMessage());
            }
        }else if(action_Type.equals("delete")){

            try {
                mks.deleteissue(id);
                log.info("删除变更id ：" + id);
            } catch (APIException e) {
                log.info("error: " + "删除变更出错！"+ e.getMessage());
                throw new MsgArgumentException("201","删除变更出错 "+ e.getMessage());
            }
            resultData = ResultStr("msg","Success");
        }
        mks.close(host,port,loginName);
        return resultData;
//        return ResultStr("msg","Success");
    }

    //测试
    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test(@PathParam("str1") String str1){
        Cache cache = cacheManager.getCache("orgCodeFindAll");
        cache.remove("swid");
        cache.put(new Element("swid",str1));
//        JSONObject str = (JSONObject)cache.get(str1).getObjectValue();
        String str = cache.get("swid").getObjectValue().toString();
//
        return str;
    }
    //测试
    @RequestMapping(value="/test2", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public JSONObject test2(@PathParam("text") InputStream text){
        String str = filePath + "\\123456.rtf";
        String str1 = filePath + "\\123456";


        //如果文件夹不存在则创建
        if  (!new File(filePath) .exists()  && !new File(filePath) .isDirectory())
        {
            new File(filePath) .mkdir();
        }
        //没有文件就创建
        if(!new File(str).exists()){
            try {
                new File(str).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new ConvertRTFToHtml().sc(text,str);//输入流保存到本地
        new ConvertRTFToHtml().RTFToHtml(str,str1);//本地rtf文件转换为html
        String htmldata = null;//获取html中元素
        try {
            htmldata = new ConvertRTFToHtml().readHtml(str1+".htm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cache cache = cacheManager.getCache("orgCodeFindAll");
        cache.put(new Element("swid",htmldata));
        return ResultStr("msg","1");
    }

    //输入流转换html string
    public String rtfString(InputStream text,String uuid){
        String str = filePath + "\\"+ uuid +".rtf";
        String str1 = filePath + "\\" + uuid;


        //如果文件夹不存在则创建
        if  (!new File(filePath) .exists()  && !new File(filePath) .isDirectory())
        {
            new File(filePath) .mkdir();
        }
        //没有文件就创建
        if(!new File(str).exists()){
            try {
                new File(str).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new ConvertRTFToHtml().sc(text,str);//输入流保存到本地
        new ConvertRTFToHtml().RTFToHtml(str,str1);//本地rtf文件转换为html
        String htmldata = null;//获取html中元素
        try {
            htmldata = new ConvertRTFToHtml().readHtml(str1+".htm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmldata;
    }

    //接受输入流转存本地 编辑上传附件
    public JSONObject test3(JSONObject jsonObject,String id,MKSCommand mks){
        InputStream fj = (InputStream)jsonObject.get("fileContent");
        String fileNmae = jsonObject.get("fileName").toString();
        String fileType = jsonObject.get("fileType").toString();
        String attachmentFile = jsonObject.get("attachmentFile").toString();
        String str = filePath + "\\"+fileNmae+"."+fileType;

        //如果文件夹不存在则创建
        if  (!new File(filePath) .exists()  && !new File(filePath) .isDirectory()) {
            new File(filePath) .mkdir();
        }
        //没有文件就创建
        if(!new File(str).exists()){
            try {
                new File(str).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new ConvertRTFToHtml().sc(fj,str);//输入流保存到本地
        Attachment attachment = new Attachment();
        attachment.setName(fileNmae+"."+fileType);
        attachment.setPath(str);
        try {
            mks.addAttachment(id,attachment,attachmentFile);
        } catch (APIException e) {
            log.info("上传附件出错: "+id +"("+e.getMessage()+")");
            e.printStackTrace();
        }

        return ResultStr("200","1111");
    }


    public static void main(String[] str){
//        MKSCommand mks = new MKSCommand();
//        mks.initMksCommand("192.168.120.128",7001,"admin","admin");
////        String isNO = mks.getProjectNameById("22324");
//        String id = mks.getDocIdsByType("SW_ID","85125614","type");
        JSONObject j = new JSONObject();
        j.put("cs",new String[]{});
        String[] s = (String[])j.get("cs");
        System.out.println(s.length);
    }
}
