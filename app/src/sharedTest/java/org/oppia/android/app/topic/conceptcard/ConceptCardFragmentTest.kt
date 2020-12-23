package org.oppia.android.app.topic.conceptcard

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.parser.RichTextViewMatcher.Companion.containsRichText
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ConceptCardFragmentTestActivity
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

/** Tests for [ConceptCardFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ConceptCardFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ConceptCardFragmentTest {

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testConceptCardFragment_clickOnToolbarNavigationButton_closeActivity() {
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      onView(withContentDescription(R.string.concept_card_close_icon_description))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.concept_card_toolbar)).check(doesNotExist())
    }
  }

  @Test
  fun testConceptCardFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.concept_card_toolbar))
        )
      ).inRoot(isDialog()).check(matches(withText(R.string.concept_card_toolbar_title)))
    }
  }

  @Test
  fun testConceptCardFragment_configurationChange_toolbarTitle_isDisplayedSuccessfully() {
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.open_dialog_0)).perform(click())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.concept_card_toolbar))
        )
      ).inRoot(isDialog()).check(matches(withText(R.string.concept_card_toolbar_title)))
    }
  }

  @Test
  fun testConceptCardFragment_configurationChange_conceptCardIsDisplayedCorrectly() {
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.open_dialog_0)).perform(click())
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Hello. Welcome to Oppia."
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(not(containsRichText())))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment0_checkSkillAndExplanationAreDisplayedWithoutRichText() { // ktlint-disable max-line-length
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "An important skill"
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Hello. Welcome to Oppia."
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(not(containsRichText())))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment1_checkSkillAndExplanationAreDisplayedWithRichText() { // ktlint-disable max-line-length
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Another important skill"
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Explanation with rich text."
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(containsRichText()))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_afterConfigurationChange_workedExamplesAreDisplayed() { // ktlint-disable max-line-length
    launch(ConceptCardFragmentTestActivity::class.java).use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Another important skill"
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(
          matches(
            withText(
              "Explanation with rich text."
            )
          )
        )
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(containsRichText()))
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

    fun inject(conceptCardFragmentTest: ConceptCardFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerConceptCardFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(conceptCardFragmentTest: ConceptCardFragmentTest) {
      component.inject(conceptCardFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
