package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File
import java.util.Locale

/** Tests for [LintCommonTest]. */
@Suppress("FunctionName")
class LintCommonTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()
  private lateinit var workingDirectory: File

  @Before
  fun setUp() {
    workingDirectory = tempFolder.newFolder("working-directory")
  }

  @Test
  fun testModuleName_app_hasCorrectModuleName() {
    val appModule = ModuleName.APP

    assertThat(appModule.moduleName).isEqualTo("app")
  }

  @Test
  fun testModuleName_domain_hasCorrectModuleName() {
    val domainModule = ModuleName.DOMAIN

    assertThat(domainModule.moduleName).isEqualTo("domain")
  }

  @Test
  fun testModuleName_testing_hasCorrectModuleName() {
    val testingModule = ModuleName.TESTING

    assertThat(testingModule.moduleName).isEqualTo("testing")
  }

  @Test
  fun testModuleName_utility_hasCorrectModuleName() {
    val utilityModule = ModuleName.UTILITY

    assertThat(utilityModule.moduleName).isEqualTo("utility")
  }

  @Test
  fun testModuleName_data_hasCorrectModuleName() {
    val dataModule = ModuleName.DATA

    assertThat(dataModule.moduleName).isEqualTo("data")
  }

  @Test
  fun testModuleName_applicationModule_returnsAppModule() {
    val applicationModule = ModuleName.APPLICATION_MODULE

    assertThat(applicationModule).isEqualTo(ModuleName.APP)
    assertThat(applicationModule.moduleName).isEqualTo("app")
  }

  @Test
  fun testModuleName_libraryModules_containsAllLibraryModules() {
    val libraryModules = ModuleName.LIBRARY_MODULES

    assertThat(libraryModules).hasSize(4)
    assertThat(libraryModules).contains(ModuleName.DOMAIN)
    assertThat(libraryModules).contains(ModuleName.TESTING)
    assertThat(libraryModules).contains(ModuleName.UTILITY)
    assertThat(libraryModules).contains(ModuleName.DATA)
  }

  @Test
  fun testModuleName_libraryModules_doesNotContainAppModule() {
    val libraryModules = ModuleName.LIBRARY_MODULES

    assertThat(libraryModules).doesNotContain(ModuleName.APP)
  }

  @Test
  fun testModuleConfig_createWithAllParameters_setsAllProperties() {
    val aarFileInfo = AarFileInfo("original/path", "extracted/path")
    val moduleConfig = ModuleConfig(
      name = "test-module",
      isAndroid = true,
      isLibrary = false,
      isTest = true,
      srcFiles = listOf("src/main/java/Test.kt"),
      testFiles = listOf("src/test/java/TestTest.kt"),
      resourceDirs = listOf("src/main/res"),
      manifestFile = "src/main/AndroidManifest.xml",
      dependencies = listOf("dependency1", "dependency2"),
      aarFiles = listOf(aarFileInfo),
      jarFiles = listOf("test.jar"),
      lintCheckJars = listOf("lint-check.jar"),
      lintModelDir = workingDirectory
    )

    assertThat(moduleConfig.name).isEqualTo("test-module")
    assertThat(moduleConfig.isAndroid).isTrue()
    assertThat(moduleConfig.isLibrary).isFalse()
    assertThat(moduleConfig.isTest).isTrue()
    assertThat(moduleConfig.srcFiles).containsExactly("src/main/java/Test.kt")
    assertThat(moduleConfig.testFiles).containsExactly("src/test/java/TestTest.kt")
    assertThat(moduleConfig.resourceDirs).containsExactly("src/main/res")
    assertThat(moduleConfig.manifestFile).isEqualTo("src/main/AndroidManifest.xml")
    assertThat(moduleConfig.dependencies).containsExactly("dependency1", "dependency2")
    assertThat(moduleConfig.aarFiles).containsExactly(aarFileInfo)
    assertThat(moduleConfig.jarFiles).containsExactly("test.jar")
    assertThat(moduleConfig.lintCheckJars).containsExactly("lint-check.jar")
    assertThat(moduleConfig.lintModelDir).isEqualTo(workingDirectory)
  }

  @Test
  fun testModuleConfig_createWithDefaultLintModelDir_hasNullLintModelDir() {
    val moduleConfig = ModuleConfig(
      name = "test-module",
      isAndroid = true,
      isLibrary = false,
      isTest = false,
      srcFiles = emptyList(),
      testFiles = emptyList(),
      resourceDirs = emptyList(),
      manifestFile = "",
      dependencies = emptyList(),
      aarFiles = emptyList(),
      jarFiles = emptyList(),
      lintCheckJars = emptyList()
    )

    assertThat(moduleConfig.lintModelDir).isNull()
  }

  @Test
  fun testModuleConfig_createLibraryModule_hasCorrectLibraryFlag() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    assertThat(moduleConfig.isLibrary).isTrue()
    assertThat(moduleConfig.name).isEqualTo("utility")
  }

  @Test
  fun testModuleConfig_createAppModule_hasCorrectLibraryFlag() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    assertThat(moduleConfig.isLibrary).isFalse()
    assertThat(moduleConfig.name).isEqualTo("app")
  }

  @Test
  fun testAarFileInfo_createWithPaths_setsCorrectPaths() {
    val aarFileInfo = AarFileInfo(
      originalPath = "/original/path/library.aar",
      extractedPath = "/extracted/path/library"
    )

    assertThat(aarFileInfo.originalPath).isEqualTo("/original/path/library.aar")
    assertThat(aarFileInfo.extractedPath).isEqualTo("/extracted/path/library")
  }

  @Test
  fun testAarFileInfo_createMultipleInstances_maintainsIndependentPaths() {
    val aarFileInfo1 = AarFileInfo("path1/lib1.aar", "extracted1/lib1")
    val aarFileInfo2 = AarFileInfo("path2/lib2.aar", "extracted2/lib2")

    assertThat(aarFileInfo1.originalPath).isEqualTo("path1/lib1.aar")
    assertThat(aarFileInfo1.extractedPath).isEqualTo("extracted1/lib1")
    assertThat(aarFileInfo2.originalPath).isEqualTo("path2/lib2.aar")
    assertThat(aarFileInfo2.extractedPath).isEqualTo("extracted2/lib2")
  }

  @Test
  fun testLintLogger_logError_createsLogFile() {
    val lintLogger = LintLogger(workingDirectory)

    lintLogger.logError("Test error message")

    val logFile = File(workingDirectory, "error-logs")
    assertThat(logFile.exists()).isTrue()
  }

  @Test
  fun testLintLogger_logError_writesMessageWithTimestamp() {
    val lintLogger = LintLogger(workingDirectory)

    lintLogger.logError("Test error message")

    val logFile = File(workingDirectory, "error-logs")
    val content = logFile.readText()
    assertThat(content).contains("Test error message")
    assertThat(content).contains("[")
    assertThat(content).contains("]")
  }

  @Test
  fun testLintLogger_logMultipleErrors_appendsToSameFile() {
    val lintLogger = LintLogger(workingDirectory)

    lintLogger.logError("First error")
    lintLogger.logError("Second error")

    val logFile = File(workingDirectory, "error-logs")
    val content = logFile.readText()
    assertThat(content).contains("First error")
    assertThat(content).contains("Second error")

    val lines = content.lines().filter { it.isNotEmpty() }
    assertThat(lines).hasSize(2)
  }

  @Test
  fun testJavaConfiguration_createWithValidBazelInfo_setsCorrectProperties() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11",
      "java-runtime" to "OpenJDK Runtime Environment (build 11.0.16+8-post)"
    )

    val javaConfiguration = JavaConfiguration(bazelInfo)

    assertThat(javaConfiguration.getJdkHome()).isEqualTo(File("/usr/lib/jvm/java-11"))
    assertThat(javaConfiguration.getVersion()).isEqualTo("11.0.16")
  }

  @Test
  fun testJavaConfiguration_createWithDifferentJavaVersion_extractsCorrectVersion() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-17",
      "java-runtime" to "OpenJDK Runtime Environment (build 17.0.2+8-Ubuntu)"
    )

    val javaConfiguration = JavaConfiguration(bazelInfo)

    assertThat(javaConfiguration.getJdkHome()).isEqualTo(File("/usr/lib/jvm/java-17"))
    assertThat(javaConfiguration.getVersion()).isEqualTo("17.0.2")
  }

  @Test
  fun testJavaConfiguration_createWithMissingJavaHome_throwsError() {
    val bazelInfo = mapOf(
      "java-runtime" to "OpenJDK Runtime Environment (build 11.0.16+8-post)"
    )

    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }
    assertThat(exception.message).contains("java-home not found in bazel info output")
  }

  @Test
  fun testJavaConfiguration_createWithMissingJavaRuntime_throwsError() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11"
    )

    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }
    assertThat(exception.message).contains("java-runtime not found in bazel info output")
  }

  @Test
  fun testJavaConfiguration_createWithInvalidVersionFormat_throwsError() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11",
      "java-runtime" to "Invalid runtime string without version"
    )

    val exception = assertThrows<IllegalStateException> {
      JavaConfiguration(bazelInfo)
    }
    assertThat(exception.message).contains("Could not extract Java version from:")
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
