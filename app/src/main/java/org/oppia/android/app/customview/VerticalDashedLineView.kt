package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import org.oppia.android.R

private const val DASH_GAP_IN_DP = 16
private const val DASH_LENGTH_IN_DP = 32
private const val DASH_THICKNESS_IN_DP = 6

/**
 * Custom view for drawing a vertical dashed line.
 *
 * This view can be customised by changing the values of [DASH_GAP_IN_DP], [DASH_LENGTH_IN_DP] and
 * [DASH_THICKNESS_IN_DP].
 *
 * Reference: https://stackoverflow.com/a/27054463
 */
class VerticalDashedLineView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  private val paint: Paint

  init {
    val dashGap: Float = dpToPx(DASH_GAP_IN_DP)
    val dashLength: Float = dpToPx(DASH_LENGTH_IN_DP)
    val dashThickness: Float = dpToPx(DASH_THICKNESS_IN_DP)

    paint = Paint().apply {
      isAntiAlias = true
      color = ContextCompat.getColor(context, R.color.oppia_primary)
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeWidth = dashThickness
      pathEffect = DashPathEffect(floatArrayOf(dashLength, dashGap), /* phase= */ 0F)
    }
  }

  /** Sets the color. */
  fun setColor(colorId: Int) {
    paint.color = colorId
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    val center = width * .5f
    canvas.drawLine(center, /* startY= */ 0f, center, height.toFloat(), paint)
  }

  private fun dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.toFloat(),
      resources.displayMetrics
    )
  }
}
