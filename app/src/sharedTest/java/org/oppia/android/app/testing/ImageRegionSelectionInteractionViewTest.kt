package org.oppia.app.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.DefaultRegionClickedEvent
import org.oppia.app.utility.NamedRegionClickedEvent
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.RegionClickedEvent
import org.oppia.app.utility.capture
import org.oppia.app.utility.clickPoint
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
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
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

  @Inject
  lateinit var context: Context

  @Mock
  lateinit var onClickableAreaClickedListener: OnClickableAreaClickedListener

  @Captor
  lateinit var regionClickedEvent: ArgumentCaptor<RegionClickedEvent>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    MockitoAnnotations.initMocks(this)
    FirebaseApp.initializeApp(context)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @Ignore
  fun testImageRegionSelectionInteractionView_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.3f, 0.3f)
      )

      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 3"))
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @Ignore
  fun testImageRegionSelectionInteractionView_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.7f, 0.3f)
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
      assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 2"))
    }
  }

  @Test
  // TODO(#1611): Fix ImageRegionSelectionInteractionViewTest
  @Ignore
  fun testImageRegionSelectionInteractionView_clickOnDefaultRegion_defaultRegionClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.0f, 0.0f)
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
  @Ignore("Move to Roboelectric")
  fun testView_withTalkbackEnabled_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.7f, 0.3f)
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
      assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 2"))
    }
  }

  @Test
  @Ignore("Move to Roboelectric")
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      verify(onClickableAreaClickedListener)
        .onClickableAreaTouched(
          capture(regionClickedEvent)
        )
      assertThat(regionClickedEvent.value).isEqualTo(NamedRegionClickedEvent("Region 3"))
    }
  }

  @Test
  @Ignore("Move to Roboelectric")
  fun testView_withTalkbackEnabled_clickOnDefaultRegion_defaultRegionNotClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity {
        it.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
          .setListener(onClickableAreaClickedListener)
      }
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.0f, 0.0f)
      )
      onView(withId(R.id.default_selected_region)).check(
        matches(not(isDisplayed()))
      )

      assertThat(regionClickedEvent.value).isInstanceOf(DefaultRegionClickedEvent::class.java)
    }
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
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
