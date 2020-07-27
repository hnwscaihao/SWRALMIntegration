package com.sw.SWAPI.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.AnalysisXML;
import com.sw.SWAPI.util.ConvertRTFToHtml;
import com.sw.SWAPI.util.MKSCommand;
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
import static com.sw.SWAPI.util.ResultJson.ResultStr;

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


    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/16 15:33
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception   获取ALM中所有用户信息
     */
    @RequestMapping(value="/getAllUsers", method = RequestMethod.GET)
    public JSONObject getAllUsers(){
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        List<User> allUsers = new ArrayList<>();
        try {
            allUsers = mks.getAllUsers(Arrays.asList("fullname","name","Email"));
        } catch (APIException e) {
            log.info("error: " + "查询所有用户错误！");
            e.printStackTrace();
        }

        mks.close();

        JSONObject resultData = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<allUsers.size();i++){
            JSONObject jsonObject = new JSONObject();
            User user = allUsers.get(i);
            jsonObject.put("userName",user.getUserName());
            jsonObject.put("login_ID",user.getLogin_ID());
            jsonObject.put("email",user.getEmail());
            jsonArray.add(jsonObject);
        }
        resultData.put("data", jsonArray);

        JSONObject result = new JSONObject();
        result.put("status",200);
        result.put("body", resultData);
        return result;
    }

    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/17 14:53
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception 获取ALM中Project列表
     */
    @RequestMapping(value="/getAllProject", method = RequestMethod.GET)
    public JSONObject getAllProject(){
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand(host,port,loginName,passWord);

        List<Project> allUsers = new ArrayList<>();
        try {
            allUsers = mks.getAllprojects(Arrays.asList("backingIssueID","name"));
        } catch (APIException e) {
            log.info("error: " + "查询所有project错误！");
            e.printStackTrace();
        }

        mks.close();

        JSONObject resultData = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<allUsers.size();i++){
            JSONObject jsonObject = new JSONObject();
            Project project = allUsers.get(i);
            jsonObject.put("project",project.getProject());
            jsonObject.put("PID",project.getPID());
            jsonArray.add(jsonObject);
        }
        resultData.put("data", jsonArray);

        JSONObject result = new JSONObject();
        result.put("status",200);
        result.put("body", resultData);
        return result;
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
        Cache cache = cacheManager.getCache("orgCodeFindAll");

         String item_Owner = IsNull(jsonData.get("item_Owner"));//SystemWeaver需求最后撰写人

         String Attachments = IsNull(jsonData.get("Attachments"));//传递附件

         String text = IsNull(jsonData.get("text"));//rtf数据量
         String end = IsNull(jsonData.get("end"));//结尾标记，标识本次文档数据传输完毕

        //移动到父id下面的位置 参数 first  last  before:name  after:name
//        String insertLocation = IsNull(jsonData.get("insertLocation"));

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
                String resultStr = integritymks(jsonObject1,mks);
            }
            mks.close();
            cache.removeAll();
        }
        return ResultStr("msg","Success");
    }

    //文档删改查
    public String integritymks(JSONObject jsonData,MKSCommand mks){
        JSONObject resultData = new JSONObject();
        String str = "";
        //状态字段
        String action_Type = IsNull(jsonData.get("action_Type"));//创建、更新、删除或移动
        String issue_Type = IsNull(jsonData.get("issue_Type"));//创建文档类型或创建条目类型(创建时必须)

        //id
        String id = IsNull(jsonData.get("id"));//文档id
        String parentID = IsNull(jsonData.get("Parent_ID"));//SystemWeaver中父节点唯一标识(创建移动时必须)
        String ids = IsNull(jsonData.get("ids"));//需要移动的id 多个空格 1 2 3
        String DOC_UUID = IsNull(jsonData.get("DOC_UUID"));

        //在已经建立的追溯关系中删除 12223,12234
        String Delete_Trace_ID = IsNull(jsonData.get("Delete_Trace_ID"));
        // SWR Handle ID在ALM查找对应的需求，并与当前需求建立追溯 12223,12234
        String Trace_ID = IsNull(jsonData.get("Trace_ID"));//

        Map<String,String> dataMap = new HashMap<>();//普通字段
        Map<String,String> richDataMap = new HashMap<>();//富文本字段

        if(action_Type.equals("add")){
            if(issue_Type.indexOf("Document") > -1){ //创建文档
                //创建文档时必须参数
                String docType = new AnalysisXML().resultType(issue_Type);
                Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
                for(String key : map.keySet()){
                    String strKey = IsNull(jsonData.get(key));
                    if(!strKey.equals("")){
                        dataMap.put(map.get(key),strKey);
                    }
                }

                try {
                    str = mks.createDocument(docType,dataMap);
                    log.info("创建的文档id： "+str);
                } catch (APIException e) {
                    log.info("error: " + "创建文档出错！"+ e.getMessage());
                    throw new MsgArgumentException("201","创建文档出错 "+ e.getMessage());
                }

            }else {
                //创建文档条目时必须参数
                String docType = new AnalysisXML().resultType(issue_Type);
                Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
                for(String key : map.keySet()){
                    String strKey = IsNull(jsonData.get(key));
                    if(!strKey.equals("")){
                        if(key.equals("Text")){
                            InputStream text = (InputStream)jsonData.get(key);
                            String htmlStr = new AlmController().rtfString(text,DOC_UUID);
                            richDataMap.put("Text",htmlStr);
                        } else {
                            dataMap.put(map.get(key),strKey);
                        }
                    }
                }

                try {
                    str = mks.createcontent(docType,parentID,dataMap);
                    log.info("创建的条目id： "+str);

                } catch (APIException e) {
                    log.info("error: " + "创建文档出错！"+ e.getMessage());
                    throw new MsgArgumentException("201","创建文档出错 "+ e.getMessage());
                }
            }

        }else if(action_Type.equals("update")){
            //修改时参数
            Map<String,String> map =  new AnalysisXML().resultFile(issue_Type);
            for(String key : map.keySet()){
                String strKey = IsNull(jsonData.get(key));
                if(!strKey.equals("")){
                    if(key.equals("Text")){
                        InputStream text = (InputStream)jsonData.get(key);
                        String htmlStr = new AlmController().rtfString(text,DOC_UUID);
                        richDataMap.put("Text",htmlStr);
                    } else {
                        dataMap.put(map.get(key),strKey);
                    }
                }
            }

            try {
                mks.editIssue(id,dataMap,richDataMap);
                log.info("修改的文档： "+id);
            } catch (APIException e) {
                log.info("error: " + "修改文档出错！"+ e.getMessage());
                throw new MsgArgumentException("201","修改文档出错 "+ e.getMessage());
            }
        }else if(action_Type.equals("delete")){
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
        }else if(action_Type.equals("move")){
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
        mks.close();
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

}
