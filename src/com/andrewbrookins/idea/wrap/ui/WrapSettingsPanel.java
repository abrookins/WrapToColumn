package com.andrewbrookins.idea.wrap.ui;

import com.intellij.openapi.util.Comparing;

import javax.swing.*;

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider;


/**
 * Plugin settings UI.
 */
public class WrapSettingsPanel {

    private WrapSettingsProvider settingsProvider;
    private JTextField columnWidthOverrideField;
    private JTextPane textPane;
    private JPanel panel;

    public WrapSettingsPanel() {
        settingsProvider = WrapSettingsProvider.getInstance();
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isModified() {
        return !Comparing.equal(columnWidthOverrideField.getText(), settingsProvider.getState().columnWidthOverride);
    }

    public void apply() {
        // TODO: Show an error for non-integer input.
        Integer columnWidth;

        try {
            columnWidth = Integer.parseInt(columnWidthOverrideField.getText());
        }
        catch (NumberFormatException e) {
            columnWidth = null;
        }

        settingsProvider.getState().columnWidthOverride = columnWidth;
    }

    public void reset() {
        Integer columnOverride = settingsProvider.getState().columnWidthOverride;
        String overrideText;

        if (columnOverride == null) {
            overrideText = "";
        }
        else {
            overrideText = String.valueOf(columnOverride);
        }

        columnWidthOverrideField.setText(overrideText);
    }
}

