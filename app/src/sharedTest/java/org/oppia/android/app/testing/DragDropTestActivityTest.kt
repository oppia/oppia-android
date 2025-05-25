package org.oppia.android.app.testing

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
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
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.DragAndDropItemFacilitator
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.ChildViewCoordinatesProvider
import org.oppia.android.app.utility.CustomGeneralLocation
import org.oppia.android.app.utility.DragViewAction
import org.oppia.android.app.utility.RecyclerViewCoordinatesProvider
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
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
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.ImageLoaderTestModule
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.PlatformParameterTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DragDropTestActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class DragDropTestActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

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
  fun testDragDropTestActivity_dragItem0ToPosition1() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            position = 0,
            ChildViewCoordinatesProvider(
              childViewId = R.id.text_view_for_string_no_data_binding,
              insideChildViewCoordinatesProvider = GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(
            position = 1,
            childItemCoordinatesProvider = CustomGeneralLocation.UNDER_RIGHT
          ),
          precisionDescriber = Press.FINGER
        )
      )
      testCoroutineDispatchers.runCurrent()
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 0))
        .check(matches(withText("Item 2")))
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 1))
        .check(matches(withText("Item 1")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem1ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            position = 1,
            ChildViewCoordinatesProvider(
              childViewId = R.id.text_view_for_string_no_data_binding,
              insideChildViewCoordinatesProvider = GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(
            position = 2,
            childItemCoordinatesProvider = CustomGeneralLocation.UNDER_RIGHT
          ),
          precisionDescriber = Press.FINGER
        )
      )
      testCoroutineDispatchers.runCurrent()
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 1))
        .check(matches(withText("Item 3")))
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 2))
        .check(matches(withText("Item 2")))
    }
  }

  @Test
  fun testDragDropTestActivity_dragItem3ToPosition2() {
    launch(DragDropTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        attachDragDropToActivity(activity)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.drag_drop_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            position = 3,
            ChildViewCoordinatesProvider(
              childViewId = R.id.text_view_for_string_no_data_binding,
              insideChildViewCoordinatesProvider = GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(
            position = 2,
            childItemCoordinatesProvider = CustomGeneralLocation.ABOVE_RIGHT
          ),
          precisionDescriber = Press.FINGER
        )
      )
      testCoroutineDispatchers.runCurrent()
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 2))
        .check(matches(withText("Item 4")))
      onView(atPosition(recyclerViewId = R.id.drag_drop_recycler_view, position = 3))
        .check(matches(withText("Item 3")))
    }
  }

  private fun attachDragDropToActivity(activity: DragDropTestActivity) {
    val dragDragTestFragment: DragDropTestFragment = activity.supportFragmentManager
      .findFragmentById(R.id.drag_drop_test_fragment_placeholder) as DragDropTestFragment
    val recyclerView: RecyclerView? =
      dragDragTestFragment.view?.findViewById(R.id.drag_drop_recycler_view)
    val itemTouchHelper = ItemTouchHelper(createDragCallback(fragment = dragDragTestFragment))
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  private fun createDragCallback(fragment: DragDropTestFragment): ItemTouchHelper.Callback {
    return DragAndDropItemFacilitator(
      fragment as OnItemDragListener,
      fragment as OnDragEndedListener
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
      AuthenticationTestModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DispatcherTestModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageTestModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageLoaderTestModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleTestModule::class,
      LogReportWorkerModule::class,
      LogReportingTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PerformanceMetricsAssessorTestModule::class,
      PlatformParameterSingletonModule::class,
      PlatformParameterTestModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusTestModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(dragDropTestActivityTest: DragDropTestActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDragDropTestActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(dragDropTestActivityTest: DragDropTestActivityTest) {
      component.inject(dragDropTestActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
