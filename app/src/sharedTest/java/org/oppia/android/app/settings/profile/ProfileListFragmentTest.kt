package org.oppia.android.app.settings.profile

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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.oppia.android.app.model.ProfileListFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.testing.profile.ProfileTestHelper
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileListFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProfileListFragment_initializeProfiles_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 0,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 0,
          targetViewId = R.id.profile_list_admin_text
        )
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 1,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 1,
          targetViewId = R.id.profile_list_admin_text
        )
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testProfileListFragment_initializeProfiles_changeConfiguration_checkProfilesAreShown() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 0,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Admin"))
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 0,
          targetViewId = R.id.profile_list_admin_text
        )
      ).check(
        matches(withText(context.getString(R.string.profile_chooser_admin)))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 1,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Ben"))
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 1,
          targetViewId = R.id.profile_list_admin_text
        )
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testProfileListFragment_addManyProfiles_checkProfilesAreSorted() {
    profileTestHelper.initializeProfiles()
    profileTestHelper.addMoreProfiles(numProfiles = 5)
    launch(ProfileListActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 0,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Admin"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 1,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("A"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 2,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("B"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 3,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("Ben"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 4,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("C"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          5
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 5,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("D"))
      )
      onView(withId(R.id.profile_list_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          6
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.profile_list_recycler_view,
          position = 6,
          targetViewId = R.id.profile_list_name
        )
      ).check(
        matches(withText("E"))
      )
    }
  }

  @Test
  fun testProfileListFragment_initializeProfile_clickProfile_checkOpensProfileEditActivity() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(atPosition(R.id.profile_list_recycler_view, 0)).perform(click())
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    profileTestHelper.initializeProfiles()
    launch(ProfileListActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val profileListFragment = activity.supportFragmentManager
          .findFragmentById(R.id.profile_list_container) as ProfileListFragment

        val arguments = checkNotNull(profileListFragment.arguments) {
          "Expected variables to be passed to ProfileListFragment"
        }
        val args = arguments.getProto(
          ProfileListFragment.PROFILE_LIST_FRAGMENT_ARGUMENTS_KEY,
          ProfileListFragmentArguments.getDefaultInstance()
        )
        val receivedIsMultipane = args.isMultipane

        assertThat(receivedIsMultipane).isEqualTo(false)
      }
    }
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
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
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

    fun inject(profileListFragmentTest: ProfileListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileListFragmentTest: ProfileListFragmentTest) {
      component.inject(profileListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
