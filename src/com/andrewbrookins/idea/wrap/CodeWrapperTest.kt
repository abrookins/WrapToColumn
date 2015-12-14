package com.andrewbrookins.idea.wrap

import org.junit.Test
import org.junit.Assert.*


class CodeWrapperTests {

    val wrapper: CodeWrapper = CodeWrapper()

    @Test
    fun testCreateWithoutOptions() {
        val original = "// This is my text.\n// This is my text.\n"
        val text = wrapper.wrap(original)
        assertEquals("// This is my text. This is my text.\n", text)

    }

    @Test
    fun testWrapsToColumnWidthComment() {
        val text = wrapper.wrap("// a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n")

        val expected = "// a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n// a a a a a a a a a a a a a a a a a a a a a\n"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapsToColumnWidthCStyleOpeningComment() {
        val text = wrapper.wrap("/** a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n*/")

        val expected = "/** a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n * a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n * a a a a a a\n*/"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapsToColumnWidthNoComment() {
        val text = wrapper.wrap("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\n")

        val expected = "a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a\na a a a a a a a a a a a a a a a a a a a\n"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapOneLongLine() {
        val text = wrapper.wrap("// This is my very long line of text. This is my very long line of text. This is my very long line of text.\n")

        val expected = "// This is my very long line of text. This is my very long line of text. This is\n// my very long line of text.\n"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapRetainsSeparateParagraphs() {
        val text = wrapper.wrap("// This is my very long line of text. This is my very long line of text. This is my very long line of text.\n\n// This is a second paragraph.\n")
        val expected = "// This is my very long line of text. This is my very long line of text. This is\n// my very long line of text.\n\n// This is a second paragraph.\n"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapDoesNotCombineTwoShortLines() {
        val text = wrapper.wrap("// This is my text.\n// This is my text.")
        assertEquals("// This is my text. This is my text.", text)
    }

    @Test
    fun testWrapFillsMultiLineOpener() {
        val text = wrapper.wrap("/** This is my text This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.")
        assertEquals("/** This is my text This is my long multi-line comment opener text. More text\n * please. This is yet another bunch of text in my test comment, so I will get\n * multiple lines in the comment.", text)
    }

    @Test
    fun testWrapFillsMultiLineOpenerBeginningSpace() {
        val text = wrapper.wrap("  /* This is my text This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment. */")
        assertEquals("  /* This is my text This is my long multi-line comment opener text. More text\n   * please. This is yet another bunch of text in my test comment, so I will get\n   * multiple lines in the comment. */", text)
    }

    @Test
    fun testWrapPreservesEmptyCommentLines() {
        val originalText = "/*\n * This is my text. This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.\n *\n * This is another line of text.\n*/"
        val wrappedText = wrapper.wrap(originalText)
        assertEquals("/*\n * This is my text. This is my long multi-line comment opener text. More text\n * please. This is yet another bunch of text in my test comment, so I will get\n * multiple lines in the comment.\n *\n * This is another line of text.\n*/", wrappedText)
    }

    @Test
    fun testWrapMultipleCommentParagraphs() {
        val originalText = "/*\n * This is my text. This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.\n *\n * This is another line of text.\n * \n * And yet another long line of text. Text going on and an endlessly, much longer than it really should.\n*/"
        val wrappedText = wrapper.wrap(originalText)
        assertEquals("/*\n * This is my text. This is my long multi-line comment opener text. More text\n * please. This is yet another bunch of text in my test comment, so I will get\n * multiple lines in the comment.\n *\n * This is another line of text.\n * \n * And yet another long line of text. Text going on and an endlessly, much\n * longer than it really should.\n*/", wrappedText)
    }


    @Test
    fun testWrapRetainsSpaceIndent() {
        val text = wrapper.wrap("    This is my long indented string. It's too long to fit on one line, uh oh! What will happen?")
        val expected = "    This is my long indented string. It's too long to fit on one line, uh oh!\n    What will happen?"
        assertEquals(expected, text)
    }

    @Test
    fun testWrapHandlesLinesWithinMultiLineComment() {
        val text = wrapper.wrap("* This is a long line in a multi-line comment block. Note the star at the beginning.\n* This is another line in a multi-line comment.")
        val expected = "* This is a long line in a multi-line comment block. Note the star at the\n* beginning. This is another line in a multi-line comment."
        assertEquals(expected, text)
    }

    @Test
    fun testWrapRemovesExtraBlankLine() {
        val text = wrapper.wrap("\nMy block of text. My block of text. My block of text. My block of text. My block of text. My block of text.")
        val expected = "My block of text. My block of text. My block of text. My block of text. My block\nof text. My block of text."
        assertEquals(expected, text)
    }

    @Test
    fun testWrapPreservesLeadingIndent() {
        val text = wrapper.wrap(". My long bullet line. My long bullet line. My long bullet line. My long bullet line.")
        val expected = ". My long bullet line. My long bullet line. My long bullet line. My long bullet\n. line."
        assertEquals(expected, text)
    }

    @Test
    fun testIgnoresTrailingSpaces() {
        val text = wrapper.wrap("The quick brown fox \njumps over the lazy \ndog")
        val expected = "The quick brown fox jumps over the lazy dog"
        assertEquals(expected, text)
    }

    @Test
    fun preservesCommentSymbolsWithinText() {
        val text = wrapper.wrap("/**\n * Let's provide a javadoc comment that has a link to some method, e.g. {@link #m()}.\n */")
        val expected = "/**\n * Let's provide a javadoc comment that has a link to some method, e.g. {@link\n * #m()}.\n */"
        assertEquals(expected, text)
    }

    @Test
    fun wrapsNullStrings() {
        val text = wrapper.wrap(null)
        val expected = ""
        assertEquals(expected, text)
    }

    @Test
    fun testAccountsForTabWidth() {
        val tabWrapper = CodeWrapper(width = 40, tabWidth = 5)
        val text = tabWrapper.wrap("\t\t\t\tThis is my very long line of text. This is my very long line of text. This is my\t very long line of text.")
        val expected = "\t\t\t\tThis is my very long\n\t\t\t\tline of text. This\n\t\t\t\tis my very long line\n\t\t\t\tof text. This is\n\t\t\t\tmy\t very long\n\t\t\t\tline of text."
        assertEquals(expected, text)
    }

    // This is wrong: we break on space, but the line-wrapping rules in Chinese are more complicated.
    @Test
    fun testSupportsChinese() {
        val text = wrapper.wrap("它是如何工作的呢？实际上，每个bundle在定义自己的服务配置都是跟目前为止你看到的是一样的。换句话说，一个bundle使用一个或者多个配置资源文件（通常是XML)来指定bundle所需要的参数和服务。然而，我们不直接在配置文件中使用 imports 命令导入它们，而是仅仅在bundle中调用一个服务容器扩展来为我们做同样的工作。一个服务容器扩展是 bundle 的作者创建的一个PHP类，它主要完成两件事情")
        val expected = "它是如何工作的呢？实际上，每个bundle在定义自己的服务配置都是跟目前为止你看到的是一样的。换句话说，一个bundle使用一个或者多个配置资源文件（通常是XML)来指定bundle所需要的参数和服务。然而，我们不直接在配置文件中使用\nimports 命令导入它们，而是仅仅在bundle中调用一个服务容器扩展来为我们做同样的工作。一个服务容器扩展是 bundle\n的作者创建的一个PHP类，它主要完成两件事情"
        assertEquals(expected, text)
    }
}

