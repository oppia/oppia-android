package org.oppia.app.options

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
class OptionsFragmentTest {

  private lateinit var sharedPref :SharedPreferences

  @get:Rule
  var optionActivityTestRule: ActivityTestRule<OptionsActivity> = ActivityTestRule(
    OptionsActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToLargeSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {

      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(10))
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToMediumSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(5))
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToExtraLargeSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
      onView(withId(androidx.preference.R.id.recycler_view))
        .perform(
          RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(15))
    }
  }

  private fun clickSeekBar(position: Int): ViewAction {
    return GeneralClickAction(Tap.SINGLE, CoordinatesProvider { view ->
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
    }, Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0)
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageToFrenchSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
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
            1,
            click()
          )
        )
    }
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageHindiSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
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
  fun testOptionFragment_clickDefaultAudioLanguage_changeDefaultAudioLanguageToEnglishSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
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
            1,
            click()
          )
        )
    }
  }

  @Test
  fun testOptionFragment_clickDefaultAudioLanguage_changeDefaultAudioLanguageToChineseSuccessfully() {
    ActivityScenario.launch(OptionsActivity::class.java).use {
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
            4,
            click()
          )
        )
    }
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
