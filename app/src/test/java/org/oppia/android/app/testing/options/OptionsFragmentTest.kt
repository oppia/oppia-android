package org.oppia.android.app.testing.options

import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.options.APP_LANGUAGE
import org.oppia.android.app.options.AUDIO_LANGUAGE
import org.oppia.android.app.options.AppLanguageActivity
import org.oppia.android.app.options.AppLanguageFragment
import org.oppia.android.app.options.DefaultAudioActivity
import org.oppia.android.app.options.DefaultAudioFragment
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.options.READING_TEXT_SIZE
import org.oppia.android.app.options.ReadingTextSizeActivity
import org.oppia.android.app.options.ReadingTextSizeFragment
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
class OptionsFragmentTest {

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
  fun testOptionsFragment_clickReadingTextSize_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(ReadingTextSizeActivity::class.java.name))
      intended(
        hasExtra(
          ReadingTextSizeActivity.KEY_READING_TEXT_SIZE_PREFERENCE_TITLE,
          READING_TEXT_SIZE
        )
      )
    }
  }

  @Test
  fun testOptionsFragment_clickAppLanguage_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1,
          R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(AppLanguageActivity::class.java.name))
      intended(
        hasExtra(
          AppLanguageActivity.KEY_APP_LANGUAGE_PREFERENCE_TITLE,
          APP_LANGUAGE
        )
      )
    }
  }

  @Test
  fun testOptionsFragment_clickDefaultAudioLanguage_checkSendingTheCorrectIntent() {
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
      intended(hasComponent(DefaultAudioActivity::class.java.name))
      intended(
        hasExtra(
          DefaultAudioActivity.KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE,
          AUDIO_LANGUAGE
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testOptionsFragment_checkInitiallyLoadedFragmentIsReadingTextSizeFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is ReadingTextSizeFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testOptionsFragment_clickReadingTextSize_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is ReadingTextSizeFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testOptionsFragment_clickAppLanguage_checkLoadingTheCorrectFragment() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1,
          R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is AppLanguageFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testOptionsFragment_clickDefaultAudio_checkLoadingTheCorrectFragment() {
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
      it.onActivity { activity ->
        val loadedFragment =
          activity.supportFragmentManager.findFragmentById(R.id.multipane_options_container)
        assertThat(loadedFragment is DefaultAudioFragment).isTrue()
      }
    }
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
