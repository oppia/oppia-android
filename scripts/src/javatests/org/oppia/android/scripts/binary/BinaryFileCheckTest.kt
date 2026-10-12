package org.oppia.android.scripts.binary

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.BinaryFileExemptions
import org.oppia.android.scripts.proto.BinaryFileExemptions.BinaryFileExemption
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [BinaryFileCheck]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BinaryFileCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var repoDir: File

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
    repoDir = tempFolder.newFolder("repo")
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testCheck_emptyDirectory_passes() {
    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_textFileWithAllowedExtension_passes() {
    createRepoFile("example.kt", "package org.oppia.android\nclass Example")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_textprotoFile_passes() {
    createRepoFile(
      "example.textproto",
      """
      test_file_exemption {
        exempted_file_path: "app/src/main/java/Example.kt"
        test_file_not_required: true
      }
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_jsonFile_passes() {
    createRepoFile("config.json", """{"key": "value", "number": 42}""")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_xmlFile_passes() {
    createRepoFile(
      "config.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <string name="app_name">Oppia</string>
      </resources>
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_markdownFile_passes() {
    createRepoFile("README.md", "# Title\n\nSome documentation text.")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_emptyFileWithAllowedExtension_passes() {
    createRepoFile("empty.kt", "")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_fileWithUnicodeLetters_passes() {
    createRepoFile(
      "unicode.kt",
      "package org.oppia\n// Comment with Unicode: résumé naïve café"
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_fileWithUnicodeDigits_passes() {
    createRepoFile("digits.txt", "Unicode digits: \u0661\u0662\u0663 \u0E51\u0E52\u0E53")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_fileWithUnknownExtension_fails() {
    createRepoFile("file.xyz", "some content")

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("Binary files found: 1")
    assertThat(outContent.toString()).contains("file.xyz (.xyz)")
    assertThat(outContent.toString()).contains("please add an exemption to")
  }

  @Test
  fun testCheck_pngFileNotExempted_fails() {
    createRepoFileBytes(
      "icon.png",
      byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("Binary files found: 1")
    assertThat(outContent.toString()).contains("icon.png (.png)")
  }

  @Test
  fun testCheck_fileWithNullBytes_fails() {
    createRepoFileBytes("corrupt.kt", "package org.oppia\u0000binary content".toByteArray())

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("Files with binary content: 1")
    assertThat(outContent.toString()).contains("corrupt.kt")
  }

  @Test
  fun testCheck_exemptedBinaryFileExists_passes() {
    createRepoFileBytes(
      "icon.png",
      byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
    )

    val exemption = BinaryFileExemption.newBuilder().apply {
      this.exemptedFilePath = "icon.png"
    }.build()

    val checkPassed = runScript(exemption)

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_exemptedFileDoesNotExist_fails() {
    val exemption = BinaryFileExemption.newBuilder().apply {
      this.exemptedFilePath = "nonexistent/file.png"
    }.build()

    val checkPassed = runScript(exemption)

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("Stale binary file exemptions: 1")
    assertThat(outContent.toString()).contains("nonexistent/file.png")
    assertThat(outContent.toString()).contains("remove the stale entries")
  }

  @Test
  fun testCheck_multipleTextFiles_allPass() {
    createRepoFile("file.kt", "package org.oppia")
    createRepoFile("file.java", "package org.oppia;")
    createRepoFile("file.xml", "<root/>")
    createRepoFile("file.json", "{}")
    createRepoFile("file.textproto", "field: \"value\"")
    createRepoFile("file.md", "# Title")
    createRepoFile("file.sh", "#!/bin/bash")
    createRepoFile("file.yaml", "key: value")
    createRepoFile("file.proto", "syntax = \"proto3\";")
    createRepoFile("file.bazel", "load()")
    createRepoFile("file.txt", "plain text")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_mixOfGoodAndBadFiles_reportsOnlyBadOnes() {
    createRepoFile("good.kt", "package org.oppia")
    createRepoFile("bad.xyz", "some content")
    createRepoFileBytes("corrupt.kt", "package\u0000binary".toByteArray())

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("Binary files found: 1")
    assertThat(outContent.toString()).contains("bad.xyz")
    assertThat(outContent.toString()).contains("Files with binary content: 1")
    assertThat(outContent.toString()).contains("corrupt.kt")
  }

  /** Creates a text file in the repo directory. */
  private fun createRepoFile(name: String, content: String): File {
    val file = File(repoDir, name)
    file.parentFile?.mkdirs()
    file.writeText(content)
    return file
  }

  /** Creates a file with raw bytes in the repo directory. */
  private fun createRepoFileBytes(name: String, bytes: ByteArray): File {
    val file = File(repoDir, name)
    file.parentFile?.mkdirs()
    file.writeBytes(bytes)
    return file
  }

  private fun runScript(vararg exemptions: BinaryFileExemption): Boolean {
    // Store the .pb file outside the repo directory so it doesn't get scanned.
    val exemptionsFile = File(tempFolder.root, "binary_file_exemptions.pb")
    val exemptionsProto = BinaryFileExemptions.newBuilder().apply {
      addAllBinaryFileExemption(exemptions.toList())
    }.build()
    exemptionsProto.writeTo(exemptionsFile.outputStream())

    val check = BinaryFileCheck(
      repoPath = repoDir.absolutePath + "/",
      binaryFileExemptionProtoPath = exemptionsFile.absolutePath
    )
    return check.execute()
  }
}
