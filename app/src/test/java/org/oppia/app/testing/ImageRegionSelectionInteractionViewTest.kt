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
import junit.framework.Assert.assertEquals
import kotlinx.android.synthetic.main.image_region_selection_test_fragment.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
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
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.Point2d
import org.oppia.app.player.state.StateFragment
import org.oppia.app.utility.NamedRegionClickedEvent
import org.oppia.app.utility.OnClickableAreaClickedListener
import org.oppia.app.utility.RegionClickedEvent
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
      it.onActivity { activity ->
        setUpActivity(activity)

        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.3f, 0.3f)
        )
        onView(allOf(withTagValue(`is`("Region 3"))))
          .check(
            matches(isDisplayed())
          )

        verify(onClickableAreaClickedListener, times(1)).onClickableAreaTouched(
          NamedRegionClickedEvent("Region 3")
        )
      }
    }
  }

  private fun setUpActivity(activity: ImageRegionSelectionTestActivity) = with(activity) {
    val clickableAreas: List<ImageWithRegions.LabeledRegion> = getClickableAreas()
    clickable_image_view.setClickableAreas(clickableAreas)
    val clickableAreasImage = org.oppia.app.utility.ClickableAreasImage(
      clickable_image_view,
      image_parent_view,
      onClickableAreaClickedListener
    )
    clickableAreasImage.addRegionViews()
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickRegion3_clickRegion2_Region2Clicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
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
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_clickOnDefaultRegion_defaultRegionClicked() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      it.onActivity { activity ->
        setUpActivity(activity)
        onView(withId(R.id.clickable_image_view)).perform(
          clickPoint(0.0f, 0.0f)
        )
        onView(withId(R.id.default_selected_region)).check(
          matches(isDisplayed())
        )
        val argumentCaptor = ArgumentCaptor.forClass(RegionClickedEvent::class.java)
        verify(onClickableAreaClickedListener).onClickableAreaTouched(argumentCaptor.capture())
        assertEquals("", argumentCaptor.value)
      }
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_clickRegion2_Region2Clicked() { // ktlint-disable max-line-length
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use {
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
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickRegion3_Region3Clicked() {
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )
    }
  }

  @Test
  fun testImageRegionSelectionInteractionView_withTalkbackEnabled_clickOnDefaultRegion_defaultRegionNotClicked() { // ktlint-disable max-line-length
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use { activityScenario ->
      onView(withId(R.id.clickable_image_view)).perform(
        clickPoint(0.0f, 0.0f)
      )
      onView(withId(R.id.default_selected_region)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
  }

  private fun getClickableAreas(): List<ImageWithRegions.LabeledRegion> {
    return listOf(
      createLabeledRegion(
        "Region 1",
        createPoint2d(0.553030303030303f, 0.5470132743362832f) to
          createPoint2d(0.7613636363636364f, 0.7638274336283186f)
      ),
      createLabeledRegion(
        "Region 2",
        createPoint2d(0.5454545454545454f, 0.22842920353982302f) to
          createPoint2d(0.7537878787878788f, 0.4540929203539823f)
      ),
      createLabeledRegion(
        "Region 3",
        createPoint2d(0.24242424242424243f, 0.22400442477876106f) to
          createPoint2d(0.49242424242424243f, 0.7638274336283186f)
      )
    )
  }

  private fun createLabeledRegion(
    label: String,
    points: Pair<Point2d, Point2d>
  ): ImageWithRegions.LabeledRegion {
    return ImageWithRegions.LabeledRegion.newBuilder().setLabel(label)
      .setRegion(
        ImageWithRegions.LabeledRegion.Region.newBuilder()
          .setRegionType(ImageWithRegions.LabeledRegion.Region.RegionType.RECTANGLE)
          .setArea(
            ImageWithRegions.LabeledRegion.Region.NormalizedRectangle2d.newBuilder()
              .setUpperLeft(points.first)
              .setLowerRight(points.second)
          )
      )
      .build()
  }

  private fun createPoint2d(x: Float, y: Float): Point2d {
    return Point2d.newBuilder().setX(x).setY(y).build()
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
