package com.andrewbrookins.idea.wrap.ui;

import com.intellij.openapi.util.BooleanGetter;
import com.intellij.openapi.util.Comparing;

import javax.swing.*;

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider;
import sun.jvm.hotspot.types.JBooleanField;


/**
 * Plugin settings UI.
 *
 * NOTE: I could not figure out how to get WrapSettingsPanel.form to use this class
 * properly when WrapSettingsPanel is written in Kotlin!
 */
public class WrapSettingsPanel {

    private WrapSettingsProvider settingsProvider;
    private JTextField columnWidthOverrideField;
    private JPanel panel;
    private JLabel columnWidthOverrideLabel;
    private JCheckBox useMinimumRaggednessAlgorithmCheckBox;

    public WrapSettingsPanel() {
        settingsProvider = WrapSettingsProvider.getInstance();
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isModified() {
        Integer columnOverride = settingsProvider.getState().getColumnWidthOverride();
        Boolean useMinimumRaggednessAlgorithm = settingsProvider.getState().getUseMinimumRaggednessAlgorithm();
        return !Comparing.equal(columnWidthOverrideField.getText(), String.valueOf(columnOverride)) |
            !Comparing.equal(useMinimumRaggednessAlgorithmCheckBox.isSelected(), useMinimumRaggednessAlgorithm);
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
        settingsProvider.getState().setUseMinimumRaggednessAlgorithm(useMinimumRaggednessAlgorithmCheckBox.isSelected());
    }

    public void reset() {
        Integer columnOverride = settingsProvider.getState().getColumnWidthOverride();
        Boolean useMinimumRaggednessAlgorithm = settingsProvider.getState().getUseMinimumRaggednessAlgorithm();
        String overrideText = columnOverride == null ? "" : String.valueOf(columnOverride);

        columnWidthOverrideField.setText(overrideText);
        useMinimumRaggednessAlgorithmCheckBox.setSelected(useMinimumRaggednessAlgorithm);
    }
}

