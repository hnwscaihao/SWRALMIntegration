package com.sw.SWAPI.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.*;
import com.mks.api.response.*;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.controller.AlmController;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.Attachment;

import connect.Connection;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: System Weaver集成API接口
 */
@Component
public class MKSCommand {

    /** 日志记录*/
    ;
    private static final Log logger = LogFactory.getLog(MKSCommand.class);
    private Session mksSession = null;
    private IntegrationPointFactory mksIpf = null;
    private IntegrationPoint mksIp = null;
    private static CmdRunner mksCmdRunner = null;
    private Command mksCommand = null;
    private Response mksResponse = null;
    private boolean success = false;
    private String currentCommand;
    private String hostname = null;
    private int port = 7001;
    private String user;
    private String password;
    private int APIMajor = 4;
    private int APIMinor = 16;
    private static String errorLog;
    private static final String FIELDS = "fields";
    private static final String CONTAINS = "Contains";
    private static final String PARENT_FIELD = "Contained By";
    private static final String DOCUMENT_SHORT_TITLE_FIELD = "Document Short Title";

    public static final Map<String, String> ENVIRONMENTVAR = System.getenv();
    public static MKSCommand cmd;
    public static List<String> tsIds = new ArrayList<String>();
    private static String longinUser;
    /**
     * fromat
     */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final Pattern LASTVALUE = Pattern.compile("\\(([^\\)]+)\\)");

    public static Connection conn;
    //    private static Connection conn = new Connection();
    //AES算法的KEY；
    private static final String KEY = "ABCDEFGHIJKLMNOP";

    public MKSCommand() {
//        new IntegrityFactory();
    }

    public MKSCommand(String hostname, int port, String user, String password, int apimajor, int apiminor) {
        hostname = hostname;
        port = port;
        user = user;
        password = password;
//		createSession();
        getSession();
    }

    public MKSCommand(String[] args) {
        hostname = args[0];
        port = Integer.parseInt(args[1]);
        user = args[2];
        password = args[3];
        APIMajor = Integer.parseInt(args[4]);
        APIMinor = Integer.parseInt(args[5]);
        createSession();
    }

    public CmdRunner getCmdRunner() {
        try {
            Properties prop = new Properties();
//            prop.load(MKSCommand.class.getClassLoader().getSystemResourceAsStream("sw.properties"));
            prop.load(ClassLoader.getSystemResourceAsStream("sw.properties"));
            String ce_host = prop.getProperty("ce_host");
            String user = prop.getProperty("ce_user");
            String password = prop.getProperty("ce_password");
            IntegrationPointFactory instance = IntegrationPointFactory.getInstance();
            IntegrationPoint mksIp = instance.createIntegrationPoint(ce_host, port, APIMajor, APIMinor);
            Session session = mksIp.createSession(user, password);
            CmdRunner cmdRunner = session.createCmdRunner();
            cmdRunner.setDefaultHostname(ce_host);
            cmdRunner.setDefaultPort(port);
            cmdRunner.setDefaultUsername(user);
            cmdRunner.setDefaultPassword(password);
            return cmdRunner;
        } catch (Exception e) {
            logger.info("错误：" + e.getMessage());
        }
        return null;
    }

