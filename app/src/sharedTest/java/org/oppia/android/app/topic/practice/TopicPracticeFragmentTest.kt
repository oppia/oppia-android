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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.EnablePracticeTab
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.topic.questionplayer.QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
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
import org.oppia.android.util.logging.performancemetrics.MetricLogSchedulerModule

/** Tests for [TopicPracticeFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicPracticeFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicPracticeFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private var skillIdList = ArrayList<String>()
  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @JvmField
  @field:[Inject EnablePracticeTab]
  var enablePracticeTab: Boolean = false

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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 0,
          targetViewId = R.id.master_skills_text_view
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
        )
      ).check(matches(isCompletelyDisplayed()))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 1,
          targetViewId = R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
      clickPracticeItem(position = 2, targetViewId = R.id.subtopic_check_box)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 2,
          targetViewId = R.id.subtopic_check_box
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 1,
          targetViewId = R.id.subtopic_check_box
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_thenDeselect_startButtonIsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      scrollToPosition(position = 5)
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_clickStartButton_skillListTransferSuccessfully() { // ktlint-disable max-line-length
    testCoroutineDispatchers.unregisterIdlingResource()
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      scrollToPosition(position = 5)
      clickPracticeItem(position = 5, targetViewId = R.id.topic_practice_start_button)
      intended(hasComponent(QuestionPlayerActivity::class.java.name))
      intended(hasExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY, skillIdList))
    }
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 1,
          targetViewId = R.id.subtopic_check_box
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 5)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configChange_startButtonRemainsActive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 5)
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 5,
          targetViewId = R.id.topic_practice_start_button
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 0,
          targetViewId = R.id.master_skills_text_view
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
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 0,
          targetViewId = R.id.master_skills_text_view
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
        withText(TopicTab.getTabForPosition(position = 2, enablePracticeTab).name),
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
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickPracticeItem(position: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.topic_practice_skill_list,
        position = position,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class
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
