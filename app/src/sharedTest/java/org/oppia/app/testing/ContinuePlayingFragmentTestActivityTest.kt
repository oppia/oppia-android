package org.oppia.app.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.story.StoryActivity

/** Tests for [ContinuePlayingFragmentTestActivity]. */
@RunWith(AndroidJUnit4::class)
class ContinuePlayingFragmentTestActivityTest {

  @get:Rule
  var activityFragmentTestRule: ActivityTestRule<ContinuePlayingFragmentTestActivity> = ActivityTestRule(
    ContinuePlayingFragmentTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testContinuePlayingTestActivity_recyclerViewItem4_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    activityFragmentTestRule.launchActivity(null)
    onView(withId(R.id.ongoing_story_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        4
      )
    )
    onView(atPosition(R.id.ongoing_story_recycler_view, 4)).perform(click())
    intended(hasComponent(StoryActivity::class.java.name))
    intended(hasExtra(StoryActivity.STORY_ACTIVITY_INTENT_EXTRA, "test_story_id_0"))
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
