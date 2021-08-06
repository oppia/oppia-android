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
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Description
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
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.CacheAssetsLocally
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
  val accessibilityTestRule = AccessibilityTestRule()

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
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent("Small")).use {
      verifyItemIsCheckedInTextSizeRecyclerView(SMALL_TEXT_SIZE_INDEX)
      clickOnTextSizeRecyclerViewItem(LARGE_TEXT_SIZE_INDEX)
      rotateToLandscape()
      verifyItemIsCheckedInTextSizeRecyclerView(LARGE_TEXT_SIZE_INDEX)
    }
  }

  @Test
  fun testTextSize_checkTextSizeOfAllFourItems_textSizeMatchedCorrectly() {
    launch<ReadingTextSizeActivity>(createReadingTextSizeActivityIntent("Small")).use {
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

  @Test
  @Config(qualifiers = "sw600dp")
  fun testTextSize_changeTextSizeToMedium_mediumItemIsSelected() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      clickOnTextSizeRecyclerViewItem(MEDIUM_TEXT_SIZE_INDEX)
      checkTextSizeLabel("Medium")
    }
  }

  private fun createReadingTextSizeActivityIntent(summaryValue: String): Intent {
    return ReadingTextSizeActivity.createReadingTextSizeActivityIntent(
      ApplicationProvider.getApplicationContext(),
      READING_TEXT_SIZE,
      summaryValue
    )
  }

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
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

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
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
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogUploadWorkerModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
