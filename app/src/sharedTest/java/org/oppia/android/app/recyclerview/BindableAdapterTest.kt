package org.oppia.android.app.recyclerview

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationContext
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.fragment.FragmentModule
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.android.app.recyclerview.BindableAdapter.MultiTypeBuilder
import org.oppia.android.app.recyclerview.BindableAdapter.SingleTypeBuilder
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.BINDABLE_TEST_FRAGMENT_TAG
import org.oppia.android.app.testing.BindableAdapterTestActivity
import org.oppia.android.app.testing.BindableAdapterTestDataModel
import org.oppia.android.app.testing.BindableAdapterTestDataModel.IntModel
import org.oppia.android.app.testing.BindableAdapterTestDataModel.LiveDataModel
import org.oppia.android.app.testing.BindableAdapterTestDataModel.StringModel
import org.oppia.android.app.testing.BindableAdapterTestFragment
import org.oppia.android.app.testing.BindableAdapterTestFragmentPresenter
import org.oppia.android.app.testing.BindableAdapterTestViewModel
import org.oppia.android.databinding.TestTextViewForIntWithDataBindingBinding
import org.oppia.android.databinding.TestTextViewForLiveDataWithDataBindingBinding
import org.oppia.android.databinding.TestTextViewForStringWithDataBindingBinding
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [BindableAdapter]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = BindableAdapterTest.TestApplication::class, qualifiers = "port-xxhdpi")
class BindableAdapterTest {
  companion object {
    private val STR_VALUE_0 = StringModel("Item 0")
    private val STR_VALUE_1 = StringModel("Item 1")
    private val STR_VALUE_2 = StringModel("Item 2")
    private val INT_VALUE_0 = IntModel(17)
    private val INT_VALUE_1 = IntModel(42)
  }

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()

    // Ensure that the test module's test state is properly reset each time.
    TestModule.testAdapterFactory = null
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()

