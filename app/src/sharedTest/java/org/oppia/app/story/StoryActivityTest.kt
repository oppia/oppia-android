package org.oppia.app.story

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.domain.topic.TEST_EXPLORATION_ID_1
import org.oppia.domain.topic.TEST_STORY_ID_1

/** Tests for [StoryActivity]. */
@RunWith(AndroidJUnit4::class)
class StoryActivityTest {
  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun clickOnStory_intentsToExplorationActivity() {
    launch<StoryActivity>(createStoryActivityIntent(TEST_STORY_ID_1)).use {
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.story_chapter_list)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
          1,
          click()
        )
      )

      intended(
        allOf(
          hasExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, TEST_EXPLORATION_ID_1),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  private fun createStoryActivityIntent(storyId: String): Intent {
    return StoryActivity.createStoryActivityIntent(ApplicationProvider.getApplicationContext(), storyId)
  }
}
