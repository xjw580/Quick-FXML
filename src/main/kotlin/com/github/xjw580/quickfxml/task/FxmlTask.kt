package com.github.xjw580.quickfxml.task

import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project


/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/5/6 18:00
 */
abstract class FxmlTask(project: Project?) : Task.Backgroundable(project, "Resolve fxml and handler controller")