package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.rules.TemporaryFolder
import java.io.File

/** The version of Bazel to use in tests that set up Bazel workspaces. */
const val BAZEL_VERSION = "6.5.0"

/**
 * Test utility for generating various test & library targets in the specified [TemporaryFolder].
 * This is meant to be used to arrange the local test filesystem for use with a real Bazel
 * application on the host system.
 *
 * Note that constructing this class is insufficient to start using Bazel locally. At minimum,
 * [initEmptyWorkspace] must be called first.
 */
class TestBazelWorkspace(private val temporaryRootFolder: TemporaryFolder) {

  /** The [File] corresponding to the Bazel WORKSPACE file. */
  val workspaceFile by lazy { temporaryRootFolder.newFile("WORKSPACE") }

  /**
   * The root BUILD.bazel file which will, by default, hold generated libraries & tests (for those
   * not generated as part of subpackages).
   */
  val rootBuildFile: File by lazy { temporaryRootFolder.newFile("BUILD.bazel") }

  private val bazelVersionFile by lazy {
    temporaryRootFolder.newFile(".bazelversion").also { it.writeText(BAZEL_VERSION) }
  }
  private val bazelRcFile by lazy {
    temporaryRootFolder.newFile(".bazelrc").also {
      it.writeText(
        """
          --noenable_bzlmod
          build --java_runtime_version=remotejdk_11 --tool_java_runtime_version=remotejdk_11
        """.trimIndent()
      )
    }
  }

  private val testFileMap = mutableMapOf<String, File>()
  private val libraryFileMap = mutableMapOf<String, File>()
  private val testDependencyNameMap = mutableMapOf<String, String>()
  private var isConfiguredForKotlin = false
  private var isConfiguredForRulesJvmExternal = false
  private val filesConfiguredForTests = mutableListOf<File>()
  private val filesConfiguredForLibraries = mutableListOf<File>()

  /** Initializes the local Bazel workspace by introducing a new, empty WORKSPACE file. */
  fun initEmptyWorkspace() {
    // Sanity checks, but in reality this is just initializing workspaceFile to ensure that it
    // exists.
    assertThat(workspaceFile.exists()).isTrue()
    assertThat(bazelVersionFile.exists()).isTrue()
    assertThat(bazelRcFile.exists()).isTrue()
  }

  /**
   * Adds a source file and test file with the specified name and content,
   * and updates the corresponding build configuration.
   *
   * @param filename the name of the source file (without the .kt extension)
   * @param sourceContent the content of the source file
   * @param testContent the content of the test file
   * @param sourceSubpackage the subpackage under which the source files should be added
   * @param testSubpackage the subpackage under which the test files should be added
   */
  fun addSourceAndTestFileWithContent(
    filename: String,
    testFilename: String,
    sourceContent: String,
    testContent: String,
    sourceSubpackage: String,
    testSubpackage: String
  ) {
    addSourceContentAndBuildFile(
      filename,
      sourceContent,
      sourceSubpackage
    )

    addTestContentAndBuildFile(
      filename,
      testFilename,
      testContent,
      sourceSubpackage,
      testSubpackage
    )
  }

  /**
   * Adds a source file and 2 test files with the specified name and content,
   * and updates the corresponding build configuration.
   *
   * @param filename the name of the source file (without the .kt extension)
   * @param sourceContent the content of the source file
   * @param testContentShared the content of the test file for SharedTest Package
   * @param testContentLocal the content of the test file for Test Package
   * @param subpackage the subpackage under which the source and test files should be added
   */
  fun addMultiLevelSourceAndTestFileWithContent(
    filename: String,
    sourceContent: String,
    testContentShared: String,
    testContentLocal: String,
    subpackage: String
  ) {
    val sourceSubpackage = "$subpackage/main/java/com/example"
    addSourceContentAndBuildFile(
      filename,
      sourceContent,
      sourceSubpackage
    )

    val testSubpackageShared = "$subpackage/sharedTest/java/com/example"
    val testFileNameShared = "${filename}Test"
    addTestContentAndBuildFile(
      filename,
      testFileNameShared,
      testContentShared,
      sourceSubpackage,
      testSubpackageShared
    )

    val testSubpackageLocal = "$subpackage/test/java/com/example"
    val testFileNameLocal = "${filename}LocalTest"
    addTestContentAndBuildFile(
      filename,
      testFileNameLocal,
      testContentLocal,
      sourceSubpackage,
      testSubpackageLocal
    )
  }

