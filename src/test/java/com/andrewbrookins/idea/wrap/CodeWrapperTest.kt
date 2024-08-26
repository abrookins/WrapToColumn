package com.andrewbrookins.idea.wrap.tests

import org.junit.Test
import org.junit.Assert.*
import com.andrewbrookins.idea.wrap.*

data class WrapTestCase(
    val description: String,
    private val rawInput: String?,
    private val rawExpectedOutput: String,
    val trimIndent: Boolean = true,
    val width: Int = 80,
    val tabWidth: Int = 4,
    val useMinimumRaggedness: Boolean = false
) {
    val input: String? = rawInput?.let { if (trimIndent) it.trimIndent() else it }
    val expectedOutput: String = if (trimIndent) rawExpectedOutput.trimIndent() else rawExpectedOutput
}

class CodeWrapperTests {

    private val testCases = listOf(
        WrapTestCase(
            "Test trimIndent option in tests (false)",
            """            This text should not be trimmed """,
            """            This text should not be trimmed""",
            trimIndent = false
        ),
        WrapTestCase(
            "Test trimIndent option in tests (true)",
            """            This text should be trimmed """,
            """            This text should be trimmed""",
            trimIndent = true
        ),
        WrapTestCase(
            "Create without options",
            """
            // This is my text.
            // This is my text.
            """,
            """
            // This is my text. This is my text.
            """
        ),
        WrapTestCase(
            "Wraps to column width - comment",
            """
            // aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            """,
            """
            // aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            // a a a a a a a a a a a a a a a a a a a a a a
            """
        ),
        WrapTestCase(
            "Wraps to column width - C-style opening comment",
            """
            /** aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            */
            """,
            """
            /** aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a aa
             * a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            */
            """
        ),
        WrapTestCase(
            "Wraps to column width - no comment",
            """
            aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            """,
            """
            aa a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a
            a a a a a a a a a a a a a a a a a a a a
            """
        ),
        WrapTestCase(
            "Wraps one long line",
            """
            // This is my very long line of text. This is my very long line of text. This is my very long line of text.
            """,
            """
            // This is my very long line of text. This is my very long line of text. This is
            // my very long line of text.
            """
        ),
        WrapTestCase(
            "Wrap retains separate paragraphs",
            """
            // This is my very long line of text. This is my very long line of text. This is my very long line of text.

            // This is a second paragraph.
            """,
            """
            // This is my very long line of text. This is my very long line of text. This is
            // my very long line of text.

            // This is a second paragraph.
            """
        ),
        WrapTestCase(
            "Wrap wraps Python comments",
            """
            # This is my very long line of text. This is my very long line of text. This is my very long line of text.

            # This is a second paragraph.
            """,
            """
            # This is my very long line of text. This is my very long line of text. This is
            # my very long line of text.

            # This is a second paragraph.
            """
        ),
        WrapTestCase(
            "Wrap combine two short lines",
            """
            // This is my text.
            // This is my text.
            """,
            """
            // This is my text. This is my text.
            """
        ),
        WrapTestCase(
            "Wrap fills multiline opener",
            """
            /** This is my text This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.
            """,
            """
            /** This is my text This is my long multi-line comment opener text. More text
             * please. This is yet another bunch of text in my test comment, so I will get
             * multiple lines in the comment.
            """
        ),
        WrapTestCase(
            "Wrap fills multiline opener with beginning space",
            """
              /* This is my text This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment. */
            """,
            """
              /* This is my text This is my long multi-line comment opener text. More text
               * please. This is yet another bunch of text in my test comment, so I will get
               * multiple lines in the comment. */
            """
        ),
        WrapTestCase(
            "Wrap preserves empty comment lines",
            """
            /*
             * This is my text. This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.
             *
             * This is another line of text.
            */
            """,
            """
            /*
             * This is my text. This is my long multi-line comment opener text. More text
             * please. This is yet another bunch of text in my test comment, so I will get
             * multiple lines in the comment.
             *
             * This is another line of text.
            */
            """
        ),
        WrapTestCase(
            "Wrap multiple comment paragraphs",
            """
            /*
             * This is my text. This is my long multi-line comment opener text. More text please. This is yet another bunch of text in my test comment, so I will get multiple lines in the comment.
             *
             * This is another line of text.
             * 
             * And yet another long line of text. Text going on and on endlessly, much longer than it really should.
            */
            """,
            """
            /*
             * This is my text. This is my long multi-line comment opener text. More text
             * please. This is yet another bunch of text in my test comment, so I will get
             * multiple lines in the comment.
             *
             * This is another line of text.
             * 
             * And yet another long line of text. Text going on and on endlessly, much
             * longer than it really should.
            */
            """
        ),
        WrapTestCase(
            "Wrap retains space indent",
            "    This is my long indented string. It's too long to fit on one line, uh oh! What will happen?",
            "    This is my long indented string. It's too long to fit on one line, uh oh!\n    What will happen?",
            trimIndent = false
        ),
        WrapTestCase(
            "Wrap handles lines within multiline comment",
            """
            * This is a long line in a multi-line comment block. Note the star at the beginning.
            * This is another line in a multi-line comment.
            """,
            """
            * This is a long line in a multi-line comment block. Note the star at the
            * beginning. This is another line in a multi-line comment.
            """,
        ),
        WrapTestCase(
            "Wrap removes extra blank line",
            """
            
            My block of text. My block of text. My block of text. My block of text. My block of text. My block of text.
            """,
            """
            My block of text. My block of text. My block of text. My block of text. My block
            of text. My block of text.
            """
        ),
        WrapTestCase(
            "Wrap preserves leading indent",
            """
            . My long bullet line. My long bullet line. My long bullet line. My long bullet line.
            """,
            """
            . My long bullet line. My long bullet line. My long bullet line. My long bullet
            . line.
            """
        ),
        WrapTestCase(
            "Ignores trailing spaces",
            """
            The quick brown fox 
            jumps over the lazy 
            dog
            """,
            """
            The quick brown fox jumps over the lazy dog
            """
        ),
        WrapTestCase(
            "Preserves comment symbols within text",
            """
            /**
             * Let's provide a javadoc comment that has a link to some method, e.g. {@link #m()}.
             */
            """,
            """
            /**
             * Let's provide a javadoc comment that has a link to some method, e.g. {@link
             * #m()}.
             */
            """
        ),
        WrapTestCase(
            "Wraps null strings",
            null,
            ""
        ),
        WrapTestCase(
            "Accounts for tab width",
            "\t\t\t\tThis is my very long line of text. This is my very long line of text. This is my\t very long line of text.",
            "\t\t\t\tThis is my very long\n\t\t\t\tline of text. This\n\t\t\t\tis my very long line\n\t\t\t\tof text. This is\n\t\t\t\tmy\t very long\n\t\t\t\tline of text.",
            trimIndent = false,
            width = 40,
            tabWidth = 5
        ),
        WrapTestCase(
            "Minimum raggedness",
            """
            lk jsdflkj sdlkfj sdlkfj slkj flkj dslkfj sdlkfj lkjs dflkj sdlfkj sdlkfj lkj sdflkj sdlkj sdlkj fdslkjfsdlkjsd flkj sdflkj sdlfkj sdlfkj sdlkjf sdlkjf dslkj fdslkj fsdlkj flsdkjsldklkslkslkslkslsk djl jsdkf
            """,
            """
            lk jsdflkj sdlkfj sdlkfj slkj flkj dslkfj
            sdlkfj lkjs dflkj sdlfkj sdlkfj lkj sdflkj
            sdlkj sdlkj fdslkjfsdlkjsd flkj sdflkj
            sdlfkj sdlfkj sdlkjf sdlkjf dslkj fdslkj
            fsdlkj flsdkjsldklkslkslkslkslsk djl jsdkf
            """,
            width = 50,
            useMinimumRaggedness = true
        ),
        WrapTestCase(
            "Supports Chinese",
            """
            它是如何工作的呢？实际上，每个bundle在定义自己的服务配置都是跟目前为止你看到的是一样的。换句话说，一个bundle使用一个或者多个配置资源文件（通常是XML)来指定bundle所需要的参数和服务。然而，我们不直接在配置文件中使用 imports 命令导入它们，而是仅仅在bundle中调用一个服务容器扩展来为我们做同样的工作。一个服务容器扩展是 bundle 的作者创建的一个PHP类，它主要完成两件事情
            """,
            """
            它是如何工作的呢？实际上，每个bundle在定义自己的服务配置都是跟目前为止你看到的是一样的。换句话说，一个bundle使用一个或者多个配置资源文件（通常是XML)来指定bundle所需要的参数和服务。然而，我们不直接在配置文件中使用
            imports 命令导入它们，而是仅仅在bundle中调用一个服务容器扩展来为我们做同样的工作。一个服务容器扩展是 bundle
            的作者创建的一个PHP类，它主要完成两件事情
            """,
        ),
        WrapTestCase(
            "Treat HTML newline as paragraph separator",
            """
            /**
             * Text on first paragraph.
             * <p>
             * Text on second paragraph. In this case the line is very long and will be wrapped.
             * <br>
             * Third paragraph.
             * <br/>
             * Fourth paragraph.
             * <Br />
             * Fifth paragraph is the last one.
             */
            """,
            """
            /**
             * Text on first paragraph.
             * <p>
             * Text on second paragraph. In this case the line is very long and will be
             * wrapped.
             * <br>
             * Third paragraph.
             * <br/>
             * Fourth paragraph.
             * <Br />
             * Fifth paragraph is the last one.
             */
            """
        ),
        WrapTestCase(
            "Wraps SQL comments",
            """
            -- This is a SQL comment. It may not be an important comment, but it's mine. My own. My precious.
            """,
            """
            -- This is a SQL comment. It may not be an important comment, but it's mine. My
            -- own. My precious.
            """
        ),
        WrapTestCase(
            "Wraps Python docstrings",
            """
            ${"\"\"\""}
            This is a long docstring comment. It goes on and on to explain how the function works. However, I forgot to add line breaks!
            
            Except, I didn't forget! Here, I added a line break
            and then wrote a very short couple of lines.
            ${"\"\"\""}
            """,
            """
            ${"\"\"\""}
            This is a long docstring comment. It goes on and on to explain how the function
            works. However, I forgot to add line breaks!
            
            Except, I didn't forget! Here, I added a line break and then wrote a very short
            couple of lines.
            ${"\"\"\""}
            """
        ),
        WrapTestCase(
            "Wraps Python docstrings with single quotes",
            """
            '''
            This is a long docstring comment. It goes on and on to explain how the function works. However, I forgot to add line breaks!
            
            Except, I didn't forget! Here, I added a line break
            and then wrote a very short couple of lines.
            '''
            """,
            """
            '''
            This is a long docstring comment. It goes on and on to explain how the function
            works. However, I forgot to add line breaks!
            
            Except, I didn't forget! Here, I added a line break and then wrote a very short
            couple of lines.
            '''
            """
        ),
        WrapTestCase(
            "Wraps Markdown block quotes",
            """
            > Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation 
            """,
            """
            > Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
            > incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis
            > nostrud exercitation
            """
        )
    )

    @Test
    fun runAllTests() {
        testCases.forEach { testCase ->
            val wrapper = CodeWrapper(
                tabWidth = testCase.tabWidth,
                width = testCase.width,
                useMinimumRaggedness = testCase.useMinimumRaggedness
            )
            val result = wrapper.wrap(testCase.input)
            assertEquals(testCase.description, testCase.expectedOutput, result)
        }
    }
}