package org.oppia.android.testing

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.BaseMatcher
import javax.inject.Inject

/**
 * Provides different scrolling actions for [RecyclerView]s. This is needed because different
 * testing scenarios potentially require different scrolling actions for the tests to be successful.
 * Use the methods in this class for all scrolling actions for [RecyclerView]s in Roboelectric and
 * Espresso test scenarios. See #2209 for more information.
 */
class RecyclerViewScrollingActions @Inject constructor(
  val testCoroutineDispatchers: TestCoroutineDispatchers
) {

  /**
   * Performs scrolling action to particular RecyclerView at a required position. Use this method
   * for all testing scenarios when scrolling to a RecyclerView at a particular position.
   */
  fun scrollToPosition(@LayoutRes recyclerViewId: Int, position: Int) {
    onView(withId(recyclerViewId)).perform(
      actionOnItemAtPosition<RecyclerView.ViewHolder>(
        position,
        scrollTo()
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * Performs scrolling action to [RecyclerView] of a particular ViewType designed by a view holder
   * via [baseMatcher]. Use this method for all testing scenarios when the [View] type of the
   * destination [RecyclerView] is known.
   */
  fun scrollToViewType(
    @LayoutRes recyclerViewId: Int,
    baseMatcher: BaseMatcher<RecyclerView.ViewHolder>
  ) {
    onView(withId(recyclerViewId)).perform(
      scrollToHolder(baseMatcher)
    )
    testCoroutineDispatchers.runCurrent()
  }
}
