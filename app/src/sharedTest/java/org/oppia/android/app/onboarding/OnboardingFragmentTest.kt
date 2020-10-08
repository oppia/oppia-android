package org.oppia.android.app.onboarding

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.ViewPagerActions.scrollToPage
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withAlpha
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
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
  fun testOnboardingFragment_checkDefaultSlideTitle_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_0_title)))
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
  fun testOnboardingFragment_checkDefaultSlideImage_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(
        allOf(
          withId(R.id.slide_image_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withContentDescription(R.string.onboarding_slide_0_title)))
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
  fun testOnboardingFragment_checkDefaultSlide_clickSkipButton_shiftsToLastSlide() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.skip_text_view)).perform(scrollTo(), click())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
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
      ).check(matches(withText(R.string.onboarding_slide_0_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_1_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1Image_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
      onView(
        allOf(
          withId(R.id.slide_image_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withContentDescription(R.string.onboarding_slide_1_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_index1DotIsActive_otherDotsAreInactive() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
      onView(withId(R.id.skip_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide1_clickSkipButton_shiftsToLastSlide() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
      onView(withId(R.id.skip_text_view)).perform(scrollTo(), click())
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
      onView(withId(R.id.get_started_button)).check(doesNotExist())
    }
  }

  @Test
  fun testOnboardingFragment_swipeLeftThenSwipeRight_isWorking() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(swipeLeft())
      onView(withId(R.id.onboarding_slide_view_pager)).perform(swipeRight())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_0_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_2_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2Image_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
      onView(
        allOf(
          withId(R.id.slide_image_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withContentDescription(R.string.onboarding_slide_2_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_index2DotIsActive_otherDotsAreInactive() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
      onView(withId(R.id.skip_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide2_clickSkipButton_shiftsToLastSlide() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
      onView(withId(R.id.skip_text_view)).perform(scrollTo(), click())
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(2))
      onView(withId(R.id.get_started_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3Title_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
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
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
      onView(
        allOf(
          withId(R.id.slide_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_description)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3Image_isCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
      onView(
        allOf(
          withId(R.id.slide_image_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withContentDescription(R.string.onboarding_slide_3_title)))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_skipButtonIsNotVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
      onView(withId(R.id.skip_text_view)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_getStartedButtonIsVisible() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
      onView(withId(R.id.get_started_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testOnboardingFragment_checkSlide3_clickGetStartedButton_opensProfileActivity() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
      onView(withId(R.id.get_started_button)).perform(scrollTo(), click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testOnboardingFragment_swipeLeftOnLastSlide_doesNotWork() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(3))
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
      ).check(matches(withText(R.string.onboarding_slide_0_title)))
    }
  }

  @Test
  fun testOnboardingFragment_moveToSlide1_changeOrientation_titleIsCorrect() {
    launch(OnboardingActivity::class.java).use {
      onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
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
      onView(withId(R.id.skip_text_view)).perform(scrollTo(), click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.slide_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.onboarding_slide_3_title)))
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
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
