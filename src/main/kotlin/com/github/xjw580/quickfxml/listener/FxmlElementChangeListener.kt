package com.github.xjw580.quickfxml.listener

import com.github.xjw580.quickfxml.task.FxmlTask
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlFileImpl
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.search.GlobalSearchScope

/**
 * fxml文件元素改变监听器
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 17:58
 */
class FxmlElementChangeListener : PsiTreeChangeAdapter() {

    override fun childReplaced(event: PsiTreeChangeEvent) {
        if (event.parent is XmlAttributeValueImpl){
            val property = (event.parent as XmlAttributeValueImpl).parent.text
            if (property.startsWith("fx:id")){
                val id = getId(property)
                if (id.isBlank()){
                    return
                }
                handleField(event, id, (event.parent.parent.parent as XmlTagImpl).name, false)
            }else if (property.startsWith("on")){
                val methodName = getMethodName(property)
                if (methodName.isBlank()){
                    return
                }
                handleMethod(event, methodName, false)
            }
        }
    }

    override fun childAdded(event: PsiTreeChangeEvent) {
        handelChild(event, false)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        handelChild(event, true)
    }

    private fun handelChild(event: PsiTreeChangeEvent, isRemove: Boolean){
        if (event.child is XmlTagImpl){
            val xmlTagImpl = event.child as XmlTagImpl
            val idProperty = xmlTagImpl.getAttribute("fx:id")
            idProperty?.let {
                val id = getId(idProperty.text)
                if (id.isBlank()){
                    return@let
                }
                handleField(event, id, (event.child as XmlTagImpl).name, isRemove)
            }
            xmlTagImpl.attributes.forEach { xmlAttribute ->
                if (xmlAttribute.name.startsWith("on")){
                    val methodName = getMethodName(xmlAttribute.text)
                    handleMethod(event, methodName, isRemove)
                }
            }
        }else if (event.child is XmlAttributeImpl){
            val property = event.child.text
            if (property.startsWith("fx:id")){
                val id = getId(property)
                if (id.isBlank()){
                    return
                }
                handleField(event, id, (event.parent as XmlTagImpl).name, isRemove)
            }else if (property.startsWith("on")){
                val method = getMethodName(property)
                if (method.isBlank()){
                    return
                }
                handleMethod(event, method, isRemove)
            }
        }
    }

    private fun handleMethod(event: PsiTreeChangeEvent, methodName: String, isRemove: Boolean){
        if (methodName.isBlank()){
            return
        }

        val xmlFileImpl = event.file as XmlFileImpl
        val controllerProperty = xmlFileImpl.rootTag?.getAttribute("fx:controller")?.text
        controllerProperty?.let {
            val project = xmlFileImpl.project
            val controllerClass = JavaPsiFacade.getInstance(project).findClass(getControllerName(it), GlobalSearchScope.projectScope(project)) ?: return
            controllerClass as PsiClassImpl
            val ownMethods = controllerClass.ownMethods
            var task: FxmlTask? = null

            if (isRemove){
                ownMethods.forEach { method ->
                    if (method.name == methodName){
                        task = object : FxmlTask(project){
                            override fun run(indicator: ProgressIndicator) {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    method.delete()
                                }
                            }
                        }
                        return@forEach
                    }
                }
            }else{
                val oldMethodName:String? = event.oldChild?.text?.removePrefix("#")
                var removeMethod: PsiMethod? = null
                oldMethodName?.let {
                    ownMethods.forEach{ method ->
                        if (method.name == oldMethodName){
                            removeMethod = method
                            return@forEach
                        }
                    }
                }

                val factory = JavaPsiFacade.getElementFactory(project)
                val newMethod = factory.createMethodFromText("""
                    @FXML
                    protected void ${methodName}(){
                    }
                """.trimIndent(), controllerClass)
                task = addOrReplaceElement(project, removeMethod, newMethod, controllerClass)
            }

            task?.let { t ->
                ProgressManager.getInstance().run(t)
            }
        }
    }

    private fun handleField(event: PsiTreeChangeEvent, id: String, tagName: String, isRemove: Boolean){
        if (id.isBlank() || tagName.isBlank()){
            return
        }

        val xmlFileImpl = event.file as XmlFileImpl
        val controllerProperty = xmlFileImpl.rootTag?.getAttribute("fx:controller")?.text
        controllerProperty?.let {
            val project = xmlFileImpl.project
            val controllerClass = JavaPsiFacade.getInstance(project).findClass(getControllerName(it), GlobalSearchScope.projectScope(project)) ?: return
            controllerClass as PsiClassImpl
            val ownFields = controllerClass.ownFields
            val task: FxmlTask?

            if (isRemove){
                task = deleteElement(project, ownFields, id, controllerClass.parent as PsiFile)
            }else{
                val oldId:String? = event.oldChild?.text
                var removeField: PsiField? = null
                oldId?.let {
                    ownFields.forEach{ field ->
                        if (field.name == oldId){
                            removeField = field
                            return@forEach
                        }
                    }
                }

                val factory = JavaPsiFacade.getElementFactory(project)
                val newField = factory.createFieldFromText("@FXML private $tagName ${id};", null)
                task = addOrReplaceElement(project, removeField, newField, controllerClass)
            }

            task?.let { t ->
                ProgressManager.getInstance().run(t)
            }
        }
    }

    private fun getControllerName(controllerProperty: String):String{
        return controllerProperty.substring(14).removeSuffix("\"").removePrefix("\"")
    }

    private fun getId(idProperty: String):String{
        return idProperty.substring(6).removeSuffix("\"").removePrefix("\"")
    }

    private fun getMethodName(methodProperty: String):String{
        val split = methodProperty.split("=")
        if (split.size < 2){
            return ""
        }
        return split[1].removePrefix("\"#").removeSuffix("\"")
    }

    private fun addOrReplaceElement(project: Project, oldElement: PsiElement?, newElement: PsiElement, psiClass: PsiClass): FxmlTask {
        return object : FxmlTask(project){
            override fun run(indicator: ProgressIndicator) {
                val runnable:Runnable
                val commandName:String
                val groupID:String
                if (oldElement == null){
                    runnable = Runnable{
                        psiClass.add(newElement)
                        CodeStyleManager.getInstance(project).reformat(newElement)
                    }
                    commandName = "Add $newElement"
                    groupID = "add element"
                }else{
                    runnable = Runnable{
                        oldElement.replace(newElement)
                    }
                    commandName = "Replace $oldElement to $newElement"
                    groupID = "replace element"
                }
                WriteCommandAction.runWriteCommandAction(project, commandName, groupID, runnable, psiClass.parent as PsiFile?)
            }
        }
    }

    private fun deleteElement(project: Project, list: List<PsiNamedElement>, name: String, psiFile: PsiFile): FxmlTask?{
        list.forEach{
            if (it.name == name){
                return object : FxmlTask(project){
                    override fun run(indicator: ProgressIndicator) {
                        WriteCommandAction.runWriteCommandAction(project, "Delete $name", "delete element", {
                            it.delete()
                        }, psiFile)
                    }
                }
            }
        }
        return null
    }

}