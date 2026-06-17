package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import org.oppia.android.app.views.R

private const val NODE_DIAMETER_DP = 16f
private const val CONNECTOR_THICKNESS_DP = 4f
private const val RING_STROKE_WIDTH_DP = 2f

/**
 * [View] that draws the lesson progress indicator shown above the navigation buttons: a horizontal
 * connector line with one circular node per checkpoint.
 *
 * Every checkpoint the learner has reached -- including the one they're currently on -- is drawn as
 * a solid filled circle, so the first card shows a single solid node and the terminal card shows
 * every node solid. The next upcoming checkpoint is drawn as an outlined teal ring to hint that it's
 * coming up; all remaining checkpoints are faint hollow rings. The connector is filled teal up to the
 * last reached node plus half the gap toward that upcoming ring, clamping to a full teal bar on the
 * terminal card. The raw counts are supplied via data binding (see [setLessonProgressCompletedCount]
 * and [setLessonProgressTotalCount]).
 */
class LessonProgressIndicatorView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private var completedCount: Int = 0
  private var totalCount: Int = 0

  private val nodeRadius = dpToPx(NODE_DIAMETER_DP / 2f)
  private val ringStrokeWidth = dpToPx(RING_STROKE_WIDTH_DP)

  private val reachedColor = ContextCompat.getColor(
    context, R.color.component_color_lesson_progress_indicator_reached_color
  )
  private val upcomingColor = ContextCompat.getColor(
    context, R.color.component_color_lesson_progress_indicator_upcoming_color
  )
  private val nodeBackgroundColor = ContextCompat.getColor(
    context, R.color.component_color_lesson_progress_indicator_node_background_color
  )

  private val connectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeWidth = dpToPx(CONNECTOR_THICKNESS_DP)
  }
  private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
  private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = ringStrokeWidth
  }

  /** Sets the number of checkpoints the learner has completed and redraws the indicator. */
  fun setLessonProgressCompletedCount(count: Int) {
    completedCount = count
    invalidate()
  }

  /** Sets the total number of checkpoints in the exploration and redraws the indicator. */
  fun setLessonProgressTotalCount(count: Int) {
    totalCount = count
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (totalCount <= 0) return
    // Mirror horizontally for right-to-left languages so progress fills from the right.
    if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)

    val centerY = height / 2f
    val firstCenterX = paddingStart + nodeRadius
    val lastCenterX = width - paddingEnd - nodeRadius
    val span = lastCenterX - firstCenterX

    if (totalCount > 1) {
      // Draw the full upcoming (unreached) connector, then overlay teal up to the last reached node
      // plus half the gap toward the upcoming look-ahead ring. That half-filled segment is the
      // proposal's "in-progress" connector state between the last completed checkpoint and the next
      // one; on the terminal card (every node solid, no ring) it clamps to a full teal bar.
      connectorPaint.color = upcomingColor
      canvas.drawLine(firstCenterX, centerY, lastCenterX, centerY, connectorPaint)
      if (completedCount >= 1) {
        val reachedFraction = (completedCount - 0.5f) / (totalCount - 1)
        val reachedEndX = (firstCenterX + span * reachedFraction).coerceAtMost(lastCenterX)
        connectorPaint.color = reachedColor
        canvas.drawLine(firstCenterX, centerY, reachedEndX, centerY, connectorPaint)
      }
    }

    for (i in 0 until totalCount) {
      val centerX = when {
        totalCount == 1 -> firstCenterX + span / 2f
        else -> firstCenterX + span * i / (totalCount - 1)
      }
      when {
        // Reached checkpoint, including the current one: solid filled node.
        i < completedCount -> {
          fillPaint.color = reachedColor
          canvas.drawCircle(centerX, centerY, nodeRadius, fillPaint)
        }
        // The next upcoming checkpoint: a teal ring hinting it's coming up. Shown on every card
        // (including the first) so the indicator has a uniform structure.
        i == completedCount ->
          drawRingNode(canvas, centerX, centerY, reachedColor)
        // Remaining upcoming checkpoints: faint hollow ring.
        else -> drawRingNode(canvas, centerX, centerY, upcomingColor)
      }
    }
  }

  private fun drawRingNode(canvas: Canvas, centerX: Float, centerY: Float, ringColor: Int) {
    val ringRadius = nodeRadius - ringStrokeWidth / 2f
    fillPaint.color = nodeBackgroundColor
    canvas.drawCircle(centerX, centerY, ringRadius, fillPaint)
    ringPaint.color = ringColor
    canvas.drawCircle(centerX, centerY, ringRadius, ringPaint)
  }

  private fun dpToPx(dp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
