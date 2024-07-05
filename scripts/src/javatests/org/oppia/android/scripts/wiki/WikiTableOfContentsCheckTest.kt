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
  private val WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR = "WIKI TABLE OF CONTENTS CHECK PASSED"
  private val WIKI_TOC_CHECK_FAILED_OUTPUT_INDICATOR = "WIKI TABLE OF CONTENTS CHECK FAILED"

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("wiki")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testWikiTOCCheck_validWikiTOC_checkPass() {
    val file = tempFolder.newFile("wiki/wiki.md")
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

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_missingWikiTOC_returnsNoTOCFound() {
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText("""          
            - [Introduction](#introduction)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
        """.trimIndent())

    runScript()

    assertThat(outContent.toString().trim()).contains("No Table of Contents found")
  }

  @Test
  fun testWikiTOCCheck_mismatchWikiTOC_checkFail() {
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText("""   
            ## Table of Contents
             
            - [Introduction](#introductions)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
        """.trimIndent())

    val exception = assertThrows<IllegalStateException>() {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(WIKI_TOC_CHECK_FAILED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_validWikiTOCWithSeparator_checkPass() {
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText("""   
            ## Table of Contents
             
            - [Introduction To Wiki](#introduction-to-wiki)
            - [Usage Wiki-Content](#usage-wiki-content)
            
            ## Introduction
            Content
            
            ## Usage
            Content
        """.trimIndent())

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_validWikiTOCWithSpecialCharacter_checkPass() {
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText("""   
            ## Table of Contents
             
            - [Introduction](#introduction?)
            - [Usage?](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
        """.trimIndent())

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }
}