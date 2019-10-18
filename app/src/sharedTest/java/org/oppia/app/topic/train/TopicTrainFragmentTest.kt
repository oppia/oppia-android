package org.oppia.app.topic.train

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.topic.TopicActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import org.oppia.app.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import androidx.test.rule.ActivityTestRule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity

/** Tests for [TopicTrainFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicTrainFragmentTest {

  private var skillIdList = ArrayList<String>()

  private lateinit var activityScenario: ActivityScenario<TopicActivity>

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    activityScenario = ActivityScenario.launch(TopicActivity::class.java)

    Intents.init()
    skillIdList.add("test_skill_id_0")
  }

  @Test
  fun testTopicTrainFragment_loadFragment_displaySkills_startButtonIsInactive() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(withId(R.id.master_skills_text_view)).check(matches(withText(R.string.topic_train_master_these_skills)))
      onView(atPosition(R.id.skill_recycler_view, 0)).check(matches(hasDescendant(withId(R.id.skill_check_box))))
      onView(withId(R.id.topic_train_start_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_isSuccessful() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
      onView(atPosition(R.id.skill_recycler_view, 1)).perform(click())
    }
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_startButtonIsActive() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
      onView(withId(R.id.topic_train_start_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_deselectSkills_isSuccessful() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
    }
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_deselectSkills_startButtonIsInactive() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
      onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
      onView(withId(R.id.topic_train_start_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_clickStartButton_skillListTransferSuccessfully() {
    activityTestRule.launchActivity(null)
    onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
    onView(withId(R.id.topic_train_start_button)).perform(click())
    intended(hasComponent(QuestionPlayerActivity::class.java.name))
    intended(hasExtra(QuestionPlayerActivity.getIntentKey(), skillIdList))
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_configurationChange_skillsAreSelected() {
    onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(atPositionOnView(R.id.skill_recycler_view, 0, R.id.skill_check_box)).check(matches(isChecked()))
  }

  @Test
  fun testTopicTrainFragment_loadFragment_configurationChange_startButtonRemainsInactive() {
    onView(withId(R.id.topic_train_start_button)).check(matches(not(isClickable())))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(withId(R.id.topic_train_start_button)).check(matches(not(isClickable())))
  }

  @Test
  fun testTopicTrainFragment_loadFragment_selectSkills_configurationChange_startButtonRemainsActive() {
    onView(atPosition(R.id.skill_recycler_view, 0)).perform(click())
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(withId(R.id.topic_train_start_button)).check(matches(isClickable()))
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }
  }
}
