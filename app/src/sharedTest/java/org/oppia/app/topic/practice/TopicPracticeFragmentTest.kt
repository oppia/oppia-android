package org.oppia.app.topic.practice

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicPracticeFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicPracticeFragmentTest {

  private var skillIdList = ArrayList<String>()

  private lateinit var activityScenario: ActivityScenario<TopicActivity>

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    activityScenario = launchTopicActivityIntent(TEST_TOPIC_ID_0)

    Intents.init()
    skillIdList.add("test_skill_id_0")
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_displaySkills_startButtonIsInactive() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.master_skills_text_view)).check(matches(withText(R.string.topic_practice_master_these_skills)))
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box))
        .check(matches(withId(R.id.skill_check_box)))
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_isSuccessful() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 2, R.id.skill_check_box)).perform(click())
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_startButtonIsActive() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
      onView(withId(R.id.topic_practice_start_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_deselectSkills_isSuccessful() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_deselectSkills_startButtonIsInactive() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
      onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_clickStartButton_skillListTransferSuccessfully() {
    launchTopicActivityIntent(TEST_TOPIC_ID_0)
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(2).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
    onView(withId(R.id.topic_practice_start_button)).perform(click())
    intended(hasComponent(QuestionPlayerActivity::class.java.name))
    intended(hasExtra(QuestionPlayerActivity.getIntentKey(), skillIdList))
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_skillsAreSelected() {
    onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).check(matches(isChecked()))
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicPracticeFragment_loadFragment_configurationChange_startButtonRemainsInactive() {
    onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_startButtonRemainsActive() {
    onView(atPositionOnView(R.id.topic_practice_skill_list, 1, R.id.skill_check_box)).perform(click())
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    activityScenario.recreate()
    onView(withId(R.id.topic_practice_start_button)).check(matches(isClickable()))
  }

  private fun launchTopicActivityIntent(topicId: String): ActivityScenario<TopicActivity> {
    val intent = TopicActivity.createTopicActivityIntent(ApplicationProvider.getApplicationContext(), topicId)
    return ActivityScenario.launch(intent)
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
