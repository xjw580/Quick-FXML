package com.github.xjw580.quickfxml.utils

import com.github.xjw580.quickfxml.enums.JavaFXClassNameEnum
import com.intellij.java.library.JavaLibraryUtil
import org.jetbrains.annotations.Nullable
import com.intellij.openapi.project.Project

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/19 10:49
 */
object JavaFXLibraryUtil {

    fun hasJavaFXClasses(project: @Nullable Project?) = JavaLibraryUtil.hasLibraryClass(project, JavaFXClassNameEnum.FXML.className)

}