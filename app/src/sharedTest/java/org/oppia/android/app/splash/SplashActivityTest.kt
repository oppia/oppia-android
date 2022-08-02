package org.oppia.android.app.splash

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.test.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.onboarding.testing.FakeExpirationMetaDataRetriever
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.hamcrest.Matcher
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedAutoAndroidTestRunner
import org.oppia.android.util.data.AsyncDataSubscriptionManager

/**
 * Tests for [SplashActivity]. For context on the activity test rule setup see:
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedAutoAndroidTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SplashActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class SplashActivityTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeMetaDataRetriever: FakeExpirationMetaDataRetriever
  @Inject lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler
  // TODO: Still needed?
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Parameter lateinit var firstOpen: String
  @Parameter lateinit var secondOpen: String

  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
  private val firstOpenFlavor by lazy { BuildFlavor.valueOf(firstOpen) }
  private val secondOpenFlavor by lazy { BuildFlavor.valueOf(secondOpen) }

  @Before
  fun setUp() {
    TestModule.buildFlavor = BuildFlavor.BUILD_FLAVOR_UNSPECIFIED
    Intents.init()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testSplashActivity_initialOpen_routesToOnboardingActivity() {
    initializeTestApplication()

    launchSplashActivity {
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileChooserActivity() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()

    launchSplashActivity {
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_beforeExpDate_intentsToOnboardingFlow() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringAfterToday())

    launchSplashActivity {
      // App deprecation is enabled, but this app hasn't yet expired.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_afterExpDate_showsDeprecationDialog() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    launchSplashActivity {
      // The current app is expired.
      onView(withText(R.string.unsupported_app_version_dialog_title))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_afterExpDate_clickOnCloseDialog_endsActivity() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    launchSplashActivity { scenario ->
      onView(withText(R.string.unsupported_app_version_dialog_close_button_text))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.advanceUntilIdle()

      scenario.onActivity { activity ->
        // Closing the dialog should close the activity (and thus, the app).
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testOpenApp_initial_expirationDisabled_afterExpDate_showsOnboardingFlow() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = false)
    setAutoAppExpirationDate(dateStringBeforeToday())

    launchSplashActivity {
      // The app is technically deprecated, but because the deprecation check is disabled the
      // onboarding flow should be shown, instead.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_reopen_onboarded_expirationEnabled_beforeExpDate_routesToProfileChooser() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringAfterToday())

    launchSplashActivity {
      // Reopening the app before it's expired should result in the profile activity showing since
      // the user has already been onboarded.
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_reopen_onboarded_expirationEnabled_afterExpDate_showsToDeprecationDialog() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    launchSplashActivity {
      // Reopening the app after it expires should prevent further access.
      onView(withText(R.string.unsupported_app_version_dialog_title))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_englishLocale_initializesLocaleHandlerWithEnglishContext() {
    initializeTestApplication()
    forceDefaultLocale(Locale.ENGLISH)

    launchSplashActivity {
      // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
      // that the correct display locale is defined per the system locale.
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
      assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
      assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
      assertThat(context.hasFallbackLanguageDefinition()).isFalse()
      assertThat(context.regionDefinition.region).isEqualTo(OppiaRegion.REGION_UNSPECIFIED)
      assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("")
      assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_arabicLocale_initializesLocaleHandlerWithArabicContext() {
    initializeTestApplication()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)

    launchSplashActivity {
      // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
      // that the correct display locale is defined per the system locale.
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(ARABIC)
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_brazilianPortugueseLocale_initializesLocaleHandlerPortugueseContext() {
    initializeTestApplication()
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)

    launchSplashActivity {
      // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
      // that the correct display locale is defined per the system locale.
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    }
  }

  @Test
  fun testSplashActivity_unsupportedLocale_initializesLocaleHandlerWithUnspecifiedLanguage() {
    initializeTestApplication()
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)

    launchSplashActivity {
      // Verify that the context is the default state (due to the unsupported locale).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val languageDefinition = displayLocale.localeContext.languageDefinition
      assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
      assertThat(languageDefinition.minAndroidSdkVersion).isEqualTo(1)
      assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("tr-TR")
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_initializationFailure_initializesLocaleHandlerWithDefaultContext() {
    corruptCacheFile()
    initializeTestApplication()

    launchSplashActivity {
      // Verify that the context is the default state (due to the unsupported locale).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
      assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
      assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
      assertThat(context.hasFallbackLanguageDefinition()).isFalse()
      assertThat(context.regionDefinition.region).isEqualTo(OppiaRegion.UNITED_STATES)
      assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("US")
      assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
    }
  }

  @Test
  fun testSplashActivity_initializationFailure_routesToOnboardingActivity() {
    corruptCacheFile()
    initializeTestApplication()

    launchSplashActivity {
      // Verify that an initialization failure leads to the onboarding activity by default.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_hasCorrectActivityLabel() {
    initializeTestApplication()

    launchSplashActivity { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        assertThat(title).isEqualTo(context.getString(R.string.app_name))
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA"),
    Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA"),
    Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA"),
    Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  )
  fun testSplashActivity_newUser_betaFlavorTransitions_showsBetaNotice() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivity {
      onDialogView(withText(R.string.beta_notice_dialog_title)).check(matches(isDisplayed()))
      onDialogView(withId(R.id.beta_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  @RunParameterized(
    Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA"),
    Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA"),
    Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA"),
    Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  )
  fun testSplashActivity_newUser_betaFlavorTransitions_closeNotice_routesToOnboardingFlow() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivity {
      // Close the notice.
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the onboarding flow after seeing the beta notice.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  @RunParameterized(
    Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA"),
    Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA"),
    Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA"),
    Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  )
  fun testSplashActivity_newUser_betaFlavorTransitions_doNotShowAgain_routesToOnboardingFlow() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivity {
      // Close the notice.
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the onboarding flow after seeing the beta notice.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  // TODO: Add new tests.
  //
  //
  //
  // testSplashActivity_newUser_dismissBetaNotice_reopenApp_doesNotShowNotice
  // testSplashActivity_newUser_dismissBetaNotice_retriggerNotice_showsBetaNotice
  // testSplashActivity_newUser_dismissBetaNoticeForever_retriggerNotice_doesNotShowNotice

  // testSplashActivity_onboarded_gaFlavorTransitions_showsGaUpgradeNotice
  // testSplashActivity_onboarded_gaFlavorTransitions_closeNotice_routesToProfileChooser
  // testSplashActivity_onboarded_gaFlavorTransitions_doNotShowAgain_routesToProfileChooser
  // testSplashActivity_onboarded_dismissGaNotice_reopenApp_doesNotShowNotice
  // testSplashActivity_onboarded_dismissGaNotice_retriggerNotice_showsGaNotice
  // testSplashActivity_onboarded_dismissGaNoticeForever_retriggerNotice_doesNotShowNotice

  // testSplashActivity_newUser_ignoredFlavorTransitions_routesToOnboardingFlow
  // testSplashActivity_onboarded_ignoredFlavorTransitions_routesToProfileChooser
  // testSplashActivity_appDeprecated_allFlavorTransitions_showsDeprecationNotice

  // (Wait ones are Robo-only)
  // testSplashActivity_onboarded_devFlavor_showDevText
  // testSplashActivity_onboarded_alphaFlavor_showAlphaText
  // testSplashActivity_onboarded_betaFlavor_showBetaText
  // testSplashActivity_onboarded_testingFlavor_doesNotWaitToStart
  // testSplashActivity_onboarded_devFlavor_doesNotWaitToStart
  // testSplashActivity_onboarded_alphaFlavor_waitsTwoSecondsToStart
  // testSplashActivity_onboarded_betaFlavor_waitsTwoSecondsToStart
  // testSplashActivity_onboarded_gaFlavor_doesNotWaitToStart

  private fun simulateAppAlreadyOpened() {
    println("@@@@@ root application: ${ApplicationProvider.getApplicationContext<Context>()}")
    runInNewTestApplication {
      println("@@@@@ create monitor; current context: $this")
      println("@@@@@ expected test context: $asdfContext, app context: ${asdfContext.applicationContext}")
      println("@@@@@ separate mgr: $asdf")
      println("@@@@@ app controller: $appStartupStateController")
      val monitor = monitorFactory.createMonitor(appStartupStateController.getAppStartupState())
      println("@@@@@ wait for execution")
      testCoroutineDispatchers.advanceUntilIdle()
      println("@@@@@ finished wait")
      monitor.ensureNextResultIsSuccess()
    }
  }

  private fun simulateAppAlreadyOnboarded() {
    // Simulate the app was already onboarded by creating an isolated onboarding flow controller and
    // saving the onboarding status on the system before the activity is opened. Note that this has
    // to be done in an isolated test application since the test application of this class shares
    // state with production code under test. The isolated test application must be created through
    // Instrumentation to ensure it's properly attached.
    runInNewTestApplication {
      appStartupStateController.markOnboardingFlowCompleted()
      testCoroutineDispatchers.advanceUntilIdle()
    }
  }

  private fun runInNewTestApplication(block: TestApplication.() -> Unit) {
    val newApplication = Instrumentation.newApplication(
      TestApplication::class.java,
      InstrumentationRegistry.getInstrumentation().targetContext
    ) as TestApplication
    newApplication.testCoroutineDispatchers.registerIdlingResource()
    newApplication.block()
    newApplication.testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun simulateAppAlreadyOpenedWithFlavor(buildFlavor: BuildFlavor) {
    TestModule.buildFlavor = buildFlavor
    simulateAppAlreadyOpened()
  }

  private fun simulateAppAlreadyOnboardedWithFlavor(buildFlavor: BuildFlavor) {
    TestModule.buildFlavor = buildFlavor
    simulateAppAlreadyOnboarded()
  }

  private fun initializeTestApplication() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
    setAutoAppExpirationEnabled(enabled = false) // Default to disabled.
  }

  private fun initializeTestApplicationWithFlavor(buildFlavor: BuildFlavor) {
    TestModule.buildFlavor = buildFlavor
    initializeTestApplication()
  }

  private fun launchSplashActivity(testBlock: (ActivityScenario<SplashActivity>) -> Unit) {
    val openFromLauncher = Intent(context, SplashActivity::class.java).also {
      it.action = Intent.ACTION_MAIN
      it.addCategory(Intent.CATEGORY_LAUNCHER)
    }
    ActivityScenario.launch<SplashActivity>(openFromLauncher).also {
      testCoroutineDispatchers.advanceUntilIdle()
    }.use(testBlock)
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

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun corruptCacheFile() {
    // Statically retrieve the application context since injection may not have yet occurred.
    val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    File(applicationContext.filesDir, "on_boarding_flow.cache").writeText("broken")
  }

  @Module
  class TestModule {
    companion object {
      var buildFlavor = BuildFlavor.BUILD_FLAVOR_UNSPECIFIED
    }

    @Provides
    fun provideTestingBuildFlavor(): BuildFlavor = buildFlavor
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class, PlatformParameterModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getAppStartupStateController(): AppStartupStateController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getMonitorFactory(): DataProviderTestMonitor.Factory

    // TODO: Remove.
    fun getAsdf(): AsyncDataSubscriptionManager
    fun getContext(): Context

    fun inject(splashActivityTest: SplashActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSplashActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    val appStartupStateController by lazy { component.getAppStartupStateController() }
    val testCoroutineDispatchers by lazy { component.getTestCoroutineDispatchers() }
    val monitorFactory by lazy { component.getMonitorFactory() }
    val asdf by lazy { component.getAsdf() }
    val asdfContext by lazy { component.getContext() }

    fun inject(splashActivityTest: SplashActivityTest) {
      component.inject(splashActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")

    private fun onDialogView(matcher: Matcher<View>) = onView(matcher).inRoot(isDialog())
  }
}
