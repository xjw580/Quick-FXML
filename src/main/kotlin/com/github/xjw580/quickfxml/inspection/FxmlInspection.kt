package com.github.xjw580.quickfxml.inspection

import com.github.xjw580.quickfxml.data.FxmlData
import com.github.xjw580.quickfxml.enums.JavaFXClassNameEnum
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlProlog
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/19 10:51
 */
class FxmlInspection: XmlInspectionBase() {

    override fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): XmlElementVisitor = FxmlVisitor()

    class FxmlVisitor: XmlRecursiveElementVisitor() {

        private var currentFxml: FxmlData? = null

        override fun visitXmlFile(file: XmlFile) {
            super.visitXmlFile(file)
            currentFxml?.let { fxml ->
                val project = file.project
                val controllerClass = JavaPsiFacade.getInstance(project).findClass(fxml.controllerFullName, GlobalSearchScope.projectScope(project))
                controllerClass?.let {
                    controllerClass as PsiClassImpl
                    val go = CoroutineScope(Dispatchers.Default)
                    val factory = JavaPsiFacade.getElementFactory(project)
                    val existFieldsList = controllerClass.ownFields
                    val existFieldsNameSet = existFieldsList.stream().map { e -> e.name }.collect(Collectors.toSet())
                    val fieldSet = fxml.fieldSet.stream().map { e -> e.name }.collect(Collectors.toSet())
                    val existImportSet = (controllerClass.parent as PsiJavaFileImpl).importList?.importStatements.stream().map { e -> e.qualifiedName }.collect(Collectors.toSet())
//                为controller导包
                    fxml.importPackageSet.forEach { packageName ->
                        if (packageName.endsWith("*") && !existImportSet.contains(packageName.removeSuffix(".*"))){
                            go.launch{
                                val importStatement = factory.createImportStatementOnDemand(packageName.removeSuffix(".*"))
                                WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                                    (controllerClass.parent as PsiJavaFileImpl).importList?.add(importStatement)
                                }
                            }.start()
                        }
                    }
//                    导入@FXML所需的包
                    if (!existImportSet.contains(JavaFXClassNameEnum.FXML.className)){
                        go.launch{
                            WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                                (controllerClass.parent as PsiJavaFileImpl).importList?.add(factory.createImportStatement(JavaPsiFacade.getInstance(project).findClass(JavaFXClassNameEnum.FXML.className, GlobalSearchScope.allScope(project)) as PsiClass))
                            }
                        }.start()
                    }
//                删除controller中无用的@FXML的成员变量
                    existFieldsList.forEach { f ->
                        val annotationsNameSet = f.annotations.stream().map { e -> e.qualifiedName }.collect(Collectors.toSet())
                        if (annotationsNameSet.contains(JavaFXClassNameEnum.FXML.className) && !fieldSet.contains(f.name)){
                            go.launch {
                                WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                                    f.delete()
                                }
                            }
                        }
                    }
//                为controller添加@FXML成员变量
                    fxml.fieldSet.forEach { f ->
                        if (!existFieldsNameSet.contains(f.name)){
                            go.launch{
                                val field = factory.createFieldFromText("@FXML private ${f.type} ${f.name};", null)
//                            导入该变量所需的包
                                fxml.importPackageSet.forEach findPackage@{ classFullName ->
                                    if (classFullName.endsWith(f.type) && !existImportSet.contains(classFullName) && !existImportSet.contains(classFullName.removeSuffix(".*"))){
                                        WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                                            (controllerClass.parent as PsiJavaFileImpl).importList?.add(factory.createImportStatement(JavaPsiFacade.getInstance(project).findClass(classFullName, GlobalSearchScope.allScope(project)) as PsiClass))
                                        }
                                        return@findPackage
                                    }
                                }
                                WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                                    controllerClass.add(field)
                                    CodeStyleManager.getInstance(project).reformat(field)
                                }
                            }.start()
                        }
                    }
                }
            }
            currentFxml = null
        }

        override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            val attributes = tag.attributes
            currentFxml?.let {
                for (attribute in attributes) {
                    if (attribute.name == "fx:controller"){
                        it.controllerFullName = attribute.value.toString()
                    }else if (attribute.name == "fx:id"){
                        it.fieldSet.add(FxmlData.FxmlFieldData(attribute.value.toString(), tag.name))
                    }
                }
            }
        }

        override fun visitXmlProlog(prolog: XmlProlog) {
            super.visitXmlProlog(prolog)
            currentFxml = FxmlData()
            val headLine = prolog.text.split("\n")
            val regex = "^<\\?import\\s+([\\w.*]+)\\?>$".toRegex()
            for (line in headLine) {
                val matchResult = regex.find(line)
                val importPackage = matchResult?.groupValues?.get(1)
                if (importPackage != null && !importPackage.contains("java.lang") && !importPackage.contains("java.util")){
                    currentFxml?.importPackageSet?.add(importPackage)
                }
            }
        }
    }
}