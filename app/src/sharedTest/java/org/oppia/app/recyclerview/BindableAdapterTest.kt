package org.oppia.app.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.BINDABLE_TEST_FRAGMENT_TAG
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.testing.BindableAdapterTestFragmentPresenter
import org.oppia.app.testing.BindableAdapterTestViewModel

const val ITEM_0 = "Item 0"
const val ITEM_1 = "Item 1"

/** Tests for [BindableAdapter]. */
@RunWith(AndroidJUnit4::class)
class BindableAdapterTest {
  @Before
  fun setUp() {
    // Ensure that the bindable fragment's test state is properly reset each time.
    BindableAdapterTestFragmentPresenter.testBindableAdapter = null
  }
  @After
  fun tearDown() {
    // Ensure that the bindable fragment's test state is properly cleaned up.
    BindableAdapterTestFragmentPresenter.testBindableAdapter = null
  }

  @Test
  fun testBindableAdapter_withOneViewType_noData_bindsNoViews() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeNoDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView = getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)

        assertThat(recyclerView.childCount).isEqualTo(0)
      }
    }
  }

  @Test
  fun testBindableAdapter_withOneViewType_setItem_automaticallyBindsView() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeNoDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(ITEM_0)
        onIdle()

        val recyclerView: RecyclerView = getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(1)
//        onView(withId(R.id.text_view_no_data_binding)).check(matches(withText(ITEM_0)))
      }
    }
  }

  // testBindableAdapter_withOneViewType_setMultipleItems_automaticallyBinds
  // testBindableAdapter_withTwoViewTypes_setItems_autoBindsCorrectItemsPerTypes
  // testBindableAdapter_withTwoViewTypes_setItems_autoBindsCorrectItemsPerTypes

  private fun createSingleViewTypeNoDataBindingBindableAdapter(): BindableAdapter<String> {
    return BindableAdapter.Builder
      .newBuilder<String>()
      .registerViewBinder(
        inflateView = this::inflateTextViewWithoutDataBinding,
        bindView = this::bindTextViewWithoutDataBinding
      )
      .build()
  }

  private fun inflateTextViewWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())
    return inflater.inflate(R.layout.test_text_view_no_data_binding, viewGroup, /* attachToRoot= */ false) as TextView
  }

  private fun bindTextViewWithoutDataBinding(textView: TextView, data: String) {
    textView.text = data
  }

  private fun getRecyclerViewListLiveData(activity: BindableAdapterTestActivity): MutableLiveData<List<String>> {
    return getTestViewModel(activity).dataListLiveData
  }

  private fun getTestViewModel(activity: BindableAdapterTestActivity): BindableAdapterTestViewModel {
    return getTestFragmentPresenter(activity).viewModel
  }

  private fun getTestFragmentPresenter(activity: BindableAdapterTestActivity): BindableAdapterTestFragmentPresenter {
    return getTestFragment(activity).bindableAdapterTestFragmentPresenter
  }

  private fun getTestFragment(activity: BindableAdapterTestActivity): BindableAdapterTestFragment {
    return activity.supportFragmentManager.findFragmentByTag(BINDABLE_TEST_FRAGMENT_TAG) as BindableAdapterTestFragment
  }
}
