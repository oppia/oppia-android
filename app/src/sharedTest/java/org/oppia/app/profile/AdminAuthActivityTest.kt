package org.oppia.app.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
class AdminAuthActivityTest {

  @Inject lateinit var context: Context

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAdminAuthActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testAdminAuthActivity_inputCorrectPassword_opensAddProfileActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(click())
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("4"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_inputAgain_checkErrorIsGone() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("4"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminAuthActivity_buttonState_configurationChanged_buttonStateIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.submit_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testAdminAuthActivity_inputText_configurationChanged_inputTextIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("12345")
        )
      )
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPasswordLandscape_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        -10710042
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
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

    fun inject(adminAuthActivityTest: AdminAuthActivityTest)
  }
}
