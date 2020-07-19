package org.oppia.app.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.state.StateFragment
import org.oppia.app.utility.clickAtXY
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
@Config(application = ImageRegionSelectionAccessibilityTest.TestApplication::class)
class ImageRegionSelectionAccessibilityTest {

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var fakeAccessibilityManager: FakeAccessibilityManager

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnRegion3() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )
      onView(allOf(withId(R.id.submit_button)))
        .check(
          matches(isEnabled())
        )
    }
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnRegion3_clickOnRegion2_onlyRegion2IsDisplayed() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )

      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.7f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 2"))))
        .check(
          matches(isDisplayed())
        )
      onView(allOf(withId(R.id.submit_button)))
        .check(
          matches(isEnabled())
        )
    }
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnDefaultRegion() {
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.0f, 0.0f)
      )
      onView(withId(R.id.default_selected_region)).check(
        matches((isDisplayed()))
      )
      onView(allOf(withId(R.id.submit_button)))
        .check(
          matches(not(isEnabled()))
        )
    }
  }

  @Test
  fun testImageRegionSelectionTestActivity_clickOnDefaultRegionWithAccessibility() {
    fakeAccessibilityManager.setTalkbackEnabled(true)
    launch(ImageRegionSelectionTestActivity::class.java).use {
      onView(withId(R.id.clickable_image_view)).perform(
        clickAtXY(0.3f, 0.3f)
      )
      onView(allOf(withTagValue(`is`("Region 3"))))
        .check(
          matches(isDisplayed())
        )
      onView(allOf(withId(R.id.submit_button)))
        .check(
          matches(isEnabled())
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
      ImageClickInputModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(imageRegionSelectionAccessibilityTest: ImageRegionSelectionAccessibilityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerImageRegionSelectionAccessibilityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(imageRegionSelectionAccessibilityTest: ImageRegionSelectionAccessibilityTest) {
      component.inject(imageRegionSelectionAccessibilityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
