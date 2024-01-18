/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.com.h0tk3y.kotlin.staticObjectNotation

import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.Element
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.LanguageResult
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.LanguageTreeResult
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.MultipleFailuresResult
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.ParseTestUtil.Parser.parseWithAst
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.ParsingError
import com.h0tk3y.kotlin.staticObjectNotation.astToLanguageTree.UnsupportedConstruct
import com.h0tk3y.kotlin.staticObjectNotation.language.Assignment
import com.h0tk3y.kotlin.staticObjectNotation.language.Block
import com.h0tk3y.kotlin.staticObjectNotation.language.ErroneousStatement
import com.h0tk3y.kotlin.staticObjectNotation.language.FunctionArgument
import com.h0tk3y.kotlin.staticObjectNotation.language.FunctionCall
import com.h0tk3y.kotlin.staticObjectNotation.language.Import
import com.h0tk3y.kotlin.staticObjectNotation.language.LanguageTreeElement
import com.h0tk3y.kotlin.staticObjectNotation.language.Literal
import com.h0tk3y.kotlin.staticObjectNotation.language.LocalValue
import com.h0tk3y.kotlin.staticObjectNotation.language.Null
import com.h0tk3y.kotlin.staticObjectNotation.language.PropertyAccess
import com.h0tk3y.kotlin.staticObjectNotation.language.This
import com.h0tk3y.kotlin.staticObjectNotation.language.prettyPrint

fun prettyPrintLanguageTreeResult(languageTreeResult: LanguageTreeResult): String {
    val languageResults = languageTreeResult.imports.map { Element(it) } + languageTreeResult.topLevelBlock.content.map { Element(it) }
    return languageResults.joinToString("\n") { prettyPrintLanguageResult(it) }
}

fun prettyPrintLanguageResult(languageResult: LanguageResult<*>, startDepth: Int = 0): String {
    fun StringBuilder.recurse(current: LanguageResult<*>, depth: Int) {
        fun indent() = "    ".repeat(depth)
        fun nextIndent() = "    ".repeat(depth + 1)
        fun appendIndented(value: Any) {
            append(indent())
            append(value)
        }
        fun appendNextIndented(value: Any) {
            append(nextIndent())
            append(value)
        }
        fun recurseDeeper(next: LanguageResult<*>) = recurse(next, depth + 1)
        when (current) {
            is MultipleFailuresResult -> {
                appendIndented("MultipleFailures(\n")
                current.failures.forEach {
                    recurseDeeper(it)
                    appendLine()
                }
                appendIndented(")")
            }
            is ParsingError -> {
                appendIndented("ParsingError(")
                appendLine()
                appendNextIndented("message = ${current.message}, ")
                appendLine()
                appendNextIndented("potentialElementSource = ${current.potentialElementSource.prettyPrint()}, ")
                appendLine()
                appendNextIndented("erroneousSource = ${current.erroneousSource.prettyPrint()}")
                appendLine()
                appendIndented(")")
            }
            is UnsupportedConstruct -> {
                appendIndented("UnsupportedConstruct(")
                appendLine()
                appendNextIndented("languageFeature = ${current.languageFeature.javaClass.simpleName}, ")
                appendLine()
                appendNextIndented("potentialElementSource = ${current.potentialElementSource.prettyPrint()}, ")
                appendLine()
                appendNextIndented("erroneousSource = ${current.erroneousSource.prettyPrint()}")
                appendLine()
                appendIndented(")")
            }
            is Element -> {
                append(prettyPrintLanguageTree(current.element as LanguageTreeElement))
            }
            else -> { error("Unhandled language result type: ${current.javaClass.simpleName}")}
        }
    }
    return buildString { recurse(languageResult, startDepth) }
}

