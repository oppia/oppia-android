package org.oppia.android.util.extensions

import android.os.Bundle
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.MessageLite

/**
 * Saves the specified proto in the bundle under the specified key name.
 *
 * The proto can then be retrieved from the bundle using [Bundle.getProto].
 */
fun <T : MessageLite> Bundle.putProto(name: String, message: T) {
  putSerializable(name, message.toByteString())
}

/**
 * Returns the proto stored in this bundle corresponding to the specified name, or the default value
 * if there is no proto stored under the specified name.
 *
 * This should only be used by protos stored using [Bundle.putProto].
 *
 * Note that the proto type of [defaultValue] must correspond to the type stored in the bundle,
 * otherwise this function will have undefined behavior. The proto stored in the bundle should be
 * interoperable with the current version of the proto (in case the persisted proto survives across
 * app processes which may be possible in some circumstances such as low-memory process kills),
 * otherwise no guarantees can be made about the stability of the data returned.
 *
 * If the [name] provided corresponds to an existing value of a non-proto type, this function will
 * return [defaultValue].
 */
fun <T : MessageLite> Bundle.getProto(name: String, defaultValue: T): T {
  val serializedByteString = getSerializable(name) as? ByteString
  return serializedByteString?.let {
    // Type safety is *generally* guaranteed by newBuilderForType. If the bundle actually has an
    // incorrect type, then the mergeFrom() call should fail.
    return@let try {
      @Suppress("UNCHECKED_CAST")
      defaultValue.newBuilderForType().mergeFrom(it).build() as T
    } catch (e: InvalidProtocolBufferException) {
      null
    }
  } ?: defaultValue
}
