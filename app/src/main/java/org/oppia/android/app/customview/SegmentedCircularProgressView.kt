package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

private const val STROKE_DASH_GAP_IN_DEGREE = 12

/**
 * CustomView to represent story progress in Topic-Play-Tab.
 * Without chaptersFinished and totalChapters values this custom-view cannot be created.
 *
 * Reference: // https://stackoverflow.com/a/39210676
 */
class SegmentedCircularProgressView : View {
  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var sweepAngle = 0f
  private var strokeWidth = 0f
  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  private lateinit var baseRect: RectF
  private lateinit var chapterFinishedArcPaint: Paint
  private lateinit var chapterInProgressArcPaint: Paint
  private lateinit var chapterNotStartedArcPaint: Paint

  private var chaptersNotStarted: Int = 0
  private var chaptersFinished: Int = 0
  private var chaptersInProgress: Int = 0
  private var totalChapters: Int = 0

  fun setStoryChapterDetails(
    totalChaptersCount: Int,
    chaptersFinishedCount: Int,
    chaptersInProgressCount: Int
  ) {
    if (this.totalChapters != totalChaptersCount ||
      this.chaptersFinished != chaptersFinishedCount ||
      this.chaptersInProgress != chaptersInProgressCount
    ) {
      this.totalChapters = totalChaptersCount
      this.chaptersFinished = chaptersFinishedCount
      this.chaptersInProgress = chaptersInProgressCount
      this.chaptersNotStarted = totalChaptersCount - chaptersFinishedCount - chaptersInProgressCount
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
    chaptersNotStarted = totalChapters - chaptersFinished - chaptersInProgress
    strokeWidth = dpToPx(4)
    calculateSweepAngle()

    chapterFinishedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    setupArcPaint(
      chapterFinishedArcPaint,
      R.color.component_color_lessons_tab_activity_chapter_completed_progress_color
    )

    chapterInProgressArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    setupArcPaint(
      chapterInProgressArcPaint,
      R.color.component_color_lessons_tab_activity_chapter_in_progress_progress_color
    )

    chapterNotStartedArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    if (chaptersFinished != 0) {
      setupArcPaint(
        chapterNotStartedArcPaint,
        R.color.component_color_lessons_tab_activity_chapter_not_finished_progress_color
      )
    } else {
      setupArcPaint(
        chapterNotStartedArcPaint,
        R.color.component_color_lessons_tab_activity_chapter_not_started_progress_color
      )
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }

  override fun onDraw(canvas: Canvas) {
    if (isRtl)
      rotationY = 180f
    super.onDraw(canvas)
    if (!this::baseRect.isInitialized) {
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
          angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE) +
            STROKE_DASH_GAP_IN_DEGREE / 2
        canvas.drawArc(baseRect, startAngle, sweepAngle, false, chapterFinishedArcPaint)
      }
      angleStartPoint += chaptersFinished * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE)
      // Draws arc for every chapter that is in progress.
      for (i in 0 until chaptersInProgress) {
        val startAngle =
          angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE) +
            STROKE_DASH_GAP_IN_DEGREE / 2
        canvas.drawArc(baseRect, startAngle, sweepAngle, false, chapterInProgressArcPaint)
      }
      angleStartPoint += chaptersInProgress * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE)
      // Draws arc for every chapter that is not started.
      for (i in 0 until chaptersNotStarted) {
        val startAngle =
          angleStartPoint + i * (sweepAngle + STROKE_DASH_GAP_IN_DEGREE) +
            STROKE_DASH_GAP_IN_DEGREE / 2
        canvas.drawArc(baseRect, startAngle, sweepAngle, false, chapterNotStartedArcPaint)
      }
    } else if (totalChapters == 1) {
      // Draws entire circle for finished an unfinished chapter.
      if (chaptersFinished == 1) {
        canvas.drawArc(
          baseRect,
          angleStartPoint,
          360f,
          false,
          chapterFinishedArcPaint
        )
      } else if (chaptersInProgress == 1) {
        canvas.drawArc(
          baseRect,
          angleStartPoint,
          360f,
          false,
          chapterInProgressArcPaint
        )
      } else {
        canvas.drawArc(
          baseRect,
          angleStartPoint,
          360f,
          false,
          chapterNotStartedArcPaint
        )
      }
    }
  }

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

  private fun setupArcPaint(arcPaint: Paint, color: Int) {
    arcPaint.apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeWidth = this@SegmentedCircularProgressView.strokeWidth
      this.color = ContextCompat.getColor(context, color)
    }
  }
}
