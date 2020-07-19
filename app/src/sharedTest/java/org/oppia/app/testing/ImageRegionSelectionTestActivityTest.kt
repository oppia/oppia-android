package org.oppia.app.testing

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.clickAtXY

@RunWith(AndroidJUnit4::class)
class ImageRegionSelectionTestActivityTest {

  @Test
  fun testImageRegionSelectionTestActivity_clickOnRegion3() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3")))).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnRegion3_clickOnRegion2_onlyRegion2IsDisplayed() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3")))).check(
        matches(isDisplayed())
      )

      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.5f, 0.5f)
      )
      onView(allOf(withTagValue(`is`("Region 2")))).check(
        matches(isDisplayed())
      )

      onView(allOf(withTagValue(`is`("Region 3")))).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnDefaultRegion() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.default_selected_region)).perform(
        clickAtXY(0.1f, 0.1f)
      )
      onView(withId(R.id.test_fraction_input_interaction_view)).check(
        matches(isDisplayed())
      )
    }
  }
}
