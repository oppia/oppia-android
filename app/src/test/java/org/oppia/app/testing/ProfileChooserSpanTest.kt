package org.oppia.app.testing

import android.content.Context
import android.content.res.Configuration
import android.view.View
import com.google.common.truth.Truth.assertThat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.*
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.objectweb.asm.util.CheckClassAdapter.verify
import org.oppia.app.R
import org.oppia.app.databinding.ProfileChooserAddViewBinding
import org.oppia.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.app.help.HelpActivity
import org.oppia.app.model.ProfileChooserUiModel
import org.oppia.app.profile.*
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.app.utility.OrientationChangeAction
import org.robolectric.Robolectric
import org.robolectric.android.Bootstrap.applyQualifiers
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

/** Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileChooserSpanTest {

  lateinit var context: Context
  lateinit var activity: ProfileChooserFragmentTestActivity
  lateinit var fragment: ProfileChooserFragment
  lateinit var recyclerView: RecyclerView
  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    activity = Robolectric.setupActivity(ProfileChooserFragmentTestActivity::class.java)
    fragment = getProfileChooserFragment()
    recyclerView = (fragment!!.view!!.findViewWithTag<View>("profile_recycler_view") as RecyclerView)
    ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
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
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(3)
    assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(3)
  }

  @Test
  @Config(qualifiers = "land-mdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForMdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(3)
  }

  @Test
  @Config(qualifiers = "land-hdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForHdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(4)
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(5)
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun ProfileChooserFragmentTest_onConfigLandScapeAndForXxhdpi_hasRecyclerViewSpanCountVerifiedSucessfully() {
    assertThat(context.resources.getInteger(R.integer.profile_chooser_span_count)).isEqualTo(5)
  }

  @Test
  @Config(qualifiers = "land-xhdpi")
  fun validateTextViewContent() {
    assertThat((recyclerView.layoutManager as GridLayoutManager).spanCount).isEqualTo(5)
  }

  fun getProfileChooserFragment(): ProfileChooserFragment {
    return activity.supportFragmentManager.findFragmentByTag(ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT) as ProfileChooserFragment
  }
}
