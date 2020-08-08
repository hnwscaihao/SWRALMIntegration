package com.sw.SWAPI.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.*;

//import org.w3c.dom.Document;

/*
*Dom4j解析xml
 */
public class AnalysisXML {

    private static final Log log = LogFactory.getLog(AnalysisXML.class);

    public List<String> resultCategory(String type){
        List<String> l = new ArrayList<>();
       String jdsx = type;
       //1.创建Reader对象
       SAXReader reader = new SAXReader();
       //2.加载xml
       Document document = null;
        SAXReader saxReader = new SAXReader();
       try {

           //返回读取指定资源的输入流
           InputStream in = com.sw.SWAPI.util.AnalysisXML.class.getClassLoader().getSystemResourceAsStream("Category.xml");
//           writeToLocal(pathxml,in);
           document = reader.read(in);

           //3.获取根节点
           Element rootElement = document.getRootElement();
           Iterator iterator = rootElement.elementIterator();
           while (iterator.hasNext()){
               Element stu = (Element) iterator.next();
               if(stu.attribute("swr").getValue().equals(type)){
                   Iterator iterator1 = stu.elementIterator();

                   while (iterator1.hasNext()){
                       Element stuChild = (Element) iterator1.next();
                       l.add(stuChild.attribute("categoryalm").getValue());
                   }
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return l;
   }

    public Map<String,String> resultFile(String type){
        Map<String,String> map = new HashMap<>();
        String jdsx = type;
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        //2.加载xml
        Document document = null;
        SAXReader saxReader = new SAXReader();
        try {

            //返回读取指定资源的输入流
            InputStream in = com.sw.SWAPI.util.AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            //3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()){
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                String s = "";
                if(stu.attribute("swr").getValue().equals(type)){
                    Iterator iterator1 = stu.elementIterator();

                    while (iterator1.hasNext()){
                        Element stuChild = (Element) iterator1.next();
                        map.put(stuChild.attribute("swr").getValue(),stuChild.attribute("alm").getValue());

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public String resultType(String type){
        Map<String,String> map = new HashMap<>();
        String jdsx = type;
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        //2.加载xml
        Document document = null;
        String s = "";
        try {

            //返回读取指定资源的输入流
            InputStream in = com.sw.SWAPI.util.AnalysisXML.class.getClassLoader().getSystemResourceAsStream("file.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            //3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()){
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                if(stu.attribute("swr").getValue().equals(type)){
                     for (Attribute attribute : attributes) {
                         if(attribute.getName().equals("alm")) {
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

    public String resultRelationShipFile(String type1,String type2){
        String RelationShipFile = "";
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        //2.加载xml
        Document document = null;
        SAXReader saxReader = new SAXReader();
        try {

            //返回读取指定资源的输入流
            InputStream in = com.sw.SWAPI.util.AnalysisXML.class.getClassLoader().getSystemResourceAsStream("RelationshipFile.xml");
//           writeToLocal(pathxml,in);
            document = reader.read(in);

            //3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()){
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();
//               System.out.println("======获取属性值======");
                String s = "";
                if(stu.attribute("alm").getValue().equals(type1)){
                    Iterator iterator1 = stu.elementIterator();

                    while (iterator1.hasNext()){
                        Element stuChild = (Element) iterator1.next();
                        if(stuChild.attribute("type").getValue().equals(type2)){
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

   public static void main(String[] arg){
       String sl = new AnalysisXML().resultRelationShipFile("System Requirement Specification Document","System Requirement Specification");
//       new AnalysisXML().resultCategory("Feature Function List");
       System.out.println(sl);
   }

}
