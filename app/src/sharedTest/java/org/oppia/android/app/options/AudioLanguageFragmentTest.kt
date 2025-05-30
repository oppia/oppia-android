package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
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
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.ENGLISH_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
import org.oppia.android.app.model.FeatureFlagId.MULTIPLE_CLASSROOMS
import org.oppia.android.app.model.FeatureFlagId.ONBOARDING_FLOW_V2
import org.oppia.android.app.options.AudioLanguageFragment.Companion.retrieveLanguageFromArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.DisableFeatureFlag
import org.oppia.android.testing.EnableFeatureFlag
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
    private const val PORTUGUESE_BUTTON_INDEX = 2
    private const val ARABIC_BUTTON_INDEX = 3
    private const val NIGERIAN_PIDGIN_BUTTON_INDEX = 4
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

  @Before
  fun setUp() {
    initializeTestApplicationComponent()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    TestPlatformParameterConfigRetriever.reset()
    Intents.release()
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testOpenFragment_withEnglish_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      verifyEnglishIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testOpenFragment_withPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(BRAZILIAN_PORTUGUESE_LANGUAGE).use {
      verifyPortugueseIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testOpenFragment_withNigerianPidgin_selectedLanguageIsNaija() {
    launchActivityWithLanguage(NIGERIAN_PIDGIN_LANGUAGE).use {
      verifyNigerianPidginIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_configChange_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      rotateToLandscape()

      verifyEnglishIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_tabletConfig_selectedLanguageIsEnglish() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()

      verifyEnglishIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_changeLanguageToPortuguese_configChange_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      rotateToLandscape()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_configChange_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      rotateToLandscape()

      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  @DisableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_selectPortuguese_thenEnglish_selectedLanguageIsPortuguese() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      selectEnglish()

      verifyEnglishIsSelected()
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_onboardingV2Enabled_allViewsAreDisplayed() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(withId(R.id.audio_language_text)).check(
        matches(withText("In Oppia, you can listen to lessons!"))
      )
      onView(withId(R.id.audio_language_subtitle)).check(
        matches(withText(context.getString(R.string.audio_language_fragment_subtitle)))
      )
      onView(withId(R.id.audio_language_dropdown_list)).check(
        matches(withText(context.getString(R.string.english_localized_language_name)))
      )
      onView(withId(R.id.onboarding_navigation_back)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
      onView(withId(R.id.onboarding_navigation_continue)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testAudioLanguage_onboardingV2Enabled_configChange_allViewsAreDisplayed() {
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_language_text)).check(
        matches(withText("In Oppia, you can listen to lessons!"))
      )
      onView(withId(R.id.audio_language_subtitle)).check(
        matches(withText(context.getString(R.string.audio_language_fragment_subtitle)))
      )
      onView(withId(R.id.audio_language_dropdown_list)).check(
        matches(withText(context.getString(R.string.english_localized_language_name)))
      )
      onView(withId(R.id.onboarding_navigation_back)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
      onView(withId(R.id.onboarding_navigation_continue)).check(
        matches(withEffectiveVisibility(Visibility.VISIBLE))
      )
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testFragment_portraitMode_backButtonPressed_currentScreenIsDestroyed() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testFragment_landscapeMode_backButtonPressed_currentScreenIsDestroyed() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @DisableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_portraitMode_continueButtonClicked_launchesHomeScreen() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @DisableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_landscapeMode_continueButtonClicked_launchesHomeScreen() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @EnableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_multipleClassroomsEnabled_continueButtonClicked_launchesClassroomScreen() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @EnableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_landscapeMode_multipleClassroomsEnabled_continueButtonLaunchesClassroomScreen() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @DisableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_languageSelectionChanged_selectionIsUpdated() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Naijá")))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withId(R.id.audio_language_dropdown_list)).check(
          matches(withText(R.string.nigerian_pidgin_localized_language_name))
        )

        onView(withId(R.id.onboarding_navigation_continue)).perform(click())
        testCoroutineDispatchers.runCurrent()
        intended(hasComponent(HomeActivity::class.java.name))
      }
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  @DisableFeatureFlag(MULTIPLE_CLASSROOMS)
  fun testFragment_languageSelectionChanged_configChange_selectionIsUpdated() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        onData(
          CoreMatchers.allOf(
            `is`(instanceOf(String::class.java)), `is`("Naijá")
          )
        )
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        onView(isRoot()).perform(orientationLandscape())
        testCoroutineDispatchers.runCurrent()

        // Verifies that the selected language is still set successfully after configuration change.
        onView(withId(R.id.audio_language_dropdown_list)).check(
          matches(withText(R.string.nigerian_pidgin_localized_language_name))
        )

        onView(withId(R.id.onboarding_navigation_continue)).perform(click())
        testCoroutineDispatchers.runCurrent()

        intended(hasComponent(HomeActivity::class.java.name))
      }
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val fragment = activity.supportFragmentManager
          .findFragmentById(R.id.audio_language_fragment_container) as AudioLanguageFragment
        val receivedAudioLanguage = fragment.arguments?.retrieveLanguageFromArguments()

        assertThat(ENGLISH_AUDIO_LANGUAGE).isEqualTo(receivedAudioLanguage)
      }
    }
  }

  @Test
  @EnableFeatureFlag(ONBOARDING_FLOW_V2)
  fun testFragment_saveInstanceState_verifyCorrectStateRestored() {
    launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(ENGLISH_AUDIO_LANGUAGE)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      var language: AudioLanguage? = null

      scenario.onActivity { activity ->
        var fragment = activity.supportFragmentManager
          .findFragmentById(R.id.audio_language_fragment_container) as AudioLanguageFragment
        language = fragment.audioLanguageFragmentPresenterV1.getLanguageSelected()
      }

      scenario.recreate()

      scenario.onActivity { activity ->
        val newfragment = activity.supportFragmentManager
          .findFragmentById(R.id.audio_language_fragment_container) as AudioLanguageFragment
        val restoredAudioLanguage =
          newfragment.audioLanguageFragmentPresenterV1.getLanguageSelected()

        assertThat(restoredAudioLanguage).isEqualTo(language)
      }
    }
  }

  @Test
  fun testFragment_withEnglish_verifyContentDescriptionReadsArabicLanguage() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      verifyEnglishIsSelected()

      onView(
        atPositionOnView(
          R.id.audio_language_recycler_view,
          3,
          R.id.language_text_view
        )
      ).check(matches(withText(R.string.arabic_localized_language_name)))

      onView(
        atPositionOnView(
          R.id.audio_language_recycler_view,
          3,
          R.id.language_text_view
        )
      ).check(
        matches(withContentDescription(R.string.arabic_language_display_name_content_description))
      )
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

  private fun initializeTestApplicationComponent() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
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
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
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
