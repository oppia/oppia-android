package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.app.story.testing.StoryFragmentTestActivity
import org.oppia.domain.topic.TEST_STORY_ID_1

/** Tests for [StoryFragment]. */
@RunWith(AndroidJUnit4::class)
class StoryFragmentTest {
  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun checkCorrectStoryCountLoadedInHeader() {
    launch<StoryFragmentTestActivity>(createTestActivityIntent(TEST_STORY_ID_1)).use {
      val headerString: String = getResources().getQuantityString(R.plurals.story_total_chapters, 3, 1, 3)

      onView(withId(R.id.story_progress_chapter_completed_text)).check(matches(withText(headerString)))
    }
  }

  @Test
  fun checkCorrectNumberOfStoriesLoadedInRecyclerView() {
    launch<StoryFragmentTestActivity>(createTestActivityIntent(TEST_STORY_ID_1)).use {
      onView(withId(R.id.story_chapter_list)).check(hasItemCount(4))
    }
  }

  private fun createTestActivityIntent(storyId: String): Intent {
    return StoryFragmentTestActivity.createTestActivityIntent(ApplicationProvider.getApplicationContext(), storyId)
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
