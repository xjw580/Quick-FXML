package com.github.xjw580.quickfxml.marker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/29 18:25
 */
class ControllerLineMarker: RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
        forNavigation: Boolean
    ) {
        println(1111111111111111)
        super.collectNavigationMarkers(elements, result, forNavigation)
    }
}