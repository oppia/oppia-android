package org.oppia.android.app.topic.conceptcard

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ConceptCardFragmentTestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.TEST_SKILL_ID_0
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RichTextViewMatcher.Companion.containsRichText
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.ConceptCardFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** Tests for [ConceptCardFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ConceptCardFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ConceptCardFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()

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
  fun testConceptCardFragment_clickOnToolbarNavigationButton_closeActivity() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withContentDescription(R.string.navigate_up))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_toolbar)).check(doesNotExist())
    }
  }

  @Test
  fun testConceptCardFragment_toolbarTitle_isDisplayedSuccessfully() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

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
    launchTestActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()
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
    launchTestActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Hello. Welcome to Oppia."))))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment0_checkExplanationDisplayedWithoutRichText() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("An important skill")))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Hello. Welcome to Oppia."))))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(not(containsRichText())))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment1_checkExplanationDisplayedWithRichText() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("Another important skill")))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(not(withText("An important skill"))))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Explanation with rich text."))))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(containsRichText()))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragment1_clickOnConceptCardLink_opensConceptCard() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Click the concept card link to open it.
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .perform(openClickableSpan("test_skill_id_0 concept card"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("An important skill")))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Hello. Welcome to Oppia."))))
    }
  }

  @Test
  fun testConceptCardFragment_openDialogFragmentWithSkill2_configChange_workedExamplesDisplayed() {
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("Another important skill")))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Explanation with rich text."))))
      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(containsRichText()))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testConceptCardFragment_englishContentLang_explanationIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Welcome to Oppia"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testConceptCardFragment_englishContentLang_switchToArabic_explanationIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Switch to Arabic after opening the card. It should trigger an update to the text with the
      // correct translation shown.
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("مرحبا بكم في"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testConceptCardFragment_profileWithArabicContentLang_explanationIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.concept_card_explanation_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("مرحبا بكم في"))))
    }
  }

  @Test
  fun testConceptCardFragment_launchSeveralConceptCardsWithSameSkill_onlyTheFirstShows() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragment =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        assertThat(activity.supportFragmentManager.fragments).hasSize(1)
        assertThat(activity.supportFragmentManager.fragments[0]).isEqualTo(fragment)
      }
    }
  }

  @Test
  fun testConceptCardFragment_twoConceptCards_secondOneReplacesFirstOne() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill0 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_1,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill1 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        assertThat(fragmentSkill1).isNotNull()
        assertThat(activity.supportFragmentManager.fragments).hasSize(1)
        assertThat(activity.supportFragmentManager.fragments[0]).isEqualTo(fragmentSkill1)
        assertThat(fragmentSkill1).isNotEqualTo(fragmentSkill0)
      }
    }
  }

  @Test
  fun testConceptCardFragment_severalConceptCards_dismissAll() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill0 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_1,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill1 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        assertThat(fragmentSkill1).isNotEqualTo(fragmentSkill0)
        fragmentSkill0.showNow(activity.supportFragmentManager, fragmentSkill0.tag)
        assertThat(activity.supportFragmentManager.fragments).hasSize(2)
        ConceptCardFragment.dismissAll(activity.supportFragmentManager)
        assertThat(activity.supportFragmentManager.fragments).isEmpty()
      }
    }
  }

  @Test
  fun testConceptCardFragment_mixedWithNonConceptCardFragments_onlyConceptCardsAreAffected() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->
        // Show a non-ConceptCard fragment
        val randomFragment = TestFragment()
        randomFragment.showNow(activity.supportFragmentManager, "test_tag")

        // Show two ConceptCards
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill0 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_1,
          ProfileId.getDefaultInstance(),
          activity.supportFragmentManager
        )
        val fragmentSkill1 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()
        assertThat(fragmentSkill1).isNotEqualTo(fragmentSkill0)

        // Assert that the fragment manager only has two fragments: the first and the last
        assertThat(activity.supportFragmentManager.fragments).hasSize(2)
        assertThat(activity.supportFragmentManager.fragments[0]).isEqualTo(randomFragment)
        assertThat(activity.supportFragmentManager.fragments[1]).isEqualTo(fragmentSkill1)

        // Assert that DismissAll does not dismiss non ConceptCard fragments
        ConceptCardFragment.dismissAll(activity.supportFragmentManager)
        assertThat(activity.supportFragmentManager.fragments).hasSize(1)
        assertThat(activity.supportFragmentManager.fragments[0]).isEqualTo(randomFragment)
      }
    }
  }

  @Test
  fun testConceptCardFragment_dismissAllWhenZeroFragments_noError() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->
        assertThat(activity.supportFragmentManager.fragments).isEmpty()
        ConceptCardFragment.dismissAll(activity.supportFragmentManager)
      }
    }
  }

  @Test
  fun testConceptCardFragment_arguments_workingProperly() {
    launchTestActivity().use { scenario ->
      scenario.onActivity { activity ->

        ConceptCardFragment.bringToFrontOrCreateIfNew(
          TEST_SKILL_ID_0,
          profileId,
          activity.supportFragmentManager
        )
        val fragmentSkill0 =
          activity.supportFragmentManager.fragments.filterIsInstance<ConceptCardFragment>().single()

        val arguments = checkNotNull(fragmentSkill0.arguments) {
          "Expected arguments to be passed to ConceptCardFragment"
        }
        val args = arguments.getProto(
          ConceptCardFragment.CONCEPT_CARD_FRAGMENT_ARGUMENTS_KEY,
          ConceptCardFragmentArguments.getDefaultInstance()
        )
        val skillId =
          checkNotNull(args.skillId) {
            "Expected skillId to be passed to ConceptCardFragment"
          }
        val receivedProfileId = arguments.extractCurrentUserProfileId()

        assertThat(skillId).isEqualTo(TEST_SKILL_ID_0)
        assertThat(receivedProfileId).isEqualTo(profileId)
      }
    }
  }

  private fun launchTestActivity(): ActivityScenario<ConceptCardFragmentTestActivity> {
    val scenario = ActivityScenario.launch<ConceptCardFragmentTestActivity>(
      ConceptCardFragmentTestActivity.createIntent(context, profileId)
    )
    testCoroutineDispatchers.runCurrent()
    return scenario
  }

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

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

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(text: String) =
    find { text in it.first }?.second

  @Module
  class TestModule {
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
      TestModule::class, PlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class,
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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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

class TestFragment : InjectableDialogFragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return TextView(activity)
  }
}
