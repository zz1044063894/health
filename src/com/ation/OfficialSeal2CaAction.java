package com.ation;

import com.alibaba.fastjson.JSON;
import com.utils.FileUtils;
import com.utils.HttpRequestUtils;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.docs.webservices.DocAttachment;
import weaver.docs.webservices.DocInfo;
import weaver.docs.webservices.DocService;
import weaver.docs.webservices.DocServiceImpl;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.HashMap;
import java.util.Map;

import static com.constant.Constant.*;


/**
 * @description: 发往CA系统 盖公章节点后action
 * @author: JingChu
 * @createtime :2020-12-21 10:05:40
 **/
public class OfficialSeal2CaAction extends BaseBean implements Action {
    String baseUrl = this.getPropValue("messageOfCa", "CA_BASE_URL");
    String tokenUrl = this.getPropValue("messageOfCa", "CA_TOKEN_URL");
    String offSealUrl = this.getPropValue("messageOfCa", "CA_OFF_SEAL_URL");
    String clientId = this.getPropValue("messageOfCa", "CA_CLIENTID");
    String appSecret = this.getPropValue("messageOfCa", "CA_APPSECRET");
    String filePath = this.getPropValue("messageOfCa", "CA_FILE_PATH");
    String departId = this.getPropValue("messageOfCa", "CA_DEPAIT_ID");
    String loginUrl = this.getPropValue("messageOfCa", "OA_LOGIN_URL");
    String username = this.getPropValue("messageOfCa", "OA_LOGIN_USERNAME");
    String pwd = this.getPropValue("messageOfCa", "OA_LOGIN_PASSWORD");
    String fileNameAdd = this.getPropValue("messageOfCa", "CA_FILENAME_ADD");

    HttpRequestUtils httpRequestUtils = new HttpRequestUtils();

    FileUtils fileUtils = new FileUtils();

    /**
     * 获取表单上的pdf文件发送到CA做公章
     *
     * @param requestInfo
     * @return
     */
    @Override
    public String execute(RequestInfo requestInfo) {


        String url = baseUrl + tokenUrl;
        String param = "clientId=" + clientId + "&appSecret=" + appSecret;
        String keyword = "国家卫生健康委员会";//盖章关键字
        String billKey = EMPTY_KEY;
        //获取accessToken接口青求
        String billTable = requestInfo.getRequestManager().getBillTableName();
        String requestId = requestInfo.getRequestid();
        String accessToken = getAccessToken(url, param);
        try {
            //先获取表单信息
            RecordSet rs = new RecordSet();
            String sql = "SELECT * FROM " + billTable + " WHERE requestid = " + requestId;

            rs.execute(sql);
            String fjId = EMPTY_KEY;//附件id
            String wd = EMPTY_KEY;//文档id
            if (rs.next()) {
                fjId = Util.null2String(rs.getString("gzwj"));
                wd = Util.null2String(rs.getString("thhwd"));
                billKey = Util.null2String(rs.getString("gjz"));
            }
            writeLog("------------------->INFO:  sql " + sql + " fjID " + fjId);
            //文档相关操作
            DocService docService = new DocServiceImpl();
            String session = docService.login(username, pwd, 0, loginUrl);
            writeLog("------------------->INFO:  session :" + session);
            //取得有权限访问的文档数
            DocInfo doc1;
            if (EMPTY_KEY.equals(fjId)) {
                /*requestInfo.getRequestManager().setMessageid("10000");
                requestInfo.getRequestManager().setMessagecontent("请联系系统管理员,未发现需要盖章文件" );
                return FAILURE_AND_CONTINUE;*/
                doc1 = docService.getDoc(-1, session);
            } else {
                doc1 = docService.getDoc(Integer.parseInt(fjId), session);
            }

            String fileNameOrg = doc1.getAttachments()[0].getFilename();
            if (!EMPTY_KEY.equals(billKey)) {
                keyword = billKey;
            }

            String pdfBase64 = doc1.getAttachments()[0].getFilecontent();
            if (!EMPTY_KEY.equals(accessToken)) {
                //accessToken不为空是表示获取accesstoken成功，调用盖章请求

                writeLog("------------------->INFO: accessToken : " + accessToken);
                url = baseUrl + offSealUrl;


                //head参数拼接
                Map<String, String> headMap = new HashMap<>(2);
                headMap.put("clientId", clientId);
                headMap.put("accessToken", accessToken);
                Map<String, String> bodyMap = new HashMap<>(3);
                //pdf数组字节的base64编码格式

                bodyMap.put("pdfBase64", pdfBase64);
                bodyMap.put("keyword", keyword);
                bodyMap.put("departId", departId);
                //bodyMap.put("moveType", EMPTY_KEY);

                Map<String, Map<String, String>> requestMap = new HashMap<>(2);

                requestMap.put("head", headMap);
                requestMap.put("body", bodyMap);
                String requestStr = JSON.toJSONString(requestMap);
                String result = sendOfficalSeal(url, requestStr);
                JSONObject jsonObj = JSONObject.fromObject(result);
                String message = jsonObj.getString("message");
                if (!CA_MESSAGE_SUCCESS.equals(message)) {
                    requestInfo.getRequestManager().setMessageid("10000");
                    requestInfo.getRequestManager().setMessagecontent("请联系系统管理员，盖章失败" + result);
                    return FAILURE_AND_CONTINUE;
                } else {
                    String content = jsonObj.getString("data");
                    if (uploadDoc(fileNameOrg, session, docService, content, billTable, requestId)) {

                    } else {
                        requestInfo.getRequestManager().setMessageid("10000");
                        requestInfo.getRequestManager().setMessagecontent("请联系系统管理员，反写上传文档失败");
                        return FAILURE_AND_CONTINUE;
                    }

                }

            } else {

                requestInfo.getRequestManager().setMessageid("10000");
                requestInfo.getRequestManager().setMessagecontent("请联系系统管理员，获取accessToken失败");
                return FAILURE_AND_CONTINUE;
            }
        } catch (Exception e) {
            requestInfo.getRequestManager().setMessageid("10000");
            requestInfo.getRequestManager().setMessagecontent("请联系系统管理员，获取accessToken失败" + e.getMessage());
            return FAILURE_AND_CONTINUE;
        }
        return SUCCESS;
    }

