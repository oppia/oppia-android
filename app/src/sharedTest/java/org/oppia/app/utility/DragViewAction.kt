package org.oppia.app.utility

import android.os.SystemClock
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.util.HumanReadables
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf

/**
 * The number of move events to send for each drag.
 */
private const val DRAG_STEP_COUNT = 10

/**
 * Duration between the last move event and the up event, in milliseconds.
 */
private const val WAIT_BEFORE_SENDING_UP_MS = 400

/**
 * A custom [ViewAction] that can be replicate the long press & drag action for espresso.
 *
 * Reference: https://android.googlesource.com/platform/frameworks/base/+/2ff41d4/core/tests/coretests/src/android/widget/espresso/DragAction.java
 */
class DragViewAction(
  private val startCoordinatesProvider: CoordinatesProvider,
  private val endCoordinatesProvider: CoordinatesProvider,
  private val precisionDescriber: PrecisionDescriber
) : ViewAction {

  override fun getDescription(): String = "long press and drag"

  override fun getConstraints(): Matcher<View> = allOf(isCompletelyDisplayed())

  override fun perform(uiController: UiController, view: View) {
    val startCoordinates = startCoordinatesProvider.calculateCoordinates(view)
    val endCoordinates = endCoordinatesProvider.calculateCoordinates(view)
    val precision = precisionDescriber.describePrecision()
    val downEvent = MotionEvents.sendDown(uiController, startCoordinates, precision).down
    var status: Swiper.Status

    try {
      // Factor 1.5 is needed, otherwise a long press is not safely detected.
      val longPressTimeout = (ViewConfiguration.getLongPressTimeout() * 1.5f).toLong()
      uiController.loopMainThreadForAtLeast(longPressTimeout)

      val steps = interpolate(startCoordinates, endCoordinates)

      steps.forEachIndexed { _, step ->
        if (!MotionEvents.sendMovement(uiController, downEvent, step)) {
          MotionEvents.sendCancel(uiController, downEvent)
          status = Swiper.Status.FAILURE
        }
      }
      // Wait before sending up because some drag handling logic may discard move events
      // that has been sent immediately before the up event.
      uiController.loopMainThreadForAtLeast(WAIT_BEFORE_SENDING_UP_MS.toLong())
      status = if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
        MotionEvents.sendCancel(uiController, downEvent)
        Swiper.Status.FAILURE
      } else {
        Swiper.Status.SUCCESS
      }
    } catch (re: RuntimeException) {
      throw PerformException.Builder()
        .withActionDescription(description)
        .withViewDescription(HumanReadables.describe(view))
        .withCause(re)
        .build()
    } finally {
      downEvent.recycle()
    }
    val duration = ViewConfiguration.getPressedStateDuration()
    // ensures that all work enqueued to process the swipe has been run.
    if (duration > 0) {
      uiController.loopMainThreadForAtLeast(duration.toLong())
    }
    if (status == Swiper.Status.FAILURE) {
      throw PerformException.Builder()
        .withActionDescription(description)
        .withViewDescription(HumanReadables.describe(view))
        .withCause(java.lang.RuntimeException("$description failed"))
        .build()
    }
  }

  /**
   * Calculates [DRAG_STEP_COUNT] points between the start and end so that view can be smoothly
   * transitioned when performing drag action.
   */
  private fun interpolate(start: FloatArray, end: FloatArray): Array<FloatArray> {
    val res = Array(DRAG_STEP_COUNT) { FloatArray(2) }
    for (i in 0 until DRAG_STEP_COUNT) {
      res[i][0] = start[0] + (end[0] - start[0]) * i / (DRAG_STEP_COUNT - 1f)
      res[i][1] = start[1] + (end[1] - start[1]) * i / (DRAG_STEP_COUNT - 1f)
    }
    return res
  }

}

/** Class to find coordinates of an item within your RecyclerView.*/
class RecyclerViewCoordinatesProvider(
  private val position: Int,
  private val childItemCoordinatesProvider: CoordinatesProvider
) : CoordinatesProvider {
  override fun calculateCoordinates(view: View): FloatArray {
    return childItemCoordinatesProvider.calculateCoordinates(
      (view as RecyclerView).layoutManager!!.findViewByPosition(position)!!
    )
  }
}

/** Class to find coordinates of a child with a given id.*/
class ChildViewCoordinatesProvider(
  private val childViewId: Int,
  private val insideChildViewCoordinatesProvider: CoordinatesProvider
) : CoordinatesProvider {
  override fun calculateCoordinates(view: View): FloatArray {
    return insideChildViewCoordinatesProvider.calculateCoordinates(
      view.findViewById<View>(
        childViewId
      )
    )
  }
}

/**Class to provide coordinates above and under a View.*/
enum class CustomGeneralLocation : CoordinatesProvider {
  UNDER_RIGHT {
    override fun calculateCoordinates(view: View): FloatArray {
      val screenLocation = IntArray(2)
      view.getLocationOnScreen(screenLocation)
      return floatArrayOf(
        screenLocation[0] + view.width - 1f,
        screenLocation[1] + view.height * 1.5f
      )
    }
  },
  ABOVE_RIGHT {
    override fun calculateCoordinates(view: View): FloatArray {
      val screenLocation = IntArray(2)
      view.getLocationOnScreen(screenLocation)
      return floatArrayOf(
        screenLocation[0] + view.width - 1f,
        screenLocation[1] - view.height * 0.5f
      )
    }

  }
}
