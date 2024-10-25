package org.oppia.android.domain.onboarding

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode.FLAVOR_NOTICE_MODE_UNSPECIFIED
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode.NO_NOTICE
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode.SHOW_BETA_NOTICE
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode.SHOW_UPGRADE_TO_GENERAL_AVAILABILITY_NOTICE
import org.oppia.android.app.model.AppStartupState.StartupMode.APP_IS_DEPRECATED
import org.oppia.android.app.model.AppStartupState.StartupMode.OPTIONAL_UPDATE_AVAILABLE
import org.oppia.android.app.model.AppStartupState.StartupMode.OS_IS_DEPRECATED
import org.oppia.android.app.model.AppStartupState.StartupMode.USER_IS_ONBOARDED
import org.oppia.android.app.model.AppStartupState.StartupMode.USER_NOT_YET_ONBOARDED
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.appDeprecationResponse
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.enableAppAndOsDeprecation
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.forcedAppUpdateVersion
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.lowestApiLevel
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.optionalAppUpdateVersion
import org.oppia.android.domain.onboarding.AppStartupStateControllerTest.TestModule.Companion.osDeprecationResponse
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.FORCED_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.util.platformparameter.OPTIONAL_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.system.OppiaClockModule
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppStartupStateController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(application = AppStartupStateControllerTest.TestApplication::class)
class AppStartupStateControllerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var appStartupStateController: AppStartupStateController
  @Inject lateinit var platformParameterController: PlatformParameterController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Parameter lateinit var initialFlavorName: String

  // TODO(#3792): Remove this usage of Locale (probably by introducing a test utility in the locale
  //  package to generate these strings).
  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  @Before
  fun setUp() {
    TestModule.buildFlavor = BuildFlavor.BUILD_FLAVOR_UNSPECIFIED
  }

  @Test
  fun testController_providesInitialState_indicatesUserHasNotOnboardedTheApp() {
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testControllerObserver_observedAfterSettingAppOnboarded_providesState_userDidNotOnboardApp() {
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState()

    appStartupStateController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()

    // The result should not indicate that the user onboarded the app because markUserOnboardedApp
    // does not notify observers of the change.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testController_afterSettingAppOnboarded_logsCompletedOnboardingEvent() {
    setUpDefaultTestApplicationComponent()
    appStartupStateController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event.priority).isEqualTo(EventLog.Priority.OPTIONAL)
    assertThat(event.context.activityContextCase)
      .isEqualTo(EventLog.Context.ActivityContextCase.COMPLETE_APP_ONBOARDING)
  }

  @Test
  fun testController_settingAppOnboarded_observedNewController_userOnboardedApp() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState()

    // The user should be considered onboarded since a new DataProvider instance was observed after
    // marking the user as onboarded.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testController_onboardedApp_cleared_observeNewController_userDidNotOnboardApp() {
    // Simulate the previous app already having completed onboarding, then cleared.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()

      val onboardingFlowStore = testComponent.getCacheFactory().create(
        "on_boarding_flow",
        OnboardingState.getDefaultInstance()
      )
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
      // Clear, then recreate the controller.
      onboardingFlowStore.clearCacheAsync()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState()

    // The app should be considered not yet onboarded since the previous history was cleared.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_beforeDeprecationDate_appNotDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_onDeprecationDate_appIsDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_afterDeprecationDate_appIsDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationDisabled_afterDeprecationDate_appIsNotDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = false, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowNotDone_deprecationEnabled_beforeDepDate_appNotDeprecated() {
    executeInPreviousAppInstance { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringAfterToday()
      )
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowNotDone_deprecationEnabled_afterDepDate_appIsDeprecated() {
    executeInPreviousAppInstance { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringBeforeToday()
      )
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowCompleted_depEnabled_beforeDepDate_appNotDeprecated() {
    executeInPreviousAppInstance { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringAfterToday()
      )

      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    // The user should be considered onboarded, but the app is not yet deprecated.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowCompleted_deprecationEnabled_afterDepDate_appIsDeprecated() {
    executeInPreviousAppInstance { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringBeforeToday()
      )

      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    // Despite the user completing the onboarding flow, the app is still deprecated.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.startupMode).isEqualTo(APP_IS_DEPRECATED)
  }

  /* Tests to verify that beta & no notices are shown at the expected times. */

  @Test
  fun testController_initialState_testingBuild_showsUnspecifiedNotice() {
    TestModule.buildFlavor = BuildFlavor.TESTING
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Testing mode is specially handled as it's generally not expected to be encountered (but is
    // still possible).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(FLAVOR_NOTICE_MODE_UNSPECIFIED)
  }

  @Test
  fun testController_initialState_developerBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.DEVELOPER
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_initialState_alphaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.ALPHA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_initialState_betaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // No notice is shown here since the 'prior' version is beta, and the notice is only shown if
    // the build flavor changes for the user.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_initialState_gaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_appDeprecated_testingBuild_showsUnspecifiedNotice() {
    TestModule.buildFlavor = BuildFlavor.TESTING
    setUpDefaultTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    // Testing mode is specially handled as it's generally not expected to be encountered (but is
    // still possible).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(FLAVOR_NOTICE_MODE_UNSPECIFIED)
  }

  @Test
  fun testController_appDeprecated_developerBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.DEVELOPER
    setUpDefaultTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_appDeprecated_alphaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.ALPHA
    setUpDefaultTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_appDeprecated_betaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    // The beta notice is not shown in cases when the app is deprecated (since there's no point in
    // showing it; the user can't actually use the app).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_appDeprecated_gaBuild_showNoNotice() {
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_testingBuild_showsUnspecifiedNotice() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.TESTING
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Testing mode is specially handled as it's generally not expected to be encountered (but is
    // still possible).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(FLAVOR_NOTICE_MODE_UNSPECIFIED)
  }

  @Test
  fun testController_userOnboarded_developerBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.DEVELOPER
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_alphaBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.ALPHA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_betaBuild_showBetaNotice() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Beta is shown when using beta mode.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(SHOW_BETA_NOTICE)
  }

  @Test
  fun testController_userOnboarded_gaBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  /* Tests to verify that changing from one build flavor to another can cause notices to show. */

  @Test
  fun testController_userOnboarded_changeToTestingBuild_showsUnspecifiedNotice() {
    // Simulate the previous app already having completed onboarding in a non-testing build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.DEVELOPER
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.TESTING
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(FLAVOR_NOTICE_MODE_UNSPECIFIED)
  }

  @Test
  fun testController_userOnboarded_changeToDevBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding in a non-dev build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.TESTING
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.DEVELOPER
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToAlphaBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding in a non-alpha build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.DEVELOPER
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.ALPHA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToBetaBuild_fromAlpha_showBetaNotice() {
    // Simulate the previous app already having completed onboarding in an alpha build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.ALPHA
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Changing from alpha to beta should result in the notice showing.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(SHOW_BETA_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToBetaBuild_fromBetaAgain_showNoNotice() {
    // Simulate the previous app already having completed onboarding in a beta build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.BETA
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // The beta notice was shown on the last run; don't show it again.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToBetaBuild_fromGa_showBetaNotice() {
    // Simulate the previous app already having completed onboarding in a generally available build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Changing from GA to beta should result in the notice showing.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(SHOW_BETA_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToGaBuild_fromDeveloperBuild_showNoNotice() {
    // Simulate the previous app already having completed onboarding in an developer-only build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.DEVELOPER
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // The GA upgrade notice is only shown when changing from alpha or beta.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToGaBuild_fromAlpha_showGaNotice() {
    // Simulate the previous app already having completed onboarding in an alpha build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.ALPHA
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Changing from alpha to GA should result in the GA upgrade notice showing.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(SHOW_UPGRADE_TO_GENERAL_AVAILABILITY_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToGaBuild_fromBeta_showGaNotice() {
    // Simulate the previous app already having completed onboarding in a beta build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.BETA
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Changing from beta to GA should result in the GA upgrade notice showing.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(SHOW_UPGRADE_TO_GENERAL_AVAILABILITY_NOTICE)
  }

  @Test
  fun testController_userOnboarded_changeToGaBuild_fromGaAgain_showNoNotice() {
    // Simulate the previous app already having completed onboarding in a generally available build.
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // The GA upgrade notice is only shown when changing from beta.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  /* Tests to verify that notices can be permanently dismissed. */

  @Test
  fun testController_dismissBetaNoticePermanently_scenariosWithoutBetaNotice_showNoNotice() {
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.BETA
      testComponent.getAppStartupStateController().apply {
        markOnboardingFlowCompleted()
        // While this is technically impossible for this exact configuration, it could be done by
        // permanently dismissing during an earlier time when the notice was shown before the user
        // returned to the beta flavor of the app.
        dismissBetaNoticesPermanently()
      }
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // No notice is shown in this case (beta -> beta).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  @Iteration("testing_to_beta", "initialFlavorName=TESTING")
  @Iteration("dev_to_beta", "initialFlavorName=DEVELOPER")
  @Iteration("alpha_to_beta", "initialFlavorName=ALPHA")
  @Iteration("ga_to_beta", "initialFlavorName=GENERAL_AVAILABILITY")
  fun testController_dismissBetaNoticePermanently_scenariosWhenBetaNoticeDoesShow_showNoNotice() {
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.valueOf(initialFlavorName)
      testComponent.getAppStartupStateController().apply {
        markOnboardingFlowCompleted()
        dismissBetaNoticesPermanently()
      }
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.BETA
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Despite a notice normally showing in this circumstance, it doesn't here since the beta notice
    // was permanently disabled.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  @Iteration("testing_to_ga", "initialFlavorName=TESTING")
  @Iteration("dev_to_ga", "initialFlavorName=DEVELOPER")
  fun testController_dismissGaNoticePermanently_scenariosWhenGaNoticeDoesNotShow_showNoNotice() {
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.valueOf(initialFlavorName)
      testComponent.getAppStartupStateController().apply {
        markOnboardingFlowCompleted()
        dismissGaUpgradeNoticesPermanently()
      }
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // A notice does not show in these circumstances (though, it wouldn't be expected to regardless
    // of whether the GA notice was permanently disabled).
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  @Iteration("alpha_to_ga", "initialFlavorName=ALPHA")
  @Iteration("beta_to_ga", "initialFlavorName=BETA")
  fun testController_dismissGaNoticePermanently_scenariosWhenGaNoticeDoesShow_showNoNotice() {
    executeInPreviousAppInstance { testComponent ->
      TestModule.buildFlavor = BuildFlavor.valueOf(initialFlavorName)
      testComponent.getAppStartupStateController().apply {
        markOnboardingFlowCompleted()
        dismissGaUpgradeNoticesPermanently()
      }
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    TestModule.buildFlavor = BuildFlavor.GENERAL_AVAILABILITY
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState()

    // Despite a notice normally showing in this circumstance, it doesn't here since the GA upgrade
    // notice was permanently disabled.
    val mode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(mode.buildFlavorNoticeMode).isEqualTo(NO_NOTICE)
  }

  @Test
  fun testController_appAndOsDeprecationEnabled_initialLaunch_startupModeIsUserNotOnboarded() {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        listOf(enableAppAndOsDeprecation)
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpDefaultTestApplicationComponent()

    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())
    testCoroutineDispatchers.runCurrent()

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testController_appAndOsDeprecationEnabled_userIsOnboarded_returnsUserOnboardedStartupMode() {
    setUpTestApplicationWithAppAndOSDeprecationEnabled()

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  fun testController_osIsDeprecated_returnsOsDeprecatedStartupMode() {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      platformParameterToEnable = lowestApiLevel
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(OS_IS_DEPRECATED)
  }

  @Test
  fun testController_osIsDeprecated_previousResponseExists_returnsUserOnboardedStartupMode() {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      previousResponses = listOf(osDeprecationResponse),
      platformParameterToEnable = lowestApiLevel
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  fun testController_optionalUpdateAvailable_returnsOptionalUpdateStartupMode() {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      platformParameterToEnable = optionalAppUpdateVersion
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(OPTIONAL_UPDATE_AVAILABLE)
  }

  @Test
  fun testController_optionalUpdateAvailable_previousResponseExists_returnsUserOnboardedStartupMode
  () {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      previousResponses = listOf(appDeprecationResponse),
      platformParameterToEnable = optionalAppUpdateVersion
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  fun testController_forcedUpdateAvailable_returnsAppDeprecatedStartupMode() {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      platformParameterToEnable = forcedAppUpdateVersion
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testController_forcedUpdateAvailable_previousResponseExists_returnsUserOnboardedStartupMode
  () {
    setUpTestApplicationWithAppAndOSDeprecationEnabled(
      previousResponses = listOf(appDeprecationResponse),
      platformParameterToEnable = forcedAppUpdateVersion
    )

    val appStartupState = appStartupStateController.getAppStartupState()

    val startupMode = monitorFactory.waitForNextSuccessfulResult(appStartupState)
    assertThat(startupMode.startupMode).isEqualTo(USER_IS_ONBOARDED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpDefaultTestApplicationComponent() {
    setUpTestApplicationComponent()

    // By default, set up the application to never expire.
    setUpOppiaApplication(expirationEnabled = false, expDate = "9999-12-31")
  }

  private fun setUpTestApplicationWithAppAndOSDeprecationEnabled(
    previousResponses: List<DeprecationResponse> = emptyList(),
    platformParameterToEnable: PlatformParameter? = null
  ) {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()

      previousResponses.forEach {
        testComponent.getDeprecationController().saveDeprecationResponse(it)
        testComponent.getTestCoroutineDispatchers().runCurrent()
      }

      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        platformParameterToEnable?.let { listOf(it, enableAppAndOsDeprecation) }
          ?: listOf(enableAppAndOsDeprecation)
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestApplicationComponent()

    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * Creates a separate test application component and executes the specified block. This should be
   * called before [setUpTestApplicationComponent] to avoid undefined behavior in production code.
   * This can be used to simulate arranging state in a "prior" run of the app.
   *
   * Note that only dependencies fetched from the specified [TestApplicationComponent] should be
   * used, not any class-level injected dependencies.
   */
  private fun executeInPreviousAppInstance(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(
      DaggerAppStartupStateControllerTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
    )
  }

  /** Returns a date string occurring before today. */
  private fun dateStringBeforeToday(): String {
    return computeDateString(Instant.now() - Duration.ofDays(1))
  }

  private fun dateStringForToday(): String {
    return computeDateString(Instant.now())
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

  private fun setUpOppiaApplication(expirationEnabled: Boolean, expDate: String) {
    setUpOppiaApplicationForContext(context, expirationEnabled, expDate)
  }

  private fun setUpOppiaApplicationForContext(
    context: Context,
    expirationEnabled: Boolean,
    expDate: String
  ) {
    val packageManager = shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setName("Oppia")
        .build()
    applicationInfo.metaData = Bundle()
    applicationInfo.metaData.putBoolean("automatic_app_expiration_enabled", expirationEnabled)
    applicationInfo.metaData.putString("expiration_date", expDate)
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageManager.installPackage(packageInfo)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    companion object {
      var buildFlavor = BuildFlavor.BUILD_FLAVOR_UNSPECIFIED

      val lowestApiLevel: PlatformParameter = PlatformParameter.newBuilder()
        .setName(LOWEST_SUPPORTED_API_LEVEL)
        .setInteger(Int.MAX_VALUE)
        .setSyncStatus(PlatformParameter.SyncStatus.SYNCED_FROM_SERVER)
        .build()

      val optionalAppUpdateVersion: PlatformParameter = PlatformParameter.newBuilder()
        .setName(OPTIONAL_APP_UPDATE_VERSION_CODE)
        .setInteger(Int.MAX_VALUE)
        .setSyncStatus(PlatformParameter.SyncStatus.SYNCED_FROM_SERVER)
        .build()

      val forcedAppUpdateVersion: PlatformParameter = PlatformParameter.newBuilder()
        .setName(FORCED_APP_UPDATE_VERSION_CODE)
        .setInteger(Int.MAX_VALUE)
        .setSyncStatus(PlatformParameter.SyncStatus.SYNCED_FROM_SERVER)
        .build()

      val enableAppAndOsDeprecation: PlatformParameter = PlatformParameter.newBuilder()
        .setName(APP_AND_OS_DEPRECATION)
        .setBoolean(true)
        .setSyncStatus(PlatformParameter.SyncStatus.SYNCED_FROM_SERVER)
        .build()

      val osDeprecationResponse: DeprecationResponse = DeprecationResponse.newBuilder()
        .setDeprecationNoticeType(DeprecationNoticeType.OS_DEPRECATION)
        .setDeprecatedVersion(Int.MAX_VALUE)
        .build()

      val appDeprecationResponse: DeprecationResponse = DeprecationResponse.newBuilder()
        .setDeprecationNoticeType(DeprecationNoticeType.APP_DEPRECATION)
        .setDeprecatedVersion(Int.MAX_VALUE)
        .build()
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
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

    @Provides
    fun provideTestingBuildFlavor(): BuildFlavor = buildFlavor
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      LogStorageModule::class, RobolectricModule::class,
      TestModule::class, TestDispatcherModule::class, TestLogReportingModule::class,
      NetworkConnectionUtilDebugModule::class,
      OppiaClockModule::class, LocaleProdModule::class,
      ExpirationMetaDataRetrieverModule::class, // Use real implementation to test closer to prod.
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, AssetModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getAppStartupStateController(): AppStartupStateController

    fun getCacheFactory(): PersistentCacheStore.Factory

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getContext(): Context

    fun getPlatformParameterController(): PlatformParameterController

    fun getDeprecationController(): DeprecationController

    fun inject(appStartupStateControllerTest: AppStartupStateControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppStartupStateControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appStartupStateControllerTest: AppStartupStateControllerTest) {
      component.inject(appStartupStateControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
