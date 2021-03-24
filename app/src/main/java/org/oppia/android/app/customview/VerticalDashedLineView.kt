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

/**
 * Custom view to represent chapter progress in [StoryActivity].
 *
 * Reference: https://stackoverflow.com/a/27054463
 */
class VerticalDashedLineView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  private val paint: Paint
  private lateinit var canvas: Canvas

  init {
    val dashGap: Float = dpToPx(16)
    val dashLength: Float = dpToPx(32)
    val dashThickness: Float = dpToPx(6)

    paint = Paint()
    paint.isAntiAlias = true
    paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = dashThickness
    paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, dashGap), 0F)
  }

  fun setColor(colorId: Int) {
    paint.color = colorId
    drawLine()
  }

  override fun onDraw(canvas: Canvas) {
    this.canvas = canvas
    drawLine()
  }

  private fun drawLine() {
    if (::canvas.isInitialized) {
      val center = width * .5f
      this.canvas.drawLine(center, 0f, center, height.toFloat(), paint)
    }
  }

  private fun dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.toFloat(),
      resources.displayMetrics
    )
  }
}