fun prettyPrintLanguageTree(languageTreeElement: LanguageTreeElement): String {
    fun StringBuilder.recurse(current: LanguageTreeElement, depth: Int) {
        fun indent() = "    ".repeat(depth)
        fun nextIndent() = "    ".repeat(depth + 1)
        fun appendIndented(value: Any) {
            append(indent())
            append(value)
        }

        fun appendNextIndented(value: Any) {
            append(nextIndent())
            append(value)
        }

        fun recurseDeeper(next: LanguageTreeElement) = recurse(next, depth + 1)

        fun source() = current.sourceData.prettyPrint()

        when (current) {
            is Block -> {
                append("Block [${source()}] (\n")
                current.content.forEach {
                    append(nextIndent())
                    recurseDeeper(it)
                    appendLine()
                }
                appendIndented(")")
            }

            is Assignment -> {
                append("Assignment [${source()}] (\n")
                appendNextIndented("lhs = ")
                recurseDeeper(current.lhs)
                appendLine()
                appendNextIndented("rhs = ")
                recurseDeeper(current.rhs)
                appendLine()
                appendIndented(")")
            }

            is FunctionCall -> {
                append("FunctionCall [${source()}] (\n")
                appendNextIndented("name = ${current.name}\n")
                current.receiver?.let {
                    appendNextIndented("receiver = ")
                    recurseDeeper(it)
                    appendLine()
                }
                if (current.args.isNotEmpty()) {
                    appendNextIndented("args = [\n")
                    current.args.forEach {
                        appendNextIndented("    ")
                        recurse(it, depth + 2)
                        appendLine()
                    }
                    appendNextIndented("]\n")
                } else {
                    appendNextIndented("args = []\n")
                }
                appendIndented(")")
            }

            is Literal.BooleanLiteral -> {
                append("BooleanLiteral [${source()}] (${current.value})")
            }

            is Literal.IntLiteral -> {
                append("IntLiteral [${source()}] (${current.value})")
            }

            is Literal.LongLiteral -> {
                append("LongLiteral [${source()}] (${current.value})")
            }

            is Literal.StringLiteral -> {
                append("StringLiteral [${source()}] (${current.value})")
            }

            is Null -> append("Null")
            is PropertyAccess -> {
                append("PropertyAccess [${source()}] (\n")
                current.receiver?.let { receiver ->
                    appendNextIndented("receiver = ")
                    recurseDeeper(receiver)
                    appendLine()
                }
                appendNextIndented("name = ${current.name}\n")
                appendIndented(")")
            }

            is This -> append("This")

            is LocalValue -> {
                append("LocalValue [${source()}] (\n")
                appendNextIndented("name = ${current.name}\n")
                appendNextIndented("rhs = ")
                recurseDeeper(current.rhs)
                appendLine()
                appendIndented(")")
            }

            is FunctionArgument.Lambda -> {
                append("FunctionArgument.Lambda [${source()}] (\n")
                appendNextIndented("block = ")
                recurseDeeper(current.block)
                appendLine()
                appendIndented(")")
            }

            is FunctionArgument.Named -> {
                append("FunctionArgument.Named [${source()}] (\n")
                appendNextIndented("name = ${current.name},\n")
                appendNextIndented("expr = ")
                recurseDeeper(current.expr)
                appendLine()
                appendIndented(")")
            }

            is FunctionArgument.Positional -> {
                append("FunctionArgument.Positional [${source()}] (\n")
                appendNextIndented("expr = ")
                recurseDeeper(current.expr)
                appendLine()
                appendIndented(")")
            }

            is Import -> {
                append("Import [${source()} (\n")
                appendNextIndented("name parts = ${current.name.nameParts}")
                appendLine()
                appendIndented(")")
            }

            is ErroneousStatement -> {
                append("ErroneousStatement (\n")
                append(prettyPrintLanguageResult(current.failingResult, depth + 1))
                appendLine()
                appendIndented(")")
            }
        }
    }

    return buildString { recurse(languageTreeElement, 0) }
}

fun main() {
    val result = parseWithAst(
        """
        rootProject.name = "test-value"
        include(":a")
        include(projectPath = ":b")

        dependencyResolutionManagement {
            repositories {
                mavenCentral()
                google()
            }
        }
        pluginManagement {
            includeBuild("pluginIncluded")
            repositories {
                mavenCentral()
                google() }
        }
        """.trimIndent()
    ).topLevelBlock

    println(prettyPrintLanguageTree(result))
}
