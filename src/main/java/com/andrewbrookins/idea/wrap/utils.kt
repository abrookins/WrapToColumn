package com.andrewbrookins.idea.wrap

import com.andrewbrookins.idea.wrap.config.WrapSettingsState
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


fun getFileExtension(dataContext: DataContext?): String? {
    if (dataContext == null) return null
    val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
    return file?.extension
}

fun isPlaintext(dataContext: DataContext?): Boolean {
    val plaintextFileTypes = WrapSettingsState.getInstance().state.plaintextFileTypes?.split(",")

    if (dataContext == null) {
        return false
    } else {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
        return file != null && plaintextFileTypes != null && ".${file.extension}" in plaintextFileTypes
    }
}


fun getWrapper(project: Project?, editor: Editor, fileIsPlaintext: Boolean): CodeWrapper {
    val columnWidthOverride = WrapSettingsState.getInstance().state.columnWidthOverride
    val useMinimumRaggednessAlgorithm = WrapSettingsState.getInstance().state.useMinimumRaggednessAlgorithm ?: false
    val columnWidth = columnWidthOverride ?: editor.settings.getRightMargin(project)
    val tabWidth = editor.settings.getTabSize(project)
    val wrapper: CodeWrapper


    if (fileIsPlaintext) {
        wrapper = CodeWrapper(
            width = columnWidth,
            tabWidth = tabWidth,
            useMinimumRaggedness = useMinimumRaggednessAlgorithm,
            commentRegex = "(//)?".toRegex(),
            preserveLeadingSymbolSpacing = true
        )

    } else {
        wrapper = CodeWrapper(
            width = columnWidth,
            tabWidth = tabWidth,
            useMinimumRaggedness = useMinimumRaggednessAlgorithm
        )
    }

    return wrapper
}

fun shouldWrapLine(textData: TextData, isPlaintext: Boolean, fileExtension: String?): Boolean {
    val isCommentLine = textData.lineData.indent.isNotBlank() && textData.lineData.rest.isNotBlank()
    val plaintextWithoutSymbol = isPlaintext && textData.lineData.meaningfulSymbol.isBlank()
    var isDocstring = false
    if (!fileExtension.isNullOrEmpty() && (fileExtension == "py" || fileExtension == "kt")) {
        val tripleDoubleQuote = "\"\"\""
        val tripleSingleQuote = "'''"
        val pattern = ("^\\s*(?:$tripleDoubleQuote|$tripleSingleQuote)\\s*$").toRegex()
        isDocstring = pattern.matches(textData.lineData.rest.trim())
    }
    return isCommentLine || plaintextWithoutSymbol || !isDocstring
}


/**
 * Paragraph1.sldkj slkfdj sdlkj flsdkj flsdkj flsdkj flsdkj flskdj flksdj flksdj flskdj flksdj
 * flksdj flsdkjf
 * <p>
 * Paragraph2. lskaj flskdj flsdkj flsdkjf lsdkj flsdkj flksdj flksdj flksdj flkdsj flsdkj flsdkj
 * fldksj fldskjf
 */