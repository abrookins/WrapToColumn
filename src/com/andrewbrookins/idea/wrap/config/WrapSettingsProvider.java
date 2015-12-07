package com.andrewbrookins.idea.wrap.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ExportableApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;

import java.io.File;


/**
 * Load and save settings across IDE restarts.
 */
@State(
    name = "WrapSettingsProvider",
    storages = {
        @Storage(
            file = StoragePathMacros.APP_CONFIG + "/wrap.xml"
        )}
)
public class WrapSettingsProvider implements PersistentStateComponent<WrapSettingsProvider.State>,
    ExportableApplicationComponent {
    private State state = new State();

    public static WrapSettingsProvider getInstance() {
        return ApplicationManager.getApplication().getComponent(WrapSettingsProvider.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(State state) {
        this.state = state;
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    public File[] getExportFiles() {
        return new File[]{new File(PathManager.getOptionsPath() + File.separatorChar + "wrap.xml")};
    }

    @Override
    public String getPresentableName() {
        return "WrapSettingsPanel";
    }

    @Override
    public String getComponentName() {
        return "WrapSettingsProvider";
    }

    /**
     * All settings are stored in this inner class
     */
    public static class State {

        /**
         * Remember a column width override.
         */
        public Integer columnWidthOverride = null;
    }
}

