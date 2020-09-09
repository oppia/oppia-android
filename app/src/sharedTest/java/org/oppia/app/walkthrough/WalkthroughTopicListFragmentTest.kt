package org.oppia.app.walkthrough

import android.app.Application
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
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [WalkthroughTopicListFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WalkthroughTopicListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class WalkthroughTopicListFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createWalkthroughActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex0_topicHeader_topicHeaderIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          0,
          R.id.walkthrough_topic_header_text_view
        )
      ).check(
        matches(
          withText(R.string.what_do_you_want_to_learn)
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix WalkthroughTopicListFragmentTest
  @Ignore
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicCard_topicNameIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_topic_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("First Topic"))
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix WalkthroughTopicListFragmentTest
  @Ignore
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicCard_configurationChanged_topicNameIsCorrect() { // ktlint-disable max-line-length
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_topic_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("First Topic"))
        )
      )
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(walkthroughTopicListFragmentTest: WalkthroughTopicListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWalkthroughTopicListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(walkthroughTopicListFragmentTest: WalkthroughTopicListFragmentTest) {
      component.inject(walkthroughTopicListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
