package org.oppia.app.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class ProfileChooserFragmentTest {

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var context: Context

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    GlobalScope.launch(Dispatchers.Main) {
      profileTestHelper.initializeProfiles()
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileChooserFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testProfileChooserFragment_initializeProfiles_checkProfilesAreShown() {
    ActivityScenario.launch(ProfileActivity::class.java).use {
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_name_text)).check(matches(withText("Sean")))
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_is_admin_text)).check(matches(withText(context.getString(R.string.profile_chooser_admin))))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_name_text)).check(matches(withText("Ben")))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_is_admin_text)).check(matches(not(isDisplayed())))
      onView(atPositionOnView(R.id.profile_recycler_view, 2, R.id.add_profile_text)).check(matches(withText(context.getString(R.string.profile_chooser_add))))
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testProfileChooserFragment_addManyProfiles_checkProfilesSortedAndNoAddProfile() {
    GlobalScope.launch(Dispatchers.Main) {
      profileTestHelper.addMoreProfiles(8)
    }
    ActivityScenario.launch(ProfileActivity::class.java).use {
      onView(atPositionOnView(R.id.profile_recycler_view, 0, R.id.profile_name_text)).check(matches(withText("Sean")))
      onView(atPositionOnView(R.id.profile_recycler_view, 1, R.id.profile_name_text)).check(matches(withText("A")))
      onView(atPositionOnView(R.id.profile_recycler_view, 2, R.id.profile_name_text)).check(matches(withText("B")))
      onView(atPositionOnView(R.id.profile_recycler_view, 3, R.id.profile_name_text)).check(matches(withText("Ben")))
      onView(atPositionOnView(R.id.profile_recycler_view, 4, R.id.profile_name_text)).check(matches(withText("C")))
      onView(atPositionOnView(R.id.profile_recycler_view, 5, R.id.profile_name_text)).check(matches(withText("D")))
      onView(atPositionOnView(R.id.profile_recycler_view, 6, R.id.profile_name_text)).check(matches(withText("E")))
      onView(atPositionOnView(R.id.profile_recycler_view, 7, R.id.profile_name_text)).check(matches(withText("F")))
      onView(atPositionOnView(R.id.profile_recycler_view, 8, R.id.profile_name_text)).check(matches(withText("G")))
      onView(atPositionOnView(R.id.profile_recycler_view, 9, R.id.profile_name_text)).check(matches(withText("H")))
    }
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

    fun inject(profileChooserFragmentTest: ProfileChooserFragmentTest)
  }
}
