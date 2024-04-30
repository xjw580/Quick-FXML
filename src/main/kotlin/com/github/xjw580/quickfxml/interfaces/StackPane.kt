package com.github.xjw580.quickfxml.interfaces

import com.intellij.util.xml.Attribute
import com.intellij.util.xml.GenericAttributeValue

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/30 7:53
 */
interface StackPane {

    @Attribute("fx:controller")
    fun getController(): GenericAttributeValue<String?>?

}