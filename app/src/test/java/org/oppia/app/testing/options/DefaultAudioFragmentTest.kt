package org.oppia.app.testing.options

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.options.AUDIO_LANGUAGE
import org.oppia.app.options.DefaultAudioActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
class DefaultAudioFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testAudioLanguage_changeLanguageToEnglish_changeConfiguration_checkEnglishLanguageIsSelected() { // ktlint-disable max-line-length
    launch<DefaultAudioActivity>(createDefaultAudioActivityIntent("Hindi")).use {
      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.audio_language_recycler_view,
          1,
          R.id.language_radio_button
        )
      ).check(
        matches(
          isChecked()
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testAudioLanguage_clickAudioLanguage_changeAudioLanguageToFrench_checkOptionsFragmentIsUpdatedCorrectly() { // ktlint-disable max-line-length
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.audio_language_recycler_view,
          2,
          R.id.language_radio_button
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  private fun createDefaultAudioActivityIntent(summaryValue: String): Intent {
    return DefaultAudioActivity.createDefaultAudioActivityIntent(
      ApplicationProvider.getApplicationContext(),
      AUDIO_LANGUAGE,
      summaryValue
    )
  }

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      isFromNavigationDrawer
    )
  }
}
