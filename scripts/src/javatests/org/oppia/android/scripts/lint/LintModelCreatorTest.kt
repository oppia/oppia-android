package org.oppia.android.scripts.lint

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.xml.sax.SAXException
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

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
      repoRoot = tempFolder.root
    )
  }
  @Test
  fun testLintModelCreator_generateModelFiles_createsRequiredDirectories() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    val result = lintModelCreator.generateModelFiles(moduleConfig)

    Truth.assertThat(result.exists()).isTrue()
    Truth.assertThat(result.isDirectory).isTrue()
    Truth.assertThat(File(result, "build").exists()).isTrue()
    Truth.assertThat(File(result, "build/classes").exists()).isTrue()
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsModuleXml() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    Truth.assertThat(moduleXmlFile.exists()).isTrue()

    val content = moduleXmlFile.readText()
    Truth.assertThat(content).contains("<lint-module")
    Truth.assertThat(content).contains("name=\"utility\"")
    Truth.assertThat(content).contains("type=\"LIBRARY\"")
    Truth.assertThat(content).contains("maven=\"__non_maven__\"")
    Truth.assertThat(content).contains("neverShrinking=\"true\"")
    Truth.assertThat(content).contains("<variant name=\"main\"/>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsVariantXml() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    Truth.assertThat(variantXmlFile.exists()).isTrue()

    val content = variantXmlFile.readText()
    Truth.assertThat(content).contains("<variant")
    Truth.assertThat(content).contains("name=\"main\"")
    Truth.assertThat(content).contains("minSdkVersion=\"21\"")
    Truth.assertThat(content).contains("targetSdkVersion=\"34\"")
    Truth.assertThat(content).contains("debuggable=\"true\"")
    Truth.assertThat(content).contains("package=\"org.oppia.android.app\"")
    Truth.assertThat(content).contains("<buildFeatures")
    Truth.assertThat(content).contains("coreLibraryDesugaring=\"true\"")
    Truth.assertThat(content).contains("viewBinding=\"true\"")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsArtifactLibrariesXml() {
    val moduleConfig = createTestModuleConfig("data", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val librariesXmlFile = File(modelDirectory, "main-mainArtifact-libraries.xml")
    Truth.assertThat(librariesXmlFile.exists()).isTrue()

    val content = librariesXmlFile.readText()
    Truth.assertThat(content).contains("<libraries>")
    Truth.assertThat(content).contains("</libraries>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsDependenciesXml() {
    val moduleConfig = createTestModuleConfig("testing", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val dependenciesXmlFile = File(modelDirectory, "main-mainArtifact-dependencies.xml")
    Truth.assertThat(dependenciesXmlFile.exists()).isTrue()

    val content = dependenciesXmlFile.readText()
    Truth.assertThat(content).contains("<dependencies>")
    Truth.assertThat(content).contains("</dependencies>")
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
