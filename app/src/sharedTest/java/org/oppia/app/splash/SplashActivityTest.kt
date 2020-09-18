package org.oppia.app.splash

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.profile.ProfileChooserActivity
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.AppStartupStateController
import org.oppia.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.domain.onboarding.testing.FakeExpirationMetaDataRetriever
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
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
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeMetaDataRetriever: FakeExpirationMetaDataRetriever

  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  private var pid = 0
  private lateinit var process: Process

  @Before
  fun setUp() {
    Intents.init()

    try {
      val filesDir = ApplicationProvider.getApplicationContext<Context>().filesDir
      val externalFiles = ApplicationProvider.getApplicationContext<Context>().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
      println("@@@@@ Saving video to $externalFiles")
//      val process = executeSuperUserCommand("ls")
      process = executeCommand("screenrecord --time-limit 10 $externalFiles/test2.mp4")
      // Hacky way to get PID to send SIGINT: https://stackoverflow.com/a/2951193.
      pid = process.javaClass.getDeclaredField("pid").let { field ->
        field.isAccessible = true
        return@let checkNotNull(field.get(process) as? Int) { "Invalid pid field in process" }
      }
    } catch (t: Throwable) {
      t.printStackTrace()
    }
  }

  private fun executeCommand(command: String): Process {
    return Runtime.getRuntime().exec(command)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()

    println("Finishing screen recording by interrupting process $pid")
//    check(executeCommand("kill -INT $pid").waitFor(10, TimeUnit.SECONDS)) {
//      "Failed to kill screenrecord process"
//    }
    check(process.waitFor(10, TimeUnit.SECONDS)) {
      "Failed to wait for screenrecord process to end"
    }
    check(!process.isAlive) { "Expected screenrecord process to be finished" }
    val errorLines = process.errorStream.bufferedReader().use { it.readLines() }
    val outputLines = process.inputStream.bufferedReader().use { it.readLines() }
    println("Successfully finished screen recording. Errors: $errorLines, output: $outputLines")
  }

  private fun executeSuperUserCommand(command: String): Process {
    // Reference: https://stackoverflow.com/a/20932225.
    val process = Runtime.getRuntime().exec("su")
    process.outputStream.bufferedWriter().apply {
      appendln(command)
      flush()
    }
    return process
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
/*
  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileChooserActivity() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    intended(hasComponent(ProfileChooserActivity::class.java.name))
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_beforeExpDate_intentsToOnboardingFlow() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringAfterToday())

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // App deprecation is enabled, but this app hasn't yet expired.
    intended(hasComponent(OnboardingActivity::class.java.name))
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_afterExpDate_intentsToDeprecationDialog() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // The current app is expired.
    onView(withText(R.string.unsupported_app_version_dialog_title))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_afterExpDate_clickOnCloseDialog_endsActivity() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())
    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    onView(withText(R.string.unsupported_app_version_dialog_close_button_text))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.advanceUntilIdle()

    // Closing the dialog should close the activity (and thus, the app).
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testOpenApp_initial_expirationDisabled_afterExpDate_intentsToOnboardingFlow() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = false)
    setAutoAppExpirationDate(dateStringBeforeToday())

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // The app is technically deprecated, but because the deprecation check is disabled the
    // onboarding flow should be shown, instead.
    intended(hasComponent(OnboardingActivity::class.java.name))
  }

  @Test
  fun testOpenApp_reopen_onboarded_expirationEnabled_beforeExpDate_intentsToProfileChooser() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringAfterToday())

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Reopening the app before it's expired should result in the profile activity showing since the
    // user has already been onboarded.
    intended(hasComponent(ProfileChooserActivity::class.java.name))
  }

  @Test
  fun testOpenApp_reopen_onboarded_expirationEnabled_afterExpDate_intentsToDeprecationDialog() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Reopening the app after it expires should prevent further access.
    onView(withText(R.string.unsupported_app_version_dialog_title))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }
*/
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
    testApplication.getAppStartupStateController().markOnboardingFlowCompleted()
    testApplication.getTestCoroutineDispatchers().advanceUntilIdle()
  }

  private fun initializeTestApplication() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
    FirebaseApp.initializeApp(context)
    setAutoAppExpirationEnabled(enabled = false) // Default to disabled.
  }

  private fun setAutoAppExpirationEnabled(enabled: Boolean) {
    fakeMetaDataRetriever.putMetaDataBoolean("automatic_app_expiration_enabled", enabled)
  }

  private fun setAutoAppExpirationDate(dateString: String) {
    fakeMetaDataRetriever.putMetaDataString("expiration_date", dateString)
  }

  /** Returns a date string occurring before today. */
  private fun dateStringBeforeToday(): String {
    return computeDateString(Instant.now() - Duration.ofDays(1))
  }

  /** Returns a date string occurring after today. */
  private fun dateStringAfterToday(): String {
    return computeDateString(Instant.now() + Duration.ofDays(1))
  }

  private fun computeDateString(instant: Instant): String {
    return computeDateString(Date.from(instant))
  }

  private fun computeDateString(date: Date): String {
    return expirationDateFormat.format(date)
  }

  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getAppStartupStateController(): AppStartupStateController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun inject(splashActivityTest: SplashActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSplashActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(splashActivityTest: SplashActivityTest) {
      component.inject(splashActivityTest)
    }

    fun getAppStartupStateController() = component.getAppStartupStateController()

    fun getTestCoroutineDispatchers() = component.getTestCoroutineDispatchers()

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
