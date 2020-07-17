package org.oppia.app.splash

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationModule
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.AppStartupStateController
import org.oppia.domain.question.QuestionModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Tests for [SplashActivity]. For context on the activity test rule setup see:
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SplashActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class SplashActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var appStartupStateController: AppStartupStateController
  @InternalCoroutinesApi
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    simulateNewAppInstance()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  // The initialTouchMode enables the activity to be launched in touch mode. The launchActivity is
  // disabled to launch Activity explicitly within each test case.
  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
    SplashActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Test
  fun testSplashActivity_initialOpen_routesToOnboardingActivity() {
    activityTestRule.launchActivity(null)
    intended(hasComponent(OnboardingActivity::class.java.name))
  }

  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileActivity() {
    simulateAppAlreadyOnboarded()
    launch(SplashActivity::class.java).use {
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  private fun simulateNewAppInstance() {
    // Simulate a fresh app install by clearing any potential on-disk caches using an isolated
    // onboarding flow controller.
    setUpTestApplicationComponent()
    testCoroutineDispatchers.runCurrent()
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  private fun simulateAppAlreadyOnboarded() {
    // Simulate the app was already onboarded by creating an isolated onboarding flow controller and
    // saving the onboarding status on the system before the activity is opened.
    setUpTestApplicationComponent()
    appStartupStateController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, NetworkModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class
    ]
  )
  interface TestApplicationComponent: ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(splashActivityTest: SplashActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerSplashActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stateFragmentLocalTest: SplashActivityTest) {
      component.inject(stateFragmentLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