    public void closeCmdRunner(CmdRunner cmdRunner) {
        try {
            Properties prop = new Properties();
//            prop.load(MKSCommand.class.getClassLoader().getSystemResourceAsStream("sw.properties"));
            prop.load(ClassLoader.getSystemResourceAsStream("sw.properties"));
            String ce_host = prop.getProperty("ce_host");
            String user = prop.getProperty("ce_user");
            Command cmd = new Command("aa", "disconnect");
            cmd.addOption(new Option("hostname", ce_host));
            cmd.addOption(new Option("port", String.valueOf(port)));
            cmd.addOption(new Option("user", user));
            cmdRunner.execute(cmd);
            logger.info("断开链接," + ce_host + ":" + port);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    public void setCmd(String type, String cmd, ArrayList<Option> ops, String sel) {
        mksCommand = new Command(type, cmd);
        String cmdStrg = (new StringBuilder(String.valueOf(type))).append(" ").append(cmd).append(" ").toString();
        if (ops != null && ops.size() > 0) {
            for (int i = 0; i < ops.size(); i++) {
                cmdStrg = (new StringBuilder(String.valueOf(cmdStrg))).append(ops.get(i).toString()).append(" ")
                        .toString();
                // Option o = new Option(_ops.get(i).toString());
                mksCommand.addOption(ops.get(i));
            }

        }
        if (sel != null && sel != "") {
            cmdStrg = (new StringBuilder(String.valueOf(cmdStrg))).append(sel).toString();
            mksCommand.addSelection(sel);
        }
        currentCommand = cmdStrg;
        // logger.info((new StringBuilder("Command:
        // ")).append(cmdStrg).toString());
    }

    public String getCommandAsString() {
        return currentCommand;
    }

    public boolean getResultStatus() {
        return success;
    }

    public String getConnectionString() {
        String c = (new StringBuilder(String.valueOf(hostname))).append(" ").append(port).append(" ").append(user)
                .append(" ").append(password).toString();
        return c;
    }

    public void exec() {
        success = false;
        try {
            mksResponse = conn.execute(mksCommand);
            // logger.info((new StringBuilder("Exit Code:
            // ")).append(mksResponse.getExitCode()).toString());
            success = true;
        } catch (APIException ae) {
            logger.error(ae.getMessage());
            success = false;
            errorLog = ae.getMessage();
            JOptionPane.showMessageDialog(null, "错误代码：MKS 122", "提示", 1);
            System.exit(0);
        } catch (NullPointerException npe) {
            success = false;
            logger.error(npe.getMessage());
            errorLog = npe.getMessage();
            JOptionPane.showMessageDialog(null, "错误代码：MKS 128", "提示", 1);
            System.exit(0);
        }
    }

    public void release() throws IOException {
        try {
            if (mksSession != null) {
                mksCmdRunner.release();
                mksSession.release();
                mksIp.release();
                mksIpf.removeIntegrationPoint(mksIp);
            }
            success = false;
            currentCommand = "";
        } catch (APIException ae) {
            logger.error(ae.getMessage());
            JOptionPane.showMessageDialog(null, "错误代码：MKS 145", "提示", 1);
            System.exit(0);
        }
    }

    public void getSession() {
        try {
            mksIpf = IntegrationPointFactory.getInstance();
            mksIp = mksIpf.createLocalIntegrationPoint(APIMajor, APIMinor);
            mksIp.setAutoStartIntegrityClient(true);
            mksSession = mksIp.getCommonSession();
            mksCmdRunner = mksSession.createCmdRunner();
            mksCmdRunner.setDefaultUsername(user);
            mksCmdRunner.setDefaultPassword(password);
            mksCmdRunner.setDefaultHostname(hostname);
            mksCmdRunner.setDefaultPort(port);
        } catch (APIException ae) {
            logger.error("链接失败！！！！！！！！！！！！！！！！！！！！！！！");
            logger.error(ae.toString());
        }
    }

    @SuppressWarnings("deprecation")
    public void createSession() {
        try {
            mksIpf = IntegrationPointFactory.getInstance();
            mksIp = mksIpf.createIntegrationPoint(hostname, port, APIMajor, APIMinor);
            mksSession = mksIp.createSession(user, password);
            mksCmdRunner = mksSession.createCmdRunner();
            mksCmdRunner.setDefaultHostname(hostname);
            mksCmdRunner.setDefaultPort(port);
            mksCmdRunner.setDefaultUsername(user);
            mksCmdRunner.setDefaultPassword(password);
        } catch (APIException ae) {
            logger.error(ae.getMessage());
        }
    }

    public String[] getResult() {
        String result[] = null;
        int counter = 0;
        try {
            WorkItemIterator mksWii = mksResponse.getWorkItems();
            result = new String[mksResponse.getWorkItemListSize()];
            while (mksWii.hasNext()) {
                WorkItem mksWi = mksWii.next();
                Field mksField;
                for (Iterator<?> mksFields = mksWi.getFields(); mksFields.hasNext(); ) {
                    mksField = (Field) mksFields.next();
                    result[counter] = mksField.getValueAsString();
                }

                counter++;
            }
        } catch (APIException ae) {
            logger.error(ae.toString(), ae);
            JOptionPane.showMessageDialog(null, ae.toString(), "ERROR", 0);
        } catch (NullPointerException npe) {
            logger.error(npe.toString());
            JOptionPane.showMessageDialog(null, npe.toString(), "ERROR", 0);
        }
        return result;
    }

    /**
     * 根据Ids查询字段的值
     *
     * @param ids
     * @param fields
     * @return
     * @throws APIException
     */
    public List<Map<String, String>> getItemByIds(List<String> ids, List<String> fields) throws APIException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        SelectionList sl = new SelectionList();
        for (String id : ids) {
            String splitID = null;
            if (id.startsWith("[") && id.endsWith("]")) {
                splitID = id.substring(id.indexOf("[") + 1, id.indexOf("]"));
                sl.add(splitID.trim());
            } else if (id.startsWith("[")) {
                splitID = id.substring(id.indexOf("[") + 1, id.length());
                sl.add(splitID.trim());
            } else if (id.endsWith("]")) {
                splitID = id.substring(0, id.indexOf("]"));
                sl.add(splitID.trim());
            } else if (id.startsWith(" ")) {
                splitID = id.substring(1, id.length());
                sl.add(splitID.trim());
            } else {
                sl.add(id.trim());
            }
        }
        cmd.setSelectionList(sl);

        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    map.put(field, value);
                }
                list.add(map);
            }
        } catch (APIException e) {
            // success = false;
            logger.error(e.getMessage());
            JOptionPane.showMessageDialog(null, "错误代码：MKS 268", "提示", 1);
            System.exit(0);
            throw e;
        }
        return list;
    }

    /**
     * @param ids
     * @param fields
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> searchById(List<String> ids, List<String> fields) throws APIException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        SelectionList sl = new SelectionList();
        for (String id : ids) {
            sl.add(id);
        }
        cmd.setSelectionList(sl);

        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    map.put(field, value);
                }
                list.add(map);
            }
        } catch (APIException e) {
            AlmController.log.info(APIExceptionUtil.getMsg(e));
            throw e;
        }
        return list;
    }


    /**
     * 根据id查询单个结果
     *
     * @param id
     * @param field
     * @return
     * @throws APIException
     */
    public String getTypeById(String id, String field) throws APIException {
        String str = "";
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        mv.add(field);
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        cmd.addSelection(id);

        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                if (wi.getField(field) != null) {
                    str = wi.getField(field).getValueAsString();
                }
            }
        } catch (APIException e) {
            // success = false;
            logger.error(e.getMessage());
            throw e;
        }
        return str;
    }

    /**
     * @param id
     * @return
     * @throws APIException
     */
    public List<String> getSuiteById(String id, List<String> fields) throws APIException {
        List<String> list = new ArrayList<String>();
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        cmd.addSelection(id);

        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            String value = "";
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    value = wi.getField(field).getValueAsString();

                }
            }
            String[] ids = {};
            if (!"".equals(value) && value != null) {
                ids = value.split(",");
                for (int i = 0; i < ids.length; i++) {
                    list.add(shujz(ids[i]));
                }
            }

        } catch (APIException e) {
            // success = false;
            throw e;
        }
        return list;
    }

    /**
     * 过滤id中的英文 lxg
     *
     * @param a
     * @return
     */
    public String shujz(String a) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);
        return m.replaceAll("").trim();
    }


    public boolean getResultState() {
        return success;
    }

    public String getErrorLog() {
        return errorLog;
    }


    @Deprecated
    public List<Map<String, String>> getAllChild(List<String> ids, List<String> childs) throws APIException {
        List<Map<String, String>> itemByIds = getItemByIds(ids, Arrays.asList("ID", "Contains"));//查询文档id包含字段heading
        for (Map<String, String> map : itemByIds) { //
            String contains = map.get("Contains");
            String id = map.get("ID");
            map.put("ID", id);
            if (contains != null && contains.length() > 0) {
//				List<String> childIds = Arrays.asList(contains.replaceAll("ay", "").split(","));
                getAllChild(Arrays.asList(id), Arrays.asList(contains));
            }
        }
        return itemByIds;

    }

    public SelectionList contains(SelectionList documents) throws APIException {
        return relationshipValues(CONTAINS, documents);
    }

    public SelectionList relationshipValues(String fieldName, SelectionList ids) throws APIException {
        if (fieldName == null) {
            throw new APIException("invoke fieldValues() ----- fieldName is null.");
        }
        if (ids == null || ids.size() < 1) {
            throw new APIException("invoke fieldValues() ----- ids is null or empty.");
        }
        Command command = new Command(Command.IM, Constants.ISSUES);
        command.addOption(new Option(Constants.FIELDS, fieldName));
        command.setSelectionList(ids);
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        SelectionList contents = new SelectionList();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            ItemList il = (ItemList) wi.getField(fieldName).getList();
            if (il != null) {
                for (int i = 0; i < il.size(); i++) {
                    Item item = (Item) il.get(i);
                    String id = item.getId();
                    contents.add(id);
                }
            }
        }
        return contents;
    }


    /**
     * @param documentID
     * @return
     * @throws APIException
     */
    public List<String> allContainID(String documentID) throws APIException {
        List<String> allContainID = new ArrayList<String>();
        Command command = new Command("im", "issues");
        command.addOption(new Option(FIELDS, CONTAINS));
        command.addSelection(documentID);
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        SelectionList sl = new SelectionList();
        List<String> fields = new ArrayList<String>();
        fields.add("ID");
        while (it.hasNext()) {
            WorkItem wi = it.next();
            ItemList il = (ItemList) wi.getField(CONTAINS).getList();
            for (int i = 0; i < il.size(); i++) {
                Item item = (Item) il.get(i);
                String id = item.getId();
                sl.add(id);
            }
        }
        SelectionList contents = null;
        if (sl != null && sl.size() >= 1) {
            contents = contains(sl);
        }
        if (contents.size() > 0) {
            SelectionList contains = new SelectionList();
            contains.add(contents);
            while (true) {
                SelectionList conteins = contains(contains);
                if (conteins.size() < 1) {
                    break;
                }
                contents.add(conteins);
                contains = new SelectionList();
                contains.add(conteins);
            }
        }
        contents.add(sl);
        for (int i = 0; i < contents.size(); i++) {
            allContainID.add(contents.getSelection(i));
        }
        return allContainID;
    }

    public List<Map<String, String>> allContents(String document, List<String> fieldList) throws APIException, Exception {
        List<Map<String, String>> returnResult = new ArrayList<Map<String, String>>();
        Command command = new Command("im", "issues");
        command.addOption(new Option(FIELDS, CONTAINS));
        command.addSelection(document);
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        SelectionList sl = new SelectionList();
        List<String> fields = new ArrayList<String>();
        fields.add("ID");
        if (!fieldList.contains(PARENT_FIELD)) {//排序使用
            fieldList.add(PARENT_FIELD);
        }
        if (fieldList != null) {
            fields.addAll(fieldList);
        }
        while (it.hasNext()) {
            WorkItem wi = it.next();
            ItemList il = (ItemList) wi.getField(CONTAINS).getList();
            for (int i = 0; i < il.size(); i++) {
                Item item = (Item) il.get(i);
                String id = item.getId();
                sl.add(id);
            }
        }
        SelectionList contents = null;
        if (sl != null && sl.size() >= 1) {
            contents = contains(sl);

            if (contents.size() > 0) {
                SelectionList contains = new SelectionList();
                contains.add(contents);
                while (true) {
                    SelectionList conteins = contains(contains);
                    if (conteins.size() < 1) {
                        break;
                    }
                    contents.add(conteins);
                    contains = new SelectionList();
                    contains.add(conteins);
                }
            }
            contents.add(sl);
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            if (contents.size() > 500) {
                List<SelectionList> parallel = new ArrayList<SelectionList>();
                SelectionList ids = new SelectionList();
                for (int i = 0; ; i++) {
                    if (i % 500 == 0 && ids.size() > 0) {
                        parallel.add(ids);
                        ids = new SelectionList();
                    }
                    ids.add(contents.getSelection(i));
                    if (i + 1 == contents.size()) {
                        parallel.add(ids);
                        break;
                    }
                }
                for (SelectionList selectionList : parallel) {
                    list.addAll(queryIssues(selectionList, fields));
                }
            } else {
                list.addAll(queryIssues(contents, fields));
            }
            String beforeParentId = document;
            Integer startIndex = -1;
            List<String> idRecord = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> node = list.get(i);
                String parentId = node.get(PARENT_FIELD);
                if (parentId == null || "".equals(parentId) || parentId.equals(document)) {
                    node.put(PARENT_FIELD, document);
                    returnResult.add(node);
                    idRecord.add(node.get("ID"));
                }
            }
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> node = list.get(i);
                String parentId = node.get(PARENT_FIELD);
                if (parentId != null && !"".equals(parentId) && !parentId.equals(document)) {
                    if (!beforeParentId.equals(parentId)) {
                        beforeParentId = parentId;
                        startIndex = 1;
                    }
                    Integer parentIndex = idRecord.indexOf(parentId);
                    returnResult.add(parentIndex + startIndex, node);
                    idRecord.add(parentIndex + startIndex, node.get("ID"));
                    startIndex++;
                }
            }
        }
        return returnResult;
    }

    /**
     * 根据ID查询数据
     *
     * @param selectionList
     * @param fields
     * @return
     * @throws APIException
     * @throws Exception
     */
    public List<Map<String, String>> queryIssues(SelectionList selectionList, List<String> fields) throws APIException, Exception {
        List<Map<String, String>> returnResult = new ArrayList<Map<String, String>>();
        String dept = "";
        String trueType = " ";
        boolean needFilter = false;
        String category = "";
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        cmd.setSelectionList(selectionList);
        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    Field fieldObj = wi.getField(field);
                    String fieldType = fieldObj.getDataType();
                    String value = fieldObj.getValueAsString() != null ? fieldObj.getValueAsString().toString() : null;
                    value = parseDateVal(value, fieldType);
                    if (PARENT_FIELD.equals(field) && value != null
                            && value.contains("[") && value.contains("]")) {
                        value = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
                    }
                    if ("[]".equals(value)) {
                        value = null;
                    }
                    map.put(field, value);
                }
                boolean canAdd = true;
                if (needFilter) {
                    String currentCategory = map.get("Category");
                    if (!currentCategory.equals(category)) {
                        canAdd = false;
                    }
                }
                if (canAdd) {
                    returnResult.add(map);
                }
            }
        } catch (APIException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return returnResult;
    }

    public String getUserNames(String userId) throws APIException {
        if (userId != null && !"".equals(userId)) {
            List<String> listUser = new ArrayList<String>();
            listUser.add(userId);
            return getUserNames(listUser);
        } else {
            return "";
        }
    }

    public static String parseDateVal(String value, String fieldType) {
        if ("java.util.Date".equals(fieldType)) {
            value = FORMAT.format(new Date(value));
        }
        return value;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param cmdRunner
     * @throws APIException
     */
    public void updateUserInfo(JSONObject user, CmdRunner cmdRunner) throws APIException {
        //判断数据是否合法
        String id = user.getString("id");
        if ("".equals(id)) {
            logger.error("id不能为空!");
            throw new MsgArgumentException("202", "id不能为空!");
        }

        String name = user.getString("name");
        String email = user.getString("email");
        String password = user.getString("password");

        //查询是否存在
        Command cmd = new Command(Command.INTEGRITY, "mksdomainusers");
        Response res;
        if (cmdRunner != null) {
            res = cmdRunner.execute(cmd);
        } else {
            res = conn.execute(cmd);
        }
        List<String> usersId = new ArrayList<>();
        if (res != null) {
            WorkItemIterator wi = res.getWorkItems();
            while (wi.hasNext()) {
                WorkItem it = wi.next();
                usersId.add(it.getId());
            }
        }
        logger.warn("usersId个数:" + usersId.size());
        if (usersId.contains(id)) {
            //更新
            logger.info("更新id：" + id);
            Command command = new Command(Command.INTEGRITY, "editmksdomainuser");
            if ("".equals(email) && "".equals(name) && "".equals(password)) {
                logger.error("email,name,password 需有一个有值!");
                throw new MsgArgumentException("202", "email,name,password 需有一个有值!");
            }
            if (!"".equals(email)) {
                command.addOption(new Option("email", email));
            }
            if (!"".equals(name)) {
                command.addOption(new Option("fullName", name));
            }
            if (!"".equals(password)) {
                command.addOption(new Option("userPassword", password));
            }
            command.addSelection(id);
            if (cmdRunner != null) {
                cmdRunner.execute(command);
                logger.info("测试环境更新完成:" + id);
            } else {
                conn.execute(command);
                logger.info("正式环境更新完成:" + id);
            }
        } else {
            //新增
            if ("".equals(password)) {
                logger.error("password不能为空!");
                throw new MsgArgumentException("202", "password不能为空!");
            }
            if ("".equals(name)) {
                logger.error("name不能为空!");
                throw new MsgArgumentException("202", "name不能为空!");
            }
            logger.info("新增id：" + id);
            Command command = new Command(Command.INTEGRITY, "createmksdomainuser");
            if (!"".equals(email)) {
                command.addOption(new Option("email", email));
            }
            command.addOption(new Option("fullName", name));
            command.addOption(new Option("userPassword", password));
            command.addOption(new Option("loginID", id));
            if (cmdRunner != null) {
                cmdRunner.execute(command);
                logger.info("测试环境更新完成:" + id);
            } else {
                conn.execute(command);
                logger.info("正式环境更新完成:" + id);
            }
        }
    }

    /**
     * 记录用户loginId和密码
     *
     * @param loginId
     * @param pwd
     * @throws APIException
     */
    public void updateUserRecord(String loginId, String pwd) throws Exception {
        HashMap<String, String> userRecord = new HashMap<>();
        String queryCondition = "((field[Type] = User Record) and (field[Login ID] contains LIKE " + loginId + "))";// 定义查询User Record语句
        //base64加密，弃用;
        //String base64PWD = Base64.getEncoder().encodeToString(pwd.getBytes());
        //Aes算法加密
       // String base64PWD = Aes.aesEncrypt(pwd, KEY);
        //测试
        String base64PWD = pwd;
        logger.info(base64PWD);
        List<Map<String, String>> issueList = queryIssueByQuery(Arrays.asList("Login ID", "PWD", "ID", "Record_date"), queryCondition);
        Map<String, String> userMap = null;
        if (!issueList.isEmpty()) {//查询系统是否已有记录
            for (Map<String, String> map : issueList) {
                String existId = map.get("Login ID");
                if (existId.equals(loginId)) {
                    userMap = map;
                    break;
                }
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        if (userMap != null) {//查询到记录，更新
            String existBase64PWD = userMap.get("PWD");
            if (!existBase64PWD.equals(base64PWD)) {//系统已保存时，判断密码是否更新，如果更新，则更新数据，否则不更新
                userRecord.put("PWD", base64PWD);
                userRecord.put("Record_date", sdf.format(new Date()));
                editIssue(userMap.get("ID"), userRecord, null);
            }
        } else {//如果系统中没有记录数据，则创建新的
            userRecord = new HashMap<String, String>(1);
            userRecord.put("Record_date", sdf.format(new Date()));
            userRecord.put("Login ID", loginId);
            userRecord.put("PWD", base64PWD);
            createIssue("User Record", userRecord, null);
        }
    }

    /**
     * 删除用户
     *
     * @param userIds
     * @param cmdRunner
     * @throws APIException
     */
    public void deleteUsers(JSONArray userIds, CmdRunner cmdRunner) throws APIException {
        for (Object userId : userIds) {
            Command command = new Command(Command.INTEGRITY, "deletemksdomainuser");
            command.addSelection(String.valueOf(userId));
            if (cmdRunner != null) {
                cmdRunner.execute(command);
                logger.info("测试环境删除完成:" + userId);
            } else {
                conn.execute(command);
                logger.info("正式环境删除完成:" + userId);
            }
        }
    }

    public String getUserNames(List<String> userIds) throws APIException {
        String user = "";
        if (userIds != null && userIds.size() > 0) {
            Command cmd = new Command(Command.IM, "users");
            cmd.addOption(new Option("fields", "name,fullname,email,isActive"));
            for (String userId : userIds) {
                cmd.addSelection(userId);
            }
            Response res = conn.execute(cmd);
            if (res != null) {
                WorkItemIterator iterator = res.getWorkItems();
                while (iterator.hasNext()) {
                    WorkItem item = iterator.next();
                    if ("true".equalsIgnoreCase(item.getField("isActive").getValueAsString())) {
                        user = user + item.getField("fullname").getValueAsString() + ",";
                    }
                }
            }
            if (user.length() > 0) {
                user = user.substring(0, user.length() - 1);
            }
        }
        return user;
    }

    public List<String> getTestSteps(List<String> realStepFields) throws APIException {
        List<String> fieldList = new ArrayList<String>();
        if (fieldList.isEmpty()) {
            fieldList.add("ID");
            fieldList.add("Test Input");
            fieldList.add("Test Output");
            fieldList.add("Call Depth");
            fieldList.add("Test Procedure");
        }
        return fieldList;
    }

    public List<String> viewIssue(String id, boolean showRelationship)
            throws APIException {
        Command cmd = new Command(Command.IM, "viewissue");
        MultiValue mv = new MultiValue(",");
        cmd.addOption(new Option("showTestResults"));
        if (showRelationship) {
            cmd.addOption(new Option("showRelationships"));
        }
        cmd.addSelection(id);
        Response res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        List<String> relations = new ArrayList<String>();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            Iterator<?> iterator = wi.getFields();
            Map<String, String> map = new HashMap<String, String>();
            while (iterator.hasNext()) {
                Field field = (Field) iterator.next();
                String fieldName = field.getName();
//				if("MKSIssueTestResults".equals(fieldName)){
//					field.getList();
//				}
                if ("Test Steps".equals(fieldName)) {
                    System.out.println("123");
                    StringBuilder sb = new StringBuilder();
                    ItemList il = (ItemList) field.getList();
                    for (int i = 0; i < il.size(); i++) {
                        Item item = (Item) il.get(i);
                        if (i > 0) {
                            sb.append(",");
                        }
                        sb.append(item.getId());
                    }
                    map.put(fieldName, sb.toString());
                }
                if ("Test Result".equals(fieldName) || "Test Results".equals(fieldName)) {
                    System.out.println("123");
                }
            }
        }
        return relations;
    }


    /**
     * Description 获取所有Field 类型，并把Pick值预先取出
     *
     * @param fields
     * @param PICK_FIELD_RECORD
     * @return
     * @throws APIException
     */
    public Map<String, String> getAllFieldType(List<String> fields, Map<String, List<String>> PICK_FIELD_RECORD) throws APIException {
        Map<String, String> fieldTypeMap = new HashMap<String, String>();
        Command cmd = new Command("im", "fields");
        cmd.addOption(new Option("noAsAdmin"));
        cmd.addOption(new Option("fields", "picks,type"));
        for (String field : fields) {
            if (field != null && field.length() > 0) {
                cmd.addSelection(field);
            }
        }
        Response res = null;
        try {
            res = conn.execute(cmd);
        } catch (APIException e) {

            e.printStackTrace();
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "错误代码：MKS 1116", "提示", 1);
            System.exit(0);
        }

        if (res != null) {
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                String field = wi.getId();
                String fieldType = wi.getField("Type").getValueAsString();
                if ("pick".equals(fieldType)) {
                    Field picks = wi.getField("picks");
                    ItemList itemList = (ItemList) picks.getList();
                    if (itemList != null) {
                        List<String> pickVals = new ArrayList<String>();
                        for (int i = 0; i < itemList.size(); i++) {
                            Item item = (Item) itemList.get(i);
                            String visiblePick = item.getId();
                            Field attribute = item.getField("active");
                            if (attribute != null && "true".equalsIgnoreCase(attribute.getValueAsString())
                                    && !pickVals.contains(visiblePick)) {
                                pickVals.add(visiblePick);
                            }
                        }
                        PICK_FIELD_RECORD.put(field, pickVals);
                    }
                } else if ("fva".equals(fieldType)) {

                }
                fieldTypeMap.put(field, fieldType);
            }
        }
        return fieldTypeMap;
    }

    /**
     * Description 查询所有Projects
     *
     * @return
     * @throws APIException
     */
    public static List<String> getProjects() throws APIException {
        List<String> projects = new ArrayList<String>();
        Command cmd = new Command("im", "projects");

        Response res = conn.execute(cmd);
        if (res != null) {
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                String project = wi.getId();
                projects.add(project);
            }
        }
        return projects;
    }

    /**
     * 获取所有的projectid
     * 获取当前选中id的List集合  id,name
     *
     * @return
     * @throws Exception
     */
    public static List<Map<String, String>> getAllProject(List<String> fields) throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Command cmd = new Command("im", "projects");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    map.put(field, value);
                }
                list.add(map);
            }
        } catch (APIException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return list;
    }

    /**
     * Description 查询组成员
     *
     * @param groupName
     * @return
     * @throws APIException
     */
    public static List<String> getGroupMembers(String groupName) throws APIException {
        List<String> members = new ArrayList<String>();
        Command cmd = new Command("aa", "groups");
        cmd.addOption(new Option("members"));
        cmd.addSelection(groupName);
        Response res = conn.execute(cmd);
        if (res != null) {
            WorkItemIterator workItemItera = res.getWorkItems();
            while (workItemItera.hasNext()) {
                WorkItem workItem = workItemItera.next();
                Field field = workItem.getField("members");
                ItemList itemList = (ItemList) field.getValue();
                if (!itemList.isEmpty()) {
                    for (int i = 0; i < itemList.size(); i++) {
                        Item user = (Item) itemList.get(i);
                        members.add(user.getId());
                    }
                }
            }
        }
        return members;
    }


    /**
     * 查询所有用户
     *
     * @param fields
     * @return
     * @throws APIException
     */
    public List<User> getAllUsers(List<String> fields) throws APIException {
        List<User> list = new ArrayList<User>();
        Command cmd = new Command("im", "users");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        Response res = null;
        res = conn.execute(cmd);
//        res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            User user = new User();
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if ("fullname".equals(field)) {
                    user.setUserName(value);
                } else if ("name".equals(field)) {
                    user.setLogin_ID(value);
                } else if ("Email".equals(field)) {
                    user.setEmail(value);
                }
            }
            list.add(user);
        }
        return list;
    }

    /**
     * 根据用户查询用户信息
     *
     * @param fields
     * @param username
     * @return
     * @throws APIException
     */
    public User getAllUsers1(List<String> fields, String username) throws APIException {
        List<User> list = new ArrayList<User>();
        Command cmd = new Command("im", "users");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        cmd.addSelection(username);
        Response res = null;
        res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        User user = new User();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if ("fullname".equals(field)) {
                    user.setUserName(value);
                } else if ("name".equals(field)) {
                    user.setLogin_ID(value);
                } else if ("Email".equals(field)) {
                    user.setEmail(value);
                }
            }
        }
        return user;
    }

    /**
     * 查询所有project
     *
     * @param fields
     * @return
     * @throws APIException
     */
    public List<Project> getAllprojects(List<String> fields) throws APIException {
        List<Project> list = new ArrayList<Project>();
        Command cmd = new Command("im", "projects");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        mv.add("isActive");//判断是否失效
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        Response res = null;
        res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            Project project = new Project();
            WorkItem wi = it.next();
            if ("true".equalsIgnoreCase(wi.getField("isActive").getValueAsString())) {
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    if ("name".equals(field)) {
                        project.setProject(value);
                    } else if ("backingIssueID".equals(field)) {
                        project.setPID(value);
                    }
                }
                list.add(project);
            }
        }
        return list;
    }


    /**
     * 创建文档
     *
     * @param type
     * @param fieldsValue
     * @param richFieldValue
     * @return
     * @throws APIException
     */
    public String createDocument(String type, Map<String, String> fieldsValue, Map<String, String> richFieldValue) throws APIException {
        Command cmd = new Command("im", "createsegment");
        String id = null;
        OptionList ol = new OptionList();
        Option option = new Option("Type", type);
        ol.add(option);
        Set<String> set = fieldsValue.keySet();
        for (String field : set) {
            String value = fieldsValue.get(field);
            if (value != null && !value.isEmpty()) {
                Option option2 = new Option("field", field + "=" + value);
                ol.add(option2);
            }
        }
        cmd.setOptionList(ol);
        Response res = conn.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 创建分支文档
     *
     * @param docId
     * @param project
     * @return
     * @throws APIException
     */
    public String branchDocument(String docId, String project) throws APIException {
        Command cmd = new Command("im", "branchsegment");
        String id = null;
        Option option = new Option("project", project);
        cmd.addOption(option);
        cmd.addOption(new Option("--yes"));
        cmd.addSelection(docId);
        Response res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            id = wi.getResult().getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 创建复用条目
     *
     * @param
     * @param
     * @return
     * @throws APIException
     */
    public String copyContent(String parentId, String location, String itemId) throws APIException {
        Command cmd = new Command("im", "copycontent");
        String id = null;
        Option option = new Option("parentID", parentId);
        cmd.addOption(option);
        option = new Option("insertLocation", location);
        cmd.addOption(option);
        option = new Option("refmode", "reuse");//默认是reuse
        cmd.addOption(option);
        cmd.addSelection(itemId);
        Response res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            id = wi.getResult().getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 创建Baseline
     *
     * @param label
     * @param docID
     * @throws APIException
     */
    public void createBaseLine(String label, String docID) throws APIException {
        Command cmd = new Command("im", "baseline");
        cmd.addOption(new Option("label", label));
        cmd.addSelection(docID);
        try {
            conn.execute(cmd);
        } catch (APIException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 创建条目，
     *
     * @param parentId
     * @param
     * @param fields
     * @param type
     * @return
     * @throws APIException
     */
    public String createContent(String parentId, Map<String, String> fields, String type) throws APIException {
        return createContent(parentId, null, fields, type);
    }

    /**
     * 创建Content
     *
     * @param parentId
     * @param fields
     * @param type
     * @return
     */
    public String createContent(String parentId, String insertLocation, Map<String, String> fields, String type) throws APIException {
        String id = null;
        OptionList ol = new OptionList();
        Option option = new Option("Type", type);
        ol.add(option);
        Option op2 = new Option("parentID", parentId);
        ol.add(op2);
        if (insertLocation != null) {//插入位置
            op2 = new Option("insertLocation", insertLocation);
            ol.add(op2);
        }
        for (Entry<String, String> entry : fields.entrySet()) {
            if ("Text".equals(entry.getKey())) {
                Option op = new Option("richContentField", entry.getKey() + "=" + entry.getValue());
                ol.add(op);
            } else {
                Option op = new Option("field", entry.getKey() + "=" + entry.getValue());
                ol.add(op);
            }
        }
        String commandName = "createcontent";
        Command cmd = new Command("im", commandName);
        cmd.setOptionList(ol);
        // 设置cmd
        currentCommand = Arrays.toString(cmd.toStringArray());
        Response res = conn.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 修改项
     *
     * @param id
     * @param fieldValue
     * @param richFieldValue
     * @throws APIException
     */
    public void editIssue(String id, Map<String, String> fieldValue, Map<String, String> richFieldValue)
            throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        if (fieldValue != null) {
            for (Entry<String, String> entrty : fieldValue.entrySet()) {
                cmd.addOption(new Option("field", entrty.getKey() + "=" + entrty.getValue()));
            }
        }
        if (richFieldValue != null) {
            for (Entry<String, String> entrty : richFieldValue.entrySet()) {
                cmd.addOption(new Option("richContentField", entrty.getKey() + "=" + entrty.getValue()));
            }
        }

        cmd.addSelection(id);
        conn.execute(cmd);
    }

    /**
     * 编辑关联关系
     *
     * @return
     * @throws APIException
     */
    public boolean editRelationship(String issueId, Map<String, String> deleteRelationMap, Map<String, String> addRelationMap) throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        if (deleteRelationMap != null) {
            for (Entry<String, String> entry : deleteRelationMap.entrySet()) {
                MultiValue mv = new MultiValue("=");
                mv.add(entry.getKey());
                mv.add(entry.getValue());
                Option option = new Option("removeFieldValues", mv);
                cmd.addOption(option);
            }
        }
        if (addRelationMap != null) {
            for (Entry<String, String> entry : addRelationMap.entrySet()) {
                Option relationshipOption = new Option("addFieldValues");
                MultiValue mv = new MultiValue("=");
                mv.add(entry.getKey());
                mv.add(entry.getValue());
                relationshipOption.add(mv);
                cmd.addOption(relationshipOption);
            }

        }
        cmd.addSelection(issueId);
        Response res = conn.execute(cmd);
        if (res != null && res.getExitCode() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 添加附件
     *
     * @param id
     * @param attach
     * @param field
     * @throws APIException
     */
    public void addAttachment(String id, Attachment attach, String field) throws APIException {

        Command cmd = new Command("im", "editissue");
        cmd.setOptionList(getAttachmentOptionList(attach, field));
        cmd.addSelection(id);
        try {
            conn.execute(cmd);
        } catch (APIException e) {
            Response res = e.getResponse();
            if (res.getWorkItemListSize() > 0) {
                WorkItem wi = res.getWorkItem(id);
                APIException apiException = wi.getAPIException();
                logger.error(apiException.getMessage());
                throw apiException;
            }
            throw e;
        }
    }

    /**
     * 得到附件相关信息
     *
     * @param attach
     * @param field
     * @return
     */
    public OptionList getAttachmentOptionList(Attachment attach, String field) {
        OptionList ol = new OptionList();
        MultiValue mv = new MultiValue(",");
        mv.add("field=" + field);
        mv.add("path=" + attach.getPath());
//        mv.add("path=remote://" + attach.getPath());
        mv.add("name=" + attach.getName());
        Option op = new Option("addAttachment", mv);
        ol.add(op);
        return ol;
    }

    /**
     * 删除关联关系
     *
     * @param id
     * @throws APIException
     */
    public void removecontent(String id) throws APIException {
        Command cmd = new Command(Command.IM, "removecontent");
        cmd.addOption(new Option("forceConfirm", "yes"));
        cmd.addSelection(id);
        conn.execute(cmd);
    }

    /**
     * 添加关联关系
     *
     * @param id
     * @param RelationshipFile
     * @param RelationshipId
     * @throws APIException
     */
    public void addRelationships(String id, String RelationshipFile, String RelationshipId) throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        cmd.addOption(new Option("addRelationships", RelationshipFile + ":" + RelationshipId));
        cmd.addSelection(id);
        conn.execute(cmd);
    }

    /**
     * 删除项
     *
     * @param id
     * @throws APIException
     */
    public void deleteissue(String id) throws APIException {
        Command cmd = new Command(Command.IM, "deleteissue");
        cmd.addOption(new Option("noconfirm"));
        cmd.addOption(new Option("noconfirmRQ"));
        cmd.addOption(new Option("yes"));
        cmd.addSelection(id);
        conn.execute(cmd);
    }

    /**
     * 移动条目
     *
     * @param parentID
     * @param insertLocation
     * @param ids
     * @throws APIException
     */
    public void movecontent(String parentID, String insertLocation, String ids) throws APIException {
        Command cmd = new Command(Command.IM, "movecontent");
        cmd.addOption(new Option("parentID", parentID));
        cmd.addOption(new Option("insertLocation", insertLocation));
        String[] id = ids.split(",");
        SelectionList sl = new SelectionList();
        for (int i = 0; i < id.length; i++) {
            sl.add(id[i]);
        }
        cmd.setSelectionList(sl);
        conn.execute(cmd);
    }

    /**
     * 创建项
     *
     * @param type
     * @param map
     * @param richContentMap
     * @return
     * @throws APIException
     */
    public String createIssue(String type, Map<String, String> map, Map<String, String> richContentMap)
            throws APIException {
        String id = null;
        Command cmd = new Command(Command.IM, "createissue");
        cmd.addOption(new Option("type", type));
        if (map != null) {
            for (Entry<String, String> entrty : map.entrySet()) {
                String value = entrty.getValue();
                if (value == null || "null".equals(value)) {
                    value = "";
                }
                cmd.addOption(new Option("field", entrty.getKey() + "=" + value));
            }
        }
        if (richContentMap != null && richContentMap.size() > 0) {
            for (Entry<String, String> entrty : map.entrySet()) {
                String value = entrty.getValue();
                if (value == null || "null".equals(value)) {
                    value = "";
                }
                cmd.addOption(new Option("richContentField", entrty.getKey() + "=" + value));
            }
        }
        Response res = conn.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 获取SW_SID
     *
     * @param SW_SID
     * @return
     * @throws APIException
     */
    public String getALMIDBySearchSWSID(String SW_SID) throws APIException {
        List<String> result = searchALMIDBySWQuery(Arrays.asList(SW_SID));
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    /**
     * 通过Query - SW_ID查询出来ALM_ID
     *
     * @return
     * @throws APIException
     */
    public List<String> searchALMIDBySWQuery(List<String> SW_IDList) throws APIException {
        if (SW_IDList == null || SW_IDList.isEmpty()) {
            return null;
        }
        StringBuffer queryDefinition = new StringBuffer("((");
        for (int i = 0; i < SW_IDList.size(); i++) {
            String SW_ID = SW_IDList.get(i);
            queryDefinition.append("(field[SW_ID] contains LIKE" + SW_ID + ")");
            if (i < SW_IDList.size() - 1) {
                queryDefinition.append(" or ");
            }
        }
        queryDefinition.append("))");
        List<String> fields = Arrays.asList("ID", "SW_ID");
        List<Map<String, String>> resultList = queryIssueByQuery(fields, queryDefinition.toString());
        List<String> result = new ArrayList<String>();
        if (resultList != null && !resultList.isEmpty()) {
            if (resultList.size() > 10) {
                result = new ArrayList<String>(resultList.size());
            }
            for (Map<String, String> map : resultList) {
                String swID = map.get("SW_ID");
                if (SW_IDList.contains(swID)) {
                    result.add(swID);
                }
            }
        }
        return result;
    }

    /**
     * 通过Query - SW_ID查询出来ALM_ID和Type
     *
     * @return
     * @throws APIException
     */
    public List<Map<String, String>> searchALMIDTypeBySWID(List<String> SW_IDList, String project) throws APIException {
        if (SW_IDList == null || SW_IDList.isEmpty()) {
            return null;
        }
        StringBuffer queryDefinition = new StringBuffer("( (");
        for (int i = 0; i < SW_IDList.size(); i++) {
            String SW_ID = SW_IDList.get(i);
            queryDefinition.append("(field[SW_ID] contains LIKE" + SW_ID + ") ");
            if (i < SW_IDList.size() - 1) {
                queryDefinition.append(" or ");
            }
        }
        queryDefinition.append(") ");
        if (project != null && !"".equals(project)) {
            queryDefinition.append(" and (field[Project] = " + project + ") ");
        }
        queryDefinition.append(") ");

        List<String> fields = Arrays.asList("ID", "Type", "SW_ID");
        List<Map<String, String>> resultList = queryIssueByQuery(fields, queryDefinition.toString());
        AlmController.log.info("查询条目 ：" + Arrays.asList(SW_IDList));
        List<Map<String, String>> ALMSWList = new ArrayList<>();
        for (Map<String, String> map : resultList) {
            String swID = map.get("SW_ID");
            AlmController.log.info("查询到SW_ID ：" + swID);
            if (SW_IDList.contains(swID)) {
                ALMSWList.add(map);
            }
        }
        return resultList;
    }

    /**
     * 获取SW_ID与ALM_ID的对应Map
     *
     * @param
     * @param SW_IDList
     * @return
     * @throws APIException
     */
    public Map<String, String> getSWALMMap(List<String> SW_IDList, String project) throws APIException {
        Map<String, String> resultMap = new HashMap<String, String>();
        List<String> fields = Arrays.asList("ID", "SW_ID");

        StringBuffer queryDefinition = new StringBuffer("((");
        for (int i = 0; i < SW_IDList.size(); i++) {
            String SW_ID = SW_IDList.get(i);
            queryDefinition.append("(field[SW_ID] contains LIKE" + SW_ID + ")");
            if (i < SW_IDList.size() - 1) {
                queryDefinition.append(" or ");
            }
        }
        queryDefinition.append(") and field[Project] = " + project + ")");
        List<Map<String, String>> resultList = queryIssueByQuery(fields, queryDefinition.toString());
        if (resultList != null && !resultList.isEmpty()) {
            if (resultList.size() > 16) {
                resultMap = new HashMap<String, String>(resultList.size() * 4 / 3);
            }
            for (Map<String, String> map : resultList) {
                String swID = map.get("SW_ID");
                if (SW_IDList.contains(swID)) {
                    resultMap.put(swID, map.get("ID"));
                }
            }
        }
        return resultMap;
    }

    private String query(JSONObject params) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!String.valueOf(entry.getValue()).isEmpty()) {
                if ("Document Short Title".equals(entry.getKey())) {
                    list.add(String.format("(field[%s] contains %s)", entry.getKey(), entry.getValue()));
                } else {
                    list.add(String.format("(field[%s]=%s)", entry.getKey(), entry.getValue()));
                }
            }
        }
        if (list.size() > 0) {
            return String.format("(%s)", String.join(" and ", list));
        }
        return null;
    }

    public String checkIdAndName(JSONObject params) {
        Command cmd = new Command("im", "issues");
        String query = query(params);
        logger.info("query:" + query);
        cmd.addOption(new Option("queryDefinition", query));
        try {
            Response execute = conn.execute(cmd);
            WorkItemIterator it = execute.getWorkItems();
            WorkItem wi = it.next();
            if (wi != null && params.getString(DOCUMENT_SHORT_TITLE_FIELD).equals(wi.getField(DOCUMENT_SHORT_TITLE_FIELD).getValueAsString())) {
                return "206 - Short Title is exist , Please input again ";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "success";
    }

    public List<Map<String, String>> queryDocByQuery(String doc_SW_SID, String issue_Type, String project) throws APIException {
        StringBuffer queryDifinition = new StringBuffer("( (field[SW_SID] contains LIKE" + doc_SW_SID + ") ");
        if (project != null && !"".equals(project)) {
            queryDifinition.append("and (field[Project] = " + project + ") ");
        }
        if (issue_Type != null && !"".equals(issue_Type)) {
            queryDifinition.append("and (field[Type] = " + issue_Type + ") ");
        }
        queryDifinition.append(") ");
        AlmController.log.info("Doc queryDifinition = " + queryDifinition);
        List<String> fieldList = new ArrayList<>();
        fieldList.add("ID");
        fieldList.add("Project");
        fieldList.add("State");
        fieldList.add("Created Date");
        fieldList.add("SW_SID");
        if (Constants.DOC_PUBLISHED_STATE.equals(AnalysisXML.getTypeTargetState(issue_Type))) {
            // 最后状态 等于目标状态时，查询此字段
            fieldList.add("SWR Synchronize Count");
        }
        List<Map<String, String>> resultList = queryIssueByQuery(fieldList, queryDifinition.toString());
        AlmController.log.info("Query DocList = " + resultList.size());
        List<Map<String, String>> docList = new ArrayList<>();
        for (Map<String, String> map : resultList) {
            String swSID = map.get("SW_SID");
            AlmController.log.info("Query swSID = " + swSID + "|| Doc SWSID = " + doc_SW_SID);
            if (swSID.equals(doc_SW_SID)) {
                docList.add(map);
            }
        }
        return docList;
    }

    /**
     * 通过query查询数据
     *
     * @param fields
     * @param query
     * @return
     * @throws APIException
     */
    public List<Map<String, String>> queryIssueByQuery(List<String> fields, String query) throws APIException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        cmd.addOption(new Option("queryDefinition", query));

        Response res = null;
        try {
            res = conn.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    map.put(field, value);
                }
                list.add(map);
            }
        } catch (APIException e) {
            // success = false;
            logger.error(e.getMessage());
            throw e;
        }
        return list;
    }

    /**
     * 根据SWid获取ALMid
     *
     * @param SWID
     * @param IDvalue
     * @param project
     * @param type
     * @param field
     * @return
     */
    public String getIssueBySWID(String SWID, String IDvalue, String project, String type, String field) {
        String commandName = "issues";
        Command cmd = new Command("im", commandName);
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        mv.add(field);
        mv.add(SWID);
        OptionList ol = new OptionList();

        Option op = new Option("fields", mv);
        ol.add(op);
        StringBuffer queryDefinition = new StringBuffer("( (field[" + SWID + "] contains " + IDvalue + ") ");
        if (project != null && !"".equals(project)) {
            queryDefinition.append("and (field[Project]=" + project + ") ");
        }
        if (type != null && !"".equals(type)) {
            queryDefinition.append("and (field[Type]=" + type + ") ");
        }
        queryDefinition.append(")");
        Option op2 = new Option("queryDefinition", queryDefinition.toString());
        ol.add(op2);

        cmd.setOptionList(ol);
        List<Map<String, String>> resultList = new ArrayList<>();
        Response res = null;
        try {
            res = conn.execute(cmd);
            logger.info("getAllFunctionListDoc cmd : " + cmd);
            WorkItemIterator it = res.getWorkItems();
            Map<String, String> issueMap = null;
            while (it.hasNext()) {
                issueMap = new HashMap<String, String>();
                WorkItem wi = it.next();
                issueMap.put(field, wi.getField(field).getValueAsString());
                issueMap.put(SWID, wi.getField(SWID).getValueAsString());
                resultList.add(issueMap);
            }
        } catch (APIException e) {
            logger.error("getAllFunctionListDoc Exception", e);
        }

        for (int i = 0; i < resultList.size(); i++) {
            Map<String, String> issueMap = resultList.get(i);
            String sw_sid = issueMap.get(SWID);
            if (IDvalue.equals(sw_sid)) {
                return issueMap.get(field);
            }
        }
        return "";
    }

    /**
     * 根据SW_SID查询条目，如果存在多个：判断是否与当前项目相同，相同，则返回当前项目SID；不同，找到最原始的数据，
     *
     * @param fields
     * @param SW_SIDVal
     * @param project
     * @return
     */
    public Map<String, String> searchOrigIssue(List<String> fields, String SW_SIDVal, String type, String project) {
        String commandName = "issues";
        Command cmd = new Command("im", commandName);
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        OptionList ol = new OptionList();
        Option op = new Option("fields", mv);
        ol.add(op);
        StringBuffer queryDefinition = new StringBuffer("( (field[SW_SID] contains LIKE" + SW_SIDVal + ") ");
        if (project != null && !"".equals(project)) {
            queryDefinition.append("and (field[Project]=" + project + ") ");
        }
        if (type != null && !"".equals(type)) {
            queryDefinition.append("and (field[Type]=" + type + ") ");
        }
        queryDefinition.append(")");
        Option op2 = new Option("queryDefinition", queryDefinition.toString());
        ol.add(op2);

        cmd.setOptionList(ol);
        List<Map<String, String>> resultList = new ArrayList<>();
        Response res = null;
        try {
            res = conn.execute(cmd);
            logger.info("getAllFunctionListDoc cmd : " + cmd);
            WorkItemIterator it = res.getWorkItems();
            Map<String, String> issueMap = null;
            while (it.hasNext()) {
                issueMap = new HashMap<String, String>();
                WorkItem wi = it.next();
                for (String field : fields) {
                    issueMap.put(field, wi.getField(field).getValueAsString());
                }
                resultList.add(issueMap);
            }
        } catch (APIException e) {
            logger.error("getAllFunctionListDoc Exception", e);
        }
        Map<String, String> origMap = null;
        for (int i = 0; i < resultList.size(); i++) {
            Map<String, String> issueMap = resultList.get(i);
            String issueProject = issueMap.get("Project");
            if (project != null && !"".equals(project)// 当传递的Project不为空时，则判断查询到的数据project与传递是否一致，一致则返回
                    && issueProject.equals(project)) {
                origMap = issueMap;
                break;
            } else {
                if (origMap == null) {
                    origMap = issueMap;
                } else {
                    String createdDate = issueMap.get("Created Date");
                    String orCreatedDate = origMap.get("Created Date");
                    Date target = new Date(createdDate);
                    Date orgi = new Date(orCreatedDate);
                    if (target.before(orgi)) {// 比对，获取最开始的一条数据
                        origMap = issueMap;
                    }
                }
            }
        }
        return origMap;
    }


    /**
     * 根据project获取组
     *
     * @param project
     * @return
     * @throws APIException
     */
    public List<String> getGroupsByProject(String project) throws APIException {
        List<String> gorups = new ArrayList<>();
        Command cmd = new Command("im", "projects");
        cmd.addOption(new Option("fields", "permittedGroups"));
        cmd.addSelection(project);
        Response res = conn.execute(cmd);
        String str = "";
        if (res != null) {
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                str = wi.getField("permittedGroups").getValueAsString();
            }
        }
        String[] s = str.split(",");
        for (int i = 0; i < s.length; i++) {
            gorups.add(s[i]);
        }
        return gorups;
    }

    /**
     * 根据静态组查询用户
     *
     * @param projectName
     * @return
     * @throws APIException
     */
    public List<User> getProjects(String projectName) throws APIException {
        Command cmd = new Command("im", "issues");
        cmd.addOption(new Option("fields", "TeamMembers"));
        String query = "((field[Type]=Project)and(field[Project]=" + projectName + "))";
        cmd.addOption(new Option("queryDefinition", query));
        Response res = conn.execute(cmd);
        String str = "";
        if (res != null) {
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                str = wi.getField("TeamMembers").getValueAsString();
            }
        }
        List<User> us = new ArrayList<>();
        if (str != null) {
            String[] s = str.split(",");
            for (int i = 0; i < s.length; i++) {
                User u = getAllUsers1(Arrays.asList("fullname", "name", "Email"), s[i]);
                us.add(u);
            }
        }

        return us;
    }

    /**
     * @param projectName
     * @param
     * @return
     * @throws APIException
     */
    public List<User> getProjectDynaUsers(String projectName, List<String> dynamicGroups) throws APIException {
        List<User> users = new ArrayList<User>();
        Command cmd = new Command("im", "dynamicgroups");
        cmd.addOption(new Option("fields", "membership"));
        for (String group : dynamicGroups) {
            cmd.addSelection(group);
        }
        List<String> userIdList = new ArrayList<String>();
        Response res = conn.execute(cmd);
        if (res != null) {
            WorkItemIterator groupsItemItera = res.getWorkItems();
            if (groupsItemItera != null) {
                while (groupsItemItera.hasNext()) {
                    WorkItem groupItem = groupsItemItera.next();
                    String groupDGName = groupItem.getId();
                    List<String> projectUserList = new ArrayList<String>();
                    Field field = groupItem.getField("membership");
                    ItemList itemList = (ItemList) field.getValue();
                    if (!itemList.isEmpty()) {
                        for (int i = 0; i < itemList.size(); i++) {
                            Item item = (Item) itemList.get(i);
                            String project = item.getId();
                            if (!projectName.equals(project)) {// 只查询当前项目
                                continue;
                            }

                            Field userField = item.getField("Users");
                            ItemList userList = (ItemList) userField.getValue();
                            if (!userList.isEmpty()) {
                                for (int j = 0; j < userList.size(); j++) {
                                    Item user = (Item) userList.get(j);
                                    userIdList.add(user.getId());
                                }
                            }
                            Field groupField = item.getField("Groups");// 处理组成员
                            ItemList groupList = (ItemList) groupField.getValue();
                            if (!groupList.isEmpty()) {
                                for (int j = 0; j < groupList.size(); j++) {
                                    Item group = (Item) groupList.get(j);
                                    String groupName = group.getId();
                                    List<String> members = getGroupMembers(groupName);
                                    if (members != null && !members.isEmpty()) {
                                        userIdList.addAll(members);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return getAllUsers(Arrays.asList("fullname", "name", "Email"), userIdList);
    }

    /**
     * 获取文档下所有条目数据
     *
     * @param docId
     * @return
     * @throws APIException
     */
    public List<String> getDocContents(String docId, Map<String, String> SWSIDMap) throws APIException {
        Command cmd = new Command("im", "issues");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        mv.add("ID");
        mv.add("SW_SID");
        mv.add("SW_ID");
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        String queryDefinition = "((field[Document ID]=" + docId + "))";
        cmd.addOption(new Option("queryDefinition", queryDefinition));
        Response res = conn.execute(cmd);
        List<String> issueList = new ArrayList<>();
        if (res != null) {
            WorkItemIterator contentIter = res.getWorkItems();
            while (contentIter.hasNext()) {
                WorkItem wi = contentIter.next();
                String issueId = wi.getField("ID").getValueAsString();
                String issueSWSID = wi.getField("SW_SID").getValueAsString();
                AlmController.log.info("ALM-ID = " + issueId + " || " + "SW_SID = " + issueSWSID);
                SWSIDMap.put(issueSWSID, issueId);
                issueList.add(issueId);
            }
        }
        return issueList;
    }

    /**
     * 根据用户查询用户信息
     *
     * @param fields
     * @param userIdList
     * @return
     * @throws APIException
     */
    public List<User> getAllUsers(List<String> fields, List<String> userIdList) throws APIException {
        if (userIdList == null || userIdList.isEmpty()) {//如果动态组查询为空，则直接返回空数据
            return new ArrayList<User>();
        }
        List<User> list = new ArrayList<User>();
        Command cmd = new Command("im", "users");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        for (String userId : userIdList) {
            cmd.addSelection(userId);
        }
        Response res = null;
        res = conn.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        User user;
        while (it.hasNext()) {
            user = new User();
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if ("fullname".equals(field)) {
                    user.setUserName(value);
                } else if ("name".equals(field)) {
                    user.setLogin_ID(value);
                } else if ("Email".equals(field)) {
                    user.setEmail(value);
                }
            }
            list.add(user);
        }
        return list;
    }

    /**
     * 获取已保存的密码
     *
     * @param loginId
     * @return
     * @throws APIException
     */
    public String getUserPWD(String loginId) throws MsgArgumentException, APIException {
        String baseUserPwd = null;
        String queryCondition = "((field[Type] = User Record) and (field[Login ID] contains LIKE " + loginId + "))";// 定义查询User Record语句
        List<Map<String, String>> issueList = queryIssueByQuery(Arrays.asList("Login ID", "PWD", "ID", "Record_date"), queryCondition);
        Map<String, String> userMap = null;
        if (!issueList.isEmpty()) {//查询系统是否已有记录
            for (Map<String, String> map : issueList) {
                String existId = map.get("Login ID");
                if (existId.equals(loginId)) {
                    userMap = map;
                    break;
                }
            }
        }
        if (userMap != null) {//查询到记录，返回
            String recordDate = userMap.get("Record_date");
            Date date = null;
            Date curDate = new Date();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
                date = sdf.parse(recordDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null && (curDate.getTime() - date.getTime()) / (1000 * 3600 * 24) > 30) {//如果获取出来的时间大于30，表示已过期
                throw new MsgArgumentException("203", "当前域密码已过期，请重新注册您的域密码！");
            }
            baseUserPwd = userMap.get("PWD");
        }
        return baseUserPwd;
    }
}
