package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/** Tests for [LintCommon]. */
@Suppress("FunctionName")
class LintCommonTest {
  @field:[Rule JvmField]
  val tempFolder = TemporaryFolder()

  private lateinit var workingDirectory: File

  @Before
  fun setUp() {
    workingDirectory = tempFolder.newFolder("working-directory")
  }

  @Test
  fun testModuleName_allModules_haveCorrectNames() {
    assertThat(ModuleName.APP.moduleName).isEqualTo("app")
    assertThat(ModuleName.DOMAIN.moduleName).isEqualTo("domain")
    assertThat(ModuleName.TESTING.moduleName).isEqualTo("testing")
    assertThat(ModuleName.UTILITY.moduleName).isEqualTo("utility")
    assertThat(ModuleName.DATA.moduleName).isEqualTo("data")
  }

  @Test
  fun testModuleName_applicationModule_isAppModule() {
    assertThat(ModuleName.APPLICATION_MODULE).isEqualTo(ModuleName.APP)
    assertThat(ModuleName.APPLICATION_MODULE.moduleName).isEqualTo("app")
  }

  @Test
  fun testModuleName_libraryModules_containsExpectedModules() {
    val libraryModules = ModuleName.LIBRARY_MODULES
    val expectedModules =
      listOf(ModuleName.DOMAIN, ModuleName.TESTING, ModuleName.UTILITY, ModuleName.DATA)

    assertThat(libraryModules).containsExactlyElementsIn(expectedModules)
    assertThat(libraryModules).doesNotContain(ModuleName.APP)
  }

  @Test
  fun testModuleConfig_validConfiguration_createsSuccessfully() {
    val config = createValidModuleConfig()

    assertThat(config.name).isEqualTo("test-module")
    assertThat(config.isAndroid).isTrue()
    assertThat(config.isLibrary).isFalse()
    assertThat(config.isTest).isTrue()
    assertThat(config.lintModelDir).isNotNull()
  }

  @Test
  fun testModuleConfig_defaultLintModelDir_isNull() {
    val config = createMinimalModuleConfig()

    assertThat(config.lintModelDir).isNull()
  }

