package org.oppia.app.mydownloads

import android.app.Application
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
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
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
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
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MyDownloadsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MyDownloadsFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class MyDownloadsFragmentTest {
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testMyDownloadsFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.my_downloads_toolbar))
        )
      ).check(
        matches(
          withText("My Downloads")
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_showsMyDownloadsFragmentWithMultipleTabs() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).perform(click())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testMyDownloadsFragment_swipePage_hasSwipedPage() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_defaultTabIsDownloads_isSuccessful() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_clickOnDownloadsTab_showsDownloadsTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(MyDownloadsTab.getTabForPosition(0).name),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsFragment_clickOnUpdatesTab_showsUpdatesTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.tab_updates),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

    fun inject(myDownloadsFragmentTest: MyDownloadsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMyDownloadsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(myDownloadsFragmentTest: MyDownloadsFragmentTest) {
      component.inject(myDownloadsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
