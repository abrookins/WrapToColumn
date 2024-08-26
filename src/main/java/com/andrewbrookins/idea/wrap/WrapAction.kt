package com.andrewbrookins.idea.wrap

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import org.jetbrains.annotations.NotNull


fun isWhitespace(str: String?): Boolean {
    if (str == null) {
        return false
    }
    val sz = str.length
    for (i in 0 until sz) {
        if (!Character.isWhitespace(str[i])) {
            return false
        }
    }
    return true
}


class WrapAction : AnAction() {
    @NotNull
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
        e.presentation.isVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = dataContext.let { LangDataKeys.PROJECT.getData(it) }
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
            val possibleWhitespaceEnd = if (start == 0) start else (start - 1).coerceAtLeast(lineStartOffset)
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

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, wrappedText)
        }
    }
}
