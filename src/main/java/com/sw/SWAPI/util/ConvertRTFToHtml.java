package com.sw.SWAPI.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.*;
import java.util.List;

public class ConvertRTFToHtml {

	private ActiveXComponent word ;
	
	/** 图片附件路径*/
	private static String imgSrc = "mks:///item/field?fieldid=Text Attachments&attachmentname=";
	
    public static void main(String[] args) throws IOException {
    }

    public String RTFToHtml(String inPath,String toPath){
        ComThread.InitSTA();
        if(word == null || word.m_pDispatch == 0){
            word = new ActiveXComponent("KWPS.Application");
        }
        word.setProperty("Visible", new Variant(false));
        Dispatch docs = word.getProperty("Documents").toDispatch();
        Dispatch doc = Dispatch.invoke(docs, "Open", Dispatch.Method,
                new Object[] { inPath, new Variant(false), new Variant(true) }, new int[1]).toDispatch();
        Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] { toPath, new Variant(10) }, new int[1]); // 这里的new
        Variant f = new Variant(false);
//        ComThread.Release();

        try {
            File file = new File(toPath+".htm");
            Document docu = Jsoup.parse(file, "GBK", "http://example.com/");
            docu.attributes();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            word.invoke("Quit", 0);
            ComThread.Release();
        }
        return toPath+".htm";
    }

    //获取的输入流保存在本地
    public static String sc(InputStream is,String outfile){
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
        dealImg(document);//处理图片信息
        JXDocument jxDocument = JXDocument.create(document);
        String xpath = "//body";
        JXNode node = jxDocument.selNOne(xpath);
        String body = node.toString();
        System.out.println(body);
        return body;
    }
    
    /**
	 * 处理图片，更换路径 
	 * 
	 * @param doc
	 * @throws Exception
	 */
	public void dealImg(Document doc){
		Elements imgs = doc.select("img");
		for (Element img : imgs) {
			String src = img.attr("src");
			src = imgSrc + src.substring(src.lastIndexOf("/") + 1, src.length());
			img.attr("src", src);
		}
	}

}
