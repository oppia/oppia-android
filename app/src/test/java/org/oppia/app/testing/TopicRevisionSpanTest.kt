package org.oppia.app.testing

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
import org.oppia.app.topic.revision.TopicRevisionFragment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicRevisionSpanTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getRevisionSpanCount(activity: TopicRevisionTestActivity): Int {
    val revisionFragment = activity
      .supportFragmentManager
      .findFragmentByTag(TopicRevisionFragment.TOPIC_REVISION_FRAGMENT_TAG)
    val recyclerviewTag = activity.resources.getString(R.string.topic_revision_recyclerview_tag)
    val recyclerView =
      revisionFragment?.view?.findViewWithTag<RecyclerView>(recyclerviewTag)
    return (recyclerView?.layoutManager as GridLayoutManager).spanCount
  }

  @Test
  fun testTopicRevisionFragmentRecyclerView_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testTopicRevisionFragmentRecyclerView_land_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testTopicRevisionFragmentRecyclerView_tabletPort_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testTopicRevisionFragmentRecyclerView_tabletLand_hasCorrectSpanCount() {
    launch(TopicRevisionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getRevisionSpanCount(activity)).isEqualTo(4)
      }
    }
  }
}
