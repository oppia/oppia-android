package org.oppia.android.app.topic.practice

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
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
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
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

/** Tests for [TopicPracticeFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicPracticeFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicPracticeFragmentTest {

  private var skillIdList = ArrayList<String>()
  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    skillIdList.add("5RM9KPfQxobH")
    skillIdList.add("B39yK4cbHZYI")
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
  fun testTopicPracticeFragment_loadFragment_displaySubtopics_startButtonIsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          0,
          R.id.master_skills_text_view
        )
      ).check(
        matches(
          withText(
            R.string.topic_practice_master_these_skills
          )
        )
      )
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 5)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(isCompletelyDisplayed()))
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1,
          R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
      clickPracticeItem(position = 2, targetViewId = R.id.subtopic_check_box)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          2,
          R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_startButtonIsActive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 5)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_thenDeselect_selectsCorrectTopic() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1,
          R.id.subtopic_check_box
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_deselectSubtopics_startButtonIsInactive() { // ktlint-disable max-line-length
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      scrollToPosition(position = 5)
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_clickStartButton_skillListTransferSuccessfully() { // ktlint-disable max-line-length
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID)
    clickPracticeTab()
    clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
    scrollToPosition(position = 5)
    clickPracticeItem(position = 5, targetViewId = R.id.topic_practice_start_button)
    intended(hasComponent(QuestionPlayerActivity::class.java.name))
    intended(hasExtra(QuestionPlayerActivity.getIntentKey(), skillIdList))
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_skillsAreSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1,
          R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_configurationChange_startButtonRemainsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      scrollToPosition(position = 5)
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 5)
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_startButtonRemainsActive() { // ktlint-disable max-line-length
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 5)
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          5,
          R.id.topic_practice_start_button
        )
      ).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_changeOrientation_titleIsCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          0,
          R.id.master_skills_text_view
        )
      ).check(
        matches(
          withText(
            R.string.topic_practice_master_these_skills
          )
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          0,
          R.id.master_skills_text_view
        )
      ).check(
        matches(
          withText(
            R.string.topic_practice_master_these_skills
          )
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

  private fun clickPracticeTab() {
    testCoroutineDispatchers.runCurrent()
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(2).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.topic_practice_skill_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun clickPracticeItem(position: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        R.id.topic_practice_skill_list,
        position,
        targetViewId
      )
    ).perform(click())
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

    fun inject(topicPracticeFragmentTest: TopicPracticeFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicPracticeFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicPracticeFragmentTest: TopicPracticeFragmentTest) {
      component.inject(topicPracticeFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
