package org.oppia.app.testing

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.profile.ProfileChooserFragment
import org.robolectric.annotation.Config

private const val TAG_PROFILE_CHOOSER_FRAGMENT_RECYCLERVIEW = "profile_recycler_view"

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * document reffered :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 * */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileChooserSpanTest {

  private lateinit var context: Context
  private var activity: ProfileChooserFragmentTestActivity? = null

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    activity = getProfileChooserTestActivity()
      ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
  }

  @get:Rule
  var activityRule = ActivityScenarioRule(ProfileChooserFragmentTestActivity::class.java)

  private fun getProfileChooserTestActivity(): ProfileChooserFragmentTestActivity? {
    activityRule.scenario.onActivity {
      activity = it
    }
    return activity as ProfileChooserFragmentTestActivity
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForLdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(3)
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun testProfileChooserFragmentRecyclerView_landscape_mdpi_hasCorrectSpanCount() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(3)
    }
  }

  private fun getProfileRecyclerViewGridLayoutManager(activity: ProfileChooserFragmentTestActivity): GridLayoutManager {
    return getProfileRecyclerView(activity).layoutManager as GridLayoutManager
  }

  private fun getProfileRecyclerView(activity: ProfileChooserFragmentTestActivity): RecyclerView {
    return activity.findViewById(R.id.profile_recycler_view)
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForHdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(4)
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(5)
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXxhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(5)
    }
  }

  @Test
  @Config(qualifiers = "land-xxxhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXxxhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat(getProfileRecyclerViewGridLayoutManager(activity!!).spanCount).isEqualTo(5)
    }
  }
}
