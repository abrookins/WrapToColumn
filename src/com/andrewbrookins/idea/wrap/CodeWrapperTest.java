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
        String text = wrapper.fillParagraphs(original);
        assertEquals("// This is my text. This is my text.\n", text);
    }

    @Test
    public void testFillParagraphsOneLongLine() throws Exception {
        String text = wrapper.fillParagraphs("// This is my very long line of text. " +
            "This is my very long line of text. This is my very long line of text.\n");
        assertEquals("// This is my very long line of text. This is my very long line of text. This\n" +
            "// is my very long line of text.\n", text);
    }

    @Test
    public void testFillParagraphsRetainsSeparateParagraphs() throws Exception {
        String text = wrapper.fillParagraphs("// This is my very long line of text. " +
            "This is my very long line of text. This is my very long line of text.\n\n" +
            "// This is a second paragraph.\n");
        assertEquals("// This is my very long line of text. This is my very long line of text. This\n" +
            "// is my very long line of text.\n\n// This is a second paragraph.\n", text);
    }

    @Test
    public void testFillParagraphsDoesNotCombineTwoShortLines() throws Exception {
        String text = wrapper.fillParagraphs("// This is my text.\n// This is my text.");
        assertEquals("// This is my text. This is my text.", text);
    }

    @Test
    public void testFillParagraphsFillsMultiLineOpener() throws Exception {
        // This could be more graceful, I suppose.
        String text = wrapper.fillParagraphs("/** This is my text This is my long multi-" +
            "line comment opener text. More text please.");
        assertEquals("/** This is my text This is my long multi-" +
            "line comment opener text. More text\nplease.", text);
    }

    @Test
    public void testFillParagraphsRetainsSpaceIndent() throws Exception {
        String text = wrapper.fillParagraphs("    This is my long indented " +
            "string. It's too long to fit on one line, uh oh! What will happen?");
        assertEquals("    This is my long indented string. It's too long to fit " +
            "on one line, uh oh!\n    What will happen?", text);
    }

    @Test
    public void testFill() throws Exception {

    }

    @Test
    public void testWrap() throws Exception {

    }

    @Test
    public void testDewrap() throws Exception {

    }

    @Test
    public void testSplitIndent() throws Exception {

    }
}
