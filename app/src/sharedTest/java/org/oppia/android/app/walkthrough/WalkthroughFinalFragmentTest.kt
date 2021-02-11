package org.oppia.android.app.walkthrough

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
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
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
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

/** Tests for [WalkthroughFinalFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WalkthroughFinalFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class WalkthroughFinalFragmentTest {

  @Inject
  lateinit var context: Context

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

  private fun createWalkthroughActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      context,
      profileId
    )
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_firstTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("First Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_secondTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_configChange_secondTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_yesNoBtnIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_final_yes_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_clickNoBtn_noBtnWorksCorrectly() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(walkthroughFinalFragmentTest: WalkthroughFinalFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWalkthroughFinalFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(walkthroughFinalFragmentTest: WalkthroughFinalFragmentTest) {
      component.inject(walkthroughFinalFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
