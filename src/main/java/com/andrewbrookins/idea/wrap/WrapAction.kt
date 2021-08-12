package com.andrewbrookins.idea.wrap

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.TextRange
import java.lang.Math.max


fun isWhitespace(str: String?): Boolean {
    if (str == null) {
        return false
    }
    val sz = str.length
    for (i in 0 until sz) {
        if (Character.isWhitespace(str[i]) == false) {
            return false
        }
    }
    return true
}


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
                    val project = dataContext?.let { LangDataKeys.PROJECT.getData(it) }
                    val fileIsPlaintext = isPlaintext(dataContext)
                    val document = editor.document
                    val selectionModel = editor.selectionModel
                    val wrapper = getWrapper(project, editor, fileIsPlaintext)
                    val text: String
                    var start: Int
                    val end: Int

                    if (selectionModel.hasSelection()) {
                        start = selectionModel.selectionStart
                        end = selectionModel.selectionEnd

                        // Handle the case where a user selects a line but leaves out
                        // whitespace at the start of the line. We need that whitespace
                        // to correctly wrap new lines in the result.
                        val line = document.getLineNumber(start)
                        val lineStartOffset = document.getLineStartOffset(line)
                        val possibleWhitespaceEnd = if (start == 0) start else max(start - 1, lineStartOffset)
                        val possibleWhitespace = document.getText(TextRange(lineStartOffset, possibleWhitespaceEnd))
                        if (isWhitespace(possibleWhitespace)) {
                            // The selected line has leading whitespace, so expand the selection
                            // to include the whitespace.
                            start = lineStartOffset
                        }
                    } else {
                        val line = editor.caretModel.logicalPosition.line
                        start = document.getLineStartOffset(line)
                        end = document.getLineEndOffset(line)
                    }

                    text = document.getText(TextRange(start, end))
                    if (text.isBlank()) {
                        return
                    }

                    val wrappedText = wrapper.wrap(text)
                    document.replaceString(start, end, wrappedText)
                }
            })
        }
    }
}
