package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/** [BlurTransformation] is a bitmap transformation that blurs an image using RenderScript. */
class BlurTransformation(
  private val context: Context,
  private val radius: Float = 20f
) : BitmapTransformation() {

  override fun transform(
    pool: BitmapPool,
    toTransform: Bitmap,
    outWidth: Int,
    outHeight: Int
  ): Bitmap {
    val blurredBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true)
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(
      rs,
      blurredBitmap,
      Allocation.MipmapControl.MIPMAP_FULL,
      Allocation.USAGE_SHARED
    )
    val output = Allocation.createTyped(rs, input.type)
    val intrinsicScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    intrinsicScript.setInput(input)
    intrinsicScript.setRadius(radius)
    intrinsicScript.forEach(output)
    output.copyTo(blurredBitmap)
    return blurredBitmap
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update("blur transformation".toByteArray())
  }
}
