package org.oppia.android.app.help.thirdparty

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import org.oppia.android.app.model.LicenseListFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
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
import org.oppia.android.util.extensions.getProto
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

/** Tests for [LicenseListFragmentTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = LicenseListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class LicenseListFragmentTest {
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
  fun openLicenseListActivity_selectItem_opensLicenseTextViewerActivity() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(
        allOf(
          hasComponent(LicenseTextViewerActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openLicenseListActivity_changeConfig_selectItem_opensLicenseTextViewerActivity() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(
        allOf(
          hasComponent(LicenseTextViewerActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex0_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex0_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex1_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(1)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_1))))
      onView(withText(R.string.license_name_1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex1_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(1)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_1))))
      onView(withText(R.string.license_name_1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex2_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(2)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex2_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(2)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex3_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(3)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex3_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(3)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<LicenseListActivity>(createLicenseListActivity(2)).use { scenario ->

      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        var fragment = activity.supportFragmentManager
          .findFragmentById(R.id.license_list_fragment_placeholder) as LicenseListFragment

        val arguments = checkNotNull(fragment.arguments) {
          "Expected arguments to be passed to LicenseListFragment"
        }
        val args = arguments.getProto(
          "LicenseListFragment.arguments",
          LicenseListFragmentArguments.getDefaultInstance()
        )
        val receivedDependencyIndex = args.dependencyIndex
        val receivedIsMultipane = args.isMultipane

        assertThat(receivedDependencyIndex).isEqualTo(2)
        assertThat(receivedIsMultipane).isEqualTo(false)
      }
    }
  }

  private fun createLicenseListActivity(dependencyIndex: Int): Intent {
    return LicenseListActivity.createLicenseListActivityIntent(
      ApplicationProvider.getApplicationContext(),
      dependencyIndex
    )
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
      PlatformParameterTestModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
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

    fun inject(licenseListFragmentTest: LicenseListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLicenseListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(licenseListFragmentTest: LicenseListFragmentTest) {
      component.inject(licenseListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
