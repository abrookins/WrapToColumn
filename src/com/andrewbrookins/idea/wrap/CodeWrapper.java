package com.andrewbrookins.idea.wrap;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;

/**
 * Code-aware text wrapper.
 *
 * Wrap comments like emacs wrapParagraph-paragraph command - long comments turn
 * into multiple comments, not one comment followed by code.
 *
 * This code was inspired by Nir Soffer's codewrap library:
 *   https://pypi.python.org/pypi/codewrap/
 */
public class CodeWrapper {
    public static class Options {
        String commentRegex = "(/\\*+|\\*/|\\*|\\.|#+|//+|;+)?";

        String newlineRegex = "(\\n|\\r\\n)";

        // A string that contains only two new lines demarcates a paragraph.
        Pattern paragraphSeparatorPattern = Pattern.compile(newlineRegex + "\\s*" + commentRegex + "\\s*" + newlineRegex);

        // A string containing a comment or empty space is considered an indent.
        Pattern indentPattern = Pattern.compile("^\\s*" + commentRegex + "\\s*");

        // New lines appended to text during wrapping will use this character.
        // NOTE: Intellij always uses \n character for new lines and UI
        // components will fail assertion checks if they receive \r\n. The
        // correct line ending is used when saving the file.
        String lineSeparator = "\n";

        // The column width to wrap text to.
        Integer width = 80;
    }

    /**
     * Data about a line that has been split into two pieces: the indent portion
     * of the string, if one exists, and the rest of the string.
     */
    private static class LineData {
        String indent = "";
        String rest = "";

        public LineData(String indent, String rest) {
            this.indent = indent;
            this.rest = rest;
        }
    }

    private Options options;

    public CodeWrapper(Options options) {
        this.options = options;
    }

    public CodeWrapper() {
        options = new Options();
    }

    public CodeWrapper(Integer columnWidth) {
        options = new Options();
        options.width = columnWidth;
    }

    /**
     * Wrap ``text`` to the chosen width.
     *
     * Preserve the amount of white space between paragraphs after wrapping
     * them. A paragraph is defined as text separated by empty lines.
     *
     * @param text the text to wrap, which may contain multiple paragraphs.
     * @return text wrapped to `width`.
     */
    public String wrap(String text) {
        StringBuilder result = new StringBuilder();
        Matcher paragraphMatcher = options.paragraphSeparatorPattern.matcher(text);
        Integer textLength = text.length();
        Integer location = 0;

        while (paragraphMatcher.find()) {
            String paragraph = text.substring(location, paragraphMatcher.start());
            result.append(wrapParagraph(paragraph));
            result.append(paragraphMatcher.group());
            location = paragraphMatcher.end();
        }

        if (location < textLength) {
            result.append(wrapParagraph(text.substring(location, textLength)));
        }

        String builtResult = result.toString();

        // Keep trailing text newline.
        if (text.endsWith(options.lineSeparator)) {
            builtResult += options.lineSeparator;
        }

        return builtResult;
    }

