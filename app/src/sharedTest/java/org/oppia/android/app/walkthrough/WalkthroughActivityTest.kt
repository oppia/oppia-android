package org.oppia.android.app.walkthrough

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
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
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.ProgressMatcher.Companion.withProgress
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
import javax.inject.Singleton

/** Tests for [WalkthroughActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WalkthroughActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class WalkthroughActivityTest {
  @Test
  fun testWalkthroughFragment_defaultProgress_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_checkFrameLayout_backButton_progressBar_IsVisible() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_fragment_placeholder)).check(matches(isDisplayed()))
      onView(withId(R.id.back_button)).check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_configurationChanged_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  @Test
  fun testWalkthroughFragment_nextBtn_configurationChanged_backBtn_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.back_button)).perform(click())
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.walkthrough_welcome_description)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_onBackPressed_decreaseProgress_progressWorksCorrectly() { // ktlint-disable max-line-length
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      pressBack()
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_decreaseProgress_progressWorksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      onView(withId(R.id.back_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
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

    fun inject(walkthroughActivityTest: WalkthroughActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWalkthroughActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(walkthroughActivityTest: WalkthroughActivityTest) {
      component.inject(walkthroughActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
