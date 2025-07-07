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
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import org.xml.sax.SAXException
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Comprehensive tests for [LintProjectDescription]. */
@Suppress("FunctionName")
class LintProjectDescriptionTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelClient: BazelClient
  private lateinit var lintProjectDescription: LintProjectDescription
  private lateinit var lintProjectDescriptionWithFakeExecutor: LintProjectDescription
  private lateinit var workingDirectory: File

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    workingDirectory = tempFolder.newFolder("working-directory")
    lintProjectDescription = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = workingDirectory,
      commandExecutor = longCommandExecutor
    )
    lintProjectDescriptionWithFakeExecutor = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = workingDirectory,
      commandExecutor = fakeCommandExecutor
    )

    setupProjectStructure()
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(listOf("junit:junit:4.12"))

    // Create all required modules
    createModule("app")
    createModule("utility")
    createModule(
      "domain",
      dependencies = listOf("//utility:utility_lib")
    )
    createModule(
      "testing",
      dependencies = listOf("//utility:utility_lib", "//domain:domain_lib")
    )
    createModule("data", dependencies = listOf("//utility:utility_lib"))
  }

  private fun createModule(
    moduleName: String,
    dependencies: List<String> = emptyList()
  ) {
    createModuleDirectories(moduleName)
    createModuleFiles(moduleName)
    createModuleBuildFile(moduleName, dependencies)
  }

  private fun createModuleDirectories(moduleName: String) {
    val directories = listOf(
      moduleName,
      "$moduleName/src",
      "$moduleName/src/main",
      "$moduleName/src/main/java",
      "$moduleName/src/main/res",
      "$moduleName/src/main/res/values",
      "$moduleName/src/test",
      "$moduleName/src/test/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createModuleFiles(moduleName: String) {
    createManifestFile(moduleName)
    createSourceFile(moduleName)
    createTestFile(moduleName)
    createResourceFile(moduleName)
  }

  private fun createManifestFile(moduleName: String) {
    val manifest = tempFolder.newFile("$moduleName/src/main/AndroidManifest.xml")
    manifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.$moduleName" />
      """.trimIndent()
    )
  }

  private fun createSourceFile(moduleName: String) {
    val className = moduleName.capitalize()
    val sourceFile = tempFolder.newFile("$moduleName/src/main/java/${className}Class.kt")
    sourceFile.writeText(
      """
      package org.oppia.android.$moduleName
      
      class ${className}Class {
          fun doSomething(): String = "Hello from $moduleName"
      }
      """.trimIndent()
    )
  }

  private fun createTestFile(moduleName: String) {
    val className = moduleName.capitalize()
    val testFile = tempFolder.newFile("$moduleName/src/test/java/${className}ClassTest.kt")
    testFile.writeText(
      """
      package org.oppia.android.$moduleName
      
      import org.junit.Test
      import org.junit.Assert.assertEquals
      
      class ${className}ClassTest {
          @Test
          fun testDoSomething() {
              val instance = ${className}Class()
              assertEquals("Hello from $moduleName", instance.doSomething())
          }
      }
      """.trimIndent()
    )
  }

  private fun createResourceFile(moduleName: String) {
    val resourceFile = tempFolder.newFile("$moduleName/src/main/res/values/strings.xml")
    resourceFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="${moduleName}_name">$moduleName Module</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun createModuleBuildFile(
    moduleName: String,
    dependencies: List<String>
  ) {
    val buildFile = tempFolder.newFile("$moduleName/BUILD.bazel")
    val depsSection = createDependenciesSection(dependencies)
    val sources = "src/main/java/"
    val testSources = "src/test/java/"
    val extension = "/*.kt"
    buildFile.writeText(
      """
      load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")
      load("@rules_jvm_external//:defs.bzl", "artifact")
      
      kt_jvm_library(
          name = "${moduleName}_lib",
          srcs = glob(["$sources**$extension"]),$depsSection
          visibility = ["//visibility:public"],
      )
      
      kt_jvm_test(
          name = "${moduleName}_test",
          srcs = glob(["$testSources**$extension"]),
          deps = [
              ":${moduleName}_lib",
              artifact("junit:junit"),
          ],
          visibility = ["//visibility:public"],
      )
      """.trimIndent()
    )
  }

  private fun createDependenciesSection(dependencies: List<String>): String {
    return if (dependencies.isNotEmpty()) {
      val formattedDeps = dependencies.joinToString(",\n              ") { "\"$it\"" }
      """
          deps = [
              $formattedDeps,
          ],"""
    } else {
      ""
    }
  }

  private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
  }

  @Test
  fun testGenerateProjectDescriptionXml_generatesBasicXmlFile() {
    val xmlFile = lintProjectDescription.generateProjectDescriptionXml()

    assertThat(xmlFile.exists()).isTrue()
    assertThat(xmlFile.name).isEqualTo("lint-project-description.xml")

    val xmlContent = xmlFile.readText()

    assertThat(xmlContent).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    assertThat(xmlContent).contains("<project>")
    assertThat(xmlContent).contains("</project>")
  }

  @Test
  fun generateProjectDescriptionXml_outputsWellFormedXml() {
    val xmlFile = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = xmlFile.readText()

    val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val parsingException = try {
      parser.parse(xmlFile.inputStream())
      null
    } catch (e: SAXException) {
      e
    }

    assertThat(parsingException).isNull()
    assertThat(xmlContent).apply {
      startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
      contains("<project>")
      contains("<module")
      contains("<src file=")
      contains("<resource dir=")
      contains("<classpath jar=")
      contains("<dep module=")
      contains("<manifest file=")
      contains("</module>")
      endsWith("</project>\n")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_includesCorrectRootDirectory() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val expectedRootDir = "<root dir='${tempFolder.root.absolutePath}'/>"
    assertThat(xmlContent).contains(expectedRootDir)
    assertThat(tempFolder.root.exists()).isTrue()
    assertThat(tempFolder.root.isDirectory).isTrue()
  }

  @Test
  fun testGenerateProjectDescriptionXml_createsCacheDirectory() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val cacheDirectory = File(workingDirectory, "lint-cache-directory")
    assertThat(cacheDirectory.exists()).isTrue()
    assertThat(cacheDirectory.isDirectory).isTrue()
    assertThat(xmlContent).contains("<cache dir='${cacheDirectory.absolutePath}'/>")
  }

  @Test
  fun testGenerateProjectDescriptionXml_containsModules() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val expectedModules = listOf("app", "utility", "domain", "testing", "data")
    expectedModules.forEach { moduleName ->
      assertThat(xmlContent).contains("name=\"$moduleName\"")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_createsRequiredDirectories() {
    lintProjectDescription.generateProjectDescriptionXml()

    val requiredDirectories = listOf(
      File(workingDirectory, "lint-cache-directory"),
      File(workingDirectory, "extracted-aars")
    )

    requiredDirectories.forEach { directory ->
      assertThat(directory.exists()).isTrue()
      assertThat(directory.isDirectory).isTrue()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingManifest_throwsException() {
    val appManifest = File(tempFolder.root, "app/src/main/AndroidManifest.xml")
    appManifest.delete()

    val exception = assertThrows<IllegalArgumentException> {
      lintProjectDescription.generateProjectDescriptionXml()
    }

    assertThat(exception.message).contains("Manifest file not found")
  }

  @Test
  fun testGenerateProjectDescriptionXml_libraryModulesMarkedCorrectly() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val libraryModules = listOf("utility", "domain", "testing", "data")
    libraryModules.forEach { moduleName ->
      assertThat(xmlContent).contains("name=\"$moduleName\"")
    }

    val libraryTrueCount = xmlContent.split("library=\"true\"").size - 1
    assertThat(libraryTrueCount).isEqualTo(4)
  }

  @Test
  fun testGenerateProjectDescriptionXml_fileOverwrite() {
    val result1 = lintProjectDescription.generateProjectDescriptionXml()
    val content1 = result1.readText()

    val result2 = lintProjectDescription.generateProjectDescriptionXml()
    val content2 = result2.readText()

    assertThat(result1.absolutePath).isEqualTo(result2.absolutePath)
    assertThat(content1).isEqualTo(content2)
    assertThat(result2.exists()).isTrue()
  }

  @Test
  fun testGenerateProjectDescriptionXml_workingDirectoryCreation() {
    workingDirectory.deleteRecursively()
    assertThat(workingDirectory.exists()).isFalse()

    val newWorkingDir = File(tempFolder.root, "new-working-dir")
    val newLintProjectDescription = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = newWorkingDir,
      commandExecutor = longCommandExecutor
    )

    val result = newLintProjectDescription.generateProjectDescriptionXml()

    assertThat(newWorkingDir.exists()).isTrue()
    assertThat(result.exists()).isTrue()
    assertThat(result.parentFile).isEqualTo(newWorkingDir)
  }

  @Test
  fun testGenerateProjectDescriptionXml_sourceFilePathsAreAbsolute() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val srcFilePattern = Regex("""src file="([^"]+)"""")
    val srcFiles = srcFilePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(srcFiles).isNotEmpty()
    srcFiles.forEach { path ->
      assertThat(File(path).isAbsolute).isTrue()
      assertThat(File(path).exists()).isTrue()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_resourceDirectoryPathsAreValid() {
    val result = lintProjectDescription.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val resourcePattern = Regex("""resource dir="([^"]+)"""")
    val resourceDirs = resourcePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(resourceDirs).isNotEmpty()
    resourceDirs.forEach { path ->
      assertThat(File(path).exists()).isTrue()
      assertThat(File(path).isDirectory).isTrue()
      assertThat(path).contains("res")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_withAarDependencies() {
    // Create a fake AAR file
    val aarFile = createTestAarFile("test-library", "1.0.0")

    setupFakeCommandExecutorForAarDependencies(aarFile.absolutePath)

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<aar file=")
    assertThat(xmlContent).contains("extracted=")

    // Verify the AAR was extracted
    val extractedAarsDir = File(workingDirectory, "extracted-aars")
    assertThat(extractedAarsDir.exists()).isTrue()
  }

  @Test
  fun testCacheManager_getDependencies_cachesResults() {
    val cacheManager = CacheManager()
    var callCount = 0

    val provider = {
      callCount++
      listOf("dependency1", "dependency2")
    }

    // First call should invoke provider
    val result1 = cacheManager.getDependencies("test-key", provider)
    assertThat(callCount).isEqualTo(1)
    assertThat(result1).containsExactly("dependency1", "dependency2")

    // Second call should use cache
    val result2 = cacheManager.getDependencies("test-key", provider)
    assertThat(callCount).isEqualTo(1) // Provider not called again
    assertThat(result2).containsExactly("dependency1", "dependency2")
  }

  @Test
  fun testCacheManager_getPathResolution_cachesResults() {
    val cacheManager = CacheManager()
    var callCount = 0

    val provider = {
      callCount++
      "/resolved/path"
    }

    // First call should invoke provider
    val result1 = cacheManager.getPathResolution("test-path", provider)
    assertThat(callCount).isEqualTo(1)
    assertThat(result1).isEqualTo("/resolved/path")

    // Second call should use cache
    val result2 = cacheManager.getPathResolution("test-path", provider)
    assertThat(callCount).isEqualTo(1) // Provider not called again
    assertThat(result2).isEqualTo("/resolved/path")
  }

  @Test
  fun testCacheManager_getAarExtraction_cachesResults() {
    val cacheManager = CacheManager()
    var callCount = 0

    val provider = {
      callCount++
      "/extracted/aar/path"
    }

    // First call should invoke provider
    val result1 = cacheManager.getAarExtraction("test-aar", provider)
    assertThat(callCount).isEqualTo(1)
    assertThat(result1).isEqualTo("/extracted/aar/path")

    // Second call should use cache
    val result2 = cacheManager.getAarExtraction("test-aar", provider)
    assertThat(callCount).isEqualTo(1) // Provider not called again
    assertThat(result2).isEqualTo("/extracted/aar/path")
  }

  @Test
  fun testCacheManager_differentKeys_separateEntries() {
    val cacheManager = CacheManager()
    var callCount = 0

    val provider = {
      callCount++
      listOf("dependency-$callCount")
    }

    val result1 = cacheManager.getDependencies("key1", provider)
    val result2 = cacheManager.getDependencies("key2", provider)

    assertThat(callCount).isEqualTo(2)
    assertThat(result1).containsExactly("dependency-1")
    assertThat(result2).containsExactly("dependency-2")
  }

  @Test
  fun testCacheManager_nullPathResolution_cached() {
    val cacheManager = CacheManager()
    var callCount = 0

    val provider = {
      callCount++
      null
    }

    val result1 = cacheManager.getPathResolution("non-existent-path", provider)
    val result2 = cacheManager.getPathResolution("non-existent-path", provider)

    assertThat(callCount).isEqualTo(1)
    assertThat(result1).isNull()
    assertThat(result2).isNull()
  }

  @Test
  fun testCacheManager_memoryCleanup_performsAggressiveCleanup() {
    val cacheManager = CacheManager()

    // Fill cache with many entries to trigger cleanup
    repeat(1000) { index ->
      cacheManager.getDependencies("key-$index") {
        // Create large list to consume memory
        List(1000) { "dependency-$index-$it" }
      }
    }

    val result = cacheManager.getDependencies("final-key") {
      listOf("final-dependency")
    }

    assertThat(result).containsExactly("final-dependency")
    // Test passes if no OutOfMemoryError is thrown
  }

  @Test
  fun testCacheManager_mixedCacheTypes_workIndependently() {
    val cacheManager = CacheManager()

    val dependencies = cacheManager.getDependencies("test-key") {
      listOf("dep1", "dep2")
    }

    val pathResolution = cacheManager.getPathResolution("test-key") {
      "/some/path"
    }

    val aarExtraction = cacheManager.getAarExtraction("test-key") {
      "/extracted/path"
    }

    assertThat(dependencies).containsExactly("dep1", "dep2")
    assertThat(pathResolution).isEqualTo("/some/path")
    assertThat(aarExtraction).isEqualTo("/extracted/path")
  }

  @Test
  fun testCacheManager_expiredEntries_cleanupProperly() {
    val cacheManager = CacheManager()

    repeat(10) { index ->
      cacheManager.getDependencies("expired-key-$index") {
        listOf("dependency-$index")
      }
    }

    repeat(5) { index ->
      cacheManager.getDependencies("new-key-$index") {
        listOf("new-dependency-$index")
      }
    }

    val result = cacheManager.getDependencies("test-key") {
      listOf("test-dependency")
    }

    assertThat(result).containsExactly("test-dependency")
  }

  @Test
  fun testBuildAllModuleConfigurations_returnsCorrectNumberOfModules() {
    setupFakeCommandExecutorForModules()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val moduleCount = xmlContent.split("<module").size - 1
    assertThat(moduleCount).isEqualTo(5)

    val expectedModules = listOf("app", "utility", "domain", "testing", "data")
    expectedModules.forEach { moduleName ->
      assertThat(xmlContent).contains("name=\"$moduleName\"")
    }
  }

  @Test
  fun testBuildAllModuleConfigurations_setsCorrectLibraryFlags() {
    setupFakeCommandExecutorForModules()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("name=\"app\"")
    assertThat(xmlContent).contains("library=\"false\"")

    val libraryTrueCount = xmlContent.split("library=\"true\"").size - 1
    assertThat(libraryTrueCount).isEqualTo(4) // utility, domain, testing, data
  }

  @Test
  fun testBuildAllModuleConfigurations_includesResourceDirectories() {
    setupFakeCommandExecutorForModules()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // Verify resource directories are included
    val resourceDirCount = xmlContent.split("<resource dir=").size - 1
    assertThat(resourceDirCount).isAtLeast(5) // At least one per module

    // Verify resource directories contain "res" in path
    val resourcePattern = Regex("""<resource dir="[^"]*res[^"]*"/>""")
    val resourceMatches = resourcePattern.findAll(xmlContent).count()
    assertThat(resourceMatches).isEqualTo(resourceDirCount)
  }

  @Test
  fun testBuildAllModuleConfigurations_includesManifestFiles() {
    setupFakeCommandExecutorForModules()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // Verify each module has a manifest file
    val manifestCount = xmlContent.split("<manifest file=").size - 1
    assertThat(manifestCount).isEqualTo(5) // One per module

    // Verify manifest files point to AndroidManifest.xml
    val manifestPattern = Regex("""<manifest file="[^"]*AndroidManifest\.xml"/>""")
    val manifestMatches = manifestPattern.findAll(xmlContent).count()
    assertThat(manifestMatches).isEqualTo(5)
  }

  @Test
  fun testBuildAllModuleConfigurations_setsAndroidFlag() {
    setupFakeCommandExecutorForModules()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // All modules should have android="true"
    val androidTrueCount = xmlContent.split("android=\"true\"").size - 1
    assertThat(androidTrueCount).isEqualTo(5) // All modules are Android modules

    // No modules should have android="false"
    assertThat(xmlContent).doesNotContain("android=\"false\"")
  }

  private fun setupFakeCommandExecutorForModules() {
    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          0
        }
        args.contains("info") -> {
          outputStream.println("output_base: ${tempFolder.root.absolutePath}/bazel-out")
          outputStream.println("java-home: /usr/lib/jvm/java-11")
          outputStream.println("java-runtime: OpenJDK Runtime Environment (build 11.0.16+8-post)")
          0
        }
        else -> 0
      }
    }
  }

  private fun setupFakeCommandExecutorForAarDependencies(aarPath: String) {
    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.contains("deps(//app:*)") -> {
          outputStream.println(aarPath)
          0
        }
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          // Return empty for other modules
          0
        }
        args.contains("info") -> {
          outputStream.println("output_base: ${tempFolder.root.absolutePath}/bazel-out")
          outputStream.println("java-home: /usr/lib/jvm/java-11")
          outputStream.println("java-runtime: OpenJDK Runtime Environment (build 11.0.16+8-post)")
          0
        }
        else -> 0
      }
    }
  }

  private fun createTestAarFile(libraryName: String, version: String): File {
    val aarFile = tempFolder.newFile("$libraryName-$version.aar")

    ZipOutputStream(FileOutputStream(aarFile)).use { zipOut ->
      // Add AndroidManifest.xml
      val manifestEntry = ZipEntry("AndroidManifest.xml")
      zipOut.putNextEntry(manifestEntry)
      zipOut.write(
        """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest package="com.example.$libraryName" />
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()

      // Add classes.jar
      val classesEntry = ZipEntry("classes.jar")
      zipOut.putNextEntry(classesEntry)
      zipOut.write(ByteArray(10))
      zipOut.closeEntry()

      // Add resources
      val resEntry = ZipEntry("res/values/strings.xml")
      zipOut.putNextEntry(resEntry)
      zipOut.write(
        """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="library_name">$libraryName</string>
        </resources>
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()
    }

    return aarFile
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher,
      processTimeout = 5,
      processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
