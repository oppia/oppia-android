package org.oppia.app.help

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
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
import org.oppia.app.help.faq.FAQListActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HelpFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class HelpFragmentTest {
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun createHelpActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return HelpActivity.createHelpActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      isFromNavigationDrawer
    )
  }

  @Test
  fun testHelpFragment_parentIsExploration_checkBackArrowVisible() {
    launch<HelpActivity>(createHelpActivityIntent(0, false)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testHelpFragment_parentIsNotExploration_checkBackArrowNotVisible() {
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  fun openHelpActivity_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() {
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(R.id.help_fragment_recycler_view, 0, R.id.help_item_text_view)
      ).check(
        matches(withText(R.string.frequently_asked_questions_FAQ))
      )
    }
  }

  @Test
  fun openHelpActivity_configurationChanged_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() { // ktlint-disable max-line-length
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.help_fragment_recycler_view, 0, R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.frequently_asked_questions_FAQ)))
    }
  }

  @Test
  fun openHelpActivity_selectFAQ_showFAQActivitySuccessfully() {
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(atPosition(R.id.help_fragment_recycler_view, 0)).perform(click())
      intended(hasComponent(FAQListActivity::class.java.name))
    }
  }

  @Test
  fun openHelpActivity_openNavigationDrawer_navigationDrawerOpeningIsVerifiedSuccessfully() {
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.help_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun openHelpActivity_openNavigationDrawerAndClose_closingOfNavigationDrawerIsVerifiedSuccessfully() { // ktlint-disable max-line-length
    launch<HelpActivity>(createHelpActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.help_activity_drawer_layout)).perform(close())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isClosed()))
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
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(helpFragmentTest: HelpFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHelpFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(helpFragmentTest: HelpFragmentTest) {
      component.inject(helpFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
