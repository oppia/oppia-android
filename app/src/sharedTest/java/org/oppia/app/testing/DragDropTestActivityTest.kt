package org.oppia.app.testing

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
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
import org.oppia.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.app.recyclerview.OnItemDragListener
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.RecyclerViewCoordinatesProvider

@RunWith(AndroidJUnit4::class)
class DragDropTestActivityTest {

  @Test
  fun testDragDropTestActivity_dragItem0ToPosition1() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          DragViewAction.Dragger(),
          RecyclerViewCoordinatesProvider(
            0,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(1, CustomGeneralLocation.UNDER_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 0)).check(matches(withText("Item 2")))
      onView(atPosition(R.id.drag_drop_recycler_view, 1)).check(matches(withText("Item 1")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem1ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          DragViewAction.Dragger(),
          RecyclerViewCoordinatesProvider(
            1,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.UNDER_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 1)).check(matches(withText("Item 3")))
      onView(atPosition(R.id.drag_drop_recycler_view, 2)).check(matches(withText("Item 2")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem3ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      onView(withId(R.id.drag_drop_recycler_view))
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          DragViewAction.Dragger(),
          RecyclerViewCoordinatesProvider(
            3,
            ChildViewCoordinatesProvider(
              R.id.text_view_for_string_no_data_binding,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.ABOVE_RIGHT),
          Press.FINGER
        )
      )
      onView(atPosition(R.id.drag_drop_recycler_view, 2)).check(matches(withText("Item 4")))
      onView(atPosition(R.id.drag_drop_recycler_view, 3)).check(matches(withText("Item 3")))
    }
  }

  private fun attachDragDropToActivity(activity: DragDropTestActivity) {
    val recyclerView: RecyclerView = activity.findViewById(R.id.drag_drop_recycler_view)
    val itemTouchHelper = ItemTouchHelper(createDragCallback(activity))
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  private fun createDragCallback(activity: DragDropTestActivity): ItemTouchHelper.Callback {
    return DragAndDropItemFacilitator(activity as OnItemDragListener)
  }

}