    // Ensure that the test module's test state is properly reset each time.
    TestModule.testAdapterFactory = null
  }

  @Test
  fun testSingleTypeAdapter_withOneViewType_noData_bindsNoViews() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeNoDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)

        assertThat(recyclerView.childCount).isEqualTo(0)
      }
    }
  }

  @Test
  fun testSingleTypeAdapter_withOneViewType_setItem_automaticallyBindsView() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeNoDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_0)
      }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(1)
      }
      // Perform onView() verification off the the main thread to avoid deadlocking.
      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
    }
  }

  @Test
  fun testSingleTypeAdapter_withOneViewType_nullData_bindsNoViews() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeNoDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view_non_live_data)
        assertThat(recyclerView.childCount).isEqualTo(0)
      }
    }
  }

  @Test
  fun testSingleTypeAdapter_withOneViewType_setMultipleItems_automaticallyBinds() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeNoDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, STR_VALUE_0, STR_VALUE_2)
      }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }
      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(atPosition(R.id.test_recycler_view, 1))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
      onView(atPosition(R.id.test_recycler_view, 2))
        .check(matches(withText(STR_VALUE_2.boundStringValue)))
    }
  }

  @Test
  fun testMultiTypeAdapter_withTwoViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createMultiViewTypeNoDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, INT_VALUE_0, INT_VALUE_1)
      }
      testCoroutineDispatchers.runCurrent()

      // Verify that all three values are bound in the correct order and with the correct values.
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
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
  fun testSingleTypeAdapter_withOneDataBoundViewType_setItem_automaticallyBindsView() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeWithDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_0)
      }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(1)
      }
      // Perform onView() verification off the the main thread to avoid deadlocking.
      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
    }
  }

  @Test
  fun testSingleTypeAdapter_withTwoDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeWithDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, STR_VALUE_0, STR_VALUE_2)
      }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(atPosition(R.id.test_recycler_view, 1))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
      onView(atPosition(R.id.test_recycler_view, 2))
        .check(matches(withText(STR_VALUE_2.boundStringValue)))
    }
  }

  @Test
  fun testMultiTypeAdapter_partiallyDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createMultiViewTypeWithDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(STR_VALUE_1, INT_VALUE_0, INT_VALUE_1)
      }
      testCoroutineDispatchers.runCurrent()

      // Verify that all three values are bound in the correct order and with the correct values.
      scenario.onActivity { activity ->
        val recyclerView: RecyclerView =
          getTestFragment(activity).view!!.findViewById(R.id.test_recycler_view)
        assertThat(recyclerView.childCount).isEqualTo(3)
      }

      onView(atPosition(R.id.test_recycler_view, 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
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
  fun testSingleTypeAdapter_setLifecycleOwnerTwice_throwsException() {
    val testFragment = Fragment()

    val exception = assertThrows(IllegalStateException::class) {
      SingleTypeBuilder
        .newBuilder<BindableAdapterTestDataModel>()
        .setLifecycleOwner(testFragment)
        .setLifecycleOwner(testFragment)
        .build()
    }
    assertThat(exception).hasMessageThat().contains("lifecycle owner has already been bound")
  }

  @Test
  fun testMultiTypeAdapter_setLifecycleOwnerTwice_throwsException() {
    val testFragment = Fragment()

    val exception = assertThrows(IllegalStateException::class) {
      MultiTypeBuilder
        .newBuilder(ViewModelType.Companion::deriveTypeFrom)
        .setLifecycleOwner(testFragment)
        .setLifecycleOwner(testFragment)
        .build()
    }
    assertThat(exception).hasMessageThat().contains("lifecycle owner has already been bound")
  }

  @Test
  fun testSingleTypeAdapter_withLiveData_noLifecycleOwner_doesNotRebindLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createSingleViewTypeWithDataBindingAndLiveDataAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData<String>("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // Verify that the bound data did not change despite the underlying live data changing.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText("initial")))
    }
  }

  @Test
  fun testSingleTypeAdapter_withLiveData_withLifecycleOwner_rebindsLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { fragment ->
      createSingleViewTypeWithDataBindingAndLiveDataAdapter(lifecycleOwner = fragment)
    }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData<String>("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // The updated live data value should be reflected on the UI due to the bound lifecycle owner.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText("new value")))
    }
  }

  @Test
  fun testMultiTypeAdapter_withLiveData_noLifecycleOwner_doesNotRebindLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { createMultiViewTypeWithDataBindingBindableAdapter() }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData<String>("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // Verify that the bound data did not change despite the underlying live data changing.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText("initial")))
    }
  }

  @Test
  fun testMultiTypeAdapter_withLiveData_withLifecycleOwner_rebindsLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { fragment ->
      createMultiViewTypeWithDataBindingBindableAdapter(lifecycleOwner = fragment)
    }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData<String>("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // The updated live data value should be reflected on the UI due to the bound lifecycle owner.
      onView(atPosition(R.id.test_recycler_view, 0)).check(matches(withText("new value")))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createSingleViewTypeNoDataBindingBindableAdapter():
    BindableAdapter<BindableAdapterTestDataModel> {
      return SingleTypeBuilder
        .newBuilder<BindableAdapterTestDataModel>()
        .registerViewBinder(
          inflateView = this::inflateTextViewForStringWithoutDataBinding,
          bindView = this::bindTextViewForStringWithoutDataBinding
        )
        .build()
    }

  private fun createSingleViewTypeWithDataBindingBindableAdapter():
    BindableAdapter<BindableAdapterTestDataModel> {
      return SingleTypeBuilder
        .newBuilder<BindableAdapterTestDataModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
        )
        .build()
    }

  private fun createSingleViewTypeWithDataBindingAndLiveDataAdapter():
    BindableAdapter<BindableAdapterTestDataModel> {
      return SingleTypeBuilder
        .newBuilder<BindableAdapterTestDataModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = TestTextViewForLiveDataWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForLiveDataWithDataBindingBinding::setViewModel
        )
        .build()
    }

  private fun createSingleViewTypeWithDataBindingAndLiveDataAdapter(
    lifecycleOwner: Fragment
  ): BindableAdapter<BindableAdapterTestDataModel> {
    return SingleTypeBuilder
      .newBuilder<BindableAdapterTestDataModel>()
      .setLifecycleOwner(lifecycleOwner)
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TestTextViewForLiveDataWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForLiveDataWithDataBindingBinding::setViewModel
      )
      .build()
  }

  private fun createMultiViewTypeNoDataBindingBindableAdapter():
    BindableAdapter<BindableAdapterTestDataModel> {
      return MultiTypeBuilder
        .newBuilder(ViewModelType.Companion::deriveTypeFrom)
        .registerViewBinder(
          viewType = ViewModelType.STRING,
          inflateView = this::inflateTextViewForStringWithoutDataBinding,
          bindView = this::bindTextViewForStringWithoutDataBinding
        )
        .registerViewBinder(
          viewType = ViewModelType.INT,
          inflateView = this::inflateTextViewForIntWithoutDataBinding,
          bindView = this::bindTextViewForIntWithoutDataBinding
        )
        .build()
    }

  private fun createMultiViewTypeWithDataBindingBindableAdapter():
    BindableAdapter<BindableAdapterTestDataModel> {
      return MultiTypeBuilder
        .newBuilder(ViewModelType.Companion::deriveTypeFrom)
        .registerViewDataBinderWithSameModelType(
          viewType = ViewModelType.STRING,
          inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
        )
        .registerViewDataBinderWithSameModelType(
          viewType = ViewModelType.INT,
          inflateDataBinding = TestTextViewForIntWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForIntWithDataBindingBinding::setViewModel
        )
        .registerViewDataBinderWithSameModelType(
          viewType = ViewModelType.LIVE_DATA,
          inflateDataBinding = TestTextViewForLiveDataWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForLiveDataWithDataBindingBinding::setViewModel
        )
        .build()
    }

  private fun createMultiViewTypeWithDataBindingBindableAdapter(
    lifecycleOwner: Fragment
  ): BindableAdapter<BindableAdapterTestDataModel> {
    return MultiTypeBuilder
      .newBuilder(ViewModelType.Companion::deriveTypeFrom)
      .setLifecycleOwner(lifecycleOwner)
      .registerViewDataBinderWithSameModelType(
        viewType = ViewModelType.STRING,
        inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ViewModelType.INT,
        inflateDataBinding = TestTextViewForIntWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForIntWithDataBindingBinding::setViewModel
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ViewModelType.LIVE_DATA,
        inflateDataBinding = TestTextViewForLiveDataWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForLiveDataWithDataBindingBinding::setViewModel
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

  private fun bindTextViewForStringWithoutDataBinding(
    textView: TextView,
    data: BindableAdapterTestDataModel
  ) {
    textView.text = data.boundStringValue
  }

  private fun bindTextViewForIntWithoutDataBinding(
    textView: TextView,
    data: BindableAdapterTestDataModel
  ) {
    textView.text = "Value: " + data.boundIntValue
  }

  private fun getRecyclerViewListLiveData(
    activity: BindableAdapterTestActivity
  ): MutableLiveData<List<BindableAdapterTestDataModel>> {
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

  private fun getTestFragment(activity: BindableAdapterTestActivity): BindableAdapterTestFragment {
    return activity.supportFragmentManager.findFragmentByTag(
      BINDABLE_TEST_FRAGMENT_TAG
    ) as BindableAdapterTestFragment
  }

  private enum class ViewModelType {
    STRING,
    INT,
    LIVE_DATA;

    companion object {
      internal fun deriveTypeFrom(testDataModel: BindableAdapterTestDataModel): ViewModelType {
        return when (testDataModel) {
          is StringModel -> STRING
          is IntModel -> INT
          is LiveDataModel -> LIVE_DATA
        }
      }
    }
  }

  @Module
  class TestModule {
    companion object {
      // TODO(#1720): Move this to a test-level binding to avoid the need for static state.
      var testAdapterFactory: ((Fragment) -> BindableAdapter<BindableAdapterTestDataModel>)? = null
    }

    @Provides
    fun provideTestAdapter(): BindableAdapterTestFragmentPresenter.BindableAdapterFactory {
      val createFunction = checkNotNull(testAdapterFactory) {
        "The test adapter factory hasn't been initialized in the test"
      }
      return object : BindableAdapterTestFragmentPresenter.BindableAdapterFactory {
        override fun create(fragment: Fragment): BindableAdapter<BindableAdapterTestDataModel> {
          return createFunction(fragment)
        }
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @FragmentScope
  @Subcomponent(
    modules = [
      FragmentModule::class, InteractionViewModelModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class
    ]
  )
  interface TestFragmentComponent : FragmentComponent, BindableAdapterTestFragment.TestInjector {
    @Subcomponent.Builder
    interface Builder : FragmentComponent.Builder
  }

  @Module(subcomponents = [TestFragmentComponent::class])
  interface TestActivityModule {
    // Bridge the test & original FragmentComponent builders to properly hook up the replacement
    // test subcomponent.
    @Binds
    fun provideFragmentComponentBuilder(
      builder: TestFragmentComponent.Builder
    ): FragmentComponent.Builder
  }

  @ActivityScope
  @Subcomponent(modules = [TestActivityModule::class])
  interface TestActivityComponent : ActivityComponent, BindableAdapterTestActivity.TestInjector {
    @Subcomponent.Builder
    interface Builder : ActivityComponent.Builder
  }

  @Module(subcomponents = [TestActivityComponent::class])
  interface TestApplicationModule {
    @Binds
    @ApplicationContext
    fun provideApplicationContext(application: Application): Context

    @Binds
    fun provideContext(@ApplicationContext context: Context): Context

    // Bridge the test & original ActivityComponent builders to properly hook up the replacement
    // test subcomponent.
    @Binds
    fun provideActivityComponentBuilder(
      builder: TestActivityComponent.Builder
    ): ActivityComponent.Builder
  }

  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestModule::class,
      TestDispatcherModule::class, TestApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun getTestActivityComponentBuilderProvider(): Provider<TestActivityComponent.Builder>

    fun inject(bindableAdapterTest: BindableAdapterTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerBindableAdapterTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(bindableAdapterTest: BindableAdapterTest) {
      component.inject(bindableAdapterTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getTestActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
