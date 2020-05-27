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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
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

  private fun getOngoingStoryRecyclerViewGridLayoutManager(activity: RecentlyPlayedActivity): GridLayoutManager {
    return getOngoingStoryRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getOngoingStoryRecyclerView(activity: RecentlyPlayedActivity): RecyclerView {
    return getRecentlyPlayedFragment(activity).view?.findViewWithTag<View>(
      TAG_RECENTLY_PLAYED_FRAGMENT
    )!! as RecyclerView
  }

  @Test
  fun testProfileChooserFragmentRecyclerView_hasCorrectSpanCount() {
    launch(RecentlyPlayedActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        Truth.assertThat(getOngoingStoryRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  private fun getRecentlyPlayedFragment(activity: RecentlyPlayedActivity): RecentlyPlayedFragment {
    return activity.supportFragmentManager.findFragmentByTag(TAG_RECENTLY_PLAYED_FRAGMENT) as RecentlyPlayedFragment
  }
}