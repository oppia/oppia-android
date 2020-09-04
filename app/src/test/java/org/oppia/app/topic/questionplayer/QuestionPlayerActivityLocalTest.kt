package org.oppia.app.topic.questionplayer

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.shim.ViewBindingShimModule
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
import org.oppia.domain.oppialogger.loguploader.FirebaseLogUploaderModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
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
    onView(withId(R.id.text_input_interaction_view)).perform(appendText("1"))
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
   * properly input digits for text views using 'android:digits'. See
   * https://github.com/robolectric/robolectric/issues/5110 for specifics.
   */
  private fun appendText(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "appendText($text)"
      }

      override fun getConstraints(): Matcher<View> {
        return CoreMatchers.allOf(isEnabled())
      }

      override fun perform(uiController: UiController?, view: View?) {
        (view as? EditText)?.append(text)
        testCoroutineDispatchers.runCurrent()
      }
    }
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
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
