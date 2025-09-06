package org.oppia.android.app.devoptions.platformparameters

import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.devoptions.platformparameters.testing.PlatformParametersTestActivity
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.LocalOverridePlatformParameterDatabase
import org.oppia.android.app.model.OverriddenPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.persistence.PersistentCacheStore
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasErrorText
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.AsyncResult
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
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParametersFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PlatformParametersFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class PlatformParametersFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var context: Context
  @Inject lateinit var editTextInputAction: EditTextInputAction

  private companion object {
    private const val TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS = 24
    private const val TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE = false
    private const val TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS = 30
    private const val TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG = true
    private const val REMOTE_DATABASE_NAME = "platform_parameter_and_feature_flag_database"
    private const val LOCAL_OVERRIDE_DATABASE_NAME =
      "local_overridden_platform_parameter_and_feature_flag_database"
    private const val SPLASH_SCREEN_WELCOME_MSG_PARAMETER_NAME = "Splash Screen Welcome Message"
  }

  @Test
  fun testPlatformParametersFragment_verifyRecyclerView_hasCorrectItemCount() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val expectedCount = getEphemeralPlatformParameters().size
      onView(withId(R.id.platform_parameters_recycler_view))
        .check(RecyclerViewMatcher.hasItemCount(count = expectedCount))

      // Note to developers: if you add/remove a platform parameter, please update the
      // expected count.
      onView(withId(R.id.platform_parameters_recycler_view))
        .check(RecyclerViewMatcher.hasItemCount(count = 11))
    }
  }

  @Test
  fun testPlatformParametersFragment_verifyRecyclerViewItems_haveCorrectDetails() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      getEphemeralPlatformParameters().forEachIndexed { index, ephemeralPlatformParameter ->
        scrollToPosition(index)
        verifyPlatformParameterDisplayName(
          position = index,
          expectedDisplayName = getPlatformParameterDisplayName(ephemeralPlatformParameter.id)
        )
        verifyPlatformParameterSyncDetails(
          position = index,
          expectedSyncStatus = getSyncStatusText(ephemeralPlatformParameter.syncStatus)
        )
        verifyPlatformParameterValue(
          position = index,
          expectedValue = ephemeralPlatformParameter.currentValue
        )
      }
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withNoRemoteOrOverride_returnsCorrectDisplayName() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      verifyPlatformParameterDisplayName(
        position = position,
        expectedDisplayName = SPLASH_SCREEN_WELCOME_MSG_PARAMETER_NAME
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withNoRemoteOrOverride_returnsNeverSyncedMessage() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.platform_parameter_never_synced_message)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withNoRemoteOrOverride_returnsDefaultValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = splashScreenWelcomeMsgParameter.currentValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withNoRemoteOrOverride_hasNoBackgroundColor() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_intParam_withNoRemoteOrOverride_returnsDefaultValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val syncUpWorkerParameter = getEphemeralPlatformParameters()[1]

      scrollToPosition(1)
      verifyPlatformParameterValue(
        position = 1,
        expectedValue = syncUpWorkerParameter.currentValue
      )
    }
  }

  @Test
  fun testPlatformParmetersFragment_boolParam_withOnlyRemoteValue_returnsRemoteValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      verifyPlatformParameterValue(
        position = position,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParmetersFragment_boolParam_withOnlyRemoteValue_returnsSyncedFromServerMessage() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      verifyPlatformParameterSyncDetails(
        position = position,
        expectedSyncStatus =
          context.getString(R.string.platform_parameter_synced_from_server_message)
      )
    }
  }

  @Test
  fun testPlatformParmetersFragment_boolParam_withOnlyRemoteValue_hasNoBackgroundColor() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testPlatformParmetersFragment_boolParam_withOnlyRemoteValue_returnsCorrectDisplayName() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      verifyPlatformParameterDisplayName(
        position = position,
        expectedDisplayName = SPLASH_SCREEN_WELCOME_MSG_PARAMETER_NAME
      )
    }
  }

  @Test
  fun testPlatformParmetersFragment_intParam_withOnlyRemoteValue_returnsRemoteValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestIntegerRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSyncUpWorkerTimePeriodPosition()
      scrollToPosition(position)
      verifyPlatformParameterValue(
        position = position,
        expectedValue = PlatformParameterValue.newBuilder()
          .setInteger(TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_onlyOverriddenValue_returnsOverriddenBoolValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      verifyPlatformParameterValue(
        position = position,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_onlyOverriddenValue_returnsCorrectDisplayName() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyPlatformParameterDisplayName(
        position = 0,
        expectedDisplayName = SPLASH_SCREEN_WELCOME_MSG_PARAMETER_NAME
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_onlyOverriddenValue_returnsCurrentlyOverriddenMsg() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyPlatformParameterSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(
          R.string.platform_parameter_currently_overridden_message
        )
      )
    }
  }

  @Test
  fun testPlatfromParametersFragment_boolParam_onlyOverriddenValue_hasYellowBackgroundColor() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_overridden_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_intParam_onlyOverriddenValue_returnsOverriddenIntegerValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestIntegerOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSyncUpWorkerTimePeriodPosition()
      scrollToPosition(position)
      verifyPlatformParameterValue(
        position = position,
        expectedValue = PlatformParameterValue.newBuilder()
          .setInteger(TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_intParam_withOnlyOverrddenValue_alertIconIsVisible() {
    executeInPreviousAppInstance { testComponent ->
      addTestIntegerOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyOverriddenAlertIconIsVisible(0)
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withRemoteAndOverride_returnsOverriddenValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withRemoteAndOverride_returnsOverriddenStatus() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(
          R.string.platform_parameter_currently_overridden_message
        )
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withRemoteAndOverride_hasYellowBackgroundColor() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_overridden_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withRemoteAndOverride_returnsCorrectDisplayName() {
    executeInPreviousAppInstance { testComponent ->
      addTestBooleanRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SPLASH_SCREEN_WELCOME_MESSAGE
      )
      addTestBooleanOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SPLASH_SCREEN_WELCOME_MSG
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterDisplayName(
        position = 0,
        expectedDisplayName = SPLASH_SCREEN_WELCOME_MSG_PARAMETER_NAME
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_intParam_withRemoteAndOverride_returnsOverriddenValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestIntegerRemotePlatformParameterToDatabase(
        testComponent,
        TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      addTestIntegerOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSyncUpWorkerTimePeriodPosition()
      scrollToPosition(position)
      verifyPlatformParameterValue(
        position = position,
        expectedValue = PlatformParameterValue.newBuilder()
          .setInteger(TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_toggleBooleanParameter_updatesValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      verifyPlatformParameterValue(
        position = 0,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(!splashScreenWelcomeMsgParameter.currentValue.boolean)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_toggleBooleanPlatformParameter_configChange_persistsValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyPlatformParameterValue(
        position = 0,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(!splashScreenWelcomeMsgParameter.currentValue.boolean)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyIntegerPlatformParameter_configChange_persistsValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("29"))

      verifyPlatformParameterValue(
        position = 7,
        expectedValue = PlatformParameterValue.newBuilder()
          .setInteger(29)
          .build()
      )

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyPlatformParameterValue(
        position = 7,
        expectedValue = PlatformParameterValue.newBuilder()
          .setInteger(29)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_removeTextFromInputBox_showsInvalidInputError() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(""))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_layout
        )
      ).check(
        matches(
          hasErrorText(context.getString(R.string.platform_parameter_invalid_input_error_msg))
        )
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_invalidValueThenValidInput_clearsErrorMessage() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(""))

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_layout
        )
      ).check(
        matches(
          hasErrorText(
            context.getString(R.string.platform_parameter_invalid_input_error_msg)
          )
        )
      )

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("12"))

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_layout
        )
      ).check(
        matches(
          not(
            hasErrorText(
              context.getString(R.string.platform_parameter_invalid_input_error_msg)
            )
          )
        )
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyIntegerParameter_scrollAndBack_persistsValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 7,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("42"))

      val expectedValue = PlatformParameterValue.newBuilder()
        .setInteger(42)
        .build()

      scrollToPosition(1)

      scrollToPosition(7)
      verifyPlatformParameterValue(
        position = 7,
        expectedValue = expectedValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_toggleBooleanParameter_scrollAndBack_persistsValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val originalValue = getEphemeralPlatformParameters()[0].currentValue.boolean

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      val expectedValue = PlatformParameterValue.newBuilder()
        .setBoolean(!originalValue)
        .build()

      scrollToPosition(8)
      scrollToPosition(0)

      verifyPlatformParameterValue(
        position = 0,
        expectedValue = expectedValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_boolParam_withOverridenParameter_resetButtonIsVisible() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPlatformParametersFragment_intParam_withOverridenParameter_resetButtonIsVisible() {
    executeInPreviousAppInstance { component ->
      addTestIntegerOverriddenPlatformParameterToDatabase(component, 24)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSyncUpWorkerTimePeriodPosition()
      scrollToPosition(position)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.reset_button
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPlatformParametersFragment_withOverride_clickResetButton_resetsParameterToDefaultValue() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(false)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_withOverride_clickResetButton_disablesResetButton() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testPlatformParametersFragment_withOverride_clickResetButton_returnsNeverSyncedMessage() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.platform_parameter_never_synced_message)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_navigateBackWithInvalidInput_displaysAlertDialog() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(1)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 1,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(""))

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.platform_parameter_invalid_input_alert_dialog_title))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPlatformParametersFragment_invalidInputAlert_withValidInput_doesNotShowDialog() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use { _ ->
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(1)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 1,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(""))

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.platform_parameter_invalid_input_alert_dialog_title))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))

      onView(
        withText(
          R.string.platform_parameter_invalid_input_alert_dialog_okay_button
        )
      ).perform(click())

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 1,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("25"))

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.platform_parameter_invalid_input_alert_dialog_title))
        .check(doesNotExist())
    }
  }

  @Test
  fun testPlatformParametersFragment_noParameterModified_saveButtonIsDisabled() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(5)
      onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyAnyParameter_saveButtonIsEnabled() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testPlatformParametersFragment_clickResetButton_saveButtonIsEnabled() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyParameterAndRevert_saveButtonIsDisabled() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyAnyParameter_hasSkyBlueBackgroundColor() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_platform_parameter_modified_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyAnyParameter_configChange_skyBlueColorPersists() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_platform_parameter_modified_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_navigateBackWithParamModified_displaysPendingChangesDialog() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPlatformParametersFragment_clickReset_navigateBack_displaysPendingChangesAlertDialog() {
    executeInPreviousAppInstance { testComponent ->
      addTestIntegerOverriddenPlatformParameterToDatabase(
        testComponent,
        TEST_LOCAL_OVERRIDE_SYNC_UP_WORKER_PERIOD_HOURS
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPlatformParametersFragment_revertParamChange_navigateBack_skipsPendingChangesDialog() {
    setUpTestApplicationComponent()
    launch(PlatformParametersActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val position = getSplashScreenWelcomeMsgPosition()
      scrollToPosition(position)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.app_restart_dialog_title))
        .check(doesNotExist())
    }
  }

  @Test
  fun testPlatformParametersFragment_navigateBackWithNoParamsModified_skipsPendingChangesDialog() {
    setUpTestApplicationComponent()
    launch(PlatformParametersActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_title_text))
        .check(doesNotExist())
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyParameterAndRevert_hasNoBackgroundColor() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyOverriddenParameter_hasSkyBlueBackgroundColor() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_platform_parameter_modified_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_clickResetButton_hasSkyBlueBackgroundColor() {
    executeInPreviousAppInstance { component ->
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      verifyPlatformParameterBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_platform_parameter_modified_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_withNoOverride_alertIconIsNotVisible() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.currently_overridden_alert_icon
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testPlatformParametersFragment_withRemoteValue_alertIconIsNotVisible() {
    executeInPreviousAppInstance { component ->
      addTestBooleanRemotePlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.currently_overridden_alert_icon
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testPlatformParametersFragment_withRemoteAndOverride_clickReset_resetsParameterToRemote() {
    executeInPreviousAppInstance { component ->
      addTestBooleanRemotePlatformParameterToDatabase(component, false)
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = PlatformParameterValue.newBuilder()
          .setBoolean(false)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_withRemoteAndOverride_onReset_showsSyncedWithServerMessage() {
    executeInPreviousAppInstance { component ->
      addTestBooleanRemotePlatformParameterToDatabase(component, false)
      addTestBooleanOverriddenPlatformParameterToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      verifyPlatformParameterSyncDetails(
        position = 0,
        expectedSyncStatus =
          context.getString(R.string.platform_parameter_synced_from_server_message)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyIntegerParameterAndRevert_saveButtonIsDisabled() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val position = getSyncUpWorkerTimePeriodPosition()
      val originalValue = getEphemeralPlatformParameters()[position].currentValue.integer.toString()
      scrollToPosition(position)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("25"))
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(originalValue))
      onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyOverriddenIntegerParameter_hasSkyBlueBackgroundColor() {
    executeInPreviousAppInstance { component ->
      addTestIntegerOverriddenPlatformParameterToDatabase(component, 24)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val position = getSyncUpWorkerTimePeriodPosition()
      scrollToPosition(position)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = position,
          targetViewId = R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("30"))
      verifyPlatformParameterBackgroundColor(
        position = position,
        expectedColor =
          context.getColor(R.color.component_color_platform_parameter_modified_background_color)
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_navigateBackWithParamModified_clickDiscard_discardsChanges() {
    setUpTestApplicationComponent()
    val initialValue = getEphemeralPlatformParameters()[0].currentValue

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_discard_button_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = initialValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_removeTextFromParameterAndFocusChange_retainsOriginalValue() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      it.onActivity { activity ->
        val initialValue = getEphemeralPlatformParameters()[1].currentValue

        testCoroutineDispatchers.runCurrent()
        val recyclerView =
          activity.findViewById<RecyclerView>(R.id.platform_parameters_recycler_view)
        val viewHolder1 = recyclerView.findViewHolderForAdapterPosition(1)
        val editText1 =
          viewHolder1?.itemView?.findViewById<EditText>(R.id.platform_parameter_input_edit_text)

        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 1,
            targetViewId = R.id.platform_parameter_input_edit_text
          )
        ).perform(editTextInputAction.replaceText(""))
        testCoroutineDispatchers.runCurrent()

        val viewHolder2 = recyclerView.findViewHolderForAdapterPosition(2)
        val editText2 =
          viewHolder2?.itemView?.findViewById<EditText>(R.id.platform_parameter_input_edit_text)

        editText1?.performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, Bundle())
        editText2?.performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, Bundle())
        scrollToPosition(1)
        verifyPlatformParameterValue(
          position = 1,
          expectedValue = initialValue
        )
      }
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyParam_navigateBack_clickSave_showRestartDialogExitApp() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException>() {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()

        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())

        pressBack()
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.pending_changes_dialog_save_button_text))
          .inRoot(isDialog())
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
          .perform(click())
      }
    }
    assertThat(exception.message).contains("System.exit()")
  }

  @Test
  fun testPlatformParametersFragment_modifyParam_clickToolbarSave_showsRestartDialogExitApp() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException>() {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()

        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())

        onView(withId(R.id.save_button)).perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
          .perform(click())
      }
    }
    assertThat(exception.message).contains("System.exit()")
  }

  @Test
  fun testPlatformParametersFragment_modifyParamAndSaveOnBackNavigation_persistsChanges() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException>() {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()

        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())

        pressBack()
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.pending_changes_dialog_save_button_text))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
          .perform(click())
        testCoroutineDispatchers.runCurrent()
      }
    }
    assertThat(exception.message).contains("System.exit()")

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val expectedValue = PlatformParameterValue.newBuilder()
        .setBoolean(!getEphemeralPlatformParameters()[0].currentValue.boolean)
        .build()
      scrollToPosition(0)
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = expectedValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyParamAndSaveViaToolbar_persistsChanges() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException>() {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()

        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())

        onView(withId(R.id.save_button)).perform(click())
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
          .perform(click())
      }
    }
    assertThat(exception.message).contains("System.exit()")

    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      val expectedValue = PlatformParameterValue.newBuilder()
        .setBoolean(!getEphemeralPlatformParameters()[0].currentValue.boolean)
        .build()
      scrollToPosition(0)
      verifyPlatformParameterValue(
        position = 0,
        expectedValue = expectedValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_clickSave_showsRestartDialog_configChange_dialogPersists() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException> {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()
        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())
        onView(withId(R.id.save_button)).perform(click())
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
      }
    }
    assertThat(exception.message).contains("System.exit()")
  }

  @Test
  fun testPlatformParametersFragment_navigateBack_showsRestartDialog_configChange_dialogPersists() {
    setUpTestApplicationComponent()
    val exception = assertThrows<SecurityException> {
      launch(PlatformParametersTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()
        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = 0,
            targetViewId = R.id.platform_parameter_switch
          )
        ).perform(click())
        testCoroutineDispatchers.runCurrent()

        pressBack()
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.pending_changes_dialog_save_button_text))
          .inRoot(isDialog())
          .perform(click())
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())
        testCoroutineDispatchers.runCurrent()

        onView(withText(R.string.app_restart_dialog_title))
          .inRoot(isDialog())
          .check(matches(isDisplayed()))
      }
    }
    assertThat(exception.message).contains("System.exit()")
  }

  @Test
  fun testPlatformParametersFragment_showsPendingChangesDialog_configChange_dialogPersists() {
    setUpTestApplicationComponent()
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.platform_parameters_recycler_view,
          position = 0,
          targetViewId = R.id.platform_parameter_switch
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      pressBack()
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withText(R.string.pending_changes_dialog_title_text))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  private fun verifyPlatformParameterDisplayName(
    position: Int,
    expectedDisplayName: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.platform_parameters_recycler_view,
        position = position,
        targetViewId = R.id.platform_parameter_label_text_view
      )
    ).check(matches(withText(expectedDisplayName)))
  }

  private fun verifyPlatformParameterBackgroundColor(
    position: Int,
    expectedColor: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.platform_parameters_recycler_view,
        position = position,
        targetViewId = R.id.platform_parameter_constraint_layout
      )
    ).check { view, _ ->
      val color = (view.background as ColorDrawable).color
      assertThat(color).isEqualTo(expectedColor)
    }
  }

  private fun verifyPlatformParameterSyncDetails(
    position: Int,
    expectedSyncStatus: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.platform_parameters_recycler_view,
        position = position,
        targetViewId = R.id.sync_details_text_view
      )
    ).check(matches(withText(expectedSyncStatus)))
  }

  private fun verifyPlatformParameterValue(
    position: Int,
    expectedValue: PlatformParameterValue
  ) {
    when {
      expectedValue.hasBoolean() -> {
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = position,
            targetViewId = R.id.platform_parameter_switch
          )
        ).check(matches(if (expectedValue.boolean) isChecked() else not(isChecked())))
      }

      expectedValue.hasString() -> {
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = position,
            targetViewId = R.id.platform_parameter_input_edit_text
          )
        ).check(matches(withText(expectedValue.string)))
      }

      expectedValue.hasInteger() -> {
        onView(
          atPositionOnView(
            recyclerViewId = R.id.platform_parameters_recycler_view,
            position = position,
            targetViewId = R.id.platform_parameter_input_edit_text
          )
        ).check(matches(withText(expectedValue.integer.toString())))
      }
    }
  }

  private fun getSyncStatusText(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED ->
        context.getString(R.string.feature_flag_never_synced_message)
      SyncStatus.NOT_SYNCED_FROM_SERVER ->
        context.getString(R.string.feature_flag_never_synced_message)
      SyncStatus.SYNCED_FROM_SERVER ->
        context.getString(R.string.feature_flag_synced_from_server_message)
      SyncStatus.LOCAL_OVERRIDE ->
        context.getString(R.string.feature_flag_currently_overridden_message)
      else ->
        context.getString(R.string.feature_flag_never_synced_message)
    }
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.platform_parameters_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(position)
    )
  }

  private fun getEphemeralPlatformParameters(): List<EphemeralPlatformParameter> {
    val provider = platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    return monitorFactory.waitForNextSuccessfulResult(provider).sortedWith(
      compareByDescending<EphemeralPlatformParameter> {
        it.syncStatus == SyncStatus.LOCAL_OVERRIDE
      }.thenBy { it.id.name }
    )
  }

  private fun getPlatformParameterDisplayName(id: PlatformParameterId): String {
    return id.name
      .lowercase()
      .split('_')
      .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
  }

  private fun getSplashScreenWelcomeMsgPosition(): Int {
    return getEphemeralPlatformParameters().indexOf(
      getEphemeralPlatformParameters().first {
        it.id == PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE
      }
    )
  }

  private fun getSyncUpWorkerTimePeriodPosition(): Int {
    return getEphemeralPlatformParameters().indexOf(
      getEphemeralPlatformParameters().first {
        it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      }
    )
  }

  private fun verifyOverriddenAlertIconIsVisible(position: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.platform_parameters_recycler_view,
        position = position,
        targetViewId = R.id.currently_overridden_alert_icon
      )
    ).check(matches(isDisplayed()))
  }

  // Populates the remote DB with test platform parameter for SPLASH_SCREEN_WELCOME_MESSAGE.
  private fun addTestBooleanRemotePlatformParameterToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      REMOTE_DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemotePlatformParameter(
          RemotePlatformParameter.newBuilder().apply {
            id = PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE
            remoteValue = PlatformParameterValue.newBuilder().apply {
              boolean = value
            }.build()
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the remote DB with test platform parameter for SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS.
  private fun addTestIntegerRemotePlatformParameterToDatabase(
    component: TestApplicationComponent,
    value: Int
  ) {
    val database = component.getCacheStoreFactory().create(
      REMOTE_DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemotePlatformParameter(
          RemotePlatformParameter.newBuilder().apply {
            id = PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
            remoteValue = PlatformParameterValue.newBuilder().apply {
              integer = value
            }.build()
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the Local Overridden DB with test platform parameter for SPLASH_SCREEN_WELCOME_MESSAGE.
  private fun addTestBooleanOverriddenPlatformParameterToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      LOCAL_OVERRIDE_DATABASE_NAME,
      LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      LocalOverridePlatformParameterDatabase.newBuilder().apply {
        addOverriddenPlatformParameter(
          OverriddenPlatformParameter.newBuilder().apply {
            id = PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE
            overriddenValue = PlatformParameterValue.newBuilder()
              .setBoolean(value)
              .build()
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the Local Overridden DB with test platform parameter for SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS.
  private fun addTestIntegerOverriddenPlatformParameterToDatabase(
    component: TestApplicationComponent,
    value: Int
  ) {
    val database = component.getCacheStoreFactory().create(
      LOCAL_OVERRIDE_DATABASE_NAME,
      LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      LocalOverridePlatformParameterDatabase.newBuilder().apply {
        addOverriddenPlatformParameter(
          OverriddenPlatformParameter.newBuilder().apply {
            id = PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
            overriddenValue = PlatformParameterValue.newBuilder()
              .setInteger(value)
              .build()
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  private fun <T> Deferred<T>.waitForSuccessfulResult(
    testCoroutineDispatchers: TestCoroutineDispatchers,
    backgroundDispatcher: CoroutineDispatcher
  ) {
    return when (
      val result = waitForResult(
        testCoroutineDispatchers, backgroundDispatcher
      )
    ) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult(
    testCoroutineDispatchers: TestCoroutineDispatchers,
    backgroundDispatcher: CoroutineDispatcher
  ) = toStateFlow(backgroundDispatcher).waitForLatestValue(testCoroutineDispatchers)

  private fun <T> Deferred<T>.toStateFlow(
    backgroundDispatcher: CoroutineDispatcher
  ): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(
    testCoroutineDispatchers: TestCoroutineDispatchers
  ): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      DaggerPlatformParametersFragmentTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build() as TestApplicationComponent
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestPlatformParameterModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  /** [ApplicationComponent] for [PlatformParametersFragmentTest]. */
  interface TestApplicationComponent : ApplicationComponent {
    /** [ApplicationComponent.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    /**
     * Injects [TestApplicationComponent] to [PlatformParametersFragmentTest] providing the required
     * dagger modules.
     */
    fun inject(platformParametersFragmentTest: PlatformParametersFragmentTest)
    fun getCacheStoreFactory(): PersistentCacheStore.Factory
    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
    @BackgroundDispatcher
    override fun getBackgroundDispatcher(): CoroutineDispatcher
  }

  /** [Application] class for [PlatformParametersFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParametersFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Called when setting up [TestApplication]. */
    fun inject(platformParametersFragmentTest: PlatformParametersFragmentTest) {
      component.inject(platformParametersFragmentTest)
    }
    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }
    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
