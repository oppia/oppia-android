package org.oppia.app.testing.options

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
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.options.APP_LANGUAGE
import org.oppia.app.options.AppLanguageActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.options.STORY_TEXT_SIZE
import org.oppia.app.options.StoryTextSizeActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView

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
  fun clickStoryTextSize_checkSendingTheCorrectIntent() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      intended(hasComponent(StoryTextSizeActivity::class.java.name))
      intended(
        hasExtra(
          StoryTextSizeActivity.KEY_STORY_TEXT_SIZE_PREFERENCE_TITLE,
          STORY_TEXT_SIZE
        )
      )
    }
  }

  @Test
  fun testAppLanguage_clickAppLanguage_checkSendingTheCorrectIntent() {
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
