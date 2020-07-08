package org.oppia.app.testing

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

@RunWith(AndroidJUnit4::class)
class ImageRegionSelectionTestActivityTest {

  @Test
  fun testImageRegionSelectionTestActivity_clickonRegion3() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f,0.3f)
      )
      onView(withId(R.id.selected_region_view)).check(
        matches(isDisplayed())
      )
    }
  }

  private fun clickAtXY(pctX: Float, pctY: Float): ViewAction {
    return GeneralClickAction(
      Tap.SINGLE,
      CoordinatesProvider { view ->
        val screenPos = IntArray(2)
        view.getLocationOnScreen(screenPos)
        val w = view.width
        val h = view.height

        val x: Float = w * pctX
        val y: Float = h * pctY

        val screenX = screenPos[0] + x
        val screenY = screenPos[1] + y

        floatArrayOf(screenX, screenY)
      },
      Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0
    )
  }

}
