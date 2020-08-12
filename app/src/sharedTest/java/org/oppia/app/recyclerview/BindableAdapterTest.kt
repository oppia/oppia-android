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
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.oppia.app.R
import org.oppia.app.databinding.TestTextViewForIntWithDataBindingBinding
import org.oppia.app.databinding.TestTextViewForStringWithDataBindingBinding
import org.oppia.app.model.TestModel
import org.oppia.app.model.TestModel.ModelTypeCase
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.testing.BINDABLE_TEST_FRAGMENT_TAG
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.testing.BindableAdapterTestFragmentPresenter
import org.oppia.app.testing.BindableAdapterTestViewModel
import org.robolectric.annotation.LooperMode

/** Tests for [BindableAdapter]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class BindableAdapterTest {
  companion object {
    private val STR_VALUE_0 = TestModel.newBuilder().setStrValue("Item 0").build()
    private val STR_VALUE_1 = TestModel.newBuilder().setStrValue("Item 1").build()
    private val STR_VALUE_2 = TestModel.newBuilder().setStrValue("Item 2").build()
    private val INT_VALUE_0 = TestModel.newBuilder().setIntValue(17).build()
    private val INT_VALUE_1 = TestModel.newBuilder().setIntValue(42).build()
  }

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
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)

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
        liveData.value = listOf(STR_VALUE_0)
      }
      safelyWaitUntilIdle()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(1)
      }
      // Perform onView() verification off the the main thread to avoid deadlocking.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_0.strValue)))
    }
  }

  @Test
  fun testBindableAdapter_withOneViewType_nullData_bindsNoViews() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeNoDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view_non_live_data)
        assertThat(recyclerView.childCount).isEqualTo(0)
      }
    }
  }

  @Test
  fun testBindableAdapter_withOneViewType_setMultipleItems_automaticallyBinds() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeNoDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, STR_VALUE_0, STR_VALUE_2)
      }
      safelyWaitUntilIdle()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_1.strValue)))
      onView(atPosition(R.id.test_recycler_view, 1)).check(matches(withText(STR_VALUE_0.strValue)))
      onView(atPosition(R.id.test_recycler_view, 2)).check(matches(withText(STR_VALUE_2.strValue)))
    }
  }

  @Test
  fun testBindableAdapter_withTwoViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createMultiViewTypeNoDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, INT_VALUE_0, INT_VALUE_1)
      }
      safelyWaitUntilIdle()

      // Verify that all three values are bound in the correct order and with the correct values.
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_1.strValue)))
      onView(
        atPosition(
          R.id.test_recycler_view,
          1
        )
      ).check(matches(withSubstring(INT_VALUE_0.intValue.toString())))
      onView(
        atPosition(
          R.id.test_recycler_view,
          2
        )
      ).check(matches(withSubstring(INT_VALUE_1.intValue.toString())))
    }
  }

  @Test
  fun testBindableAdapter_withOneDataBoundViewType_setItem_automaticallyBindsView() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeWithDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_0)
      }
      safelyWaitUntilIdle()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(1)
      }
      // Perform onView() verification off the the main thread to avoid deadlocking.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_0.strValue)))
    }
  }

  @Test
  fun testBindableAdapter_withTwoDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createSingleViewTypeWithDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, STR_VALUE_0, STR_VALUE_2)
      }
      safelyWaitUntilIdle()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_1.strValue)))
      onView(atPosition(R.id.test_recycler_view, 1)).check(matches(withText(STR_VALUE_0.strValue)))
      onView(atPosition(R.id.test_recycler_view, 2)).check(matches(withText(STR_VALUE_2.strValue)))
    }
  }

  @Test
  fun testBindableAdapter_withPartiallyDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    BindableAdapterTestFragmentPresenter.testBindableAdapter =
      createMultiViewTypeWithDataBindingBindableAdapter()

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, INT_VALUE_0, INT_VALUE_1)
      }
      safelyWaitUntilIdle()

      // Verify that all three values are bound in the correct order and with the correct values.
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText(STR_VALUE_1.strValue)))
      onView(
        atPosition(
          R.id.test_recycler_view,
          1
        )
      ).check(matches(withSubstring(INT_VALUE_0.intValue.toString())))
      onView(
        atPosition(
          R.id.test_recycler_view,
          2
        )
      ).check(matches(withSubstring(INT_VALUE_1.intValue.toString())))
    }
  }

  @Test
  fun testBindableAdapter_incomingSameData_noRebindingShouldHappen() {
    val adapter = createMultiViewTypeNoDataBindingBindableAdapter()
    BindableAdapterTestFragmentPresenter.testBindableAdapter = adapter

    val oldList = listOf(STR_VALUE_1, STR_VALUE_0, INT_VALUE_1).toMutableList()
    val newList = listOf(STR_VALUE_1, STR_VALUE_0, INT_VALUE_1).toMutableList()

    val fakeObserver = mock(RecyclerView.AdapterDataObserver::class.java)

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        adapter.registerAdapterDataObserver(fakeObserver)
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = oldList
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeInserted(/* positionStart= */ 0, /* itemCount= */ 3)
        val liveDataNew = getRecyclerViewListLiveData(activity)
        liveDataNew.value = newList
        verify(fakeObserver, never()).onChanged()
        adapter.unregisterAdapterDataObserver(fakeObserver)
      }
    }
  }

  @Test
  fun testBindableAdapter_removeOneItem_verifyChangeOnlyOneItem() {
    val adapter = createMultiViewTypeNoDataBindingBindableAdapter()
    BindableAdapterTestFragmentPresenter.testBindableAdapter = adapter

    val oldList = listOf(STR_VALUE_1, STR_VALUE_1, INT_VALUE_1).toMutableList()
    val newList = listOf(STR_VALUE_1, INT_VALUE_1).toMutableList()

    val fakeObserver = mock(RecyclerView.AdapterDataObserver::class.java)

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        adapter.registerAdapterDataObserver(fakeObserver)
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = oldList
        val liveDataNew = getRecyclerViewListLiveData(activity)
        liveDataNew.value = newList
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeRemoved(/* positionStart= */0, /* itemCount= */ 1)
        adapter.unregisterAdapterDataObserver(fakeObserver)
      }
    }
  }

  @Test
  fun testBindableAdapter_insertOneItem_verifyChangeOnlyOneItem() {
    val adapter = createMultiViewTypeNoDataBindingBindableAdapter()
    BindableAdapterTestFragmentPresenter.testBindableAdapter = adapter

    val oldList = listOf(STR_VALUE_1, INT_VALUE_1).toMutableList()
    val newList = listOf(STR_VALUE_1, STR_VALUE_1, INT_VALUE_1).toMutableList()

    val fakeObserver = mock(RecyclerView.AdapterDataObserver::class.java)

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        adapter.registerAdapterDataObserver(fakeObserver)
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = oldList
        val liveDataNew = getRecyclerViewListLiveData(activity)
        liveDataNew.value = newList
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeInserted(/* positionStart= */0, /* itemCount= */ 1)
        adapter.unregisterAdapterDataObserver(fakeObserver)
      }
    }
  }

  @Test
  fun testBindableAdapter_moveOneItem_verifyNoRecreatingWholeList() {
    val adapter = createMultiViewTypeNoDataBindingBindableAdapter()
    BindableAdapterTestFragmentPresenter.testBindableAdapter = adapter

    val oldList = listOf(STR_VALUE_1, STR_VALUE_0, INT_VALUE_1).toMutableList()
    val newList = listOf(INT_VALUE_1, STR_VALUE_0, STR_VALUE_1).toMutableList()

    val fakeObserver = mock(RecyclerView.AdapterDataObserver::class.java)

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        adapter.registerAdapterDataObserver(fakeObserver)
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = oldList
        val liveDataNew = getRecyclerViewListLiveData(activity)
        liveDataNew.value = newList
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeChanged(/* positionStart= */2, /* itemCount= */ 1, /* payload= */ null)
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeChanged(/* positionStart= */0, /* itemCount= */ 1, /* payload= */ null)
        adapter.unregisterAdapterDataObserver(fakeObserver)
      }
    }
  }

  @Test
  fun testBindableAdapter_updateOneItemContent_verifyOneItemUpdated() {
    val adapter = createMultiViewTypeNoDataBindingBindableAdapter()
    BindableAdapterTestFragmentPresenter.testBindableAdapter = adapter

    val oldList = listOf(STR_VALUE_1, STR_VALUE_0, INT_VALUE_1).toMutableList()
    val newList = listOf(STR_VALUE_1, STR_VALUE_1, INT_VALUE_1).toMutableList()

    val fakeObserver = mock(RecyclerView.AdapterDataObserver::class.java)

    ActivityScenario.launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        adapter.registerAdapterDataObserver(fakeObserver)
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = oldList
        val liveDataNew = getRecyclerViewListLiveData(activity)
        liveDataNew.value = newList
        verify(fakeObserver, times(/* wantedNumberOfInvocations= */ 1))
          .onItemRangeChanged(/* positionStart= */1, /* itemCount= */ 1, /* payload= */ null)
        adapter.unregisterAdapterDataObserver(fakeObserver)
      }
    }
  }

  private fun createSingleViewTypeNoDataBindingBindableAdapter(): BindableAdapter<TestModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TestModel>()
      .registerViewBinder(
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .build()
  }

  private fun createSingleViewTypeWithDataBindingBindableAdapter(): BindableAdapter<TestModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TestModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
      )
      .build()
  }

  private fun createMultiViewTypeNoDataBindingBindableAdapter(): BindableAdapter<TestModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<TestModel, ModelTypeCase>(TestModel::getModelTypeCase)
      .registerViewBinder(
        viewType = ModelTypeCase.STR_VALUE,
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .registerViewBinder(
        viewType = ModelTypeCase.INT_VALUE,
        inflateView = this::inflateTextViewForIntWithoutDataBinding,
        bindView = this::bindTextViewForIntWithoutDataBinding
      )
      .build()
  }

  private fun createMultiViewTypeWithDataBindingBindableAdapter(): BindableAdapter<TestModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<TestModel, ModelTypeCase>(TestModel::getModelTypeCase)
      .registerViewDataBinderWithSameModelType(
        viewType = ModelTypeCase.STR_VALUE,
        inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ModelTypeCase.INT_VALUE,
        inflateDataBinding = TestTextViewForIntWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForIntWithDataBindingBinding::setViewModel
      )
      .build()
  }

  private fun inflateTextViewForStringWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())
    return inflater.inflate(
      R.layout.test_text_view_for_string_no_data_binding, viewGroup, /* attachToRoot= */ false
    ) as TextView
  }

  private fun inflateTextViewForIntWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())
    return inflater.inflate(
      R.layout.test_text_view_for_int_no_data_binding, viewGroup, /* attachToRoot= */ false
    ) as TextView
  }

  private fun bindTextViewForStringWithoutDataBinding(textView: TextView, data: TestModel) {
    textView.text = data.strValue
  }

  private fun bindTextViewForIntWithoutDataBinding(textView: TextView, data: TestModel) {
    textView.text = "Value: " + data.intValue
  }

  private fun getRecyclerViewListLiveData(
    activity: BindableAdapterTestActivity
  ): MutableLiveData<List<TestModel>> {
    return getTestViewModel(activity).dataListLiveData
  }

  private fun getTestViewModel(
    activity: BindableAdapterTestActivity
  ): BindableAdapterTestViewModel {
    return getTestFragmentPresenter(activity).viewModel
  }

  private fun getTestFragmentPresenter(
    activity: BindableAdapterTestActivity
  ): BindableAdapterTestFragmentPresenter {
    return getTestFragment(activity).bindableAdapterTestFragmentPresenter
  }

  private fun getTestFragment(
    activity: BindableAdapterTestActivity
  ): BindableAdapterTestFragment {
    return activity.supportFragmentManager.findFragmentByTag(
      BINDABLE_TEST_FRAGMENT_TAG
    ) as BindableAdapterTestFragment
  }

  private fun safelyWaitUntilIdle() {
    // This must be done off the main thread for Espresso otherwise it deadlocks.
    onIdle()
  }
}
