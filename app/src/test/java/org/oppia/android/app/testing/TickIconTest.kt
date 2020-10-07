package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import com.google.firebase.FirebaseApp
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
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
import org.oppia.android.app.recyclerview.RecyclerViewMatcher
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.utility.OrientationChangeAction
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
import org.oppia.android.domain.topic.FRACTIONS_SKILL_ID_0
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.CoroutineExecutorService
import org.oppia.android.testing.EditTextInputAction
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
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TickIconTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TickIconTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  private val SKILL_ID_LIST = arrayListOf(FRACTIONS_SKILL_ID_0)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)

    // Initialize Glide such that all of its executors use the same shared dispatcher pool as the
    // rest of Oppia so that thread execution can be synchronized via Oppia's test coroutine
    // dispatchers.
    val executorService = MockGlideExecutor.newTestExecutor(
      CoroutineExecutorService(backgroundCoroutineDispatcher)
    )
    Glide.init(
      context,
      GlideBuilder().setDiskCacheExecutor(executorService)
        .setAnimationExecutor(executorService)
        .setSourceExecutor(executorService)
    )
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testQuestionPlayer_submitAnswer_increaseTextSize_checkTickDoesNotIconOverlap() {
    launchForSkillList(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      selectMultipleChoiceOption(optionPosition = 3)


      onView(withId(R.id.submitted_answer_text_view))
    }
  }
  private fun scrollToViewType(viewType: StateItemViewModel.ViewType) {
    onView(withId(R.id.question_recycler_view)).perform(
      scrollToHolder(StateViewHolderTypeMatcher(viewType))
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun launchForSkillList(
    skillIdList: List<String>
  ): ActivityScenario<QuestionPlayerActivity> {
    val scenario = ActivityScenario.launch<QuestionPlayerActivity>(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        context, ArrayList(skillIdList)
      )
    )
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.question_recycler_view)).check(matches(ViewMatchers.isDisplayed()))
    return scenario
  }

  private fun rotateToLandscape() {
    onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  @Suppress("SameParameterValue")
  private fun selectMultipleChoiceOption(optionPosition: Int) {
    clickSelection(optionPosition, targetViewId = R.id.multiple_choice_radio_button)
  }

  @Suppress("SameParameterValue")
  private fun clickSelection(optionPosition: Int, targetViewId: Int) {
    scrollToViewType(StateItemViewModel.ViewType.SELECTION_INTERACTION)
    onView(
      RecyclerViewMatcher.atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = optionPosition,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
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
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(tickIconTest: TickIconTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTickIconTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(tickIconTest: TickIconTest) {
      component.inject(tickIconTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}