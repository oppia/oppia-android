package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The main entrypoint for retrieving the list of changed files from a particular encoded Base64
 * bucket. This is used to parse the output from compute_changed_files.
 *
 * Usage:
 *   bazel run //scripts:retrieve_changed_files -- \\
 *     <encoded_proto_in_base64> <path_to_bucket_name_output_file> \\
 *     <path_to_file_list_output_file>
 *
 * Arguments:
 * - encoded_proto_in_base64: the compressed & Base64-encoded [ChangedFilesBucket] proto computed
 *     by compute_changed_files.
 * - path_to_bucket_name_output_file: path to the file where the file bucket name corresponding to
 *     this bucket should be printed.
 * - path_to_file_list_output_file: path to the file where the list of changed files
 *     corresponding to this bucket should be printed.
 *
 * Example:
 *   bazel run //scripts:retrieve_changed_files -- $(pwd) $CHANGED_FILES_BUCKETS_BASE64_ENCODED_PROTO \\
 *     $(pwd)/file_bucket_name $(pwd)/changed_files
 */
fun main(args: Array<String>) {
  /*if (args.size < 3) {
    println(
      "Usage: bazel run //scripts:retrieve_changed_files --" +
        " <encoded_proto_in_base64> <path_to_bucket_name_output_file>" +
        " <path_to_file_list_output_file>"
    )
    exitProcess(1)
  }*/

  val repoRoot = args[0]
  val rootDirectory = File(repoRoot).absoluteFile
  val protoBase64 = args[1]
  val bucketNameOutputFile = File(args[2])
  val fileTestTargetsListOutputFile = File(args[3])

  val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"
  val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProto)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor: CommandExecutor =
      CommandExecutorImpl(
        scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
      )

    val bazelClient = BazelClient(rootDirectory, commandExecutor)

    val changedFilesBucket =
      ChangedFilesBucket.getDefaultInstance().mergeFromCompressedBase64(protoBase64)
    bucketNameOutputFile.printWriter().use { writer ->
      writer.println(changedFilesBucket.cacheBucketName)
    }

    val changedFilesTestFiles = changedFilesBucket.changedFilesList.flatMap { changedFile ->
      val exemption = testFileExemptionList[changedFile]
      if (exemption != null && exemption.testFileNotRequired) {
        emptyList()
      } else {
        findTestFile(rootDirectory, changedFile)
      }
    }
    println("Changed Files Test Files: $changedFilesTestFiles")

    val changedFilesTestTargets = bazelClient.retrieveBazelTargets(changedFilesTestFiles)
    println("Changed Files Test Targets: $changedFilesTestTargets")

    val changedFilesTestTargetWithoutSuffix = changedFilesTestTargets.map { it.removeSuffix(".kt") }
    println("Changed Files Test Targets without suffix: $changedFilesTestTargetWithoutSuffix")

    fileTestTargetsListOutputFile.printWriter().use { writer ->
      writer.println(changedFilesTestTargetWithoutSuffix.joinToString(separator = " "))
    }
  }
}

private fun findTestFile(rootDirectory: File, filePath: String): List<String> {
  val possibleTestFilePaths = when {
    filePath.startsWith("scripts/") -> {
      listOf(filePath.replace("/java/", "/javatests/").replace(".kt", "Test.kt"))
    }
    filePath.startsWith("app/") -> {
      listOf(
        filePath.replace("/main/", "/sharedTest/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "LocalTest.kt")
      )
    }
    else -> {
      listOf(filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"))
    }
  }

  // val repoRootFile = File(repoRoot).absoluteFile

  return possibleTestFilePaths
    .map { File(rootDirectory, it) }
    .filter(File::exists)
    .map { it.relativeTo(rootDirectory).path }
}

private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
  return File("$testFileExemptiontextProto.pb").inputStream().use { stream ->
    TestFileExemptions.newBuilder().also { builder ->
      builder.mergeFrom(stream)
    }.build()
  }
}
