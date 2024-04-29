package com.github.xjw580.quickfxml.action

import com.github.xjw580.javafx_fast.interfaces.Root
import com.github.xjw580.quickfxml.listener.XmlAttributeChangeListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.vcs.update.ScopeInfo.FILES
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 16:04
 */
class GeneratedXMLCode: AnAction() {
    var count = 0
    override fun actionPerformed(e: AnActionEvent) {
        println("=================================1")
        var project = e.project
        var xmlFile = e.getData(LangDataKeys.PSI_FILE) as XmlFile
        var manager = DomManager.getDomManager(project)
        var root = manager.getFileElement(xmlFile, Root::class.java)?.rootElement
        println(root?.getFoo())
        if (project != null && count++ == 0){
            PsiManager.getInstance(project).addPsiTreeChangeListener(XmlAttributeChangeListener(project))
            println("add")
        }
        println("=================================")
    }

}