package org.oppia.android.app.recyclerview

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.ActivityIntentFactoriesModule
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.fragment.FragmentComponent
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.FragmentModule
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.player.state.itemviewmodel.InteractionViewModelModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
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
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.view.ViewComponentBuilderModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.databinding.TestTextViewForIntWithDataBindingBinding
import org.oppia.android.databinding.TestTextViewForLiveDataWithDataBindingBinding
import org.oppia.android.databinding.TestTextViewForStringWithDataBindingBinding
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
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
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

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
    TestModule.testAdapterFactory =
      { singleFactory, _ -> createSingleViewTypeNoDataBindingBindableAdapter(singleFactory) }

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
    TestModule.testAdapterFactory = { singleTypeFactory, _ ->
      createSingleViewTypeNoDataBindingBindableAdapter(singleTypeFactory)
    }

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
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
    }
  }

  @Test
  fun testSingleTypeAdapter_withOneViewType_nullData_bindsNoViews() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { singleTypeFactory, _ ->
      createSingleViewTypeNoDataBindingBindableAdapter(singleTypeFactory)
    }

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
    TestModule.testAdapterFactory = { singleTypeFactory, _ ->
      createSingleViewTypeNoDataBindingBindableAdapter(singleTypeFactory)
    }

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
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 1))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 2))
        .check(matches(withText(STR_VALUE_2.boundStringValue)))
    }
  }

  @Test
  fun testMultiTypeAdapter_withTwoViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory =
      { _, multiTypeFactory -> createMultiViewTypeNoDataBindingBindableAdapter(multiTypeFactory) }

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

      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 1
        )
      ).check(matches(withSubstring(INT_VALUE_0.intValue.toString())))
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 2
        )
      ).check(matches(withSubstring(INT_VALUE_1.intValue.toString())))
    }
  }

  @Test
  fun testSingleTypeAdapter_withOneDataBoundViewType_setItem_automaticallyBindsView() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { singleTypeFactory, _ ->
      createSingleViewTypeWithDataBindingBindableAdapter(singleTypeFactory)
    }

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
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
    }
  }

  @Test
  fun testSingleTypeAdapter_withTwoDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory =
      { singleTypeFactory, _ ->
        createSingleViewTypeWithDataBindingBindableAdapter(singleTypeFactory)
      }

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

      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 1))
        .check(matches(withText(STR_VALUE_0.boundStringValue)))
      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 2))
        .check(matches(withText(STR_VALUE_2.boundStringValue)))
    }
  }

  @Test
  fun testMultiTypeAdapter_partiallyDataBoundViewTypes_setItems_autoBindsCorrectItemsPerTypes() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory =
      { _, multiTypeFactory -> createMultiViewTypeWithDataBindingBindableAdapter(multiTypeFactory) }

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

      onView(atPosition(recyclerViewId = R.id.test_recycler_view, position = 0))
        .check(matches(withText(STR_VALUE_1.boundStringValue)))
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 1
        )
      ).check(matches(withSubstring(INT_VALUE_0.intValue.toString())))
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 2
        )
      ).check(matches(withSubstring(INT_VALUE_1.intValue.toString())))
    }
  }

  @Test
  fun testSingleTypeAdapter_withLiveData_withLifecycleOwner_rebindsLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { singleTypeFactory, _ ->
      createSingleViewTypeWithDataBindingAndLiveDataAdapter(singleTypeFactory)
    }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // The updated live data value should be reflected on the UI due to the bound lifecycle owner.
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 0
        )
      ).check(matches(withText("new value")))
    }
  }

  @Test
  fun testMultiTypeAdapter_withLiveData_withLifecycleOwner_rebindsLiveDataValues() {
    // Set up the adapter to be used for this test.
    TestModule.testAdapterFactory = { _, multiTypeFactory ->
      createMultiViewTypeWithDataBindingBindableAdapter(multiTypeFactory)
    }

    launch(BindableAdapterTestActivity::class.java).use { scenario ->
      val itemLiveData = MutableLiveData("initial")
      scenario.onActivity { activity ->
        val liveData = getRecyclerViewListLiveData(activity)
        liveData.value = listOf(LiveDataModel(itemLiveData))
      }
      testCoroutineDispatchers.runCurrent()

      itemLiveData.postValue("new value")
      testCoroutineDispatchers.runCurrent()

      // The updated live data value should be reflected on the UI due to the bound lifecycle owner.
      onView(
        atPosition(
          recyclerViewId = R.id.test_recycler_view,
          position = 0
        )
      ).check(matches(withText("new value")))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createSingleViewTypeNoDataBindingBindableAdapter(
    singleTypeBuilderFactory: SingleTypeBuilder.Factory
  ): BindableAdapter<BindableAdapterTestDataModel> {
    return singleTypeBuilderFactory.create<BindableAdapterTestDataModel>()
      .registerViewBinder(
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .build()
  }

  private fun createSingleViewTypeWithDataBindingBindableAdapter(
    singleTypeBuilder: SingleTypeBuilder.Factory
  ):
    BindableAdapter<BindableAdapterTestDataModel> {
      return singleTypeBuilder.create<BindableAdapterTestDataModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = TestTextViewForStringWithDataBindingBinding::inflate,
          setViewModel = TestTextViewForStringWithDataBindingBinding::setViewModel
        )
        .build()
    }

  private fun createSingleViewTypeWithDataBindingAndLiveDataAdapter(
    singleTypeBuilder: SingleTypeBuilder.Factory
  ): BindableAdapter<BindableAdapterTestDataModel> {
    return singleTypeBuilder.create<BindableAdapterTestDataModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TestTextViewForLiveDataWithDataBindingBinding::inflate,
        setViewModel = TestTextViewForLiveDataWithDataBindingBinding::setViewModel
      )
      .build()
  }

  private fun createSingleAdapterWithoutView(singleTypeBuilder: SingleTypeBuilder.Factory):
    BindableAdapter<BindableAdapterTestDataModel> {
      return singleTypeBuilder.create<BindableAdapterTestDataModel>().build()
    }

  private fun createMultiViewTypeNoDataBindingBindableAdapter(
    multiTypeBuilderFactory: MultiTypeBuilder.Factory
  ):
    BindableAdapter<BindableAdapterTestDataModel> {
      return multiTypeBuilderFactory.create(ViewModelType.Companion::deriveTypeFrom)
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

  private fun createMultiViewTypeWithDataBindingBindableAdapter(
    multiTypeBuilderFactory: MultiTypeBuilder.Factory
  ): BindableAdapter<BindableAdapterTestDataModel> {
    return multiTypeBuilderFactory.create(ViewModelType.Companion::deriveTypeFrom)
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
      var testAdapterFactory: (
        (SingleTypeBuilder.Factory, MultiTypeBuilder.Factory) ->
        BindableAdapter<BindableAdapterTestDataModel>
      )? = null
    }

    @Provides
    fun provideTestAdapter(): BindableAdapterTestFragmentPresenter.BindableAdapterFactory {
      val createFunction = checkNotNull(testAdapterFactory) {
        "The test adapter factory hasn't been initialized in the test"
      }
      return object : BindableAdapterTestFragmentPresenter.BindableAdapterFactory {
        override fun create(
          singleTypeBuilderFactory: SingleTypeBuilder.Factory,
          multiTypeBuilderFactory: MultiTypeBuilder.Factory
        ): BindableAdapter<BindableAdapterTestDataModel> {
          return createFunction(singleTypeBuilderFactory, multiTypeBuilderFactory)
        }
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @FragmentScope
  @Subcomponent(
    modules = [
      FragmentModule::class, InteractionViewModelModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, ViewComponentBuilderModule::class
    ]
  )
  interface TestFragmentComponent :
    FragmentComponentImpl, BindableAdapterTestFragment.TestInjector {
    @Subcomponent.Builder
    interface Builder : FragmentComponentImpl.Builder
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
  @Subcomponent(modules = [TestActivityModule::class, ActivityIntentFactoriesModule::class])
  interface TestActivityComponent :
    ActivityComponentImpl, BindableAdapterTestActivity.TestInjector {

    @Subcomponent.Builder
    interface Builder : ActivityComponentImpl.Builder
  }

  @Module(subcomponents = [TestActivityComponent::class])
  interface TestApplicationModule {
    @Binds
    fun provideContext(application: Application): Context

    // Bridge the test & original ActivityComponent builders to properly hook up the :replacement
    // test subcomponent.
    @Binds
    fun provideActivityComponentBuilder(
      builder: TestActivityComponent.Builder
    ): ActivityComponentImpl.Builder
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, TestApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
