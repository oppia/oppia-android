package org.oppia.app.topic.practice

import android.app.Application
import android.content.Context
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicPracticeFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicPracticeFragmentTest {

  private var skillIdList = ArrayList<String>()
  private val internalProfileId = 0
  private lateinit var activityScenario: ActivityScenario<TopicActivity>

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    activityScenario = launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID)

    Intents.init()
    skillIdList.add("5RM9KPfQxobH")
    skillIdList.add("B39yK4cbHZYI")
    skillIdList.add("UxTGIJqaHMLa")
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_displaySubtopics_startButtonIsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.master_skills_text_view))
        .check(
          matches(
            withText(
              R.string.topic_practice_master_these_skills
            )
          )
        )
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          2, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_startButtonIsActive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_deselectSubtopics_isSuccessful() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_deselectsubtopics_startButtonIsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    }
  }
  /* ktlint-enable max-line-length */

  /* ktlint-disable max-line-length */
  @Test
  fun testTopicPracticeFragment_loadFragment_selectSubtopics_clickStartButton_skillListTransferSuccessfully() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID)
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(2).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPositionOnView(
        R.id.topic_practice_skill_list,
        1, R.id.subtopic_check_box
      )
    ).perform(
      click()
    )
    onView(withId(R.id.topic_practice_skill_list)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        10
      )
    )
    onView(withId(R.id.topic_practice_start_button)).perform(click())
    intended(hasComponent(QuestionPlayerActivity::class.java.name))
    intended(hasExtra(QuestionPlayerActivity.getIntentKey(), skillIdList))
  }
  /* ktlint-enable max-line-length */

  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_skillsAreSelected() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).check(
        matches(isChecked())
      )
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_configurationChange_startButtonRemainsInactive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(not(isClickable())))
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testTopicPracticeFragment_loadFragment_selectSkills_configurationChange_startButtonRemainsActive() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.topic_practice_skill_list,
          1, R.id.subtopic_check_box
        )
      ).perform(
        click()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_practice_skill_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          10
        )
      )
      onView(withId(R.id.topic_practice_start_button)).check(matches(isClickable()))
    }
  }
  /* ktlint-enable max-line-length */

  @Test
  fun testTopicPracticeFragment_loadFragment_changeOrientation_titleIsCorrect() {
    launchTopicActivityIntent(internalProfileId, FRACTIONS_TOPIC_ID).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(2).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.master_skills_text_view)).check(matches(withText(R.string.topic_practice_master_these_skills)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.master_skills_text_view)).check(matches(withText(R.string.topic_practice_master_these_skills)))
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
    fun provideBackgroundDispatcher(
      @BlockingDispatcher blockingDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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
