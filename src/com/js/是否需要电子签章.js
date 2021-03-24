/**
 * @description:
 *
 * @author: JingChu
 *
 * @createtime :2021-02-24 16:06:19
 **/

var sfsydzgz = WfForm.convertFieldNameToId("sfsydzgz");//是否需要电子盖章
jQuery(document).ready(function () {

    WfForm.registerCheckEvent(WfForm.OPER_SUBMIT, function (callback) {
        var sfsydzgzValue = WfForm.getFieldValue(sfsydzgz);
        if(sfsydzgzValue && sfsydzgzValue==0){
            window.top.Dialog.alert("批准成功后请至待办列表中找到此流程，核对盖章后pdf文件是否正确。");
        }
        callback();

    });
});
