package org.oppia.app.testing

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.testing.FragmentScenario
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
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
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

/** Tests for ensuring [ProfileChooserFragment] uses the correct column count for profiles based on display density. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileChooserSpanTest {

  private var context: Context?=null

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserUiModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ProfileChooserUiModel, ProfileChooserUiModel.ModelTypeCase>(ProfileChooserUiModel::getModelTypeCase)
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.PROFILE,
        inflateDataBinding = ProfileChooserProfileViewBinding::inflate,
        setViewModel = this::bindProfileView
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.ADD_PROFILE,
        inflateDataBinding = ProfileChooserAddViewBinding::inflate,
        setViewModel = this::bindAddView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileChooserProfileViewBinding,
    model: ProfileChooserUiModel
  ) {
    binding.viewModel = model
  }

  private fun bindAddView(binding: ProfileChooserAddViewBinding, @Suppress("UNUSED_PARAMETER") model: ProfileChooserUiModel) {
  }

  @Test
  @Config(qualifiers = "ldpi")
  fun testGridAutoFitLayoutManager_spanCountThreeVerifiedSuccessfully() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(3)
    )
  }

  @Test
  @Config(qualifiers = "mdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenMdpi() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(3)
    )
  }

  @Test
  @Config(qualifiers = "hdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenHdpi() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(4)
    )
  }

  @Test
  @Config(qualifiers = "xhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXhdpi() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(5)
    )
  }

  @Test
  @Config(qualifiers = "xxhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXxhdpi() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(5)
    )
  }

  @Test
  @Config(qualifiers = "xxxhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXxxhdpi() {
    val adapter = createRecyclerViewAdapter()
    val layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.profile_chooser_span_count))
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(5)
    )
  }
@Test
  @Config(qualifiers = "xxxhdpi")
  fun testProfileChooserSpanTest_onConfigLandScapeAndScreenXxxhdpi_() {
 launch(ProfileChooserFragmentTestActivity::class.java).use {
   onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.orientationLandscape())

   Espresso.onView(ViewMatchers.withId(R.id.profile_recycler_view))
      .check(RecyclerViewMatcher.hasGridItemCount(2))

  }
}





  fun getProfileChooserFragment(): ProfileChooserFragment? {
    val activity=ProfileChooserFragmentTestActivity()
    return activity.supportFragmentManager.findFragmentByTag(ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT) as ProfileChooserFragment?
  }

}
