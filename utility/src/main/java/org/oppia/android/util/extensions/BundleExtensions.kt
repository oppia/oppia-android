package org.oppia.android.util.extensions

import android.content.Intent
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

/**
 * Puts the specified proto as an extra in this [Intent], initializing the intent's extras if they
 * aren't already, under the specified name.
 *
 * The proto can be retrieved using [getProtoExtra].
 */
fun <T : MessageLite> Intent.putProtoExtra(name: String, message: T) {
  // Ensure the extras Bundle is fully initialized before adding the proto.
  replaceExtras((extras ?: Bundle()).also { it.putProto(name, message) })
}

/**
 * Returns the proto corresponding to the specified key from this [Intent]'s extras, or
 * [defaultValue] if the intent doesn't have its extras initialized. See also [getProto] for other
 * scenarios in which the default value is returned.
 */
fun <T : MessageLite> Intent.getProtoExtra(name: String, defaultValue: T): T {
  return extras?.getProto(name, defaultValue) ?: defaultValue
}

/**
 * Returns the string from this [Bundle] corresponding to the specified key, or null if there isn't
 * one.
 */
fun Bundle.getStringFromBundle(key: String): String? = getString(key)
