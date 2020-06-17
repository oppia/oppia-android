package org.oppia.app.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.DragViewAction

@RunWith(AndroidJUnit4::class)
class DragDropTestActivityTest {

  @Test
  fun testDragDropTestActivity_dragItem1() {
    launch(DragDropTestActivity::class.java).use {

      safelyWaitUntilIdle()

      onView(withId(R.id.drag_drop_recycler_View)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
          DragViewAction(
            DragViewAction.Drag.LONG_PRESS,
            GeneralLocation.CENTER,
            CoordinatesProvider { floatArrayOf(0F, 0F) },
            Press.FINGER
          )
        ))

    }
  }

  private fun safelyWaitUntilIdle() {
    // This must be done off the main thread for Espresso otherwise it deadlocks.
    Espresso.onIdle()
  }
}
