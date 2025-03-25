package com.andrewbrookins.idea.wrap

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import org.jetbrains.annotations.NotNull
import com.intellij.openapi.actionSystem.ActionUpdateThread

data class TextData(val lineStart: Int, val lineEnd: Int, val lineData: CodeWrapper.LineData)

fun getTextAtOffset(document: Document, wrapper: CodeWrapper, offset: Int): TextData {
    val lineStart = document.getLineStartOffset(offset)
    val lineEnd = document.getLineEndOffset(offset)
    val text = document.getText(TextRange(lineStart, lineEnd))
    return TextData(lineStart, lineEnd, wrapper.splitOnIndent(text))
}

class WrapParagraphAction : AnAction() {
    @NotNull
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = dataContext.let { LangDataKeys.PROJECT.getData(it) }
        val document = editor.document
        val caret = editor.caretModel.currentCaret as? Caret
        val startingLine = caret?.logicalPosition?.line ?: return
        val documentEnd = document.getLineNumber(document.textLength)
        val fileIsPlaintext = isPlaintext(dataContext)
        // val fileExtension = getFileExtension(dataContext)
        val wrapper = getWrapper(project, editor, fileIsPlaintext)
        val selectionModel = editor.selectionModel
        var start: Int
        var end: Int

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
            start = document.getLineStartOffset(startingLine)
            end = document.getLineEndOffset(startingLine)

            // Starting from the current line, expand the selection upwards and downwards.
            for (direction in arrayOf(-1, 1)) {
                var lineTracker = startingLine
                while (lineTracker in 1 until documentEnd) {
                    lineTracker += direction
                    val textData = getTextAtOffset(document, wrapper, lineTracker)
                    // Pass in file extension?
                    if (!shouldWrapLine(textData, fileIsPlaintext) || textData.lineData.rest.isBlank()) {
                        break
                    }
                    if (direction == -1) start = textData.lineStart
                    else end = textData.lineEnd
                }
            }
        }

        val text = document.getText(TextRange(start, end))
        if (text.isBlank()) {
            return
        }
        val wrappedText = wrapper.wrap(text)

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, wrappedText)
        }
    }
}
