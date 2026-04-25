package io.github.kotlinmania.starlark_kotlin.docs.tests

import kotlin.test.Test

class MarkdownTest {
    @Test
    fun goldenDocsStarlarkTest() {
        goldenDocsStarlark()
    }

    @Test
    fun nativeDocsModuleTest() {
        nativeDocsModule()
    }

    @Test
    fun globalsRenderDefaultTest() {
        globalsRenderDefault()
    }

    @Test
    fun globalsRenderDefaultWithLinkedTypeTest() {
        globalsRenderDefaultWithLinkedType()
    }

    @Test
    fun globalsRenderSignatureAtBottomTest() {
        globalsRenderSignatureAtBottom()
    }

    @Test
    fun globalsRenderSignatureAtBottomWithLinkedTypeTest() {
        globalsRenderSignatureAtBottomWithLinkedType()
    }

    @Test
    fun goldenDocsObjectTest() {
        goldenDocsObject()
    }
}

