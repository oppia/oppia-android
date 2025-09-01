package org.oppia.android.app.devoptions.featureflags

import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
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
import org.junit.After
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
import org.oppia.android.app.devoptions.featureflags.testing.FeatureFlagsTestActivity
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.LocalOverridePlatformParameterDatabase
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.RemoteFeatureFlag
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
import org.oppia.android.domain.oppialogger.OppiaLogger
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
import org.oppia.android.testing.data.DataProviderTestMonitor
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

/** Tests for [FeatureFlagsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = FeatureFlagsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class FeatureFlagsFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var context: Context
  @Inject lateinit var oppiaLogger: OppiaLogger

  private companion object {
    private const val REMOTE_DATABASE_NAME = "platform_parameter_and_feature_flag_database"
    private const val LOCAL_OVERRIDE_DATABASE_NAME =
      "local_overridden_platform_parameter_and_feature_flag_database"
    private const val DOWNLOADS_SUPPORT_FLAG_NAME = "Downloads Support"
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testFeatureFlagsFragment_verifyRecyclerView_hasCorrectItemCount() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val expectedCount = getEphemeralFeatureFlags().size

      onView(withId(R.id.feature_flags_recycler_view))
        .check(RecyclerViewMatcher.hasItemCount(count = expectedCount))

      // Note to developers: if you add/remove a feature flag, please update the expected count.
      onView(withId(R.id.feature_flags_recycler_view))
        .check(RecyclerViewMatcher.hasItemCount(count = 14))
    }
  }

  @Test
  fun testFeatureFlagsFragment_verifyRecyclerViewItems_haveCorrectDetails() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      getEphemeralFeatureFlags().forEachIndexed { index, ephemeralFeatureFlag ->
        scrollToPosition(index)
        verifyFeatureFlagDisplayName(
          position = index,
          expectedDisplayName = getFeatureFlagDisplayName(ephemeralFeatureFlag.id)
        )
        verifyFeatureFlagSyncDetails(
          position = index,
          expectedSyncStatus = getSyncStatusText(ephemeralFeatureFlag.syncStatus)
        )
        verifyFeatureFlagSwitchState(
          position = index,
          expectedState = ephemeralFeatureFlag.currentValue
        )
        verifyFeatureFlagBackgroundColor(
          position = index,
          expectedColor = when (ephemeralFeatureFlag.syncStatus) {
            SyncStatus.SYNCED_FROM_SERVER -> {
              context.getColor(R.color.component_color_shared_item_background_solid_color)
            }
            SyncStatus.NOT_SYNCED_FROM_SERVER -> {
              context.getColor(R.color.component_color_shared_item_background_solid_color)
            }
            SyncStatus.LOCAL_OVERRIDE -> {
              context.getColor(R.color.component_color_feature_flag_overridden_background_color)
            }
            else -> context.getColor(R.color.component_color_shared_item_background_solid_color)
          }
        )
      }
    }
  }

  @Test
  fun testFeatureFlagsFragment_withNoRemoteOrOverriddenValues_returnsCorrectDisplayName() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(1)
      verifyFeatureFlagDisplayName(
        position = 1,
        expectedDisplayName = DOWNLOADS_SUPPORT_FLAG_NAME
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withNoRemoteOrOverriddenValues_returnsDefaultValue() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(1)
      verifyFeatureFlagSwitchState(
        position = 1,
        expectedState = false
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withNoRemoteOrOverriddenValues_returnsNeverSyncedMessage() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.feature_flag_never_synced_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withNoRemoteOrOverriddenValues_returnsDefaultBackgroundColor() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyRemoteValue_returnsSyncedFromServerMessage() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(1)
      verifyFeatureFlagSyncDetails(
        position = 1,
        expectedSyncStatus = context.getString(R.string.feature_flag_synced_from_server_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyRemoteValue_returnsServerBackgroundColor() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyRemoteValue_returnsRemoteBooleanValue() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(1)
      verifyFeatureFlagSwitchState(
        position = 1,
        expectedState = true
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyRemoteValue_returnsRemoteDisplayName() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(1)
      verifyFeatureFlagDisplayName(
        position = 1,
        expectedDisplayName = DOWNLOADS_SUPPORT_FLAG_NAME
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyOverriddenValue_returnsCurrentlyOverriddenMessage() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.feature_flag_currently_overridden_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyOverriddenValue_returnsOverriddenBackgroundColor() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_overridden_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyOverriddenValue_returnsOverriddenBooleanValue() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = true
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyOverriddenValue_returnsCorrectDisplayName() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      verifyFeatureFlagDisplayName(
        position = 0,
        expectedDisplayName = DOWNLOADS_SUPPORT_FLAG_NAME
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteAndOverriddenValues_returnsCurrentlyOverriddenMessage() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, false)
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.feature_flag_currently_overridden_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteAndOverriddenValues_returnsOverriddenBooleanValue() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, false)
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = true
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteAndOverriddenValues_returnsOverriddenBackgroundColor() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, false)
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_overridden_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_whenSwitchToggled_DownloadsSupportFlagUpdatesValue() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val downloadsSupportFlag = getEphemeralFeatureFlags()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = !downloadsSupportFlag.currentValue
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_toggleDownloadsSupportFlag_configChange_persistsValue() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val downloadsSupportFlag = getEphemeralFeatureFlags()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = !downloadsSupportFlag.currentValue
      )

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = !downloadsSupportFlag.currentValue
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_toggleDownloadsSupportFlag_scrollAndBack_persistsValue() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val originalValue = getEphemeralFeatureFlags()[0].currentValue

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      val expectedState = !originalValue

      scrollToPosition(8)

      scrollToPosition(0)
      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = expectedState
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_toggleFlag_navigateBackAndReopen_persistsValue() {
    setUpTestApplicationComponent()
    val expectedState = !getEphemeralFeatureFlags()[0].currentValue

    launch(FeatureFlagsTestActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      pressBack()
      testCoroutineDispatchers.runCurrent()
      scenario.close()
    }

    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyFeatureFlagSwitchState(
        position = 0,
        expectedState = expectedState
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOverriddenFlag_resetButtonIsVisible() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOverrideAndNoRemote_clickResetButton_resetsFlagToDefault() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 1,
          targetViewId = R.id.feature_flag_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOverrideAndNoRemote_onReset_showsNeverSyncedMessage() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      verifyFeatureFlagSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.feature_flag_never_synced_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteAndOverride_clickResetButton_resetsFlagToRemote() {
    executeInPreviousAppInstance { component ->
      addTestRemoteFeatureFlagToDatabase(component, false)
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteOverride_onReset_showsSyncedWithServerMessage() {
    executeInPreviousAppInstance { component ->
      addTestRemoteFeatureFlagToDatabase(component, false)
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      verifyFeatureFlagSyncDetails(
        position = 0,
        expectedSyncStatus = context.getString(R.string.feature_flag_synced_from_server_message)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOverride_clickResetButton_disablesResetButton() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_clickReset_navigateBackAndReopen_noResetButtonIsVisible() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      pressBack()
      testCoroutineDispatchers.runCurrent()
      scenario.close()
    }

    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_whenNoFlagsModified_saveButtonIsDisabled() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyAnyFlag_saveButtonIsEnabled() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyFlagAndRevert_saveButtonIsDisabled() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_clickResetButton_saveButtonIsEnabled() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyAnyFlag_hasSkyBlueBackgroundColor() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_modified_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyAnyFlag_configChange_skyBlueColorPersists() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_modified_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyFlag_clickSave_reopenDashboard_valuePersists() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())

      onView(withId(R.id.save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      launch(FeatureFlagsTestActivity::class.java).use {
        testCoroutineDispatchers.runCurrent()
        scrollToPosition(0)
        onView(
          atPositionOnView(
            recyclerViewId = R.id.feature_flags_recycler_view,
            position = 0,
            targetViewId = R.id.feature_flag_switch
          )
        ).check(matches(isChecked()))
      }
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyFlagAndRevert_hasNoBackgroundColor() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor = context.getColor(R.color.component_color_shared_item_background_solid_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_modifyOverriddenFlag_hasSkyBlueBackgroundColor() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.feature_flag_switch
        )
      ).perform(click())
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_modified_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_clickResetButton_hasSkyBlueBackgroundColor() {
    executeInPreviousAppInstance { component ->
      addTestOverriddenFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.reset_button
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      verifyFeatureFlagBackgroundColor(
        position = 0,
        expectedColor =
          context.getColor(R.color.component_color_feature_flag_modified_background_color)
      )
    }
  }

  @Test
  fun testFeatureFlagsFragment_withOnlyOverriddenValue_alertIconIsVisible() {
    TestPlatformParameterModule.forceEnableDownloadsSupport(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(0)
      verifyOverriddenAlertIconIsVisible(0)
    }
  }

  @Test
  fun testFeatureFlagsFragment_withNoOverride_alertIconIsNotVisible() {
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.currently_overridden_alert_icon
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFeatureFlagsFragment_withRemoteValue_alertIconIsNotVisible() {
    executeInPreviousAppInstance { component ->
      addTestRemoteFeatureFlagToDatabase(component, true)
      component.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    launch(FeatureFlagsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.feature_flags_recycler_view,
          position = 0,
          targetViewId = R.id.currently_overridden_alert_icon
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  private fun verifyFeatureFlagDisplayName(
    position: Int,
    expectedDisplayName: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = position,
        targetViewId = R.id.feature_flag_label_text_view
      )
    ).check(matches(withText(expectedDisplayName)))
  }

  private fun verifyFeatureFlagSyncDetails(
    position: Int,
    expectedSyncStatus: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = position,
        targetViewId = R.id.sync_details_text_view
      )
    ).check(matches(withText(expectedSyncStatus)))
  }

  private fun verifyOverriddenAlertIconIsVisible(position: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = position,
        targetViewId = R.id.currently_overridden_alert_icon
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyFeatureFlagBackgroundColor(
    position: Int,
    expectedColor: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = position,
        targetViewId = R.id.feature_flag_constraint_layout
      )
    ).check { view, _ ->
      val color = (view.background as ColorDrawable).color
      assertThat(color).isEqualTo(expectedColor)
    }
  }

  private fun verifyFeatureFlagSwitchState(
    position: Int,
    expectedState: Boolean
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.feature_flags_recycler_view,
        position = position,
        targetViewId = R.id.feature_flag_switch
      )
    ).check(matches(if (expectedState) isChecked() else not(isChecked())))
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
    onView(withId(R.id.feature_flags_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(position)
    )
  }

  private fun getEphemeralFeatureFlags(): List<EphemeralFeatureFlag> {
    val provider = platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    return monitorFactory.waitForNextSuccessfulResult(provider).sortedWith(
      compareByDescending<EphemeralFeatureFlag> {
        it.syncStatus == SyncStatus.LOCAL_OVERRIDE
      }.thenBy { it.id.name }
    )
  }

  private fun getFeatureFlagDisplayName(id: FeatureFlagId): String {
    return id.name
      .lowercase()
      .split('_')
      .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
  }

  // Populates the remote DB with test feature flag for DOWNLOADS_SUPPORT.
  private fun addTestRemoteFeatureFlagToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      REMOTE_DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemoteFeatureFlag(
          RemoteFeatureFlag.newBuilder().apply {
            id = FeatureFlagId.DOWNLOADS_SUPPORT
            remoteIsEnabled = value
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the local override DB with test feature flag for DOWNLOADS_SUPPORT.
  private fun addTestOverriddenFeatureFlagToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      LOCAL_OVERRIDE_DATABASE_NAME,
      LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )
    database.storeDataAsync {
      LocalOverridePlatformParameterDatabase.newBuilder().apply {
        addOverriddenFeatureFlag(
          OverriddenFeatureFlag.newBuilder()
            .setId(FeatureFlagId.DOWNLOADS_SUPPORT)
            .setOverriddenValue(value)
            .build()
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
      DaggerFeatureFlagsFragmentTest_TestApplicationComponent.builder()
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
  /** [ApplicationComponent] for [FeatureFlagsFragmentTest]. */
  interface TestApplicationComponent : ApplicationComponent {
    /** [ApplicationComponent.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    /**
     * Injects [TestApplicationComponent] to [FeatureFlagsFragmentTest] providing the required
     * dagger modules.
     */
    fun inject(featureFlagsFragmentTest: FeatureFlagsFragmentTest)
    fun getCacheStoreFactory(): PersistentCacheStore.Factory
    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
    @BackgroundDispatcher
    override fun getBackgroundDispatcher(): CoroutineDispatcher
  }

  /** [Application] class for [FeatureFlagsFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeatureFlagsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Called when setting up [TestApplication]. */
    fun inject(featureFlagsFragmentTest: FeatureFlagsFragmentTest) {
      component.inject(featureFlagsFragmentTest)
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
