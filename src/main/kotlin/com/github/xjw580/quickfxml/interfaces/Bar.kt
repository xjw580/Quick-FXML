package com.github.xjw580.javafx_fast.interfaces

import com.intellij.util.xml.Attribute
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue



/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 12:24
 */
interface Bar: DomElement {

    fun getValue(): String?
    fun setValue(s: String?)
    @Attribute("name")
    fun getName(): GenericAttributeValue<String?>?

}