  @Test
  fun testModuleConfig_blankName_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(name = "")
    }

    assertThat(exception).hasMessageThat().contains("Module name cannot be blank")
  }

  @Test
  fun testModuleConfig_nonExistentProGuardFile_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(proGuardFiles = listOf("/non/existent/proguard.pro"))
    }

    assertThat(exception).hasMessageThat().contains("ProGuard files do not exist")
    assertThat(exception).hasMessageThat().contains("/non/existent/proguard.pro")
  }

  @Test
  fun testModuleConfig_nonExistentAarFile_throwsException() {
    val nonExistentAar =
      AarFileInfo("/non/existent/test.aar", "extracted/path")

    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(aarFiles = listOf(nonExistentAar))
    }

    assertThat(exception).hasMessageThat().contains("AAR files do not exist")
    assertThat(exception).hasMessageThat().contains("/non/existent/test.aar")
  }

  @Test
  fun testModuleConfig_nonExistentResourceDir_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(resourceDirs = listOf("/non/existent/res"))
    }

    assertThat(exception).hasMessageThat().contains("Missing or invalid resource directories")
  }

  @Test
  fun testModuleConfig_nonExistentSourceFiles_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(srcFiles = listOf("/non/existent/Source.kt"))
    }

    assertThat(exception).hasMessageThat().contains("Missing source files")
  }

  @Test
  fun testModuleConfig_nonExistentTestFiles_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(testFiles = listOf("/non/existent/Test.kt"))
    }

    assertThat(exception).hasMessageThat().contains("Missing test files")
  }

  @Test
  fun testModuleConfig_nonExistentJarFiles_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      createMinimalModuleConfig(jarFiles = listOf("/non/existent/lib.jar"))
    }

    assertThat(exception).hasMessageThat().contains("Missing JAR files")
  }

  @Test
  fun testModuleConfig_libraryModule_hasCorrectFlags() {
    val config = createMinimalModuleConfig(name = "utility", isLibrary = true)

    assertThat(config.isLibrary).isTrue()
    assertThat(config.name).isEqualTo("utility")
  }

  @Test
  fun testModuleConfig_appModule_hasCorrectFlags() {
    val config = createMinimalModuleConfig(name = "app", isLibrary = false)

    assertThat(config.isLibrary).isFalse()
    assertThat(config.name).isEqualTo("app")
  }

  @Test
  fun testAarFileInfo_creation_setsCorrectPaths() {
    val aarInfo = AarFileInfo(
      originalPath = "/original/path/library.aar",
      extractedPath = "/extracted/path/library"
    )

    assertThat(aarInfo.originalPath).isEqualTo("/original/path/library.aar")
    assertThat(aarInfo.extractedPath).isEqualTo("/extracted/path/library")
  }

  @Test
  fun testAarFileInfo_multipleInstances_maintainIndependentPaths() {
    val aar1 = AarFileInfo("path1/lib1.aar", "extracted1/lib1")
    val aar2 = AarFileInfo("path2/lib2.aar", "extracted2/lib2")

    assertThat(aar1.originalPath).isEqualTo("path1/lib1.aar")
    assertThat(aar1.extractedPath).isEqualTo("extracted1/lib1")
    assertThat(aar2.originalPath).isEqualTo("path2/lib2.aar")
    assertThat(aar2.extractedPath).isEqualTo("extracted2/lib2")
  }

  @Test
  fun testLintLogger_logError_createsLogFile() {
    val logger = LintLogger(workingDirectory)

    logger.logError("Test error message")

    val logFile = File(workingDirectory, "error-logs")
    assertThat(logFile.exists()).isTrue()
  }

  @Test
  fun testLintLogger_logError_writesMessageWithTimestamp() {
    val logger = LintLogger(workingDirectory)

    logger.logError("Test error message")

    val logFile = File(workingDirectory, "error-logs")
    val content = logFile.readText()
    assertThat(content).contains("Test error message")
    assertThat(content).contains("[")
    assertThat(content).contains("]")
  }

  @Test
  fun testLintLogger_multipleErrors_appendsToSameFile() {
    val logger = LintLogger(workingDirectory)

    logger.logError("First error")
    logger.logError("Second error")

    val logFile = File(workingDirectory, "error-logs")
    val content = logFile.readText()
    val lines = content.lines().filter { it.isNotEmpty() }

    assertThat(content).contains("First error")
    assertThat(content).contains("Second error")
    assertThat(lines).hasSize(2)
  }

  @Test
  fun testJavaConfiguration_validBazelInfo_setsCorrectProperties() {
    val bazelInfo = createValidBazelInfo()

    val javaConfig = JavaConfiguration(bazelInfo)

    assertThat(javaConfig.getJdkHome()).isEqualTo(File("/usr/lib/jvm/java-11"))
    assertThat(javaConfig.getVersion()).isEqualTo("11.0.16")
  }

  @Test
  fun testJavaConfiguration_differentJavaVersion_extractsCorrectVersion() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-17",
      "java-runtime" to "OpenJDK Runtime Environment (build 17.0.2+8-Ubuntu)"
    )

    val javaConfig = JavaConfiguration(bazelInfo)

    assertThat(javaConfig.getJdkHome()).isEqualTo(File("/usr/lib/jvm/java-17"))
    assertThat(javaConfig.getVersion()).isEqualTo("17.0.2")
  }

  @Test
  fun testJavaConfiguration_missingJavaHome_throwsException() {
    val bazelInfo = mapOf("java-runtime" to "OpenJDK Runtime Environment (build 11.0.16+8-post)")

    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }

    assertThat(exception).hasMessageThat().contains("java-home not found in bazel info output")
  }

  @Test
  fun testJavaConfiguration_missingJavaRuntime_throwsException() {
    val bazelInfo = mapOf("java-home" to "/usr/lib/jvm/java-11")
    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }

    assertThat(exception).hasMessageThat().contains("java-runtime not found in bazel info output")
  }

  @Test
  fun testJavaConfiguration_invalidVersionFormat_throwsException() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11",
      "java-runtime" to "Invalid runtime string without version"
    )

    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }

    assertThat(exception).hasMessageThat().contains("Could not extract Java version from:")
  }

  private fun createMinimalModuleConfig(
    name: String = "test-module",
    isLibrary: Boolean = false,
    isAndroid: Boolean = true,
    isTest: Boolean = false,
    srcFiles: List<String> = emptyList(),
    testFiles: List<String> = emptyList(),
    resourceDirs: List<String> = emptyList(),
    jarFiles: List<String> = emptyList(),
    aarFiles: List<AarFileInfo> = emptyList(),
    proGuardFiles: List<String> = emptyList()
  ): ModuleConfig {
    return ModuleConfig(
      name = name,
      isLibrary = isLibrary,
      isAndroid = isAndroid,
      isTest = isTest,
      srcFiles = srcFiles,
      testFiles = testFiles,
      resourceDirs = resourceDirs,
      manifestFile = "",
      dependencies = emptyList(),
      jarFiles = jarFiles,
      aarFiles = aarFiles,
      lintCheckJars = emptyList(),
      annotationZips = emptyList(),
      partialResultsDir = File(workingDirectory, "partial-results"),
      proGuardFiles = proGuardFiles
    )
  }

  private fun createValidModuleConfig(): ModuleConfig {
    val proGuardFile = File(workingDirectory, "proguard-rules.pro").apply { createNewFile() }
    val lintCheckJar = File(workingDirectory, "lint-check.jar").apply { createNewFile() }
    val aarFile = File(workingDirectory, "test.aar").apply { createNewFile() }
    val jarFile = File(workingDirectory, "test.jar").apply { createNewFile() }

    val srcDir = File(workingDirectory, "src/main/java").apply { mkdirs() }
    val srcFile = File(srcDir, "Test.kt").apply { createNewFile() }

    val testDir = File(workingDirectory, "src/test/java").apply { mkdirs() }
    val testFile = File(testDir, "TestTest.kt").apply { createNewFile() }

    val resourceDir = File(workingDirectory, "src/main/res").apply { mkdirs() }

    return ModuleConfig(
      name = "test-module",
      isAndroid = true,
      isLibrary = false,
      isTest = true,
      srcFiles = listOf(srcFile.absolutePath),
      testFiles = listOf(testFile.absolutePath),
      resourceDirs = listOf(resourceDir.absolutePath),
      manifestFile = "src/main/AndroidManifest.xml",
      dependencies = listOf("dependency1", "dependency2"),
      aarFiles = listOf(AarFileInfo(aarFile.absolutePath, "extracted/path")),
      jarFiles = listOf(jarFile.absolutePath),
      lintCheckJars = listOf(lintCheckJar.absolutePath),
      lintModelDir = File(workingDirectory, "model-dir"),
      annotationZips = listOf("annotation.zip"),
      proGuardFiles = listOf(proGuardFile.absolutePath),
      partialResultsDir = File(workingDirectory, "partial-results")
    )
  }

  private fun createValidBazelInfo(): Map<String, String> {
    return mapOf(
      "java-home" to "/usr/lib/jvm/java-11",
      "java-runtime" to "OpenJDK Runtime Environment (build 11.0.16+8-post)"
    )
  }
}
