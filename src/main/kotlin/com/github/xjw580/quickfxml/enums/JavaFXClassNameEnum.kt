package com.github.xjw580.quickfxml.enums

/**
 * @author 肖嘉威 xjw580@qq.com
 * @date 2024/4/19 10:51
 */
sealed class JavaFXClassNameEnum {

    abstract val className: String

    data object FXML : JavaFXClassNameEnum() {
        override val className: String
            get() = "javafx.fxml.FXML"
    }

}