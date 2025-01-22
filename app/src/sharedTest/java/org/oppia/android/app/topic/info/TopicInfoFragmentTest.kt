package org.oppia.android.app.topic.info

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicInfoFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicActivity.Companion.createTopicActivityIntent
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.espresso.ImageViewMatcher.Companion.hasScaleType
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_CLASSROOM_ID = "test_classroom_id_1"
private const val TEST_TOPIC_ID = "GJ2rLXRKD5hw"
private const val TOPIC_NAME = "Fractions"

private const val TOPIC_DESCRIPTION =
  "You'll often need to talk about part of an object or group. For example, " +
    "a jar of milk might be half-full, or some of the eggs in a box might have broken. " +
    "In these lessons, you'll learn to use fractions to describe situations like these."
private const val DUMMY_TOPIC_DESCRIPTION_LONG =
  "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
    "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, " +
    "when an unknown printer took a galley of type and scrambled it to make a type " +
    "specimen book. It has survived not only five centuries, but also the leap into " +
    "electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s " +
    "with the release of Letraset sheets containing Lorem Ipsum passages, and more " +
    "recently with desktop publishing software like Aldus PageMaker " +
    "including versions of Lorem Ipsum."

/** Tests for [TopicInfoFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicInfoFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicInfoFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject EnableExtraTopicTabsUi]
  lateinit var enableExtraTopicTabsUi: PlatformParameterValue<Boolean>

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  private val topicThumbnail = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableExtraTopicTabsUi(true)
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testTopicInfoFragment_loadFragment_checkTopicName_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(TOPIC_NAME))))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescription_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_description_text_view)).check(
        matches(
          withText(
            containsString(
              TOPIC_DESCRIPTION
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescriptionInRtl_isCorrect() {
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        context = context,
        profileId = profileId,
        classroomId = TEST_CLASSROOM_ID,
        topicId = TEST_TOPIC_ID
      )
    )
    testCoroutineDispatchers.runCurrent()
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    onView(withId(R.id.topic_description_text_view)).check { view, _ ->
      val topicDescriptionTextview: TextView = view.findViewById(
        R.id.topic_description_text_view
      )
      assertThat(topicDescriptionTextview.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescriptionInLtr_isCorrect() {
    activityTestRule.launchActivity(
      createTopicActivityIntent(
        context = context,
        profileId = profileId,
        classroomId = TEST_CLASSROOM_ID,
        topicId = TEST_TOPIC_ID
      )
    )
    testCoroutineDispatchers.runCurrent()
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR
    onView(withId(R.id.topic_description_text_view)).check { view, _ ->
      val topicDescriptionTextview: TextView = view.findViewById(
        R.id.topic_description_text_view
      )
      assertThat(topicDescriptionTextview.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_thumbnail_image_view)).check(matches(withDrawable(topicThumbnail)))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_checkTopicThumbnail_hasCorrectScaleType() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_thumbnail_image_view)).check(
        matches(
          hasScaleType(ImageView.ScaleType.FIT_CENTER)
        )
      )
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicName_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_name_text_view))
        .check(
          matches(
            withText(
              containsString(
                TOPIC_NAME
              )
            )
          )
        )
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_configurationLandscape_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_viewpager_container)).check(matches(isDisplayed()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_configurationLandscape_imageViewNotDisplayed() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_thumbnail_image_view)).check(doesNotExist())
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_checkDefaultTopicDescriptionLines_fiveLinesVisible() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.topic_description_text_view))
        .check(
          matches(
            maxLines(
              lineCount = 5
            )
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_moreThanFiveLines_seeMoreIsVisible() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.topic_description_text_view)).perform(
        setTextInTextView(
          DUMMY_TOPIC_DESCRIPTION_LONG
        )
      )
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(isDisplayed()))
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_more)))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_seeMoreIsVisible_and_fiveLinesVisible() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.topic_description_text_view)).perform(
        setTextInTextView(
          DUMMY_TOPIC_DESCRIPTION_LONG
        )
      )
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_description_text_view))
        .check(
          matches(
            maxLines(
              lineCount = 5
            )
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_clickSeeMore_seeLessVisible() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.topic_description_text_view)).perform(
        setTextInTextView(
          DUMMY_TOPIC_DESCRIPTION_LONG
        )
      )
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).perform(click())
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_less)))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_seeMoreIsVisible() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(isDisplayed()))
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_more)))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#2057): Enable for Robolectric.
  fun testTopicInfoFragment_loadFragment_clickSeeMore_textChangesToSeeLess() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = RATIOS_TOPIC_ID
    ).use {
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).perform(click())
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_less)))
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID,
      topicId = TEST_TOPIC_ID
    ).use { scenario ->
      clickInfoTab()
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val topicFragment = activity.supportFragmentManager
          .findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment
        val viewPager = topicFragment.requireView()
          .findViewById<ViewPager2>(R.id.topic_tabs_viewpager)
        val topicInfoFragment = topicFragment.childFragmentManager
          .findFragmentByTag("f${viewPager.currentItem}") as TopicInfoFragment

        val args = topicInfoFragment.arguments?.getProto(
          TopicInfoFragment.TOPIC_INFO_FRAGMENT_ARGUMENTS_KEY,
          TopicInfoFragmentArguments.getDefaultInstance()
        )
        val receivedInternalProfileId = topicInfoFragment
          .arguments?.extractCurrentUserProfileId()?.internalId ?: -1
        val receivedTopicId = checkNotNull(args?.topicId) {
          "Expected topic ID to be included in arguments for TopicInfoFragment."
        }

        assertThat(receivedInternalProfileId).isEqualTo(profileId.internalId)
        assertThat(receivedTopicId).isEqualTo(TEST_TOPIC_ID)
      }
    }
  }

  private fun launchTopicActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        profileId,
        classroomId,
        topicId
      )
    return ActivityScenario.launch(intent)
  }

  private fun clickInfoTab() {
    onView(
      allOf(
        withText(
          TopicTab.getTabForPosition(
            position = 0,
            enableExtraTopicTabsUi = enableExtraTopicTabsUi.value
          ).name
        ),
        ViewMatchers.isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /** Custom function to set dummy text in the TextView. */
  private fun setTextInTextView(value: String): ViewAction {
    return object : ViewAction {
      override fun getConstraints(): Matcher<View> {
        return CoreMatchers.allOf(
          isDisplayed(),
          ViewMatchers.isAssignableFrom(TextView::class.java)
        )
      }

      override fun perform(uiController: UiController, view: View) {
        (view as TextView).text = value
      }

      override fun getDescription(): String {
        return "replace text"
      }
    }
  }

  // Reference: https://stackoverflow.com/a/46296194
  /** Custom function to check the maxLines value for a TextView. */
  private fun maxLines(lineCount: Int): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(item: View): Boolean {
        return (item as TextView).lineCount == lineCount
      }

      override fun describeTo(description: Description) {
        description.appendText("isTextInLines")
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestPlatformParameterModule::class,
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
      PlatformParameterSingletonModule::class,
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

    fun inject(topicInfoFragmentTest: TopicInfoFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicInfoFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicInfoFragmentTest: TopicInfoFragmentTest) {
      component.inject(topicInfoFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
