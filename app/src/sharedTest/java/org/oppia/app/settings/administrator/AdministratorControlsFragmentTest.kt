package org.oppia.app.settings.administrator

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.oppia.app.R
import org.oppia.app.settings.profile.ProfileListActivityTest
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton

class AdministratorControlsFragmentTest {

  private lateinit var activityScenario: ActivityScenario<AdministratorControlsActivity>

  @get:Rule
  var activityTestRule: ActivityTestRule<AdministratorControlsActivity> = ActivityTestRule(
    AdministratorControlsActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    activityScenario = launchAdministratorControlsActivityIntent(0)
    Intents.init()
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayGeneralAndAccountSettings() {
    launchAdministratorControlsActivityIntent(0).use {
      onView(withId(R.id.general_text_view)).check(matches(isDisplayed()))
      onView(withText("Edit account")).check(matches(isDisplayed()))
      onView(withId(R.id.administrator_controls_scroll_view)).perform(ViewActions.swipeUp())
      onView(withId(R.id.account_actions_text_view)).check(matches(isDisplayed()))
      onView(withText("Log Out")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayProfileSettings() {
    launchAdministratorControlsActivityIntent(0).use {
      onView(withId(R.id.profile_management_text_view)).check(matches(isDisplayed()))
      onView(withText("Edit profiles")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayAppSettings() {
    launchAdministratorControlsActivityIntent(0).use {
      onView(withId(R.id.administrator_controls_scroll_view)).perform(ViewActions.swipeUp())
      onView(withId(R.id.app_information_text_view)).check(matches(isDisplayed()))
      onView(withText("App Version")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_displayDownloadPermissionsAndSettings() {
    launchAdministratorControlsActivityIntent(0).use {
      onView(withText("Download Permissions")).check(matches(isDisplayed()))
      onView(withId(R.id.topic_update_on_wifi_constraint_layout)).check(matches(isDisplayed()))
      onView(withId(R.id.administrator_controls_scroll_view)).perform(ViewActions.swipeUp())
      onView(withId(R.id.auto_update_topic_constraint_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdministratorControlsFragment_loadFragment_topicUpdateOnWifiSwitchIsNotChecked_autoUpdateTopicSwitchIsChecked() {
    launchAdministratorControlsActivityIntent(0).use {
      onView(withId(R.id.topic_update_on_wifi_switch)).check(matches(not(isChecked())))
      onView(withId(R.id.administrator_controls_scroll_view)).perform(ViewActions.swipeUp())
      onView(withId(R.id.auto_update_topic_switch)).check(matches(isChecked()))
    }
  }

  private fun launchAdministratorControlsActivityIntent(profileId: Int?): ActivityScenario<AdministratorControlsActivity> {
    val intent = AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
    return ActivityScenario.launch(intent)
  }

  @After
  fun tearDown() {
    Intents.release()
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

    fun inject(profileListActivityTest: ProfileListActivityTest)
  }
}