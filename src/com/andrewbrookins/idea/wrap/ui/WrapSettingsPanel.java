package com.andrewbrookins.idea.wrap.ui;

import com.intellij.openapi.util.Comparing;

import javax.swing.*;

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider;


/**
 * Plugin settings UI.
 *
 * NOTE: I could not figure out how to get WrapSettingsPanel.form to use this class
 * properly when WrapSettingsPanel is written in Kotlin!
 */
public class WrapSettingsPanel {

    private WrapSettingsProvider settingsProvider;
    private JTextField columnWidthOverrideField;
    private JTextPane textPane;
    private JPanel panel;
    private JLabel columnWidthOverrideLabel;

    public WrapSettingsPanel() {
        settingsProvider = WrapSettingsProvider.getInstance();
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isModified() {
        Integer columnOverride = settingsProvider.getState().getColumnWidthOverride();
        return !Comparing.equal(columnWidthOverrideField.getText(), String.valueOf(columnOverride));
    }

    public void apply() {
        // TODO: Show an error for non-integer input.
        Integer columnWidth;

        try {
            columnWidth = Integer.parseInt(columnWidthOverrideField.getText());
        } catch (NumberFormatException e) {
            columnWidth = null;
        }

        settingsProvider.getState().setColumnWidthOverride(columnWidth);
    }

    public void reset() {
        Integer columnOverride = settingsProvider.getState().getColumnWidthOverride();
        String overrideText = columnOverride == null ? "" : String.valueOf(columnOverride);

        columnWidthOverrideField.setText(overrideText);
    }
}

