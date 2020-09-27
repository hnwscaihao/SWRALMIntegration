package com.sw.SWAPI.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import java.util.*;

import static com.sw.SWAPI.util.ResultJson.*;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value = "/alm")
public class AlmController {


    public static final Log log = LogFactory.getLog(AlmController.class);


    String filePath = "C:\\\\Program Files\\\\Integrity\\\\ILMServer12\\\\data\\\\tmp";

    @Value("${token}")
    private String token;

    private MKSCommand mks;

    private IntegrityUtil util = new IntegrityUtil();

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
        List<String> dynamicGroups = Arrays.asList(AnalysisXML.getTypeFilterGroup(type).split(","));

//		if ("Component Requirement Specification Document".equals(type)) {// Component到In
//			dynamicGroups.add("Project Team");
////			dynamicGroups.add("Review Committee Leader DG");
//		} else {// 其他到In Approve，查询Project Manager DG
//			dynamicGroups.add("Project Manager DG");
//		}

        log.info("project-----" + project);
        // MKSCommand mks = new MKSCommand();
        // mks.initMksCommand("192.168.120.128", 7001, "admin", "admin");
        if (mks == null) {
            mks = new MKSCommand();
        }
        List<User> allUsers = new ArrayList<User>();
        try {
            allUsers = mks.getProjectDynaUsers(project, dynamicGroups);
        } catch (APIException e) {
            log.error("error: " + "查询所有用户错误！" + e.getMessage());
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
            log.error("error: " + "查询所有project错误！" + e.getMessage());
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

        // docID = ""; //文档id
        if (mks == null) {
            mks = new MKSCommand();
        }
//		log.info(jsonData);
        String docUUID = jsonData.getString(Constants.DOC_UUID);
        String end = jsonData.get("end").toString();// 结尾标记，标识本次文档数据传输完毕
        // 移动到父id下面的位置 参数 first last before:name after:name
        // String insertLocation = jsonData.get("insertLocation"));
        if ("true".equals(end)) { // 所有参数存入缓存
            log.info("-------------数据下发 缓存完毕 UUID " + docUUID + "-------------");
            MapCache.cacheVal(docUUID, jsonData);
            List<JSONObject> listData = MapCache.getList(docUUID);// 保存排序后条目数据
            MapCache.clearCache(docUUID);

            String info = util.checkData(listData);
            log.warn("校验数据：" + info);
            if (!"success".equals(info)) {
                return ResultJson("data", info);
            }
            try {
            	IntegrityCallable call = new IntegrityCallable(listData);
                Thread t = new Thread(call);
                t.start();
            } catch (Exception e) {
                log.error("多线程错误：" + e.getMessage());
                throw new MsgArgumentException("210", e.getMessage());
            }
            log.info("返");
        } else {
            log.info("-------------数据下发 缓存中 UUID " + docUUID + "-------------");
            MapCache.cacheVal(docUUID, jsonData);
            return ResultJson("data", "");
        }
        return ResultJson("data", "");
    }

    // 变更反馈增删改条目
    @RequestMapping(value = "/changeAction", method = RequestMethod.POST)
    public JSONObject changeAction1(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", util.changeExecution(jsonData)));
        log.info("变更完成：");
        return jsonObject;
    }


    /**
     * token验证
     */
    public void getToken(String str) {
        if (token.equals(str)) {
            return;
        } else {
            log.error("token验证失败!");
            throw new MsgArgumentException("201", "token Validation failed!");
        }
    }


    public static void main(String[] str) {

    }
}
