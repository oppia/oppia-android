package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
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

// TODO(#3418): Separate DeveloperOptionsActivityTest into activity and fragment test files.
/** Tests for [DeveloperOptionsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = DeveloperOptionsActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class DeveloperOptionsActivityTest {

  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    DeveloperOptionsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testDeveloperOptionsActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createDeveloperOptionsActivityIntent(internalProfileId))
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.developer_options_activity_title))
  }

  @Test
  fun testDeveloperOptionsFragment_modifyLessonProgressIsDisplayed() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 0,
        targetView = R.id.modify_lesson_progress_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_chapters_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_chapters_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_stories_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_stories_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_topics_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_topics_completed
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_viewLogsIsDisplayed() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 1,
        targetView = R.id.view_logs_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.event_logs_text_view,
        stringIdToMatch = R.string.developer_options_event_logs
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_overrideAppBehaviorsIsDisplayed() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 2,
        targetView = R.id.override_app_behaviors_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.show_all_hints_solution_text_view,
        stringIdToMatch = R.string.developer_options_show_all_hints_solution
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_network_type_text_view,
        stringIdToMatch = R.string.developer_options_force_network_type
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_crash_text_view,
        stringIdToMatch = R.string.developer_options_force_crash
      )
    }
  }

  // TODO(#3397): When the logic to show all hints and solutions is implemented, write a test to
  //  check for click operation of the 'Show all hints/solution' switch
  @Test
  fun testDeveloperOptionsFragment_hintsAndSolutionSwitchIsUncheck() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.developer_options_list,
          position = 2,
          targetViewId = R.id.show_all_hints_solution_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickForceCrash_throwsRuntimeException() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      val exception = assertThrows(RuntimeException::class) {
        scrollToPosition(position = 2)
        onView(withId(R.id.force_crash_text_view)).perform(click())
      }
      assertThat(exception.cause).hasMessageThat().contains("Force crash occurred")
    }
  }

  @Test
  fun testDeveloperOptionsFragment_configChange_clickForceCrash_throwsRuntimeException() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      val exception = assertThrows(RuntimeException::class) {
        scrollToPosition(position = 2)
        onView(withId(R.id.force_crash_text_view)).perform(click())
      }
      assertThat(exception.cause).hasMessageThat().contains("Force crash occurred")
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickEventLogs_opensViewEventLogsActivity() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(withId(R.id.event_logs_text_view)).perform(click())
      intended(hasComponent(ViewEventLogsActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_configChange_clickEventLogs_opensViewEventLogsActivity() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      onView(withId(R.id.event_logs_text_view)).perform(click())
      intended(hasComponent(ViewEventLogsActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_selectDevOptionsNavItem_developerOptionsListIsDisplayed() {
    launch<DeveloperOptionsActivity>(
      createDeveloperOptionsActivityIntent(internalProfileId)
    ).use {
      it.openNavigationDrawer()
      onView(withId(R.id.developer_options_linear_layout)).perform(nestedScrollTo())
        .perform(click())
      onView(withId(R.id.developer_options_list)).check(matches(isDisplayed()))
    }
  }

  private fun createDeveloperOptionsActivityIntent(internalProfileId: Int): Intent {
    return DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, internalProfileId)
  }

  private fun ActivityScenario<DeveloperOptionsActivity>.openNavigationDrawer() {
    onView(withContentDescription(R.string.drawer_open_content_description))
      .check(matches(isCompletelyDisplayed()))
      .perform(click())

    // Force the drawer animation to start. See https://github.com/oppia/oppia-android/pull/2204 for
    // background context.
    onActivity { activity ->
      val drawerLayout =
        activity.findViewById<DrawerLayout>(R.id.developer_options_activity_drawer_layout)
      // Note that this only initiates a single computeScroll() in Robolectric. Normally, Android
      // will compute several of these across multiple draw calls, but one seems sufficient for
      // Robolectric. Note that Robolectric is also *supposed* to handle the animation loop one call
      // to this method initiates in the view choreographer class, but it seems to not actually
      // flush the choreographer per observation. In Espresso, this method is automatically called
      // during draw (and a few other situations), but it's fine to call it directly once to kick it
      // off (to avoid disparity between Espresso/Robolectric runs of the tests).
      // NOTE TO DEVELOPERS: if this ever flakes, we can probably put this in a loop with fake time
      // adjustments to simulate the render loop.
      drawerLayout.computeScroll()
    }

    // Wait for the drawer to fully open (mostly for Espresso since Robolectric should synchronously
    // stabilize the drawer layout after the previous logic completes).
    testCoroutineDispatchers.runCurrent()
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return Matchers.allOf(
          isDescendantOfA(isAssignableFrom(NestedScrollView::class.java))
        )
      }

      override fun perform(uiController: UiController, view: View) {
        try {
          val nestedScrollView =
            findFirstParentLayoutOfClass(view, NestedScrollView::class.java) as NestedScrollView
          nestedScrollView.scrollTo(0, view.getTop())
        } catch (e: Exception) {
          throw PerformException.Builder()
            .withActionDescription(this.description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(e)
            .build()
        }
        uiController.loopMainThreadUntilIdle()
      }
    }
  }

  private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View {
    var parent: ViewParent = FrameLayout(view.getContext())
    lateinit var incrementView: ViewParent
    var i = 0
    while (!(parent.javaClass === parentClass)) {
      if (i == 0) {
        parent = findParent(view)
      } else {
        parent = findParent(incrementView)
      }
      incrementView = parent
      i++
    }
    return parent as View
  }

  private fun findParent(view: View): ViewParent {
    return view.getParent()
  }

  private fun findParent(view: ViewParent): ViewParent {
    return view.getParent()
  }

  private fun verifyItemDisplayedOnDeveloperOptionsListItem(
    itemPosition: Int,
    targetView: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.developer_options_list,
        position = itemPosition,
        targetViewId = targetView
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyTextOnDeveloperOptionsListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    @StringRes stringIdToMatch: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.developer_options_list,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(context.getString(stringIdToMatch))))
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.developer_options_list)).perform(
      scrollToPosition<ViewHolder>(
        position
      )
    )
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

    fun inject(developerOptionsActivityTest: DeveloperOptionsActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDeveloperOptionsActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(developerOptionsActivityTest: DeveloperOptionsActivityTest) {
      component.inject(developerOptionsActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
