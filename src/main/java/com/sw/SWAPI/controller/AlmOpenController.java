package com.sw.SWAPI.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.sw.SWAPI.Error.MsgArgumentException;
import com.sw.SWAPI.damain.Project;
import com.sw.SWAPI.damain.User;
import com.sw.SWAPI.util.AnalysisXML;
import com.sw.SWAPI.util.Attachment;
import com.sw.SWAPI.util.ConvertRTFToHtml;
import com.sw.SWAPI.util.MKSCommand;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;

import javax.websocket.server.PathParam;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.sw.SWAPI.util.Obj.IsNull;
import static com.sw.SWAPI.util.ResultJson.*;

/**
 *  @author: liuxiaoguang
 *  @Date: 2020/7/16 15:28
 *  @Description: System Weaver集成API接口
 */
@RestController
@RequestMapping(value="/SWR")
public class AlmOpenController {

    private static final Log log = LogFactory.getLog(AlmOpenController.class);


    /**
     * @Description
     * @Author  liuxiaoguang
     * @Date   2020/7/16 15:33
     * @Param  []
     * @Return      com.alibaba.fastjson.JSONObject
     * @Exception   获取ALM中所有用户信息
     */
    @RequestMapping(value="/Issue", method = RequestMethod.POST)
    public JSONObject getAllUsers(@RequestBody JSONObject jsonData){
    String str  = "{\n" +
        "    \"Issue_Back\": [\n" +
        "        {\n" +
        "            \"B_Relations\": [\n" +
        "                {\n" +
        "                    \"B_Item_ID\": \"x0400000000084DA6\",\n" +
        "                    \"B_Structure_ID\": \"x0400000000084DA6\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"B_Issue_ID\": \"574\"\n" +
        "        }\n" +
        "    ],\n" +
        "    \"Status\": \"Status:100\"\n" +
        "}";
        return JSONObject.parseObject(str);
    }



