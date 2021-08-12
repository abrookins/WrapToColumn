package com.andrewbrookins.idea.wrap

import org.apache.commons.lang.StringUtils
import java.util.regex.Pattern


/**
 * Code-aware text wrapper.
 *
 * Wrap comments like emacs fill-paragraph command.
 *
 * This code was inspired by Nir Soffer's codewrap library: * https://pypi.python.org/pypi/codewrap/
 */
class CodeWrapper(
    private val commentRegex: Regex = "(/\\*+|\\*/|\\*|\\.|#+|//+|;+|--)?".toRegex(),

    private val newlineRegex: Regex = "(\\r?\\n)".toRegex(),

    private val htmlSeparatorsRegex: Regex = "<[pP]>|<[bB][rR] ?/?>".toRegex(),

    // A string that contains only two new lines demarcates a paragraph.
    private val paragraphSeparatorPattern: Pattern = Pattern.compile(
            "($newlineRegex)\\s*$commentRegex\\s*($htmlSeparatorsRegex)?$newlineRegex"),

    private val tabPlaceholder: String = "☃",

    // A string containing a comment or empty space is considered an indent.
    private val indentRegex: String = "^(\\s|$tabPlaceholder)*$commentRegex\\s*($htmlSeparatorsRegex)?",
    private val indentPattern: Pattern = Pattern.compile(indentRegex),

    // New lines appended to text during wrapping will use this character.
    // NOTE: Intellij always uses \n character for new lines and UI
    // components will fail assertion checks if they receive \r\n. The
    // correct line ending is used when saving the file.
    private val lineSeparator: String = "\n",

    // The column width to wrap text to.
    val width: Int = 80,

    // The number of display columns that a tab character should represent.
    val tabWidth: Int = 4,

    val useMinimumRaggedness: Boolean = false,

    // If the first line in reflowed text contained a symbol character, like *,
    // align subsequent reflowed lines with the space created for that symbol.
    // This is only useful in text (Markdown, AsciiDoc) files that don't have
    // comments, but do have tons of symbol usage.
    val preserveLeadingSymbolSpacing: Boolean = false,

    // Meaningful non-comment symbols, like Markdown lists, etc. Used if
    // preserveLeadingSymbolSpacing is true.
    private val meaningfulSymbolRegex: String = "^(\\s+)?(\\*|-|(\\d+\\.))(\\s+)",
    private val meaningfulSymbolPattern: Pattern = Pattern.compile(meaningfulSymbolRegex)) {

    /**
     * Data about a line that has been split into two pieces: the indent portion
     * of the string, if one exists, and the rest of the string.
     */
    class LineData(indent: String, meaningfulSymbol: String, rest: String) {
        internal var indent = ""
        internal var meaningfulSymbol = ""
        internal var rest = ""

        init {
            this.indent = indent
            this.meaningfulSymbol = meaningfulSymbol
            this.rest = rest
        }
    }

    /**
     * Wrap ``text`` to the chosen width.
     *
     * Preserve the amount of white space between paragraphs after wrapping
     * them. A paragraph is defined as text separated by empty lines. A line is
     * considered empty if contains only start of comment characters and a
     * single ``<p>`` or ``<br>`` HTML tag (this is common in Javadoc).
     *
     * @param text the text to wrap, which may contain multiple paragraphs.
     *
     * @return text wrapped to `width`.
     */
    fun wrap(text: String?): String {
        if (text == null) {
            return ""
        }
        val expandedTabPlaceholder = tabPlaceholder.repeat(tabWidth)
        val textWithTabPlaceholders = text.replace("\t", expandedTabPlaceholder)
        val result = StringBuilder()
        val paragraphMatcher = paragraphSeparatorPattern.matcher(textWithTabPlaceholders)
        val textLength = textWithTabPlaceholders.length
        var location = 0

        while (paragraphMatcher.find()) {
            val paragraph = textWithTabPlaceholders.substring(location, paragraphMatcher.start())
            result.append(wrapParagraph(paragraph))
            result.append(paragraphMatcher.group())
            location = paragraphMatcher.end()
        }

        if (location < textLength) {
            result.append(wrapParagraph(textWithTabPlaceholders.substring(location, textLength)))
        }

        var builtResult = result.toString()

        // Keep trailing text newline.
        if (textWithTabPlaceholders.endsWith(lineSeparator)) {
            builtResult += lineSeparator
        }

        return builtResult.replace(expandedTabPlaceholder, "\t")
    }

    /**
     * Wrap a single paragraph of text.

     * Breaks ``paragraph`` into an array of lines of the chosen width, then
     * joins them back into a single string.

     * @param paragraph the paragraph to wrap
     * *
     * @return text reflowed to chosen width
     */
    private fun wrapParagraph(paragraph: String): String {
        val resultBuilder = StringBuilder()
        val emptyCommentPattern = Pattern.compile("$indentRegex\$", Pattern.MULTILINE)
        val emptyCommentMatcher = emptyCommentPattern.matcher(paragraph)
        val paragraphLength = paragraph.length
        var location = 0

        while (emptyCommentMatcher.find()) {
            val match = emptyCommentMatcher.group()

            // No need to preserve a single empty new-line
            if (match.isEmpty()) {
                continue
            }

            val otherText = paragraph.substring(location, emptyCommentMatcher.start())
            val wrappedLines = breakToLinesOfChosenWidth(otherText)

            if (paragraph.startsWith(match)) {
                resultBuilder.append(match + lineSeparator)
            }

            for (wrappedLine in wrappedLines) {
                resultBuilder.append(wrappedLine + lineSeparator)
            }

            if (paragraph.endsWith(match)) {
                resultBuilder.append(match)
            }

            location = emptyCommentMatcher.end()
        }

        // There were either empty comment lines, or we worked through them all.
        // TODO: Pull some of this code into a method that the while loop also calls.
        if (location < paragraphLength) {
            val otherText = paragraph.substring(location, paragraphLength)
            val wrappedLines = breakToLinesOfChosenWidth(otherText)
            for (wrappedLine in wrappedLines) {
                resultBuilder.append(wrappedLine + lineSeparator)
            }
        }

        var result = resultBuilder.toString()

        // The calling function will append new-lines to the very last line.
        if (result.endsWith(lineSeparator)) {
            result = result.substring(0, result.length - 1)
        }

        return result
    }

    /**
     * Reformat the single paragraph in `text` to lines of the chosen width,
     * and return an array of these lines.
     *
     * Note: C-style multi-line comments are always reflowed to the chosen
     * column width. This means that the first line might stick out because
     * it's indent is longer ("&#47;** " instead of " * " on continuation lines).
     *
     * @param text single paragraph of text
     *
     * @return array of lines
     */
    private fun breakToLinesOfChosenWidth(text:String):MutableList<String> {
        val firstLineIndent = splitOnIndent(text).indent
        val firstLineIsCommentOpener = firstLineIndent.matches("\\s*(/\\*+|\"\"\"|''')\\s*".toRegex())
        var unwrappedText = unwrap(text)
        val lines: Array<String>
        var leadingSymbolWidth = 0
        var leadingSymbol = ""
        var width = width

    if (preserveLeadingSymbolSpacing) {
            val symbolMatcher = meaningfulSymbolPattern.matcher(text)
            if (symbolMatcher.find()) {
                leadingSymbol = symbolMatcher.group()
                leadingSymbolWidth = leadingSymbol.length
                unwrappedText = unwrappedText.substring(leadingSymbolWidth)
            }
            width -= leadingSymbolWidth
        } else {
            width -= firstLineIndent.length
        }

        if (useMinimumRaggedness) {
            lines = wrapMinimumRaggedness(unwrappedText, width).dropLastWhile(String::isEmpty).toTypedArray()
        }
        else {
            lines = wrapGreedy(unwrappedText, width, lineSeparator)
                    .split(lineSeparator.toRegex())
                    .dropLastWhile(String::isEmpty)
                    .toTypedArray()
        }
        val result = mutableListOf<String>()
        val length = lines.size
        var whitespaceBeforeOpener = ""

        if (firstLineIsCommentOpener) {
            val whitespaceMatcher = Pattern.compile("^\\s*").matcher(firstLineIndent)
            if (whitespaceMatcher.find()) {
                whitespaceBeforeOpener = whitespaceMatcher.group()
            }
        }

        for (i in 0 until length) {
            val line = lines[i]
            var lineIndent = firstLineIndent

            if (leadingSymbol.isNotBlank() && preserveLeadingSymbolSpacing) {
                if (i == 0) {
                    lineIndent = leadingSymbol
                } else {
                    lineIndent = StringUtils.repeat(" ", leadingSymbolWidth)
                }
            }

            if (i > 0 && firstLineIsCommentOpener) {
                // This is a hack. We don't know how much whitespace to use!
                lineIndent = "$whitespaceBeforeOpener * "
            }

            result.add(lineIndent + line)
        }

        return result
    }

    /**
     * Convert a hard wrapped paragraph to one line.
     *
     * Indent and comment characters are striped.
     *
     * @param text one paragraph of text, possibly hard-wrapped
     *
     * @return one line of text
     */
    private fun unwrap(text: String): String {
        if (text.isEmpty()) {
            return text
        }

        val lines = text.split("[\\r\\n]+".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        val result = StringBuilder()
        var lastLineWasCarriageReturn = false
        val length = lines.size

        for (i in 0 until length) {
            val line = lines[i]
            val unindentedLine = splitOnIndent(line).rest.trim { it <= ' ' }

            if (line.isEmpty()) {
                // Ignore a line that was just a carriage return/new line.
                lastLineWasCarriageReturn = true
                continue
            }

            // Only add a space if we're joining two sentences that contained words.
            if (lastLineWasCarriageReturn || length == 1 || i == 0) {
                result.append(unindentedLine)
            } else {
                result.append(" ").append(unindentedLine)
            }

            lastLineWasCarriageReturn = false
        }

        return result.toString()
    }

    /**
     * Split text on indent, including comment characters
     *
     * Example (parsed from left margin):
     * // Comment -> ' // ', 'Comment'
     *
     * @param text text to remove indents from
     * *
     * @return indent string, rest
     */
    fun splitOnIndent(text: String): LineData {
        val indentMatcher = indentPattern.matcher(text)
        val symbolMatcher = meaningfulSymbolPattern.matcher(text)
        val lineData = LineData("", "", text)

        // Only break on the first indent-worthy sequence found, to avoid any
        // weirdness with comments-embedded-in-comments.
        if (indentMatcher.find()) {
            lineData.indent = indentMatcher.group()
            lineData.rest = text.substring(indentMatcher.end(), text.length).trim({ it <= ' ' })
            // We might get "/*\n", so strip the newline if so.
            lineData.indent = lineData.indent.replace("[\\r\\n]+".toRegex(), "")
        }

        // If we suspect a line begins with a "meaningful symbol," save that.
        // This is important for file types that use comment-like symbols for
        // things like lists, e.g. Markdown, AsciiDoc.
        if (symbolMatcher.find()) {
            lineData.meaningfulSymbol = symbolMatcher.group()
        }

        return lineData
    }
}
