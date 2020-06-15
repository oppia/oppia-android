package org.oppia.app.options

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
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
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
class OptionsFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Before
  @ExperimentalCoroutinesApi
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

  private fun createOptionActivityIntent(profileId: Int): Intent {
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testOptionFragment_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.options_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.options_activity_drawer_layout)).check(matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToLargeSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(10))
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
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
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToLarge_changeConfiguration_checkTextSizeLargeIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(10))
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
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
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToMediumSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(5))
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Medium"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToMedium_changeConfiguration_checkTextSizeMediumIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(5))
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Medium"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToExtraLargeSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(15))
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Extra Large"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToExtraLarge_changeConfiguration_checkTextSizeExtraLargeIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.story_text_size_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.story_text_size_seekBar)).perform(clickSeekBar(15))
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Extra Large"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageToFrenchSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
  fun testOptionFragment_clickDefaultAudioLanguage_changeDefaultAudioLanguageToEnglishSuccessfully() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )

      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun testOptionFragment_checkDefaultAudioLanguage_changeLanguageToEnglish_changeConfiguration_checkEnglishLanguageIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1,
            click()
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("English"))
      )
    }
  }

  @Test
  fun testOptionFragment_checkDefaultAudioLanguage_changeLanguageToChinese_changeConfiguration_checkChineseLanguageIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            4,
            click()
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      swipeUp()
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("Chinese"))
      )
    }
  }

  @Test
  fun testOptionFragment_checkDefaultAudioLanguage_changeLanguageToHindi_changeConfiguration_checkHindiLanguageIsSelected() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            3,
            click()
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("Hindi"))
      )
    }
  }

  @Test
  fun testOptionFragment_clickDefaultAudioLanguage_changeDefaultAudioLanguageToChineseSuccessfully() { // ktlint-disable max-length-line
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_laguage_item_layout
        )
      ).perform(
        click()
      )
      onView(withId(R.id.audio_language_recycler_view))
        .perform(
          actionOnItemAtPosition<RecyclerView.ViewHolder>(
            4,
            click()
          )
        )
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("Chinese"))
      )
    }
  }

  @Test
  fun testOptionFragment_changeConfiguration_checkTextSizeLargeIsSmall() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0, R.id.story_text_size_text_view
        )
      ).check(
        matches(withText("Small"))
      )
    }
  }

  @Test
  fun testOptionFragment_changeConfiguration_checkAppLanguageIsEnglish() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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

  @Test
  fun testOptionFragment_changeConfiguration_checkAudioLanguageIsHindi() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2, R.id.audio_language_text_view
        )
      ).check(
        matches(withText("Hindi"))
      )
    }
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

  @Qualifier
  annotation class TestDispatcher

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
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
