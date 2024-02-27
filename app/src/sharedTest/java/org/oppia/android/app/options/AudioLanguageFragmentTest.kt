package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import dagger.Component
import dagger.Module
import dagger.Provides
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
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.ENGLISH_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
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

/** Tests for [AudioLanguageFragment]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AudioLanguageFragmentTest.TestApplication::class)
class AudioLanguageFragmentTest {
  private companion object {
    private const val ENGLISH_BUTTON_INDEX = 0
    private const val PORTUGUESE_BUTTON_INDEX = 4
    private const val ARABIC_BUTTON_INDEX = 5
    private const val NIGERIAN_PIDGIN_BUTTON_INDEX = 6
  }

  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @Test
  fun testOpenFragment_withEnglish_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testOpenFragment_withPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(BRAZILIAN_PORTUGUESE_LANGUAGE).use {
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testOpenFragment_withNigerianPidgin_selectedLanguageIsNaija() {
    launchActivityWithLanguage(NIGERIAN_PIDGIN_LANGUAGE).use {
      verifyNigerianPidginIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_configChange_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      rotateToLandscape()

      verifyEnglishIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAudioLanguage_tabletConfig_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()

      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_changeLanguageToPortuguese_configChange_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      rotateToLandscape()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAudioLanguage_configChange_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      rotateToLandscape()

      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_selectPortuguese_thenEnglish_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      selectEnglish()

      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_onboardingV2Enabled_languageSelectionDropdownIsDisplayed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_language_dropdown_background)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Config(qualifiers = "land")
  @Test
  fun testAudioLanguage_onboardingV2Enabled_configChange_languageDropdownIsDisplayed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_language_dropdown_background)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testAudioLanguage_onboardingV2Enabled_tabletConfigChange_languageDropdownIsDisplayed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(withId(R.id.audio_language_dropdown_background)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Config(qualifiers = "land")
  @Test
  fun testFragment_landscapeMode_stepCountText_isNotDisplayed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_steps_count))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testFragment_tabletLandscapeMode_stepCountText_isNotDisplayed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_steps_count))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  @RunOn(TestPlatform.ESPRESSO) // Robolectric is usually not used to test the interaction of
  // Android components
  @Test
  fun testFragment_backButtonClicked_currentScreenIsDestroyed() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use { scenario ->
      onView(withId(R.id.onboarding_navigation_back))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      if (scenario != null) {
        Truth.assertThat(scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
      }
    }
  }

  @Test
  fun testFragment_continueButtonClicked_launchesHomeScreen() {
    forceEnableOnboardingFlowV2()
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(withId(R.id.onboarding_navigation_continue))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  private fun launchActivityWithLanguage(
    audioLanguage: AudioLanguage
  ): ActivityScenario<AppLanguageActivity> {
    return launch<AppLanguageActivity>(createDefaultAudioActivityIntent(audioLanguage)).also {
      testCoroutineDispatchers.runCurrent()
    }
  }

  private fun createDefaultAudioActivityIntent(audioLanguage: AudioLanguage) =
    AudioLanguageActivity.createAudioLanguageActivityIntent(context, audioLanguage)

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun selectEnglish() {
    selectLanguage(ENGLISH_BUTTON_INDEX)
  }

  private fun selectPortuguese() {
    selectLanguage(PORTUGUESE_BUTTON_INDEX)
  }

  private fun selectLanguage(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.audio_language_recycler_view,
        position = index,
        targetViewId = R.id.language_radio_button
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyEnglishIsSelected() {
    verifyLanguageIsSelected(index = ENGLISH_BUTTON_INDEX, expectedLanguageName = "English")
  }

  private fun verifyPortugueseIsSelected() {
    verifyLanguageIsSelected(index = PORTUGUESE_BUTTON_INDEX, expectedLanguageName = "Português")
  }

  private fun verifyNigerianPidginIsSelected() {
    verifyLanguageIsSelected(index = NIGERIAN_PIDGIN_BUTTON_INDEX, expectedLanguageName = "Naijá")
  }

  private fun verifyLanguageIsSelected(index: Int, expectedLanguageName: String) {
    onView(
      atPositionOnView(
        R.id.audio_language_recycler_view,
        index,
        R.id.language_radio_button
      )
    ).check(matches(isChecked()))
    onView(
      atPositionOnView(
        R.id.audio_language_recycler_view,
        index,
        R.id.language_text_view
      )
    ).check(matches(withText(expectedLanguageName)))
  }

  private fun forceEnableOnboardingFlowV2() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
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
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, LogReportWorkerModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
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
    interface Builder : ApplicationComponent.Builder

    fun inject(audioLanguageFragmentTest: AudioLanguageFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioLanguageFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(audioLanguageFragmentTest: AudioLanguageFragmentTest) {
      component.inject(audioLanguageFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
