package org.oppia.android.app.options

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Description
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
import org.oppia.android.app.model.ReadingTextSize.SMALL_TEXT_SIZE
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
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
import org.oppia.android.app.model.ReadingTextSize.MEDIUM_TEXT_SIZE

// TODO(#1815): Remove these duplicate values once Screenshot tests are implemented.
private const val SMALL_TEXT_SIZE_SCALE = 0.8f
private const val MEDIUM_TEXT_SIZE_SCALE = 1.0f
private const val LARGE_TEXT_SIZE_SCALE = 1.2f
private const val EXTRA_LARGE_TEXT_SIZE_SCALE = 1.4f

private const val SMALL_TEXT_SIZE_INDEX = 0
private const val MEDIUM_TEXT_SIZE_INDEX = 1
private const val LARGE_TEXT_SIZE_INDEX = 2
private const val EXTRA_LARGE_TEXT_SIZE_INDEX = 3

/** Tests for [ReadingTextSizeFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ReadingTextSizeFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ReadingTextSizeFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val defaultTextSizeInFloat by lazy {
    context.resources.getDimension(R.dimen.default_reading_text_size)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testTextSize_changeTextSizeToLarge_changeConfiguration_checkTextSizeLargeIsSelected() {
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent()).use {
      verifyItemIsCheckedInTextSizeRecyclerView(SMALL_TEXT_SIZE_INDEX)
      clickOnTextSizeRecyclerViewItem(LARGE_TEXT_SIZE_INDEX)
      rotateToLandscape()
      verifyItemIsCheckedInTextSizeRecyclerView(LARGE_TEXT_SIZE_INDEX)
    }
  }

  @Test
  fun testTextSize_checkTextSizeOfAllFourItems_textSizeMatchedCorrectly() {
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent()).use {
      matchTextSizeOfTextSizeRecyclerViewItem(
        SMALL_TEXT_SIZE_INDEX, defaultTextSizeInFloat * SMALL_TEXT_SIZE_SCALE
      )
      matchTextSizeOfTextSizeRecyclerViewItem(
        MEDIUM_TEXT_SIZE_INDEX, defaultTextSizeInFloat * MEDIUM_TEXT_SIZE_SCALE
      )
      matchTextSizeOfTextSizeRecyclerViewItem(
        LARGE_TEXT_SIZE_INDEX, defaultTextSizeInFloat * LARGE_TEXT_SIZE_SCALE
      )
      matchTextSizeOfTextSizeRecyclerViewItem(
        EXTRA_LARGE_TEXT_SIZE_INDEX, defaultTextSizeInFloat * EXTRA_LARGE_TEXT_SIZE_SCALE
      )
    }
  }

  // Requires language configurations.
  @Test
  @Config(qualifiers = "sw600dp")
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testTextSize_changeTextSizeToMedium_mediumItemIsSelected() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      clickOnTextSizeRecyclerViewItem(MEDIUM_TEXT_SIZE_INDEX)
      checkTextSizeLabel("Medium")
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent()).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val readingTextSizeFragment = activity.supportFragmentManager
          .findFragmentById(R.id.reading_text_size_container) as ReadingTextSizeFragment
        val receivedReadingTextSize = readingTextSizeFragment.retrieveFragmentArguments()
          .readingTextSize

        assertThat(receivedReadingTextSize).isEqualTo(SMALL_TEXT_SIZE)
      }
    }
  }

  @Test
  fun testFragment_saveInstanceState_verifyCorrectStateRestored() {
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent()).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val readingTextSizeFragment = activity.supportFragmentManager
          .findFragmentById(R.id.reading_text_size_container) as ReadingTextSizeFragment
        readingTextSizeFragment.readingTextSizeFragmentPresenter
          .onTextSizeSelected(MEDIUM_TEXT_SIZE)
      }

      scenario.recreate()

      scenario.onActivity { activity ->
        val newReadingTextSizeFragment = activity.supportFragmentManager
          .findFragmentById(R.id.reading_text_size_container) as ReadingTextSizeFragment
        val restoredTopicIdList =
          newReadingTextSizeFragment.readingTextSizeFragmentPresenter.getTextSizeSelected()

        assertThat(restoredTopicIdList).isEqualTo(MEDIUM_TEXT_SIZE)
      }
    }
  }

  private fun createReadingTextSizeActivityIntent() =
    ReadingTextSizeActivity.createReadingTextSizeActivityIntent(context, SMALL_TEXT_SIZE)

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      profileId,
      isFromNavigationDrawer
    )
  }

  /** Matcher for comparing the textSize of content inside a TextView to the expected size. */
  private fun matchTextViewTextSize(expectedSize: Float): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description?) {
        description?.appendText("TextView with text size: $expectedSize")
      }

      override fun matchesSafely(item: View): Boolean {
        return item is TextView && item.textSize == expectedSize
      }
    }
  }

  /** Check the selected item inside TextSizeRecyclerView. */
  private fun verifyItemIsCheckedInTextSizeRecyclerView(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.text_size_recycler_view,
        position = index,
        targetViewId = R.id.text_size_radio_button
      )
    ).check(
      matches(isChecked())
    )
    testCoroutineDispatchers.runCurrent()
  }

  /** Check the textSize of item inside TextSizeRecyclerView. */
  private fun matchTextSizeOfTextSizeRecyclerViewItem(index: Int, size: Float) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.text_size_recycler_view,
        position = index,
        targetViewId = R.id.text_size_text_view
      )
    ).check(
      matches(matchTextViewTextSize(size))
    )
    testCoroutineDispatchers.runCurrent()
  }

  /** Click on the item inside TextSizeRecyclerView. */
  private fun clickOnTextSizeRecyclerViewItem(index: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.text_size_recycler_view,
        position = index,
        targetViewId = R.id.text_size_radio_button
      )
    ).perform(
      click()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun checkTextSizeLabel(label: String) {
    onView(
      atPositionOnView(
        R.id.options_recyclerview,
        0,
        R.id.reading_text_size_text_view
      )
    ).check(
      matches(withText(label))
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, PlatformParameterModule::class, ApplicationModule::class,
      RobolectricModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogReportWorkerModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(readingTextSizeFragmentTest: ReadingTextSizeFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerReadingTextSizeFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(readingTextSizeFragmentTest: ReadingTextSizeFragmentTest) {
      component.inject(readingTextSizeFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
