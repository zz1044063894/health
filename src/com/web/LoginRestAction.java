package com.web;

import com.alibaba.fastjson.JSON;
import com.engine.common.util.ParamUtil;
import com.utils.HttpRequestUtils;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static com.constant.Constant.*;

/**
 * @description: 登录相关rest接口
 * @author: JingChu
 * @createtime :2021-01-20 16:18:03
 **/
public class LoginRestAction extends BaseBean {

    String baseUrl = this.getPropValue("messageOfCa", "CA_BASE_URL");
    String loginUrl = this.getPropValue("messageOfCa", "CA_GET_PIC_LOGIN_RUL");
    String loginParam = this.getPropValue("messageOfCa", "CA_PIC_LOGIN_PARAM");
    String loginRedirect = this.getPropValue("messageOfCa", "CA_PIC_LOGIN_REDIRECT");
    String clientId = this.getPropValue("messageOfCa", "CA_CLIENTID");
    String loginResUrl = this.getPropValue("messageOfCa", "CA_GET_LOGIN_RESULT_URL");
    String accesstokenUrl = this.getPropValue("messageOfCa", "CA_GET_USER_ACCESSTOKEN_RUL");
    String accesstokenParam = this.getPropValue("messageOfCa", "CA_GET_USER_ACCESSTOKEN_PARAM");
    String userMsgUrl = this.getPropValue("messageOfCa", "CA_GET_USER_MSG_URL");
    String appSecret = this.getPropValue("messageOfCa", "CA_APPSECRET");
    String oaLoginUrl = this.getPropValue("messageOfCa", "OA_LOGIN_URL_USED");
    String oaLoginUrlUsed = this.getPropValue("messageOfCa", "OA_LOGIN_URL_USED");

    HttpRequestUtils httpRequestUtils = new HttpRequestUtils();

    @GET
    @Path("/get/pic")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLoginPic(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

        Map<String, Object> map = ParamUtil.request2Map(request);
        writeLog("------------------->INFO:  进入web action:" + this.getClass().getName());
        //存放结果集合
        HashMap<String, String> resMap = new HashMap<>();
        try {
            String url = baseUrl + loginUrl;
            String param = loginParam + loginRedirect + clientId;
            resMap = getLoginMsg(url, param);

        } catch (Exception e) {
            this.writeLog("------------------->INFO: 获取二维码失败失败 " + e.getMessage());
            return JSON.toJSONString(errorMap);
        }
        return JSON.toJSONString(resMap);
    }

    @GET
    @Path("/get/result")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLoginResult(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

        Map<String, Object> map = ParamUtil.request2Map(request);
        writeLog("------------------->INFO:  进入web action:" + this.getClass().getName());
        HashMap<String, String> resultMap = new HashMap<>();
        //存放结果集合
        Map<String, String> resMap = new HashMap<>();
        try {
            String url = baseUrl + loginResUrl;
            if(EMPTY_KEY.equals(map.get("requestId"))){
                return JSON.toJSONString("不存在requestId");
            }
            String param = "requestId=" + map.get("requestId");
            resMap = getLoginResult(url, param);

            if (resMap.size() > 0 && resMap.get("result").equals("0")) {
                //登录成功
                url = baseUrl + accesstokenUrl;
                param = accesstokenParam + "&client_id=" + clientId + "&client_secret=" + appSecret + "&code=" + resMap.get("code");
                resMap = getUserAccesstoken(url, param);
                if (resMap.size() > 0) {
                    url = baseUrl + userMsgUrl;
                    param = "access_token=" + resMap.get("accessToken");
                    resMap = getUserMsg(url, param);
                    String token = getUserToken(resMap.get("userName"), resMap.get("userIdcardNum"));
                    resultMap.put("token", token);
                    resultMap.put("url", oaLoginUrlUsed + "/wui/index.html?ssoToken=" + token + "#/main");
                } else {
                    return JSON.toJSONString("获取token失败");
                }
            } else {
                return JSON.toJSONString("登录失败");
            }


        } catch (Exception e) {
            this.writeLog("------------------->INFO:  登录失败 " + e.getMessage());
            return JSON.toJSONString(errorMap);
        }
        return JSON.toJSONString(resultMap);
    }


    /**
     * 获取登录使用的二维码等信息
     *
     * @param url   路径 eg:http://test.51trust.com/am/v2/oauth2/cs/authorize
     * @param param 参数 eg:service=userService&response_type=code&redirect_uri=null&client_id=2020121714324081
     * @return 结果map
     */
    public HashMap<String, String> getLoginMsg(String url, String param) {
        HashMap<String, String> map = new HashMap<>(2);
        try {
            //发送get请求
            String result = httpRequestUtils.sendGet(url, param);
            JSONObject jsonObj = JSONObject.fromObject(result);
            String message = jsonObj.getString("message");
            if (CA_MESSAGE_SUCCESS.equals(message)) {
                //成功则获取accesstoken
                JSONObject data = JSONObject.fromObject(jsonObj.getString("data"));
                String imgBase64 = data.getString("imgBase64");
                String requestId = data.getString("requestId");
                map.put("requestId", requestId);
                map.put("imgBase64", "data:image/jpg;base64," + imgBase64);
                this.writeLog("------------------->INFO:  获取二维码信成功 " + map.toString());
            } else {
                //失败记录失败日志
                this.writeLog("------------------->INFO:  获取二维码信息失败 " + result);
            }

        } catch (Exception e) {
            this.writeLog("------------------->INFO:  获取二维码信息失败 " + e.getMessage());
        }
        return map;

    }


