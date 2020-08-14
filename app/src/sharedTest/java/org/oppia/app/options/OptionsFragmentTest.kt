package org.oppia.app.options

import android.app.Activity
import android.app.Application
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OptionsFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOptionsFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @get:Rule
  var optionActivityTestRule: ActivityTestRule<OptionsActivity> = ActivityTestRule(
    OptionsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

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

  @Test
  fun testOptionsFragment_parentIsExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(createOptionActivityIntent(0, false)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testOptionsFragment_parentIsNotExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  fun testOptionFragment_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.options_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.options_activity_drawer_layout)).check(matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  fun testOptionsFragment_storyTextSize_testOnActivityResult() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      val resultDataIntent = Intent()
      resultDataIntent.putExtra(KEY_MESSAGE_STORY_TEXT_SIZE, "Large")
      val activityResult = ActivityResult(Activity.RESULT_OK, resultDataIntent)

      val activityMonitor = getInstrumentation().addMonitor(
        StoryTextSizeActivity::class.java.name,
        activityResult,
        true
      )

      it.onActivity { activity ->
        activity.startActivityForResult(
          createStoryTextSizeActivityIntent("Small"),
          REQUEST_CODE_TEXT_SIZE
        )
      }

      getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3)

      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Large"))
      )
    }
  }

  @Test
  fun testOptionsFragment_audioLanguage_testOnActivityResult() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      val resultDataIntent = Intent()
      resultDataIntent.putExtra(KEY_MESSAGE_AUDIO_LANGUAGE, "French")
      val activityResult = ActivityResult(Activity.RESULT_OK, resultDataIntent)

      val activityMonitor = getInstrumentation().addMonitor(
        DefaultAudioActivity::class.java.name,
        activityResult,
        true
      )

      it.onActivity { activity ->
        activity.startActivityForResult(
          createDefaultAudioActivityIntent("Hindi"),
          REQUEST_CODE_AUDIO_LANGUAGE
        )
      }

      getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3)

      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageToFrenchSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.language_recycler_view,
          1, R.id.language_radio_button
        )
      ).perform(
        click()
      )
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageToFrench_changeConfiguration_selectedLanguageIsFrench() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.language_recycler_view,
          1, R.id.language_radio_button
        )
      ).perform(
        click()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageHindiSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            2,
            click()
          )
        )
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_text_view
        )
      ).check(
        matches(withText("Hindi"))
      )
    }
  }

  @Test
  fun testOptionFragment_changeConfiguration_checkAppLanguageIsEnglish() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  private fun createStoryTextSizeActivityIntent(summaryValue: String): Intent {
    return StoryTextSizeActivity.createStoryTextSizeActivityIntent(
      ApplicationProvider.getApplicationContext(),
      STORY_TEXT_SIZE,
      summaryValue
    )
  }

  private fun createDefaultAudioActivityIntent(summaryValue: String): Intent {
    return DefaultAudioActivity.createDefaultAudioActivityIntent(
      ApplicationProvider.getApplicationContext(),
      AUDIO_LANGUAGE,
      summaryValue
    )
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(optionsFragmentTest: OptionsFragmentTest)
  }
}
