package org.oppia.app.utility

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.Nullable
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
const val DRAG_STEP_COUNT = 10
/**
 * Length of time a drag should last for, in milliseconds.
 */
private const val DRAG_DURATION = 1500

/**
 * Duration between the last move event and the up event, in milliseconds.
 */
private const val WAIT_BEFORE_SENDING_UP = 400

class DragViewAction(
  private val mDragger: Dragger,
  private val mStartCoordinatesProvider:CoordinatesProvider,
  private val mEndCoordinatesProvider: CoordinatesProvider,
  private val mPrecisionDescriber: PrecisionDescriber
) : ViewAction {

  interface Dragger : Swiper {
    fun wrapUiController(uiController: UiController): UiController
  }

  /**
   * Executes different drag types to given positions.
   */
  enum class Drag : Dragger {
    /**
     * Starts a drag with a long-press.
     */
    LONG_PRESS {
      private val downMotion: DownMotionPerformer =
        object : DownMotionPerformer {
          override fun perform(uiController: UiController, coordinates: FloatArray, precision: FloatArray): MotionEvent {
            return performLongPress(uiController,coordinates,precision)
          }
        }

      private fun performLongPress(uiController: UiController, coordinates: FloatArray, precision: FloatArray): MotionEvent {
        val downEvent = MotionEvents.sendDown(uiController, coordinates, precision).down
        // Duration before a press turns into a long press.
        // Factor 1.5 is needed, otherwise a long press is not safely detected.
        // See android.test.TouchUtils longClickView
        val longPressTimeout = (ViewConfiguration.getLongPressTimeout() * 1.5f).toLong()
        uiController.loopMainThreadForAtLeast(longPressTimeout)
        return downEvent
      }

      override fun sendSwipe(uiController: UiController, startCoordinates: FloatArray, endCoordinates: FloatArray, precision: FloatArray): Swiper.Status {
        return sendLinearDrag(uiController, downMotion, startCoordinates, endCoordinates, precision)
      }

      private fun sendLinearDrag(uiController: UiController, downMotion: DragViewAction.DownMotionPerformer, startCoordinates: FloatArray, endCoordinates: FloatArray, precision: FloatArray): Swiper.Status {
        val steps = interpolate(startCoordinates,endCoordinates)
        val delayBetweenMovements = DRAG_DURATION / steps.size
        val downEvent = downMotion.perform(uiController, startCoordinates, precision) ?: return Swiper.Status.FAILURE
        try {
          steps.forEachIndexed { index, step ->
            if (!MotionEvents.sendMovement(uiController, downEvent, step)) {
              MotionEvents.sendCancel(uiController, downEvent)
              return Swiper.Status.FAILURE
            }
            val desiredTime: Long = downEvent.downTime + delayBetweenMovements * index
            val timeUntilDesired = desiredTime - SystemClock.uptimeMillis()
            if (timeUntilDesired > 10) {
              // If the wait time until the next event isn't long enough, skip the wait
              // and execute the next event.
              uiController.loopMainThreadForAtLeast(timeUntilDesired)
            }
          }
          // Wait before sending up because some drag handling logic may discard move events
          // that has been sent immediately before the up event. e.g. HandleView.
          uiController.loopMainThreadForAtLeast(WAIT_BEFORE_SENDING_UP.toLong())
          if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
            MotionEvents.sendCancel(uiController, downEvent)
            return Swiper.Status.FAILURE
          }
        } finally {
          downEvent.recycle()
        }
        return Swiper.Status.SUCCESS
      }

      private fun interpolate(start:FloatArray,end:FloatArray):Array<FloatArray>{
        val res = Array(DRAG_STEP_COUNT) { FloatArray(2) }
        for (i in 0 until DRAG_STEP_COUNT) {
          res[i][0] = start[0] + (end[0] - start[0]) * i / (DRAG_STEP_COUNT - 1f)
          res[i][1] = start[1] + (end[1] - start[1]) * i / (DRAG_STEP_COUNT - 1f)
        }
        return res
      }

      override fun toString(): String { return "long press and drag" }
    };

    override fun wrapUiController(uiController: UiController): UiController {
      return uiController
    }
  }

  /**
   * Interface to implement different "down motion" types.
   */
  interface DownMotionPerformer{
    /**
     * Performs and returns a down motion.
     *
     * @param uiController a UiController to use to send MotionEvents to the screen.
     * @param coordinates  a float[] with x and y values of center of the tap.
     * @param precision    a float[] with x and y values of precision of the tap.
     * @return the down motion event or null if the down motion event failed.
     */
    @Nullable
    fun perform(uiController: UiController, coordinates: FloatArray, precision: FloatArray): MotionEvent?
  }

  override fun getDescription(): String = mDragger.toString()

  override fun getConstraints(): Matcher<View> = allOf(isCompletelyDisplayed())

  override fun perform(uiController: UiController, view: View) {
    val mUiController = mDragger.wrapUiController(uiController)
    val startCoordinates = mStartCoordinatesProvider.calculateCoordinates(view)
    val endCoordinates = mEndCoordinatesProvider.calculateCoordinates(view)
    val precision = mPrecisionDescriber.describePrecision()
    val status: Swiper.Status
    status = try {
      mDragger.sendSwipe(
        mUiController, startCoordinates, endCoordinates, precision
      )
    } catch (re: RuntimeException) {
      throw PerformException.Builder()
        .withActionDescription(description)
        .withViewDescription(HumanReadables.describe(view))
        .withCause(re)
        .build()
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

}