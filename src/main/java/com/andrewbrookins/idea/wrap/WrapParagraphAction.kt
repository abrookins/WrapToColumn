package com.andrewbrookins.idea.wrap

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.TextRange


data class TextData(val lineStart: Int, val lineEnd: Int, val lineData: CodeWrapper.LineData)
fun getTextAtOffset(document: Document, wrapper: CodeWrapper, offset: Int): TextData {
    val lineStart = document.getLineStartOffset(offset)
    val lineEnd = document.getLineEndOffset(offset)
    val text = document.getText(TextRange(lineStart, lineEnd))
    return TextData(lineStart, lineEnd, wrapper.splitOnIndent(text))
}


class WrapParagraphAction : EditorAction(WrapParagraphAction.WrapHandler()) {

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ActionPlaces.isPopupPlace(e.place)) {
            e.presentation.isVisible = e.presentation.isEnabled
        }
    }

    private class WrapHandler : EditorActionHandler() {
        override fun execute(editor: Editor, dataContext: DataContext?) {
            super.execute(editor, dataContext)
            ApplicationManager.getApplication().runWriteAction(object : Runnable {
                override fun run() {
                    val project = LangDataKeys.PROJECT.getData(dataContext!!)
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

                    // Don't try to wrap if the user starts on a line that looks blank.
                    if (getTextAtOffset(document, wrapper, startingLine).lineData.rest.isBlank()) {
                        return
                    }

                    // Starting from the current line, move upward until we reach an empty line
                    // or the start of the document.
                    while (upwardLineTracker > 0) {
                        upwardLineTracker--
                        val textData = getTextAtOffset(document, wrapper, upwardLineTracker)
                        if (textData.lineData.rest.isBlank()) {
                            break
                        }
                        selectionStart = textData.lineStart
                    }

                    // Starting from the current line, move downward until we reach an empty line
                    // or the end of the document.
                    while (downwardLineTracker < documentEnd) {
                        downwardLineTracker++
                        val textData = getTextAtOffset(document, wrapper, downwardLineTracker)
                        if (textData.lineData.rest.isBlank()) {
                            break
                        }
                        selectionEnd = textData.lineEnd
                    }

                    val text = document.getText(TextRange(selectionStart, selectionEnd))
                    val wrappedText = wrapper.wrap(text)

                    document.replaceString(selectionStart, selectionEnd, wrappedText)
                }
            })
        }
    }
}