  /**
   * Adds a source file with the specified name and content to the specified subpackage,
   * and updates the corresponding build configuration.
   *
   * @param filename the name of the source file (without the .kt extension)
   * @param sourceContent the content of the source file
   * @param sourceSubpackage the subpackage under which the source file should be added
   * @return the target name of the added source file
   */
  fun addSourceContentAndBuildFile(
    filename: String,
    sourceContent: String,
    sourceSubpackage: String
  ) {
    initEmptyWorkspace()
    ensureWorkspaceIsConfiguredForKotlin()
    setUpWorkspaceForRulesJvmExternal(
      listOf("junit:junit:4.12")
    )

    // Create the source subpackage directory if it doesn't exist
    if (!File(temporaryRootFolder.root, sourceSubpackage.replace(".", "/")).exists()) {
      temporaryRootFolder.newFolder(*(sourceSubpackage.split(".")).toTypedArray())
    }

    // Create the source file
    val sourceFile = temporaryRootFolder.newFile(
      "${sourceSubpackage.replace(".", "/")}/$filename.kt"
    )
    sourceFile.writeText(sourceContent)

    // Create or update the BUILD file for the source file
    val buildFileRelativePath = "${sourceSubpackage.replace(".", "/")}/BUILD.bazel"
    val buildFile = File(temporaryRootFolder.root, buildFileRelativePath)
    if (!buildFile.exists()) {
      temporaryRootFolder.newFile(buildFileRelativePath)
    }
    prepareBuildFileForLibraries(buildFile)

    buildFile.appendText(
      """
      kt_jvm_library(
          name = "${filename.lowercase()}",
          srcs = ["$filename.kt"],
          visibility = ["//visibility:public"]
      )
      """.trimIndent() + "\n"
    )
  }

  /**
   * Adds a test file with the specified name and content to the specified subpackage,
   * and updates the corresponding build configuration.
   *
   * @param filename the name of the source file (without the .kt extension)
   * @param testName the name of the test file (without the .kt extension)
   * @param testContent the content of the test file
   * @param testSubpackage the subpackage for the test file
   */
  fun addTestContentAndBuildFile(
    filename: String,
    testName: String,
    testContent: String,
    sourceSubpackage: String,
    testSubpackage: String
  ) {
    initEmptyWorkspace()

    // Create the test subpackage directory for the test file if it doesn't exist
    if (!File(temporaryRootFolder.root, testSubpackage.replace(".", "/")).exists()) {
      temporaryRootFolder.newFolder(*(testSubpackage.split(".")).toTypedArray())
    }

    // Create the test file
    val testFile = temporaryRootFolder.newFile("${testSubpackage.replace(".", "/")}/$testName.kt")
    testFile.writeText(testContent)

    // Create or update the BUILD file for the test file
    val testBuildFileRelativePath = "${testSubpackage.replace(".", "/")}/BUILD.bazel"
    val testBuildFile = File(temporaryRootFolder.root, testBuildFileRelativePath)
    if (!testBuildFile.exists()) {
      temporaryRootFolder.newFile(testBuildFileRelativePath)
    }
    prepareBuildFileForTests(testBuildFile)

    // Add the test file to the BUILD file with appropriate dependencies
    testBuildFile.appendText(
      """
      kt_jvm_test(
          name = "$testName",
          srcs = ["$testName.kt"],
          deps = [
              "//$sourceSubpackage:${filename.lowercase()}",
              "@maven//:junit_junit",
          ],
          visibility = ["//visibility:public"],
          test_class = "com.example.$testName",
      )
      """.trimIndent() + "\n"
    )
  }

