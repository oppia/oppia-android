package org.oppia.android.app.topic.info

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
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

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val topicThumbnail = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
  private val internalProfileId = 0

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
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
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(TOPIC_NAME))))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescription_isCorrect() {
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
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
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_thumbnail_image_view)).check(matches(withDrawable(topicThumbnail)))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicName_isCorrect() {
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
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
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_tabs_viewpager_container)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_configurationLandscape_imageViewNotDisplayed() {
    launchTopicActivityIntent(internalProfileId, TEST_TOPIC_ID).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.topic_thumbnail_image_view)).check(doesNotExist())
    }
  }

  @Test
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_checkDefaultTopicDescriptionLines_fiveLinesVisible() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
      onView(withId(R.id.topic_description_text_view))
        .check(
          matches(
            maxLines(
              /* lineCount= */ 5
            )
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_moreThanFiveLines_seeMoreIsVisible() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
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
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_seeMoreIsVisible_and_fiveLinesVisible() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
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
              /* lineCount= */ 5
            )
          )
        )
    }
  }

  @Test
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_clickSeeMore_seeLessVisible() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
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
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_seeMoreIsVisible() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(isDisplayed()))
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_more)))
    }
  }

  @Test
  // TODO(#973): Fix TopicInfoFragmentTest
  @Ignore
  fun testTopicInfoFragment_loadFragment_clickSeeMore_textChangesToSeeLess() {
    launchTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID).use {
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).perform(click())
      onView(withId(R.id.see_more_text_view)).perform(scrollTo())
      onView(withId(R.id.see_more_text_view)).check(matches(withText(R.string.see_less)))
    }
  }

  private fun launchTopicActivityIntent(
    internalProfileId: Int,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    val intent =
      TopicActivity.createTopicActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        topicId
      )
    return ActivityScenario.launch(intent)
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
