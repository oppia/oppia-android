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
  fun testGenerateProjectDescriptionXml_containsLayers() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val expectedLayers = listOf("app", "utility", "domain", "testing", "data")
    expectedLayers.forEach { layerName ->
      assertThat(xmlContent).contains("name=\"$layerName\"")
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
  fun testGenerateProjectDescriptionXml_createsLayerSpecificDirectories() {
    setupFakeCommandExecutor()

    lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()

    val expectedLayers = listOf("app", "utility", "domain", "testing", "data")
    expectedLayers.forEach { layerName ->
      val layerModelDir = File(workingDirectory, "models-directory/$layerName")
      val layerPartialResultsDir =
        File(workingDirectory, "partial-results-directory/$layerName-partial-results")

      assertThat(layerModelDir.exists()).isTrue()
      assertThat(layerPartialResultsDir.exists()).isTrue()
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
  fun testGenerateProjectDescriptionXml_libraryLayersMarkedCorrectly() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // App layer should be library="false"
    assertThat(xmlContent).contains("name=\"app\"")
    val appLayerPattern = Regex("""name="app"[^>]*library="false"""")
    assertThat(appLayerPattern.find(xmlContent)).isNotNull()

    // Library layers should be library="true"
    val libraryLayers = listOf("utility", "domain", "testing", "data")
    libraryLayers.forEach { layerName ->
      assertThat(xmlContent).contains("name=\"$layerName\"")
    }

    val libraryTrueCount = xmlContent.split("library=\"true\"").size - 1
    assertThat(libraryTrueCount).isEqualTo(4)
  }

  @Test
  fun testGenerateProjectDescriptionXml_testLayerMarkedCorrectly() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val testLayerPattern = Regex("""name="testing"[^>]*test="true"""")
    assertThat(testLayerPattern.find(xmlContent)).isNotNull()

    val nonTestLayers = listOf("app", "utility", "domain", "data")
    nonTestLayers.forEach { layerName ->
      val nonTestLayerPattern = Regex("""name="$layerName"[^>]*test="false"""")
      assertThat(nonTestLayerPattern.find(xmlContent)).isNotNull()
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
  fun testGenerateProjectDescriptionXml_hasLayerDependencies() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("<dep module=\"utility\"/>")
    assertThat(xmlContent).contains("<dep module=\"domain\"/>")
    assertThat(xmlContent).contains("<dep module=\"data\"/>")

    val testingLayerContent = extractModuleContent(xmlContent, "testing")
    assertThat(testingLayerContent).contains("<dep module=\"utility\"/>")
    assertThat(testingLayerContent).contains("<dep module=\"domain\"/>")

    val domainLayerContent = extractModuleContent(xmlContent, "domain")
    assertThat(domainLayerContent).contains("<dep module=\"utility\"/>")

    val dataLayerContent = extractModuleContent(xmlContent, "data")
    assertThat(dataLayerContent).contains("<dep module=\"utility\"/>")
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
  fun testGenerateProjectDescriptionXml_layerAttributesAreCorrect() {
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

    // Check that we have multiple source files per layer
    val srcFilePattern = Regex("""src file="([^"]+)"(?! test="true")""")
    val srcFiles = srcFilePattern.findAll(xmlContent).map { it.groupValues[1] }.toList()

    // We should have at least 15 source files (3 per layer * 5 layers)
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
  fun testGenerateProjectDescriptionXml_emptyLayerHandling() {
    tempFolder.newFolder("empty-layer")
    val emptyLayerSrcDir = tempFolder.newFolder("empty-layer", "src", "main")
    val emptyLayerManifest = File(emptyLayerSrcDir, "AndroidManifest.xml")
    emptyLayerManifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.empty" />
      """.trimIndent()
    )

    setupFakeCommandExecutor()

    // Should not throw an exception even with empty layers
    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    assertThat(result.exists()).isTrue()
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
  fun testBuildAllLayerConfigurations_returnsCorrectNumberOfLayers() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    val layerCount = xmlContent.split("<module").size - 1
    assertThat(layerCount).isEqualTo(5)

    val expectedLayers = listOf("app", "utility", "domain", "testing", "data")
    expectedLayers.forEach { layerName ->
      assertThat(xmlContent).contains("name=\"$layerName\"")
    }
  }

  @Test
  fun testBuildAllLayerConfigurations_setsCorrectLibraryFlags() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    assertThat(xmlContent).contains("name=\"app\"")
    assertThat(xmlContent).contains("library=\"false\"")

    val libraryTrueCount = xmlContent.split("library=\"true\"").size - 1
    assertThat(libraryTrueCount).isEqualTo(4) // utility, domain, testing, data
  }

  @Test
  fun testBuildAllLayerConfigurations_includesResourceDirectories() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // Verify resource directories are included
    val resourceDirCount = xmlContent.split("<resource dir=").size - 1
    assertThat(resourceDirCount).isAtLeast(5) // At least one per layer

    // Verify resource directories contain "res" in path
    val resourcePattern = Regex("""<resource dir="[^"]*res[^"]*"/>""")
    val resourceMatches = resourcePattern.findAll(xmlContent).count()
    assertThat(resourceMatches).isEqualTo(resourceDirCount)
  }

  @Test
  fun testBuildAllLayerConfigurations_includesManifestFiles() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // Verify each layer has a manifest file
    val manifestCount = xmlContent.split("<manifest file=").size - 1
    assertThat(manifestCount).isEqualTo(5) // One per layer

    // Verify manifest files point to AndroidManifest.xml
    val manifestPattern = Regex("""<manifest file="[^"]*AndroidManifest\.xml"/>""")
    val manifestMatches = manifestPattern.findAll(xmlContent).count()
    assertThat(manifestMatches).isEqualTo(5)
  }

  @Test
  fun testBuildAllLayerConfigurations_setsAndroidFlag() {
    setupFakeCommandExecutor()

    val result = lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    val xmlContent = result.readText()

    // All layers should have android="true"
    val androidTrueCount = xmlContent.split("android=\"true\"").size - 1
    assertThat(androidTrueCount).isEqualTo(5) // All layers are Android code

    // No layers should have android="false"
    assertThat(xmlContent).doesNotContain("android=\"false\"")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingDeclaredJar_failsFastWithActionableError() {
    registerCqueryResults(appDeclaredPaths = listOf("external/missing/missing-1.0.jar"))

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("Android Lint cannot run")
    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat().contains("external/missing/missing-1.0.jar")
    assertThat(exception).hasMessageThat()
      .contains(File(tempFolder.root, "bazel-out/external/missing/missing-1.0.jar").absolutePath)
    assertThat(exception).hasMessageThat().contains("needed by layer(s): app)")
    assertThat(exception).hasMessageThat().contains("Build the Bazel targets")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingDeclaredAar_failsFastWithActionableError() {
    val missingAar = "bazel-out/k8-fastbuild/bin/external/maven/missing/missing-1.0.aar"
    registerCqueryResults(appDeclaredPaths = listOf(missingAar))

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("Android Lint cannot run")
    assertThat(exception).hasMessageThat().contains(missingAar)
    assertThat(exception).hasMessageThat().contains("needed by layer(s): app)")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingDeclaredDependency_doesNotWriteProjectDescription() {
    registerCqueryResults(appDeclaredPaths = listOf("external/missing/missing-1.0.jar"))

    assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(File(workingDirectory, "lint-project-description.xml").exists()).isFalse()
  }

  @Test
  fun testGenerateProjectDescriptionXml_someDeclaredDependenciesMissing_reportsOnlyMissingOnes() {
    createExternalJar("external/present/present-1.0.jar")
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "external/present/present-1.0.jar",
        "external/absent/absent-1.0.jar"
      )
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    // A partial classpath must never be silently accepted.
    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat().contains("external/absent/absent-1.0.jar")
    assertThat(exception).hasMessageThat().doesNotContain("present-1.0.jar")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingDependencyDeclaredByAllLayers_listsOnceWithLayers() {
    registerCqueryResults(
      appDeclaredPaths = listOf("external/shared/shared-1.0.jar"),
      otherLayerDeclaredPaths = listOf("external/shared/shared-1.0.jar")
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat()
      .contains("needed by layer(s): app, domain, testing, utility, data)")
  }

  @Test
  fun testGenerateProjectDescriptionXml_manyMissingDependencies_truncatesReportedList() {
    registerCqueryResults(
      appDeclaredPaths = (1..30).map { "external/many/lib-$it.jar" }
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("30 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat().contains("... and 5 more.")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingNonArchiveOutputs_doNotFailFast() {
    // Only AAR/JAR outputs are classpath entries; other declared outputs (e.g. sources) are not
    // verified here.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "app/src/main/java/org/oppia/android/app/Missing.kt",
        "bazel-out/k8-fastbuild/bin/app/missing_kt-kapt-gensrc.srcjar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingCompileAvoidanceJars_doNotFailFast() {
    // Compile-avoidance / annotation-processing JARs are build-system intermediates that are
    // never placed on the Lint classpath; they may not exist even in a warm build.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/app/app_kt-hjar.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-java-hjar.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt.abi.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-native-header.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-kapt-generated-stub.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-gensrc.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-kapt-gensrc.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt-java-gensrc.jar"
      )
    )

    // None of these paths exist on disk; a broad check would throw IllegalStateException. The
    // narrowed check must succeed because compile-avoidance JARs are never Lint classpath entries.
    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingExecConfigJars_doNotFailFast() {
    // Outputs built in a Bazel exec configuration (k8-opt-exec-*, -exec-/) are build tools,
    // not application code, and are never on the Lint classpath.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-opt-exec-2B5CBBC6/bin/external/rules_java/toolchains/platformclasspath.jar",
        "bazel-out/k8-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_kotlin/kotlin/compiler.jar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingBuildToolJars_doNotFailFast() {
    // JARs from Bazel build-rule packages (rules_jvm_external, rules_java,
    // io_bazel_rules_kotlin, remote_java_tools) are build-system internals that Lint never needs.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "external/rules_jvm_external/coursier/cli.jar",
        "external/rules_java/toolchains/java_toolchain.jar",
        "external/io_bazel_rules_kotlin/kotlin/kotlin-stdlib.jar",
        "external/remote_java_tools/java_tools.jar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingInternalModuleAar_doesNotFailFast() {
    // Internal module .aar outputs declared by kt_android_library are not external AAR
    // dependencies and are not consumed by Lint (which models layers via <dep module="..."/>).
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/app/app.aar",
        "bazel-out/k8-fastbuild/bin/domain/domain.aar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<aar file=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingIntermediateAarJar_doesNotFailFast() {
    // Intermediate AAR extraction outputs (classes_and_libs_merged.jar) are redundant with the
    // retained AAR (which already exposes equivalent classes to Lint via its extracted directory).
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/external/maven/androidx/appcompat/appcompat/1.4.0/" +
          "_aar/appcompat/classes_and_libs_merged.jar",
        "bazel-out/k8-fastbuild/bin/external/maven/androidx/core/core/1.7.0/" +
          "_aar/core/classes.jar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingExternalAarResourcesJar_doesNotFailFast() {
    // External AAR synthetic resource JARs are not needed since Lint extracts the AAR's res/ dir.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/external/maven/androidx/appcompat/appcompat/1.4.0/" +
          "appcompat-1.4.0_resources.jar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingTestRunnerBinaryJar_doesNotFailFast() {
    // Test runner executable JARs (_test.jar, _deploy.jar) are terminal targets, not classpath
    // JARs.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/app/src_test_java_org_oppia_android_app_ActivityTest_test.jar",
        "bazel-out/k8-fastbuild/bin/app/src_test_java_org_oppia_android_app_ActivityTest_deploy.jar"
      )
    )

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    assertThat(xmlContent).doesNotContain("<classpath jar=")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingInternalModuleJar_failsFast() {
    // Internal compiled module JARs (e.g. domain_kt.jar) are required classpath entries,
    // especially for incremental Lint runs where unchanged source files are omitted from the
    // source model.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/domain/domain_kt.jar"
      )
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("Android Lint cannot run")
    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat()
      .contains("bazel-out/k8-fastbuild/bin/domain/domain_kt.jar")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingKaptGeneratedClassJar_failsFast() {
    // KAPT-generated class JARs (containing Dagger factories/components) are required classpath
    // entries.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        "bazel-out/k8-fastbuild/bin/app/app_kt-kapt-generated-class.jar"
      )
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("Android Lint cannot run")
    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat()
      .contains("bazel-out/k8-fastbuild/bin/app/app_kt-kapt-generated-class.jar")
  }

  @Test
  fun testGenerateProjectDescriptionXml_missingLintClasspathJar_stillFailsFast() {
    // A regular application JAR (e.g. a generated proto JAR) is a Lint classpath entry and must
    // trigger a fail-fast when it is missing, even if compile-avoidance JARs are also declared.
    registerCqueryResults(
      appDeclaredPaths = listOf(
        // These compile-avoidance JARs are excluded and must not trigger the check.
        "bazel-out/k8-fastbuild/bin/app/app_kt-hjar.jar",
        "bazel-out/k8-fastbuild/bin/app/app_kt.abi.jar",
        // This is a genuine Lint classpath entry that is missing.
        "bazel-out/k8-fastbuild/bin/model/src/main/proto/libmath_proto-lite.jar"
      )
    )

    val exception = assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    assertThat(exception).hasMessageThat().contains("Android Lint cannot run")
    assertThat(exception).hasMessageThat().contains("1 AAR/JAR artifact(s)")
    assertThat(exception).hasMessageThat()
      .contains("bazel-out/k8-fastbuild/bin/model/src/main/proto/libmath_proto-lite.jar")
    // The compile-avoidance JARs must not appear in the error message.
    assertThat(exception).hasMessageThat().doesNotContain("app_kt-hjar.jar")
    assertThat(exception).hasMessageThat().doesNotContain("app_kt.abi.jar")
  }

  @Test
  fun testGenerateProjectDescriptionXml_allDeclaredDependenciesPresent_includesEachOnClasspath() {
    val generatedProtoJar =
      "bazel-out/k8-fastbuild/bin/model/src/main/proto/libgenerated_proto-lite.jar"
    val declaredJars = listOf(
      "external/lib_a/lib-a-1.0.jar",
      "external/lib_b/lib-b-2.0.jar",
      generatedProtoJar
    )
    createExternalJar("external/lib_a/lib-a-1.0.jar")
    createExternalJar("external/lib_b/lib-b-2.0.jar")
    // Generated outputs resolve against the repository root via the bazel-out symlink.
    createJarAtRepoRelativePath(generatedProtoJar)
    registerCqueryResults(appDeclaredPaths = declaredJars)

    val xmlContent =
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml().readText()

    // Compare the actual resolved entries, not just their count.
    val classpathJars = Regex("""classpath jar="([^"]+)"""").findAll(xmlContent)
      .map { it.groupValues[1] }.toList()
    assertThat(classpathJars).containsExactly(
      File(tempFolder.root, "bazel-out/external/lib_a/lib-a-1.0.jar").absolutePath,
      File(tempFolder.root, "bazel-out/external/lib_b/lib-b-2.0.jar").absolutePath,
      File(
        tempFolder.root,
        "bazel-out/k8-fastbuild/bin/model/src/main/proto/libgenerated_proto-lite.jar"
      ).absolutePath
    )
  }

  @Test
  fun testGenerateProjectDescriptionXml_dependencyBuiltAfterFailedRun_laterRunSucceeds() {
    val declaredJar = "external/lib/lib-1.0.jar"
    registerCqueryResults(appDeclaredPaths = listOf(declaredJar))

    // Simulates a clean workspace: the declared artifact has not been built yet.
    assertThrows<IllegalStateException> {
      lintProjectDescriptionWithFakeExecutor.generateProjectDescriptionXml()
    }

    // Simulates a subsequent warm workspace: the artifact now exists. A fresh run (as happens for
    // each script invocation) must resolve it without relying on any state from the failed run.
    createExternalJar(declaredJar)
    val warmDescription = LintProjectDescription(
      repoRoot = tempFolder.root,
      workingDirectory = tempFolder.newFolder("warm-working-directory"),
      commandExecutor = fakeCommandExecutor
    )
    val xmlContent = warmDescription.generateProjectDescriptionXml().readText()

    val expectedJar = File(tempFolder.root, "bazel-out/$declaredJar").absolutePath
    assertThat(xmlContent).contains("<classpath jar=\"$expectedJar\"/>")
  }

  /**
   * Registers a fake Bazel that reports the specified declared dependency paths for `cquery`:
   * [appDeclaredPaths] for the app layer and [otherLayerDeclaredPaths] for every other layer.
   */
  private fun registerCqueryResults(
    appDeclaredPaths: List<String>,
    otherLayerDeclaredPaths: List<String> = listOf()
  ) {
    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.contains("deps(//app:*)") -> {
          appDeclaredPaths.forEach { outputStream.println(it) }
          0
        }
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          otherLayerDeclaredPaths.forEach { outputStream.println(it) }
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

  /**
   * Creates an empty-but-valid JAR at [declaredPath] resolved against the faked Bazel output base
   * (which is how 'external/...' paths are resolved).
   */
  private fun createExternalJar(declaredPath: String): File =
    createJar(File(File(tempFolder.root, "bazel-out"), declaredPath))

  /** Creates an empty-but-valid JAR at [relativePath] resolved against the repository root. */
  private fun createJarAtRepoRelativePath(relativePath: String): File =
    createJar(File(tempFolder.root, relativePath))

  private fun createJar(jarFile: File): File {
    jarFile.parentFile?.mkdirs()
    ZipOutputStream(FileOutputStream(jarFile)).use { zipOut ->
      zipOut.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
      zipOut.write("Manifest-Version: 1.0\n".toByteArray())
      zipOut.closeEntry()
    }
    return jarFile
  }

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(listOf("junit:junit:4.12"))

    // Create all required layers
    createLayer("app")
    createLayer("utility")
    createLayer("domain")
    createLayer("testing")
    createLayer("data")
  }

  private fun createLayer(layerName: String) {
    createLayerDirectories(layerName)
    createLayerFiles(layerName)
  }

  private fun createLayerDirectories(layerName: String) {
    val directories = listOf(
      layerName,
      "$layerName/src",
      "$layerName/src/main",
      "$layerName/src/main/java",
      "$layerName/src/main/res",
      "$layerName/src/main/res/values",
      "$layerName/src/test",
      "$layerName/src/test/java",
      "$layerName/src/sharedTest",
      "$layerName/src/sharedTest/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createLayerFiles(layerName: String) {
    createManifestFile(layerName)
    createSourceFile(layerName)
    createTestFile(layerName)
    createSharedTestFile(layerName)
    createResourceFile(layerName)
    createAdditionalSourceFiles(layerName)
  }

  private fun createManifestFile(layerName: String) {
    val manifest = tempFolder.newFile("$layerName/src/main/AndroidManifest.xml")
    manifest.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="org.oppia.android.$layerName" />
      """.trimIndent()
    )
  }

  private fun createSourceFile(layerName: String) {
    val className = layerName.capitalize()
    val sourceFile = tempFolder.newFile("$layerName/src/main/java/${className}Class.kt")
    sourceFile.writeText(
      """
      package org.oppia.android.$layerName

      class ${className}Class {
          fun doSomething(): String = "Hello from $layerName"
      }
      """.trimIndent()
    )
  }

  private fun createTestFile(layerName: String) {
    val className = layerName.capitalize()
    val testFile = tempFolder.newFile("$layerName/src/test/java/${className}ClassTest.kt")
    testFile.writeText(
      """
      package org.oppia.android.$layerName

      import org.junit.Test
      import org.junit.Assert.assertEquals

      class ${className}ClassTest {
          @Test
          fun testDoSomething() {
              val instance = ${className}Class()
              assertEquals("Hello from $layerName", instance.doSomething())
          }
      }
      """.trimIndent()
    )
  }

  private fun createSharedTestFile(layerName: String) {
    val className = layerName.capitalize()
    val sharedTestFile =
      tempFolder.newFile("$layerName/src/sharedTest/java/${className}SharedTest.kt")
    sharedTestFile.writeText(
      """
      package org.oppia.android.$layerName

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

  private fun createAdditionalSourceFiles(layerName: String) {
    val javaFile = tempFolder
      .newFile("$layerName/src/main/java/${layerName.capitalize()}Helper.java")
    javaFile.writeText(
      """
      package org.oppia.android.$layerName;

      public class ${layerName.capitalize()}Helper {
          public static String getLayerName() {
              return "$layerName";
          }
      }
      """.trimIndent()
    )

    val utilFile = tempFolder.newFile("$layerName/src/main/java/${layerName.capitalize()}Util.kt")
    utilFile.writeText(
      """
      package org.oppia.android.$layerName

      object ${layerName.capitalize()}Util {
          const val LAYER_NAME = "$layerName"
      }
      """.trimIndent()
    )
  }

  private fun createResourceFile(layerName: String) {
    val resourceFile = tempFolder.newFile("$layerName/src/main/res/values/strings.xml")
    resourceFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="${layerName}_name">$layerName Layer</string>
          <string name="${layerName}_description">Description for $layerName</string>
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

  private fun extractModuleContent(xmlContent: String, layerName: String): String {
    val moduleStart = xmlContent.indexOf("name=\"$layerName\"")
    if (moduleStart == -1) return ""

    val moduleEnd = xmlContent.indexOf("</module>", moduleStart)
    if (moduleEnd == -1) return ""

    return xmlContent.substring(moduleStart, moduleEnd)
  }

  private fun setupFakeCommandExecutor() {
    val aarPath = createTestAarFile("test-library", "1.0.0").absolutePath
    val jarPath = createTestJarFile("test-library", "1.0.0").absolutePath
    // Declared dependencies must physically exist, otherwise lint fails fast (see
    // LintProjectDescription), so ensure the external JARs reported below are present.
    createExternalJar("external/junit/junit-4.12.jar")
    createExternalJar("external/hamcrest/hamcrest-core-1.3.jar")

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
          // Return some dependencies for other Bazel targets
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
