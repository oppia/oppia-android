package org.oppia.app.testing

import android.content.Context
import android.content.res.Configuration
import android.view.View
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

/**
 * Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density.
 * document reffered :https://developer.android.com/reference/androidx/test/core/app/ActivityScenario
 * */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileChooserSpanTest {

  lateinit var context: Context
  private var activity: ProfileChooserFragmentTestActivity? = null
  lateinit var fragment: ProfileChooserFragment
  lateinit var recyclerView: RecyclerView

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    activity = getProfileChooserTestActivity()
    fragment = getProfileChooserFragment()
    recyclerView = (fragment!!.view!!.findViewWithTag<View>("profile_recycler_view") as RecyclerView)
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
  @Config(qualifiers = "ldpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenldpi() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(3)
  }

  @Test
  @Config(qualifiers = "mdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenMdpi() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(3)
  }

  @Test
  @Config(qualifiers = "hdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenHdpi() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(4)
  }

  @Test
  @Config(qualifiers = "xhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXhdpi() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(5)
  }

  @Test
  @Config(qualifiers = "xxhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXxhdpi() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(5)
  }

  @Test
  @Config(qualifiers = "land-ldpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForLdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(3)
    }
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForMdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(3)
    }
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForHdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(4)
    }
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(5)
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXxhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    launch(ProfileChooserFragmentTestActivity::class.java).use {
      assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(5)
    }
  }

  fun getProfileChooserFragment(): ProfileChooserFragment {
    return activity!!.supportFragmentManager.findFragmentByTag(ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT) as ProfileChooserFragment
  }
}
