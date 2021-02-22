package com.api.login.cmd.login;

import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.hrm.biz.HrmSanyuanAdminBiz;
import org.apache.commons.lang.StringUtils;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.login.VerifyLogin;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetQCLoginStatusCmdOld extends AbstractCommonCommand<Map<String, Object>> {
  HttpServletRequest request = null;
  HttpServletResponse response = null;
  ServletContext application = null;
  public GetQCLoginStatusCmdOld(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, ServletContext application) {
    this.params = params;
    this.request = request;
    this.response = response;
    this.application = application;
  }

  @Override
  public Map<String, Object> execute(CommandContext commandContext) {
    Map<String, Object> retmap = new HashMap<String, Object>();
    HttpSession session = request.getSession(true);
    String loginkey = Util.null2String(params.get("loginkey"));
    String isIE = Util.null2String(params.get("isie"));

    String agent = request.getHeader("user-agent");
    if(agent.indexOf("rv:11") == -1 && agent.indexOf("MSIE") == -1){
      isIE = "false";
    }

    if(agent.indexOf("rv:11") > -1 && agent.indexOf("Mozilla") > -1){
      isIE = "true";
    }

    if(!isIE.equals("false")){
      isIE = "true";
    }
    session.setAttribute("browser_isie",isIE);

    String appId = Util.null2String(params.get("appid")) ;
    if(StringUtils.isNotBlank(appId)){
      if(StringUtils.isBlank(loginkey)){
        retmap.put("status","0");
        return retmap;
      }
      String logind = SSOUser(loginkey,session) ;
      if(StringUtils.isBlank(logind)){
        retmap.put("status","0");
        return retmap;
      }
      retmap.put("status","1");
      return retmap;
    }

    if (loginkey != null && !"".equals(loginkey)) {
      Object obj = null ;
      try{
        obj =  new weaver.login.VerifyLogin4QCode().getUserCheck(application,request,response);
      }catch(Exception e){
        retmap.put("status","0");
        writeLog(e);
        return retmap;
      }

      if(obj == null){	//兼容微信端老版本扫码
        obj = application.getAttribute(loginkey);
      }
      if (obj != null) {
        User user = (User) obj;
        String langid = Util.null2String(params.get("langid"));
        if (!langid.equals("") && !langid.equals("undefined")) {
          user.setLanguage(Util.getIntValue(langid, 7));
        }

        request.getSession(true).setAttribute("weaver_user@bean", user);
        if (user.getUID() != 1) {  //is not sysadmin
          List accounts = new VerifyLogin().getAccountsById(user.getUID());
          request.getSession(true).setAttribute("accounts", accounts);
        }
        retmap.put("status","1");
        //三员管理员--参数 start
          boolean hasRight=HrmSanyuanAdminBiz.hasSanYuanRight(user);
          if(hasRight){
            retmap.put("needJumpToBackstage","true");
          }
        //over
      } else {
        String logout = (String)application.getAttribute(loginkey + ":logout");
        if (logout != null && !"".equals(logout)) {
          request.getSession(true).removeAttribute("weaver_user@bean");
          application.removeAttribute(loginkey + ":logout");
          logout = "9";
        } else {
          logout = "0";
        }
        retmap.put("status",logout+"");
        //登录成功后，只登录一次
        application.removeAttribute(loginkey);
      }
    } else {
        retmap.put("status","0");
    }
    return retmap;
  }

  private String SSOUser(String loginkey,HttpSession session){
    String qSql = "select loginid from QRCodeComInfo where loginkey=?" ;
    RecordSet rs = new RecordSet() ;
    rs.executeQuery(qSql,loginkey) ;
    if(!rs.next()) return null ;
    String loginid = rs.getString("loginid") ;

    rs.execute("delete from QRCodeComInfo where loginkey='"+loginkey+"'") ;

    session.setAttribute("_SSO_HRM_LOGINID_",loginid);
    return loginid ;
  }

  @Override
  public BizLogContext getLogContext() {
    return null;
  }

}
