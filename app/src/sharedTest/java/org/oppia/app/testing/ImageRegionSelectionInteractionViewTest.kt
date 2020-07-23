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
import com.google.firebase.FirebaseApp
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.image_region_selection_test_fragment.clickable_image_view
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Before
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
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.state.StateFragment
import org.oppia.app.utility.DefaultRegionClickedEvent
import org.oppia.app.utility.NamedRegionClickedEvent
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.RegionClickedEvent
import org.oppia.app.utility.capture
import org.oppia.app.utility.clickPoint
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.question.QuestionModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.accessibility.FakeAccessibilityManager
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@Config(application = ImageRegionSelectionInteractionViewTest.TestApplication::class)
class ImageRegionSelectionInteractionViewTest {

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var fakeAccessibilityManager: FakeAccessibilityManager

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
  fun testImageRegionSelectionInteractionView_clickRegion3_Region3Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)

        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )

        verify(onClickableAreaClickedListener)
          .onClickableAreaTouched(
            capture(regionClickedEvent)
          )
        assertEquals(NamedRegionClickedEvent("Region 3"), regionClickedEvent.value)
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickRegion3_clickRegion2_region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
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
        assertEquals(NamedRegionClickedEvent("Region 2"), regionClickedEvent.allValues[1])
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickOnDefaultRegion_defaultRegionClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
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
        assertEquals(DefaultRegionClickedEvent(), regionClickedEvent.value)
      }
    }
  }

  @Test
  fun testView_withTalkbackEnabled_clickRegion3_clickRegion2_Region2Clicked() {
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
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
        assertEquals(NamedRegionClickedEvent("Region 2"), regionClickedEvent.allValues[1])
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_Region3Clicked() {
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
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
        assertEquals(NamedRegionClickedEvent("Region 3"), regionClickedEvent.value)
      }
    }
  }

  @Test
  fun testView_withTalkbackEnabled_clickOnDefaultRegion_defaultRegionNotClicked() {
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use { scenario ->
      scenario.onActivity {
        it.clickable_image_view.setListener(onClickableAreaClickedListener)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.0f, 0.0f)
        )
        onView(withId(R.id.default_selected_region)).check(
          matches(not(isDisplayed()))
        )

        assertEquals(DefaultRegionClickedEvent(), regionClickedEvent.value)
      }
    }
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      NetworkModule::class, LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      LogStorageModule::class, ImageClickInputModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(imageRegionSelectionAccessibilityTest: ImageRegionSelectionInteractionViewTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerImageRegionSelectionInteractionViewTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(imageRegionSelectionAccessibilityTest: ImageRegionSelectionInteractionViewTest) {
      component.inject(imageRegionSelectionAccessibilityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
