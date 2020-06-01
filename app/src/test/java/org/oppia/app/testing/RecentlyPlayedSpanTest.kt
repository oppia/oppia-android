package org.oppia.app.testing

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.app.home.recentlyplayed.TAG_RECENTLY_PLAYED_FRAGMENT
import org.robolectric.annotation.LooperMode

private const val TAG_RECENTLY_PLAYED_FRAGMENT_RECYCLER_VIEW = "recently_played_recycler_view"

@RunWith(AndroidJUnit4::class)
class RecentlyPlayedSpanTest {

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @LooperMode(LooperMode.Mode.PAUSED)
  @Test
  fun testA() {
    launch(RecentlyPlayedActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        Truth.assertThat(getOngoingStoryRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  private fun getOngoingStoryRecyclerViewGridLayoutManager(activity: RecentlyPlayedActivity): GridLayoutManager {
    return getOngoingStoryRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getOngoingStoryRecyclerView(activity: RecentlyPlayedActivity): RecyclerView {
    return getRecentlyPlayedFragment(activity).view?.findViewWithTag<View>(
      TAG_RECENTLY_PLAYED_FRAGMENT_RECYCLER_VIEW
    )!! as RecyclerView
  }

  private fun getRecentlyPlayedFragment(activity: RecentlyPlayedActivity): RecentlyPlayedFragment {
    return activity.supportFragmentManager.findFragmentByTag(TAG_RECENTLY_PLAYED_FRAGMENT) as RecentlyPlayedFragment
  }
}