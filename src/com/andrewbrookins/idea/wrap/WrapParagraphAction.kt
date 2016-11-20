package com.andrewbrookins.idea.wrap

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.TextRange


class WrapParagraphAction : EditorAction(WrapParagraphAction.WrapHandler()) {

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ActionPlaces.isPopupPlace(e.place)) {
            e.presentation.isVisible = e.presentation.isEnabled
        }
    }

    private class WrapHandler : EditorActionHandler() {
        override fun execute(editor: Editor, dataContext: DataContext) {
            ApplicationManager.getApplication().runWriteAction(object : Runnable {
                override fun run() {
                    val project = LangDataKeys.PROJECT.getData(dataContext)
                    val document = editor.document
                    val columnWidthOverride = WrapSettingsProvider.getInstance().state?.columnWidthOverride
                    val useMinimumRaggednessAlgorithm = WrapSettingsProvider.getInstance().state?.useMinimumRaggednessAlgorithm ?: false
                    val columnWidth = columnWidthOverride ?: editor.settings.getRightMargin(project)
                    val tabWidth = editor.settings.getTabSize(project)
                    val wrapper = CodeWrapper(width = columnWidth, tabWidth = tabWidth, useMinimumRaggedness = useMinimumRaggednessAlgorithm)
                    val caret = editor.caretModel
                    val startingLine = caret.logicalPosition.line
                    val documentEnd = document.getLineNumber(document.textLength)
                    var selectionStart =  document.getLineStartOffset(startingLine)
                    var selectionEnd = document.getLineEndOffset(startingLine)
                    var upwardLineTracker = startingLine
                    var downwardLineTracker = startingLine

                    if (startingLine > 1) {
                        while (true) {
                            upwardLineTracker--
                            val lineStart = document.getLineStartOffset(upwardLineTracker)
                            val lineEnd = document.getLineEndOffset(upwardLineTracker)
                            val text = document.getText(TextRange(lineStart, lineEnd))
                            val lineData = wrapper.splitOnIndent(text)

                            if (lineData.rest.isBlank() || upwardLineTracker == 1) {
                                break
                            }

                            selectionStart = lineStart
                        }
                    }

                    // Don't keep going down after we reach the last line.
                    if (downwardLineTracker < documentEnd) {
                        while (true) {
                            downwardLineTracker++
                            val lineStart = document.getLineStartOffset(downwardLineTracker)
                            val lineEnd = document.getLineEndOffset(downwardLineTracker)
                            val text = document.getText(TextRange(lineStart, lineEnd))
                            val lineData = wrapper.splitOnIndent(text)

                            if (lineData.rest.isBlank() || downwardLineTracker == documentEnd) {
                                break
                            }

                            selectionEnd = lineEnd
                        }
                    }

                    val text = document.getText(TextRange(selectionStart, selectionEnd))
                    val wrappedText = wrapper.wrap(text)

                    document.replaceString(selectionStart, selectionEnd, wrappedText)
                }
            })
        }
    }

    companion object {
        private val log = Logger.getInstance(WrapParagraphAction::class.java)
    }
}
