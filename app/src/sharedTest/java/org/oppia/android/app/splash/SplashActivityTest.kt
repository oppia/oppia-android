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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.NIGERIAN_PIDGIN
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.onboarding.testing.FakeExpirationMetaDataRetriever
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedAutoAndroidTestRunner
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

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

  @Inject
  lateinit var context: Context
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject
  lateinit var fakeMetaDataRetriever: FakeExpirationMetaDataRetriever
  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler
  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject
  lateinit var appStartupStateController: AppStartupStateController

  @Parameter
  lateinit var firstOpen: String
  @Parameter
  lateinit var secondOpen: String

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

    launchSplashActivityFully {
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_secondOpen_routesToChooseProfileChooserActivity() {
    simulateAppAlreadyOnboarded()
    initializeTestApplication()

    launchSplashActivityFully {
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_beforeExpDate_intentsToOnboardingFlow() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringAfterToday())

    launchSplashActivityFully {
      // App deprecation is enabled, but this app hasn't yet expired.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testOpenApp_initial_expirationEnabled_afterExpDate_showsDeprecationDialog() {
    initializeTestApplication()
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    launchSplashActivityFully {
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

    launchSplashActivityFully { scenario ->
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

    launchSplashActivityFully {
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

    launchSplashActivityFully {
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

    launchSplashActivityFully {
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

    launchSplashActivityFully {
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

    launchSplashActivityFully {
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

    launchSplashActivityFully {
      // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
      // that the correct display locale is defined per the system locale.
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_nigerianPidginLocale_initializesLocaleHandlerNaijaContext() {
    initializeTestApplication()
    forceDefaultLocale(NIGERIAN_PIDGIN_LOCALE)

    launchSplashActivityFully {
      // Verify that the locale is initialized (i.e. getDisplayLocale doesn't throw an exception) &
      // that the correct display locale is defined per the system locale.
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val context = displayLocale.localeContext
      assertThat(context.languageDefinition.language).isEqualTo(NIGERIAN_PIDGIN)
    }
  }

  @Test
  fun testSplashActivity_unsupportedLocale_initializesLocaleHandlerWithUnspecifiedLanguage() {
    initializeTestApplication()
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)

    launchSplashActivityFully {
      // Verify that the context is the default state (due to the unsupported locale).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val languageDefinition = displayLocale.localeContext.languageDefinition
      assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
      assertThat(languageDefinition.minAndroidSdkVersion).isEqualTo(1)
      assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("tr-TR")
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testSplashActivity_initializationFailure_initializesLocaleHandlerWithDefaultContext() {
    corruptCacheFile()
    initializeTestApplication()

    launchSplashActivityFully {
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

    launchSplashActivityFully {
      // Verify that an initialization failure leads to the onboarding activity by default.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_hasCorrectActivityLabel() {
    initializeTestApplication()

    launchSplashActivityFully { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        assertThat(title).isEqualTo(context.getString(R.string.app_name))
      }
    }
  }

  @Test
  fun testSplashActivity_newUser_firstTimeOpeningBetaFlavor_doesNotShowBetaNotice() {
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)

    launchSplashActivityFully {
      // Verify that the beta notice does not open (since there wasn't a version change).
      onView(withId(R.id.beta_notice_dialog_message)).check(doesNotExist())
    }
  }

  @Test
  @Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA")
  @Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA")
  @Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA")
  @Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  fun testSplashActivity_newUser_betaFlavorTransitions_showsBetaNotice() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      onDialogView(withText(R.string.beta_notice_dialog_title)).check(matches(isDisplayed()))
      onDialogView(withId(R.id.beta_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA")
  @Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA")
  @Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA")
  @Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  fun testSplashActivity_newUser_betaFlavorTransitions_closeNotice_routesToOnboardingFlow() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // Close the notice.
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the onboarding flow after seeing the beta notice.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  @Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA")
  @Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA")
  @Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA")
  @Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  fun testSplashActivity_newUser_betaFlavorTransitions_doNotShowAgain_routesToOnboardingFlow() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // Close the notice after selecting to never show it again.
      onDialogView(withId(R.id.beta_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the onboarding flow after seeing the beta notice.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_newUser_dismissBetaNotice_reopenApp_doesNotShowNotice() {
    // Open the app in beta notice mode, then dismiss the notice.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)
    launchSplashActivityFully {
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // Note this is a different "recreation" than other tests since the same instrumentation
    // process needs to be preserved for Espresso to work correctly.
    recreateExistingApplication()

    launchSplashActivityFully {
      // The user should be routed to the onboarding flow after seeing the beta notice.
      onView(withId(R.id.beta_notice_dialog_message)).check(doesNotExist())
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_newUser_dismissBetaNotice_retriggerNotice_showsBetaNotice() {
    // Open the app in beta notice mode, then dismiss the notice.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)
    launchSplashActivityFully {
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // "Retrigger" the notice by switching flavors again, then "recreate" the existing application
    // so that new states can be observed.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    recreateExistingApplicationWithFlavor(BuildFlavor.BETA)

    launchSplashActivityFully {
      // The user should see the beta notice again despite dismissing it since the beta notice
      // condition again occurred.
      onDialogView(withText(R.string.beta_notice_dialog_title)).check(matches(isDisplayed()))
      onDialogView(withId(R.id.beta_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSplashActivity_newUser_dismissBetaNoticeForever_retriggerNotice_doesNotShowNotice() {
    // Open the app in beta notice mode, then dismiss the notice permanently.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)
    launchSplashActivityFully {
      onDialogView(withId(R.id.beta_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // "Retrigger" the notice by switching flavors again, then "recreate" the existing application
    // so that new states can be observed.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    recreateExistingApplicationWithFlavor(BuildFlavor.BETA)

    launchSplashActivityFully {
      // The user should not see the beta notice again even though they changed flavors since they
      // opted to permanently disable the notice.
      onView(withId(R.id.beta_notice_dialog_message)).check(doesNotExist())
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_newUser_firstTimeOpeningGaFlavor_doesNotShowGaUpgradeNotice() {
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    launchSplashActivityFully {
      // Verify that the GA notice does not open (since there wasn't an upgrade).
      onView(withId(R.id.ga_update_notice_dialog_message)).check(doesNotExist())
    }
  }

  @Test
  @Iteration("alpha_to_ga", "firstOpen=ALPHA", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("beta_to_ga", "firstOpen=BETA", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_onboarded_gaFlavorTransitions_showsGaUpgradeNotice() {
    simulateAppAlreadyOnboardedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      onDialogView(withText(R.string.general_availability_notice_dialog_title))
        .check(matches(isDisplayed()))
      onDialogView(withId(R.id.ga_update_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Iteration("alpha_to_ga", "firstOpen=ALPHA", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("beta_to_ga", "firstOpen=BETA", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_onboarded_gaFlavorTransitions_closeNotice_routesToProfileChooser() {
    simulateAppAlreadyOnboardedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // Close the notice.
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the profile chooser after seeing the GA upgrade notice.
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @Iteration("alpha_to_ga", "firstOpen=ALPHA", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("beta_to_ga", "firstOpen=BETA", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_onboarded_gaFlavorTransitions_doNotShowAgain_routesToProfileChooser() {
    simulateAppAlreadyOnboardedWithFlavor(firstOpenFlavor)
    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // Close the notice after selecting to never show it again.
      onDialogView(withId(R.id.ga_update_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      // The user should be routed to the profile chooser after seeing the GA upgrade notice.
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_onboarded_dismissGaNotice_reopenApp_doesNotShowNotice() {
    // Open the app in GA upgrade mode, then dismiss the notice.
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.BETA)
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)
    launchSplashActivityFully {
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // Note this is a different "recreation" than other tests since the same instrumentation
    // process needs to be preserved for Espresso to work correctly.
    recreateExistingApplication()

    launchSplashActivityFully {
      // The user should be routed to the profile chooser after seeing the GA upgrade notice.
      onView(withId(R.id.ga_update_notice_dialog_message)).check(doesNotExist())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_onboarded_dismissGaNotice_retriggerNotice_showsGaNotice() {
    // Open the app in GA upgrade mode, then dismiss the notice.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)
    launchSplashActivityFully {
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // "Retrigger" the notice by switching flavors again, then "recreate" the existing application
    // so that new states can be observed.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    recreateExistingApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    launchSplashActivityFully {
      // The user should see the GA upgrade notice again despite dismissing it since the notice
      // condition again occurred.
      onDialogView(withText(R.string.general_availability_notice_dialog_title))
        .check(matches(isDisplayed()))
      onDialogView(withId(R.id.ga_update_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSplashActivity_onboarded_dismissGaNoticeForever_retriggerNotice_doesNotShowNotice() {
    // Open the app in GA upgrade mode, then dismiss the notice permanently.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.BETA)
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)
    launchSplashActivityFully {
      onDialogView(withId(R.id.ga_update_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // "Retrigger" the notice by switching flavors again, then "recreate" the existing application
    // so that new states can be observed.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    recreateExistingApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    launchSplashActivityFully {
      // The user should not see the GA upgrade notice again even though they changed flavors since
      // they opted to permanently disable the notice.
      onView(withId(R.id.ga_update_notice_dialog_message)).check(doesNotExist())
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  fun testSplashActivity_newUser_betaNoticeConditionsThenGa_showsGaNotice() {
    // Simulate a beta notice first.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.BETA)

    // Then simulate the GA upgrade notice.
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // The GA upgrade notice should be the one to show since it's more recent.
    launchSplashActivityFully {
      onDialogView(withText(R.string.general_availability_notice_dialog_title))
        .check(matches(isDisplayed()))
      onDialogView(withId(R.id.ga_update_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSplashActivity_newUser_betaNoticeConditionsThenGa_gaDisabled_showsNoNotice() {
    // First, disable the GA notice, then trigger a beta notice.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)
    launchSplashActivityFully {
      onDialogView(withId(R.id.ga_update_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.general_availability_notice_dialog_close_button_text))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
    }
    reopenAppWithNewFlavor(BuildFlavor.BETA)

    // Then simulate the GA upgrade notice.
    reopenAppWithNewFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // No notice should show since the GA upgrade notice would normally show, but it's been
    // permanently disabled.
    launchSplashActivityFully {
      onView(withId(R.id.beta_notice_dialog_message)).check(doesNotExist())
      onView(withId(R.id.ga_update_notice_dialog_message)).check(doesNotExist())
    }
  }

  @Test
  fun testSplashActivity_newUser_gaNoticeConditionsThenBeta_showsBetaNotice() {
    // Simulate a GA upgrade notice first.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.BETA)
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // Then simulate the beta notice.
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)

    // The beta notice should be the one to show since it's more recent.
    launchSplashActivityFully {
      onDialogView(withText(R.string.beta_notice_dialog_title)).check(matches(isDisplayed()))
      onDialogView(withId(R.id.beta_notice_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSplashActivity_newUser_gaNoticeConditionsThenBeta_betaDisabled_showsNoNotice() {
    // First, disable the beta notice, then trigger a GA upgrade notice.
    simulateAppAlreadyOpenedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)
    launchSplashActivityFully {
      onDialogView(withId(R.id.beta_notice_dialog_preference_checkbox)).perform(click())
      onDialogView(withText(R.string.beta_notice_dialog_close_button_text)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }
    reopenAppWithNewFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // Then simulate the beta notice.
    reopenAppWithNewFlavor(BuildFlavor.BETA)

    // No notice should show since the beta notice would normally show, but it's been permanently
    // disabled.
    launchSplashActivityFully {
      onView(withId(R.id.beta_notice_dialog_message)).check(doesNotExist())
      onView(withId(R.id.ga_update_notice_dialog_message)).check(doesNotExist())
    }
  }

  @Test
  @Iteration("testing_to_testing", "firstOpen=TESTING", "secondOpen=TESTING")
  @Iteration("testing_to_dev", "firstOpen=TESTING", "secondOpen=DEVELOPER")
  @Iteration("testing_to_alpha", "firstOpen=TESTING", "secondOpen=ALPHA")
  @Iteration("testing_to_ga", "firstOpen=TESTING", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("dev_to_testing", "firstOpen=DEVELOPER", "secondOpen=TESTING")
  @Iteration("dev_to_dev", "firstOpen=DEVELOPER", "secondOpen=DEVELOPER")
  @Iteration("dev_to_alpha", "firstOpen=DEVELOPER", "secondOpen=ALPHA")
  @Iteration("dev_to_ga", "firstOpen=DEVELOPER", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("alpha_to_testing", "firstOpen=ALPHA", "secondOpen=TESTING")
  @Iteration("alpha_to_dev", "firstOpen=ALPHA", "secondOpen=DEVELOPER")
  @Iteration("alpha_to_alpha", "firstOpen=ALPHA", "secondOpen=ALPHA")
  @Iteration("beta_to_testing", "firstOpen=BETA", "secondOpen=TESTING")
  @Iteration("beta_to_dev", "firstOpen=BETA", "secondOpen=DEVELOPER")
  @Iteration("beta_to_alpha", "firstOpen=BETA", "secondOpen=ALPHA")
  @Iteration("beta_to_beta", "firstOpen=BETA", "secondOpen=BETA")
  @Iteration("ga_to_testing", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=TESTING")
  @Iteration("ga_to_dev", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=DEVELOPER")
  @Iteration("ga_to_alpha", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=ALPHA")
  @Iteration("ga_to_ga", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_newUser_ignoredFlavorTransitions_routesToOnboardingFlow() {
    simulateAppAlreadyOpenedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // The user should be immediately routed to the onboarding flow since this flavor transition
      // does not trigger a notice.
      intended(hasComponent(OnboardingActivity::class.java.name))
    }
  }

  @Test
  @Iteration("testing_to_testing", "firstOpen=TESTING", "secondOpen=TESTING")
  @Iteration("testing_to_dev", "firstOpen=TESTING", "secondOpen=DEVELOPER")
  @Iteration("testing_to_alpha", "firstOpen=TESTING", "secondOpen=ALPHA")
  @Iteration("testing_to_ga", "firstOpen=TESTING", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("dev_to_testing", "firstOpen=DEVELOPER", "secondOpen=TESTING")
  @Iteration("dev_to_dev", "firstOpen=DEVELOPER", "secondOpen=DEVELOPER")
  @Iteration("dev_to_alpha", "firstOpen=DEVELOPER", "secondOpen=ALPHA")
  @Iteration("dev_to_ga", "firstOpen=DEVELOPER", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("alpha_to_testing", "firstOpen=ALPHA", "secondOpen=TESTING")
  @Iteration("alpha_to_dev", "firstOpen=ALPHA", "secondOpen=DEVELOPER")
  @Iteration("alpha_to_alpha", "firstOpen=ALPHA", "secondOpen=ALPHA")
  @Iteration("beta_to_testing", "firstOpen=BETA", "secondOpen=TESTING")
  @Iteration("beta_to_dev", "firstOpen=BETA", "secondOpen=DEVELOPER")
  @Iteration("beta_to_alpha", "firstOpen=BETA", "secondOpen=ALPHA")
  @Iteration("beta_to_beta", "firstOpen=BETA", "secondOpen=BETA")
  @Iteration("ga_to_testing", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=TESTING")
  @Iteration("ga_to_dev", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=DEVELOPER")
  @Iteration("ga_to_alpha", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=ALPHA")
  @Iteration("ga_to_ga", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_onboarded_ignoredFlavorTransitions_routesToProfileChooser() {
    simulateAppAlreadyOnboardedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)

    launchSplashActivityFully {
      // The user should be immediately routed to the profile chooser since this flavor transition
      // does not trigger a notice.
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @Iteration("testing_to_testing", "firstOpen=TESTING", "secondOpen=TESTING")
  @Iteration("testing_to_dev", "firstOpen=TESTING", "secondOpen=DEVELOPER")
  @Iteration("testing_to_alpha", "firstOpen=TESTING", "secondOpen=ALPHA")
  @Iteration("testing_to_beta", "firstOpen=TESTING", "secondOpen=BETA")
  @Iteration("testing_to_ga", "firstOpen=TESTING", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("dev_to_testing", "firstOpen=DEVELOPER", "secondOpen=TESTING")
  @Iteration("dev_to_dev", "firstOpen=DEVELOPER", "secondOpen=DEVELOPER")
  @Iteration("dev_to_alpha", "firstOpen=DEVELOPER", "secondOpen=ALPHA")
  @Iteration("dev_to_beta", "firstOpen=DEVELOPER", "secondOpen=BETA")
  @Iteration("dev_to_ga", "firstOpen=DEVELOPER", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("alpha_to_testing", "firstOpen=ALPHA", "secondOpen=TESTING")
  @Iteration("alpha_to_dev", "firstOpen=ALPHA", "secondOpen=DEVELOPER")
  @Iteration("alpha_to_alpha", "firstOpen=ALPHA", "secondOpen=ALPHA")
  @Iteration("alpha_to_beta", "firstOpen=ALPHA", "secondOpen=BETA")
  @Iteration("alpha_to_ga", "firstOpen=ALPHA", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("beta_to_testing", "firstOpen=BETA", "secondOpen=TESTING")
  @Iteration("beta_to_dev", "firstOpen=BETA", "secondOpen=DEVELOPER")
  @Iteration("beta_to_alpha", "firstOpen=BETA", "secondOpen=ALPHA")
  @Iteration("beta_to_beta", "firstOpen=BETA", "secondOpen=BETA")
  @Iteration("beta_to_ga", "firstOpen=BETA", "secondOpen=GENERAL_AVAILABILITY")
  @Iteration("ga_to_testing", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=TESTING")
  @Iteration("ga_to_dev", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=DEVELOPER")
  @Iteration("ga_to_alpha", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=ALPHA")
  @Iteration("ga_to_beta", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=BETA")
  @Iteration("ga_to_ga", "firstOpen=GENERAL_AVAILABILITY", "secondOpen=GENERAL_AVAILABILITY")
  fun testSplashActivity_appDeprecated_allFlavorTransitions_showsDeprecationNotice() {
    simulateAppAlreadyOnboardedWithFlavor(firstOpenFlavor)

    initializeTestApplicationWithFlavor(secondOpenFlavor)
    setAutoAppExpirationEnabled(enabled = true)
    setAutoAppExpirationDate(dateStringBeforeToday())

    // The current app is expired, so the deprecation notice should show regardless of the build
    // flavor notices that would normally show.
    launchSplashActivityFully {
      onView(withText(R.string.unsupported_app_version_dialog_title))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSplashActivity_onboarded_testingFlavor_showsNoFlavorText() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.TESTING)

    initializeTestApplicationWithFlavor(BuildFlavor.TESTING)

    // No label should show for this version of the app since it's meant to simulate the GA flavor.
    launchSplashActivityFully {
      onView(withId(R.id.build_flavor_label)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testSplashActivity_onboarded_devFlavor_showsDevFlavorText() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.DEVELOPER)

    initializeTestApplicationWithFlavor(BuildFlavor.DEVELOPER)

    // The developer label should be showing.
    launchSplashActivityFully {
      onView(withId(R.id.build_flavor_label)).check(matches(isDisplayed()))
      onView(withId(R.id.build_flavor_label))
        .check(matches(withText(R.string.splash_screen_developer_label)))
    }
  }

  @Test
  fun testSplashActivity_onboarded_alphaFlavor_showsAlphaFlavorText() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.ALPHA)

    initializeTestApplicationWithFlavor(BuildFlavor.ALPHA)

    // The alpha label should be showing.
    launchSplashActivityFully {
      onView(withId(R.id.build_flavor_label)).check(matches(isDisplayed()))
      onView(withId(R.id.build_flavor_label))
        .check(matches(withText(R.string.splash_screen_alpha_label)))
    }
  }

  @Test
  fun testSplashActivity_onboarded_betaFlavor_showsBetaFlavorText() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.BETA)

    initializeTestApplicationWithFlavor(BuildFlavor.BETA)

    // The beta label should be showing.
    launchSplashActivityFully {
      onView(withId(R.id.build_flavor_label)).check(matches(isDisplayed()))
      onView(withId(R.id.build_flavor_label))
        .check(matches(withText(R.string.splash_screen_beta_label)))
    }
  }

  @Test
  fun testSplashActivity_onboarded_generalAvailabilityFlavor_showsNoFlavorText() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // No label should show for this version of the app.
    launchSplashActivityFully {
      onView(withId(R.id.build_flavor_label)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_testingFlavor_doesNotWaitToStart() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.TESTING)
    initializeTestApplicationWithFlavor(BuildFlavor.TESTING)

    // The profile chooser opens immediately for the testing flavor since it has no delay.
    launchSplashActivityPartially {
      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_devFlavor_doesNotWaitToStart() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.DEVELOPER)
    initializeTestApplicationWithFlavor(BuildFlavor.DEVELOPER)

    // The profile chooser opens immediately for the developer flavor since it has no delay.
    launchSplashActivityPartially {
      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_alphaFlavor_doNotWait_doesNotStart() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.ALPHA)

    // Nothing opens without waiting for the alpha startup notice to finish.
    launchSplashActivityPartially {
      testCoroutineDispatchers.runCurrent()

      Intents.assertNoUnverifiedIntents()
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_alphaFlavor_waitTwoSeconds_intentsToProfileChooser() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.ALPHA)
    initializeTestApplicationWithFlavor(BuildFlavor.ALPHA)

    // The profile chooser should appear after the 2 seconds wait for the alpha splash screen.
    launchSplashActivityPartially {
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(2))

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_betaFlavor_doNotWait_doesNotStart() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.BETA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)

    // Nothing opens without waiting for the beta startup notice to finish.
    launchSplashActivityPartially {
      testCoroutineDispatchers.runCurrent()

      Intents.assertNoUnverifiedIntents()
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_betaFlavor_waitTwoSeconds_intentsToProfileChooser() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.BETA)
    initializeTestApplicationWithFlavor(BuildFlavor.BETA)

    // The profile chooser should appear after the 2 seconds wait for the beta splash screen.
    launchSplashActivityPartially {
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(2))

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testSplashActivity_onboarded_gaFlavor_doesNotWaitToStart() {
    simulateAppAlreadyOnboardedWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)
    initializeTestApplicationWithFlavor(BuildFlavor.GENERAL_AVAILABILITY)

    // The profile chooser opens immediately for the GA flavor since it has no delay.
    launchSplashActivityPartially {
      testCoroutineDispatchers.runCurrent()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  private fun simulateAppAlreadyOpened() {
    runInNewTestApplication {
      val monitor = monitorFactory.createMonitor(appStartupStateController.getAppStartupState())
      testCoroutineDispatchers.advanceUntilIdle()
      monitor.ensureNextResultIsSuccess()
    }
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    initializeTestApplication()
    launchSplashActivity { activityScenario ->
      testCoroutineDispatchers.advanceUntilIdle()
      activityScenario.onActivity { activity ->
        val currentScreenName = activity.intent.extractCurrentAppScreenName()
        assertThat(currentScreenName).isEqualTo(ScreenName.SPLASH_ACTIVITY)
      }
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

  /**
   * Allows the existing [TestApplication] to be treated as though it was recreated.
   *
   * This should only be used when Espresso needs to run operations in a "previous" application
   * instance (since Espresso can only use the current instrumentation and not a new one). For all
   * other cases, prefer [runInNewTestApplication] since it actually creates an entirely new
   * application in isolation, and is closer to a separate app instance than what this method
   * produces.
   */
  private fun recreateExistingApplication() {
    testCoroutineDispatchers.unregisterIdlingResource()
    ApplicationProvider.getApplicationContext<TestApplication>().recreateDaggerGraph()
    initializeTestApplication()

    // Reset any intents previously recorded.
    Intents.release()
    Intents.init()
  }

  private fun recreateExistingApplicationWithFlavor(buildFlavor: BuildFlavor) {
    TestModule.buildFlavor = buildFlavor
    recreateExistingApplication()
  }

  /** See [recreateExistingApplication] for when to use this. */
  private fun reopenAppWithNewFlavor(buildFlavor: BuildFlavor) {
    TestModule.buildFlavor = buildFlavor
    recreateExistingApplication()
    val monitor = monitorFactory.createMonitor(appStartupStateController.getAppStartupState())
    testCoroutineDispatchers.advanceUntilIdle()
    monitor.ensureNextResultIsSuccess()
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

  /**
   * Launches [SplashActivity] and waits for all initial time-based operations to complete before
   * executing [testBlock].
   */
  private fun launchSplashActivityFully(testBlock: (ActivityScenario<SplashActivity>) -> Unit) {
    launchSplashActivity {
      testCoroutineDispatchers.advanceUntilIdle()
      testBlock(it)
    }
  }

  /** Launches [SplashActivity] and waits for initial time-based operations to start. */
  private fun launchSplashActivityPartially(testBlock: (ActivityScenario<SplashActivity>) -> Unit) {
    launchSplashActivity {
      testCoroutineDispatchers.runCurrent()
      testBlock(it)
    }
  }

  private fun launchSplashActivity(testBlock: (ActivityScenario<SplashActivity>) -> Unit) {
    val openFromLauncher = Intent(context, SplashActivity::class.java).also {
      it.action = Intent.ACTION_MAIN
      it.addCategory(Intent.CATEGORY_LAUNCHER)
    }
    ActivityScenario.launch<SplashActivity>(openFromLauncher).use(testBlock)
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
      ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
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

    fun inject(splashActivityTest: SplashActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private var component: TestApplicationComponent = createTestApplicationComponent()

    val appStartupStateController: AppStartupStateController
      get() = component.getAppStartupStateController()
    val testCoroutineDispatchers: TestCoroutineDispatchers
      get() = component.getTestCoroutineDispatchers()
    val monitorFactory: DataProviderTestMonitor.Factory
      get() = component.getMonitorFactory()

    fun inject(splashActivityTest: SplashActivityTest) {
      component.inject(splashActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component

    fun recreateDaggerGraph() {
      component = createTestApplicationComponent()
    }

    private fun createTestApplicationComponent(): TestApplicationComponent {
      return DaggerSplashActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }
  }

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val NIGERIAN_PIDGIN_LOCALE = Locale("pcm", "NG")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")

    private fun onDialogView(matcher: Matcher<View>) = onView(matcher).inRoot(isDialog())
  }
}
