package org.oppia.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import org.oppia.app.R

private const val STROKE_DASH_GAP_IN_DEGREE = 12

/**
 * CustomView to represent story progress in Topic-Play-Tab.
 * Without chaptersFinished and totalChapters values this custom-view cannot be created.
 *
 * Reference: // https://stackoverflow.com/a/39210676
 */
class SegmentedCircularProgressView : View {
  private var sweepAngle = 0f
  private var strokeWidth = 0f

  private var baseRect: RectF? = null
  private lateinit var chapterFinishedArcPaint: Paint
  private lateinit var chapterNotFinishedArcPaint: Paint

  private var chaptersNotFinished: Int = 0
  private var chaptersFinished: Int = 0
  private var totalChapters: Int = 0

  fun setStoryChapterDetails(totalChaptersCount: Int, chaptersFinishedCount: Int) {
    if (this.totalChapters != totalChaptersCount ||
      this.chaptersFinished != chaptersFinishedCount
    ) {
      this.totalChapters = totalChaptersCount
      this.chaptersFinished = chaptersFinishedCount
      this.chaptersNotFinished = totalChaptersCount - chaptersFinishedCount
      initialise()
    }
  }

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  private fun initialise() {
    chaptersNotFinished = totalChapters - chaptersFinished
    strokeWidth = dpToPx(4)
    calculateSweepAngle()

    chapterFinishedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    chapterFinishedArcPaint.style = Paint.Style.STROKE
    chapterFinishedArcPaint.strokeCap = Paint.Cap.ROUND
    chapterFinishedArcPaint.strokeWidth = strokeWidth
    chapterFinishedArcPaint.color =
      ContextCompat.getColor(context, R.color.oppiaProgressChapterFinished)

    chapterNotFinishedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    chapterNotFinishedArcPaint.style = Paint.Style.STROKE
    chapterNotFinishedArcPaint.strokeCap = Paint.Cap.ROUND
    chapterNotFinishedArcPaint.strokeWidth = strokeWidth
    if (chaptersFinished != 0) {
      chapterNotFinishedArcPaint.color =
        ContextCompat.getColor(context, R.color.oppiaProgressChapterNotFinished)
    } else {
      chapterNotFinishedArcPaint.color = ContextCompat.getColor(context, R.color.grey_shade_20)
    }
  }

  /* ktlint-disable max-line-length */
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (baseRect == null) {
      val centerX = measuredWidth / 2
      val centerY = measuredHeight / 2
      val radius = Math.min(centerX, centerY)

      val startTop = (strokeWidth / 2).toInt()
      val startLeft = (strokeWidth / 2).toInt()

      val endBottom = 2 * radius - startTop
      val endRight = 2 * radius - startTop

      // baseRect will define the drawing space for drawArc().
      baseRect =
        RectF(startLeft.toFloat(), startTop.toFloat(), endRight.toFloat(), endBottom.toFloat())
    }

    var angleStartPoint = -90f

    if (totalChapters > 1) {
      // Draws arc for every finished chapter.
      for (i in 0 until chaptersFinished) {
        val startAngle =
          angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE) + STROKE_DASH_GAP_IN_DEGREE / 2
        canvas.drawArc(baseRect!!, startAngle, sweepAngle, false, chapterFinishedArcPaint)
      }
      angleStartPoint += chaptersFinished * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE)
      // Draws arc for every unfinished chapter.
      for (i in 0 until chaptersNotFinished) {
        val startAngle =
          angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE) + STROKE_DASH_GAP_IN_DEGREE / 2
        canvas.drawArc(baseRect!!, startAngle, sweepAngle, false, chapterNotFinishedArcPaint)
      }
    } else if (totalChapters == 1) {
      // Draws entire circle for finished an unfinished chapter.
      if (chaptersFinished == 1) {
        canvas.drawArc(baseRect!!, angleStartPoint, 360f, false, chapterFinishedArcPaint)
      } else {
        canvas.drawArc(baseRect!!, angleStartPoint, 360f, false, chapterNotFinishedArcPaint)
      }
    }
  }
  /* ktlint-enable max-line-length */

  private fun calculateSweepAngle() {
    val totalRemainingDegrees = (360 - STROKE_DASH_GAP_IN_DEGREE * totalChapters).toFloat()
    sweepAngle = totalRemainingDegrees / totalChapters
  }

  private fun dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.toFloat(),
      resources.displayMetrics
    )
  }
}
