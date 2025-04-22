package org.oppia.android.scripts.testfile

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that all production files have test files present.
 *
 * Usage:
 *   bazel run //scripts:test_file_check -- <path_to_directory_root> <path_to_exemptions>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:test_file_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"
  val testFileExemptionProtoPath = "scripts/assets/test_file_exemptions.pb"
  if (TestFileCheck(repoPath, testFileExemptionProtoPath).execute()) {
    println("TEST FILE CHECK PASSED")
  } else throw Exception("TEST FILE CHECK FAILED")
}

/**
 * Class for checking the presence of test files in a repository.
 *
 * @param repoPath The path of the repo
 * @param testFileExemptionProtoPath the location of the test file exemption proto file
 */
class TestFileCheck(
  private val repoPath: String,
  private val testFileExemptionProtoPath: String
) {
  /**
   * Executes the test file presence check mechanism for the repository and returns whether it was a
   * success.
   */
  fun execute(): Boolean {
    // TODO(#3436): Develop a mechanism for permanently exempting files which do not ever need tests.
    val repoRoot = File(repoPath).absoluteFile.normalize()
    val absoluteExemptedProdFilePaths =
      loadTestFileExemptionsProto(testFileExemptionProtoPath)
        .testFileExemptionList
        .filter { it.testFileNotRequired }
        .mapTo(mutableSetOf()) { File(repoRoot, it.exemptedFilePath).absolutePath }
    val absoluteExemptedTestFilePaths =
      loadTestFileExemptionsProto(testFileExemptionProtoPath)
        .testFileExemptionList
        .filter { it.isExtraTestFile }
        .mapTo(mutableSetOf()) { File(repoRoot, it.exemptedFilePath).absolutePath }

    val searchFiles = RepositoryFile.collectSearchFiles(
      repoPath = repoPath,
      expectedExtension = ".kt",
      exemptionsList = emptyList()
    )

    val testableProdClasses = searchFiles.filter { !it.name.endsWith("Test.kt") }.map { prodFile ->
      TestableProdClass.createFromProdFile(repoRoot, prodFile)
    }
    val testedClasses = searchFiles.filter { it.name.endsWith("Test.kt") }.map { testFile ->
      TestableProdClass.createFromTestFile(repoRoot, testFile)
    }
    val allTestableClasses = testableProdClasses + testedClasses

    val classesWithFailures = allTestableClasses.filter { testableClass ->
      testableClass.possibleProdFiles.none(File::exists) ||
        testableClass.possibleTestFiles.none(File::exists)
    }
    val classesMissingTests =
      classesWithFailures.filterIsInstance<TestableProdClass.DerivedFromProdFile>()
        .filterNot { it.isExempted(absoluteExemptedProdFilePaths) }
        .sortedBy { it.prodFile }
    val testsMissingProdClasses =
      classesWithFailures.filterIsInstance<TestableProdClass.DerivedFromTestFile>()
        .filterNot { it.isExempted(absoluteExemptedTestFilePaths) }
        .sortedBy { it.testFile }

    if (classesMissingTests.isNotEmpty()) {
      println("========== Classes missing test files: ${classesMissingTests.size} ==========")
      classesMissingTests.forEach { testableClass ->
        val prodFilePath = testableClass.prodFile.toRelativeString(repoRoot)
        val possibleTestFiles = allTestableClasses.filter {
          it.className == testableClass.className
        }.flatMap { it.possibleTestFiles }.filter(File::exists).toSet()
        if (possibleTestFiles.isNotEmpty()) {
          println("- File $prodFilePath has no corresponding test file. Possible matches:")
          possibleTestFiles.forEach { file ->
            println("  - ${file.toRelativeString(repoRoot)}")
          }
        } else println("- File $prodFilePath has no corresponding test file.")
      }
      println()
    }

    if (testsMissingProdClasses.isNotEmpty()) {
      println("========== Tests missing prod files: ${testsMissingProdClasses.size} ==========")
      testsMissingProdClasses.forEach { testableClass ->
        val testFilePath = testableClass.testFile.toRelativeString(repoRoot)
        val possibleProdFiles = allTestableClasses.filter {
          it.className == testableClass.className
        }.flatMap { it.possibleProdFiles }.filter(File::exists).toSet()
        val baseMessage =
          "- Test $testFilePath has no corresponding prod file. Is it in the wrong package?"
        if (possibleProdFiles.isNotEmpty()) {
          println("$baseMessage Possible matches:")
          possibleProdFiles.forEach { file ->
            println("  - ${file.toRelativeString(repoRoot)}")
          }
        } else println(baseMessage)
      }
      println()
    }

    // Validate that all exemptions exist.
    val allExemptedAbsoluteFilePaths =
      loadTestFileExemptionsProto(testFileExemptionProtoPath)
        .testFileExemptionList
        .mapTo(mutableSetOf()) { File(repoRoot, it.exemptedFilePath).absolutePath }
    val missingExemptedFiles = allExemptedAbsoluteFilePaths.map(::File).filterNot(File::exists)
    if (missingExemptedFiles.isNotEmpty()) {
      println("========== Test file exemption failures: ${missingExemptedFiles.size} ==========")
      missingExemptedFiles.forEach {
        println("- Exempted file path does not exist: ${it.toRelativeString(repoRoot)}.")
      }
      println()
    }

    if (classesMissingTests.isNotEmpty() || testsMissingProdClasses.isNotEmpty()) {
      println(
        "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
          "#test-file-presence-check for more details on how to fix this."
      )
      println()
    }

    return classesMissingTests.isEmpty()
      && testsMissingProdClasses.isEmpty()
      && missingExemptedFiles.isEmpty()
  }
}

