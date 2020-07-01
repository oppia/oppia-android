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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.app.ongoingtopiclist.OngoingTopicListFragment
import org.robolectric.annotation.Config

private const val TAG_ONGOING_RECYCLER_VIEW = "TAG_ONGOING_RECYCLER_VIEW"

@RunWith(AndroidJUnit4::class)
class OngoingTopicListSpanTest {

  @Before
  @ExperimentalCoroutinesApi
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
      TAG_ONGOING_RECYCLER_VIEW
    )!! as RecyclerView
  }

  private fun getOngoingTopicListFragment(
    activity: OngoingTopicListActivity
  ): OngoingTopicListFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        OngoingTopicListFragment.TAG_ONGOING_TOPIC_LIST_FRAGMENT
      ) as OngoingTopicListFragment
  }

  @Test
  fun testOngoingTopicListFragmentRecyclerView_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testOngoingTopicListFragmentRecyclerView_land_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testOngoingTopicListFragmentRecyclerView_tablet_port_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testOngoingTopicListFragmentRecyclerView_tablet_land_hasCorrectSpanCount() {
    launch(OngoingTopicListActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getOngoingRecyclerViewGridLayoutManager(activity).spanCount).isEqualTo(4)
      }
    }
  }
}
