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

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 将System Weaver传递的RTF文件转换为Html富文本信息
 */
public class ConvertRTFToHtml {

	private ActiveXComponent word ;
	
	/** 图片附件路径*/
	private static String imgSrc = "mks:///item/field?fieldid=Text Attachments&attachmentname=";
    /**
     * 将RTF转换为Html
     * @param inPath
     * @param toPath
     * @return
     */
    @SuppressWarnings("unused")
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

    /**
     * 将数据保存到本地
     * @param is
     * @param outfile
     * @return
     */
    public static void saveData2File(InputStream is,String outfile){
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
    }

    /**
     * 读取Html文件
     * @param path
     * @return
     * @throws IOException
     */
    public String readHtml(String path) throws IOException {
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
