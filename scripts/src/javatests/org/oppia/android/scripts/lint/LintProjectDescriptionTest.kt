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
  private lateinit var lintModelCreator: LintModelCreator
  private lateinit var modelDirectory: File

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
    modelDirectory = tempFolder.newFolder("model-directory")
    lintModelCreator = LintModelCreator(
      modelDir = modelDirectory,
      repoRoot = tempFolder.root
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
  fun testGenerateProjectDescriptionXml_basicGeneration() {
    val result = lintProjectDescription.generateProjectDescriptionXml()

    assertThat(result.exists()).isTrue()
    assertThat(result.name).isEqualTo("lint-project-description.xml")

    val xmlContent = result.readText()
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

  @Test
  fun testLintModelCreator_appModule_hasCorrectType() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    val content = moduleXmlFile.readText()
    assertThat(content).contains("type=\"APP\"")
  }

  @Test
  fun testLintModelCreator_libraryModule_hasCorrectType() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val moduleXmlFile = File(modelDirectory, "module.xml")
    val content = moduleXmlFile.readText()
    assertThat(content).contains("type=\"LIBRARY\"")
  }

  @Test
  fun testLintModelCreator_extractsPackageFromManifest() {
    val moduleConfig = createTestModuleConfig("domain", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    assertThat(content).contains("package=\"org.oppia.android.domain\"")
  }

  @Test
  fun testLintModelCreator_includesManifestFile() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    val expectedManifestPath = File(tempFolder.root,
      "utility/src/main/AndroidManifest.xml").absolutePath
    assertThat(content).contains("manifest=\"$expectedManifestPath\"")
  }

  @Test
  fun testLintModelCreator_includesJavaDirectories() {
    val moduleConfig = createTestModuleConfig("data", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    val expectedJavaPath = File(tempFolder.root, "data/src/main/java").absolutePath
    assertThat(content).contains("javaDirectories=\"$expectedJavaPath\"")
  }

  @Test
  fun testLintModelCreator_includesResourceDirectories() {
    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    val expectedResPath = File(tempFolder.root, "app/src/main/res").absolutePath
    assertThat(content).contains("resDirectories=\"$expectedResPath\"")
  }

  @Test
  fun testLintModelCreator_includesTestSourceProviders() {
    val moduleConfig = createTestModuleConfig("testing", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    assertThat(content).contains("<testSourceProviders>")
    assertThat(content).contains("</testSourceProviders>")
  }

  @Test
  fun testLintModelCreator_handlesProguardFiles() {
    // Create proguard directory and files for app module
    val proguardDir = tempFolder.newFolder("config", "proguard")
    val proguardFile = File(proguardDir, "app-rules.pro")
    proguardFile.writeText("# Proguard rules")

    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    assertThat(content).contains("proguardFiles=")
    assertThat(content).contains("app-rules.pro")
  }

  @Test
  fun testLintModelCreator_generatesWellFormedXml() {
    val moduleConfig = createTestModuleConfig("utility", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val xmlFiles = listOf("module.xml", "main.xml",
      "main-mainArtifact-libraries.xml", "main-mainArtifact-dependencies.xml")

    xmlFiles.forEach { fileName ->
      val xmlFile = File(modelDirectory, fileName)
      assertThat(xmlFile.exists()).isTrue()

      val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      val parsingException = try {
        parser.parse(xmlFile.inputStream())
        null
      } catch (e: SAXException) {
        e
      }

      assertThat(parsingException).isNull()
    }
  }

  @Test
  fun testLintModelCreator_buildsAbsolutePaths() {
    val moduleConfig = createTestModuleConfig("domain", isLibrary = true)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()

    val manifestPattern = Regex("""manifest="([^"]+)"""")
    val javaPattern = Regex("""javaDirectories="([^"]+)"""")
    val resPattern = Regex("""resDirectories="([^"]+)"""")

    manifestPattern.find(content)?.let { match ->
      val manifestPath = match.groupValues[1]
      assertThat(File(manifestPath).isAbsolute).isTrue()
      assertThat(File(manifestPath).exists()).isTrue()
    }

    javaPattern.find(content)?.let { match ->
      val javaPath = match.groupValues[1]
      assertThat(File(javaPath).isAbsolute).isTrue()
      assertThat(File(javaPath).exists()).isTrue()
    }

    resPattern.find(content)?.let { match ->
      val resPath = match.groupValues[1]
      assertThat(File(resPath).isAbsolute).isTrue()
      assertThat(File(resPath).exists()).isTrue()
    }
  }

  @Test
  fun testLintModelCreator_handlesAssetsDirectory() {
    val assetsDir = tempFolder.newFolder("app", "src", "main", "assets")
    val assetFile = File(assetsDir, "test-asset.txt")
    assetFile.writeText("test content")

    val moduleConfig = createTestModuleConfig("app", isLibrary = false)

    lintModelCreator.generateModelFiles(moduleConfig)

    val variantXmlFile = File(modelDirectory, "main.xml")
    val content = variantXmlFile.readText()
    assertThat(content).contains("assetsDirectories=")
    assertThat(content).contains(assetsDir.absolutePath)
  }

  private fun createTestModuleConfig(moduleName: String, isLibrary: Boolean): ModuleConfig {
    val manifestPath = "${tempFolder.root}/$moduleName/src/main/AndroidManifest.xml"
    val resourceDirs = listOf("${tempFolder.root}/$moduleName/src/main/res")
    val srcFiles = listOf("${tempFolder.root}/$moduleName/src/main/java/${moduleName.capitalize()}Class.kt")
    val testFiles = listOf("${tempFolder.root}/$moduleName/src/test/java/${moduleName.capitalize()}ClassTest.kt")

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
