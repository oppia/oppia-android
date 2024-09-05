package org.oppia.android.app.thirdparty

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.allOf
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
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
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
import org.oppia.android.util.logging.EventLoggingConfigurationModule
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
import org.oppia.android.app.help.thirdparty.ThirdPartyDependencyListFragment
import org.oppia.android.app.model.ThirdPartyDependencyListFragmentArguments
import org.oppia.android.util.extensions.getProto

/** Tests for [ThirdPartyDependencyListFragmentTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ThirdPartyDependencyListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ThirdPartyDependencyListFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun openThirdPartyDepsListActivity_selectItem_opensLicenseListActivity() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1
        )
      ).perform(ViewActions.click())
      intended(
        allOf(
          hasComponent(LicenseListActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_changeConfig_selectItem_opensLicenseListActivity() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1
        )
      ).perform(ViewActions.click())
      intended(
        allOf(
          hasComponent(LicenseListActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem0_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      val dependencyName0 = retrieveDependencyName(R.string.third_party_dependency_name_0)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(dependencyName0))))
      onView(withText(dependencyName0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem1_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      val dependencyName1 = retrieveDependencyName(R.string.third_party_dependency_name_1)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(dependencyName1))))
      onView(withText(dependencyName1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem2_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(2)
      )
      val dependencyName2 = retrieveDependencyName(R.string.third_party_dependency_name_2)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 2
        )
      ).check(matches(hasDescendant(withText(dependencyName2))))
      onView(withText(dependencyName2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem3_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      val dependencyName3 = retrieveDependencyName(R.string.third_party_dependency_name_3)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 3
        )
      ).check(matches(hasDescendant(withText(dependencyName3))))
      onView(withText(dependencyName3)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem0_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      val dependencyName0 = retrieveDependencyName(R.string.third_party_dependency_name_0)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(dependencyName0))))
      onView(withText(dependencyName0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem1_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      val dependencyName1 = retrieveDependencyName(R.string.third_party_dependency_name_1)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(dependencyName1))))
      onView(withText(dependencyName1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem2_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(2)
      )
      val dependencyName2 = retrieveDependencyName(R.string.third_party_dependency_name_2)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 2
        )
      ).check(matches(hasDescendant(withText(dependencyName2))))
      onView(withText(dependencyName2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem3_displaysCorrectDepName() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      val dependencyName3 = retrieveDependencyName(R.string.third_party_dependency_name_3)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 3
        )
      ).check(matches(hasDescendant(withText(dependencyName3))))
      onView(withText(dependencyName3)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem0_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      val version0 = retrieveDependencyVersion(R.string.third_party_dependency_version_0)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(version0))))
      onView(withText(version0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem1_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      val version1 = retrieveDependencyVersion(R.string.third_party_dependency_version_1)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1,
        )
      ).check(matches(hasDescendant(withText(version1))))
      onView(withText(version1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem2_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(2)
      )
      val version2 = retrieveDependencyVersion(R.string.third_party_dependency_version_2)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 2,
        )
      ).check(matches(hasDescendant(withText(version2))))
      onView(withText(version2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_recyclerviewItem3_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      val version3 = retrieveDependencyVersion(R.string.third_party_dependency_version_3)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 3,
        )
      ).check(matches(hasDescendant(withText(version3))))
      onView(withText(version3)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem0_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      val version0 = retrieveDependencyVersion(R.string.third_party_dependency_version_0)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(version0))))
      onView(withText(version0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem1_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      val version1 = retrieveDependencyVersion(R.string.third_party_dependency_version_1)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 1,
        )
      ).check(matches(hasDescendant(withText(version1))))
      onView(withText(version1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem2_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(2)
      )
      val version2 = retrieveDependencyVersion(R.string.third_party_dependency_version_2)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 2,
        )
      ).check(matches(hasDescendant(withText(version2))))
      onView(withText(version2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openThirdPartyDepsListActivity_configLand_recyclerviewItem3_displaysCorrectDepVersion() {
    launch(ThirdPartyDependencyListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.third_party_dependency_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      val version3 = retrieveDependencyVersion(R.string.third_party_dependency_version_3)
      onView(
        atPosition(
          recyclerViewId = R.id.third_party_dependency_list_fragment_recycler_view,
          position = 3,
        )
      ).check(matches(hasDescendant(withText(version3))))
      onView(withText(version3)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch(ThirdPartyDependencyListActivity::class.java).use { scenario->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val thirdPartyDependencyListFragment = activity.supportFragmentManager
          .findFragmentById(R.id.third_party_dependency_list_fragment_placeholder)
          as ThirdPartyDependencyListFragment

        val arguments = checkNotNull(thirdPartyDependencyListFragment.arguments) {
          "Expected arguments to be passed to ThirdPartyDependencyListFragment"
        }
        val args = arguments.getProto(
          "ThirdPartyDependencyListFragment.arguments",
          ThirdPartyDependencyListFragmentArguments.getDefaultInstance()
        )
        val receivedIsMultipane = args?.isMultipane ?: false

        assertThat(receivedIsMultipane).isEqualTo(false)
      }
    }
  }

  private fun retrieveDependencyName(id: Int): String {
    return ApplicationProvider.getApplicationContext<TestApplication>()
      .resources.getString(id)
  }

  private fun retrieveDependencyVersion(id: Int): String {
    val res = ApplicationProvider.getApplicationContext<TestApplication>().resources
    return res.getString(R.string.third_party_dependency_version_formatter, res.getString(id))
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {

    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(thirdPartyDependencyListFragmentTest: ThirdPartyDependencyListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerThirdPartyDependencyListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(thirdPartyDependencyListFragmentTest: ThirdPartyDependencyListFragmentTest) {
      component.inject(thirdPartyDependencyListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
