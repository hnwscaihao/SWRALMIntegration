package com.sw.SWAPI.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.CmdRunner;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
//import sun.misc.BASE64Decoder;

import static com.sw.SWAPI.util.ResultJson.*;

import java.util.*;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value = "/alm")
//@CrossOrigin(origins = "*",maxAge = 3600,allowedHeaders = {"Content-Type"})
public class AlmController {

    public static final Log log = LogFactory.getLog(AlmController.class);

    @Value("${token}")
    private String token;

    @Value("${host}")
    private String host;

    @Value("${ce_host}")
    private String testHost;

    private MKSCommand mks;

    private IntegrityUtil util = new IntegrityUtil();

    /**
     * 查询所有用户
     *
     * @Author liuxiaoguang
     * @Date 2020/7/16 15:33
     * @Param []
     * @Return com.alibaba.fastjson.JSONArray
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
     * 根据Type和Project查询用户组
     *
     * @param jsonData
     * @return com.alibaba.fastjson.JSONArray
     */
    @RequestMapping(value = "/getAllUsersByProject", method = RequestMethod.POST)
    public JSONArray getAllUsers(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        String project = jsonData.getString("project");
        // 根据类型判断获取的动态组，Component获取Review
        String type = jsonData.getString("type");
        List<String> dynamicGroups = Arrays.asList(AnalysisXML.getTypeFilterGroup(type).split(","));
        log.info("project-----" + project);
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
     * 查询所有有效的项目
     *
     * @Author liuxiaoguang
     * @Date 2020/7/17 14:53
     * @Param []
     * @Return com.alibaba.fastjson.JSONArray
     * @Exception 获取ALM中Project列表
     */
    @RequestMapping(value = "/getAllProjects", method = RequestMethod.POST)
    public JSONArray getAllProject(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        log.info("-------------查询所用项目-------------");
        if (mks == null) {
            mks = new MKSCommand();
        }
        List<Project> allProjects = new ArrayList<Project>();
        try {
            allProjects = mks.getAllprojects(Arrays.asList("backingIssueID", "name"));
        } catch (APIException e) {
            log.error("error: " + "查询所有project错误！" + e.getMessage());
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < allProjects.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            Project project = allProjects.get(i);
            jsonObject.put("project", project.getProject());
            jsonObject.put("PID", project.getPID());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 下发数据
     *
     * @Author liuxiaoguang
     * @Date 2020/7/22 10:02
     * @Param [jsonData]
     * @Return com.alibaba.fastjson.JSONObject
     * @Exception 创建 修改 删除 移动文档条目
     */
    @RequestMapping(value = "/releaseData", method = RequestMethod.POST)
    public JSONObject createDocument(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));

        if (mks == null) {
            mks = new MKSCommand();
        }
        String docUuid = jsonData.getString(Constants.DOC_UUID);
        // 结尾标记，标识本次文档数据传输完毕
        String end = jsonData.get("end").toString();
        // 移动到父id下面的位置 参数 first last before:name after:name
        // 所有参数存入缓存
        if (Constants.TRUE_STRING.equals(end)) {
            log.info("-------------数据下发 缓存完毕 UUID " + docUuid + "-------------");
            MapCache.cacheVal(docUuid, jsonData);
            // 保存排序后条目数据
            List<JSONObject> listData = MapCache.getList(docUuid);
            MapCache.clearCache(docUuid);

            String info = util.checkData(listData);
            log.warn("校验数据：" + info);
            if (!Constants.SUCCESS.equals(info)) {
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
        } else {
            log.info("-------------数据下发 缓存中 UUID " + docUuid + "-------------");
            MapCache.cacheVal(docUuid, jsonData);
            return ResultJson("data", "");
        }
        return ResultJson("data", "");
    }

    /**
     * 变更执行
     *
     * @param jsonData
     * @return com.alibaba.fastjson.JSONObject
     */
    @RequestMapping(value = "/changeAction", method = RequestMethod.POST)
    public JSONObject changeAction1(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", ResultJson("DOC_ID", util.changeExecution(jsonData)));
        log.info("变更完成：");
        return jsonObject;
    }

    /**
     * 同步用户信息到MKS Domain
     *
     * @param jsonData
     * @return
     */
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    public JSONObject updateUserInfo(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        JSONArray users = jsonData.getJSONArray("Users");
        // 生产环境
        if (mks == null) {
            mks = new MKSCommand();
        }
        try {
            for (Object user : users) {
                mks.updateUserInfo((JSONObject) JSONObject.toJSON(user), null);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MsgArgumentException("202", e.getMessage());
        }
        log.info("正式：" + host);
        log.info("测试：" + testHost);
        if (!host.equals(testHost)) {
            // 测试环境
            CmdRunner cmdRunner = mks.getCmdRunner();
            try {
                for (Object user : users) {
                    mks.updateUserInfo((JSONObject) JSONObject.toJSON(user), cmdRunner);
                }

            } catch (Exception e) {
                log.error(e.getMessage());
                throw new MsgArgumentException("203", e.getMessage());
            } finally {
                mks.closeCmdRunner(cmdRunner);
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "200");
        jsonObject.put("message", "success");
        return jsonObject;
    }
    
    /**
     * 记录用户密码信息
     *
     * @param jsonData
     * @return
     */
	@RequestMapping(value = "/recordUserInfo", method = RequestMethod.POST)
	public JSONObject recordUserInfo(@RequestBody JSONObject jsonData) {
		getToken(jsonData.getString("Access_Token"));
		try {
			if (mks == null) {
	            mks = new MKSCommand();
	        }
			String loginId = jsonData.getString("loginId");
			String pwd = jsonData.getString("password");
			if(loginId == null || "".equals(loginId)) {
				throw new MsgArgumentException("201", "用户Login ID为空，无法进行记录");
			}
			if(pwd == null || "".equals(pwd)) {
				throw new MsgArgumentException("202", "用户PWD为空，无法进行记录");
			}
			loginId = loginId.trim();
			pwd = pwd.trim();
			mks.updateUserRecord(loginId, pwd);
		} catch (APIException e) {
			log.error(APIExceptionUtil.getMsg(e));
			throw new MsgArgumentException("203", "用户信息记录失败，" + APIExceptionUtil.getMsg(e));
		} catch (MsgArgumentException e) {
			log.error(e.toString());
			throw e;
		} catch (Exception e) {
			log.error(e);
			throw new MsgArgumentException("203", "用户信息记录失败" + e);
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "200");
		jsonObject.put("message", "success");
		return jsonObject;
	}

    /**
     * 从MKS Domain删除用户
     *
     * @param jsonData
     * @return
     */
    @RequestMapping(value = "deleteUsers", method = RequestMethod.POST)
    public JSONObject deleteUsers(@RequestBody JSONObject jsonData) {
        getToken(jsonData.getString("Access_Token"));
        JSONArray userIds = jsonData.getJSONArray("UserId");
        if (!(userIds.size() > 0)) {
            log.error("删除的id不能为空!");
            throw new MsgArgumentException("202", "删除的id不能为空!");
        }
        // 生产环境
        if (mks == null) {
            mks = new MKSCommand();
        }
        try {
            mks.deleteUsers(userIds, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MsgArgumentException("202", e.getMessage());
        }
        log.info("正式：" + host);
        log.info("测试：" + testHost);
        if (!host.equals(testHost)) {
            // 测试环境
            CmdRunner cmdRunner = mks.getCmdRunner();
            try {
                mks.deleteUsers(userIds, cmdRunner);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new MsgArgumentException("203", e.getMessage());
            } finally {
                mks.closeCmdRunner(cmdRunner);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "200");
        jsonObject.put("message", "success");
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

    @RequestMapping(value = "/getUserPWD", method = RequestMethod.POST)
    public JSONObject getUserPassword(@RequestBody JSONObject jsonData) {
        JSONObject jsonObject = new JSONObject();
        getToken(jsonData.getString("Access_Token"));
        String loginId = jsonData.getString("loginId");
        if (mks == null) {
            mks = new MKSCommand();
        }
        if ("".equals(loginId) || loginId == null) {
            log.error("用户登录ID为空");
            throw new MsgArgumentException("201", "用户登录Login ID为空");
        }
        try {
        	String userBase64Password = mks.getUserPWD(loginId);
            if (userBase64Password == null){
                log.error("首次登录，请注册您的域密码");
                throw new MsgArgumentException("202", "首次登录，请注册您的域密码");
            }
            jsonObject.put("data", userBase64Password);
        } catch(MsgArgumentException e) {
			throw e;
		} catch (Exception e) {
            log.error(e.getMessage());
            throw new MsgArgumentException("204", "查询异常，请联系管理员");
        }
        jsonObject.put("status", "200");
        
        return jsonObject;
    }

}
