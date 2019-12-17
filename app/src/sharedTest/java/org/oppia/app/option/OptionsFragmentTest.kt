package org.oppia.app.option

import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
class OptionsFragmentTest {

  @get:Rule
  var optionActivityTestRule: ActivityTestRule<OptionActivity> = ActivityTestRule(
    OptionActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeSettingsSuccessfully() {
    ActivityScenario.launch(OptionActivity::class.java).use {
      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(withId(R.id.seekBar)).perform(clickSeekBar(5))
    }
  }

  private fun clickSeekBar(position: Int): ViewAction {
    return GeneralClickAction(Tap.SINGLE, object: CoordinatesProvider {
      override fun calculateCoordinates(view: View?): FloatArray {
        val seekBar = view as SeekBar
        val screenPos = IntArray(2)
        seekBar.getLocationInWindow(screenPos)
        val trueWith = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight

        val percentagePos = (position.toFloat() / seekBar.max)
        val screenX = trueWith * percentagePos + screenPos[0] + seekBar.paddingLeft
        val screenY = seekBar.height/2f + screenPos[1]
        val coordinates = FloatArray(2)
        coordinates[0] = screenX
        coordinates[1] = screenY
        return coordinates
      }
    }, Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0)
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageSettingsSuccessfully() {
    ActivityScenario.launch(OptionActivity::class.java).use {
      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            2,
            click()
          )
        )

      onView(withId(R.id.language_recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            2,
            click()
          )
        )
    }
  }

  @Test
  fun testOptionFragment_clickDefaultAudioLanguange_changeDefaultAudioLanguageSuccessfully() {
    ActivityScenario.launch(OptionActivity::class.java).use {
      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            3,
            click()
          )
        )

      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            3,
            click()
          )
        )
    }
  }

}
