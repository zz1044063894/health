package com.impl;

import com.weaverboot.frame.ioc.anno.classAnno.WeaIocReplaceComponent;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceAfter;
import com.weaverboot.frame.ioc.handler.replace.weaReplaceParam.impl.WeaAfterReplaceParam;

/**
 * @description: RequestAttentionAction rest接口无侵入
 * @author: JingChu
 * @createtime :2020-12-15 16:22:02
 **/
@WeaIocReplaceComponent("RequestAttentionAction")
public class RequestAttentionActionImpl {
    //这个是接口后置方法，大概的用法跟前置方法差不多，稍有差别
    //注解名称为WeaReplaceAfter
    //返回类型必须为String
    //参数叫WeaAfterReplaceParam，这个类前四个参数跟前置方法的那个相同，不同的是多了一个叫data的String，这个是那个接口执行完返回的报文
    //你可以对那个报文进行操作，然后在这个方法里return回去
    @WeaReplaceAfter(value = "/api/workflow/requestAttention/getAttentionTypeSet", order = 1)
    public String after(WeaAfterReplaceParam weaAfterReplaceParam) {
        String data = weaAfterReplaceParam.getData();//这个就是接口执行完的报文

        data = data.replaceAll("\"checked\":\"false\",\"id\":\"1\"","\"checked\":\"true\",\"id\":\"1\"");
        System.out.println(1);
        return data;
    }


}
