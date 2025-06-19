package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.testing.TestBazelWorkspace
import java.io.File
import java.util.concurrent.TimeUnit

/** Tests for [LintProjectDescription]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class LintProjectDescriptionTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelClient: BazelClient
  private lateinit var lintProjectDescription: LintProjectDescription
  private lateinit var workingDirectory: File
  private lateinit var validProjectDescription: File

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    workingDirectory = tempFolder.newFolder("working-directory")
    lintProjectDescription = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = workingDirectory,
      bazelClient = bazelClient
    )

    setupProjectStructure()
    validProjectDescription = lintProjectDescription.generateProjectDescriptionXml()
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()

    // Set up rules_jvm_external for external dependencies
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf(
        "androidx.annotation:annotation:1.1.0",
        "com.android.tools.lint:lint-api:30.0.0",
        "junit:junit:4.12"
      )
    )

    setupUtilityModule()
    setupDomainModule()
    setupTestingModule()
    setupDataModule()
    setupAppModule()
  }

  private fun setupAppModule() {
    // Create app module structure
    createModuleDirectories("app")

    // Create AndroidManifest.xml for app module
    val appManifest = tempFolder.newFile("app/src/main/AndroidManifest.xml")
    appManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <application android:label="@string/app_name">
              <activity android:name=".MainActivity">
                  <intent-filter>
                      <action android:name="android.intent.action.MAIN" />
                      <category android:name="android.intent.category.LAUNCHER" />
                  </intent-filter>
              </activity>
          </application>
      </manifest>
      """.trimIndent()
    )

    // Create a sample source file in app module
    val appSourceFile = tempFolder.newFile("app/src/main/java/MainActivity.kt")
    appSourceFile.writeText(
      """
      package org.oppia.android
      
      import android.app.Activity
      import android.os.Bundle
      
      class MainActivity : Activity() {
          override fun onCreate(savedInstanceState: Bundle?) {
              super.onCreate(savedInstanceState)
          }
      }
      """.trimIndent()
    )

    // Create a shared test file for app module
    val appSharedTestFile = tempFolder.newFile("app/src/sharedTest/java/MainActivityTest.kt")
    appSharedTestFile.writeText(
      """
      package org.oppia.android
      
      import org.junit.Test
      import org.junit.Assert.assertNotNull
      
      class MainActivityTest {
          @Test
          fun testMainActivityCreation() {
              val activity = MainActivity()
              assertNotNull(activity)
          }
      }
      """.trimIndent()
    )

    // Create resources for app module
    val appResValues = tempFolder.newFile("app/src/main/res/values/strings.xml")
    appResValues.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia Android</string>
      </resources>
      """.trimIndent()
    )

    createAppModuleBuildFile()
  }

  private fun setupDomainModule() {
    createModuleDirectories("domain")

    val domainManifest = tempFolder.newFile("domain/src/main/AndroidManifest.xml")
    domainManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.domain" />
      """.trimIndent()
    )

    val domainSourceFile = tempFolder.newFile("domain/src/main/java/DomainController.kt")
    domainSourceFile.writeText(
      """
      package org.oppia.android.domain
      
      import org.oppia.android.util.logging.ConsoleLogger
      
      class DomainController(private val logger: ConsoleLogger) {
          fun processData(data: String): String {
              logger.d("Processing data: ${'$'}data")
              return data.uppercase()
          }
      }
      """.trimIndent()
    )

    val domainTestFile = tempFolder.newFile("domain/src/test/java/DomainControllerTest.kt")
    domainTestFile.writeText(
      """
      package org.oppia.android.domain
      
      import org.junit.Test
      import org.junit.Assert.assertEquals
      import org.oppia.android.util.logging.ConsoleLogger
      
      class DomainControllerTest {
          @Test
          fun testProcessData() {
              val logger = ConsoleLogger()
              val controller = DomainController(logger)
              assertEquals("HELLO", controller.processData("hello"))
          }
      }
      """.trimIndent()
    )

    createLibraryModuleBuildFile("domain", listOf("//utility:utility_lib"))
  }

  private fun setupTestingModule() {
    createModuleDirectories("testing")

    val testingManifest = tempFolder.newFile("testing/src/main/AndroidManifest.xml")
    testingManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.testing" />
      """.trimIndent()
    )

    val testingSourceFile = tempFolder.newFile("testing/src/main/java/TestHelper.kt")
    testingSourceFile.writeText(
      """
      package org.oppia.android.testing
      
      import org.oppia.android.util.logging.ConsoleLogger
      import org.oppia.android.domain.DomainController
      
      class TestHelper {
          fun createTestDomainController(): DomainController {
              return DomainController(ConsoleLogger())
          }
      }
      """.trimIndent()
    )

    val testingTestFile = tempFolder.newFile("testing/src/test/java/TestHelperTest.kt")
    testingTestFile.writeText(
      """
      package org.oppia.android.testing
      
      import org.junit.Test
      import org.junit.Assert.assertNotNull
      
      class TestHelperTest {
          @Test
          fun testCreateTestDomainController() {
              val helper = TestHelper()
              assertNotNull(helper.createTestDomainController())
          }
      }
      """.trimIndent()
    )

    createLibraryModuleBuildFile("testing", listOf("//utility:utility_lib", "//domain:domain_lib"))
  }

  private fun setupUtilityModule() {
    createModuleDirectories("utility")

    val utilityManifest = tempFolder.newFile("utility/src/main/AndroidManifest.xml")
    utilityManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.util" />
      """.trimIndent()
    )

    val utilitySourceFile = tempFolder.newFile("utility/src/main/java/logging/ConsoleLogger.kt")
    utilitySourceFile.writeText(
      """
      package org.oppia.android.util.logging
      
      class ConsoleLogger {
          fun d(message: String) {
              println("DEBUG: ${'$'}message")
          }
          
          fun e(message: String) {
              println("ERROR: ${'$'}message")
          }
      }
      """.trimIndent()
    )

    val utilityTestFile = tempFolder.newFile("utility/src/test/java/logging/ConsoleLoggerTest.kt")
    utilityTestFile.writeText(
      """
      package org.oppia.android.util.logging
      
      import org.junit.Test
      import org.junit.Assert.assertNotNull
      
      class ConsoleLoggerTest {
          @Test
          fun testLoggerCreation() {
              val logger = ConsoleLogger()
              assertNotNull(logger)
              logger.d("Test message")
          }
      }
      """.trimIndent()
    )

    // Create resources for utility module
    val utilityResValues = tempFolder.newFile("utility/src/main/res/values/colors.xml")
    utilityResValues.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <color name="primary_blue">#1976D2</color>
      </resources>
      """.trimIndent()
    )

    val utilityTestResValues = tempFolder.newFile("utility/src/test/res/values/test_colors.xml")
    utilityTestResValues.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <color name="test_red">#F44336</color>
      </resources>
      """.trimIndent()
    )

    createLibraryModuleBuildFile("utility", emptyList())
  }

  private fun setupDataModule() {
    createModuleDirectories("data")

    val dataManifest = tempFolder.newFile("data/src/main/AndroidManifest.xml")
    dataManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.data" />
      """.trimIndent()
    )

    val dataSourceFile = tempFolder.newFile("data/src/main/java/DataRepository.kt")
    dataSourceFile.writeText(
      """
      package org.oppia.android.data
      
      import org.oppia.android.util.logging.ConsoleLogger
      
      class DataRepository(private val logger: ConsoleLogger) {
          fun fetchData(): List<String> {
              logger.d("Fetching data")
              return listOf("data1", "data2", "data3")
          }
      }
      """.trimIndent()
    )

    val dataTestFile = tempFolder.newFile("data/src/test/java/DataRepositoryTest.kt")
    dataTestFile.writeText(
      """
      package org.oppia.android.data
      
      import org.junit.Test
      import org.junit.Assert.assertEquals
      import org.oppia.android.util.logging.ConsoleLogger
      
      class DataRepositoryTest {
          @Test
          fun testFetchData() {
              val logger = ConsoleLogger()
              val repository = DataRepository(logger)
              assertEquals(3, repository.fetchData().size)
          }
      }
      """.trimIndent()
    )

    createLibraryModuleBuildFile("data", listOf("//utility:utility_lib"))
  }

  private fun createModuleDirectories(moduleName: String) {
    // Main source directories
    tempFolder.newFolder(moduleName, "src", "main", "java")
    tempFolder.newFolder(moduleName, "src", "main", "res", "values")

    // Test directories
    tempFolder.newFolder(moduleName, "src", "test", "java")

    // Additional directory for app module
    if (moduleName == "app") {
      tempFolder.newFolder(moduleName, "src", "sharedTest", "java")
    }

    if (moduleName == "utility") {
      tempFolder.newFolder(moduleName, "src", "test", "res", "values")
      tempFolder.newFolder(moduleName, "src", "main", "java", "logging")
      tempFolder.newFolder(moduleName, "src", "test", "java", "logging")
    }
  }

  private fun createAppModuleBuildFile() {
    val appBuildFile = tempFolder.newFile("app/BUILD.bazel")
    appBuildFile.writeText(
      """
      load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
      load("@rules_jvm_external//:defs.bzl", "artifact")
      
      kt_jvm_library(
          name = "app_lib",
          srcs = glob(["src/main/java/**/*.kt"]),
          deps = [
              "//domain:domain_lib",
              "//testing:testing_lib",
              "//utility:utility_lib",
              "//data:data_lib",
              artifact("androidx.annotation:annotation"),
          ],
          visibility = ["//visibility:public"],
      )
      
      # Mock AAR dependencies for testing
      genrule(
          name = "mock_android_aar",
          outs = ["android_support.aar"],
          cmd = "echo 'mock aar content' > $@",
          visibility = ["//visibility:public"],
      )
      """.trimIndent()
    )
  }

  private fun createLibraryModuleBuildFile(moduleName: String, dependencies: List<String>) {
    val buildFile = tempFolder.newFile("$moduleName/BUILD.bazel")

    val depsSection = if (dependencies.isNotEmpty()) {
      """
          deps = [
              ${dependencies.joinToString(",\n              ") { "\"$it\"" }},
          ],"""
    } else {
      ""
    }

    buildFile.writeText(
      """
    load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
    load("@rules_jvm_external//:defs.bzl", "artifact")
    
    kt_jvm_library(
        name = "${moduleName}_lib",
        srcs = glob(["src/main/java/**/*.kt"]),$depsSection
        visibility = ["//visibility:public"],
    )
    
    kt_jvm_test(
        name = "${moduleName}_test",
        srcs = glob(["src/test/java/**/*.kt"]),
        deps = [
            ":${moduleName}_lib",
            artifact("junit:junit"),
        ],
        visibility = ["//visibility:public"],
    )
    
    # Mock JAR dependencies for testing
    genrule(
        name = "mock_${moduleName}_jar",
        outs = ["${moduleName}_support.jar"],
        cmd = "echo 'mock jar content' > $@",
        visibility = ["//visibility:public"],
    )
      """.trimIndent()
    )
  }

  @Test
  fun testGenerateProjectDescriptionXml_validProject_generatesCorrectXml() {
    val result = validProjectDescription

    assertThat(result.exists()).isTrue()
    assertThat(result.name).isEqualTo("lint-project-description.xml")

    val xmlContent = result.readText()

    // Verify basic XML structure
    assertThat(xmlContent).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    assertThat(xmlContent).contains("<project>")
    assertThat(xmlContent).contains("</project>")

    // Verify root directory is set correctly
    assertThat(xmlContent).contains("<root dir='${tempFolder.root.absolutePath}'/>")

    // Verify cache directory is created and referenced
    assertThat(xmlContent).contains("<cache dir='")
    val cacheDir = File(workingDirectory, "lint-cache-directory")
    assertThat(cacheDir.exists()).isTrue()
  }

  private fun extractModuleSection(xmlContent: String, moduleName: String): String {
    val startPattern = Regex("""<module[^>]*name="$moduleName"""")
    val startMatch = startPattern.find(xmlContent) ?: return ""

    val startIndex = startMatch.range.first
    val endIndex = xmlContent.indexOf("</module>", startIndex)
    if (endIndex == -1) return ""

    return xmlContent.substring(startIndex, endIndex + "</module>".length)
  }

  private inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
    try {
      block()
      throw AssertionError(
        "Expected ${T::class.simpleName} to be thrown"
      )
    } catch (e: Throwable) {
      if (e is T) {
        return e
      } else {
        throw AssertionError(
          "Expected ${T::class.simpleName} but got ${e::class.simpleName}: ${e.message}"
        )
      }
    }
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
