package com.github.xjw580.quickfxml.listener

import com.github.xjw580.quickfxml.utils.JavaFXLibraryUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.psi.PsiManager
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import java.util.Timer
import java.util.TimerTask

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/5/6 10:24
 */
class MyToolWindowManagerListener(private val project: Project) : ToolWindowManagerListener {

    private val log = Logger.getInstance(MyToolWindowManagerListener::class.java)

    override fun toolWindowShown(toolWindow: ToolWindow) {
        super.toolWindowShown(toolWindow)
        DumbService.getInstance(project).runWhenSmart{
            if (JavaFXLibraryUtil.hasJavaFXClasses(project)){
                PsiManager.getInstance(project).addPsiTreeChangeListener(FxmlElementChangeListener(), project)
                val text = "Activate plugin in `${project.name}`"
                log.info(text)
                println(text)
            }else{
                val text = "${project.name} is not a javafx project"
                log.info(text)
                println(text)
            }
        }
    }

}