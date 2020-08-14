package org.oppia.app.testing.options

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.options.STORY_TEXT_SIZE
import org.oppia.app.options.StoryTextSizeActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape

@RunWith(AndroidJUnit4::class)
class StoryTextSizeFragmentTest {

  @Test
  fun changeTextSizeToLarge_changeConfiguration_checkTextSizeLargeIsSelected() {
    launch<StoryTextSizeActivity>(createStoryTextSizeActivityIntent("Small")).use {
      onView(withId(R.id.story_text_size_seekBar)).check(matches(seekBarProgress(0)))
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(10))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.story_text_size_seekBar)).check(matches(seekBarProgress(10)))
    }
  }

  private fun createStoryTextSizeActivityIntent(summaryValue: String): Intent {
    return StoryTextSizeActivity.createStoryTextSizeActivityIntent(
      ApplicationProvider.getApplicationContext(),
      STORY_TEXT_SIZE,
      summaryValue
    )
  }

  private fun clickSeekBar(position: Int): ViewAction {
    return GeneralClickAction(
      Tap.SINGLE,
      CoordinatesProvider { view ->
        val seekBar = view as SeekBar
        val screenPos = IntArray(2)
        seekBar.getLocationInWindow(screenPos)
        val trueWith = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight

        val percentagePos = (position.toFloat() / seekBar.max)
        val screenX = trueWith * percentagePos + screenPos[0] + seekBar.paddingLeft
        val screenY = seekBar.height / 2f + screenPos[1]
        val coordinates = FloatArray(2)
        coordinates[0] = screenX
        coordinates[1] = screenY
        coordinates
      },
      Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0
    )
  }

  private fun seekBarProgress(progress: Int): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description?) {
        description?.appendText("SeekBarProgress")
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as SeekBar).progress == progress
      }
    }
  }
}
