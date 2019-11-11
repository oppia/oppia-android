package org.oppia.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.oppia.app.R

private const val STROKE_DASH_GAP_IN_DEGREE = 12

/**
 * CustomView to represent story progress in Topic-Play-Tab.
 * Reference: // https://stackoverflow.com/a/39210676
 */
class CustomProgressView : View {
  private var sweepAngle = 0f
  private var strokeWidth = 0f

  private var baseRect: RectF? = null
  private var chapterFinishedArcPaint: Paint? = null
  private var chapterNotFinishedArcPaint: Paint? = null

  var chaptersFinished: Int = 0
  var chaptersNotFinished: Int = 0
  var totalChapters: Int = 0

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init()
  }

  fun init() {
    chaptersNotFinished = totalChapters - chaptersFinished
    strokeWidth = dpToPx(4)
    calculateSweepAngle()

    chapterFinishedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    chapterFinishedArcPaint!!.style = Paint.Style.STROKE
    chapterFinishedArcPaint!!.strokeCap = Paint.Cap.ROUND
    chapterFinishedArcPaint!!.strokeWidth = strokeWidth
    chapterFinishedArcPaint!!.color = ContextCompat.getColor(context, R.color.oppiaProgressChapterFinished)

    chapterNotFinishedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    chapterNotFinishedArcPaint!!.style = Paint.Style.STROKE
    chapterNotFinishedArcPaint!!.strokeCap = Paint.Cap.ROUND
    chapterNotFinishedArcPaint!!.strokeWidth = strokeWidth
    chapterNotFinishedArcPaint!!.color = ContextCompat.getColor(context, R.color.oppiaProgressChapterNotFinished)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // getHeight() is not reliable, use getMeasuredHeight() on first run:
    // Note: baseRect will also be null after a configuration change,
    // so in this case the new measured height and width values will be used:
    if (baseRect == null) {
      // take the minimum of width and height here to be on he safe side:
      val centerX = measuredWidth / 2
      val centerY = measuredHeight / 2
      val radius = Math.min(centerX, centerY)

      // baseRect will define the drawing space for drawArc()
      // We have to take into account the strokeWidth with drawArc() as well as drawCircle():
      // circles as well as arcs are drawn 50% outside of the bounds defined by the radius (radius for arcs is calculated from the rectangle baseRect).
      // So if baseRect is too large, the lines will not fit into the View
      val startTop = (strokeWidth / 2).toInt()
      val startLeft = (strokeWidth / 2).toInt()

      val endBottom = 2 * radius - startTop
      val endRight = 2 * radius - startTop

      baseRect = RectF(startLeft.toFloat(), startTop.toFloat(), endRight.toFloat(), endBottom.toFloat())
    }

    var angleStartPoint = -90f
    for (i in 0 until chaptersFinished) {
      canvas.drawArc(
        baseRect!!,
        angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE),
        sweepAngle,
        false,
        chapterFinishedArcPaint!!
      )
    }

    angleStartPoint += chaptersFinished * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE)
    for (i in 0 until chaptersNotFinished) {
      canvas.drawArc(
        baseRect!!,
        angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE),
        sweepAngle,
        false,
        chapterNotFinishedArcPaint!!
      )
    }
  }

  private fun calculateSweepAngle() {
    val totalRemainingDegrees = (360 - STROKE_DASH_GAP_IN_DEGREE * totalChapters).toFloat()
    sweepAngle = totalRemainingDegrees / totalChapters
  }

  private fun dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
  }
}
