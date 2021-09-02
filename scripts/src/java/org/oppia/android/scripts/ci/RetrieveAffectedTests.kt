package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
import java.io.File

fun main(args: Array<String>) {
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
