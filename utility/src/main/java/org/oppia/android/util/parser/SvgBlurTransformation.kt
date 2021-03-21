package org.oppia.android.util.parser

import android.content.Context
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import java.security.MessageDigest

class SvgBlurTransformation: Transformation<OppiaSvg> {
  private companion object {
    // See: https://bumptech.github.io/glide/doc/transformations.html#required-methods.
    private val ID = SvgBlurTransformation::class.java.name
  }

  override fun transform(
    context: Context,
    toTransform: Resource<OppiaSvg>,
    outWidth: Int,
    outHeight: Int
  ): Resource<OppiaSvg> {
    return SimpleResource(toTransform.get().transform(listOf(ImageTransformation.BLUR)))
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update(ID.toByteArray())
  }

  override fun hashCode(): Int = ID.hashCode()

  override fun equals(other: Any?): Boolean = other is SvgBlurTransformation
}
