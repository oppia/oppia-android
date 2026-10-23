package org.oppia.android.app.topic.studyguide

import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
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
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.StudyGuideFragmentArguments
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.topic.RouteToStudyGuideListener
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
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.oppia.android.util.profile.toProfileIdPreservingZero
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val FRACTIONS_SUBTOPIC_TOPIC_ID_0 = 1
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_1 = 2
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_2 = 3
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_3 = 4
private const val FRACTIONS_SUBTOPIC_LIST_SIZE = 4

/** Tests for [StudyGuideFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StudyGuideFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class StudyGuideFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @field:[Rule JvmField]
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Mock
  lateinit var mockRouteToStudyGuideListener: RouteToStudyGuideListener

  @Captor
  lateinit var profileIdCaptor: ArgumentCaptor<LegacyProfileId>

  @Captor
  lateinit var topicIdCaptor: ArgumentCaptor<String>

  @Captor
  lateinit var subtopicIndexCaptor: ArgumentCaptor<Int>

  @Captor
  lateinit var subtopicListSizeCaptor: ArgumentCaptor<Int>

  private val profileId = LegacyProfileId.newBuilder().apply { internalId = 1 }.build()

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceLoadLessonProtosFromAssets(true)
    TestPlatformParameterModule.forceEnableWorkedExamples(true)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testStudyGuide_testTopicSubtopic1_firstSectionHeadingIsDisplayed() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 0,
          targetViewId = R.id.study_guide_section_heading_text
        )
      ).check(matches(withText("What is a test subtopic?")))
    }
  }

  @Test
  fun testStudyGuide_testTopicSubtopic1_firstSectionContentIsDisplayed() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 1,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  @Test
  fun testStudyGuide_loadJson_fractionsSubtopic2_sectionContentIsDisplayed() {
    // Reapply both flags together since resetting one platform parameter clears all overrides.
    TestPlatformParameterModule.reset()
    TestPlatformParameterModule.forceLoadLessonProtosFromAssets(false)
    TestPlatformParameterModule.forceEnableWorkedExamples(true)
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      subtopicListSize = FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 1,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  @Test
  fun testStudyGuide_testTopicSubtopic1_secondSectionHeadingIsDisplayed() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 2,
          targetViewId = R.id.study_guide_section_heading_text
        )
      ).check(matches(withText("Review related skills")))
    }
  }

  @Test
  fun testStudyGuide_testTopicSubtopic1_secondSectionShowsConceptCardLinkText() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 3,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("test_skill_id_0 concept card"))))
    }
  }

  @Test
  fun testStudyGuide_testTopicSubtopic1_secondSectionShowsOnlyValidWorkedExample() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      val workedExampleContent = atPositionOnView(
        recyclerViewId = R.id.study_guide_section_recycler_view,
        position = 3,
        targetViewId = R.id.study_guide_section_content_text
      )
      onView(workedExampleContent)
        .check(matches(withText(containsString("Question:\nWhat is one half as a fraction?"))))
      onView(workedExampleContent)
        .check(matches(withText(containsString("Answer:\nOne half is 1/2."))))
      onView(workedExampleContent)
        .check(matches(not(withText(containsString("This answer should be ignored.")))))
      onView(workedExampleContent)
        .check(matches(not(withText(containsString("This question should be ignored.")))))
    }
  }

  @Test
  fun testStudyGuide_workedExamplesDisabled_secondSectionDoesNotShowWorkedExample() {
    // The flags forced in setUp() have to be re-applied together since overriding one of them on
    // its own would leave the rest of the flags uninitialized.
    TestPlatformParameterModule.reset()
    TestPlatformParameterModule.forceLoadLessonProtosFromAssets(true)
    TestPlatformParameterModule.forceEnableWorkedExamples(false)
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      val workedExampleContent = atPositionOnView(
        recyclerViewId = R.id.study_guide_section_recycler_view,
        position = 3,
        targetViewId = R.id.study_guide_section_content_text
      )
      onView(workedExampleContent)
        .check(matches(withText(containsString("This section reviews a related skill"))))
      onView(workedExampleContent)
        .check(matches(not(withText(containsString("Question:")))))
      onView(workedExampleContent)
        .check(matches(not(withText(containsString("What is one half as a fraction?")))))
    }
  }

  @Test
  fun testStudyGuide_clickConceptCardLinkText_opensConceptCard() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 3,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).perform(openClickableSpan("test_skill_id_0 concept card"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("An important skill")))
    }
  }

  @Test
  fun testStudyGuide_previousSubtopicTitle_whatIsAFraction_hasCorrectContentDescription() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_prev_subtopic_title)).check(
        matches(
          withContentDescription(
            "The previous subtopic is What is a Fraction?"
          )
        )
      )
    }
  }

  @Test
  fun testStudyGuide_nextSubtopicTitle_mixedNumbers_hasCorrectContentDescription() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_next_subtopic_title)).check(
        matches(
          withContentDescription(
            "The next subtopic is Mixed Numbers"
          )
        )
      )
    }
  }

  @Test
  fun testStudyGuide_fractionSubtopicId1_onlyNextNavCardIsVisible() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_0,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_next_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
      onView(withId(R.id.study_guide_previous_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  @Test
  fun testStudyGuide_fractionSubtopicId2_previousAndNextNavCardsAreVisible() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_previous_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
      onView(withId(R.id.study_guide_next_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
  }

  @Test
  fun testStudyGuide_fractionSubtopicId4_onlyPreviousNavCardIsVisible() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_3,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_previous_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
      onView(withId(R.id.study_guide_next_navigation_card))
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testStudyGuide_fracSubtopicId2_clickNextNavCard_routesToNextSubtopic() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_navigation_card_container)).perform(nestedScrollTo())
      onView(withId(R.id.study_guide_next_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()

      verify(mockRouteToStudyGuideListener).routeToStudyGuide(
        capture(profileIdCaptor),
        capture(topicIdCaptor),
        capture(subtopicIndexCaptor),
        capture(subtopicListSizeCaptor)
      )
      assertThat(profileIdCaptor.value).isEqualTo(profileId)
      assertThat(topicIdCaptor.value).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(subtopicIndexCaptor.value).isEqualTo(FRACTIONS_SUBTOPIC_TOPIC_ID_2)
      assertThat(subtopicListSizeCaptor.value).isEqualTo(FRACTIONS_SUBTOPIC_LIST_SIZE)
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testStudyGuide_fracSubtopicId2_clickPrevNavCard_routesToPreviousSubtopic() {
    runWithLaunchedActivityAndAddedFragment(
      FRACTIONS_TOPIC_ID,
      subtopicIndex = FRACTIONS_SUBTOPIC_TOPIC_ID_1,
      FRACTIONS_SUBTOPIC_LIST_SIZE
    ) {
      onView(withId(R.id.study_guide_navigation_card_container)).perform(nestedScrollTo())
      onView(withId(R.id.study_guide_previous_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()

      verify(mockRouteToStudyGuideListener).routeToStudyGuide(
        capture(profileIdCaptor),
        capture(topicIdCaptor),
        capture(subtopicIndexCaptor),
        capture(subtopicListSizeCaptor)
      )
      assertThat(profileIdCaptor.value).isEqualTo(profileId)
      assertThat(topicIdCaptor.value).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(subtopicIndexCaptor.value).isEqualTo(FRACTIONS_SUBTOPIC_TOPIC_ID_0)
      assertThat(subtopicListSizeCaptor.value).isEqualTo(FRACTIONS_SUBTOPIC_LIST_SIZE)
    }
  }

  @Test
  fun testStudyGuide_oneSubtopicInList_continueStudyingTextIsNotShown() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(withId(R.id.study_guide_continue_studying_text_view))
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onActivity { activity ->
        val studyGuideFragment = activity.supportFragmentManager
          .findFragmentById(R.id.test_fragment_placeholder) as StudyGuideFragment
        val arguments = studyGuideFragment.arguments
        assertThat(arguments).isNotNull()
        val args = arguments!!.getProto(
          StudyGuideFragment.STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY,
          StudyGuideFragmentArguments.getDefaultInstance()
        )
        val receivedProfileId = arguments.extractCurrentUserProfileId()

        assertThat(args.topicId).isEqualTo(TEST_TOPIC_ID_0)
        assertThat(args.subtopicIndex).isEqualTo(1)
        assertThat(receivedProfileId).isEqualTo(profileId)
        assertThat(args.subtopicListSize).isEqualTo(1)
      }
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testStudyGuide_englishContentLang_sectionContentIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 1,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testStudyGuide_englishContentLang_switchToArabic_workedExampleIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      // Switch to Arabic after opening the page. It should update the section content with the
      // correct translation shown.
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)
      testCoroutineDispatchers.runCurrent()

      val workedExampleContent = atPositionOnView(
        recyclerViewId = R.id.study_guide_section_recycler_view,
        position = 3,
        targetViewId = R.id.study_guide_section_content_text
      )
      onView(workedExampleContent)
        .check(matches(withText(containsString("ما هو النصف في صورة كسر؟"))))
      onView(workedExampleContent)
        .check(matches(withText(containsString("النصف هو 1/2."))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testStudyGuide_withArabicContentLang_contentAndWorkedExampleAreInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 1,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("وصف الموضوع الفرعي هنا"))))
      val workedExampleContent = atPositionOnView(
        recyclerViewId = R.id.study_guide_section_recycler_view,
        position = 3,
        targetViewId = R.id.study_guide_section_content_text
      )
      onView(workedExampleContent)
        .check(matches(withText(containsString("ما هو النصف في صورة كسر؟"))))
      onView(workedExampleContent)
        .check(matches(withText(containsString("النصف هو 1/2."))))
      onView(workedExampleContent)
        .check(matches(withContentDescription(containsString("ما هو النصف في صورة كسر؟"))))
      onView(workedExampleContent)
        .check(matches(withContentDescription(containsString("النصف هو 1/2."))))
    }
  }

  @Test
  fun testStudyGuide_configurationChange_sectionContentIsDisplayed() {
    runWithLaunchedActivityAndAddedFragment(
      TEST_TOPIC_ID_0,
      subtopicIndex = 1,
      subtopicListSize = 1
    ) {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.study_guide_section_recycler_view,
          position = 1,
          targetViewId = R.id.study_guide_section_content_text
        )
      ).check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  private fun updateContentLanguage(profileId: LegacyProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId.toProfileIdPreservingZero(),
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun runWithLaunchedActivityAndAddedFragment(
    topicId: String,
    subtopicIndex: Int,
    subtopicListSize: Int,
    testBlock: ActivityScenario<StudyGuideFragmentTestActivity>.() -> Unit
  ) {
    val fragment = StudyGuideFragment.newInstance(
      topicId,
      subtopicIndex,
      profileId,
      subtopicListSize,
      ReadingTextSize.MEDIUM_TEXT_SIZE
    )
    val intent = Intent(context, StudyGuideFragmentTestActivity::class.java)
    TestActivity.registerWithPackageManager<StudyGuideFragmentTestActivity>(context)
    ActivityScenario.launch<StudyGuideFragmentTestActivity>(intent).use { scenario ->
      scenario.onActivity { activity ->
        activity.mockRouteToStudyGuideListener = mockRouteToStudyGuideListener
        activity.setContentView(R.layout.test_activity)
        activity.supportFragmentManager.beginTransaction()
          .add(R.id.test_fragment_placeholder, fragment)
          .commitNow()
      }
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  /** See the version in StateFragmentTest for documentation details. */
  @Suppress("SameParameterValue")
  private fun openClickableSpan(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = "openClickableSpan"

      override fun getConstraints(): Matcher<View> = hasClickableSpanWithText(text)

      override fun perform(uiController: UiController?, view: View?) {
        // The view shouldn't be null if the constraints are being met.
        (view as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text)?.onClick(view)
      }
    }
  }

  /** See the version in StateFragmentTest for documentation details. */
  private fun hasClickableSpanWithText(text: String): Matcher<View> {
    return object : TypeSafeMatcher<View>(TextView::class.java) {
      override fun describeTo(description: Description?) {
        description?.appendText("has ClickableSpan with text")?.appendValue(text)
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text) != null
      }
    }
  }

  private fun TextView.getClickableSpans(): List<Pair<String, ClickableSpan>> {
    val viewText = text
    return (viewText as Spannable).getSpans(
      /* start= */ 0, /* end= */ text.length, ClickableSpan::class.java
    ).map {
      viewText.subSequence(viewText.getSpanStart(it), viewText.getSpanEnd(it)).toString() to it
    }
  }

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(
    text: String
  ): ClickableSpan? = find { text in it.first }?.second

  /**
   * Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from:
   * https://stackoverflow.com/a/46037284/8860848
   */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = "Scroll within a NestedScrollView"

      override fun getConstraints(): Matcher<View> =
        isDescendantOfA(isAssignableFrom(NestedScrollView::class.java))

      override fun perform(uiController: UiController, view: View) {
        try {
          val nestedScrollView =
            findFirstParentLayoutOfClass(view, NestedScrollView::class.java) as NestedScrollView
          nestedScrollView.scrollTo(0, view.top)
        } catch (e: Exception) {
          throw PerformException.Builder()
            .withActionDescription(description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(e)
            .build()
        }
        uiController.loopMainThreadUntilIdle()
      }
    }
  }

  private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View {
    var parent: ViewParent = FrameLayout(view.context)
    lateinit var incrementView: ViewParent
    var i = 0
    while (!(parent.javaClass === parentClass)) {
      parent = if (i == 0) view.parent else incrementView.parent
      incrementView = parent
      i++
    }
    return parent as View
  }

  /**
   * A test-only [TestActivity] that hosts [StudyGuideFragment] in isolation so the fragment can be
   * validated without depending on [StudyGuideActivity]. It records study guide navigation requests
   * via [mockRouteToStudyGuideListener].
   */
  class StudyGuideFragmentTestActivity :
    TestActivity(),
    RouteToStudyGuideListener {
    lateinit var mockRouteToStudyGuideListener: RouteToStudyGuideListener

    override fun routeToStudyGuide(
      profileId: LegacyProfileId,
      topicId: String,
      subtopicIndex: Int,
      subtopicListSize: Int
    ) {
      mockRouteToStudyGuideListener.routeToStudyGuide(
        profileId,
        topicId,
        subtopicIndex,
        subtopicListSize
      )
    }
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
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
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

    fun inject(studyGuideFragmentTest: StudyGuideFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStudyGuideFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(studyGuideFragmentTest: StudyGuideFragmentTest) {
      component.inject(studyGuideFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
