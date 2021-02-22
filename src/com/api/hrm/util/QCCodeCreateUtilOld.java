package com.api.hrm.util;

import com.cloudstore.dev.api.util.EMManager;
import org.apache.commons.lang3.StringUtils;
import weaver.file.Prop;
import weaver.general.BaseBean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * "ecologylogin:%s,actionName:QR_LOGIN,randomNumber:%s,bizSN:-1"
 */
public class QCCodeCreateUtilOld {


    private static String qrcode_config;

    static {
        qrcode_config = Prop.getPropValue("Others", "qrcode_config");
        new BaseBean().writeLog("qrcode_config>>>" + qrcode_config);
    }


    public static Map<String, String> getE9LoginQcCode() {
        String uid = UUID.randomUUID().toString();
        String random = Integer.toString(uid.hashCode());
        //String ec_login_url = "/spa/hrm/static4mobile/index.html#/qrLogin?loginkey="+uid;
        String em_sys_id = "";
        try {
            EMManager manager = new EMManager();
            Map<String, String> data = manager.getEMData();
            em_sys_id = data.get(EMManager.ec_id);
        } catch (Exception e) {//未部署EM7

        }
        Map<String, String> params = new HashMap<String, String>();

        if (StringUtils.isBlank(qrcode_config)) {
            params.put("text", String.format("ecologylogin:%s,actionName:QR_LOGIN,randomNumber:%s,bizSN:-1,em_sys_id:%s", uid, random, em_sys_id));
        } else {
            params.put("text", qrcode_config.replace("$qrcode$", uid));
        }

        params.put("loginkey", uid);

        return params;
    }

}
