package com.sw.SWAPI.util;


import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.*;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 解析XML配置
 */
public class AnalysisXML {

    public static Map<String, String> relationshipMap = new HashMap<String, String>();

    private final static String SPLIT_FLAG = "|q|q|";

    /**
     * 获取Category值
     * @param type1
     * @param type2
     * @return String
     */
    @SuppressWarnings("rawtypes")
	public String resultCategory(String type1, String type2) {
        String s = "";
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {
            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("Category.xml");
            document = reader.read(in);
            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
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

    /**
     * 获取ALM与System Weaver之间Type的对应关系
     * @param type
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Map<String, String> resultFile(String type) {
        Map<String, String> map = new HashMap<String, String>();
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("file.xml");
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
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

    /**
     * 通过System Weaver传递的Type，获取ALM中实际的Type名称
     * @param type
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public String resultType(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        String s = "";
        try {
            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("file.xml");
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
                if (stu.attribute("swr").getValue().equals(type) || stu.attribute("alm").getValue().equals(type)) {
                    for (Attribute attribute : attributes) {
                        if ("alm".equals(attribute.getName())) {
                            s = attribute.getValue();
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
     * 通过System Weaver传递的Type，获取ALM中实际的Type名称
     * @param type
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static String getAlmType(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("file.xml");
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
    @SuppressWarnings("rawtypes")
	public static String getTypeTargetState(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("file.xml");
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
    @SuppressWarnings("rawtypes")
	public static String getTypeFilterGroup(String type) {
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("file.xml");
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

    /**
     * 获取两个类型之间的关系字段
     * @param type1
     * @param type2
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static String resultRelationShipFile(String type1, String type2) {
        String RelationShipFile = "";
        // 1.创建Reader对象
        SAXReader reader = new SAXReader();
        // 2.加载xml
        Document document = null;
        try {

            // 返回读取指定资源的输入流
            InputStream in = ClassLoader.getSystemResourceAsStream("RelationshipFile.xml");
            document = reader.read(in);

            // 3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
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

}