  /**
   * Generates and adds a new kt_jvm_test target with the target name [testName] and test file
   * [testFile]. This can be used to add multiple tests to the same build file, and will
   * automatically set up the local WORKSPACE file, if needed, to support kt_jvm_test.
   *
   * @param testName the name of the generated test target (must be unique)
   * @param testFile the local test file (which does not necessarily need to exist yet)
   * @param withGeneratedDependency whether to automatically generate a new library (see
   *     [createLibrary]) and add it to the new test target as a dependency
   * @param withExtraDependency if present, will be added as an additional dependency to the
   *     generated test target
   * @param subpackage if present, the subpackage under which the test target should live. This will
   *     create a new BUILD.bazel file if one isn't already present. Note that only one subpackage
   *     level can be used (i.e. "subpackage" is valid, but "sub.package" is not).
   * @return an iterable of [File]s that were changed as part of generating this new test
   */
  fun addTestToBuildFile(
    testName: String,
    testFile: File,
    withGeneratedDependency: Boolean = false,
    withExtraDependency: String? = null,
    subpackage: String? = null
  ): Iterable<File> {
    initEmptyWorkspace() // Ensure the workspace is at least initialized.

    check(testName !in testFileMap) { "Test '$testName' already set up" }
    val prereqFiles = ensureWorkspaceIsConfiguredForKotlin()
    val (dependencyTargetName, libPrereqFiles) = if (withGeneratedDependency) {
      createLibrary("${testName}Dependency")
    } else null to listOf()
    val buildFile = if (subpackage != null) {
      if (!File(temporaryRootFolder.root, subpackage.replace(".", "/")).exists()) {
        temporaryRootFolder.newFolder(*(subpackage.split(".")).toTypedArray())
      }
      val newBuildFileRelativePath = "${subpackage.replace(".", "/")}/BUILD.bazel"
      val newBuildFile = File(temporaryRootFolder.root, newBuildFileRelativePath)
      if (newBuildFile.exists()) {
        newBuildFile
      } else temporaryRootFolder.newFile(newBuildFileRelativePath)
    } else rootBuildFile
    prepareBuildFileForTests(buildFile)

    testFileMap[testName] = testFile
    val generatedDependencyExpression = if (withGeneratedDependency) {
      testDependencyNameMap[testName] = dependencyTargetName ?: error("Something went wrong.")
      "\"$dependencyTargetName\","
    } else ""
    val extraDependencyExpression = withExtraDependency?.let { "\"$it\"," } ?: ""
    buildFile.appendText(
      """
        kt_jvm_test(
            name = "$testName",
            srcs = ["${testFile.name}"],
            deps = [$generatedDependencyExpression$extraDependencyExpression],
        )
      """.trimIndent() + "\n"
    )

    return setOf(testFile, buildFile) + prereqFiles + libPrereqFiles
  }

  /**
   * Generates a new test target using the specified [testName], and generating a local test file
   * using the test name as the filename (with a Kotlin extension).
   *
   * For details on the parameters & return value, see [addTestToBuildFile] for more context.
   */
  fun createTest(
    testName: String,
    withGeneratedDependency: Boolean = false,
    withExtraDependency: String? = null,
    subpackage: String? = null
  ): Iterable<File> {
    // Note that the workspace doesn't need to be explicitly initialized here since the call below
    // to addTestToBuildFile() will initialize it.

    check(testName !in testFileMap) { "Test '$testName' already exists" }
    val testFile = if (subpackage != null) {
      if (!File(temporaryRootFolder.root, subpackage.replace(".", "/")).exists()) {
        temporaryRootFolder.newFolder(*(subpackage.split(".")).toTypedArray())
      }
      temporaryRootFolder.newFile("${subpackage.replace(".", "/")}/$testName.kt")
    } else temporaryRootFolder.newFile("$testName.kt")
    return addTestToBuildFile(
      testName,
      testFile,
      withGeneratedDependency,
      withExtraDependency,
      subpackage
    )
  }

  /**
   * Generates a new library using the specified dependency name, modifying the local
   * [rootBuildFile] and updating the WORKSPACE to support Kotlin libraries, if necessary.
   *
   * @param dependencyName the name of the library dependency, which will be used both for the
   *     target name and a local source file representing the 'library'. For example, if "SampleLib"
   *     is passed as the dependency name then a "SampleLib.kt" will be generated and the library's
   *     target name will be "SampleLib_lib".
   * @return a pair where the first value is the library's target name and the second value is an
   *     iterable of files that were changed as part of generating this library
   */
  fun createLibrary(dependencyName: String): Pair<String, Iterable<File>> {
    initEmptyWorkspace() // Ensure the workspace is at least initialized.

    val libTargetName = "${dependencyName}_lib"
    check("//:$libTargetName" !in libraryFileMap) { "Library '$dependencyName' already exists" }
    val prereqFiles = ensureWorkspaceIsConfiguredForKotlin()
    prepareBuildFileForLibraries(rootBuildFile)

    val depFile = temporaryRootFolder.newFile("$dependencyName.kt")
    libraryFileMap["//:$libTargetName"] = depFile
    rootBuildFile.appendText(
      """
      kt_jvm_library(
          name = "$libTargetName",
          srcs = ["${depFile.name}"],
      )
      """.trimIndent() + "\n"
    )

    return "//:$libTargetName" to (setOf(depFile, rootBuildFile) + prereqFiles)
  }

