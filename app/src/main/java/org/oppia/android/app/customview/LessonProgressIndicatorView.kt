package org.oppia.android.app.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import org.oppia.android.app.views.R

private const val NODE_DIAMETER_DP = 16f
private const val CONNECTOR_THICKNESS_DP = 4f
private const val RING_STROKE_WIDTH_DP = 2f
private const val PROGRESS_ANIMATION_DURATION_MILLIS = 600L
private const val BETWEEN_CHECKPOINT_OFFSET = 0.5f

/**
 * [View] that draws the lesson progress indicator shown above the navigation buttons: a horizontal
 * connector with one circular node per checkpoint.
 *
 * Reached checkpoints (including the current one) are drawn as solid nodes, the next checkpoint as
 * a teal ring, and the rest as faint rings. The connector rests at the current checkpoint while the
 * learner is on that checkpoint's card, animates halfway toward the next checkpoint once the
 * learner answers correctly, and animates the rest of the way when they reach that checkpoint.
 */
class LessonProgressIndicatorView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private var completedCount: Int = 0
  private var totalCount: Int = 0
  private var renderedProgressPosition: Float = 0f
  private var targetProgressPosition: Float = 0f
  private var progressAnimator: ValueAnimator? = null
  private var lastProgressUpdateWasAnimated: Boolean = false
  private var progressAnimationStartCount: Int = 0

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

  /**
   * Updates the indicator to represent the learner's checkpoint progress.
   *
   * [completedCount] is the one-based count of reached checkpoints. When
   * [isBetweenCheckpoints] is false, the connector ends inside the latest filled checkpoint. When
   * it is true, the connector ends halfway toward the next checkpoint. If [shouldAnimate] is true,
   * the connector slides over the half-step that separates it from its previous position.
   */
  fun setLessonProgress(
    completedCount: Int,
    totalCount: Int,
    isBetweenCheckpoints: Boolean,
    shouldAnimate: Boolean
  ) {
    val previousCompletedCount = this.completedCount
    val previousTotalCount = this.totalCount
    val normalizedTotalCount = totalCount.coerceAtLeast(0)
    val normalizedCompletedCount = completedCount.coerceIn(0, normalizedTotalCount)
    this.totalCount = normalizedTotalCount
    this.completedCount = normalizedCompletedCount
    val newTargetProgressPosition = computeProgressPosition(isBetweenCheckpoints)
    val shouldContinueCurrentAnimation =
      !shouldAnimate &&
        progressAnimator?.isRunning == true &&
        previousCompletedCount == normalizedCompletedCount &&
        previousTotalCount == normalizedTotalCount &&
        targetProgressPosition == newTargetProgressPosition
    targetProgressPosition = newTargetProgressPosition
    val animationStartPosition = computeAnimationStartPosition()
    lastProgressUpdateWasAnimated = shouldAnimate && animationStartPosition < targetProgressPosition
    if (shouldContinueCurrentAnimation) return

    progressAnimator?.cancel()
    progressAnimator = null
    if (lastProgressUpdateWasAnimated) {
      animateProgress(animationStartPosition, targetProgressPosition)
    } else {
      renderedProgressPosition = targetProgressPosition
      invalidate()
    }
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
      // Draw the full unreached connector, then overlay the portion the learner has reached.
      connectorPaint.color = upcomingColor
      canvas.drawLine(firstCenterX, centerY, lastCenterX, centerY, connectorPaint)
      if (renderedProgressPosition > 0f) {
        val reachedFraction = renderedProgressPosition / (totalCount - 1)
        val reachedEndX = firstCenterX + span * reachedFraction
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
        completedCount > 0 && i <= renderedProgressPosition -> {
          fillPaint.color = reachedColor
          canvas.drawCircle(centerX, centerY, nodeRadius, fillPaint)
        }
        // The next upcoming checkpoint: a teal ring hinting that it is coming up.
        i == computeNextUpcomingCheckpointIndex() ->
          drawRingNode(canvas, centerX, centerY, reachedColor)
        // Remaining upcoming checkpoints: faint hollow ring.
        else -> drawRingNode(canvas, centerX, centerY, upcomingColor)
      }
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (progressAnimator != null) {
      progressAnimator?.cancel()
      // A detached RecyclerView item can later be reattached without rebinding. Finish the visual
      // transition so that cancelling its animator cannot leave the connector at a partial point.
      renderedProgressPosition = targetProgressPosition
      invalidate()
    }
    progressAnimator = null
  }

  /** Returns the current connector position for tests. */
  @VisibleForTesting
  fun getRenderedProgressPosition(): Float = renderedProgressPosition

  /** Returns the connector's final position for tests. */
  @VisibleForTesting
  fun getTargetProgressPosition(): Float = targetProgressPosition

  /** Returns whether the connector is currently animating for tests. */
  @VisibleForTesting
  fun isProgressAnimationRunning(): Boolean = progressAnimator?.isRunning == true

  /** Returns whether the latest progress update requested an animation for tests. */
  @VisibleForTesting
  fun getLastProgressUpdateWasAnimated(): Boolean = lastProgressUpdateWasAnimated

  /** Returns the number of progress animations started by this view for tests. */
  @VisibleForTesting
  fun getProgressAnimationStartCount(): Int = progressAnimationStartCount

  private fun computeProgressPosition(isBetweenCheckpoints: Boolean): Float {
    if (totalCount <= 0 || completedCount <= 0) return 0f
    val checkpointPosition = computeCheckpointPosition()
    val targetPosition = if (isBetweenCheckpoints && completedCount < totalCount) {
      checkpointPosition + BETWEEN_CHECKPOINT_OFFSET
    } else {
      checkpointPosition
    }
    return targetPosition.coerceIn(0f, (totalCount - 1).toFloat())
  }

  private fun computeCheckpointPosition(): Float =
    (completedCount - 1).coerceAtLeast(0).toFloat()

  /**
   * Returns the position an animated update should start from.
   *
   * This is derived from the counts rather than from what's currently drawn so that a connector
   * animates over exactly one half-step even when the indicator was scrolled off screen for the
   * transition and is being bound into a recycled view.
   */
  private fun computeAnimationStartPosition(): Float {
    val checkpointPosition = computeCheckpointPosition()
    return if (targetProgressPosition > checkpointPosition) {
      // The learner completed the checkpoint they were on, so the connector leaves that checkpoint.
      checkpointPosition
    } else {
      // The learner reached a new checkpoint, so the connector arrives from the halfway point it
      // had been resting at.
      (checkpointPosition - BETWEEN_CHECKPOINT_OFFSET).coerceAtLeast(0f)
    }
  }

  private fun computeNextUpcomingCheckpointIndex(): Int =
    if (completedCount == 0) 0 else renderedProgressPosition.toInt() + 1

  private fun animateProgress(startPosition: Float, endPosition: Float) {
    ++progressAnimationStartCount
    renderedProgressPosition = startPosition
    progressAnimator = ValueAnimator.ofFloat(startPosition, endPosition).apply {
      duration = PROGRESS_ANIMATION_DURATION_MILLIS
      interpolator = DecelerateInterpolator()
      addUpdateListener { animator ->
        renderedProgressPosition = animator.animatedValue as Float
        invalidate()
      }
      start()
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
