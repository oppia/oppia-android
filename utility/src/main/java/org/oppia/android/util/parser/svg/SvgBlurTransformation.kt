package org.oppia.android.util.parser.svg

import android.content.Context
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import org.oppia.android.util.parser.image.ImageTransformation
import java.security.MessageDigest

/**
 * A Glide [Transformation] for blurring [ScalableVectorGraphic]s.
 *
 * Note that this does not actually perform any blurring directly on the graphic. Instead, it
 * arranges a new graphic that, when rendered, will be blurred at that point. Thus, this
 * transformation can be used as an SVG analog for [BitmapBlurTransformation].
 */
class SvgBlurTransformation : Transformation<ScalableVectorGraphic> {
  private companion object {
    // See: https://bumptech.github.io/glide/doc/transformations.html#required-methods.
    private val ID = SvgBlurTransformation::class.java.name
  }

  override fun transform(
    context: Context,
    toTransform: Resource<ScalableVectorGraphic>,
    outWidth: Int,
    outHeight: Int
  ): Resource<ScalableVectorGraphic> {
    return SimpleResource(toTransform.get().transform(listOf(ImageTransformation.BLUR)))
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update(ID.toByteArray())
  }

  override fun hashCode(): Int = ID.hashCode()

  override fun equals(other: Any?): Boolean = other is SvgBlurTransformation
}
