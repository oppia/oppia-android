package org.oppia.android.app.testing

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.completedstorylist.CompletedStoryListFragment.Companion.COMPLETED_STORY_LIST_FRAGMENT_TAG
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CompletedStoryListSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getCompletedStoryListSpanCount(activity: CompletedStoryListActivity): Int {
    val completedStoryListFragment =
      activity.supportFragmentManager.findFragmentByTag(COMPLETED_STORY_LIST_FRAGMENT_TAG)
    val completedStoryListRecyclerVIew =
      completedStoryListFragment?.view?.findViewWithTag<RecyclerView>(
        activity.resources.getString(
          R.string.completed_story_list_recyclerview_tag
        )
      )
    return (completedStoryListRecyclerVIew?.layoutManager as GridLayoutManager).spanCount
  }

  @Test
  fun testCompletedStoryList_checkRecyclerViewSpanCount_spanIsCorrect() {
    launch(CompletedStoryListActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getCompletedStoryListSpanCount(activity)).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testCompletedStoryList_checkRecyclerViewSpanCount_land_spanIsCorrect() {
    launch(CompletedStoryListActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getCompletedStoryListSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testCompletedStoryList_checkRecyclerViewSpanCount_tabletPort_spanIsCorrect() {
    launch(CompletedStoryListActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getCompletedStoryListSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testCompletedStoryList_checkRecyclerViewSpanCount_tabletLand_spanIsCorrect() {
    launch(CompletedStoryListActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getCompletedStoryListSpanCount(activity)).isEqualTo(4)
      }
    }
  }
}
