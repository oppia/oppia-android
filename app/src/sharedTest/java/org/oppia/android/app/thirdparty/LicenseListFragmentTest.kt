package org.oppia.android.app.thirdparty

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import org.oppia.android.app.help.thirdparty.LicenseTextViewerActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LicenseListFragmentTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = LicenseListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class LicenseListFragmentTest {
  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun openLicenseListActivity_selectItem_opensLicenseTextViewerActivity() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(
        allOf(
          hasComponent(LicenseTextViewerActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openLicenseListActivity_changeConfig_selectItem_opensLicenseTextViewerActivity() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(
        allOf(
          hasComponent(LicenseTextViewerActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex0_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex0_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex1_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(1)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_1))))
      onView(withText(R.string.license_name_1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex1_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(1)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0,
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_1))))
      onView(withText(R.string.license_name_1)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex2_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(2)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex2_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(2)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex3_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(3)).use {
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun openLicenseListActivity_dependencyIndex3_configLandscape_displaysCorrectListOfLicenses() {
    launch<LicenseListActivity>(createLicenseListActivity(3)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 0
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_2))))
      onView(withText(R.string.license_name_2)).check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.license_list_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPosition(
          recyclerViewId = R.id.license_list_fragment_recycler_view,
          position = 1
        )
      ).check(matches(hasDescendant(withText(R.string.license_name_0))))
      onView(withText(R.string.license_name_0)).check(matches(isCompletelyDisplayed()))
    }
  }

  private fun createLicenseListActivity(dependencyIndex: Int): Intent {
    return LicenseListActivity.createLicenseListActivityIntent(
      ApplicationProvider.getApplicationContext(),
      dependencyIndex
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {

    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(licenseListFragmentTest: LicenseListFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLicenseListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(licenseListFragmentTest: LicenseListFragmentTest) {
      component.inject(licenseListFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
