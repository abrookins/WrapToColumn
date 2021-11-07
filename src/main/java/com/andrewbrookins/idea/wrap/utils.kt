package com.andrewbrookins.idea.wrap

import com.andrewbrookins.idea.wrap.config.WrapSettingsProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


fun isPlaintext(dataContext: DataContext?): Boolean {
    val plaintextFileTypes = WrapSettingsProvider.getInstance().state?.plaintextFileTypes?.split(",")

    if (dataContext == null) {
        return false
    } else {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
        return file != null && plaintextFileTypes != null && ".${file.extension}" in plaintextFileTypes
    }
}


fun getWrapper(project: Project?, editor: Editor, fileIsPlaintext: Boolean): CodeWrapper {
    val columnWidthOverride = WrapSettingsProvider.getInstance().state?.columnWidthOverride
    val useMinimumRaggednessAlgorithm = WrapSettingsProvider.getInstance().state?.useMinimumRaggednessAlgorithm ?: false
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



/**
 * Paragraph1.sldkj slkfdj sdlkj flsdkj flsdkj flsdkj flsdkj flskdj flksdj flksdj flskdj flksdj
 * flksdj flsdkjf
 * <p>
 * Paragraph2. lskaj flskdj flsdkj flsdkjf lsdkj flsdkj flksdj flksdj flksdj flkdsj flsdkj flsdkj
 * fldksj fldskjf
 */