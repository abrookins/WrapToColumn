package com.andrewbrookins.idea.wrap.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.ExportableApplicationComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.io.File


/**
 * Load and save settings across IDE restarts.
 */
@State(
    name = "WrapSettingsProvider",
    storages = [Storage("wrap.xml")]
)
class WrapSettingsProvider : PersistentStateComponent<WrapSettingsProvider.State>, ExportableApplicationComponent {
    private var state = State()

    override fun getState(): State? {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    override fun initComponent() {
    }

    override fun disposeComponent() {
    }

    override fun getExportFiles(): Array<File> {
        return arrayOf(File(PathManager.getOptionsPath() + File.separatorChar + "wrap.xml"))
    }

    override fun getPresentableName(): String {
        return "WrapSettingsPanel"
    }

    override fun getComponentName(): String {
        return "WrapSettingsProvider"
    }

    /**
     * All settings are stored in this inner class
     */
    class State {

        /**
         * Remember a column width override.
         */
        var columnWidthOverride: Int? = null
        var useMinimumRaggednessAlgorithm: Boolean = false
        var plaintextFileTypes: String = ".md,.markdown,.adoc,.asciidoc,.txt"
    }

    companion object {
        @JvmStatic
        fun getInstance(): WrapSettingsProvider {
            return ApplicationManager.getApplication().getComponent(WrapSettingsProvider::class.java) as WrapSettingsProvider
        }
    }
}

