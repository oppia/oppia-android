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
import org.oppia.app.profile.ProfileChooserFragment
import org.robolectric.annotation.Config

private const val TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW = "profile_recycler_view"

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * Reference document :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileChooserSpanTest {

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

  private fun getProfileRecyclerViewGridLayoutManager(
    activity: ProfileChooserFragmentTestActivity
  ): GridLayoutManager {
    return getProfileRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getProfileRecyclerView(activity: ProfileChooserFragmentTestActivity): RecyclerView {
    return getProfileChooserFragment(activity).view?.findViewWithTag<View>(
      TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLER_VIEW
    )!! as RecyclerView
  }

  @Test
  fun testProfileChooserFragmentRecyclerView_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-ldpi")
  fun testProfileChooserFragmentRecyclerView_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-mdpi")
  fun testProfileChooserFragmentRecyclerView_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-hdpi")
  fun testProfileChooserFragmentRecyclerView_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xhdpi")
  fun testProfileChooserFragmentRecyclerView_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "port-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(2)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  fun testProfileChooserFragmentRecyclerView_landscape_ldpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(3)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_hdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(4)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  @Test
  @Config(qualifiers = "land-xxxhdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_xxxhdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(getProfileRecyclerViewGridLayoutManager(activity).spanCount)
          .isEqualTo(5)
      }
    }
  }

  private fun getProfileChooserFragment(
    activity: ProfileChooserFragmentTestActivity
  ): ProfileChooserFragment {
    return activity
      .supportFragmentManager
      .findFragmentByTag(
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ) as ProfileChooserFragment
  }
}