private fun loadTestFileExemptionsProto(testFileExemptionProtoPath: String): TestFileExemptions {
  val protoBinaryFile = File(testFileExemptionProtoPath)
  val builder = TestFileExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  val protoObj: TestFileExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as TestFileExemptions
  return protoObj
}

private sealed class TestableProdClass {
  abstract val qualifiedProdClass: String
  abstract val possibleProdFiles: List<File>
  abstract val possibleTestFiles: List<File>

  val className: String get() = qualifiedProdClass.substringAfterLast('.')

  data class DerivedFromProdFile(
    val prodFile: File,
    override val qualifiedProdClass: String,
    override val possibleTestFiles: List<File>
  ): TestableProdClass() {
    override val possibleProdFiles = listOf(prodFile)

    fun isExempted(exemptedAbsoluteProdFilePaths: Set<String>): Boolean =
      prodFile.absolutePath in exemptedAbsoluteProdFilePaths
  }

  data class DerivedFromTestFile(
    val testFile: File,
    override val qualifiedProdClass: String,
    override val possibleProdFiles: List<File>
  ): TestableProdClass() {
    override val possibleTestFiles = listOf(testFile)

    fun isExempted(exemptedAbsoluteTestFilePaths: Set<String>): Boolean =
      testFile.absolutePath in exemptedAbsoluteTestFilePaths
  }

