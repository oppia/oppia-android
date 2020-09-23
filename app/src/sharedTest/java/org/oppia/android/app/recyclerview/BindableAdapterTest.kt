package org.oppia.app.recyclerview

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.databinding.TestTextViewForIntWithDataBindingBinding
import org.oppia.app.databinding.TestTextViewForStringWithDataBindingBinding
import org.oppia.app.model.TestModel
import org.oppia.app.model.TestModel.ModelTypeCase
import org.oppia.app.parser.HtmlParserTest
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.testing.BINDABLE_TEST_FRAGMENT_TAG
import org.oppia.app.testing.BindableAdapterTestActivity
import org.oppia.app.testing.BindableAdapterTestFragment
import org.oppia.app.testing.BindableAdapterTestFragmentPresenter
import org.oppia.app.testing.BindableAdapterTestViewModel
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [BindableAdapter]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = BindableAdapterTest.TestApplication::class, qualifiers = "port-xxhdpi")
class BindableAdapterTest {
  companion object {
    private val STR_VALUE_0 = TestModel.newBuilder().setStrValue("Item 0").build()
    private val STR_VALUE_1 = TestModel.newBuilder().setStrValue("Item 1").build()
    private val STR_VALUE_2 = TestModel.newBuilder().setStrValue("Item 2").build()
    private val INT_VALUE_0 = TestModel.newBuilder().setIntValue(17).build()
    private val INT_VALUE_1 = TestModel.newBuilder().setIntValue(42).build()
  }

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()

    // Ensure that the bindable fragment's test state is properly reset each time.
    BindableAdapterTestFragmentPresenter.testBindableAdapter = null
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()

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
      testCoroutineDispatchers.runCurrent()

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
      testCoroutineDispatchers.runCurrent()

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
      testCoroutineDispatchers.runCurrent()

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
      testCoroutineDispatchers.runCurrent()

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
      testCoroutineDispatchers.runCurrent()

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
      testCoroutineDispatchers.runCurrent()

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

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

  private fun getRecyclerViewListLiveData(activity: BindableAdapterTestActivity): MutableLiveData<List<TestModel>> { // ktlint-disable max-line-length
    return getTestViewModel(activity).dataListLiveData
  }

  private fun getTestViewModel(activity: BindableAdapterTestActivity): BindableAdapterTestViewModel { // ktlint-disable max-line-length
    return getTestFragmentPresenter(activity).viewModel
  }

  private fun getTestFragmentPresenter(activity: BindableAdapterTestActivity): BindableAdapterTestFragmentPresenter { // ktlint-disable max-line-length
    return getTestFragment(activity).bindableAdapterTestFragmentPresenter
  }

  private fun getTestFragment(activity: BindableAdapterTestActivity): BindableAdapterTestFragment {
    return activity.supportFragmentManager.findFragmentByTag(BINDABLE_TEST_FRAGMENT_TAG) as BindableAdapterTestFragment // ktlint-disable max-line-length
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, HtmlParserTest.ImageTestModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