  /**
   * Return the source test file corresponding to the test with the specified [testName], if one
   * exists (otherwise an exception is thrown).
   */
  fun retrieveTestFile(testName: String): File = testFileMap.getValue(testName)

  /**
   * Returns the source library file corresponding to the library with the specified
   * [dependencyName], if one exists (otherwise an exception is thrown).
   */
  fun retrieveLibraryFile(dependencyName: String): File =
    libraryFileMap.getValue("${dependencyName}_lib")

  /**
   * Returns the source library file corresponding to the library that was generated when creating
   * the test corresponding to [testName], or an exception is thrown if either the test does not
   * exist or it was not configured with a test dependency.
   *
   * This function is only valid to call for tests that were initialized by setting
   * 'withGeneratedDependency' to 'true' when calling either [addTestToBuildFile] or [createTest].
   */
  fun retrieveTestDependencyFile(testName: String): File {
    return libraryFileMap.getValue(
      testDependencyNameMap[testName]
        ?: error("No entry for '$testName'. Was the test created without dependencies?")
    )
  }

  /** Appends rules_jvm_external configuration to the WORKSPACE file if not done already. */
  fun setUpWorkspaceForRulesJvmExternal(depsList: List<String>) {
    if (!isConfiguredForRulesJvmExternal) {
      initEmptyWorkspace()
      workspaceFile.appendText("artifactsList = [")
      for (dep in depsList) {
        workspaceFile.appendText("\"$dep\",\n")
      }
      workspaceFile.appendText("]\n")
      workspaceFile.appendText(
        """
        load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

        RULES_JVM_EXTERNAL_TAG = "4.0"
        RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

        http_archive(
            name = "rules_jvm_external",
            strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
            sha256 = RULES_JVM_EXTERNAL_SHA,
            url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
        )

        load("@rules_jvm_external//:defs.bzl", "maven_install")

        maven_install(
            artifacts = artifactsList,
            repositories = [
                "https://maven.google.com",
                "https://repo1.maven.org/maven2",
            ],
        )
        """.trimIndent() + "\n"
      )

      isConfiguredForRulesJvmExternal = true
    }
  }

  private fun ensureWorkspaceIsConfiguredForKotlin(): List<File> {
    if (!isConfiguredForKotlin) {
      // Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
      val rulesKotlinReleaseUrl =
        "https://github.com/bazelbuild/rules_kotlin/releases/download/v1.7.1" +
          "/rules_kotlin_release.tgz"
      val rulesKotlinArchiveName = "io_bazel_rules_kotlin"
      val rulesKotlinBazelPrefix = "@$rulesKotlinArchiveName//kotlin"

      workspaceFile.appendText(
        """
        load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
        http_archive(
            name = "$rulesKotlinArchiveName",
            sha256 = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3",
            urls = ["$rulesKotlinReleaseUrl"],
        )
        load("$rulesKotlinBazelPrefix:repositories.bzl", "kotlin_repositories")
        kotlin_repositories()
        load("$rulesKotlinBazelPrefix:core.bzl", "kt_register_toolchains")
        kt_register_toolchains()
        """.trimIndent() + "\n"
      )
      isConfiguredForKotlin = true
      return listOf(workspaceFile)
    }
    return listOf()
  }

  private fun prepareBuildFileForTests(buildFile: File) {
    if (buildFile !in filesConfiguredForTests) {
      buildFile.appendText("load(\"@io_bazel_rules_kotlin//kotlin:jvm.bzl\", \"kt_jvm_test\")\n")
      filesConfiguredForTests += buildFile
    }
  }

  private fun prepareBuildFileForLibraries(buildFile: File) {
    if (buildFile !in filesConfiguredForLibraries) {
      buildFile.appendText(
        "load(\"@io_bazel_rules_kotlin//kotlin:jvm.bzl\", \"kt_jvm_library\")\n"
      )
      filesConfiguredForLibraries += buildFile
    }
  }
}
