// TODO(#3616): Migrate to the proper SDK 31+ APIs (migrate away from RenderScript).
@file:Suppress("DEPRECATION") // The code is correct for targeted versions of Android.

package org.oppia.android.util.parser.image

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * The radius of the blur, a float between 0 and 25. It defines the value of the standard deviation
 * to the Gaussian function.
 */
private const val BLUR_RADIUS = 20f

/** Utility used to blur [Bitmap]s. */
class BitmapBlurrer(private val context: Context) {
  private val renderScript by lazy { RenderScript.create(context) }
  private val blurScript by lazy {
    // Create a new Gaussian blur script with 4 expect 8-bit color channels (e.g. ARGB).
    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    blurScript.setRadius(BLUR_RADIUS)
    return@lazy blurScript
  }

  /**
   * Returns a new [Bitmap] that is the blurred version of the specified bitmap. This does not
   * change the input bitmap.
   *
   * Note that this function is expensive, so the result should be cached & reused when possible.
   */
  fun blur(bitmap: Bitmap): Bitmap {
    // References: https://futurestud.io/tutorials/glide-custom-transformation and
    // https://stackoverflow.com/a/23119957.

    val blurredBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, /* isMutable = */ true)
    // Create a RenderScript allocation pointing to a copy.
    val inputAllocation = Allocation.createFromBitmap(
      renderScript,
      blurredBitmap,
      Allocation.MipmapControl.MIPMAP_FULL,
      Allocation.USAGE_SHARED
    )

    // Create a new RenderScript allocation to receive the output from the blur operation.
    val outputAllocation = Allocation.createTyped(renderScript, inputAllocation.type)
    blurScript.setInput(inputAllocation)
    blurScript.forEach(outputAllocation)
    outputAllocation.copyTo(blurredBitmap)

    return blurredBitmap
  }
}
