package com.github.xjw580.javafx_fast.interfaces

import com.intellij.util.xml.DomElement
import com.intellij.util.xml.Stubbed
import com.intellij.util.xml.SubTag

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 12:22
 */
interface Root: DomElement{

    @SubTag("foo")
    fun getFoo(): Foo?

}