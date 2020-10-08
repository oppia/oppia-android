package org.oppia.android.app.help

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
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.help.faq.FAQListActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
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
  fun openHelpActivity_configChanged_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() { 
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
  fun openHelpActivity_openNavDrawerAndClose_closingOfNavDrawerIsVerifiedSuccessfully() { 
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
  interface TestApplicationComponent : ApplicationComponent {
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
