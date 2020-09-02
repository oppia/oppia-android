package org.oppia.app.testing

import android.view.View
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
import org.oppia.app.R
import org.oppia.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.app.ongoingtopiclist.OngoingTopicListFragment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OngoingTopicListSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getOngoingRecyclerViewGridLayoutManager(
    activity: OngoingTopicListActivity
  ): GridLayoutManager {
    return getOngoingRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getOngoingRecyclerView(activity: OngoingTopicListActivity): RecyclerView {
    return getOngoingTopicListFragment(activity).view?.findViewWithTag<View>(
      activity.resources.getString(R.string.ongoing_recycler_view_tag)
    )!! as RecyclerView
  }

  private fun getOngoingTopicListFragment(
    activity: OngoingTopicListActivity
  ): OngoingTopicListFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        OngoingTopicListFragment.TAG
      ) as OngoingTopicListFragment
  }

  @Test
  fun testOngoingTopicList_checkRecyclerView_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testOngoingTopicList_checkRecyclerView_land_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testOngoingTopicList_checkRecyclerView_tabletPort_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testOngoingTopicList_checkRecyclerView_tabletLand_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(4)
      }
    }
  }
}
