package com.andrewbrookins.idea.wrap.config;

import com.andrewbrookins.idea.wrap.ui.WrapSettingsPanel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;


public class WrapSettingsConfigurable implements SearchableConfigurable {

    private WrapSettingsPanel panel;

    public WrapSettingsConfigurable() {
    }

    @Override
    @NotNull
    public String getId() {
        return "wrap.settings";
    }

    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Wrap to Column";
    }

    @Override
    public String getHelpTopic() {
        return "wrap.settings";
    }

    @Override
    public JComponent createComponent() {
        panel = new WrapSettingsPanel();
        return panel.getPanel();
    }

    @Override
    public boolean isModified() {
        return panel != null && panel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (panel != null) {
            panel.apply();
        }
    }

    @Override
    public void reset() {
        if (panel != null) {
            panel.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }
}

