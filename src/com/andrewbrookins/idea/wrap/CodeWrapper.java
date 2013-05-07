package com.andrewbrookins.idea.wrap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.WordUtils;


public class CodeWrapper {
    private static class Options {
        String paragraphSeparatorPattern = "\\n\\s*\\n";
        String indentPattern = "^\\s*(#+|//+|;+)?\\s*";
        Integer width = 80;
    }

    private Options options;
    private static final Logger log = Logger.getInstance(CodeWrapper.class);

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
        if (text.endsWith("\n")) {
            builtResult += "\n";
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
                paragraph += '\n';
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
        String lineSeparator = System.getProperty("line.separator");
        text = dewrap(text);
        String[] firstLineData = splitIndent(text);
        String[] lines = WordUtils.wrap(text, options.width).split(lineSeparator);
        ArrayList<String> result = new ArrayList<String>();

        for (String line : lines) {
            // Use indent from the first line on it and all subsequent lines.
            String[] lineData = splitIndent(line);
            result.add(firstLineData[0] + lineData[1]);
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

            String unindentedLine = ' ' + splitIndent(lines[i])[1];
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
    public String[] splitIndent(String text) {
        Pattern pattern = Pattern.compile(options.indentPattern);
        Matcher matcher = pattern.matcher(text);
        String indent = "";
        String unindented = text;

        // Only break on the first indent-worthy sequence found, to avoid any
        // weirdness with comments-embedded-in-comments.
        if (matcher.find()) {
            indent = matcher.group();
            unindented = text.substring(matcher.end(), text.length());
        }

        return new String[]{indent, unindented};
    }
}
