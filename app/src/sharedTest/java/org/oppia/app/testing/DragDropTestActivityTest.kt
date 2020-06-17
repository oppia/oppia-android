package org.oppia.app.testing

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.RecyclerViewCoordinatesProvider

@RunWith(AndroidJUnit4::class)
class DragDropTestActivityTest {

  @Test
  fun testDragDropTestActivity_dragItem0ToPosition1() {
    launch(DragDropTestActivity::class.java).use {

      safelyWaitUntilIdle()

      onView(withId(R.id.drag_drop_recycler_View)).perform(
          DragViewAction(
            DragViewAction.Drag.LONG_PRESS,
            RecyclerViewCoordinatesProvider(0, ChildViewCoordinatesProvider(R.id.text_view_for_string_no_data_binding, GeneralLocation.CENTER)),
            RecyclerViewCoordinatesProvider(1, CustomGeneralLocation.UNDER_RIGHT),
            Press.FINGER
          )
        )

      onView(atPosition(R.id.drag_drop_recycler_View, 0)).check(matches(withText("Item 2")))
      onView(atPosition(R.id.drag_drop_recycler_View, 1)).check(matches(withText("Item 1")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem1ToPosition2() {
    launch(DragDropTestActivity::class.java).use {

      safelyWaitUntilIdle()

      onView(withId(R.id.drag_drop_recycler_View)).perform(
          DragViewAction(
            DragViewAction.Drag.LONG_PRESS,
            RecyclerViewCoordinatesProvider(1, ChildViewCoordinatesProvider(R.id.text_view_for_string_no_data_binding, GeneralLocation.CENTER)),
            RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.UNDER_RIGHT),
            Press.FINGER
          )
        )
      onView(atPosition(R.id.drag_drop_recycler_View, 1)).check(matches(withText("Item 3")))
      onView(atPosition(R.id.drag_drop_recycler_View, 2)).check(matches(withText("Item 2")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem3ToPosition2() {
    launch(DragDropTestActivity::class.java).use {

      safelyWaitUntilIdle()

      onView(withId(R.id.drag_drop_recycler_View)).perform(
        DragViewAction(
          DragViewAction.Drag.LONG_PRESS,
          RecyclerViewCoordinatesProvider(3, ChildViewCoordinatesProvider(R.id.text_view_for_string_no_data_binding, GeneralLocation.CENTER)),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.ABOVE_RIGHT),
          Press.FINGER
        )
      )

      onView(atPosition(R.id.drag_drop_recycler_View, 2)).check(matches(withText("Item 4")))
      onView(atPosition(R.id.drag_drop_recycler_View, 3)).check(matches(withText("Item 3")))
    }
  }

  private fun safelyWaitUntilIdle() {
    // This must be done off the main thread for Espresso otherwise it deadlocks.
    Espresso.onIdle()
  }
}
