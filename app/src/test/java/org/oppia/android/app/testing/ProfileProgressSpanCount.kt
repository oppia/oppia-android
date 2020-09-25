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
import org.oppia.android.app.profileprogress.ProfileProgressActivity
import org.oppia.android.app.profileprogress.ProfileProgressFragment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ProfileProgressSpanCount {
  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getProfileProgressSpanCount(activity: ProfileProgressActivity): Int {
    val profileProgressFragment =
      activity.supportFragmentManager
        .findFragmentById(R.id.profile_progress_fragment_placeholder) as ProfileProgressFragment
    val profileProgressRecyclerView =
      profileProgressFragment.view?.findViewById<RecyclerView>(R.id.profile_progress_list)
    return (profileProgressRecyclerView?.layoutManager as GridLayoutManager).spanCount
  }

  @Test
  fun testProfileProgress_checkRecyclerViewSpanCount_spanIsCorrect() {
    launch<ProfileProgressActivity>(ProfileProgressActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getProfileProgressSpanCount(activity)).isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testProfileProgress_checkRecyclerViewSpanCount_land_spanIsCorrect() {
    launch<ProfileProgressActivity>(ProfileProgressActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getProfileProgressSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-port")
  fun testProfileProgress_checkRecyclerViewSpanCount_tabletPort_spanIsCorrect() {
    launch<ProfileProgressActivity>(ProfileProgressActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getProfileProgressSpanCount(activity)).isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp-land")
  fun testProfileProgress_checkRecyclerViewSpanCount_tabletLand_spanIsCorrect() {
    launch<ProfileProgressActivity>(ProfileProgressActivity::class.java).use {
      it.onActivity { activity ->
        assertThat(getProfileProgressSpanCount(activity)).isEqualTo(4)
      }
    }
  }
}
