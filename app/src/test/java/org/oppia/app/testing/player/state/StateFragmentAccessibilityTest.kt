package org.oppia.app.testing.player.state

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.accessibility.FakeAccessibilityManager
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@Config(application = StateFragmentAccessibilityTest.TestApplication::class)
class StateFragmentAccessibilityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var fakeAccessibilityManager: FakeAccessibilityManager

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    fakeAccessibilityManager.setTalkbackEnabled(true)
    FirebaseApp.initializeApp(context)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_loadDragDropExp_moveDownWithAccessibility() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()
      onView(
        RecyclerViewMatcher.atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_move_down_item
        )
      ).perform(click())
      onView(
        RecyclerViewMatcher.atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 1,
          targetViewId = R.id.drag_drop_content_text_view
        )
      ).check(matches(withText("I bought")))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_loadDragDropExp_moveUpWithAccessibility() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()
      onView(
        RecyclerViewMatcher.atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 1,
          targetViewId = R.id.drag_drop_move_up_item
        )
      ).perform(click())
      onView(
        RecyclerViewMatcher.atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_text_view
        )
      ).check(matches(withText("a camera at the store")))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun launchForExploration(
    explorationId: String
  ): ActivityScenario<StateFragmentTestActivity> {
    return ActivityScenario.launch(
      StateFragmentTestActivity.createTestActivityIntent(
        context, internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, explorationId
      )
    )
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      NetworkModule::class, LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(stateFragmentAccessibilityTest: StateFragmentAccessibilityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerStateFragmentAccessibilityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stateFragmentAccessibilityTest: StateFragmentAccessibilityTest) {
      component.inject(stateFragmentAccessibilityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
