package com.sw.SWAPI.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.*;

/*
 *Dom4j解析xml
 */
public class AnalysisXML {

    private static final Log log = LogFactory.getLog(AnalysisXML.class);

    public static Map<String, String> relationshipMap = new HashMap<String, String>();

    private final static String SPLIT_FLAG = "|q|q|";

    public String resultCategory(String type1, String type2) {
        String s = "";
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        SAXReader saxReader = new SAXReader();
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("Category.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                if (stu.attribute("swr").getValue().equals(type1)||stu.attribute("alm").getValue().equals(type1)) {
                    Iterator iterator1 = stu.elementIterator();

                    while (iterator1.hasNext()) {
                        Element stuChild = (Element) iterator1.next();
                        if (stuChild.attribute("categorySWR").getValue().equals(type2)) {
                            s = stuChild.attribute("categoryalm").getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 获取关系字段
     *
     * @param currType
     * @param targetType
     * @return
     */
    public static String getRelationshipField(String currType, String targetType) {
        String relationshipField = relationshipMap.get(currType + SPLIT_FLAG + targetType);
        if (relationshipField == null) {
            relationshipField = resultRelationShipFile(currType, targetType);
            relationshipMap.put(currType + SPLIT_FLAG + targetType, relationshipField);
        }
        return relationshipField;
    }

    public Map<String, String> resultFile(String type) {
        Map<String, String> map = new HashMap<String, String>();
        String jdsx = type;
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        SAXReader saxReader = new SAXReader();
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                String s = "";
                if (stu.attribute("swr").getValue().equals(type) || stu.attribute("alm").getValue().equals(type)) {
                    Iterator iterator1 = stu.elementIterator();

                    while (iterator1.hasNext()) {
                        Element stuChild = (Element) iterator1.next();
                        map.put(stuChild.attribute("swr").getValue(), stuChild.attribute("alm").getValue());

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public String resultType(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        String s = "";
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                if (stu.attribute("swr").getValue().equals(type) || stu.attribute("alm").getValue().equals(type)) {
                    for (Attribute attribute : attributes) {
                        if (attribute.getName().equals("alm")) {
                            s = attribute.getValue();
                        }
                    }
                }
//
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String getAlmType(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element docType = (Element) iterator.next();
                String almType = docType.attribute("alm").getValue();
                String swrType = docType.attribute("swr").getValue();
                if (type.equals(swrType)) {
                    return almType;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取类型目标状态
     *
     * @param type
     * @return
     */
    public static String getTypeTargetState(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element docType = (Element) iterator.next();
                String almType = docType.attribute("alm").getValue();
                String swrType = docType.attribute("swr").getValue();
                if (almType.equals(type) || swrType.equals(type)) {
                    return docType.attribute("targetState").getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取类型过滤动态组
     *
     * @param type
     * @return
     */
    public static String getTypeFilterGroup(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element docType = (Element) iterator.next();
                String almType = docType.attribute("alm").getValue();
                String swrType = docType.attribute("swr").getValue();
                if (almType.equals(type) || swrType.equals(type)) {
                    return docType.attribute("filterGroup").getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String resultRelationShipFile(String type1, String type2) {
        String RelationShipFile = "";
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        SAXReader saxReader = new SAXReader();
        try {

            // 返回读取指定资源的输入流
            InputStream in = AnalysisXML.class.getClassLoader().getSystemResourceAsStream("RelationshipFile.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                String s = "";
                if (stu.attribute("alm").getValue().equals(type1)) {
                    Iterator iterator1 = stu.elementIterator();

                    while (iterator1.hasNext()) {
                        Element stuChild = (Element) iterator1.next();
                        if (stuChild.attribute("type").getValue().equals(type2)) {
                            RelationShipFile = stuChild.attribute("relationShipFile").getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RelationShipFile;
    }

    public static void main(String[] arg) {
        String sl = new AnalysisXML().resultCategory("Feature Function List", "c1s");
//       new AnalysisXML().resultCategory("Feature Function List");
        System.out.println(sl);
    }

}
