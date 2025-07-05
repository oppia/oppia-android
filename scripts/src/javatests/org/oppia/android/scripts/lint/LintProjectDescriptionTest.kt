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
  private lateinit var lintProjectDescriptionWithFakeExecutor: LintProjectDescription
  private lateinit var workingDirectory: File
  private lateinit var bazelBinFolder: File

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    workingDirectory = tempFolder.newFolder("working-directory")
    bazelBinFolder = tempFolder.newFolder("bazel-bin")

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
    createModule("domain")
    createModule("testing")
    createModule("data")
  }

  private fun createModule(moduleName: String) {
    createModuleDirectories(moduleName)
    createModuleFiles(moduleName)
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
      "$moduleName/src/test/java",
      "$moduleName/src/sharedTest",
      "$moduleName/src/sharedTest/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createModuleFiles(moduleName: String) {
    createManifestFile(moduleName)
    createSourceFile(moduleName)
    createTestFile(moduleName)
    createSharedTestFile(moduleName)
    createResourceFile(moduleName)
    createAdditionalSourceFiles(moduleName)
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

  private fun createSharedTestFile(moduleName: String) {
    val className = moduleName.capitalize()
    val sharedTestFile
    = tempFolder.newFile("$moduleName/src/sharedTest/java/${className}SharedTest.kt")
    sharedTestFile.writeText(
      """
      package org.oppia.android.$moduleName
      
      import org.junit.Test
      import org.junit.Assert.assertNotNull
      
      class ${className}SharedTest {
          @Test
          fun testSharedFunctionality() {
              val instance = ${className}Class()
              assertNotNull(instance)
          }
      }
      """.trimIndent()
    )
  }

  private fun createAdditionalSourceFiles(moduleName: String) {
    val javaFile = tempFolder
      .newFile("$moduleName/src/main/java/${moduleName.capitalize()}Helper.java")
    javaFile.writeText(
      """
      package org.oppia.android.$moduleName;
      
      public class ${moduleName.capitalize()}Helper {
          public static String getModuleName() {
              return "$moduleName";
          }
      }
      """.trimIndent()
    )

    val utilFile = tempFolder.newFile("$moduleName/src/main/java/${moduleName.capitalize()}Util.kt")
    utilFile.writeText(
      """
      package org.oppia.android.$moduleName
      
      object ${moduleName.capitalize()}Util {
          const val MODULE_NAME = "$moduleName"
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
          <string name="${moduleName}_description">Description for $moduleName</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun createProguardFiles() {
    val proguardDir = tempFolder.newFolder("config", "proguard")

    val proguardFile1 = File(proguardDir, "proguard-rules.pro")
    proguardFile1.writeText(
      """
      -keep class org.oppia.android.** { *; }
      -dontwarn javax.annotation.**
      """.trimIndent()
    )

    val proguardFile2 = File(proguardDir, "consumer-rules.pro")
    proguardFile2.writeText(
      """
      -keep class org.oppia.android.app.** { *; }
      """.trimIndent()
    )
  }

  private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
  }

  @Test
  fun testGenerateProjectDescriptionXml_basicGeneration() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()

    assertThat(result.exists()).isTrue()
    assertThat(result.name).isEqualTo("lint-project-description.xml")

    val xmlContent = result.readText()
    assertThat(xmlContent).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    assertThat(xmlContent).contains("<project>")
    assertThat(xmlContent).contains("</project>")
  }

  @Test
  fun generateProjectDescriptionXml_outputsWellFormedXml() {
    setupFakeCommandExecutor()

    val xmlFile = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
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
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val expectedRootDir = "<root dir='${tempFolder.root.absolutePath}'/>"
    assertThat(xmlContent).contains(expectedRootDir)
    assertThat(tempFolder.root.exists()).isTrue()
    assertThat(tempFolder.root.isDirectory).isTrue()
  }

  @Test
  fun testGenerateProjectDescriptionXml_createsCacheDirectory() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val cacheDirectory = File(workingDirectory, "lint-cache-directory")
    assertThat(cacheDirectory.exists()).isTrue()
    assertThat(cacheDirectory.isDirectory).isTrue()
    assertThat(xmlContent).contains("<cache dir='${cacheDirectory.absolutePath}'/>")
  }

  @Test
  fun testGenerateProjectDescriptionXml_containsModules() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val expectedModules = listOf("app", "utility", "domain", "testing", "data")
    expectedModules.forEach { moduleName ->
      assertThat(xmlContent).contains("name=\"$moduleName\"")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_createsRequiredDirectories() {
    setupFakeCommandExecutor()

    lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()

    val requiredDirectories = listOf(
      File(workingDirectory, "lint-cache-directory"),
      File(workingDirectory, "extracted-aars"),
      File(workingDirectory, "models-directory"),
      File(workingDirectory, "partial-results-directory")
    )

    requiredDirectories.forEach { directory ->
      assertThat(directory.exists()).isTrue()
      assertThat(directory.isDirectory).isTrue()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_createsModuleSpecificDirectories() {
    setupFakeCommandExecutor()

    lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()

    val expectedModules = listOf("app", "utility", "domain", "testing", "data")
    expectedModules.forEach { moduleName ->
      val moduleModelDir =
        File(workingDirectory, "models-directory/$moduleName")
      val modulePartialResultsDir =
        File(workingDirectory, "partial-results-directory/$moduleName-partial-results")

      assertThat(moduleModelDir.exists()).isTrue()
      assertThat(modulePartialResultsDir.exists()).isTrue()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingManifest_throwsException() {
    val appManifest = File(tempFolder.root, "app/src/main/AndroidManifest.xml")
    appManifest.delete()
    setupFakeCommandExecutor()

    val exception = assertThrows<IllegalArgumentException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception.message).contains("Manifest file not found")
  }

  @Test
  fun testGenerateProjectDescriptionXml_libraryModulesMarkedCorrectly() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // App module should be library="false"
    assertThat(xmlContent).contains("name=\"app\"")
    val appModulePattern = Regex("""name="app"[^>]*library="false"""")
    assertThat(appModulePattern.find(xmlContent)).isNotNull()

    // Library modules should be library="true"
    val libraryModules = listOf("utility", "domain", "testing", "data")
    libraryModules.forEach { moduleName ->
      assertThat(xmlContent).contains("name=\"$moduleName\"")
    }

    val libraryTrueCount = xmlContent.split("library=\"true\"").size - 1
    assertThat(libraryTrueCount).isEqualTo(4)
  }

  @Test
  fun testGenerateProjectDescriptionXml_testModuleMarkedCorrectly() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val testModulePattern = Regex("""name="testing"[^>]*test="true"""")
    assertThat(testModulePattern.find(xmlContent)).isNotNull()

    val nonTestModules = listOf("app", "utility", "domain", "data")
    nonTestModules.forEach { moduleName ->
      val nonTestModulePattern = Regex("""name="$moduleName"[^>]*test="false"""")
      assertThat(nonTestModulePattern.find(xmlContent)).isNotNull()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_fileOverwrite() {
    setupFakeCommandExecutor()
    val result1 = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val content1 = result1.readText()

    val result2 = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
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
      commandExecutor = fakeCommandExecutor
    )

    setupFakeCommandExecutor()
    val result = newLintProjectDescription.generateProjectDescriptionXml()

    assertThat(newWorkingDir.exists()).isTrue()
    assertThat(result.exists()).isTrue()
    assertThat(result.parentFile).isEqualTo(newWorkingDir)
  }

  @Test
  fun testGenerateProjectDescriptionXml_sourceFilePathsAreAbsolute() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
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
  fun testGenerateProjectDescriptionXml_separatesTestAndSourceFiles() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val testFilePattern = Regex("""src file="([^"]+)" test="true"""")
    val testFiles = testFilePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    val srcFilePattern = Regex("""src file="([^"]+)"(?! test="true")""")
    val srcFiles = srcFilePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(testFiles).isNotEmpty()
    assertThat(srcFiles).isNotEmpty()

    testFiles.forEach { path ->
      assertThat(path.contains("/test/") || path.contains("/sharedTest/")).isTrue()
    }

    srcFiles.forEach { path ->
      assertThat(path.contains("/test/") || path.contains("/sharedTest/")).isFalse()
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_resourceDirectoryPathsAreValid() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
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
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<aar file=")
    assertThat(xmlContent).contains("extracted=")

    val extractedAarsDir = File(workingDirectory, "extracted-aars")
    assertThat(extractedAarsDir.exists()).isTrue()
  }

  @Test
  fun testGenerateProjectDescriptionXml_withJarDependencies() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<classpath jar=")

    val classpathPattern = Regex("""classpath jar="([^"]+)"""")
    val jarPaths = classpathPattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(jarPaths).isNotEmpty()
    jarPaths.forEach { path ->
      assertThat(File(path).exists()).isTrue()
      assertThat(path).endsWith(".jar")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_withProguardFiles() {
    createProguardFiles()
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<proguard file=")

    val proguardPattern = Regex("""proguard file="([^"]+)"""")
    val proguardPaths = proguardPattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(proguardPaths).isNotEmpty()
    proguardPaths.forEach { path ->
      assertThat(File(path).exists()).isTrue()
      assertThat(path).endsWith(".pro")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_moduleDependencies() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<dep module=\"utility\"/>")
    assertThat(xmlContent).contains("<dep module=\"domain\"/>")
    assertThat(xmlContent).contains("<dep module=\"data\"/>")

    val testingModuleContent = extractModuleContent(xmlContent, "testing")
    assertThat(testingModuleContent).contains("<dep module=\"utility\"/>")
    assertThat(testingModuleContent).contains("<dep module=\"domain\"/>")

    val domainModuleContent = extractModuleContent(xmlContent, "domain")
    assertThat(domainModuleContent).contains("<dep module=\"utility\"/>")

    val dataModuleContent = extractModuleContent(xmlContent, "data")
    assertThat(dataModuleContent).contains("<dep module=\"utility\"/>")
  }

  @Test
  fun testGenerateProjectDescriptionXml_manifestFilePathsAreValid() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val manifestPattern = Regex("""manifest file="([^"]+)"""")
    val manifestPaths = manifestPattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    assertThat(manifestPaths).hasSize(5)
    manifestPaths.forEach { path ->
      assertThat(File(path).exists()).isTrue()
      assertThat(path).endsWith("AndroidManifest.xml")
    }
  }

  @Test
  fun testGenerateProjectDescriptionXml_moduleAttributesAreCorrect() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val androidTrueCount = xmlContent.split("android=\"true\"").size - 1
    assertThat(androidTrueCount).isEqualTo(5)

    val desugarFullCount = xmlContent.split("desugar=\"full\"").size - 1
    assertThat(desugarFullCount).isEqualTo(5)

    val modelCount = xmlContent.split("model=\"").size - 1
    assertThat(modelCount).isEqualTo(5)

    val partialResultsCount = xmlContent.split("partial-results=\"").size - 1
    assertThat(partialResultsCount).isEqualTo(5)
  }

  @Test
  fun testGenerateProjectDescriptionXml_handlesMultipleSourceFiles() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // Check that we have multiple source files per module
    val srcFilePattern = Regex("""src file="([^"]+)"(?! test="true")""")
    val srcFiles = srcFilePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    // We should have at least 15 source files (3 per module * 5 modules)
    assertThat(srcFiles.size).isAtLeast(15)

    // Verify we have both .kt and .java files
    val ktFiles = srcFiles.filter { it.endsWith(".kt") }
    val javaFiles = srcFiles.filter { it.endsWith(".java") }

    assertThat(ktFiles).isNotEmpty()
    assertThat(javaFiles).isNotEmpty()
  }

  @Test
  fun testGenerateProjectDescriptionXml_errorHandling_invalidWorkingDirectory() {
    val invalidWorkingDir = File("/invalid/path/that/does/not/exist")
    val lintProjectDescription = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = invalidWorkingDir,
      commandExecutor = fakeCommandExecutor
    )

    setupFakeCommandExecutor()

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescription.generateProjectDescriptionXml()
    }

    assertThat(exception.message).contains("Failed to create directory")
  }

  @Test
  fun testGenerateProjectDescriptionXml_emptyModuleHandling() {
    tempFolder.newFolder("empty-module")
    val emptyModuleSrcDir = tempFolder.newFolder("empty-module", "src", "main")
    val emptyModuleManifest = File(emptyModuleSrcDir, "AndroidManifest.xml")
    emptyModuleManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.empty" />
      """.trimIndent()
    )

    setupFakeCommandExecutor()

    // Should not throw an exception even with empty modules
    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    assertThat(result.exists()).isTrue()
  }

  private fun extractModuleContent(xmlContent: String, moduleName: String): String {
    val moduleStart = xmlContent.indexOf("name=\"$moduleName\"")
    if (moduleStart == -1) return ""

    val moduleEnd = xmlContent.indexOf("</module>", moduleStart)
    if (moduleEnd == -1) return ""

    return xmlContent.substring(moduleStart, moduleEnd)
  }

  private fun setupFakeCommandExecutor() {
    val aarPath = createTestAarFile("test-library", "1.0.0").absolutePath
    val jarPath = createTestJarFile("test-library", "1.0.0").absolutePath

    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.contains("deps(//app:*)") -> {
          val relativeAarPath = File(aarPath).relativeTo(tempFolder.root).path
          outputStream.println(relativeAarPath)
          val relativeJarPath = File(jarPath).relativeTo(tempFolder.root).path
          outputStream.println(relativeJarPath)
          0
        }
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          // Return some dependencies for other modules
          outputStream.println("external/junit/junit-4.12.jar")
          outputStream.println("external/hamcrest/hamcrest-core-1.3.jar")
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
    val aarFile = File(bazelBinFolder, "$libraryName-$version.aar")

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

      // Add lint.jar (for lint check testing)
      val lintEntry = ZipEntry("lint.jar")
      zipOut.putNextEntry(lintEntry)
      zipOut.write(ByteArray(10))
      zipOut.closeEntry()

      // Add annotations.zip (for annotation testing)
      val annotationsEntry = ZipEntry("annotations.zip")
      zipOut.putNextEntry(annotationsEntry)
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

  private fun createTestJarFile(libraryName: String, version: String): File {
    val jarFile = File(bazelBinFolder, "$libraryName-$version.jar")

    ZipOutputStream(FileOutputStream(jarFile)).use { zipOut ->
      // Add a dummy class file
      val classEntry = ZipEntry("com/example/$libraryName/TestClass.class")
      zipOut.putNextEntry(classEntry)
      zipOut.write(ByteArray(50)) // Dummy class file content
      zipOut.closeEntry()

      // Add META-INF/MANIFEST.MF
      val manifestEntry = ZipEntry("META-INF/MANIFEST.MF")
      zipOut.putNextEntry(manifestEntry)
      zipOut.write(
        """
        Manifest-Version: 1.0
        Implementation-Title: $libraryName
        Implementation-Version: $version
        
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()

      // Add some dummy annotation files
      val annotationEntry = ZipEntry("META-INF/annotations.xml")
      zipOut.putNextEntry(annotationEntry)
      zipOut.write(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <root>
          <item name="com.example.$libraryName.TestClass">
            <annotation name="androidx.annotation.NonNull" />
          </item>
        </root>
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()
    }

    return jarFile
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher,
      processTimeout = 5,
      processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
