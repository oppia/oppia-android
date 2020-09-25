package org.oppia.android.app.story

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
import com.google.firebase.FirebaseApp
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_1
import org.oppia.android.domain.topic.TEST_STORY_ID_1
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.robolectric.annotation.LooperMode

/** Tests for [StoryActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class StoryActivityTest {

  private val internalProfileId = 0

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun clickOnStory_intentsToExplorationActivity() {
    launch<StoryActivity>(
      createStoryActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
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
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
            TEST_EXPLORATION_ID_1
          ),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  private fun createStoryActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      storyId
    )
  }
}
