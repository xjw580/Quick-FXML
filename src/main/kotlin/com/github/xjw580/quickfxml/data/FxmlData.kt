package com.github.xjw580.quickfxml.data

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/19 10:52
 */
data class FxmlData(
    var controllerFullName:String = "",
    val importPackageSet:MutableSet<String> = mutableSetOf(),
    val fieldSet:MutableSet<FxmlFieldData> = mutableSetOf(),
){

    data class FxmlFieldData(
        var name:String,
        var type:String,
    )

}