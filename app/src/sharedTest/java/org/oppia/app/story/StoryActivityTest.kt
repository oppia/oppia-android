package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.domain.topic.TEST_STORY_ID_1

/** Tests for [StoryActivity]. */
@RunWith(AndroidJUnit4::class)
class StoryActivityTest {

  lateinit var context: Context
  lateinit var intent: Intent

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
      onView(withId(R.id.story_chapter_list)).check(CustomAssertions.hasItemCount(4))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  class CustomAssertions {
    companion object {
      fun hasItemCount(count: Int): ViewAssertion {
        return RecyclerViewItemCountAssertion(count)
      }
    }

    private class RecyclerViewItemCountAssertion(private val count: Int) : ViewAssertion {

      override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
          throw noViewFoundException
        }

        check(view is RecyclerView) { "The asserted view is not RecyclerView" }

        checkNotNull(view.adapter) { "No adapter is assigned to RecyclerView" }

        ViewMatchers.assertThat(
          "RecyclerView item count",
          view.adapter!!.itemCount,
          CoreMatchers.equalTo(count)
        )
      }
    }
  }
}
