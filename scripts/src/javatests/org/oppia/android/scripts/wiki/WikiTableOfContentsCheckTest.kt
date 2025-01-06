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
    System.setOut(PrintStream(outContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testWikiTOCCheck_noWikiDirExists_printsNoContentFound() {
    runScript()
    assertThat(outContent.toString().trim()).isEqualTo("No contents found in the Wiki directory.")
  }

  @Test
  fun testWikiTOCCheck_noWikiDirectory_printsNoContentFound() {
    tempFolder.newFile("wiki")
    runScript()
    assertThat(outContent.toString().trim()).isEqualTo("No contents found in the Wiki directory.")
  }

  @Test
  fun testWikiTOCCheck_validWikiTOC_checkPass() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """
            ## Table of Contents
            
            - [Introduction](#introduction)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
      """.trimIndent()
    )

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_missingWikiTOC_returnsNoTOCFound() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """          
            - [Introduction](#introduction)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
      """.trimIndent()
    )

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_wikiTOCReference_noHeadersFound_throwsException() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """   
            ## Table of Contents
             
            - [Introduction](#introductions)
            
      """.trimIndent()
    )

    val exception = assertThrows<IllegalStateException>() {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(
      "Wiki doesn't contain headers referenced in Table of Contents."
    )
  }

  @Test
  fun testWikiTOCCheck_mismatchWikiTOC_checkFail() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """   
            ## Table of Contents
             
            - [Introduction](#introductions)
            - [Usage](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
      """.trimIndent()
    )

    val exception = assertThrows<IllegalStateException>() {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(WIKI_TOC_CHECK_FAILED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_validWikiTOCWithSeparator_checkPass() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """   
            ## Table of Contents
             
            - [Introduction To Wiki](#introduction-to-wiki)
            - [Usage Wiki-Content](#usage-wiki-content)
            
            ## Introduction
            Content
            
            ## Usage
            Content
      """.trimIndent()
    )

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testWikiTOCCheck_validWikiTOCWithSpecialCharacter_checkPass() {
    tempFolder.newFolder("wiki")
    val file = tempFolder.newFile("wiki/wiki.md")
    file.writeText(
      """   
            ## Table of Contents
             
            - [Introduction](#introduction?)
            - [Usage?](#usage)
            
            ## Introduction
            Content
            
            ## Usage
            Content
      """.trimIndent()
    )

    runScript()

    assertThat(outContent.toString().trim()).contains(WIKI_TOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }
}
