package com.sw.SWAPI.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sw.SWAPI.util.MKSCommand;
import com.mks.api.response.APIException;

@SuppressWarnings("all")
public class DealService {

    private static final String POST_CONFIG_FILE = "EngineerMapping.xml";

    private static final Log logger = LogFactory.getLog(MKSCommand.class);
    public static final String POSITION = "position";

    public static final String FIELD_TYPE = "fieldType";

    public static final String GROUP = "group";

    public static final String FIELD_NAME = "FieldName";

    public static final String SUPER_USER = "SUPER_USER";

    public static final String PASSWORD = "PWD";

    public static String REVIEW_STATE = "";

    public static Map<String,List<Map<String,String>>> typeReviewRecord = new HashMap<String, List<Map<String, String>>>();

    public static Map<String,String> engineerMapping = new HashMap<String, String>();

    public static List<String> reviewEngineer = new ArrayList<String>();

    public static String GROUPLIST = "";

    /**
     * 记录组成员
     */
    public static Map<String,List<String>> groupMemberRecord = new HashMap<String, List<String>>();

    /**
     * 记录查询处理的所有用户
     */
    public static List<String> allUserList = new ArrayList<String>();

    /**
     * 记录用户ID Fullname
     */
    public static Map<String,String> USERID_RECORD = new HashMap<String, String>();

    /**
     * 记录用户Fullname ID
     */
    public static Map<String,String> USERNAME_RECORD = new HashMap<String, String>();

    //所用用户
    public static List<String> All_user = new ArrayList();

    private static Properties props = new Properties();
    /**
     * 解析XML
     *
     * @param project
     * @throws APIException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws Exception
     */
    public void parseXML(String editType) throws APIException, Exception {
        DealService.logger.info("start to parse xml : " + POST_CONFIG_FILE);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(DealService.class.getClassLoader().getResourceAsStream( POST_CONFIG_FILE));
        Element root = doc.getDocumentElement();
        //获取Mapping内所有的
        NodeList typeList = root.getElementsByTagName("type");
        for (int j = 0; j < typeList.getLength(); j++) {
            Element type = (Element) typeList.item(j);
            String reviewType = type.getAttribute("name");
            if(editType.equals(reviewType)){
                NodeList FieldList = type.getElementsByTagName("field");
                REVIEW_STATE = type.getAttribute("allowEditState");
                List<String> reviewFields = new ArrayList<String>();
                for (int i = 0; i < FieldList.getLength(); i++) {
                    Element field = (Element) FieldList.item(i);
                    Map<String,String> fieldMap = new HashMap<String,String>();
                    String position = field.getAttribute(POSITION);
                    String fieldName = field.getAttribute("name");
                    fieldMap.put(FIELD_NAME, fieldName);
                    List<Map<String,String>> positionFields = typeReviewRecord.get(fieldName);
                    if(positionFields == null){
                        positionFields = new ArrayList<Map<String, String>>();
                        typeReviewRecord.put(fieldName, positionFields);
                    }
                    if(field.hasAttribute(FIELD_TYPE)){
                        String fieldType = field.getAttribute(FIELD_TYPE);
                        fieldMap.put(FIELD_TYPE, fieldType);
                        reviewEngineer.add(fieldName);
                    }
                    if(field.hasAttribute(GROUP)){
                        String group = field.getAttribute(GROUP);
                        fieldMap.put(GROUP, group);
                        GROUPLIST = GROUPLIST + group + ",";
                    }
                    engineerMapping.put(fieldName, position);
                    fieldMap.put(POSITION, position);
                    positionFields.add(fieldMap);
                }
            }
        }
    }

    static {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream("superAdmin.properties");
            props.load(is);
        } catch (Exception e) {
            logger.info("Load Properties: " + e.getMessage());
        }
    }

    public static Properties getProperties() {
        return props;
    }

    public static String getProperty(String prop) {
        return props.getProperty(prop);
    }
}
