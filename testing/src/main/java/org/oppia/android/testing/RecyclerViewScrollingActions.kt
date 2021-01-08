package org.oppia.android.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import javax.inject.Inject

/**
 * Provides the different scrolling options for RecyclerView
 *
 * This is needed because few Roboelectric tests require different
 * scrolling actions for the tests to be successful.
 * See https://github.com/oppia/oppia-android/issues/2209
 * for more information.
 */
class RecyclerViewScrollingActions @Inject constructor(
  val testCoroutineDispatchers: TestCoroutineDispatchers
){

  fun scrollToPositionWithForAction(viewId: Int, position: Int) {
    onView(withId(viewId)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  fun scrollToPositionWithCompleteDisplayForAction(viewId: Int, position: Int) {
    onView(withId(viewId)).perform(
      actionOnItemAtPosition<RecyclerView.ViewHolder>(
        position,
        scrollTo()
      )
    )
    testCoroutineDispatchers.runCurrent()
  }
}