package org.oppia.android.app.devoptions.forcenetworktype

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.forcenetworktype.testing.ForceNetworkTypeTestActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
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
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
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

/** Tests for [ForceNetworkTypeFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ForceNetworkTypeFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ForceNetworkTypeFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testForceNetworkTypeFragment_networkTypesAreShown() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "Default"
      )
      scrollToPosition(position = 1)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Wifi"
      )
      scrollToPosition(position = 2)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Cellular"
      )
      scrollToPosition(position = 3)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "No network"
      )
    }
  }

  @Test
  fun testForceNetworkTypeFragment_configChange_networkTypesAreShown() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "Default"
      )
      scrollToPosition(position = 1)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Wifi"
      )
      scrollToPosition(position = 2)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Cellular"
      )
      scrollToPosition(position = 3)
      verifyTextOnNetworkTypeListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "No network"
      )
    }
  }

  @Test
  fun testForceNetworkTypeFragment_defaultIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_configChange_defaultIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickLocal_localIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemClickOnNetworkTypeListItem(itemPosition = 1)
      scrollToPosition(position = 0)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickLocal_configChange_localIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemClickOnNetworkTypeListItem(itemPosition = 1)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickCellular_cellularIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      performItemClickOnNetworkTypeListItem(itemPosition = 2)
      scrollToPosition(position = 0)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickCellular_configChange_cellularIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      performItemClickOnNetworkTypeListItem(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickNone_noneIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      performItemClickOnNetworkTypeListItem(itemPosition = 3)
      scrollToPosition(position = 3)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  @Test
  fun testForceNetworkTypeFragment_clickNone_configChange_noneIsChecked_restAreUnchecked() {
    launch(ForceNetworkTypeTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      performItemClickOnNetworkTypeListItem(itemPosition = 3)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition = 3)
    }
  }

  private fun verifyTextOnNetworkTypeListItemAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.force_network_type_recycler_view,
        position = itemPosition,
        targetViewId = R.id.network_type_text_view
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyItemCheckedOnNetworkTypeListItemAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.force_network_type_recycler_view,
        position = itemPosition,
        targetViewId = R.id.selected_network_tick
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyItemNotCheckedOnNetworkTypeListItemAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.force_network_type_recycler_view,
        position = itemPosition,
        targetViewId = R.id.selected_network_tick
      )
    ).check(matches(not(isDisplayed())))
  }

  private fun performItemClickOnNetworkTypeListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.force_network_type_recycler_view,
        position = itemPosition,
        targetViewId = R.id.network_type_layout
      )
    ).perform(click())
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.force_network_type_recycler_view)).perform(
      scrollToPosition<ViewHolder>(position)
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class
    ]
  )
  /** [ApplicationComponent] for [ForceNetworkTypeFragmentTest]. */
  interface TestApplicationComponent : ApplicationComponent {
    /** [ApplicationComponent.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    /**
     * Injects [TestApplicationComponent] to [ForceNetworkTypeFragmentTest] providing the required
     * dagger modules.
     */
    fun inject(forceNetworkTypeFragmentTest: ForceNetworkTypeFragmentTest)
  }

  /** [Application] class for [ForceNetworkTypeFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerForceNetworkTypeFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Called when setting up [TestApplication]. */
    fun inject(forceNetworkTypeFragmentTest: ForceNetworkTypeFragmentTest) {
      component.inject(forceNetworkTypeFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
