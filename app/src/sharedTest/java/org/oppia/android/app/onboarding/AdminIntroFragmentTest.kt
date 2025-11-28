package org.oppia.android.app.onboarding

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
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
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AdminIntroFragment]. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdminIntroFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminIntroFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule val composeRule = createEmptyComposeRule()
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    Intents.release()
  }

  @Test
  fun testIntroFragment_onLaunch_allViewsAreCorrectlyDisplayed() {
    launch(AdminIntroActivity::class.java).use {

      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_header))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_settings_text))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_learners_text))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_step_count_four))
        .assertIsDisplayed()

      composeRule.onNodeWithContentDescription(
        context.getString(R.string.onboarding_otter_content_description)
      )
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_back))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsDisplayed()
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testIntroFragment_landscapeMode_viewsAreCorrectlyDisplayed_stepCountIsNotVisible() {
    launch(AdminIntroActivity::class.java).use {
      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_header))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_settings_text))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.admin_intro_activity_learners_text))
        .assertIsDisplayed()

      composeRule.onNodeWithContentDescription(
        context.getString(R.string.onboarding_otter_content_description)
      )
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_back))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsDisplayed()

      composeRule.onNodeWithText(context.getString(R.string.onboarding_step_count_four))
        .assertDoesNotExist()
    }
  }

  @Test
  fun testIntroFragment_onBackButtonClicked_currentScreenIsDestroyed() {
    launch(AdminIntroActivity::class.java).use { scenario ->

      scenario.onActivity { activity ->
        composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_back))
          .performClick()

        testCoroutineDispatchers.runCurrent()

        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  // This is a placeholder test that should fail when the PIN creation screen has been implemented.
  @Test
  fun testIntroFragment_continueButtonClicked_launchesProfileChooserActivity() {
    launchAdminIntroActivity().use {
      composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .performClick()

      testCoroutineDispatchers.runCurrent()

      val expectedParams = ProfileChooserActivityParams.newBuilder()
        .setParentScreen(ProfileChooserActivityParams.ParentScreen.ADMIN_INTRO_SCREEN)
        .setProfileNickname("Admin")
        .build()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
      intended(hasProtoExtra(PROFILE_CHOOSER_PARAMS_KEY, expectedParams))
      intended(hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR))
    }
  }

  @Test
  fun testFragment_launchFragment_logsProfileOnboardingStartedEvent() {
    val testProfileId = ProfileId.newBuilder().setInternalId(0).build()

    launch(AdminIntroActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      val event = fakeAnalyticsEventLogger.getMostRecentEvent()
      assertThat(event).hasStartProfileOnboardingContextThat {
        hasProfileIdThat().isEqualTo(testProfileId)
      }
    }
  }

  private fun launchAdminIntroActivity(): ActivityScenario<AdminIntroActivity> {
    val testProfileId = ProfileId.newBuilder().setInternalId(0).build()

    val scenario = launch<AdminIntroActivity>(
      AdminIntroActivity.createAdminIntroActivityIntent(
        context,
        testProfileId,
        ProfileType.SUPERVISOR,
        "Admin"
      ).apply {
        decorateWithUserProfileId(testProfileId)
      }
    )
    testCoroutineDispatchers.runCurrent()
    return scenario
  }

  private fun setUpTestApplicationComponent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

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
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(adminIntroFragmentTest: AdminIntroFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdminIntroFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(adminIntroFragmentTest: AdminIntroFragmentTest) {
      component.inject(adminIntroFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
