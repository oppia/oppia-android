package org.oppia.android.app.devoptions.platformparameters

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
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
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testPlatformParametersFragment_recyclerView_hasCorrectItemCount() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val expectedCount = getEphemeralPlatformParameters().size

      onView(withId(R.id.platform_parameters_recycler_view)).check { view, _ ->
        val recyclerView = view as RecyclerView
        assertThat(recyclerView.adapter?.itemCount).isEqualTo(expectedCount)
        // Note to developers: if you add/remove a feature flag, please update the expected count.
        assertThat(recyclerView.adapter?.itemCount).isEqualTo(11)
      }
    }
  }

  @Test
  fun testPlatformParametersFragment_recyclerViewItems_hasCorrectDetails() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      getEphemeralPlatformParameters().forEachIndexed { index, ephemeralPlatformParameter ->
        scrollToPosition(index)
        verifyPlatformParameterDisplayName(
          index,
          getPlatformParameterDisplayName(ephemeralPlatformParameter.id)
        )
        verifyPlatformParameterSyncStatus(
          index,
          getSyncStatusText(ephemeralPlatformParameter.syncStatus)
        )
        verifyPlatformParameterState(
          index,
          ephemeralPlatformParameter.currentValue
        )
      }
    }
  }

  @Test
  fun testPlatformParametersFragment_splashScreenWelcomeMsgParameter_hasCorrectDetails() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      verifyPlatformParameterDisplayName(
        0,
        getPlatformParameterDisplayName(splashScreenWelcomeMsgParameter.id)
      )
      verifyPlatformParameterSyncStatus(
        0,
        getSyncStatusText(splashScreenWelcomeMsgParameter.syncStatus)
      )

      verifyPlatformParameterState(
        0,
        splashScreenWelcomeMsgParameter.currentValue
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_splashScreenWelcomeMsgParameter_switchToggled_updatesValue() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          0,
          R.id.platform_parameter_switch
        )
      ).perform(click())

      verifyPlatformParameterState(
        0,
        PlatformParameterValue.newBuilder()
          .setBoolean(!splashScreenWelcomeMsgParameter.currentValue.boolean)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_toggleSplashScreenWelcomeMsg_configChanges_valuePersists() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      val splashScreenWelcomeMsgParameter = getEphemeralPlatformParameters()[0]

      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          0,
          R.id.platform_parameter_switch
        )
      ).perform(click())

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyPlatformParameterState(
        0,
        PlatformParameterValue.newBuilder()
          .setBoolean(!splashScreenWelcomeMsgParameter.currentValue.boolean)
          .build()
      )
    }
  }

  @Test
  fun testPlatformParametersFragment_modifyIntegerPlatformParameter_configChange_changesPersist() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          7,
          R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText("29"))

      verifyPlatformParameterState(
        7,
        PlatformParameterValue.newBuilder()
          .setInteger(29)
          .build()
      )

      onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())

      verifyPlatformParameterState(
        7,
        PlatformParameterValue.newBuilder()
          .setInteger(29)
          .build()
      )
    }
  }

  fun testPlatformParametersFragment_removeTextFromInputBox_showsInvalidInputError() {
    launch(PlatformParametersTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(7)
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          7,
          R.id.platform_parameter_input_edit_text
        )
      ).perform(editTextInputAction.replaceText(""))

      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          7,
          R.id.platform_parameter_input_edit_text
        )
      ).check(
        matches(
          hasErrorText(context.getString(R.string.platform_parameter_invalid_input_error_msg))
        )
      )
    }
  }

  private fun verifyPlatformParameterDisplayName(
    position: Int,
    expectedDisplayName: String
  ) {
    onView(
      atPositionOnView(
        R.id.platform_parameters_recycler_view,
        position,
        R.id.platform_parameter_label_text_view
      )
    ).check(
      matches(
        withText(expectedDisplayName)
      )
    )
  }

  private fun verifyPlatformParameterSyncStatus(
    position: Int,
    expectedSyncStatus: String
  ) {
    onView(
      atPositionOnView(
        R.id.platform_parameters_recycler_view,
        position,
        R.id.sync_status_value_text_view
      )
    ).check(
      matches(
        withText(expectedSyncStatus)
      )
    )
  }

  private fun verifyPlatformParameterState(
    position: Int,
    expectedValue: PlatformParameterValue
  ) {
    if (expectedValue.hasBoolean()) {
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          position,
          R.id.platform_parameter_switch
        )
      ).check(matches(if (expectedValue.boolean) isChecked() else not(isChecked())))
    } else if (expectedValue.hasString()) {
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          position,
          R.id.platform_parameter_input_edit_text
        )
      ).check(matches(withText(expectedValue.string)))
    } else if (expectedValue.hasInteger()) {
      onView(
        atPositionOnView(
          R.id.platform_parameters_recycler_view,
          position,
          R.id.platform_parameter_input_edit_text
        )
      ).check(matches(withText(expectedValue.integer.toString())))
    }
  }

  private fun getSyncStatusText(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED ->
        context.getString(R.string.platform_parameter_unknown_sync_status)

      SyncStatus.NOT_SYNCED_FROM_SERVER ->
        context.getString(R.string.platform_parameter_default_sync_status)

      SyncStatus.SYNCED_FROM_SERVER ->
        context.getString(R.string.platform_parameter_server_sync_status)

      else ->
        context.getString(R.string.platform_parameter_unknown_sync_status)
    }
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.platform_parameters_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(position)
    )
  }

  private fun getEphemeralPlatformParameters(): List<EphemeralPlatformParameter> {
    val provider = platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    return monitorFactory.waitForNextSuccessfulResult(provider)
  }

  private fun getPlatformParameterDisplayName(
    id: PlatformParameterId
  ): String {
    return id.name
      .lowercase()
      .split('_')
      .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
    fun inject(featureFlagsFragmentTest: PlatformParametersFragmentTest)
  }

  /** [Application] class for [PlatformParametersFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParametersFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Called when setting up [TestApplication]. */
    fun inject(featureFlagsFragmentTest: PlatformParametersFragmentTest) {
      component.inject(featureFlagsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