  companion object {
    fun createFromProdFile(repoRoot: File, prodFile: File): TestableProdClass {
      val prodPackage = extractPackage(repoRoot, prodFile)
      val baseProdName = prodFile.nameWithoutExtension
      val layer = CodeLayer.computeFromPackage(repoRoot, prodFile, prodPackage)
      val layerProdDir = File(repoRoot, layer.prodFilesPath)
      val parentProdDir = File(layerProdDir, prodPackage.replace('.', '/'))
      val expectedProdFiles = layer.possibleProdFileExtensions.map {
        File(parentProdDir, "$baseProdName.$it")
      }
      // Don't check prod files that aren't in the prod directory (such as test base classes).
      check(".." in prodFile.toRelativeString(layerProdDir) || prodFile in expectedProdFiles) {
        "File ${prodFile.toRelativeString(repoRoot)}'s path doesn't match its package" +
          " $prodPackage, or it's in the wrong test directory for this layer."
      }
      val layerTestDirs = layer.testFilesPaths.map { File(repoRoot, it) }
      val testFileParents = layerTestDirs.map { File(it, prodPackage.replace('.', '/')) }
      return DerivedFromProdFile(
        prodFile,
        qualifiedProdClass = "$prodPackage.$baseProdName",
        possibleTestFiles = testFileParents.map { File(it, "${baseProdName}Test.kt") }
      )
    }

    fun createFromTestFile(repoRoot: File, testFile: File): TestableProdClass {
      val testPackage = extractPackage(repoRoot, testFile)
      val baseProdName = testFile.nameWithoutExtension.removeSuffix("Test")
      val layer = CodeLayer.computeFromPackage(repoRoot, testFile, testPackage)
      val layerTestDirs = layer.testFilesPaths.map { File(repoRoot, it) }
      val testFileParents = layerTestDirs.map { File(it, testPackage.replace('.', '/')) }
      val expectedTestFiles = testFileParents.map { File(it, "${baseProdName}Test.kt") }
      check(testFile in expectedTestFiles) {
        "Test ${testFile.toRelativeString(repoRoot)}'s path doesn't match its package" +
          " $testPackage, or it's in the wrong test directory for this layer."
      }
      val layerProdDir = File(repoRoot, layer.prodFilesPath)
      val parentProdDir = File(layerProdDir, testPackage.replace('.', '/'))
      return DerivedFromTestFile(
        testFile,
        qualifiedProdClass = "$testPackage.$baseProdName",
        possibleProdFiles = layer.possibleProdFileExtensions.map {
          File(parentProdDir, "$baseProdName.$it")
        }
      )
    }

    private fun extractPackage(repoRoot: File, file: File): String {
      val filePath = file.toRelativeString(repoRoot)
      val packageDecl = file.bufferedReader().use { reader ->
        reader.lineSequence().firstOrNull { it.startsWith("package") }
      } ?: error("Failed to find 'package' declaration in file: $filePath.")
      // TODO(#1639): Remove the extra ';' being removed here since that's Java-specific.
      return packageDecl.removePrefix("package").removeSuffix(";").trim().also {
        check(it.isNotEmpty()) { "File $filePath is missing expected package declaration: $it." }
        check(it.startsWith("org.oppia.android")) { "Invalid package parsed in $filePath: $it." }
      }
    }
  }
}

private sealed class CodeLayer {
  // TODO(#1639): Remove support for Java files once they're no longer needed.
  abstract val possibleProdFileExtensions: List<String>
  abstract val prodFilesPath: String
  // TODO(#2532): Consolidate tests to one location rather than have differentiaton.
  abstract val testFilesPaths: List<String>

  object App: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt", "java")
    override val prodFilesPath = "app/src/main/java"
    override val testFilesPaths = listOf("app/src/test/java", "app/src/sharedTest/java")
  }

  object Domain: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "domain/src/main/java"
    override val testFilesPaths = listOf("domain/src/test/java")
  }

  object Data: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "data/src/main/java"
    override val testFilesPaths = listOf("data/src/test/java")
  }

  object Testing: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "testing/src/main/java"
    override val testFilesPaths = listOf("testing/src/test/java")
  }

  object Utility: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "utility/src/main/java"
    override val testFilesPaths = listOf("utility/src/test/java")
  }

  object Instrumentation: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "instrumentation/src/java"
    override val testFilesPaths = listOf("instrumentation/src/javatests")
  }

  object Scripts: CodeLayer() {
    override val possibleProdFileExtensions = listOf("kt")
    override val prodFilesPath = "scripts/src/java"
    override val testFilesPaths = listOf("scripts/src/javatests")
  }

  companion object {
    fun computeFromPackage(repoRoot: File, file: File, pkg: String): CodeLayer {
      val layerHint = pkg.substringAfter("org.oppia.android.").substringBefore('.')
      val filePath = file.toRelativeString(repoRoot)
      return when (layerHint) {
        "app" -> App
        "domain" -> Domain
        "data" -> Data
        "testing" -> Testing
        "util" -> Utility
        "instrumentation" -> Instrumentation
        "scripts" -> Scripts
        else -> error("Invalid layer $layerHint in package: $pkg found in file: $filePath.")
      }
    }
  }
}
