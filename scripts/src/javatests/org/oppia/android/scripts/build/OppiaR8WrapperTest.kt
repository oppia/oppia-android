package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/** Tests for [OppiaR8Wrapper]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class OppiaR8WrapperTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Test
  fun testRun_emptyArgs_passesReleaseAndClassfileFlags() {
    val capturedArgs = runWrapperWithCapturedArgs()

    assertThat(capturedArgs).containsExactly("--release", "--classfile").inOrder()
  }

  @Test
  fun testRun_forceProcessingFlag_isOmittedFromR8Args() {
    val capturedArgs = runWrapperWithCapturedArgs("-forceprocessing")

    assertThat(capturedArgs).containsExactly("--release", "--classfile").inOrder()
  }

  @Test
  fun testRun_injarsFlag_passesInputJarPositionally() {
    val capturedArgs = runWrapperWithCapturedArgs("-injars", "input.jar")

    assertThat(capturedArgs).containsExactly("--release", "--classfile", "input.jar").inOrder()
  }

  @Test
  fun testRun_outjarsFlag_translatesToOutput() {
    val capturedArgs = runWrapperWithCapturedArgs("-outjars", "output.jar")

    assertThat(capturedArgs)
      .containsExactly("--release", "--classfile", "--output", "output.jar")
      .inOrder()
  }

  @Test
  fun testRun_libraryjarsFlag_translatesToLib() {
    val capturedArgs = runWrapperWithCapturedArgs("-libraryjars", "android.jar")

    assertThat(capturedArgs)
      .containsExactly("--release", "--classfile", "--lib", "android.jar")
      .inOrder()
  }

  @Test
  fun testRun_atConfigFile_translatesToPgConf() {
    val capturedArgs = runWrapperWithCapturedArgs("@proguard-rules.pro")

    assertThat(capturedArgs)
      .containsExactly("--release", "--classfile", "--pg-conf", "proguard-rules.pro")
      .inOrder()
  }

  @Test
  fun testRun_printMappingAndConfigFlags_translateToR8OutputFlags() {
    val capturedArgs = runWrapperWithCapturedArgs(
      "-printmapping",
      "mapping.txt",
      "-printconfiguration",
      "config.txt"
    )

    assertThat(capturedArgs)
      .containsExactly(
        "--release",
        "--classfile",
        "--pg-map-output",
        "mapping.txt",
        "--pg-conf-output",
        "config.txt"
      )
      .inOrder()
  }

  @Test
  fun testRun_unrecognizedArg_isPassedThroughUnchanged() {
    val capturedArgs = runWrapperWithCapturedArgs("--pg-compat", "extra.jar")

    assertThat(capturedArgs)
      .containsExactly("--release", "--classfile", "--pg-compat", "extra.jar")
      .inOrder()
  }

  @Test
  fun testRun_printSeedsAndPrintUsage_createEmptyPlaceholderFilesAndOmitFromR8Args() {
    val seedsFile = File(tempFolder.root, "nested/seeds/seeds.txt")
    val usageFile = File(tempFolder.root, "nested/usage/usage.txt")

    val capturedArgs = runWrapperWithCapturedArgs(
      "-printseeds",
      seedsFile.absolutePath,
      "-printusage",
      usageFile.absolutePath
    )

    assertThat(capturedArgs).containsExactly("--release", "--classfile").inOrder()
    assertThat(seedsFile.exists()).isTrue()
    assertThat(seedsFile.readText()).isEmpty()
    assertThat(usageFile.exists()).isTrue()
    assertThat(usageFile.readText()).isEmpty()
  }

  @Test
  fun testRun_missingArgValues_throwsIllegalArgumentException() {
    val flagsRequiringValue = listOf(
      "-injars",
      "-outjars",
      "-libraryjars",
      "-printmapping",
      "-printconfiguration",
      "-printseeds",
      "-printusage"
    )

    flagsRequiringValue.forEach { flag ->
      val exception = assertThrows<IllegalArgumentException> {
        runWrapperWithCapturedArgs(flag)
      }
      assertThat(exception).hasMessageThat().contains(flag)
    }
  }

  @Test
  fun testRun_emptyAtConfigArg_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      runWrapperWithCapturedArgs("@")
    }

    assertThat(exception).hasMessageThat().contains("@")
  }

  @Test
  fun testMain_endToEndWithRealR8_shrinksJarAndWritesExpectedOutputs() {
    val inputJar = createSampleInputJar(tempFolder.newFile("input.jar"))
    val outputJar = File(tempFolder.root, "output.jar")
    val mappingFile = File(tempFolder.root, "mapping.txt")
    val configOutputFile = File(tempFolder.root, "resolved_config.txt")
    val seedsFile = File(tempFolder.root, "seeds.txt")
    val usageFile = File(tempFolder.root, "usage.txt")
    val rulesFile = tempFolder.newFile("rules.pro").apply {
      writeText(
        """
        -keep class **SampleInputClass { *; }
        -dontwarn java.**
        -dontwarn kotlin.**
        -dontwarn org.jetbrains.annotations.**
        """.trimIndent()
      )
    }

    main(
      "-forceprocessing",
      "-injars",
      inputJar.absolutePath,
      "-outjars",
      outputJar.absolutePath,
      "-libraryjars",
      checkNotNull(System.getProperty("java.home")) { "Expected java.home system property." },
      "@${rulesFile.absolutePath}",
      "-printmapping",
      mappingFile.absolutePath,
      "-printconfiguration",
      configOutputFile.absolutePath,
      "-printseeds",
      seedsFile.absolutePath,
      "-printusage",
      usageFile.absolutePath
    )

    assertThat(outputJar.exists()).isTrue()
    assertThat(mappingFile.exists()).isTrue()
    assertThat(configOutputFile.exists()).isTrue()
    assertThat(seedsFile.exists()).isTrue()
    assertThat(usageFile.exists()).isTrue()
    val expectedEntryName =
      "${SampleInputClass::class.java.name.replace('.', '/')}.class"
    JarFile(outputJar).use { jar ->
      val entryNames = jar.entries().asSequence().map { it.name }.toList()
      assertThat(entryNames).contains(expectedEntryName)
    }
  }

  private fun runWrapperWithCapturedArgs(vararg args: String): List<String> {
    var capturedArgs: List<String>? = null
    OppiaR8Wrapper { translatedArgs ->
      capturedArgs = translatedArgs.toList()
    }.run(args)
    return checkNotNull(capturedArgs) { "Expected r8Runner to be invoked." }
  }

  private fun createSampleInputJar(destination: File): File {
    val entryName = "${SampleInputClass::class.java.name.replace('.', '/')}.class"
    val classBytes = checkNotNull(
      SampleInputClass::class.java.classLoader?.getResourceAsStream(entryName)
    ) { "Expected class bytes for $entryName on test classpath." }.use { it.readBytes() }
    JarOutputStream(destination.outputStream()).use { jarOut ->
      jarOut.putNextEntry(JarEntry(entryName))
      jarOut.write(classBytes)
      jarOut.closeEntry()
    }
    return destination
  }

  /** Minimal sample class packaged into a JAR for end-to-end R8 verification. */
  class SampleInputClass {
    /** Returns a constant greeting so R8 retains a concrete method body. */
    fun greet(): String = "hello"
  }
}
