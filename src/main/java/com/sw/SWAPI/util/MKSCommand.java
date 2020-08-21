package com.sw.SWAPI.util;

import com.mks.api.*;
import com.mks.api.response.*;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MKSCommand {

    public MKSCommand() {

    }

    //	private static final Logger logger = Logger.getLogger(MKSCommand.class.getName());
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

    public static final Map<String, String> ENVIRONMENTVAR = System.getenv();
    public static MKSCommand cmd;
    public static List<String> tsIds = new ArrayList<String>();
    private static String DOCUMENT_TYPE;
    private static String documentName;
    private static List<String> typeList = null;
    private static JComboBox comboBox;
    private static String longinUser;


    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MKSCommand(String _hostname, int _port, String _user, String _password, int _apimajor, int _apiminor) {
        hostname = _hostname;
        port = _port;
        user = _user;
        password = _password;
//		createSession();
        getSession();
    }

    public MKSCommand(String args[]) {
        hostname = args[0];
        port = Integer.parseInt(args[1]);
        user = args[2];
        password = args[3];
        APIMajor = Integer.parseInt(args[4]);
        APIMinor = Integer.parseInt(args[5]);
        createSession();
    }


    public void setCmd(String _type, String _cmd, ArrayList<Option> _ops, String _sel) {
        mksCommand = new Command(_type, _cmd);
        String cmdStrg = (new StringBuilder(String.valueOf(_type))).append(" ").append(_cmd).append(" ").toString();
        if (_ops != null && _ops.size() > 0) {
            for (int i = 0; i < _ops.size(); i++) {
                cmdStrg = (new StringBuilder(String.valueOf(cmdStrg))).append(_ops.get(i).toString()).append(" ")
                        .toString();
                // Option o = new Option(_ops.get(i).toString());
                mksCommand.addOption(_ops.get(i));
            }

        }
        if (_sel != null && _sel != "") {
            cmdStrg = (new StringBuilder(String.valueOf(cmdStrg))).append(_sel).toString();
            mksCommand.addSelection(_sel);
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
            mksResponse = mksCmdRunner.execute(mksCommand);
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
            res = mksCmdRunner.execute(cmd);
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

    //查询caseid和name
    public Map<String, String> getCaseInfoById(String id, List<String> fields) throws APIException {
        Map<String, String> list = new HashMap<String, String>();
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
            res = mksCmdRunner.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                Map<String, String> map = new HashMap<String, String>();
                for (String field : fields) {
                    if (field.contains("::")) {
                        field = field.split("::")[0];
                    }
                    String value = wi.getField(field).getValueAsString();
                    list.put(field, value);
                }
            }
        } catch (APIException e) {
            // success = false;
            logger.error(e.getMessage());
            throw e;
        }
        return list;
    }

    //根据id查询单个结果
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
            res = mksCmdRunner.execute(cmd);
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
            res = mksCmdRunner.execute(cmd);
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
            if (!value.equals("") && value != null) {
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

    //过滤id中的英文 lxg
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
        Response res = mksCmdRunner.execute(command);
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
        Response res = mksCmdRunner.execute(command);
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
        if (sl != null && sl.size() >= 1)
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
        Response res = mksCmdRunner.execute(command);
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

    //未知错误 String dept = "";String trueType   = " "; lxg
    public List<Map<String, String>> queryIssues(SelectionList selectionList, List<String> fields) throws APIException, Exception {
        List<Map<String, String>> returnResult = new ArrayList<Map<String, String>>();
        String dept = "";
        String trueType = " ";
        boolean needFilter = false;
        String category = "";
        if (!"Transmission".equals(dept) && trueType.contains("Test Specification")) {
            needFilter = true;
            category = trueType.substring(0, trueType.indexOf("Speci") - 1);
            fields.add("Test Steps");//测试数据才需要额外查询Test Step数据
            fields.add("Category");
        }
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
            res = mksCmdRunner.execute(cmd);
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
                    if (!currentCategory.equals(category))
                        canAdd = false;
                }
                if (canAdd)
                    returnResult.add(map);
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

    public String getUserNames(List<String> userIds) throws APIException {
        String user = "";
        if (userIds != null && userIds.size() > 0) {
            Command cmd = new Command(Command.IM, "users");
            cmd.addOption(new Option("fields", "name,fullname,email,isActive"));
            for (String userId : userIds) {
                cmd.addSelection(userId);
            }
            Response res = mksCmdRunner.execute(cmd);
            if (res != null) {
                WorkItemIterator iterator = res.getWorkItems();
                while (iterator.hasNext()) {
                    WorkItem item = iterator.next();
                    if (item.getField("isActive").getValueAsString().equalsIgnoreCase("true")) {
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
        Response res = mksCmdRunner.execute(cmd);
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


    public List<Map<String, Object>> getResult(String sessionID, String suiteID, String type) throws APIException {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        SelectionList list = new SelectionList();
        Command cmd = new Command("tm", "results");

        //cmd.addOption(new Option("sessionID", sessionID));
//		if (type.equals("Test Suite")) {
        cmd.addOption(new Option("caseID", suiteID));
//		} else if (type.equals("Test Case")) {
//			cmd.addSelection(sessionID);
//		}
        List<String> fields = new ArrayList<String>();
        fields.add("caseID");
        fields.add("sessionID");
        fields.add("verdict");
        fields.add("Observed Result");
        fields.add("Annotation");
        fields.add("Result Serverity");
        fields.add("Reproducibility");

        fields.add("SW Version");
        fields.add("HW Result Version");


        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);
        Response res = null;
        if (type.equals("Test Suite")) {
            res = mksCmdRunner.execute(cmd);
            WorkItemIterator wk = res.getWorkItems();
            while (wk.hasNext()) {
                Map<String, Object> map = new HashMap<String, Object>();
                WorkItem wi = wk.next();
                for (String field : fields) {
                    Object value = wi.getField(field).getValue();
                    map.put(field, value);
                }
                result.add(map);
            }
        } else if (type.equals("Test Case")) {
            try {
                res = mksCmdRunner.execute(cmd);
                WorkItemIterator wk = res.getWorkItems();
                while (wk.hasNext()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    WorkItem wi = wk.next();
                    for (String field : fields) {
                        Object value = wi.getField(field).getValue();
                        if (value instanceof Item) {
                            Item item = (Item) value;
                            value = item.getId();
                        }
                        if ("verdict".equals(field))
                            field = "verdictType";
                        map.put(field, value);
                    }
                    result.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return result;
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
            res = mksCmdRunner.execute(cmd);
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
                            if (attribute != null && attribute.getValueAsString().equalsIgnoreCase("true")
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

        Response res = mksCmdRunner.execute(cmd);
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
     * 初始化MKSCommand中的参数，并获得连接
     */
    public static void initMksCommand(String host,int port,String defaultUser,String pwd) {
        try {
            logger.info("host:" + host + "; defaultUser:" + defaultUser + "; pwd:" + pwd);
            cmd = new MKSCommand(host, port, defaultUser, pwd, 4, 16);
            logger.info("已连接：" + host);
//			cmd.getSession();
        } catch (Exception e) {
            logger.info("无法连接!");
            System.exit(0);

        }
    }

    /**
     * issues
     * 获取当前选中id的List集合
     *
     * @return
     * @throws Exception
     */
    public static Map<String, String> getSelectedIdList() throws Exception {
        Map<String, String> list = new HashMap<String, String>();
        List<String> caseIds = new ArrayList<String>();
        String issueCount = ENVIRONMENTVAR.get(Constants.MKSSI_NISSUE);
        if (issueCount != null && issueCount.trim().length() > 0) {
            for (int index = 0; index < Integer.parseInt(issueCount); index++) {
                String id = ENVIRONMENTVAR.get(String.format(Constants.MKSSI_ISSUE_X, index));
                tsIds.add(id);//获取到当前选中的id添加进集合Ids集合
            }
        } else {
            logger.info("身份验证失败!! :" + issueCount);
        }
//		tsIds.add("21604");//本地aocun
//查詢project
        Command cmd = new Command("im", "issues");
        cmd.addOption(new Option("fields", "Project,id"));
        String query = "((field[Type]=Project) and ((field[Project Manager]=" + longinUser + ") or (field[Created By]=" + longinUser + ") ))";
        cmd.addOption(new Option("queryDefinition", query));
        Response res = null;
        try {
            res = mksCmdRunner.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                String value = wi.getField("Project").getValueAsString();
                String projectid = wi.getId();
                list.put(projectid, value);
            }
        } catch (APIException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return list;
    }

    //根据id获取项目名称
    public String getProjectNameById(String id) {
        String name = "";
        Command cmd = new Command("im", "issues");
        cmd.addOption(new Option("fields", "Project"));
        cmd.addSelection(id);
        Response res = null;
        try {
            res = mksCmdRunner.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                name = wi.getField("Project").getValueAsString();
            }
        } catch (APIException e) {
            logger.error(e.getMessage());
        }
        return name;
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
            res = mksCmdRunner.execute(cmd);
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

    //查询peoject组用户
    public static void getProjectDynamicGroupsMember(List<String> Groups, String currentProject) throws APIException {
        Command cmd = new Command("im", "dynamicgroups");
        cmd.addOption(new Option("fields", "membership"));
        for (String group : Groups) {
            cmd.addSelection(group);
        }
        Response res = mksCmdRunner.execute(cmd);
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
                            if (!currentProject.equals(project))//只查询当前项目
                                continue;
                            Field userField = item.getField("Users");
                            ItemList userList = (ItemList) userField.getValue();
                            if (!userList.isEmpty()) {
                                for (int j = 0; j < userList.size(); j++) {
                                    Item user = (Item) userList.get(j);
                                    projectUserList.add(user.getId());
                                }
                            }
                            Field groupField = item.getField("Groups");//处理组成员
                            ItemList groupList = (ItemList) groupField.getValue();
                            if (!groupList.isEmpty()) {
                                for (int j = 0; j < groupList.size(); j++) {
                                    Item group = (Item) groupList.get(j);
                                    String groupName = group.getId();
                                    List<String> members = getGroupMembers(groupName);
                                    if (members != null && !members.isEmpty()) {
                                        projectUserList.addAll(members);
                                    }
                                }
                            }
                        }
                    }
                    DealService.allUserList.addAll(projectUserList);
                    DealService.groupMemberRecord.put(groupDGName, projectUserList);
                }
            }
        }
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
        if (DealService.groupMemberRecord.get(groupName) != null) {
            members = DealService.groupMemberRecord.get(groupName);
            return members;
        }
        Command cmd = new Command("aa", "groups");
        cmd.addOption(new Option("members"));
        cmd.addSelection(groupName);
        Response res = mksCmdRunner.execute(cmd);
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
        DealService.groupMemberRecord.put(groupName, members);
        return members;
    }

    public void getAllUserIdAndName(List<String> users) throws APIException {
        Command cmd = new Command(Command.IM, "users");
        cmd.addOption(new Option("fields", "fullname"));
        for (String user : users) {
            cmd.addSelection(user);
        }
        Response res = mksCmdRunner.execute(cmd);
        if (res != null) {
            WorkItemIterator workItemItera = res.getWorkItems();
            if (workItemItera != null) {
                while (workItemItera.hasNext()) {
                    WorkItem workItem = workItemItera.next();
                    String Id = workItem.getId();
                    String fullname = workItem.getField("fullname").getValueAsString();
                    fullname = fullname != null ? fullname + "(" + Id + ")" : Id;
                    DealService.USERNAME_RECORD.put(fullname, Id);
                    DealService.USERID_RECORD.put(Id, fullname);
                }
            }
        }
    }

    //获取用户的权限和名字
    public static List<String> getAllUserIdAndName1(List<String> users) throws APIException {
        List<String> list = new ArrayList<String>();
        Command cmd = new Command(Command.IM, "users");
        cmd.addOption(new Option("fields", "fullname"));
        for (String user : users) {
            cmd.addSelection(user);
        }
        Response res = mksCmdRunner.execute(cmd);
        if (res != null) {
            WorkItemIterator workItemItera = res.getWorkItems();
            if (workItemItera != null) {
                while (workItemItera.hasNext()) {
                    WorkItem workItem = workItemItera.next();
                    String Id = workItem.getId();
                    String fullname = workItem.getField("fullname").getValueAsString();
                    fullname = fullname != null ? fullname + "(" + Id + ")" : Id;
                    DealService.USERNAME_RECORD.put(fullname, Id);
                    DealService.USERID_RECORD.put(Id, fullname);
                    list.add(fullname);
                }
            }
        }
        return list;
    }

    //获取所有的用户
    public void getAllUser() throws APIException {
        List<String> list = new ArrayList<String>();
        Command cmd = new Command(Command.IM, "users");
        cmd.addOption(new Option("fields", "fullname,isActive"));
        Response res = mksCmdRunner.execute(cmd);
        if (res != null) {
            WorkItemIterator workItemItera = res.getWorkItems();
            if (workItemItera != null) {
                while (workItemItera.hasNext()) {
                    WorkItem workItem = workItemItera.next();
                    String Id = workItem.getId();
                    String isActive = workItem.getField("isActive").getValueAsString();
                    if (isActive.equals("true")) { //判断是否是有效用户
                        String fullname = workItem.getField("fullname").getValueAsString();
                        fullname = fullname != null ? fullname + "(" + Id + ")" : Id;
                        DealService.All_user.add(fullname);
                    }
                }
            }
        }
    }

    //修改动态组
    public void updateDynamicGroup(String projectName, String dynamicGroupName, List<String> userJoint) throws APIException {
        if (userJoint.size() > 0) {
            Command cmd = new Command("im", "editdynamicgroup");
            MultiValue mv = new MultiValue();
            mv.setSeparator(",");
            for (String field : userJoint) {
                String[] s = field.split("\\(");
                if (s.length > 1) {
                    mv.add(getValuesInParentheses(field));
                } else {
                    mv.add(field);
                }
            }
            cmd.addOption(new Option("projectmembership", projectName + "=u=" + mv));
            cmd.addSelection(dynamicGroupName);
            mksCmdRunner.execute(cmd);
        } else {
            Command cmd = new Command("im", "editdynamicgroup");
            cmd.addOption(new Option("projectmembership", projectName + "=nomembers"));
            cmd.addSelection(dynamicGroupName);
            mksCmdRunner.execute(cmd);
        }
    }

    //获取最后一个口号中的值
    public static String getValuesInParentheses(String msg) {
        if (msg.split("\\(").length == 1) {
            return msg;
        }
        String projectId = "";
        Pattern p = Pattern.compile("\\(([^\\)]+)\\)");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            projectId = m.group().substring(1, m.group().length() - 1);
        }
//		String newstr = projectId.replace("/"+projectId+"/g","");
        return projectId;
    }

    //查询peoject组用户
    public static Map<String, List<String>> getProjectDynamicGroupsMember1(List<String> Groups, String currentProject) throws APIException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        Command cmd = new Command("im", "dynamicgroups");
        cmd.addOption(new Option("fields", "membership"));
        for (String group : Groups) {
            cmd.addSelection(group);
        }
        Response res = mksCmdRunner.execute(cmd);
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
                            if (!currentProject.equals(project))//只查询当前项目
                                continue;
                            Field userField = item.getField("Users");
                            ItemList userList = (ItemList) userField.getValue();
                            if (!userList.isEmpty()) {
                                for (int j = 0; j < userList.size(); j++) {
                                    Item user = (Item) userList.get(j);
                                    projectUserList.add(user.getId());
                                }
                            }
                            Field groupField = item.getField("Groups");//处理组成员
                            ItemList groupList = (ItemList) groupField.getValue();
                            if (!groupList.isEmpty()) {
                                for (int j = 0; j < groupList.size(); j++) {
                                    Item group = (Item) groupList.get(j);
                                    String groupName = group.getId();
                                    List<String> members = getGroupMembers1(groupName, result);
                                    if (members != null && !members.isEmpty()) {
                                        projectUserList.addAll(members);
                                    }
                                }
                            }
                        }
                    }
                    result.put(groupDGName, projectUserList);
                }
            }
        }
        return result;
    }

    public static List<String> getGroupMembers1(String groupName, Map<String, List<String>> result) throws APIException {
        List<String> members = new ArrayList<String>();
        if (result.get(groupName) != null) {
            members = result.get(groupName);
            return members;
        }
        Command cmd = new Command("aa", "groups");
        cmd.addOption(new Option("members"));
        cmd.addSelection(groupName);
        Response res = mksCmdRunner.execute(cmd);
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
        result.put(groupName, members);
        return members;
    }


    //修改project动态组
    public void updateProjectDynamicGroup(String projectId, Map<String, List<String>> fieldValue) throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        if (fieldValue != null) {
            for (String key : fieldValue.keySet()) {
                String str = "";
                for (String s : fieldValue.get(key)) {
                    str += getValuesInParentheses(s) + ",";
                }
                if (!str.equals("")) {
                    str = str.substring(0, str.length() - 1);
                }
                cmd.addOption(new Option("field", key + "=" + str));
            }
        }
        cmd.addSelection(projectId);
        mksCmdRunner.execute(cmd);
    }

    //查询所有用户
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
        res = mksCmdRunner.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
			User user = new User();
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if(field.equals("fullname")){
                    user.setUserName(value);
                }else if(field.equals("name")){
                    user.setLogin_ID(value);
                }else if(field.equals("Email")){
                    user.setEmail(value);
                }
            }
			list.add(user);
        }
        return list;
    }
    //根据用户查询用户信息
    public User getAllUsers1(List<String> fields,String username) throws APIException {
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
        res = mksCmdRunner.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        User user = new User();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if(field.equals("fullname")){
                    user.setUserName(value);
                }else if(field.equals("name")){
                    user.setLogin_ID(value);
                }else if(field.equals("Email")){
                    user.setEmail(value);
                }
            }
        }
        return user;
    }

    //查询所有project
    public List<Project> getAllprojects(List<String> fields) throws APIException {
		List<Project> list = new ArrayList<Project>();
        Command cmd = new Command("im", "projects");
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        for (String field : fields) {
            mv.add(field);
        }
        Option op = new Option("fields", mv);
        cmd.addOption(op);

        Response res = null;
        res = mksCmdRunner.execute(cmd);
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            Project project = new Project();
            WorkItem wi = it.next();
            for (String field : fields) {
                if (field.contains("::")) {
                    field = field.split("::")[0];
                }
                String value = wi.getField(field).getValueAsString();
                if(field.equals("name")){
                    project.setProject(value);
                }else if(field.equals("backingIssueID")){
                    project.setPID(value);
                }
            }
			list.add(project);
        }
        return list;
    }

    //关闭integrity链接
    public void close(String hostname,int port,String user){
        List<User> list = new ArrayList<User>();
        Command cmd = new Command("aa", "disconnect");
        cmd.addOption(new Option("hostname", hostname));
        cmd.addOption(new Option("port", port+""));
        cmd.addOption(new Option("user", user));
        try {
            mksCmdRunner.execute(cmd);
            logger.info("断开链接： "+hostname);
        } catch (APIException e) {
            logger.info("断开链接错误 "+ hostname);
            e.printStackTrace();
        }
    }

    //创建文档
    public String createDocument(String type,Map<String,String> fieldsValue,Map<String,String> richFieldValue) throws APIException{
        Command cmd = new Command("im", "createsegment");
        String id = null;
        OptionList ol = new OptionList();
        Option option = new Option("Type", type);
        ol.add(option);
        Set<String> set = fieldsValue.keySet();
        for(String field : set){
            String value = fieldsValue.get(field);
            if(value!=null && !value.isEmpty()){
                Option option2 = new Option("field", field+"="+value);
                ol.add(option2);
            }
        }
        cmd.setOptionList(ol);
        Response res =  mksCmdRunner.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }

    /**
     * 创建Content
     * @param parentId
     * @param fields
     * @param type
     * @return
     */
    public String createContent(String parentId, Map<String, String> fields, String type) throws APIException {
        String id = null;
        OptionList ol = new OptionList();
        Option option = new Option("Type", type);
        ol.add(option);
        Option op2 = new Option("parentID", parentId);
        ol.add(op2);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getKey().equals("Text")) {
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
        Response res = mksCmdRunner.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }

    //修改项
    public void editIssue(String id, Map<String, String> fieldValue, Map<String, String> richFieldValue)
            throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        if (fieldValue != null) {
            for (Map.Entry<String, String> entrty : fieldValue.entrySet()) {
                cmd.addOption(new Option("field", entrty.getKey() + "=" + entrty.getValue()));
            }
        }
        if (richFieldValue != null) {
            for (Map.Entry<String, String> entrty : richFieldValue.entrySet()) {
                cmd.addOption(new Option("richContentField", entrty.getKey() + "=" + entrty.getValue()));
            }
        }

        cmd.addSelection(id);
        mksCmdRunner.execute(cmd);
    }

    /**
     * 添加附件
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
            mksCmdRunner.execute(cmd);
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

    //删除关联关系
    public void removecontent(String id) throws APIException {
        Command cmd = new Command(Command.IM, "removecontent");
        cmd.addOption(new Option("forceConfirm", "yes"));
        cmd.addSelection(id);
        mksCmdRunner.execute(cmd);
    }

    //添加关联关系
    public void addRelationships(String id,String RelationshipFile,String RelationshipId) throws APIException {
        Command cmd = new Command(Command.IM, "editissue");
        cmd.addOption(new Option("addRelationships", RelationshipFile+":"+RelationshipId));
        cmd.addSelection(id);
        mksCmdRunner.execute(cmd);
    }

    //删除项
    public void deleteissue(String id)  throws APIException {
        Command cmd = new Command(Command.IM, "deleteissue");
        cmd.addOption(new Option("noconfirm"));
        cmd.addOption(new Option("noconfirmRQ"));
        cmd.addOption(new Option("yes"));
        cmd.addSelection(id);
        mksCmdRunner.execute(cmd);
    }

    //移动条目
    public void movecontent(String parentID,String insertLocation,String ids)  throws APIException {
        Command cmd = new Command(Command.IM, "movecontent");
        cmd.addOption(new Option("parentID",parentID));
        cmd.addOption(new Option("insertLocation",insertLocation));
        String[] id = ids.split(",");
        SelectionList sl = new SelectionList();
        for (int i = 0;i<id.length;i++) {
            sl.add(id[i]);
        }
        cmd.setSelectionList(sl);
        mksCmdRunner.execute(cmd);
    }

    //创建项
    public String createIssue(String type, Map<String, String> map, Map<String, String> richContentMap)
            throws APIException {
        String id = null;
        Command cmd = new Command(Command.IM, "createissue");
        cmd.addOption(new Option("type", type));
        if (map != null ) {
            for (Map.Entry<String, String> entrty : map.entrySet()) {
                String value = entrty.getValue();
                if(value==null || value.equals("null")){
                    value = "";
                }
                cmd.addOption(new Option("field", entrty.getKey() + "=" + value));
            }
        }
        if (richContentMap != null && richContentMap.size() >0) {
            for (Map.Entry<String, String> entrty : map.entrySet()) {
                String value = entrty.getValue();
                if(value==null || value.equals("null")){
                    value = "";
                }
                cmd.addOption(new Option("richContentField", entrty.getKey() + "=" + value));
            }
        }
        Response res = mksCmdRunner.execute(cmd);
        Result result = res.getResult();
        if (result != null) {
            id = result.getField("resultant").getValueAsString();
        }
        return id;
    }
    //根据SWid获取ALMid
    public String getDocIdsByType(String SWID,String IDvalue,String file) {
        String commandName = "issues";
        Command cmd = new Command("im", commandName);
        MultiValue mv = new MultiValue();
        mv.setSeparator(",");
        mv.add(file);
        OptionList ol = new OptionList();

        Option op = new Option("fields", mv);
        ol.add(op);

        Option op2 = new Option("queryDefinition", "( (field["+SWID+"] contains " + IDvalue + ") )");
        ol.add(op2);

        cmd.setOptionList(ol);
        List<String>  ids= new ArrayList<>();
        Response res = null;
        try {
            res = mksCmdRunner.execute(cmd);
            logger.info("getAllFunctionListDoc cmd : " + cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                ids.add(wi.getField(file).getValueAsString());
            }
        } catch (APIException e) {
            logger.error("getAllFunctionListDoc Exception", e);
        }
        String id ="";
        for(int i=0;i<ids.size();i++){
            try {
                String sw_sid = getTypeById(ids.get(i),"SW_SID");
                if(IDvalue.equals(sw_sid)){
                    id =  ids.get(i);
                    break;
                }
            } catch (APIException e) {
                logger.error("匹配sw_sid出错！", e);
                e.printStackTrace();
            }
        }
        return id;

    }
    //获取静态组
    public String[] getStaticGroup(String staticGroup){
        Command cmd = new Command("aa", "groups");
        cmd.addOption(new Option("members"));

        cmd.addSelection(staticGroup);
        String  ids = "";
        Response res = null;
        try {
            res = mksCmdRunner.execute(cmd);
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                ids  = wi.getField("members").getValueAsString();
            }
        } catch (APIException e) {
            logger.error("getAllFunctionListDoc Exception", e);
        }
        String[] id = ids.split(",");
        return id;
    }

    //根据project获取组
    public List<String> getGroupsByProject(String project ) throws APIException {
       List<String> gorups = new ArrayList<>();
        Command cmd = new Command("im", "projects");
        cmd.addOption(new Option("fields","permittedGroups"));
        cmd.addSelection(project);
        Response res = mksCmdRunner.execute(cmd);
        String str = "";
        if (res != null) {
            WorkItemIterator it = res.getWorkItems();
            while (it.hasNext()) {
                WorkItem wi = it.next();
                str = wi.getField("permittedGroups").getValueAsString();
            }
        }
        String[] s = str.split(",");
        for(int i=0;i<s.length;i++){
            gorups.add(s[i]);
        }
       return gorups;
    }
//根据静态组查询用户
public List<User>  getProjects(String projectName) throws APIException{
    Command cmd = new Command("im", "issues");
    cmd.addOption(new Option("fields","TeamMembers"));
    String query = "((field[Type]=Project)and(field[Project]="+projectName+"))";
    cmd.addOption(new Option("queryDefinition",query));
    Response res = mksCmdRunner.execute(cmd);
    String str = "";
    if (res != null) {
        WorkItemIterator it = res.getWorkItems();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            str = wi.getField("TeamMembers").getValueAsString();
        }
    }
    List<User> us = new ArrayList<>();
    if(str!=null){
        String[] s = str.split(",");
        for(int i=0;i<s.length;i++){
            User u = getAllUsers1(Arrays.asList("fullname","name","Email"),s[i]);
            us.add(u);
        }
    }

    return us;
}

}
