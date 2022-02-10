package org.oppia.android.util.parser.math

import com.bumptech.glide.load.Key
import java.nio.ByteBuffer
import java.security.MessageDigest

data class MathModel(
  val rawLatex: String, val lineHeight: Float, val useInlineRendering: Boolean
) {
  fun toKeySignature(): MathModelSignature =
    MathModelSignature.createSignature(rawLatex, lineHeight, useInlineRendering)

  // Reference: http://bumptech.github.io/glide/doc/caching.html#custom-cache-invalidation.
  // TODO: document that lineHeight is only stored up to 2 decimal places, and to use factory method.
  data class MathModelSignature(
    val rawLatex: String, val lineHeightHundredX: Int, val useInlineRendering: Boolean
  ) : Key {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
      val rawLatexBytes = rawLatex.encodeToByteArray()
      messageDigest.update(ByteBuffer.allocate(rawLatexBytes.size + Int.SIZE_BYTES + 1).apply {
        put(rawLatexBytes)
      putInt(lineHeightHundredX)
        put(if (useInlineRendering) 1 else 0)
      }.array())
    }

    companion object {
      fun createSignature(
        rawLatex: String, lineHeight: Float, useInlineRendering: Boolean
      ): MathModelSignature {
        val lineHeightHundredX = (lineHeight * 100f).toInt()
        return MathModelSignature(rawLatex, lineHeightHundredX, useInlineRendering)
      }
    }
  }
}
