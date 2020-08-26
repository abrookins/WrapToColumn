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
fun getTextAtLine(document: Document, wrapper: CodeWrapper, lineNum: Int): TextData {
    val lineStart = document.getLineStartOffset(lineNum)
    val lineEnd = document.getLineEndOffset(lineNum)
    val text = document.getText(TextRange(lineStart, lineEnd))
    return TextData(lineStart, lineEnd, wrapper.splitOnIndent(text))
}


class WrapParagraphAction : EditorAction(WrapHandler()) {

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
                    val selectionModel = editor.selectionModel

                    val text: String

                    if (selectionModel.hasSelection()) {
                        text = selectionModel.selectedText ?: return
                        selectionStart = selectionModel.selectionStart
                        selectionEnd = selectionModel.selectionEnd
                    } else {
                        // Don't try to wrap if the user starts on a line that looks blank.
                        if (getTextAtLine(document, wrapper, startingLine).lineData.rest.isBlank()) {
                            return
                        }

                        // Starting from the current line, move upward until we reach an empty line
                        // or the start of the document.
                        for (lineNum in startingLine - 1 downTo 0) {
                            val textData = getTextAtLine(document, wrapper, lineNum)
                            if (textData.lineData.rest.isBlank()) {
                                break
                            }
                            selectionStart = textData.lineStart
                        }

                        // Starting from the current line, move downward until we reach an empty line
                        // or the end of the document.
                        for (lineNum in startingLine + 1 .. documentEnd) {
                            val textData = getTextAtLine(document, wrapper, lineNum)
                            if (textData.lineData.rest.isBlank()) {
                                break
                            }
                            selectionEnd = textData.lineEnd
                        }

                        text = document.getText(TextRange(selectionStart, selectionEnd))
                    }

                    if (text.isBlank()) {
                        return
                    }

                    val wrappedText = wrapper.wrap(text)
                    document.replaceString(selectionStart, selectionEnd, wrappedText)
                }
            })
        }
    }
}
