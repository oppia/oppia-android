package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.DefaultRegionClickedEvent
import org.oppia.android.app.utility.NamedRegionClickedEvent
import org.oppia.android.app.utility.OnClickableAreaClickedListener
import org.oppia.android.app.utility.RegionClickedEvent
import org.oppia.android.app.utility.clickPoint
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
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
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.mockito.capture
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ImageRegionSelectionInteractionViewTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ImageRegionSelectionInteractionViewTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var onClickableAreaClickedListener: OnClickableAreaClickedListener

  @Captor
  lateinit var regionClickedEvent: ArgumentCaptor<RegionClickedEvent>

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var imageLoader: TestGlideImageLoader

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
    imageLoader.arrangeBitmap("test_image_url.drawable", R.drawable.testing_fraction)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )

      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 3", contentDescription = "You have selected Region 3"
          )
        )
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.7f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 2"))))
        .check(
          matches(isDisplayed())
        )

      verify(
        onClickableAreaClickedListener,
        times(2)
      ).onClickableAreaTouched(
        capture(
          regionClickedEvent
        )
      )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 2", contentDescription = "You have selected Region 2"
          )
        )
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_clickOnDefaultRegion_defaultRegionClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.0f, pointY = 0.0f)
      )
      onView(withId(R.id.default_selected_region)).check(
        matches(isDisplayed())
      )
      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value).isInstanceOf(DefaultRegionClickedEvent::class.java)
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testView_withTalkbackEnabled_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.7f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 2"))))
        .check(
          matches(isDisplayed())
        )

      verify(
        onClickableAreaClickedListener,
        times(2)
      ).onClickableAreaTouched(
        capture(
          regionClickedEvent
        )
      )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 2", contentDescription = "You have selected Region 2"
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 3", contentDescription = "You have selected Region 3"
          )
        )
    }
  }

  @Test
  @Ignore("Move to Robolectric")
  fun testView_withTalkbackEnabled_clickOnDefaultRegion_defaultRegionNotClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.0f, pointY = 0.0f)
      )
      onView(withId(R.id.default_selected_region)).check(
        matches(not(isDisplayed()))
      )

      assertThat(regionClickedEvent.value).isInstanceOf(DefaultRegionClickedEvent::class.java)
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_rtl_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )

      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 3", contentDescription = "You have selected Region 3"
          )
        )
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_rtl_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
        activity.setMockOnClickableAreaClickedListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.3f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(pointX = 0.7f, pointY = 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 2"))))
        .check(
          matches(isDisplayed())
        )

      verify(
        onClickableAreaClickedListener,
        times(2)
      ).onClickableAreaTouched(
        capture(
          regionClickedEvent
        )
      )
      assertThat(regionClickedEvent.value)
        .isEqualTo(
          NamedRegionClickedEvent(
            regionLabel = "Region 2", contentDescription = "You have selected Region 2"
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_withBlankInput_submit_emptyInputErrorIsDisplayed() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.submit_button)).check(matches(isDisplayed()))
        .perform(
          click()
        )
      onView(withId(R.id.image_input_error))
        .check(
          matches(
            withText(
              R.string.image_error_empty_input
            )
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO)
  fun testImageRegionSelectionInteractionView_submitBbutton_isEnabledByDefault() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.submit_button)).check(matches(isEnabled()))
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(imageRegionSelectionInteractionViewTest: ImageRegionSelectionInteractionViewTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerImageRegionSelectionInteractionViewTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(imageRegionSelectionInteractionViewTest: ImageRegionSelectionInteractionViewTest) {
      component.inject(imageRegionSelectionInteractionViewTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
