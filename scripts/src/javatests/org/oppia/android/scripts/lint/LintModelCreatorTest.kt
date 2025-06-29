package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale

/** Comprehensive tests for [LintModelCreator]. */
@Suppress("FunctionName")
class LintModelCreatorTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()
  private lateinit var lintModelCreator: LintModelCreator
  private lateinit var modelDirectory: File

  @Before
  fun setUp() {
    modelDirectory = tempFolder.newFolder("model-directory")
    lintModelCreator = LintModelCreator(
      modelDir = modelDirectory,
      repoRoot = tempFolder.root,
      bazelInfo = mapOf(
        "java-home" to "/usr/lib/jvm/java-11",
        "java-runtime" to "OpenJDK Runtime Environment (build 11.0.16+8-post)"
      )
    )
  }
  @Test
  fun testLintModelCreator_generateModelFiles_createsRequiredDirectories() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    val result = lintModelCreator.generateModelFiles(moduleConfig)

    assertThat(result.exists()).isTrue()
    assertThat(result.isDirectory).isTrue()
    assertThat(File(result, "build").exists()).isTrue()
    assertThat(File(result, "build/classes").exists()).isTrue()
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsModuleXml() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    assertThat(moduleXmlFile.exists()).isTrue()

    val content = moduleXmlFile.readText()
    assertThat(content).contains("<lint-module")
    assertThat(content).contains("name=\"utility\"")
    assertThat(content).contains("type=\"LIBRARY\"")
    assertThat(content).contains("maven=\"__non_maven__\"")
    assertThat(content).contains("javaSourceLevel=\"11.0.16\"")
    assertThat(content).contains("neverShrinking=\"true\"")
    assertThat(content).contains("<variant name=\"main\"/>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsVariantXml() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    assertThat(variantXmlFile.exists()).isTrue()

    val content = variantXmlFile.readText()
    assertThat(content).contains("<variant")
    assertThat(content).contains("name=\"main\"")
    assertThat(content).contains("minSdkVersion=\"21\"")
    assertThat(content).contains("targetSdkVersion=\"34\"")
    assertThat(content).contains("debuggable=\"true\"")
    assertThat(content).contains("package=\"org.oppia.android.app\"")
    assertThat(content).contains("<buildFeatures")
    assertThat(content).contains("coreLibraryDesugaring=\"true\"")
    assertThat(content).contains("viewBinding=\"true\"")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsArtifactLibrariesXml() {
    val moduleConfig = createTestModuleConfig("data", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val librariesXmlFile = File(modelDirectory, "main-mainArtifact-libraries.xml")
    assertThat(librariesXmlFile.exists()).isTrue()

    val content = librariesXmlFile.readText()
    assertThat(content).contains("<libraries>")
    assertThat(content).contains("</libraries>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsDependenciesXml() {
    val moduleConfig = createTestModuleConfig("testing", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val dependenciesXmlFile = File(modelDirectory, "main-mainArtifact-dependencies.xml")
    assertThat(dependenciesXmlFile.exists()).isTrue()

    val content = dependenciesXmlFile.readText()
    assertThat(content).contains("<dependencies>")
    assertThat(content).contains("</dependencies>")
  }

  private fun createTestModuleConfig(moduleName: String, isLibrary: Boolean): ModuleConfig {
    val manifestPath = "${tempFolder.root}/$moduleName/src/main/AndroidManifest.xml"
    val resourceDirs = listOf("${tempFolder.root}/$moduleName/src/main/res")
    val srcFiles =
      listOf("${tempFolder.root}/$moduleName/src/main/java/${moduleName.capitalize()}Class.kt")
    val testFiles =
      listOf("${tempFolder.root}/$moduleName/src/test/java/${moduleName.capitalize()}ClassTest.kt")

    return ModuleConfig(
      name = moduleName,
      isLibrary = isLibrary,
      isAndroid = true,
      isTest = false,
      manifestFile = manifestPath,
      resourceDirs = resourceDirs,
      dependencies = emptyList(),
      srcFiles = srcFiles,
      testFiles = testFiles,
      jarFiles = emptyList(),
      aarFiles = emptyList(),
      lintCheckJars = emptyList()
    )
  }

  private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
  }
}
