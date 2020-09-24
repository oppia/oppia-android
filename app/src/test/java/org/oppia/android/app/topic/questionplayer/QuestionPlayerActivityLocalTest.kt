package org.oppia.android.app.topic.questionplayer

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationContext
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [QuestionPlayerActivity] that can only be run locally, e.g. using Robolectric, and not on an
 * emulator.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = QuestionPlayerActivityLocalTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class QuestionPlayerActivityLocalTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val SKILL_ID_LIST = arrayListOf(TEST_SKILL_ID_1)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_checkPreviousHeaderVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_checkPreviousHeaderCollapsed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_expandResponse_checkPreviousHeaderExpanded() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view))
        .perform(scrollToViewType(StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER))
      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 6)
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_expandCollapseResponse_checkPreviousHeaderCollapsed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )

      onView(withId(R.id.question_recycler_view))
        .perform(scrollToViewType(StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER))
      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 6)
        )
      )

      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )
    }
  }

  private fun launchForQuestionPlayer(
    skillIdList: ArrayList<String>
  ): ActivityScenario<QuestionPlayerActivity> {
    return ActivityScenario.launch(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        context, skillIdList
      )
    )
  }

  private fun submitTwoWrongAnswersToQuestionPlayer() {
    submitWrongAnswerToQuestionPlayerFractionInput()
    submitWrongAnswerToQuestionPlayerFractionInput()
  }

  private fun submitWrongAnswerToQuestionPlayerFractionInput() {
    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION))
    onView(withId(R.id.text_input_interaction_view)).perform(editTextInputAction.appendText("1"))
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType): ViewAction {
    return scrollToHolder(StateViewHolderTypeMatcher(viewType))
  }

  /**
   * [BaseMatcher] that matches against the first occurrence of the specified view holder type in
   * StateFragment's RecyclerView.
   */
  private class StateViewHolderTypeMatcher(
    private val viewType: StateItemViewModel.ViewType
  ) : BaseMatcher<RecyclerView.ViewHolder>() {
    override fun describeTo(description: Description?) {
      description?.appendText("item view type of $viewType")
    }

    override fun matches(item: Any?): Boolean {
      return (item as? RecyclerView.ViewHolder)?.itemViewType == viewType.ordinal
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
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(questionPlayerActivityLocalTest: QuestionPlayerActivityLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerQuestionPlayerActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(questionPlayerActivityLocalTest: QuestionPlayerActivityLocalTest) {
      component.inject(questionPlayerActivityLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
