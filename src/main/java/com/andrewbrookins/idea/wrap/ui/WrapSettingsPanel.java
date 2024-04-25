package com.andrewbrookins.idea.wrap.ui;

import com.andrewbrookins.idea.wrap.config.WrapSettingsState;
import com.intellij.openapi.util.Comparing;

import javax.swing.*;
import java.util.Objects;


/**
 * Plugin settings UI.
 *
 * NOTE: I could not figure out how to get WrapSettingsPanel.form to use this class
 * properly when WrapSettingsPanel is written in Kotlin!
 */
public class WrapSettingsPanel {

    private WrapSettingsState settingsProvider;
    private JTextField columnWidthOverrideField;
    private JTextField plaintextFileTypesField;
    private JPanel panel;
    private JLabel columnWidthOverrideLabel;
    private JCheckBox useMinimumRaggednessAlgorithmCheckBox;
    private JLabel plaintextFileTypesLabel;

    public WrapSettingsPanel() {
        settingsProvider = WrapSettingsState.getInstance();
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isModified() {
        Integer columnOverride = Objects.requireNonNull(settingsProvider.getState()).getColumnWidthOverride();
        Boolean useMinimumRaggednessAlgorithm = settingsProvider.getState().getUseMinimumRaggednessAlgorithm();
        String plaintextFileTypes = settingsProvider.getState().getPlaintextFileTypes();
        return !Objects.equals(columnWidthOverrideField.getText(), String.valueOf(columnOverride)) |
            !Objects.equals(plaintextFileTypesField.getText(), plaintextFileTypes) |
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

        Objects.requireNonNull(settingsProvider.getState()).setColumnWidthOverride(columnWidth);
        settingsProvider.getState().setPlaintextFileTypes(plaintextFileTypesField.getText());
        settingsProvider.getState().setUseMinimumRaggednessAlgorithm(useMinimumRaggednessAlgorithmCheckBox.isSelected());
    }

    public void reset() {
        Integer columnOverride = Objects.requireNonNull(settingsProvider.getState()).getColumnWidthOverride();
        boolean useMinimumRaggednessAlgorithm = settingsProvider.getState().getUseMinimumRaggednessAlgorithm();
        String plaintextFileTypes = settingsProvider.getState().getPlaintextFileTypes();
        String overrideText = columnOverride == null ? "" : String.valueOf(columnOverride);

        columnWidthOverrideField.setText(overrideText);
        useMinimumRaggednessAlgorithmCheckBox.setSelected(useMinimumRaggednessAlgorithm);
        plaintextFileTypesField.setText(plaintextFileTypes);
    }
}

