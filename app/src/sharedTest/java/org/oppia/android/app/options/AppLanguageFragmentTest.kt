package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
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
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
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
import org.oppia.android.app.options.AppLanguageFragment.Companion.retrieveLanguageFromArguments
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** Tests for [AppLanguageFragment]. */
@RunWith(AndroidJUnit4::class)
@RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageFragmentTest.TestApplication::class)
class AppLanguageFragmentTest {

  private companion object {
    private const val ENGLISH_BUTTON_INDEX = 1
    private const val KISWAHILI_BUTTON_INDEX = 4
    private const val PORTUGUESE_BUTTON_INDEX = 3
  }

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

  private val internalProfileId: Int = -1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testAppLanguage_selectedLanguageIsEnglish() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAppLanguage_configChange_selectedLanguageIsEnglish() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      rotateToLandscape()
      verifyEnglishIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAppLanguage_tabletConfig_selectedLanguageIsEnglish() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToPortuguese_configChange_selectedLanguageIsPortuguese() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      rotateToLandscape()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAppLanguage_tabletConfig_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectPortuguese()
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAppLanguage_changeLanguageToSwahili_selectedLanguageObservedIsSwahili() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use {
      testCoroutineDispatchers.runCurrent()
      verifyEnglishIsSelected()
      selectKiswahili()
      var appLanguageActivity: AppLanguageActivity? = null
      it.onActivity { it1 -> appLanguageActivity = it1 }
      testCoroutineDispatchers.runCurrent()
      appLanguageActivity?.recreate()
      testCoroutineDispatchers.runCurrent()
      verifyKiswahiliIsSelected(appLanguageActivity)
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testAppLanguageFragment_arguments_workingProperly() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val appLanguageFragment = activity.supportFragmentManager
          .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
        val recievedLanguage = appLanguageFragment.arguments?.retrieveLanguageFromArguments()
        val receivedProfileId = appLanguageFragment.arguments?.extractCurrentUserProfileId()?.internalId

        assertThat(recievedLanguage).isEqualTo(OppiaLanguage.ENGLISH)
        assertThat(receivedProfileId).isEqualTo(internalProfileId)
      }
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testAppLanguageFragment_saveInstanceState_workingProperly() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent(OppiaLanguage.ENGLISH)).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        var appLanguageFragment = activity.supportFragmentManager
          .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
        appLanguageFragment.appLanguageFragmentPresenter.onLanguageSelected(OppiaLanguage.ARABIC)

        activity.recreate()

        appLanguageFragment = activity.supportFragmentManager
          .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment
        val recievedLanguage = appLanguageFragment.appLanguageFragmentPresenter.getLanguageSelected()

        assertThat(recievedLanguage).isEqualTo(OppiaLanguage.ARABIC)
      }
    }
  }

  private fun verifyKiswahiliIsSelected(appLanguageActivity: AppLanguageActivity?) {
    checkSelectedLanguage(index = KISWAHILI_BUTTON_INDEX, expectedLanguageName = "Kiswahili")
    assertThat(appLanguageActivity?.appLanguageActivityPresenter?.getLanguageSelected()?.name)
      .isEqualTo(OppiaLanguage.SWAHILI.name)
  }

  private fun selectPortuguese() {
    selectLanguage(PORTUGUESE_BUTTON_INDEX)
  }

  private fun selectKiswahili() {
    selectLanguage(KISWAHILI_BUTTON_INDEX)
  }

  private fun verifyEnglishIsSelected() {
    checkSelectedLanguage(index = ENGLISH_BUTTON_INDEX, expectedLanguageName = "English")
  }

  private fun verifyPortugueseIsSelected() {
    checkSelectedLanguage(index = PORTUGUESE_BUTTON_INDEX, expectedLanguageName = "Português")
  }

  private fun checkSelectedLanguage(index: Int, expectedLanguageName: String) {
    onView(
      atPositionOnView(
        R.id.language_recycler_view,
        index,
        R.id.language_radio_button
      )
    ).check(matches(isChecked()))
    onView(
      atPositionOnView(
        R.id.language_recycler_view,
        index,
        R.id.language_text_view
      )
    ).check(matches(ViewMatchers.withText(expectedLanguageName)))
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun selectLanguage(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.language_recycler_view,
        position = index,
        targetViewId = R.id.language_radio_button
      )
    ).perform(
      click()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun createAppLanguageActivityIntent(oppiaLanguage: OppiaLanguage): Intent {
    return AppLanguageActivity.createAppLanguageActivityIntent(
      context,
      oppiaLanguage,
      internalProfileId
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, PlatformParameterModule::class, ApplicationModule::class,
      RobolectricModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogReportWorkerModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(appLanguageFragmentTest: AppLanguageFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(appLanguageFragmentTest: AppLanguageFragmentTest) {
      component.inject(appLanguageFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
