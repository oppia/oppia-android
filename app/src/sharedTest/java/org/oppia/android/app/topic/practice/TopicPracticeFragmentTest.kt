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
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
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
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.QuestionPlayerActivityParams
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.TopicPracticeFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity.Companion.QUESTION_PLAYER_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
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
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
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
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
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
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private var skillIdList = ArrayList<String>()
  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @field:[Inject EnableExtraTopicTabsUi]
  lateinit var enableExtraTopicTabsUiValue: PlatformParameterValue<Boolean>

  @Before
  fun setUp() {
    Intents.init()
    TestPlatformParameterModule.forceEnableExtraTopicTabsUi(true)
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    skillIdList.add("5RM9KPfQxobH")
    skillIdList.add("B39yK4cbHZYI")
    markAllSpotlightsSeen()
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
      clickPracticeTab()
      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      scrollToPosition(position = 5)
      clickPracticeItem(position = 5, targetViewId = R.id.topic_practice_start_button)
      val args = QuestionPlayerActivityParams.newBuilder().addAllSkillIds(skillIdList).build()
      intended(hasComponent(QuestionPlayerActivity::class.java.name))
      intended(hasProtoExtra(QUESTION_PLAYER_ACTIVITY_PARAMS_KEY, args))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_skillsAreSelected() {
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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
    launchTopicActivityIntent(profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID).use {
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

  @Test
  fun testFragment_argumentsAreCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use { scenario ->
      clickPracticeTab()
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val topicFragment = activity.supportFragmentManager
          .findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment
        val viewPager = topicFragment.requireView()
          .findViewById<ViewPager2>(R.id.topic_tabs_viewpager)
        val topicPracticeFragment = topicFragment.childFragmentManager
          .findFragmentByTag("f${viewPager.currentItem}") as TopicPracticeFragment

        val args = topicPracticeFragment.arguments?.getProto(
          TopicPracticeFragment.TOPIC_PRACTICE_FRAGMENT_ARGUMENTS_KEY,
          TopicPracticeFragmentArguments.getDefaultInstance()
        )
        val receivedInternalProfileId = topicPracticeFragment
          .arguments?.extractCurrentUserProfileId()?.internalId ?: -1
        val receivedTopicId = checkNotNull(args?.topicId) {
          "Expected topic ID to be included in arguments for TopicPracticeFragment."
        }

        assertThat(receivedInternalProfileId).isEqualTo(profileId.internalId)
        assertThat(receivedTopicId).isEqualTo(FRACTIONS_TOPIC_ID)
      }
    }
  }

  @Test
  fun testTopicPracticeFragment_saveInstanceState_verifyCorrectStateRestored() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use { scenario ->
      clickPracticeTab()
      testCoroutineDispatchers.runCurrent()

      clickPracticeItem(position = 1, targetViewId = R.id.subtopic_check_box)
      clickPracticeItem(position = 2, targetViewId = R.id.subtopic_check_box)

      scenario.recreate()

      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 1,
          targetViewId = R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.topic_practice_skill_list,
          position = 2,
          targetViewId = R.id.subtopic_check_box
        )
      ).check(matches(isChecked()))
    }
  }

  private fun launchTopicActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        profileId,
        classroomId,
        topicId
      )
    return ActivityScenario.launch(intent)
  }

  private fun clickPracticeTab() {
    testCoroutineDispatchers.runCurrent()
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position = 2, enableExtraTopicTabsUiValue.value).name),
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

  private fun markAllSpotlightsSeen() {
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_LESSON_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_REVISION_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestPlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