    /**
     * Wrap a single paragraph of text.
     *
     * Breaks ``paragraph`` into an array of lines of the chosen width, then
     * joins them back into a single string.
     *
     * @param paragraph the paragraph to wrap
     * @return text reflowed to chosen width
     */
    public String wrapParagraph(String paragraph) {
        StringBuilder resultBuilder = new StringBuilder();
        Pattern emptyCommentPattern = Pattern.compile(options.indentPattern + "$", Pattern.MULTILINE);
        Matcher emptyCommentMatcher = emptyCommentPattern.matcher(paragraph);
        Integer paragraphLength = paragraph.length();
        Integer location = 0;

        while (emptyCommentMatcher.find()) {
            String match = emptyCommentMatcher.group();

            // No need to preserve a single empty new-line
            if (match.isEmpty()) {
                continue;
            }

            String otherText = paragraph.substring(location, emptyCommentMatcher.start());
            ArrayList<String> wrappedLines = breakToLinesOfChosenWidth(otherText);

            if (paragraph.startsWith(match)) {
                resultBuilder.append(match);
            }

            for (String wrappedLine : wrappedLines) {
                wrappedLine += options.lineSeparator;
                resultBuilder.append(wrappedLine);
            }

            if (paragraph.endsWith(match)) {
                resultBuilder.append(match);
            }

            location = emptyCommentMatcher.end();
        }

        // There were either empty comment lines, or we worked through them all.
        // TODO: Pull some of this code into a method that the while loop also calls.
        if (location < paragraphLength) {
            String otherText = paragraph.substring(location, paragraphLength);
            ArrayList<String> wrappedLines = breakToLinesOfChosenWidth(otherText);
            for (String wrappedLine : wrappedLines) {
                wrappedLine += options.lineSeparator;
                resultBuilder.append(wrappedLine);
            }
        }

        String result = resultBuilder.toString();

        // The calling function will append new-lines to the very last line.
        if (result.endsWith(options.lineSeparator)) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

     /**
     * Reformat the single paragraph in `text` to lines of the chosen width,
     * and return an array of these lines.
     *
     * @param text single paragraph of text
     * @return array of lines
     */
    public ArrayList<String> breakToLinesOfChosenWidth(String text) {
        LineData firstLineData = splitOnIndent(text);
        Integer width = options.width - firstLineData.indent.length();
        String unwrappedText = unwrap(text);
        String[] lines = WordUtils.wrap(unwrappedText, width, options.lineSeparator, true)
            .split(options.lineSeparator);
        ArrayList<String> result = new ArrayList<String>();
        int length = lines.length;
        Boolean firstLineIsCommentOpener = firstLineData.indent.startsWith("/*");

        for (int i = 0; i < length; i++) {
            String line = lines[i];
            String lineIndent = firstLineData.indent;
            LineData lineData = splitOnIndent(line);
            line = lineData.rest;

            if (i > 0) {
                // This is a hack. We don't know how much whitespace to use!
                lineIndent = firstLineIsCommentOpener ? " * " : lineIndent;
            }

            result.add(lineIndent + line);
        }

        return result;
    }

    /**
     * Convert hard wrapped paragraph to one line.
     *
     * The indentation and comments of the first line are preserved;
     * subsequent lines indent and comment characters are striped.
     *
     * @param text one paragraph of text, possibly hard-wrapped
     * @return one line of text
     */
    public String unwrap(String text) {
        if (text.isEmpty()) {
            return text;
        }

        String[] lines = text.split("[\\r\\n]+");
        int length = lines.length;
        StringBuilder result = new StringBuilder();
        boolean firstLineWasCarriageReturn = false;
        int start = 0;

        if (length > 0) {
            // Ignore a line that is just a carriage return.
            if (lines[0].isEmpty()) {
                firstLineWasCarriageReturn = true;
            }
            // Add first line as is, keeping indent.
            else {
                result.append(lines[0].trim());
                start = 1;
            }
        }

        for (int i = start; i < length; i++) {
            String unindentedLine = splitOnIndent(lines[i]).rest;
            // If we ignored the first line, then we don't need to add a space to the item.
            if (firstLineWasCarriageReturn) {
                result.append(unindentedLine);
            }
            else {
                result.append(" ").append(unindentedLine);
            }
        }

        return result.toString();
    }

    /**
     * Split text on indent, including comment characters
     *
     * Example (parsed from left margin):
     *      // Comment -> ' // ', 'Comment'
     *
     * @param text text to remove indents from
     * @return indent string, rest
     */
    public LineData splitOnIndent(String text) {
        Matcher matcher = options.indentPattern.matcher(text);
        LineData lineData = new LineData("", text);

        // Only break on the first indent-worthy sequence found, to avoid any
        // weirdness with comments-embedded-in-comments.
        if (matcher.find()) {
            lineData.indent = matcher.group();
            lineData.rest = text.substring(matcher.end(), text.length()).trim();
            // We might get "/*\n", so strip the newline if so.
            lineData.indent = lineData.indent.replaceAll("[\\r\\n]+", "");
        }

        return lineData;
    }
}
