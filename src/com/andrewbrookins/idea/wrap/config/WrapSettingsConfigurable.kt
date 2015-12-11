package com.andrewbrookins.idea.wrap.config

import com.andrewbrookins.idea.wrap.ui.WrapSettingsPanel
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable

import javax.swing.JComponent


class WrapSettingsConfigurable : SearchableConfigurable {

    private var panel: WrapSettingsPanel? = null

    override fun getId(): String {
        return "wrap.settings"
    }

    override fun enableSearch(option: String): Runnable? {
        return null
    }

    override fun getDisplayName(): String {
        return "Wrap to Column"
    }

    override fun getHelpTopic(): String? {
        return "wrap.settings"
    }

    override fun createComponent(): JComponent? {
        panel = WrapSettingsPanel()
        return panel?.panel
    }

    override fun isModified(): Boolean {
        return panel != null && panel!!.isModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        if (panel != null) {
            panel?.apply()
        }
    }

    override fun reset() {
        if (panel != null) {
            panel?.reset()
        }
    }

    override fun disposeUIResources() {
        panel = null
    }
}

