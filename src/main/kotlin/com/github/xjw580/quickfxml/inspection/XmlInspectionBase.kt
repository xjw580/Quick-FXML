package com.github.xjw580.quickfxml.inspection

import com.github.xjw580.quickfxml.utils.JavaFXLibraryUtil
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.XmlSuppressableInspectionTool
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/19 10:50
 */
abstract class XmlInspectionBase: XmlSuppressableInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        if (JavaFXLibraryUtil.hasJavaFXClasses(holder.project)){
            return createVisitor(holder, session)
        }
        return super.buildVisitor(holder, isOnTheFly);
    }

    protected abstract fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): XmlElementVisitor

}