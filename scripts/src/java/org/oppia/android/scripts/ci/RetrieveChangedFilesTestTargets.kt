package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.proto.ChangedFilesBucket
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

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

  val rootDirectory = File(args[0]).absoluteFile
  val protoBase64 = args[1]
  val bucketNameOutputFile = File(args[2])
  val fileTestTargetsListOutputFile = File(args[3])

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

  val changedFilesTestTargets = bazelClient.retrieveBazelTargets(changedFilesBucket.changedFilesList)
  println("Changed Files Test Targets: $changedFilesTestTargets")

  fileTestTargetsListOutputFile.printWriter().use { writer ->
    writer.println(changedFilesTestTargets.joinToString(separator = " "))
  }
}
