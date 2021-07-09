package org.oppia.android.app.topic.revisioncard

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity.Companion.createRevisionCardActivityIntent // ktlint-disable max-line-length
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
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.SUBTOPIC_TOPIC_ID
import org.oppia.android.domain.topic.SUBTOPIC_TOPIC_ID_2
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
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

/** Tests for [RevisionCardActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RevisionCardFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RevisionCardFragmentTest {
  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  private val internalProfileId = 1

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testRevisionCardTest_overflowMenu_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.menu_help)))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRevisionCardTest_openOverflowMenu_selectHelpInOverflowMenu_opensHelpActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_help))).perform(ViewActions.click())
      intended(hasComponent(HelpActivity::class.java.name))
      intended(
        hasExtra(
          HelpActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
  }

  @Test
  fun testRevisionCardTest_openOverflowMenu_selectOptionsInOverflowMenu_opensOptionsActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).perform(ViewActions.click())
      intended(hasComponent(OptionsActivity::class.java.name))
      intended(
        hasExtra(
          OptionsActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
  }

  @Test
  fun testRevisionCardTestActivity_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is a Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2
      )
    ).use {
      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_return_button))
        .check(matches(withText(R.string.return_to_topic)))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is a Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_return_button))
        .check(matches(withText(R.string.return_to_topic)))
    }
  }

  @Test
  fun testRevisionCard_showsLinkTextForConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2
      )
    ).use {
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withText(containsString("Learn more")))
      )
    }
  }

  @Test
  fun testRevisionCard_landscape_showsLinkTextForConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withText(containsString("Learn more")))
      )
    }
  }

  @Test
  fun testRevisionCard_clickConceptCardLinkText_opensConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2
      )
    ).use {
      onView(withId(R.id.revision_card_explanation_text)).perform(openClickableSpan("Learn more"))
      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Given a picture divided into unequal parts"))))
    }
  }

  @Test
  fun testRevisionCard_landscape_clickConceptCardLinkText_opensConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_explanation_text)).perform(openClickableSpan("Learn more"))
      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Given a picture divided into unequal parts"))))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  /** See the version in StateFragmentTest for documentation details. */
  @Suppress("SameParameterValue")
  private fun openClickableSpan(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = "openClickableSpan"

      override fun getConstraints(): Matcher<View> = hasClickableSpanWithText(text)

      override fun perform(uiController: UiController?, view: View?) {
        // The view shouldn't be null if the constraints are being met.
        (view as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text)?.onClick(view)
      }
    }
  }

  /** See the version in StateFragmentTest for documentation details. */
  private fun hasClickableSpanWithText(text: String): Matcher<View> {
    return object : TypeSafeMatcher<View>(TextView::class.java) {
      override fun describeTo(description: Description?) {
        description?.appendText("has ClickableSpan with text")?.appendValue(text)
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text) != null
      }
    }
  }

  private fun TextView.getClickableSpans(): List<Pair<String, ClickableSpan>> {
    val viewText = text
    return (viewText as Spannable).getSpans(
      /* start= */ 0, /* end= */ text.length, ClickableSpan::class.java
    ).map {
      viewText.subSequence(viewText.getSpanStart(it), viewText.getSpanEnd(it)).toString() to it
    }
  }

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(
    text: String
  ): ClickableSpan? = find { text in it.first }?.second

  @Module
  class TestModule {
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @TopicListToCache
    fun provideTopicListToCache(): List<String> = listOf()

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    @LoadImagesFromAssets
    fun provideLoadImagesFromAssets(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class,
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

    fun inject(revisionCardFragmentTest: RevisionCardFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRevisionCardFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(revisionCardFragmentTest: RevisionCardFragmentTest) {
      component.inject(revisionCardFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
