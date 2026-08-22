package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale

/** Tests for [LintModelCreator]. */
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
    val layerConfig = createTestLayerConfig("app", isLibrary = false)

    val result = lintModelCreator.generateModelFiles(layerConfig)

    assertThat(result.exists()).isTrue()
    assertThat(result.isDirectory).isTrue()
    assertThat(File(result, "build").exists()).isTrue()
    assertThat(File(result, "build/classes").exists()).isTrue()
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsModuleXml() {
    val layerConfig = createTestLayerConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(layerConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    assertThat(moduleXmlFile.exists()).isTrue()

    val content = moduleXmlFile.readText()
    assertThat(content).contains("<lint-module")
    assertThat(content).contains("name=\"utility\"")
    assertThat(content).contains("type=\"LIBRARY\"")
    assertThat(content).contains("javaSourceLevel=\"11.0.16\"")
    assertThat(content).contains("neverShrinking=\"true\"")
    assertThat(content).contains("<variant name=\"main\"/>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsVariantXml() {
    val layerConfig = createTestLayerConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(layerConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    assertThat(variantXmlFile.exists()).isTrue()

    val content = variantXmlFile.readText()
    assertThat(content).contains("<variant")
    assertThat(content).contains("name=\"main\"")
    assertThat(content).contains("minSdkVersion=\"21\"")
    assertThat(content).contains("targetSdkVersion=\"35\"")
    assertThat(content).contains("debuggable=\"true\"")
    assertThat(content).contains("package=\"org.oppia.android.app\"")
    assertThat(content).contains("<buildFeatures")
    assertThat(content).contains("coreLibraryDesugaring=\"true\"")
    assertThat(content).contains("viewBinding=\"true\"")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsArtifactLibrariesXml() {
    val layerConfig = createTestLayerConfig("data", isLibrary = true)

    lintModelCreator.generateModelFiles(layerConfig)

    val librariesXmlFile = File(modelDirectory, "main-mainArtifact-libraries.xml")
    assertThat(librariesXmlFile.exists()).isTrue()

    val content = librariesXmlFile.readText()
    assertThat(content).contains("<libraries>")
    assertThat(content).contains("</libraries>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_createsDependenciesXml() {
    val layerConfig = createTestLayerConfig("testing", isLibrary = true)

    lintModelCreator.generateModelFiles(layerConfig)

    val dependenciesXmlFile = File(modelDirectory, "main-mainArtifact-dependencies.xml")
    assertThat(dependenciesXmlFile.exists()).isTrue()

    val content = dependenciesXmlFile.readText()
    assertThat(content).contains("<dependencies>")
    assertThat(content).contains("</dependencies>")
  }

  @Test
  fun testLintModelCreator_generateModelFiles_usesCacheWhenInputsUnchanged() {
    val layerConfig = createTestLayerConfig("app", isLibrary = false)

    val manifestFile = File(layerConfig.manifestFile)
    manifestFile.parentFile?.mkdirs()
    manifestFile.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="org.oppia.android.app">
    </manifest>
      """.trimIndent()
    )

    layerConfig.srcFiles.forEach { srcFile ->
      val file = File(srcFile)
      file.parentFile?.mkdirs()
      file.writeText("class AppClass")
    }

    val firstResult = lintModelCreator.generateModelFiles(layerConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    val variantXmlFile = File(modelDirectory, "main.xml")
    val cacheFile = File(modelDirectory, ".lint-model-cache")

    val firstModuleXmlTimestamp = moduleXmlFile.lastModified()
    val firstVariantXmlTimestamp = variantXmlFile.lastModified()

    val secondResult = lintModelCreator.generateModelFiles(layerConfig)

    // Files should not be regenerated (same timestamps)
    assertThat(secondResult).isEqualTo(firstResult)
    assertThat(moduleXmlFile.lastModified()).isEqualTo(firstModuleXmlTimestamp)
    assertThat(variantXmlFile.lastModified()).isEqualTo(firstVariantXmlTimestamp)

    assertThat(cacheFile.exists()).isTrue()
    val cacheContent = cacheFile.readText().trim().split("\n")
    assertThat(cacheContent).hasSize(2)
    assertThat(cacheContent[0]).hasLength(64)

    assertThat(cacheContent[1].toLong()).isGreaterThan(0)
  }

  @Test
  fun testLintModelCreator_generateModelFiles_regeneratesWhenInputsChange() {
    val layerConfig = createTestLayerConfig("utility", isLibrary = true)

    val manifestFile = File(layerConfig.manifestFile)
    manifestFile.parentFile?.mkdirs()
    manifestFile.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="org.oppia.android.utility">
    </manifest>
      """.trimIndent()
    )

    layerConfig.srcFiles.forEach { srcFile ->
      val file = File(srcFile)
      file.parentFile?.mkdirs()
      file.writeText("class UtilityClass")
    }

    lintModelCreator.generateModelFiles(layerConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    val cacheFile = File(modelDirectory, ".lint-model-cache")
    val initialModuleXmlTimestamp = moduleXmlFile.lastModified()
    val initialCacheContent = cacheFile.readText()

    manifestFile.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="org.oppia.android.utility.modified">
        <uses-permission android:name="android.permission.INTERNET" />
    </manifest>
      """.trimIndent()
    )

    lintModelCreator.generateModelFiles(layerConfig)

    // Files should be regenerated
    assertThat(moduleXmlFile.lastModified()).isGreaterThan(initialModuleXmlTimestamp)

    // Cache should be updated with new hash
    val newCacheContent = cacheFile.readText()
    assertThat(newCacheContent).isNotEqualTo(initialCacheContent)

    // Verify the hash changed
    val initialHash = initialCacheContent.trim().split("\n")[0]
    val newHash = newCacheContent.trim().split("\n")[0]
    assertThat(newHash).isNotEqualTo(initialHash)
    assertThat(newHash).hasLength(64)

    val moduleXmlContent = moduleXmlFile.readText()
    assertThat(moduleXmlContent).contains("name=\"utility\"")
    assertThat(moduleXmlContent).contains("type=\"LIBRARY\"")
  }

  private fun createTestLayerConfig(layerName: String, isLibrary: Boolean): LayerConfig {
    val basePath = File(tempFolder.root, layerName)

    val manifestPath = "$basePath/src/main/AndroidManifest.xml"
    val resourceDirs = listOf("$basePath/src/main/res")
    val srcFilePath = "$basePath/src/main/java/${layerName.capitalize()}Class.kt"
    val testFilePath = "$basePath/src/test/java/${layerName.capitalize()}ClassTest.kt"

    // Ensure all directories and files exist
    File(resourceDirs[0]).mkdirs()
    File(srcFilePath).apply {
      parentFile?.mkdirs()
      writeText("// dummy source file for $layerName")
    }
    File(testFilePath).apply {
      parentFile?.mkdirs()
      writeText("// dummy test file for $layerName")
    }
    File(manifestPath).apply {
      parentFile?.mkdirs()
      writeText(
        """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.$layerName">
      </manifest>
        """.trimIndent()
      )
    }

    return LayerConfig(
      name = layerName,
      isLibrary = isLibrary,
      isAndroid = true,
      isTest = false,
      manifestFile = manifestPath,
      resourceDirs = resourceDirs,
      dependencies = emptyList(),
      srcFiles = listOf(srcFilePath),
      testFiles = listOf(testFilePath),
      jarFiles = emptyList(),
      aarFiles = emptyList(),
      lintCheckJars = emptyList(),
      proGuardFiles = emptyList(),
      annotationZips = emptyList(),
      partialResultsDir = File(tempFolder.root, "partial-results"),
    )
  }

  private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
  }
}
