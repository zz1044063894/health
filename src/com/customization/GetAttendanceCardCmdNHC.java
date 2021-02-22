package com.customization;

import com.engine.core.cfg.annotation.CommandDynamicProxy;
import com.engine.core.interceptor.AbstractCommandProxy;
import com.engine.core.interceptor.Command;
import com.engine.kq.cmd.attendanceEvent.GetAttendanceCardCmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.constant.Constant.EMPTY_KEY;

/**
 * @description: 卫健委补卡流程不展示非旷工数据
 * @author: JingChu
 * @createtime :2020-12-10 10:26:17
 **/
@CommandDynamicProxy(target = GetAttendanceCardCmd.class, desc = "补卡流程不展示[非旷工数据]}")
public class GetAttendanceCardCmdNHC extends AbstractCommandProxy {
    @Override
    public Object execute(Command command) {
        System.out.println("------------------------>INFO：      进入无侵入开发服务");
        //获取被代理的cmd对象
        GetAttendanceCardCmd getAttendanceCardCmd = (GetAttendanceCardCmd) command;
        //获取被代理对象参数
        Map<String, Object> param = getAttendanceCardCmd.getParams();
        //对参数做预处理


        //参数回写
        getAttendanceCardCmd.setParams(param);
        //执行标准的业务处理
        Map<String, Object> result = (Map<String, Object>) nextExecute(command);

        //加工返回值
        Map<String, String> item;
        String atteStatus;
        ArrayList arrayList = (ArrayList) result.get("cardlist");
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            item = (Map<String, String>) arrayList.get(i);
            atteStatus = item.get("atteStatus");
            if(atteStatus.indexOf("旷工")<0 && atteStatus.indexOf("漏签")<0){
            //if ((!"旷工".equals(atteStatus)) && (!"漏签".equals(atteStatus))) {
                arrayList.remove(i);
            }
            item.put("signtime",EMPTY_KEY);

        }
        result.put("cardlist", arrayList);
        return result;
    }
}