    /**
     * 获取accesstoken请求
     *
     * @param url   请求url
     * @param param 请求参数
     * @return accesstoken 失败返回EMPTY_KEY，成功返回内容
     */
    public String getAccessToken(String url, String param) {
        String accessToken = EMPTY_KEY;
        writeLog("------------------->INFO: url " + url + " param " + param);
        try {
            //发送get请求
            String result = httpRequestUtils.sendGet(url, param);
            JSONObject jsonObj = JSONObject.fromObject(result);
            String message = jsonObj.getString("message");
            if (CA_MESSAGE_SUCCESS.equals(message)) {
                //成功则获取accesstoken
                JSONObject data = JSONObject.fromObject(jsonObj.getString("data"));
                accessToken = data.getString("accessToken");
            } else {
                //失败记录失败日志
                writeLog("------------------->INFO:  获取accessToken失败 " + result);
            }

        } catch (Exception e) {
            writeLog("------------------->INFO:  获取accessToken失败 " + e.getMessage());
        }
        return accessToken;

    }

    /**
     * 盖公章
     *
     * @param url         请求url
     * @param requestBody 请求body参数
     * @return 请求返回的结果，如果是失败返回空字符串
     */
    public String sendOfficalSeal(String url, String requestBody) {
        writeLog("------------------->INFO: url " + url + " param " + requestBody);
        String result = EMPTY_KEY;
        try {

            result = httpRequestUtils.httpPost(url, requestBody);
            return result;

        } catch (Exception e) {
            writeLog("------------------->INFO:  盖公章失败 " + e.getMessage());
        }
        return result;
    }


    /**
     * 上传附件
     *
     * @param FileName   附件名称
     * @param session    登录session
     * @param docService 文档操作service
     * @param content    文档内容
     * @param billTable  表名
     * @param requestId  reqeustId
     * @return 返回是否成功
     */
    public Boolean uploadDoc(String FileName, String session, DocService docService, String content, String billTable, String requestId) {
        try {
            DocInfo docInfo = new DocInfo();
            DocAttachment attachment = new DocAttachment();

            String fileName = fileUtils.getFileName(FileName) + OA_FILENAME_ADD + fileUtils.getFileExtension(FileName);


            //附件信息设置attachment
            attachment.setFilename(fileName);
            attachment.setDocid(0);
            attachment.setImagefileid(0);
            attachment.setFiletype(OA_FILETYPE_PDF);
            attachment.setFilecontent(EMPTY_KEY);
            attachment.setFilerealpath(filePath);
            attachment.setFilecontent(content);
            attachment.setIszip(1);


            //文档设置docInfo
            docInfo.setId(0);
            docInfo.setDocSubject(fileUtils.getFileName(FileName) + fileNameAdd);
            docInfo.setDoccontent(fileUtils.getFileName(FileName) + fileNameAdd);
            docInfo.setAttachments(new DocAttachment[]{attachment});
            docInfo.setAccessorycount(1);
            docInfo.setDocStatus(1);
            docInfo.setDocType(1);


            int docId = docService.createDoc(docInfo, session);
            RecordSet rs = new RecordSet();
            String sql = "UPDATE " + billTable + " SET scgzhtsmj=" + docId + " WHERE requestid = " + requestId;
            rs.executeUpdate(sql);
        } catch (Exception e) {
            writeLog("------------------->INFO:  上传文档失败" + e.getMessage());
            return false;
        }
        return true;
    }

}