    public static void main(String[] str){
        MKSCommand mks = new MKSCommand();
        mks.initMksCommand("192.168.120.128",7001,"admin","admin");
////        String isNO = mks.getProjectNameById("22324");
//        String id = mks.getDocIdsByType("SW_ID","85125614","type");
//        mks.getDocIdsByType("SW_SID","entry_x0400000000084B8D","ID");
//        String value = "{\\rtf1 \\ansi \\ansicpg936 \\deff0 \\stshfdbch1 \\stshfloch2 \\stshfhich2 \\deflang2052 \\deflangfe2052 {\\fonttbl {\\f0 \\froman \\fcharset0 \\fprq2 {\\*\\panose 02020603050405020304}Times New Roman{\\*\\falt Times New Roman};}{\\f1 \\fnil \\fcharset134 \\fprq0 {\\*\\panose 02010600030101010101}\\'cb\\'ce\\'cc\\'e5{\\*\\falt \\'cb\\'ce\\'cc\\'e5};}{\\f2 \\fswiss \\fcharset0 \\fprq0 {\\*\\panose 020f0502020204030204}Calibri{\\*\\falt Calibri};}{\\f3 \\fnil \\fcharset2 \\fprq0 {\\*\\panose 05000000000000000000}Wingdings{\\*\\falt Wingdings};}}{\\colortbl;\\red0\\green0\\blue0;\\red128\\green0\\blue0;\\red255\\green0\\blue0;\\red0\\green128\\blue0;\\red128\\green128\\blue0;\\red0\\green255\\blue0;\\red255\\green255\\blue0;\\red0\\green0\\blue128;\\red128\\green0\\blue128;\\red0\\green128\\blue128;\\red128\\green128\\blue128;\\red192\\green192\\blue192;\\red0\\green0\\blue255;\\red255\\green0\\blue255;\\red0\\green255\\blue255;\\red255\\green255\\blue255;}{\\stylesheet {\\qj \\li0 \\ri0 \\nowidctlpar \\aspalpha \\aspnum \\adjustright \\lin0 \\rin0 \\itap0 \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\dbch \\af1 \\hich \\af2 \\loch \\f2 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 \\snext0 \\sqformat \\spriority0 Normal;}{\\*\\cs10 \\rtlch \\ltrch \\snext10 \\ssemihidden \\spriority0 Default Paragraph Font;}}{\\*\\latentstyles \\lsdstimax260 \\lsdlockeddef0 \\lsdsemihiddendef1 \\lsdunhideuseddef1 \\lsdqformatdef0 \\lsdprioritydef99 {\\lsdlockedexcept \\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Normal;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 1;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 2;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 3;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 4;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 5;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 6;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 7;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 8;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 heading 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toc 9;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Normal Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footnote text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 header;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footer;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 index heading;\\lsdqformat1 \\lsdpriority0 \\lsdlocked0 caption;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 table of figures;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 envelope address;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 envelope return;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 footnote reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 line number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 page number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 endnote reference;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 endnote text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 table of authorities;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 macro;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 toa heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Bullet 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Number 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Title;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Closing;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Signature;\\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Default Paragraph Font;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 List Continue 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Message Header;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Subtitle;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Salutation;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Date;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text First Indent;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text First Indent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Note Heading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Body Text Indent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Block Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Hyperlink;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 FollowedHyperlink;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Strong;\\lsdsemihidden0 \\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Emphasis;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Document Map;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Plain Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 E-mail Signature;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Normal (Web);\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Acronym;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Address;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Cite;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Code;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Definition;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Keyboard;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Preformatted;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Sample;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Typewriter;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 HTML Variable;\\lsdunhideused0 \\lsdqformat1 \\lsdpriority0 \\lsdlocked0 Normal Table;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 annotation subject;\\lsdpriority99 \\lsdlocked0 No List;\\lsdpriority99 \\lsdlocked0 1 / a / i;\\lsdpriority99 \\lsdlocked0 1 / 1.1 / 1.1.1;\\lsdpriority99 \\lsdlocked0 Article / Section;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Simple 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Classic 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Colorful 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Columns 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 7;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table List 8;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table 3D effects 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Contemporary;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Elegant;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Professional;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Subtle 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Subtle 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Web 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Balloon Text;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority0 \\lsdlocked0 Table Theme;\\lsdpriority99 \\lsdlocked0 Placeholder Text;\\lsdpriority99 \\lsdlocked0 No Spacing;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 1;\\lsdpriority99 \\lsdlocked0 List Paragraph;\\lsdpriority99 \\lsdlocked0 Quote;\\lsdpriority99 \\lsdlocked0 Intense Quote;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 1;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 2;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 3;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 4;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 5;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority60 \\lsdlocked0 Light Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority61 \\lsdlocked0 Light List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority62 \\lsdlocked0 Light Grid Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority63 \\lsdlocked0 Medium Shading 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority64 \\lsdlocked0 Medium Shading 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority65 \\lsdlocked0 Medium List 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority66 \\lsdlocked0 Medium List 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority67 \\lsdlocked0 Medium Grid 1 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority68 \\lsdlocked0 Medium Grid 2 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority69 \\lsdlocked0 Medium Grid 3 Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority70 \\lsdlocked0 Dark List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority71 \\lsdlocked0 Colorful Shading Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority72 \\lsdlocked0 Colorful List Accent 6;\\lsdsemihidden0 \\lsdunhideused0 \\lsdpriority73 \\lsdlocked0 Colorful Grid Accent 6;}}{\\*\\generator WPS Office}{\\info {\\author Administrator}{\\operator \\'c1\\'f5\\'d0\\'a1\\'b9\\'e2}{\\creatim \\yr2020 \\mo8 \\dy12 \\hr8 \\min58 }{\\revtim \\yr2020 \\mo8 \\dy12 \\hr8 \\min59 }{\\version1 }{\\nofpages1 }}\\paperw12240 \\paperh15840 \\margl1800 \\margr1800 \\margt1440 \\margb1440 \\gutter0 \\deftab420 \\ftnbj \\aenddoc \\formshade \\dgmargin \\dghspace180 \\dgvspace156 \\dghorigin1800 \\dgvorigin1440 \\dghshow0 \\dgvshow2 \\jcompress1 \\viewkind1 \\viewscale110 \\viewscale110 \\splytwnine \\ftnlytwnine \\htmautsp \\useltbaln \\alntblind \\lytcalctblwd \\lyttblrtgr \\lnbrkrule \\nogrowautofit \\nobrkwrptbl \\wrppunct {\\*\\fchars !),.:;?]\\'7d\\'a1\\'a7\\'a1\\'a4\\'a1\\'a6\\'a1\\'a5\\'a8D\\'a1\\'ac\\'a1\\'af\\'a1\\'b1\\'a1\\'ad\\'a1\\'c3\\'a1\\'a2\\'a1\\'a3\\'a1\\'a8\\'a1\\'a9\\'a1\\'b5\\'a1\\'b7\\'a1\\'b9\\'a1\\'bb\\'a1\\'bf\\'a1\\'b3\\'a1\\'bd\\'a3\\'a1\\'a3\\'a2\\'a3\\'a7\\'a3\\'a9\\'a3\\'ac\\'a3\\'ae\\'a3\\'ba\\'a3\\'bb\\'a3\\'bf\\'a3\\'dd\\'a3\\'e0\\'a3\\'fc\\'a3\\'fd\\'a1\\'ab\\'a1\\'e9}{\\*\\lchars ([\\'7b\\'a1\\'a4\\'a1\\'ae\\'a1\\'b0\\'a1\\'b4\\'a1\\'b6\\'a1\\'b8\\'a1\\'ba\\'a1\\'be\\'a1\\'b2\\'a1\\'bc\\'a3\\'a8\\'a3\\'ae\\'a3\\'db\\'a3\\'fb\\'a1\\'ea\\'a3\\'a4}\\fet2 {\\*\\ftnsep \\pard \\plain {\\insrsid \\chftnsep \\par }}{\\*\\ftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par }}{\\*\\aftnsep \\pard \\plain {\\insrsid \\chftnsep \\par }}{\\*\\aftnsepc \\pard \\plain {\\insrsid \\chftnsepc \\par }}\\sectd \\sbkpage \\pgwsxn11906 \\pghsxn16838 \\marglsxn1800 \\margrsxn1800 \\margtsxn1440 \\margbsxn1440 \\guttersxn0 \\headery851 \\footery992 \\pgbrdropt32 \\sectlinegrid312 \\sectspecifyl \\endnhere \\pard \\plain \\qj \\li0 \\ri0 \\nowidctlpar \\aspalpha \\aspnum \\adjustright \\lin0 \\rin0 \\itap0 \\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\dbch \\af1 \\hich \\af2 \\loch \\af2 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 {\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\loch \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 Crtg}{\\fs21 \\kerning2 \\rtlch \\alang1025 \\ltrch \\loch \\af2 \\hich \\af2 \\dbch \\f1 \\lang1033 \\langnp1033 \\langfe2052 \\langfenp2052 \\par }}";
//        new AlmController().conserveFile("11111",value);
//        mks.getStaticGroup("VCU");
        try {
            mks.getProjects("/aaaaa");
        } catch (APIException e) {
            e.printStackTrace();
        }
    }
}