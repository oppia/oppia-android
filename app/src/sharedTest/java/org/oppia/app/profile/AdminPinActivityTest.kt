package org.oppia.app.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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

@RunWith(AndroidJUnit4::class)
class AdminPinActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper

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

  @Test
  fun testAdminPinActivity_inputPin_inputConfirmPin_clickSubmit_checkOpensAddProfileActivity() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(click())
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickSubmit_checkPinLengthError() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText(context.getString(R.string.admin_pin_error_pin_length))))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickSubmit_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("45"), closeSoftKeyboard())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_checkConfirmWrongError() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("1234"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText(context.getString(R.string.admin_pin_error_pin_confirm_wrong))))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("1234"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(typeText("5"), closeSoftKeyboard())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputConfirmPin_clickSubmit_checkOpensAddProfileActivity() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(),click())
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputShortPin_clickSubmit_checkPinLengthError() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText(context.getString(R.string.admin_pin_error_pin_length))))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputShortPin_clickSubmit_inputPin_checkErrorIsCleared() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("45"), closeSoftKeyboard())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickSubmit_checkConfirmWrongError() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(),typeText("1234"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText(context.getString(R.string.admin_pin_error_pin_confirm_wrong))))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickSubmit_inputConfirmPin_checkErrorIsCleared() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(),typeText("1234"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(),click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(),typeText("5"), closeSoftKeyboard())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_configurationChange_checkConfirmWrongError(){
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context,0,-10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("12345"), closeSoftKeyboard())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(scrollTo(),typeText("54321"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(matches(withText(R.string.admin_pin_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickSubmit_configurationChange_checkPinLengthError() {
    ActivityScenario.launch<AdminPinActivity>(AdminPinActivity.createAdminPinActivityIntent(context, 0, -10710042)).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(typeText("123"), closeSoftKeyboard())
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(matches(withText(R.string.admin_pin_error_pin_length)))
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerAdminPinActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    fun inject(adminPinActivityTest: AdminPinActivityTest)
  }
}
