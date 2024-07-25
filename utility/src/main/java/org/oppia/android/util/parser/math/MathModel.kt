package org.oppia.android.util.parser.math

import com.bumptech.glide.load.Key
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * Represents a set of LaTeX that can be rendered as a single bitmap.
 *
 * @property rawLatex the LaTeX to render
 * @property lineHeight the height (in pixels) of a text line (to help scale the LaTeX)
 * @property useInlineRendering whether the LaTeX will be inlined with text
 */
data class MathModel(
  val rawLatex: String,
  val lineHeight: Float,
  val useInlineRendering: Boolean
) {
  /** Returns a Glide [Key] signature (see [MathModelSignature] for specifics). */
  fun toKeySignature(): MathModelSignature =
    MathModelSignature.createSignature(rawLatex, lineHeight, useInlineRendering)

  /**
   * Glide [Key] that provides caching support by allowing individual renderable math scenarios to
   * be comparable based on select parameters.
   *
   * @property rawLatex the raw LaTeX string used to render a cached bitmap
   * @property lineHeightHundredX an [Int] representation of the 100x scaled line height from
   *     [MathModel] (this is used to preserve up to 2 digits of the height, but any past that will
   *     be truncated to reduce cache size for highly reusable cached renders)
   * @property useInlineRendering whether the render is formatted to be displayed in-line with text
   */
  data class MathModelSignature(
    val rawLatex: String,
    val lineHeightHundredX: Int,
    val useInlineRendering: Boolean
  ) : Key {
    // Impl reference: http://bumptech.github.io/glide/doc/caching.html#custom-cache-invalidation.

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
      val rawLatexBytes = rawLatex.encodeToByteArray()
      messageDigest.update(
        ByteBuffer.allocate(rawLatexBytes.size + Int.SIZE_BYTES + 1).apply {
          put(rawLatexBytes)
          putInt(lineHeightHundredX)
          put(if (useInlineRendering) 1 else 0)
        }.array()
      )
    }

    internal companion object {
      /** Returns a new [MathModelSignature] for the specified [MathModel] properties. */
      internal fun createSignature(
        rawLatex: String,
        lineHeight: Float,
        useInlineRendering: Boolean
      ): MathModelSignature {
        val lineHeightHundredX = (lineHeight * 100f).toInt()
        return MathModelSignature(rawLatex, lineHeightHundredX, useInlineRendering)
      }
    }
  }
}
