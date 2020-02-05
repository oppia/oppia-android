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
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
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
import org.oppia.domain.profile.ProfileTestHelper
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

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var context: Context

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
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
    OptionsActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  private fun createOptionActivityIntent(profileId: Int): Intent {
    return OptionsActivity.createOptionsActivity(ApplicationProvider.getApplicationContext(), profileId)
  }

  @Test
  fun testOptionFragment_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
    onView(ViewMatchers.withContentDescription(R.string.drawer_open_content_description)).check(
        ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.options_fragment_placeholder))
        .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
      onView(withId(R.id.options_activity_drawer_layout)).check(ViewAssertions.matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  fun testOptionFragment_clickStoryTextSize_changeTextSizeToLargeSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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

  @Test
  fun testOptionFragment_clickAppLanguage_changeAppLanguageToFrenchSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    launch<OptionsActivity>(createOptionActivityIntent(0)).use {
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
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
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
  @Component(modules = [TestModule::class])
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

