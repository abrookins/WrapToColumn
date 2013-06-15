package com.andrewbrookins.idea.wrap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;


/*
 * Code-aware text wrapper.
 *
 * Wrap comments like emacs fill-paragraph command - long comments turn
 * into multiple comments, not one comment followed by code.
 *
 * This code was ported from Nir Soffer's codewrap library:
 *   https://pypi.python.org/pypi/codewrap/
 */
public class CodeWrapper {
    public static class Options {
        // A string with a newline above and below it is a paragraph.
        String paragraphSeparatorPattern = "(\\n|\\r\\n)\\s*(\\n|\\r\\n)";

        // A string containing a comment or empty space is considered an indent.
        String indentPattern = "^\\s*(\\*|/\\*+|#+|//+|;+)?\\s*";

        // New lines appended to text during wrapping will use this character.
        String lineSeparator = System.getProperty("line.separator");

        // The column width to wrap text to.
        Integer width = 80;
    }

    /*
     Data about a line that has been split into two pieces: the indent portion
     of the string, if one exists, and the rest of the string.
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
     * Fill multiple paragraphs
     *
     * Assume that paragraphs are separated by empty lines. Preserve
     * the amount of white space between paragraphs.
     *
     * @param text the text to fill, which may contain multiple paragraphs.
     * @return text filled to set column width.
     */
    public String fillParagraphs(String text) {
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile(options.paragraphSeparatorPattern);
        Matcher matcher = pattern.matcher(text);
        Integer textLength = text.length();

        Integer location = 0;
        while (matcher.find()) {
            String paragraph = text.substring(location, matcher.start());
            result.append(fill(paragraph));
            result.append(matcher.group());
            location = matcher.end();
        }

        if (location < textLength) {
            result.append(fill(text.substring(location, textLength)));
        }

        String builtResult = result.toString();

        // Keep trailing text newline.
        if (text.endsWith(options.lineSeparator)) {
            builtResult += options.lineSeparator;
        }

        return builtResult;
    }

    /**
     * Fill paragraph by joining wrapped lines
     *
     * @param text the text to fill
     * @return text filled with current width
     */
    public String fill(String text) {
        StringBuilder result = new StringBuilder();
        ArrayList<String> wrappedParagraphs = wrap(text);
        int size = wrappedParagraphs.size();

        for (int i = 0; i < size; i++) {
            String paragraph = wrappedParagraphs.get(i);

            // If this is a multi-paragraph list and we aren't at the end,
            // add a new line.
            if (size > 0 && i < size - 1) {
                paragraph += options.lineSeparator;
            }

            result.append(paragraph);
        }

        return result.toString();
    }

     /**
     * Wrap code, and comments in a smart way
     *
     * Reformat the single paragraph in 'text' so it fits in lines of
     * no more than 'width' columns, and return a list of wrapped
     * lines.
     *
     * @param text single paragraph of text
     * @return lines filled with current width
     */
    public ArrayList<String> wrap(String text) {
        text = dewrap(text);
        LineData firstLineData = splitIndent(text);
        Integer width = options.width - firstLineData.indent.length();
        String[] lines = WordUtils.wrap(text, width, options.lineSeparator, false)
            .split(options.lineSeparator);
        ArrayList<String> result = new ArrayList<String>();

        // If the first line is a multi-line comment opener, subsequent lines
        // should use a star (*) as the indent. The final indent should use a
        // star and forward-slash (*/).
        boolean isMultiLineOpener = firstLineData.indent.contains("/*");
        String indent =  isMultiLineOpener ? "* " : firstLineData.indent;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String lineIndent = indent;

            if (i == 0) {
                LineData lineData = splitIndent(line);
                lineIndent = firstLineData.indent;
                line = lineData.rest;
            }

            // On the final line of a multi-line comment opened with /** or
            // /*, add a newline and */ to close the comment.
            if (i == lines.length - 1 && isMultiLineOpener) {
                line = line + options.lineSeparator + "*/";
            }

            // Use indent from the first line on it and all subsequent lines.
            result.add(lineIndent + line);
        }

        return result;
    }

    /**
     * Convert hard wrapped paragraph to one line.
     *
     * The indentation and comments of the first line are preserved,
     * subsequent lines indent and comments characters are striped.
     *
     * @param text one paragraph of text, possibly hard-wrapped
     * @return one line of text
     */
    public String dewrap(String text) {
        if (text.isEmpty()) {
            return text;
        }

        String[] lines = text.split("[\\r\\n]+");
        StringBuilder result = new StringBuilder();

        // Add first line as is, keeping indent
        result.append(lines[0]);

        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                continue;
            }

            String unindentedLine = ' ' + splitIndent(lines[i]).rest;
            // Add rest of lines removing indent
            result.append(unindentedLine);
        }

        return result.toString();
    }

    /**
     * Split text on indent, including comments characters
     *
     * Example (parsed from left margin):
     *      // Comment -> ' // ', 'Comment'
     *
     * @param text text to remove indents from
     * @return indent string, rest
     */
    public LineData splitIndent(String text) {
        Pattern pattern = Pattern.compile(options.indentPattern);
        Matcher matcher = pattern.matcher(text);
        LineData lineData = new LineData("", text);

        // Only break on the first indent-worthy sequence found, to avoid any
        // weirdness with comments-embedded-in-comments.
        if (matcher.find()) {
            lineData.indent = matcher.group();
            lineData.rest = text.substring(matcher.end(), text.length());
        }

        return lineData;
    }
}
