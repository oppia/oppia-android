package org.oppia.android.app.administratorcontrols

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.AppLanguageLocaleModule
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
import org.oppia.android.util.extensions.getLastUpdateTime
import org.oppia.android.util.extensions.getVersionName
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
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppVersionActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AppVersionActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AppVersionActivityTest {
  @get:Rule(order = 0)
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule(order = 1)
  val oppiaTestRule = OppiaTestRule()

  @get:Rule(order = 2)
  val activityScenarioRule = ActivityScenarioRule(AppVersionActivity::class.java)

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler
  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testAppVersionActivity_hasCorrectActivityLabel() {
    val scenario = activityScenarioRule.scenario
    scenario.onActivity { activity ->
      val title = activity.title
      assertThat(title).isEqualTo(context.getString(R.string.app_version_activity_title))
    }
  }

  private fun createAppVersionActivityIntent(): Intent {
    return AppVersionActivity.createAppVersionActivityIntent(
      ApplicationProvider.getApplicationContext()
    )
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
    // Reset locale after each test
    if (::appLanguageLocaleHandler.isInitialized) {
      appLanguageLocaleHandler.resetLocale()
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = createAppVersionActivityIntent().extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.APP_VERSION_ACTIVITY)
  }

  @Test
  fun testAppVersionActivity_loadFragment_displaysAppVersion() {
    val scenario = activityScenarioRule.scenario
    scenario.onActivity { activity ->
      val lastUpdateDate = scenario.convertTimeStampToDate(context.getLastUpdateTime())
      onView(
        withText(
          String.format(
            context.resources.getString(R.string.app_version_name),
            context.getVersionName()
          )
        )
      ).check(matches(isDisplayed()))
      onView(
        withText(
          String.format(
            context.resources.getString(R.string.app_last_update_date),
            lastUpdateDate
          )
        )
      ).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testAppVersionActivity_configurationChange_appVersionIsDisplayedCorrectly() {
    val scenario = activityScenarioRule.scenario
    scenario.onActivity { activity ->
      onView(isRoot()).perform(orientationLandscape())
      val lastUpdateDate = scenario.convertTimeStampToDate(context.getLastUpdateTime())
      onView(
        withId(
          R.id.app_version_text_view
        )
      ).check(
        matches(
          withText(
            String.format(
              context.resources.getString(R.string.app_version_name),
              context.getVersionName()
            )
          )
        )
      )
      onView(
        withId(
          R.id.app_last_update_date_text_view
        )
      ).check(
        matches(
          withText(
            String.format(
              context.resources.getString(R.string.app_last_update_date),
              lastUpdateDate
            )
          )
        )
      )
    }
  }

  @Test
  fun testAppVersionActivity_loadFragment_onBackPressed_displaysAdministratorControlsActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(
      launchAdministratorControlsActivityIntent(internalProfileId = 0)
    ).use { scenario ->

      testCoroutineDispatchers.runCurrent()

      // Scroll to the item in the RecyclerView
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )

      // Click on the app version option
      onView(withText(R.string.administrator_controls_app_version)).perform(click())

      // Wait for UI update
      testCoroutineDispatchers.runCurrent()

      // Verify if the AppVersionActivity is running
      var currentActivity: Activity? = null
      scenario.onActivity { activity ->
        currentActivity = getCurrentActivity()
        assertThat(currentActivity).isInstanceOf(AppVersionActivity::class.java)
      }

      // Simulate pressing back via activity rather than Espresso
      (currentActivity as? AppVersionActivity)?.onBackPressed()

      // Wait for UI update
      testCoroutineDispatchers.runCurrent()

      // Verify that AdministratorControlsActivity is visible again
      onView(withId(R.id.administrator_controls_list)).check(matches(isDisplayed()))
    }
  }

  fun getCurrentActivity(): Activity? {
    val activity = arrayOfNulls<Activity>(1)
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      activity[0] = ActivityLifecycleMonitorRegistry.getInstance()
        .getActivitiesInStage(Stage.RESUMED)
        .firstOrNull()
    }
    return activity[0]
  }

  private fun ActivityScenario<AppVersionActivity>.convertTimeStampToDate(
    timestampMillis: Long
  ): String {
    lateinit var dateTimeString: String
    onActivity { activity ->
      val resourceHandler = activity.activityComponent.getAppLanguageResourceHandler()
      dateTimeString = resourceHandler.computeDateString(timestampMillis)
    }
    return dateTimeString
  }

//  private fun launchAppVersionActivityIntent(): ActivityScenario<AppVersionActivity> {
//    val intent = AppVersionActivity.createAppVersionActivityIntent(
//      ApplicationProvider.getApplicationContext()
//    )
//    return ActivityScenario.launch(intent)
//  }

  private fun launchAdministratorControlsActivityIntent(internalProfileId: Int): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
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
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class,
      AppLanguageLocaleModule::class,
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(appVersionActivityTest: AppVersionActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppVersionActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(appVersionActivityTest: AppVersionActivityTest) {
      component.inject(appVersionActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
