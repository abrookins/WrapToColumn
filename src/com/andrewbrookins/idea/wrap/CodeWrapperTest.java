package com.andrewbrookins.idea.wrap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class CodeWrapperTest {
    CodeWrapper wrapper;

    @Before
    public void initialize() {
        wrapper = new CodeWrapper();
    }

    @Test
    public void testCreateWithoutOptions() throws Exception {
        String original = "// This is my text.\n// This is my text.\n";
        String text = wrapper.wrap(original);
        assertEquals("// This is my text. This is my text.\n", text);

    }

    @Test
    public void testWrapOneLongLine() throws Exception {
        String text = wrapper.wrap("// This is my very long line of text. " +
            "This is my very long line of text. This is my very long line of text.\n");
        String expected = "// This is my very long line of text. This is my very long line of text. This\n" +
            "// is my very long line of text.\n";
        assertEquals(expected, text);
    }

    @Test
    public void testWrapRetainsSeparateParagraphs() throws Exception {
        String text = wrapper.wrap("// This is my very long line of text. " +
            "This is my very long line of text. This is my very long line of text.\n\n" +
            "// This is a second paragraph.\n");
        String expected = "// This is my very long line of text. This is my very long line of text. This\n" +
            "// is my very long line of text.\n\n// This is a second paragraph.\n";
        assertEquals(expected, text);
    }

    @Test
    public void testWrapDoesNotCombineTwoShortLines() throws Exception {
        String text = wrapper.wrap("// This is my text.\n// This is my text.");
        assertEquals("// This is my text. This is my text.", text);
    }

    @Test
    public void testWrapFillsMultiLineOpener() throws Exception {
        String text = wrapper.wrap("/** This is my text This is my long multi-" +
            "line comment opener text. More text please. This is yet another bunch " +
            "of text in my test comment, so I will get multiple lines in the comment.");
        assertEquals("/** This is my text This is my long multi-line comment opener text. More\n" +
            " * text please. This is yet another bunch of text in my test comment, so I will\n" +
            " * get multiple lines in the comment.", text);
    }

    @Test
    public void testWrapFillsMultiLineOpenerBeginningSpace() throws Exception {
        String text = wrapper.wrap("  /* This is my text This is my long multi-" +
            "line comment opener text. More text please. This is yet another bunch " +
            "of text in my test comment, so I will get multiple lines in the comment. */");
        assertEquals("  /* This is my text This is my long multi-line comment opener text. More\n" +
            "   * text please. This is yet another bunch of text in my test comment, so I\n" +
            "   * will get multiple lines in the comment. */", text);
    }

    @Test
    public void testWrapPreservesEmptyCommentLines() throws Exception {
        String originalText = "/*\n" +
                " * This is my text. This is my long multi-line comment opener text. More " +
                "text please. This is yet another bunch of text in my test comment, so I " +
                "will get multiple lines in the comment.\n" +
                " *\n" +
                " * This is another line of text.\n" +
                "*/";
        String wrappedText = wrapper.wrap(originalText);
        assertEquals("/*\n" +
                " * This is my text. This is my long multi-line comment opener text. More text\n" +
                " * please. This is yet another bunch of text in my test comment, so I will get\n" +
                " * multiple lines in the comment.\n" +
                " *\n" +
                " * This is another line of text.\n" +
                "*/", wrappedText);
    }

    @Test
    public void testWrapMultipleCommentParagraphs() throws Exception {
        String originalText = "/*\n" +
                " * This is my text. This is my long multi-line comment opener text. More " +
                "text please. This is yet another bunch of text in my test comment, so I " +
                "will get multiple lines in the comment.\n" +
                " *\n" +
                " * This is another line of text.\n" +
                " * \n" +
                " * And yet another long line of text. Text going on and an endlessly, much longer than it really should.\n" +
                "*/";
        String wrappedText = wrapper.wrap(originalText);
        assertEquals("/*\n" +
                " * This is my text. This is my long multi-line comment opener text. More text\n" +
                " * please. This is yet another bunch of text in my test comment, so I will get\n" +
                " * multiple lines in the comment.\n" +
                " *\n" +
                " * This is another line of text.\n" +
                " * \n" +
                " * And yet another long line of text. Text going on and an endlessly, much\n" +
                " * longer than it really should.\n" +
                "*/", wrappedText);
    }


    @Test
    public void testWrapRetainsSpaceIndent() throws Exception {
        String text = wrapper.wrap("    This is my long indented " +
            "string. It's too long to fit on one line, uh oh! What will happen?");
        String expected = "    This is my long indented string. It's too long to fit " +
            "on one line, uh oh!\n    What will happen?";
        assertEquals(expected, text);
    }

    @Test
    public void testWrapHandlesLinesWithinMultiLineComment() throws Exception {
        String text = wrapper.wrap("* This is a long line in a multi-" +
            "line comment block. Note the star at the beginning.\n* This is " +
            "another line in a multi-line comment.");
        String expected = "* This is a long line in a multi-line comment block. Note the star at the\n" +
            "* beginning. This is another line in a multi-line comment.";
        assertEquals(expected, text);
    }

    @Test
    public void testWrapRemovesExtraBlankLine() throws Exception {
        String text = wrapper.wrap("\nMy block of text. My block of text. My block of text. " +
            "My block of text. My block of text. My block of text.");
        String expected = "My block of text. My block of text. My block of text. My block of text. My block\n" +
                "of text. My block of text.";
        assertEquals(expected, text);
    }

    @Test
    public void testWrapPreservesLeadingIndent() throws Exception {
        String text = wrapper.wrap(". My long bullet line. My long bullet line. My long bullet line. My long bullet line.");
        String expected = ". My long bullet line. My long bullet line. My long bullet line. My long\n. bullet line.";
        assertEquals(expected, text);
    }

    @Test
    public void testIgnoresTrailingSpaces() throws Exception {
        String text = wrapper.wrap("The quick brown fox \n" +
                "jumps over the lazy \n" +
                "dog");
        String expected = "The quick brown fox jumps over the lazy dog";
        assertEquals(expected, text);
    }

    // TODO: The real problem with Chinese is font character width.
    @Test
    public void testSupportsChinese() throws Exception {
        String text = wrapper.wrap("這是中國文字，這應該是太長，無法在一行中，並會按需要得到包裹的長行。這是中國文字，這應該是太長，無法在一行中，並會按需要得到包裹的長行。" +
                "這是中國文字，這應該是太長，無法在一行中，並會按需要得到包裹的長行。");
        String expected = "這是中國文字，這應該是太長，無法在一行中，並會按需要得到包裹的長行。這是中國文字，這應該是太長，無法在一行中，並會按需要得到包裹的長行。這是中國文字，這應該是太\n" +
                "長，無法在一行中，並會按需要得到包裹的長行。";
        assertEquals(expected, text);
    }
}
