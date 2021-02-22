package com.constant;

import java.util.HashMap;

/**
 * @description: 静态常量
 * @author: JingChu
 * @createtime :2020-12-22 10:26:27
 **/
public class Constant {
    //CA 返回成功状态，返回成功信息
    public static final String CA_STATUS_SUCCESS = "0";
    public static final String CA_MESSAGE_SUCCESS = "success";

    //oa附件类型 pdf
    public static final String OA_FILETYPE_PDF = "application/pdf";
    //oa盖章文件名称新增
    public static final String OA_FILENAME_ADD = "(盖章)";

    public static final String EMPTY_KEY = "";

    //失败返回值map
    public final static HashMap<String,String> errorMap = new HashMap();
    static {
        errorMap.put("msg", "接口调用失败");
        errorMap.put("code", "-1");
    }
    //成功返回值map
    public final static HashMap<String,String> successMap = new HashMap();
    static {
        successMap.put("msg", "接口调用成功");
        successMap.put("code", "0");
    }
}
