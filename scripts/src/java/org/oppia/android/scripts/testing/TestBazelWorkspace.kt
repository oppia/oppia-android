package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

// TODO: extract to top-level file & document & test
class TestBazelWorkspace(private val temporaryRootFolder: TemporaryFolder) {
  private val workspaceFile by lazy { temporaryRootFolder.newFile("WORKSPACE") }
  val rootBuildFile: File by lazy { temporaryRootFolder.newFile("BUILD.bazel") }
  private val testFileMap = mutableMapOf<String, File>()
  private val libraryFileMap = mutableMapOf<String, File>()
  private val testDependencyNameMap = mutableMapOf<String, String>()
  private var isConfiguredForKotlin = false
  private var isConfiguredForTests = false
  private var isConfiguredForLibraries = false

  fun initEmptyWorkspace() {
    // Sanity check, but in reality this is just initializing workspaceFile to ensure that it
    // exists.
    assertThat(workspaceFile.exists()).isTrue()
  }

  fun addTestToBuildFile(
    testName: String,
    testFile: File,
    withGeneratedDependency: Boolean = false,
    withExtraDependency: String? = null,
    subpackage: String? = null
  ): List<File> {
    val prereqFiles = ensureWorkspaceIsConfiguredForTests()
    val (dependencyTargetName, libPrereqFiles) = if (withGeneratedDependency) {
      createLibrary("${testName}Dependency")
    } else null to listOf()
    val buildFile = if (subpackage != null) {
      if (!File(temporaryRootFolder.root, subpackage).exists()) {
        temporaryRootFolder.newFolder(subpackage)
      }
      val newBuildFile = temporaryRootFolder.newFile("$subpackage/BUILD.bazel")
      prepareBuildFileForTests(newBuildFile)
      newBuildFile
    } else rootBuildFile
    return buildFile.appendingPrintWriter().use { writer ->
      testFileMap[testName] = testFile
      writer.println("kt_jvm_test(")
      writer.println("    name = \"$testName\",")
      writer.println("    srcs = [\"${testFile.name}\"],")
      writer.println("    deps = [")
      if (withGeneratedDependency) {
        testDependencyNameMap[testName] = dependencyTargetName ?: error("Something went wrong.")
        writer.println("        \":$dependencyTargetName\",")
      }
      withExtraDependency?.let { writer.println("        \"$it\",") }
      writer.println("    ],")
      writer.println(")")
      return@use listOf(testFile, buildFile) + prereqFiles + libPrereqFiles
    }
  }

  fun createTest(
    testName: String,
    withGeneratedDependency: Boolean = false,
    withExtraDependency: String? = null,
    subpackage: String? = null
  ): List<File> {
    val testFile = if (subpackage != null) {
      if (!File(temporaryRootFolder.root, subpackage).exists()) {
        temporaryRootFolder.newFolder(subpackage)
      }
      temporaryRootFolder.newFile("$subpackage/$testName.kt")
    } else temporaryRootFolder.newFile("$testName.kt")
    return addTestToBuildFile(
      testName,
      testFile,
      withGeneratedDependency,
      withExtraDependency,
      subpackage
    )
  }

  fun createLibrary(dependencyName: String): Pair<String, List<File>> {
    val prereqFiles = ensureWorkspaceIsConfiguredForLibraries()
    return rootBuildFile.appendingPrintWriter().use { writer ->
      val depFile = temporaryRootFolder.newFile("$dependencyName.kt")
      val libTargetName = "${dependencyName}_lib"
      libraryFileMap[libTargetName] = depFile
      writer.println("kt_jvm_library(")
      writer.println("    name = \"$libTargetName\",")
      writer.println("    srcs = [\"${depFile.name}\"],")
      writer.println(")")
      return@use libTargetName to listOf(depFile, rootBuildFile) + prereqFiles
    }
  }

  fun retrieveTestFile(testName: String): File = testFileMap.getValue(testName)

  fun retrieveLibraryFile(libraryName: String): File =
    libraryFileMap.getValue("${libraryName}_lib")

  fun retrieveTestDependencyFile(testName: String): File {
    return libraryFileMap.getValue(
      testDependencyNameMap[testName]
        ?: error("No entry for $testName. Was the test created without dependencies?")
    )
  }

  private fun ensureWorkspaceIsConfiguredForKotlin(): List<File> {
    if (!isConfiguredForKotlin) {
      workspaceFile.appendingPrintWriter().use { writer ->
        // Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
        writer.println("load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\")")
        writer.println("http_archive(")
        writer.println("    name = \"io_bazel_rules_kotlin\",")
        writer.println(
          "    sha256 = \"6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a9" +
            "8dcd4f07\","
        )
        writer.println(
          "    urls = [\"https://github.com/bazelbuild/rules_kotlin/releases" +
            "/download/v1.5.0-alpha-2/rules_kotlin_release.tgz\"],"
        )
        writer.println(")")
        writer.println(
          "load(\"@io_bazel_rules_kotlin//kotlin:dependencies.bzl\"," +
            " \"kt_download_local_dev_dependencies\")"
        )
        writer.println(
          "load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\"," +
            " \"kotlin_repositories\", \"kt_register_toolchains\")"
        )
        writer.println("kt_download_local_dev_dependencies()")
        writer.println("kotlin_repositories()")
        writer.println("kt_register_toolchains()")
      }
      isConfiguredForKotlin = true
      return listOf(workspaceFile)
    }
    return listOf()
  }

  private fun ensureWorkspaceIsConfiguredForTests(): List<File> {
    val affectedFiles = if (!isConfiguredForTests) {
      prepareBuildFileForTests(rootBuildFile)
      isConfiguredForTests = true
      listOf(rootBuildFile)
    } else listOf()
    return ensureWorkspaceIsConfiguredForKotlin() + affectedFiles
  }

  private fun ensureWorkspaceIsConfiguredForLibraries(): List<File> {
    val affectedFiles = if (!isConfiguredForLibraries) {
      rootBuildFile.appendingPrintWriter().use { writer ->
        writer.println("load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_library\")")
      }
      isConfiguredForLibraries = true
      listOf(rootBuildFile)
    } else listOf()
    return ensureWorkspaceIsConfiguredForKotlin() + affectedFiles
  }

  private fun prepareBuildFileForTests(buildFile: File) {
    buildFile.appendingPrintWriter().use { writer ->
      writer.println("load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_test\")")
    }
  }
}

// A version of File.printWriter() that appends content rather than overwriting it.
private fun File.appendingPrintWriter(): PrintWriter =
  PrintWriter(FileOutputStream(this, /* append= */ true).bufferedWriter())
