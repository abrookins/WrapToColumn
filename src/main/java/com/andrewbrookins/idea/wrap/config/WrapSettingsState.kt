package com.andrewbrookins.idea.wrap.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.NotNull


/**
 * Load and save settings across IDE restarts.
 */
@State(
    name = "WrapSettingsProvider",
    storages = [Storage("wrap.xml")]
)
class WrapSettingsState : PersistentStateComponent<WrapSettingsState> {
    var columnWidthOverride: Int = 80
    var useMinimumRaggednessAlgorithm: Boolean = false
    var plaintextFileTypes: String = ".md,.markdown,.adoc,.asciidoc,.txt"

    override fun getState(): WrapSettingsState {
        return this
    }

    override fun loadState(@NotNull state: WrapSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun getInstance(): WrapSettingsState {
            return ApplicationManager.getApplication().getComponent(WrapSettingsState::class.java) as WrapSettingsState
        }
    }
}

