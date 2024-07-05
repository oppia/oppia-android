package org.oppia.android.scripts.wiki

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [WikiTableOfContentsCheck]. */
class WikiTableOfContentsCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testValidToCWithMatchingHeaders() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/ValidToCWithMatchingHeaders.md")
    file.writeText("""
            ## Table of Contents
            
            - [Introduction](#introduction)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
        """.trimIndent())

    runScript()

    assertThat(outContent.toString().trim()).contains("WIKI TABLE OF CONTENTS CHECK PASSED")
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }
}