    /**
     * 获取扫码结果
     *
     * @param url   请求路径 eg:http://test.51trust.com/am/v2/oauth2/cs/getAuthCode
     * @param param 请求参数 eg:requestId=222de00c5eaf41fda5cfbc149b7c2105
     * @return
     */
    public HashMap<String, String> getLoginResult(String url, String param) {
        HashMap<String, String> map = new HashMap<>(2);
        try {
            //发送get请求
            String result = httpRequestUtils.sendGet(url, param);
            JSONObject jsonObj = JSONObject.fromObject(result);
            String message = jsonObj.getString("message");
            if (CA_MESSAGE_SUCCESS.equals(message)) {
                //成功则获取accesstoken
                JSONObject data = JSONObject.fromObject(jsonObj.getString("data"));
                String resultMsg = data.getString("result");
                String code = data.getString("code");
                map.put("result", resultMsg);
                map.put("code", code);
                writeLog("------------------->INFO:  获取ca扫码登录结果临时code:" + map.toString());
            } else {
                //失败记录失败日志
                this.writeLog("------------------->INFO:  获取ca扫码登录结果临时code失败 " + result);
            }

        } catch (Exception e) {
            this.writeLog("------------------->INFO:  获取ca扫码登录结果临时code失败 " + e.getMessage());
        }
        return map;
    }


    /**
     * 获取扫码人员accesstoken
     *
     * @param url   路径 eg：http://test.51trust.com/am/v2/oauth2/access_token
     * @param param 参数 eg：client_id=2020121714324081&client_secret=2020121714342439&grant_type=authorization_code&code=961f315e4677c18820ba5a87eb8a6507
     * @return
     */
    public HashMap<String, String> getUserAccesstoken(String url, String param) {
        HashMap<String, String> map = new HashMap<>(1);
        try {
            //发送get请求
            String result = httpRequestUtils.sendGet(url, param);
            JSONObject jsonObj = JSONObject.fromObject(result);
            String message = jsonObj.getString("message");
            if (CA_MESSAGE_SUCCESS.equals(message)) {
                //成功则获取accesstoken
                JSONObject data = JSONObject.fromObject(jsonObj.getString("data"));
                String accessToken = data.getString("access_token");
                map.put("accessToken", accessToken);
                this.writeLog("------------------->INFO:  获取accessToken成功 " + data);
            } else {
                //失败记录失败日志
                this.writeLog("------------------->INFO:  获取accessToken失败 " + result);
            }

        } catch (Exception e) {
            this.writeLog("------------------->INFO:  获取accessToken失败 " + e.getMessage());
        }
        return map;
    }


    /**
     * 获取扫码人登录成功信息
     *
     * @param url   路径 eg: http://test.51trust.com/am/oauth2/tokeninfo
     * @param param 参数 eg：access_token=c3954f02d77ef76f03c8f45187be5156
     * @return
     */
    public HashMap<String, String> getUserMsg(String url, String param) {
        HashMap<String, String> map = new HashMap<>(2);
        try {
            //发送get请求
            String result = httpRequestUtils.sendGet(url, param);
            JSONObject jsonObj = JSONObject.fromObject(result);
            String message = jsonObj.getString("message");
            if (CA_MESSAGE_SUCCESS.equals(message)) {
                //成功则获取accesstoken
                JSONObject data = JSONObject.fromObject(jsonObj.getString("data"));
                String userIdcardNum = data.getString("userIdcardNum");
                String userName = data.getString("userName");
                map.put("userIdcardNum", userIdcardNum);
                map.put("userName", userName);
                writeLog("------------------->INFO:  获取ca用户信息:" + data.toString());
            } else {
                //失败记录失败日志
                this.writeLog("------------------->INFO:  获取ca用户信息失败 " + result);
            }

        } catch (Exception e) {
            this.writeLog("------------------->INFO:  获取ca用户信息失败 " + e.getMessage());
        }
        return map;
    }

    /**
     * 获取用户token
     *
     * @param userName 用户姓名
     * @param cardId   身份证号
     * @return token内容
     * @throws Exception
     */
    public String getUserToken(String userName, String cardId) throws Exception {
        /*String url = "http://127.0.0.1:89/ssologin/getToken";
        Map<String, String> params = new HashMap<>();
        params.put("appid", "ssss");
        params.put("loginid", "1");

        String s = httpRequestUtils.httpPost(url, JSON.toJSONString(params));
        this.writeLog(s);
        String url1 = "http://127.0.0.1:89/wui/index.html?ssoToken=XXX#/main";*/
        String sql = " SELECT loginid FROM hrmresource WHERE certificatenum='" + cardId + "'" ;
        //+
        //                " AND lastname='韦利东'"
        RecordSet rs = new RecordSet();
        String loginid = "";
        rs.execute(sql);
        if (rs.next()) {
            loginid = Util.null2String(rs.getString("loginid"));
        }
        String url =oaLoginUrl +"/ssologin/getToken";
        Map<String, String> params = new HashMap<>();
        params.put("appid", "Client1");
        params.put("loginid", loginid);
        String s = httpRequestUtils.doPost(url, params).replaceAll("\n|\r", "");
        this.writeLog("------------------->INFO: 获取用户token信息 " + s);
        return s;
    }

}
