package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.rules.TemporaryFolder
import java.io.File

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

  private val testFileMap = mutableMapOf<String, File>()
  private val libraryFileMap = mutableMapOf<String, File>()
  private val testDependencyNameMap = mutableMapOf<String, String>()
  private var isConfiguredForKotlin = false
  private var isConfiguredForRulesJvmExternal = false
  private val filesConfiguredForTests = mutableListOf<File>()
  private val filesConfiguredForLibraries = mutableListOf<File>()

  /** Initializes the local Bazel workspace by introducing a new, empty WORKSPACE file. */
  fun initEmptyWorkspace() {
    // Sanity check, but in reality this is just initializing workspaceFile to ensure that it
    // exists.
    assertThat(workspaceFile.exists()).isTrue()
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
        "https://github.com/bazelbuild/rules_kotlin/releases/download/v1.5.0-alpha-2" +
          "/rules_kotlin_release.tgz"
      val rulesKotlinArchiveName = "io_bazel_rules_kotlin"
      val rulesKotlinBazelPrefix = "@$rulesKotlinArchiveName//kotlin"

      workspaceFile.appendText(
        """
        load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
        http_archive(
            name = "$rulesKotlinArchiveName",
            sha256 = "6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a98dcd4f07",
            urls = ["$rulesKotlinReleaseUrl"],
        )
        load("$rulesKotlinBazelPrefix:dependencies.bzl", "kt_download_local_dev_dependencies")
        load("$rulesKotlinBazelPrefix:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
        kt_download_local_dev_dependencies()
        kotlin_repositories()
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
      buildFile.appendText("load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_test\")\n")
      filesConfiguredForTests += buildFile
    }
  }

  private fun prepareBuildFileForLibraries(buildFile: File) {
    if (buildFile !in filesConfiguredForLibraries) {
      buildFile.appendText(
        "load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_library\")\n"
      )
      filesConfiguredForLibraries += buildFile
    }
  }
}
