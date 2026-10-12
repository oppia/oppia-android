package org.oppia.android.app.customview

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.test.R
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestPlatform
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowCanvas
import java.util.concurrent.TimeUnit

private const val VIEW_WIDTH_PX = 216
private const val VIEW_HEIGHT_PX = 20
private const val FIRST_NODE_CENTER_X_PX = 8
private const val LAST_NODE_CENTER_X_PX = 208

/** Tests for [LessonProgressIndicatorView]. */
@RunWith(AndroidJUnit4::class)
@RunOn(TestPlatform.ROBOLECTRIC)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = Application::class, qualifiers = "mdpi")
class LessonProgressIndicatorViewTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testProgressAtCheckpoint_firstCheckpoint_fillsNodeAndStopsConnectorAtNode() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 1,
      totalCount = 5,
      isBetweenCheckpoints = false,
      shouldAnimate = false
    )

    assertThat(view.getRenderedProgressPosition()).isEqualTo(0f)
    assertThat(view.getTargetProgressPosition()).isEqualTo(0f)
    assertThat(view.getLastProgressUpdateWasAnimated()).isFalse()
    val canvas = drawToCanvas(view)
    assertThat(canvas.getDrawnCircle(0).centerX).isEqualTo(FIRST_NODE_CENTER_X_PX.toFloat())
    assertThat(canvas.linePaintHistoryCount).isEqualTo(1)
    val upcomingColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_upcoming_color)
    assertThat(canvas.getDrawnLine(0).paint.color)
      .isEqualTo(upcomingColor)

    val singleNodeCanvas = drawToCanvas(
      createLaidOutView(context).apply {
        setLessonProgress(
          completedCount = 1,
          totalCount = 1,
          isBetweenCheckpoints = false,
          shouldAnimate = false
        )
      }
    )
    val reachedColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_reached_color)
    assertThat(singleNodeCanvas.getDrawnCircle(0).paint.color)
      .isEqualTo(reachedColor)
  }

  @Test
  fun testProgressBetweenCheckpoints_withoutAnimation_drawsConnectorHalfwayForward() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 1,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = false
    )

    assertThat(view.getRenderedProgressPosition()).isEqualTo(0.5f)
    assertThat(view.isProgressAnimationRunning()).isFalse()
    val canvas = drawToCanvas(view)
    val reachedColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_reached_color)
    val upcomingColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_upcoming_color)
    assertThat(canvas.linePaintHistoryCount).isEqualTo(2)
    assertThat(canvas.getDrawnLine(1).stopX).isEqualTo(33f)
    assertThat(canvas.getDrawnLine(1).paint.color).isEqualTo(reachedColor)
    assertThat(canvas.getDrawnLine(0).paint.color).isEqualTo(upcomingColor)
  }

  @Test
  fun testProgressBetweenCheckpoints_withAnimation_animatesFromCurrentNodeToHalfway() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 2,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = true
    )

    assertThat(view.getRenderedProgressPosition()).isEqualTo(1f)
    assertThat(view.getTargetProgressPosition()).isEqualTo(1.5f)
    assertThat(view.getLastProgressUpdateWasAnimated()).isTrue()
    assertThat(view.getProgressAnimationStartCount()).isEqualTo(1)
    assertThat(view.isProgressAnimationRunning()).isTrue()

    shadowOf(Looper.getMainLooper()).idleFor(700, TimeUnit.MILLISECONDS)

    assertThat(view.getRenderedProgressPosition()).isWithin(0.001f).of(1.5f)
    assertThat(view.isProgressAnimationRunning()).isFalse()
  }

  @Test
  fun testSameProgressRebind_duringAnimation_keepsCurrentAnimationRunning() {
    val view = createLaidOutView(context)
    view.setLessonProgress(
      completedCount = 2,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = true
    )

    view.setLessonProgress(
      completedCount = 2,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = false
    )

    assertThat(view.getLastProgressUpdateWasAnimated()).isFalse()
    assertThat(view.getProgressAnimationStartCount()).isEqualTo(1)
    assertThat(view.isProgressAnimationRunning()).isTrue()
    assertThat(view.getTargetProgressPosition()).isEqualTo(1.5f)
  }

  @Test
  fun testDetach_duringProgressAnimation_finishesProgressAtTarget() {
    val view = createLaidOutView(context)
    view.setLessonProgress(
      completedCount = 2,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = true
    )

    invokeViewLifecycleMethod(view, "onDetachedFromWindow")

    assertThat(view.getRenderedProgressPosition()).isEqualTo(1.5f)
    assertThat(view.getTargetProgressPosition()).isEqualTo(1.5f)
    assertThat(view.isProgressAnimationRunning()).isFalse()
  }

  @Test
  fun testProgressAtFinalCheckpoint_fillsFinalNodeAndEntireConnector() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 5,
      totalCount = 5,
      isBetweenCheckpoints = false,
      shouldAnimate = false
    )

    assertThat(view.getRenderedProgressPosition()).isEqualTo(4f)
    val canvas = drawToCanvas(view)
    val reachedColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_reached_color)
    assertThat(canvas.getDrawnLine(1).stopX).isEqualTo(LAST_NODE_CENTER_X_PX.toFloat())
    assertThat(canvas.getDrawnLine(1).paint.color).isEqualTo(reachedColor)
    assertThat(canvas.circlePaintHistoryCount).isEqualTo(5)
    assertThat(canvas.getDrawnCircle(4).paint.color).isEqualTo(reachedColor)
  }

  @Test
  fun testProgressInRtl_betweenCheckpoints_fillsFromRightTowardLeft() {
    val view = createLaidOutView(context).apply {
      layoutDirection = View.LAYOUT_DIRECTION_RTL
      setLessonProgress(
        completedCount = 1,
        totalCount = 5,
        isBetweenCheckpoints = true,
        shouldAnimate = false
      )
    }

    val canvas = drawToCanvas(view)
    val reachedColor =
      getColor(context, R.color.component_color_lesson_progress_indicator_reached_color)
    assertThat(getCanvasHorizontalScale(canvas)).isEqualTo(-1f)
    assertThat(canvas.getDrawnLine(1).stopX).isEqualTo(33f)
    assertThat(canvas.getDrawnLine(1).paint.color).isEqualTo(reachedColor)
  }

  @Test
  fun testProgressInNightMode_usesAuditedHighContrastPalette() {
    val nightContext = context.createConfigurationContext(
      Configuration(context.resources.configuration).apply {
        uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
          Configuration.UI_MODE_NIGHT_YES
      }
    )
    val view = createLaidOutView(nightContext)

    view.setLessonProgress(
      completedCount = 1,
      totalCount = 5,
      isBetweenCheckpoints = false,
      shouldAnimate = false
    )

    val canvas = drawToCanvas(view)
    assertThat(canvas.getDrawnLine(0).paint.color)
      .isEqualTo(getColor(nightContext, R.color.color_def_dark_silver))

    val reachedNodeCanvas = drawToCanvas(
      createLaidOutView(nightContext).apply {
        setLessonProgress(
          completedCount = 1,
          totalCount = 1,
          isBetweenCheckpoints = false,
          shouldAnimate = false
        )
      }
    )
    assertThat(reachedNodeCanvas.getDrawnCircle(0).paint.color)
      .isEqualTo(getColor(nightContext, R.color.color_def_oppia_turquoise))

    val upcomingNodeCanvas = drawToCanvas(
      createLaidOutView(nightContext).apply {
        setLessonProgress(
          completedCount = 0,
          totalCount = 1,
          isBetweenCheckpoints = false,
          shouldAnimate = false
        )
      }
    )
    assertThat(upcomingNodeCanvas.getDrawnCircle(0).paint.color)
      .isEqualTo(getColor(nightContext, R.color.color_def_oppia_light_black))
    assertThat(upcomingNodeCanvas.getDrawnCircle(1).paint.color)
      .isEqualTo(getColor(nightContext, R.color.color_def_oppia_turquoise))

    val laterNodeCanvas = drawToCanvas(
      createLaidOutView(nightContext).apply {
        setLessonProgress(
          completedCount = 0,
          totalCount = 2,
          isBetweenCheckpoints = false,
          shouldAnimate = false
        )
      }
    )
    assertThat(laterNodeCanvas.getDrawnCircle(3).paint.color)
      .isEqualTo(getColor(nightContext, R.color.color_def_dark_silver))
  }

  @Test
  fun testProgressReachingNextCheckpoint_withAnimation_animatesFromHalfwayIntoTheNewNode() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 2,
      totalCount = 5,
      isBetweenCheckpoints = false,
      shouldAnimate = true
    )

    // The connector was resting halfway between the first two checkpoints, so it slides the
    // remaining half-step rather than jumping into the newly filled node.
    assertThat(view.getRenderedProgressPosition()).isEqualTo(0.5f)
    assertThat(view.getTargetProgressPosition()).isEqualTo(1f)
    assertThat(view.getLastProgressUpdateWasAnimated()).isTrue()
    assertThat(view.isProgressAnimationRunning()).isTrue()

    shadowOf(Looper.getMainLooper()).idleFor(700, TimeUnit.MILLISECONDS)

    assertThat(view.getRenderedProgressPosition()).isWithin(0.001f).of(1f)
    assertThat(view.isProgressAnimationRunning()).isFalse()
  }

  @Test
  fun testProgressAtFirstCheckpoint_withAnimation_doesNotAnimateBeforeTheFirstNode() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 1,
      totalCount = 5,
      isBetweenCheckpoints = false,
      shouldAnimate = true
    )

    // There's no connector before the first checkpoint, so there's nothing to animate over.
    assertThat(view.getRenderedProgressPosition()).isEqualTo(0f)
    assertThat(view.getLastProgressUpdateWasAnimated()).isFalse()
    assertThat(view.isProgressAnimationRunning()).isFalse()
  }

  @Test
  fun testProgressWithOutOfRangeCounts_clampsToValidFinalCheckpoint() {
    val view = createLaidOutView(context)

    view.setLessonProgress(
      completedCount = 10,
      totalCount = 5,
      isBetweenCheckpoints = true,
      shouldAnimate = false
    )

    assertThat(view.getRenderedProgressPosition()).isEqualTo(4f)
    assertThat(view.getTargetProgressPosition()).isEqualTo(4f)
    assertThat(view.isProgressAnimationRunning()).isFalse()
  }

  private fun createLaidOutView(context: Context): LessonProgressIndicatorView {
    return LessonProgressIndicatorView(context).apply {
      setWillNotDraw(false)
      measure(
        View.MeasureSpec.makeMeasureSpec(VIEW_WIDTH_PX, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(VIEW_HEIGHT_PX, View.MeasureSpec.EXACTLY)
      )
      layout(0, 0, VIEW_WIDTH_PX, VIEW_HEIGHT_PX)
    }
  }

  /**
   * Draws [view] onto a shadowed [Canvas] and returns it for inspection.
   *
   * [View.draw] is deliberately not used: it skips [View.onDraw] for a view that was never attached
   * to a window, so the protected method is invoked directly instead.
   */
  private fun drawToCanvas(view: LessonProgressIndicatorView): ShadowCanvas {
    val bitmap = Bitmap.createBitmap(VIEW_WIDTH_PX, VIEW_HEIGHT_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    LessonProgressIndicatorView::class.java.getDeclaredMethod("onDraw", Canvas::class.java)
      .apply { isAccessible = true }
      .invoke(view, canvas)
    return shadowOf(canvas)
  }

  /**
   * Invokes a protected [View] lifecycle method by name, since a view that was never attached has
   * no public way to be detached and the view isn't open for subclassing.
   */
  private fun invokeViewLifecycleMethod(view: LessonProgressIndicatorView, methodName: String) {
    LessonProgressIndicatorView::class.java.getDeclaredMethod(methodName)
      .apply { isAccessible = true }
      .invoke(view)
  }

  /**
   * Returns the horizontal scale applied to [canvas], which is how the view mirrors itself for
   * right-to-left layouts. [ShadowCanvas] records the scale but exposes no accessor for it, and the
   * recorded draw coordinates are pre-transform, so this is the only way to verify the mirror
   * without screenshot testing.
   */
  private fun getCanvasHorizontalScale(canvas: ShadowCanvas): Float {
    return ShadowCanvas::class.java.getDeclaredField("scaleX").run {
      isAccessible = true
      getFloat(canvas)
    }
  }

  private fun getColor(context: Context, colorResourceId: Int): Int =
    ContextCompat.getColor(context, colorResourceId)
}
