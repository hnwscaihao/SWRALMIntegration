package com.sw.SWAPI.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openxmlformats.schemas.drawingml.x2006.chart.STRotY;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.seimicrawler.xpath.exception.XpathSyntaxErrorException;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.List;

public class ConvertRTFToHtml {

    public static void main(String[] args) throws IOException {
//        new ConvertRTFToHtml().RTFToHtml("D:\\12484.rtf","D:\\12484");
        String path = ResourceUtils.getURL("classpath:").getPath()+"124841.htm";
        new ConvertRTFToHtml().readHtml(path);
    }

    public String RTFToHtml(String inPath,String toPath){
//        inPath = "G:\\lxg\\workProject\\SWAPI\\target\\classes\\12484.rtf";
//        inPath = inPath.replaceAll("/","\\\\\\\\");
//        inPath = inPath.substring(2,inPath.length());
//        toPath = "G:\\lxg\\workProject\\SWAPI\\target\\classes\\12484";
//        toPath = toPath.replaceAll("/","\\\\\\\\");
//        toPath = toPath.substring(2,toPath.length());
        ComThread.InitSTA();
        ActiveXComponent word = new ActiveXComponent("Word.Application");
        if(word == null){
            word = new ActiveXComponent("KWPS.Application");
        }
        word.setProperty("Visible", new Variant(false));
        Dispatch docs = word.getProperty("Documents").toDispatch();
        Dispatch doc = Dispatch.invoke(docs, "Open", Dispatch.Method,
                new Object[] { inPath, new Variant(false), new Variant(true) }, new int[1]).toDispatch();
        Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] { toPath, new Variant(10) }, new int[1]); // 这里的new
        Variant f = new Variant(false);
        ComThread.Release();
        File file = new File(toPath+".htm");
        try {
            Document docu = Jsoup.parse(file, "GBK", "http://example.com/");
            docu.attributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return toPath+".htm";
    }

    //获取的输入流保存在本地
    public String sc(InputStream is,String outfile){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outfile);
            byte[] b = new byte[1024];
            while ((is.read(b)) != -1) {
                fos.write(b);// 写入数据
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "0";
    }

    /**
     * 解析html文件
     * @return
     */
    public String readHtml(String path) throws IOException {
//        JXDocument underTest = JXDocument.create(path);
        Document document = Jsoup.parse(new File(path), "GBK");
        JXDocument jxDocument = JXDocument.create(document);
        String xpath = "//body";
        JXNode node = jxDocument.selNOne(xpath);
        String body = node.toString();
        System.out.println(body);
        return body;
    }


}
