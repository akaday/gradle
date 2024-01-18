package com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree

import com.h0tk3y.kotlin.staticObjectNotation.language.SourceIdentifier
import org.intellij.lang.annotations.Language

class ParseTestUtil {

    companion object Parser {
        fun parseWithAst(@Language("kts") code: String): LanguageTreeResult {
            val ast = parseToAst(code)
            return DefaultLanguageTreeBuilder().build(ast, SourceIdentifier("test"))
        }

        fun parseWithLightParser(@Language("kts") code: String): LanguageTreeResult {
            val (tree, sourceCode, sourceOffset) = parseToLightTree(code)
            return DefaultLanguageTreeBuilder().build(tree, sourceCode, sourceOffset, SourceIdentifier("test"))
        }
    }

}
