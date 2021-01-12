package org.oppia.android.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import javax.inject.Inject

/**
 * Provides different scrolling actions for [RecyclerView]s.
 *
 * This is needed because different testing scenarios in Roboelectric tests potentially require
 * different scrolling actions for the tests to be successful.
 * See #2209 for more information.
 */
class RecyclerViewScrollingActions @Inject constructor(
  val testCoroutineDispatchers: TestCoroutineDispatchers
) {

  /**
   * Returns a [ViewAction] which scrolls RecyclerView to a position
   * Usage : TODO
   */
  fun scrollToPosition(viewId: Int, position: Int) {
    onView(withId(viewId)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * Performs a [ViewAction] on a view at position.
   * 1) Scroll RecyclerView to position
   * 2) Perform an action on the view at position
   * Usage : TODO
   */
  fun scrollToPositionAndPerformAction(viewId: Int, position: Int) {
    onView(withId(viewId)).perform(
      actionOnItemAtPosition<RecyclerView.ViewHolder>(
        position,
        scrollTo()
      )
    )
    testCoroutineDispatchers.runCurrent()
  }
}
