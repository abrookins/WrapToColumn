package com.andrewbrookins.idea.wrap

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler


class WrapAction : EditorAction(WrapAction.WrapHandler()) {

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ActionPlaces.isPopupPlace(e.place)) {
            e.presentation.isVisible = e.presentation.isEnabled
        }
    }

    private class WrapHandler : EditorActionHandler() {
        override fun execute(editor: Editor, dataContext: DataContext?) {
            ApplicationManager.getApplication().runWriteAction(object : Runnable {
                override fun run() {
                    val project = LangDataKeys.PROJECT.getData(dataContext!!)
                    val document = editor.document
                    val selectionModel = editor.selectionModel
                    val columnWidthOverride = WrapSettingsProvider.getInstance().state?.columnWidthOverride
                    val useMinimumRaggednessAlgorithm = WrapSettingsProvider.getInstance().state?.useMinimumRaggednessAlgorithm ?: false
                    val columnWidth = columnWidthOverride ?: editor.settings.getRightMargin(project)
                    val tabWidth = editor.settings.getTabSize(project)

                    if (!selectionModel.hasSelection()) {
                        selectionModel.selectLineAtCaret()
                    }

                    val text = selectionModel.selectedText ?: return
                    if (text.isBlank()) {
                        return
                    }

                    val wrapper = CodeWrapper(width = columnWidth, tabWidth = tabWidth, useMinimumRaggedness = useMinimumRaggednessAlgorithm)
                    val wrappedText = wrapper.wrap(text)

                    document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, wrappedText)
                }
            })
        }
    }
}
