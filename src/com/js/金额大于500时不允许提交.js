/**
 * @description: 表单金额字段大于500不允许提交
 *
 * @author: JingChu
 *
 * @createtime :2020-12-10 15:31:04
 **/
var zyshj = WfForm.convertFieldNameToId("zyshj");//总预算合计
jQuery(document).ready(function () {

    WfForm.registerCheckEvent(WfForm.OPER_SAVE + "," + WfForm.OPER_SUBMIT, function (callback) {
        var zyshjValue = WfForm.getFieldValue(zyshj);
        if (zyshjValue*1.0>=500) {
            window.top.Dialog.alert("用于500元以下（不含500元）的报销");
        } else {
            callback();
        }
    });
});