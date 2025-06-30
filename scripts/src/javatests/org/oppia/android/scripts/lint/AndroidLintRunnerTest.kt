package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Tests for [AndroidLintRunner]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class AndroidLintRunnerTest {
  @field:[Rule JvmField]
  var tempFolder = TemporaryFolder()

  private lateinit var outputStream: ByteArrayOutputStream
  private lateinit var originalOut: PrintStream
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var sdkPath: String
  private lateinit var jdkHome: File
  private lateinit var buildSdkVersion: String
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }
  private lateinit var androidLintAnalyzerWithFakeExecutor: AndroidLintAnalyzer
  private lateinit var workingDirectory: File

  companion object {
    private const val JAVA_VERSION = "11.0.6"
  }

  @Before
  fun setUp() {
    outputStream = ByteArrayOutputStream()
    originalOut = System.out
    sdkPath = System.getenv("ANDROID_HOME")
      ?: error("ANDROID_HOME environment variable is not set.")
    jdkHome = File(
      System.getProperty("java.home")
        ?: error("java.home system property is not set.")
    )
    System.setOut(PrintStream(outputStream))
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    buildSdkVersion = AndroidBuildSdkProperties().buildSdkVersion.toString()
    workingDirectory = File(tempFolder.root, "lint_analysis").apply { mkdirs() }
    androidLintAnalyzerWithFakeExecutor = AndroidLintAnalyzer(
      commandExecutor = fakeCommandExecutor,
      workingDirectory = workingDirectory,
      repoRoot = tempFolder.root,
    )
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testMain_noArguments_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      main()
    }

    assertThat(exception).hasMessageThat().contains(
      "<path_to_repository_root argument> is required: \$(pwd)"
    )
  }

  @Test
  fun testMain_nonExistentPath_throwsException() {
    val nonExistentPath = File(tempFolder.root, "nonexistent").absolutePath

    val exception = assertThrows<IllegalArgumentException> {
      main(nonExistentPath)
    }

    assertThat(exception).hasMessageThat().contains("Repository root path does not exist")
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesReports() {
    setupProjectStructure()
    val aarFile = createTestAarFile("test-aar", "1.0.0")
    val jarFile = createTestJarFile("test-jar", "1.0.0")

    setupFakeCommandExecutor(aarFile.absolutePath, jarFile.absolutePath)
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    val report = File(workingDirectory, "lint-report.xml")
    assertThat(report.exists()).isTrue()

    val projectDescription = File(workingDirectory, "lint-project-description.xml")
    assertThat(projectDescription.exists()).isTrue()
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesFilesInWorkingDirectory() {
    setupProjectStructure()
    val aarFile = createTestAarFile("test-aar", "1.0.0")
    val jarFile = createTestJarFile("test-jar", "1.0.0")

    setupFakeCommandExecutor(aarFile.absolutePath, jarFile.absolutePath)
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    val report = File(workingDirectory, "lint-report.xml")
    assertThat(report.exists()).isTrue()

    val projectDescription = File(workingDirectory, "lint-project-description.xml")
    assertThat(projectDescription.exists()).isTrue()
    val extractedAars = File(workingDirectory, "extracted-aars")
    val extractedAarFile = File("$extractedAars/app", "test-aar-1.0.0")
    assertThat(extractedAarFile.exists()).isTrue()
    val lintCacheDirectory = File(workingDirectory, "lint-cache-directory")
    assertThat(lintCacheDirectory.exists()).isTrue()
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesModelDirectory() {
    setupProjectStructure()

    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    val modelDirectory = File(workingDirectory, "models-directory/app")
    assertThat(modelDirectory.exists()).isTrue()

    val expectedFiles = listOf(
      File(modelDirectory, "main.xml"),
      File(modelDirectory, "module.xml"),
      File(modelDirectory, "main-mainArtifact-dependencies.xml"),
      File(modelDirectory, "main-mainArtifact-libraries.xml")
    )

    expectedFiles.forEach { file ->
      assertThat(file.exists()).isTrue()
    }
  }

  @Test
  fun testPrepareLintArguments_ensuresAllRequiredArgumentsArePresent() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val result = lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion)

    val expectedArguments = listOf(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--client-id", "cli",
      "--jdk-home", jdkHome.absolutePath,
      "--sdk-home", sdkPath,
      "--compile-sdk-version", buildSdkVersion,
      "--kotlin-language-level", "1.6",
      "--java-language-level", JAVA_VERSION,
      "--project", projectFile.absolutePath,
      "--xml", reportFile.absolutePath
    )

    assertThat(result.toList()).containsExactlyElementsIn(expectedArguments)
  }

  @Test
  fun testPrepareLintArguments_withCustomBuildSdkVersion_includesCorrectVersion() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)
    val customBuildSdk = "34"

    val result = lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, customBuildSdk)

    assertThat(result).asList().contains("--compile-sdk-version")
    val sdkVersionIndex = result.indexOf("--compile-sdk-version")
    assertThat(result[sdkVersionIndex + 1]).isEqualTo(customBuildSdk)
  }

  @Test
  fun testPrepareLintArguments_withExistingReleaseFile_doesNotOverwrite() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk_with_release")
    tempJdkDir.mkdirs()

    val releaseFile = File(tempJdkDir, "release")
    val originalContent = "ORIGINAL_CONTENT=test"
    releaseFile.writeText(originalContent)

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    lintRunner.prepareLintArguments(tempJdkDir, JAVA_VERSION, buildSdkVersion)

    assertThat(releaseFile.readText()).isEqualTo(originalContent)
  }

  @Test
  fun testPrepareLintArguments_generatesValidModulesString() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk_modules")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    lintRunner.prepareLintArguments(tempJdkDir, JAVA_VERSION, buildSdkVersion)

    val releaseFile = File(tempJdkDir, "release")
    assertThat(releaseFile.exists()).isTrue()

    val releaseContent = releaseFile.readText()
    assertThat(releaseContent).startsWith("MODULES=\"")
    assertThat(releaseContent).endsWith("\"")
    assertThat(releaseContent).contains("java.base") // Common module that should be present
  }

  @Test
  fun testRunLint_whenExitCodeIs0_shouldPassSuccessfully() {
    setupProjectStructure()
    val lintRunner = createLintRunner()
    lintRunner.runLint(lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion))

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
  }

  @Test
  fun testRunLint_whenExitCodeIs1_shouldFailScript() {
    setupProjectWithInvalidIdIssue()
    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion))
    }

    val reportFile = File(workingDirectory, "lint-report.xml")
    assertThat(reportFile.exists()).isTrue()
    assertThat(exception.message).contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    val output = outputStream.toString()
    assertThat(output).contains("InvalidId")
  }

  @Test
  fun testRunLint_withExitCode2_throwsException() {
    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(emptyArray())
    }

    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 2: Invalid usage of Lint command."
    )
  }

  @Test
  fun testRunLint_withExitCode3_throwsExceptionForFileOverwrite() {
    val outputDirectory = File(workingDirectory, "reports")
    outputDirectory.mkdirs()

    val reportPath = File(outputDirectory, "lint-report.xml")
    reportPath.createNewFile()
    reportPath.writeText("existing content")

    val disabledWrite = outputDirectory.setWritable(false)
    assertThat(disabledWrite).isTrue()

    val projectPath = createProjectDescriptionFile()
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion))
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 3")
    assertThat(exception.message).contains("Cannot overwrite existing file")

    outputDirectory.setWritable(true)
  }

  @Test
  fun testRunLint_withExitCode4_throwsException() {
    val reportPath = File(workingDirectory, "lint-report.xml")
    val projectPath = File(workingDirectory, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    // Won't happen in actual usage.
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--help"))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 4")
    assertThat(exception.message).contains("Help command invoked.")
  }

  @Test
  fun testRunLint_withExitCode5_throwsException() {
    val reportPath = File(workingDirectory, "lint-report.xml")
    val projectPath = File(workingDirectory, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testRunLint_withProjectDescription_withNonExistentFilePath_throwsInternalIssue() {
    setupProjectStructure()

    val projectDescriptionFile = createProjectDescriptionFileWithInvalidPath()
    val reportFile = File(workingDirectory, "lint-report.xml")
    val lintRunner = AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion))
    }

    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED WITH INTERNAL LINT ISSUES$RESET")
    assertThat(reportFile.exists()).isTrue()
    val report = reportFile.readText()
    assertThat(report).contains("LintError")
    assertThat(report).contains("app/src/main/nonexistent_java does not exist")
    assertThat(report).contains("line=\"8\"")
  }

  @Test
  fun testRunLint_withInvalidFlag_throwsException() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--InvalidFlag"))
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testPrepareLintArguments_withJdkEnvironmentSetup_createsReleaseFile() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    // Ensure no release file exists initially
    val releaseFile = File(tempJdkDir, "release")
    assertThat(releaseFile.exists()).isFalse()

    lintRunner.prepareLintArguments(tempJdkDir, JAVA_VERSION, buildSdkVersion)

    // Verify release file was created
    assertThat(releaseFile.exists()).isTrue()
    val releaseContent = releaseFile.readText()
    assertThat(releaseContent).contains("MODULES=")
  }

  @Test
  fun testPrepareLintArguments_withInvalidJdkHome_throwsException() {
    val nonExistentJdk = File(tempFolder.root, "nonexistent_jdk")
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalArgumentException> {
      lintRunner.prepareLintArguments(nonExistentJdk, JAVA_VERSION, buildSdkVersion)
    }

    assertThat(exception.message).contains("JDK home path does not exist or is not a directory")
  }

  @Test
  fun testAndroidLintAnalyzer_withDuplicateStringResources_detectsIssue() {
    setupProjectWithDuplicateStringIssue()
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("DuplicateStrings")
    assertThat(output)
      .contains("<string name=\"duplicate_value\">Same text</string>")
    assertThat(output).contains("Line: 5")
  }

  private fun createLintRunner(): AndroidLintRunner {
    val reportFile = File(workingDirectory, "lint-report.xml")
    val projectDescriptionFile = createProjectDescriptionFile()

    return AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile
    )
  }

  private fun setupProjectWithInvalidIdIssue() {
    setupProjectStructure()

    createFileWithContent(
      "app/src/main/res/layout/activity_main.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <TextView
              android:id="@+i/123invalid_id" 
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/app_name" />
      </LinearLayout>
      """
    )
  }

  private fun setupProjectWithDuplicateStringIssue() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/values/strings.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia</string>
          <string name="duplicate_value">Same text</string>
          <string name="another_duplicate">Same text</string>
      </resources>
      """
    )
  }

  private fun createProjectDescriptionFile(): File {
    val projectDescriptionFile = File(tempFolder.root, "lint-project-description.xml")
    val rootPath = tempFolder.root.absolutePath

    projectDescriptionFile.writeText(
      createMinimalProjectDescriptionContent(
        rootPath = rootPath,
        srcPath = "$rootPath/app/src/main/java"
      )
    )

    return projectDescriptionFile
  }

  private fun createProjectDescriptionFileWithInvalidPath(): File {
    val projectDescriptionFile = File(tempFolder.root, "lint-project-description.xml")
    val rootPath = tempFolder.root.absolutePath
    val wrongSrcPath = "$rootPath/app/src/main/nonexistent_java"

    projectDescriptionFile.writeText(
      createMinimalProjectDescriptionContent(
        rootPath = rootPath,
        srcPath = wrongSrcPath
      )
    )

    return projectDescriptionFile
  }

  private fun createMinimalProjectDescriptionContent(rootPath: String, srcPath: String): String {
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <project android="true" incomplete="false" desugar="full" client="cli">
        <root dir="$rootPath"/>
        <sdk dir="$sdkPath"/>
        <module name="app" library="false" android="true" compile-sdk-version="$buildSdkVersion"
                javaLanguage="$JAVA_VERSION" kotlinLanguage="1.6">
          <manifest file="$rootPath/app/src/main/AndroidManifest.xml"/>
          <src dir="$srcPath"/>
          <resource dir="$rootPath/app/src/main/res"/>
        </module>
      </project>
    """.trimIndent()
  }

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(listOf("junit:junit:4.12"))

    createModule("app")
    createModule("utility")
    createModule("domain")
    createModule("testing")
    createModule("data")
    val aarFile = createTestAarFile("test-aar", "1.0.0")
    val jarFile = createTestJarFile("test-jar", "1.0.0")

    setupFakeCommandExecutor(aarFile.absolutePath, jarFile.absolutePath)
  }

  private fun createModule(
    moduleName: String,
  ) {
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
      "$moduleName/src/test/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createModuleFiles(moduleName: String) {
    createManifestFile(moduleName)
    createTestManifestFile(moduleName)
    createSourceFile(moduleName)
    createTestFile(moduleName)
    createResourceFile(moduleName)
  }

  private fun createManifestFile(moduleName: String) {
    val manifest = tempFolder.newFile("$moduleName/src/main/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$moduleName">
      <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="34" />
    </manifest>
      """.trimIndent()
    )
  }

  private fun createTestManifestFile(moduleName: String) {
    val manifest = tempFolder.newFile("$moduleName/src/test/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$moduleName">
      <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="34" />
    </manifest>
      """.trimIndent()
    )
  }

  private fun createSourceFile(moduleName: String) {
    val className = moduleName.replaceFirstChar { it.uppercase() }
    val sourceDir = tempFolder.newFolder(
      moduleName, "src", "main", "java", "org", "oppia", "android", moduleName
    )
    val sourceFile = File(sourceDir, "${className}Class.kt")
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
    val className = moduleName.replaceFirstChar { it.uppercase() }
    val testDir = tempFolder.newFolder(
      moduleName, "src", "test", "java", "org", "oppia", "android", moduleName
    )
    val testFile = File(testDir, "${className}ClassTest.kt")
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

  private fun createFileWithContent(relativePath: String, content: String): File {
    val pathParts = relativePath.split("/")
    val directoryParts = pathParts.dropLast(1)

    if (directoryParts.isNotEmpty()) {
      val parentDir = File(tempFolder.root, directoryParts.joinToString("/"))
      if (!parentDir.exists()) {
        parentDir.mkdirs()
      }
    }

    val file = File(tempFolder.root, relativePath)
    file.writeText(content.trimIndent())
    return file
  }

  private fun setupFakeCommandExecutor(aarPath: String, jarPath: String) {
    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.contains("deps(//app:*)") -> {
          outputStream.println(aarPath)
          outputStream.println(jarPath)
          0
        }
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          // Return empty for other modules
          0
        }
        args.contains("info") -> {
          outputStream.println("output_base: ${tempFolder.root.absolutePath}/bazel-out")
          outputStream.println("java-home: $jdkHome")
          outputStream.println("java-runtime: OpenJDK Runtime Environment (build $JAVA_VERSION)")
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
      zipOut.putNextEntry(ZipEntry("AndroidManifest.xml"))
      zipOut.write(
        """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="com.example.$libraryName" />
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()

      // Create a valid empty JAR file in memory
      val byteStream = ByteArrayOutputStream()
      ZipOutputStream(byteStream).use {
        // optionally you can add a dummy .class here too
        it.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
        it.write("Manifest-Version: 1.0\n".toByteArray())
        it.closeEntry()
      }
      zipOut.putNextEntry(ZipEntry("classes.jar"))
      zipOut.write(byteStream.toByteArray())
      zipOut.closeEntry()

      // Add resources
      zipOut.putNextEntry(ZipEntry("res/values/strings.xml"))
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
    val jarFile = tempFolder.newFile("$libraryName-$version.jar")

    ZipOutputStream(FileOutputStream(jarFile)).use { zipOut ->
      val classEntry = ZipEntry("com/example/$libraryName/Class.class")
      zipOut.putNextEntry(classEntry)
      zipOut.write(ByteArray(10))
      zipOut.closeEntry()
    }

    return jarFile
  }
}
