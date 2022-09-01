package org.oppia.android.app.splash

import android.app.Application
import android.app.Instrumentation
import android.content.Context
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
import dagger.BindsInstance
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
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
import org.oppia.android.app.model.ScreenName
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
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
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
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
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

/**
 * Tests for [SplashActivity]. For context on the activity test rule setup see:
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SplashActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class SplashActivityTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeMetaDataRetriever: FakeExpirationMetaDataRetriever

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

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

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_englishLocale_initializesLocaleHandlerWithEnglishContext() {
    initializeTestApplication()
    forceDefaultLocale(Locale.ENGLISH)

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

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

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_arabicLocale_initializesLocaleHandlerWithArabicContext() {
    initializeTestApplication()
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
    // that the correct display locale is defined per the system locale.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ARABIC)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_brazilianPortugueseLocale_initializesLocaleHandlerPortugueseContext() {
    initializeTestApplication()
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
    // that the correct display locale is defined per the system locale.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testSplashActivity_unsupportedLocale_initializesLocaleHandlerWithUnspecifiedLanguage() {
    initializeTestApplication()
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Verify that the context is the default state (due to the unsupported locale).
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val languageDefinition = displayLocale.localeContext.languageDefinition
    assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("tr-TR")
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_initializationFailure_initializesLocaleHandlerWithDefaultContext() {
    corruptCacheFile()
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

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

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_initializationFailure_logsError() {
    // Simulate a corrupted cache file to trigger an initialization failure.
    corruptCacheFile()
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    val logs = getShadowLogsOnRobolectric()
    assertThat(logs.any { it.contains("Failed to compute initial state") }).isTrue()
  }

  @Test
  fun testSplashActivity_initializationFailure_routesToOnboardingActivity() {
    corruptCacheFile()
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    // Verify that an initialization failure leads to the onboarding activity by default.
    intended(hasComponent(OnboardingActivity::class.java.name))
  }

  @Test
  fun testSplashActivity_hasCorrectActivityLabel() {
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    val title = activityTestRule.activity.title
    assertThat(title).isEqualTo(context.getString(R.string.app_name))
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    initializeTestApplication()

    activityTestRule.launchActivity(null)
    testCoroutineDispatchers.advanceUntilIdle()

    val currentScreenName = activityTestRule.activity.intent.extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.SPLASH_ACTIVITY)
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
    testApplication.getAppStartupStateController().markOnboardingFlowCompleted()
    testApplication.getTestCoroutineDispatchers().advanceUntilIdle()
  }

  private fun initializeTestApplication() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
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

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun getShadowLogsOnRobolectric(): List<String> {
    val shadowLogClass = Class.forName("org.robolectric.shadows.ShadowLog")
    val shadowLogItem = Class.forName("org.robolectric.shadows.ShadowLog\$LogItem")
    val msgField = shadowLogItem.getDeclaredField("msg")
    val logItems = shadowLogClass.getDeclaredMethod("getLogs").invoke(/* obj= */ null) as? List<*>
    return logItems?.map { logItem ->
      msgField.get(logItem) as String
    } ?: listOf()
  }

  private fun corruptCacheFile() {
    // Statically retrieve the application context since injection may not have yet occurred.
    val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    File(applicationContext.filesDir, "on_boarding_flow.cache").writeText("broken")
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
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
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class
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

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")
  }
}
