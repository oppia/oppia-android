package org.oppia.android.app.onboarding

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withAlpha
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
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
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.AppLanguageLocaleHandler
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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

/** Tests for [OnboardingFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = OnboardingFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class OnboardingFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
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
  fun testOnboardingFragment_checkDefaultSlideTitle_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(getOnboardingSlide0Title())))
    }
  }

  @Test
  fun testOnboardingFragment_checkDefaultSlideDescription_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_0_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkDefaultSlide_index0DotIsActive_otherDotsAreInactive() {
    launch(OnboardingActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.onboarding_dot_0),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(1.0F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_1),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_2),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_3),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
    }
  }

  @Test
  fun testOnboardingFragment_checkDefaultSlide_skipButtonIsVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.skip_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkDefaultSlide_getStartedButtonIsNotVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.get_started_button)).check(doesNotExist())
    }
  }

  @Test
  fun testOnboardingFragment_swipeRight_doesNotWork() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(swipeRight())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(getOnboardingSlide0Title())))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_1_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1Description_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_1_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_index1DotIsActive_otherDotsAreInactive() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      onView(
        allOf(
          withId(R.id.onboarding_dot_0),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_1),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(1.0F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_2),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_3),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_skipButtonIsVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_clickSkipButton_shiftsToLastSlide() {
    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_getStartedButtonIsNotVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      onView(withId(R.id.get_started_button)).check(doesNotExist())
    }
  }

  @Test
  fun testOnboardingFragment_swipeLeftThenSwipeRight_isWorking() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 0))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(getOnboardingSlide0Title())))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_2_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2Description_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_2_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_index2DotIsActive_otherDotsAreInactive() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      onView(
        allOf(
          withId(R.id.onboarding_dot_0),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_1),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_2),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(1.0F)))
      onView(
        allOf(
          withId(R.id.onboarding_dot_3),
          isCompletelyDisplayed()
        )
      ).check(matches(withAlpha(0.3F)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_skipButtonIsVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_clickSkipButton_shiftsToLastSlide() {
    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_getStartedButtonIsNotVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      onView(withId(R.id.get_started_button)).check(doesNotExist())
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3Description_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_skipButtonIsNotVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_getStartedButtonIsVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.get_started_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_clickGetStartedButton_opensProfileActivity() {
    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.get_started_button)).perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testOnboardingFragment_swipeLeftOnLastSlide_doesNotWork() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 3))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_slide_view_pager)).perform(swipeLeft())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_slide0Title_changeOrientation_titleIsCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(getOnboardingSlide0Title())))
    }
  }

  @Test
  fun testOnboardingFragment_moveToSlide1_changeOrientation_titleIsCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_1_title)))
    }
  }

  @Test
  fun testOnboardingFragment_clickOnSkip_changeOrientation_titleIsCorrect() {
    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_nextArrowIcon_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_fragment_next_image_view)).check(
        matches(
          withContentDescription(
            R.string.next_arrow
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_configChange_nextArrowIcon_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.onboarding_fragment_next_image_view)).check(
        matches(
          withContentDescription(
            R.string.next_arrow
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_moveToSlide1_bottomDots_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.slide_dots_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.onboarding_slide_dots_content_description, 2, 4)
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_configChange_moveToSlide1_bottomDots_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 1))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.slide_dots_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.onboarding_slide_dots_content_description, 2, 4)
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_moveToSlide2_bottomDots_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.slide_dots_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.onboarding_slide_dots_content_description, 3, 4)
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_configChange_moveToSlide2_bottomDots_hasCorrectContentDescription() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPosition(position = 2))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.slide_dots_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.onboarding_slide_dots_content_description, 3, 4)
          )
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_policiesLinkIsVisible() {
    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.skip_text_view)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.slide_terms_of_service_and_privacy_policy_links_text_view)).perform(
        scrollTo()
      )
      onView(withId(R.id.slide_terms_of_service_and_privacy_policy_links_text_view)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_onboardingV2Enabled_languageSelectionScreenIsDisplayed() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)

    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.onboarding_language_activity_select_label)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testOnboardingFragment_onboardingV2Enabled_prefilledLanguageSelectionIsEnglish() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)

    launch(OnboardingActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.english_localized_language_name)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  // TODO add tests for supported app language default locales: arabic, portuguese, nigeria
  // Add test for unsupported default locale eg french
  // Add test for langauge list dropdown contents

  private fun getResources(): Resources =
    ApplicationProvider.getApplicationContext<Context>().resources

  private fun getAppName(): String = getResources().getString(R.string.app_name)

  private fun getOnboardingSlide0Title(): String =
    getResources().getString(R.string.onboarding_slide_0_title, getAppName())

  private fun scrollToPosition(position: Int): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "Scroll ViewPager2 to position: $position"
      }

      override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(ViewPager2::class.java)
      }

      override fun perform(uiController: UiController?, view: View?) {
        (view as ViewPager2).setCurrentItem(position, /* smoothScroll= */ false)
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
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
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(onboardingFragmentTest: OnboardingFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOnboardingFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(onboardingFragmentTest: OnboardingFragmentTest) {
      component.inject(onboardingFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
