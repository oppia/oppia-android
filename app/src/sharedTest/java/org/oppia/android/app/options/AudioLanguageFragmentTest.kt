package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.oppia.android.app.model.AudioLanguage.HINDI_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
import org.oppia.android.app.model.AudioLanguageActivityParams
import org.oppia.android.app.model.AudioLanguageActivityParams.ParentScreen.LEARNER_INTRO_SCREEN
import org.oppia.android.app.model.AudioLanguageActivityParams.ParentScreen.OPTIONS_SCREEN
import org.oppia.android.app.model.ProfileId
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
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
    private const val NIGERIAN_PIDGIN_BUTTON_INDEX = 1
    private const val PORTUGUESE_BUTTON_INDEX = 2
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
  lateinit var profileManagementController: ProfileManagementController
  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    TestPlatformParameterModule.reset()
    Intents.release()
  }

  @Test
  fun testOpenFragment_withEnglish_selectedLanguageIsEnglish() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testOpenFragment_withPortuguese_selectedLanguageIsPortuguese() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(BRAZILIAN_PORTUGUESE_LANGUAGE).use {
      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testOpenFragment_withNigerianPidgin_selectedLanguageIsNaija() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(NIGERIAN_PIDGIN_LANGUAGE).use {
      verifyNigerianPidginIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_configChange_selectedLanguageIsEnglish() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      rotateToLandscape()

      verifyEnglishIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAudioLanguage_tabletConfig_selectedLanguageIsEnglish() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()

      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_changeLanguageToPortuguese_configChange_selectedLanguageIsPortuguese() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      rotateToLandscape()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testAudioLanguage_landscape_changeLanguageToPortuguese_selectedLanguageIsPortuguese() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {

      selectPortuguese()

      verifyPortugueseIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_selectPortuguese_thenEnglish_selectedLanguageIsPortuguese() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      selectPortuguese()

      selectEnglish()

      verifyEnglishIsSelected()
    }
  }

  @Test
  fun testAudioLanguage_englishIsFirstThenAlphabetical() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)
    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.audio_language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.audio_language_recycler_view,
          position = 0,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("English")))

      onView(withId(R.id.audio_language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.audio_language_recycler_view,
          position = 1,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("Naijá")))

      onView(withId(R.id.audio_language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.audio_language_recycler_view,
          position = 2,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("Português")))

      onView(withId(R.id.audio_language_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.audio_language_recycler_view,
          position = 3,
          targetViewId = R.id.language_text_view
        )
      ).check(matches(withText("العربية")))
    }
  }

  @Test
  fun testAudioLanguage_onboardingV2Enabled_allViewsAreDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
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
  @Config(qualifiers = "land")
  fun testAudioLanguage_onboardingV2Enabled_landscapeMode_allViewsAreDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
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
  fun testAudioLanguage_fromOptions_onboardingV2Enabled_navigationViewsAreNotDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use {
      onView(withText("In Oppia, you can listen to lessons!")).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.audio_language_fragment_subtitle)))
        .check(matches(isDisplayed()))
      onView(withId(R.id.audio_language_dropdown_list)).check(matches(isDisplayed()))
      onView(withId(R.id.onboarding_steps_count)).check(matches(not(isDisplayed())))
      onView(withId(R.id.onboarding_navigation_back)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.onboarding_navigation_continue)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
    }
  }

  @Test
  fun testAudioLanguage_fromOptions_landscape_onboardingV2Enabled_navigationViewsAreNotDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use {
      onView(withText("In Oppia, you can listen to lessons!")).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.audio_language_fragment_subtitle)))
        .check(matches(isDisplayed()))
      onView(withId(R.id.audio_language_dropdown_list)).check(matches(isDisplayed()))
      onView(withId(R.id.onboarding_steps_count)).check(matches(not(isDisplayed())))
      onView(withId(R.id.onboarding_navigation_back)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
      onView(withId(R.id.onboarding_navigation_continue)).check(
        matches(withEffectiveVisibility(Visibility.GONE))
      )
    }
  }

  @Test
  fun testAudioLanguage_fromOptions_onboardingV2Enabled_toolbarIsDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use {
      onView(withId(R.id.reading_list_app_bar_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAudioLanguage_fromOnboarding_onboardingV2Enabled_toolbarIsNotDisplayed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
      onView(withId(R.id.reading_list_app_bar_layout)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFragment_portraitMode_backButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN)
      .use { scenario ->
        onView(withId(R.id.onboarding_navigation_back)).perform(click())
        testCoroutineDispatchers.runCurrent()
        scenario.onActivity { activity ->
          assertThat(activity.isFinishing).isTrue()
        }
      }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_landscapeMode_backButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_portraitMode_systemBackButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      onView(isRoot()).perform(pressBack())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_landscapeMode_systemBackButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      onView(isRoot()).perform(pressBack())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_portraitMode_toolbarBackButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      onView(withContentDescription(R.string.navigate_up)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_landscapeMode_toolbarBackButtonPressed_currentScreenIsDestroyed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      onView(withContentDescription(R.string.navigate_up)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_portraitMode_continueButtonClicked_launchesHomeScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_landscapeMode_continueButtonClicked_launchesHomeScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_multipleClassroomsEnabled_continueButtonClicked_launchesClassroomScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testFragment_landscapeMode_multipleClassroomsEnabled_continueButtonLaunchesClassroomScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use {
      onView(withId(R.id.onboarding_navigation_continue)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Verifies that accepting the default language selection works correctly.
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testFragment_fromOnboarding_languageSelectionChanged_selectionIsUpdated() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
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
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testFragment_fromOnboarding_languageSelectionChanged_configChange_selectionIsUpdated() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        onData(
          CoreMatchers.allOf(
            `is`(instanceOf(String::class.java)), `is`("Naijá")
          )
        )
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        rotateToLandscape()

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
  fun testFragment_fromOnboarding_swahiliIsFilteredOut() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withText("Kiswahili"))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .check(doesNotExist())
      }
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testFragment_fromOptions_withEnglishLanguage_defaultSelectionIsEnglish() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.audio_language_dropdown_list)).check(
        matches(withText(R.string.english_localized_language_name))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testFragment_fromOptions_withEnglishLang_naijaSelected_selectionIsUpdatedToNaija() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Naijá")))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withId(R.id.audio_language_dropdown_list)).check(
          matches(withText(R.string.nigerian_pidgin_localized_language_name))
        )
      }
    }
  }

  @Test
  fun testFragment_fromOptions_swahiliIsFilteredOut() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, OPTIONS_SCREEN).use { scenario ->
      scenario.onActivity { activity ->
        onView(withId(R.id.audio_language_dropdown_list)).perform(click())

        testCoroutineDispatchers.runCurrent()

        onView(withText("Kiswahili"))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .check(doesNotExist())
      }
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
      scenario.onActivity { activity ->

        val fragment = activity.supportFragmentManager
          .findFragmentById(R.id.audio_language_fragment_container) as AudioLanguageFragment
        val receivedAudioLanguage = fragment.arguments?.retrieveLanguageFromArguments()

        assertThat(ENGLISH_AUDIO_LANGUAGE).isEqualTo(receivedAudioLanguage)
      }
    }
  }

  @Test
  fun testFragment_saveInstanceState_verifyCorrectStateRestored() {
    launchV2FlowWithLanguage(ENGLISH_AUDIO_LANGUAGE, LEARNER_INTRO_SCREEN).use { scenario ->
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

  @Test
  fun testFragment_withHindiLanguagePreviouslySet_defaultsBackToEnglish() {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = false)

    val profileId = ProfileId.newBuilder().setInternalId(0).build()
    val updateProvider = profileManagementController.updateAudioLanguage(
      profileId,
      HINDI_AUDIO_LANGUAGE
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    launchActivityWithLanguage(ENGLISH_AUDIO_LANGUAGE).use {
      testCoroutineDispatchers.runCurrent()

      verifyEnglishIsSelected()
    }
  }

  private fun launchV2FlowWithLanguage(
    audioLanguage: AudioLanguage,
    parentScreen: AudioLanguageActivityParams.ParentScreen
  ): ActivityScenario<AudioLanguageActivity> {
    initializeTestApplicationComponent(enableOnboardingFlowV2 = true)
    return launch<AudioLanguageActivity>(
      createDefaultAudioActivityIntent(audioLanguage, parentScreen)
    ).also {
      testCoroutineDispatchers.runCurrent()
    }
  }

  private fun launchActivityWithLanguage(
    audioLanguage: AudioLanguage
  ): ActivityScenario<AppLanguageActivity> {
    return launch<AppLanguageActivity>(createDefaultAudioActivityIntent(audioLanguage)).also {
      testCoroutineDispatchers.runCurrent()
    }
  }

  private fun createDefaultAudioActivityIntent(
    audioLanguage: AudioLanguage,
    parentScreen: AudioLanguageActivityParams.ParentScreen = OPTIONS_SCREEN
  ) = AudioLanguageActivity.createAudioLanguageActivityIntent(context, audioLanguage, parentScreen)

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

  private fun initializeTestApplicationComponent(enableOnboardingFlowV2: Boolean) {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(enableOnboardingFlowV2)
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
