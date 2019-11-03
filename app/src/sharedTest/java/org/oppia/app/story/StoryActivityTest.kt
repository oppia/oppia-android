package org.oppia.app.story

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.domain.topic.TEST_STORY_ID_1

/** Tests for [StoryActivity]. */
@RunWith(AndroidJUnit4::class)
class StoryActivityTest {

  lateinit var context: Context
  private lateinit var intent: Intent

  @Before
  fun setUp() {
    Intents.init()
    context = ApplicationProvider.getApplicationContext()
    intent = StoryActivity.createStoryActivityIntent(context, TEST_STORY_ID_1)
  }

  @Test
  fun checkCorrectStoryCountLoadedInHeader() {
    launch<StoryActivity>(intent).use {
      val headerString: String =
        context.resources.getQuantityString(R.plurals.story_total_chapters, 3, 1, 3)

      onView(withId(R.id.story_progress_chapter_completed_text)).check(matches(withText(headerString)))
    }
  }

  @Test
  fun checkCorrectNumberOfStoriesLoadedInRecyclerView() {
    launch<StoryActivity>(intent).use {
      onView(withId(R.id.story_chapter_list)).check(hasItemCount(4))
    }
  }

  @Test
  fun clickOnStory_intentsToExplorationActivity() {
    launch<StoryActivity>(intent).use {
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

      intended(hasComponent(ExplorationActivity::class.java.name))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
