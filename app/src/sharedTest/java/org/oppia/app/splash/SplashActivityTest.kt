package org.oppia.app.splash

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
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
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.OnboardingFlowController
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [SplashActivity]. For context on the activity test rule setup see:
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = SplashActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
class SplashActivityTest {

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
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
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    intended(hasComponent(OnboardingActivity::class.java.name))
  }

  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileActivity() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    intended(hasComponent(ProfileActivity::class.java.name))
  }

  private fun simulateAppAlreadyOnboarded() {
    // Simulate the app was already onboarded by creating an isolated onboarding flow controller and
    // saving the onboarding status on the system before the activity is opened. Note that this has
    // to be done in an isolated test application since the test application of this class shares
    // state with production code under test. The isolated test application must be created through
    // Instrumentation to ensure it's properly attached.
    val testApplication = Instrumentation.newApplication(
      TestApplication::class.java,
      InstrumentationRegistry.getInstrumentation().targetContext
    ) as TestApplication
    testApplication.getOnboardingFlowController().markOnboardingFlowCompleted()
    testApplication.getTestCoroutineDispatchers().advanceUntilIdle()
  }

  private fun initializeTestApplication() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
    FirebaseApp.initializeApp(context)
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
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getOnboardingFlowController(): OnboardingFlowController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun inject(splashActivityTest: SplashActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerSplashActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(splashActivityTest: SplashActivityTest) {
      component.inject(splashActivityTest)
    }

    fun getOnboardingFlowController() = component.getOnboardingFlowController()

    fun getTestCoroutineDispatchers() = component.getTestCoroutineDispatchers()

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
