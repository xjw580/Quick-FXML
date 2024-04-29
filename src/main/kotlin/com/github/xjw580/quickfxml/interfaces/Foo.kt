package com.github.xjw580.javafx_fast.interfaces

import com.intellij.util.xml.Attribute
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.SubTagList


/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 12:24
 */
interface Foo: DomElement {

    @Attribute("name")
    fun getName(): GenericAttributeValue<String?>?
    @SubTagList("bar")
    fun getBars(): List<Bar?>?
    fun addBar(): Bar?

}