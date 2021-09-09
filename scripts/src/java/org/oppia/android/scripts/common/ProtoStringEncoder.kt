package org.oppia.android.scripts.common

import com.google.protobuf.MessageLite
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Encoder/decoder for stringifying lite protos in a compacted way. See companion object functions
 * for the available API.
 */
class ProtoStringEncoder private constructor() {
  private val base64Encoder by lazy { Base64.getEncoder() }
  private val base64Decoder by lazy { Base64.getDecoder() }

  private fun <M : MessageLite> encodeToCompressedBase64(message: M): String {
    val compressedMessage = ByteArrayOutputStream().also { byteOutputStream ->
      GZIPOutputStream(byteOutputStream).use { message.writeTo(it) }
    }.toByteArray()
    return base64Encoder.encodeToString(compressedMessage)
  }

  private fun <M : MessageLite> decodeFromCompressedBase64(base64: String, exampleMessage: M): M {
    val compressedMessage = base64Decoder.decode(base64)
    return GZIPInputStream(compressedMessage.inputStream()).use {
      @Suppress("UNCHECKED_CAST") // Proto guarantees type safety here.
      exampleMessage.newBuilderForType().mergeFrom(it).build() as M
    }
  }

  companion object {
    private val protoStringEncoder by lazy { ProtoStringEncoder() }

    /**
     * Returns a compressed Base64 representation of this proto that can later be decoded using
     * [mergeFromCompressedBase64].
     */
    fun <M : MessageLite> M.toCompressedBase64(): String =
      protoStringEncoder.encodeToCompressedBase64(this)

    /**
     * Merges this proto into a new proto decoded from the specified Base64 string. It's expected
     * that string was constructed using [toCompressedBase64].
     *
     * Note that this method ignores any properties in the current proto; it will treat it like a
     * default instance when populating fields from the new proto.
     */
    fun <M : MessageLite> M.mergeFromCompressedBase64(base64: String): M =
      protoStringEncoder.decodeFromCompressedBase64(base64, exampleMessage = this)
  }
}
