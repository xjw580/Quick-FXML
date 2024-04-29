package com.github.xjw580.quickfxml.listener

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 17:58
 */
class XmlAttributeChangeListener(project: Project) : PsiTreeChangeAdapter() {

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        super.childrenChanged(event)
        println("child changed")
    }


}