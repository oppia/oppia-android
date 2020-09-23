package org.oppia.app.topic.revision

import android.app.Application
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
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
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
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

/** Tests for [TopicRevisionFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicRevisionFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicRevisionFragmentTest {
  private val subtopicThumbnail = R.drawable.topic_fractions_01
  private val internalProfileId = 0

  @get:Rule
  var topicActivityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_displayReviewTopics_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_selectReviewTopics_opensReviewActivity() {
    topicActivityTestRule.launchActivity(
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID
      )
    )
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(3).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPosition(R.id.revision_recycler_view, 0)).perform(click())
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_selectReviewTopics_reviewCardDisplaysCorrectExplanation() { // ktlint-disable max-line-length
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 1)).perform(click())
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            withText(
              "Description of subtopic is here."
            )
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view)).check(
        matches(
          hasDescendant(
            withDrawable(
              subtopicThumbnail
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_checkSpanCoun_isTwo() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view))
        .check(
          GridLayoutManagerColumnCountAssertion(
            2
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicPracticeFragment_loadFragment_configurationChange_reviewSubtopicsAreDisplayed() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPosition(R.id.revision_recycler_view, 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view)).check(
        matches(
          hasDescendant(
            withDrawable(
              subtopicThumbnail
            )
          )
        )
      )
    }
  }

  @Test
  // TODO(#973): Fix TopicRevisionFragmentTest
  @Ignore
  fun testTopicRevisionFragment_loadFragment_configurationChange_checkSpanCount_isThree() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(3).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.revision_recycler_view))
        .check(
          GridLayoutManagerColumnCountAssertion(
            3
          )
        )
    }
  }

  private fun launchTopicActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        topicId
      )
    return ActivityScenario.launch(intent)
  }

  class GridLayoutManagerColumnCountAssertion(expectedColumnCount: Int) : ViewAssertion {
    private var expectedColumnCount: Int = 0

    init {
      this.expectedColumnCount = expectedColumnCount
    }

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      val recyclerView = view as RecyclerView
      if (recyclerView.layoutManager is GridLayoutManager) {
        val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
        val spanCount = gridLayoutManager.spanCount
        if (spanCount != expectedColumnCount) {
          val errorMessage =
            ("expected column count " + expectedColumnCount + " but was " + spanCount)
          throw AssertionError(errorMessage)
        }
      } else {
        throw IllegalStateException("no grid layout manager")
      }
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

    fun inject(topicRevisionFragmentTest: TopicRevisionFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicRevisionFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicRevisionFragmentTest: TopicRevisionFragmentTest) {
      component.inject(topicRevisionFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
