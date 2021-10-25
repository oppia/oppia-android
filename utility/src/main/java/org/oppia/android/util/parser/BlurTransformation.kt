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

// The radius of the blur, a float between 0 and 25. It defines the value of the standard
// deviation to the Gaussian function.
private const val BLUR_RADIUS = 20f

/**
 * [BlurTransformation] is a bitmap transformation that blurs an image using RenderScript.
 *
 * The following [tutorial](https://futurestud.io/tutorials/glide-custom-transformation)
 * was used as a reference, as well as this [article](https://stackoverflow.com/a/23119957).
 */
class BlurTransformation(private val context: Context) : BitmapTransformation() {

  private val renderScript by lazy { RenderScript.create(context) }

  override fun transform(
    pool: BitmapPool,
    toTransform: Bitmap,
    outWidth: Int,
    outHeight: Int
  ): Bitmap {
    val blurredBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true)
    // Create a RenderScript allocation pointing to a copy.
    val inputAllocation = Allocation.createFromBitmap(
      renderScript,
      blurredBitmap,
      Allocation.MipmapControl.MIPMAP_FULL,
      Allocation.USAGE_SHARED
    )
    // Create a new RenderScript allocation to receive the output from the blur operation.
    val outputAllocation = Allocation.createTyped(renderScript, inputAllocation.type)

    // Create a new Gaussian blur script with 4 expect 8-bit color channels (e.g. ARGB).
    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    blurScript.setInput(inputAllocation)
    blurScript.setRadius(BLUR_RADIUS)
    blurScript.forEach(outputAllocation)

    outputAllocation.copyTo(blurredBitmap)
    return blurredBitmap
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update("blur transformation".toByteArray())
  }
}
