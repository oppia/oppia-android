package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
import java.io.File
import kotlin.system.exitProcess

/**
 * The main entrypoint for retrieving the list of affected tests from a particular encoded Base64
 * bucket. This is used to parse the output from compute_affected_tests.
 *
 * Usage:
 *   bazel run //scripts:retrieve_affected_tests -- \\
 *     <encoded_proto_in_base64> <path_to_bucket_name_output_file> \\
 *     <path_to_test_target_list_output_file>
 *
 * Arguments:
 * - encoded_proto_in_base64: the compressed & Base64-encoded [AffectedTestsBucket] proto computed
 *     by compute_affected_tests.
 * - path_to_bucket_name_output_file: path to the file where the test bucket name corresponding to
 *     this bucket should be printed.
 * - path_to_test_target_list_output_file: path to the file where the list of affected test targets
 *     corresponding to this bucket should be printed.
 *
 * Example:
 *   bazel run //scripts:retrieve_affected_tests -- $AFFECTED_BUCKETS_BASE64_ENCODED_PROTO \\
 *     $(pwd)/test_bucket_name $(pwd)/bazel_test_targets
 */
fun main(args: Array<String>) {
  if (args.size < 3) {
    println(
      "Usage: bazel run //scripts:retrieve_affected_tests --" +
        " <encoded_proto_in_base64> <path_to_bucket_name_output_file>" +
        " <path_to_test_target_list_output_file>"
    )
    exitProcess(1)
  }

  val protoBase64 = args[0]
  val bucketNameOutputFile = File(args[1])
  val testTargetsOutputFile = File(args[2])
  val affectedTestsBucket =
    AffectedTestsBucket.getDefaultInstance().mergeFromCompressedBase64(protoBase64)
  bucketNameOutputFile.printWriter().use { writer ->
    writer.println(affectedTestsBucket.cacheBucketName)
  }
  testTargetsOutputFile.printWriter().use { writer ->
    writer.println(affectedTestsBucket.affectedTestTargetsList.joinToString(separator = " "))
  }
}
