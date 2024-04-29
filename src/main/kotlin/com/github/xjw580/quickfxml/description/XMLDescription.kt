package com.github.xjw580.quickfxml.description

import com.github.xjw580.javafx_fast.interfaces.Root
import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 14:06
 */
class XMLDescription() : DomFileDescription<Root>(Root::class.java, "root", "") {

    override fun isMyFile(file: XmlFile, module: Module?): Boolean {
        var name = file.rootTag?.name
        println("name:" + name)
        println("rootTagName:" + rootTagName)
        println("result:" + (name != null && name == rootTagName))
        return name != null && name == rootTagName
    }
}