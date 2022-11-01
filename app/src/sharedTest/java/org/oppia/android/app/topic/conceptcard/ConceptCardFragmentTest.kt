package org.oppia.android.app.topic.conceptcard

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
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
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
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
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
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

      onView(withContentDescription(R.string.concept_card_fragment_concept_card_close_icon_description))
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
      ).inRoot(isDialog()).check(matches(withText(R.string.concept_card_fragment_concept_card_toolbar_title)))
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
      ).inRoot(isDialog()).check(matches(withText(R.string.concept_card_fragment_concept_card_toolbar_title)))
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
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_0)).perform(click())
      testCoroutineDispatchers.runCurrent()

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
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      testCoroutineDispatchers.runCurrent()

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
    launchTestActivity().use {
      onView(withId(R.id.open_dialog_1)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

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
      TestModule::class, PlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
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
      CpuPerformanceSnapshotterModule::class
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
