package com.andrewbrookins.idea.wrap

import java.util.ArrayList
import java.util.regex.Pattern


/**
 * Code-aware text wrapper.

 * Wrap comments like emacs wrapParagraph-paragraph command - long comments turn
 * into multiple comments, not one comment followed by code.

 * This code was inspired by Nir Soffer's codewrap library:
 * https://pypi.python.org/pypi/codewrap/
 */
class CodeWrapper(
        val commentRegex: String = "(/\\*+|\\*/|\\*|\\.|#+|//+|;+)?",

        val newlineRegex: String = "(\\n|\\r\\n)",

        // A string that contains only two new lines demarcates a paragraph.
        val paragraphSeparatorPattern: Pattern = Pattern.compile("$newlineRegex\\s*$commentRegex\\s*$newlineRegex"),

        val tabPlaceholder: String = "☃",

        // A string containing a comment or empty space is considered an indent.
        val indentRegex: String = "^(\\s|$tabPlaceholder)*$commentRegex\\s*",
        val indentPattern: Pattern = Pattern.compile(indentRegex),

        // New lines appended to text during wrapping will use this character.
        // NOTE: Intellij always uses \n character for new lines and UI
        // components will fail assertion checks if they receive \r\n. The
        // correct line ending is used when saving the file.
        val lineSeparator: String = "\n",

        // The column width to wrap text to.
        val width: Int = 80,

        // The number of display columns that a tab character should represent.
        val tabWidth: Int = 4,

        val useMinimumRaggedness: Boolean = false) {

    /**
     * Data about a line that has been split into two pieces: the indent portion
     * of the string, if one exists, and the rest of the string.
     */
    private class LineData(indent: String, rest: String) {
        internal var indent = ""
        internal var rest = ""

        init {
            this.indent = indent
            this.rest = rest
        }
    }

    /**
     * Helper to wrap a string using a greedy algorithm.
     *
     * Adapted from Apache Commons Lang.
     */
    fun wrapGreedy(str: String, wrapLength: Int): String {
        val newLineStr = lineSeparator
        val inputLineLength = str.length
        val space = ' '
        val wrappedLine = StringBuffer(inputLineLength + 32)
        var offset = 0

        while (inputLineLength - offset > wrapLength) {
            if (str[offset] == space) {
                offset += 1
                continue
            }
            if (inputLineLength - offset <= wrapLength) {
                break
            }
            var spaceToWrapAt = str.lastIndexOf(space, wrapLength + offset)

            if (spaceToWrapAt >= offset) {
                wrappedLine.append(str.substring(offset, spaceToWrapAt))
                wrappedLine.append(newLineStr)
                offset = spaceToWrapAt + 1
            } else {
                spaceToWrapAt = str.indexOf(space, wrapLength + offset)
                if (spaceToWrapAt >= 0) {
                    wrappedLine.append(str.substring(offset, spaceToWrapAt))
                    wrappedLine.append(newLineStr)
                    offset = spaceToWrapAt + 1
                } else {
                    wrappedLine.append(str.substring(offset))
                    offset = inputLineLength
                }
            }
        }

        wrappedLine.append(str.substring(offset))
        return wrappedLine.toString()
    }

    /**
     * Helper to wrap a string using a minimum raggedness algorithm.
     *
     * Based on Aggarwal and Tokuyama's work (1998) via http://xxyxyz.org/line-breaking/
     */
    fun wrapMinimumRaggedness(text: String, width: Int): Array<String> {
        val words = text.split(' ')
        val count = words.size
        var offsets = arrayListOf(0.0)
        for (w in words) {
            offsets.add(offsets[offsets.size - 1] + w.length)
        }

        var minima = arrayOf(0.0) + Array(count, { Math.pow(10.0, 20.0) } )
        var breaks = Array(count + 1, { 0 })

        fun cost(i: Int, j: Int): Double {
            val w = offsets[j] - offsets[i] + j - i - 1
            if (w > width) {
                return Math.pow(10.0, 10.0) * (w - width)
            }
            return minima[i] + Math.pow((width - w), 2.0)
        }

        fun smawk(rows:ArrayList<Int>, columns:Array<Int>) {
            var stack = arrayListOf<Int>()
            var i = 0
            var j: Int
            var length = rows.size
            while (i < length) {
                if (stack.size > 0) {
                    val c = columns[stack.size - 1]
                    if (cost(stack[stack.size - 1], c) < cost(rows[i], c)) {
                        if (stack.size < columns.size) {
                            stack.add(rows[i])
                        }
                        i += 1
                    } else {
                        stack.removeAt(stack.size - 1)
                    }
                } else {
                    stack.add(rows[i])
                    i += 1
                }
            }

            if (columns.size > 1) {
                // Equivalent to a Python slice with step: smawk(stack, columns[1::2])
                smawk(stack, columns.copyOfRange(1, columns.size).filter({ columns.indexOf(it) % 2 != 0 }).toTypedArray())
            }

            i = 0
            j = 0
            var end: Int
            while (j < columns.size) {
                if (j + 1 < columns.size) {
                    end = breaks[columns[j + 1]]
                }
                else {
                    end = stack[stack.size -1]
                }
                val c = cost(stack[i], columns[j])
                if (c < minima[columns[j]]) {
                    minima[columns[j]] = c
                    breaks[columns[j]] = stack[i]
                }
                if (stack[i] < end) {
                    i += 1
                }
                else {
                    j += 2
                }
            }
        }

        var n = count + 1
        var i = 0
        var offset = 0

        while (true) {
            val r = Math.min(n.toDouble(), Math.pow(2.0, (i + 1.0))).toInt()
            val edge = Math.pow(2.0, i.toDouble()) + offset
            // Python ranges drop the last item, but Kotlin's preserve it -- so we subtract one.
            smawk((0 + offset).rangeTo(edge.toInt()-1).toArrayList(), edge.toInt().rangeTo((r + offset).toInt()-1).toArrayList().toTypedArray())
            val x = minima[(r - 1 + offset).toInt()]
            var costGreaterThanOrEqualToMinima = false

            // Python ranges drop the last item, but Kotlin's preserve it -- so we subtract one.
            for (j in Math.pow(2.0, i.toDouble()).toInt().rangeTo(r - 1 - 1)) {
                val y = cost(j + offset, r - 1 + offset)
                if (y <= x) {
                    n -= j
                    i = 0
                    offset += j
                    costGreaterThanOrEqualToMinima = true
                    break
                }
            }
            if (!costGreaterThanOrEqualToMinima) {
                if (r == n) {
                    break
                }
                i += 1
            }
        }

        var lines: MutableList<String> = arrayListOf()
        var j = count
        while (j > 0) {
            i = breaks[j]
            // Python ranges drop the last item, but Kotlin's preserve it -- so we subtract one.
            lines.add(words.slice(i..j - 1).joinToString(" "))
            j = i
        }
        return lines.reversed().toTypedArray()
    }

    /**
     * Wrap ``text`` to the chosen width.

     * Preserve the amount of white space between paragraphs after wrapping
     * them. A paragraph is defined as text separated by empty lines.

     * @param text the text to wrap, which may contain multiple paragraphs.
     * *
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
        var location: Int = 0

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

        val resultWithTabs = builtResult.replace(expandedTabPlaceholder, "\t")

        return resultWithTabs
    }

    /**
     * Wrap a single paragraph of text.

     * Breaks ``paragraph`` into an array of lines of the chosen width, then
     * joins them back into a single string.

     * @param paragraph the paragraph to wrap
     * *
     * @return text reflowed to chosen width
     */
    fun wrapParagraph(paragraph: String): String {
        val resultBuilder = StringBuilder()
        val emptyCommentPattern = Pattern.compile("$indentRegex\$", Pattern.MULTILINE)
        val emptyCommentMatcher = emptyCommentPattern.matcher(paragraph)
        val paragraphLength = paragraph.length
        var location: Int = 0

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
     * Note: C-style multi-line comments are always reflowed to the chosen column width minus
     * the length of the opening line comment/indent (which is, e.g. 4 for "/** Opening line... */").
     * This means that continuation lines (e.g. " * I'm a continuation line in") may be slightly
     * shorter than expected.
     *
     * @param text single paragraph of text
     *
     * @return array of lines
    */
    fun breakToLinesOfChosenWidth(text:String):ArrayList<String> {
        val firstLineData = splitOnIndent(text)
        val multiLineContinuation = " * "
        val firstLineIsCommentOpener = firstLineData.indent.matches("\\s*(/\\*+).*".toRegex())
        val width = width - firstLineData.indent.length
        val unwrappedText = unwrap(text)
        val lines: Array<String>
        if (useMinimumRaggedness) {
            lines = wrapMinimumRaggedness(unwrappedText, width).dropLastWhile({ it.isEmpty() }).toTypedArray()
        }
        else {
            lines = wrapGreedy(unwrappedText, width)
                    .split(lineSeparator.toRegex())
                    .dropLastWhile({ it.isEmpty() })
                    .toTypedArray()
        }
        val result = ArrayList<String>()
        val length = lines.size
        var whitespaceBeforeOpener = ""

        if (firstLineIsCommentOpener) {
            val whitespaceMatcher = Pattern.compile("^\\s*").matcher(firstLineData.indent)
            if (whitespaceMatcher.find()) {
                whitespaceBeforeOpener = whitespaceMatcher.group()
            }
        }

        for (i in 0..length - 1) {
            val line = lines[i]
            var lineIndent = firstLineData.indent

            if (i > 0) {
                // This is a hack. We don't know how much whitespace to use!
                lineIndent = if (firstLineIsCommentOpener) whitespaceBeforeOpener + multiLineContinuation else lineIndent
            }

            result.add(lineIndent + line)
        }

        return result
    }

    /**
     * Convert a hard wrapped paragraph to one line.

     * Indent and comment characters are striped.

     * @param text one paragraph of text, possibly hard-wrapped
     * *
     * @return one line of text
     */
    fun unwrap(text: String): String {
        if (text.isEmpty()) {
            return text
        }

        val lines = text.split("[\\r\\n]+".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val result = StringBuilder()
        var lastLineWasCarriageReturn = false
        val length = lines.size

        for (i in 0..length - 1) {
            val line = lines[i]
            val unindentedLine = splitOnIndent(line).rest.trim({ it <= ' ' })

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

     * Example (parsed from left margin):
     * // Comment -> ' // ', 'Comment'

     * @param text text to remove indents from
     * *
     * @return indent string, rest
     */
    private fun splitOnIndent(text: String): LineData {
        val matcher = indentPattern.matcher(text)
        val lineData = LineData("", text)

        // Only break on the first indent-worthy sequence found, to avoid any
        // weirdness with comments-embedded-in-comments.
        if (matcher.find()) {
            lineData.indent = matcher.group()
            lineData.rest = text.substring(matcher.end(), text.length).trim({ it <= ' ' })
            // We might get "/*\n", so strip the newline if so.
            lineData.indent = lineData.indent.replace("[\\r\\n]+".toRegex(), "")
        }

        return lineData
    }
}
