package org.oppia.android.app.testing.options

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
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
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.options.READING_TEXT_SIZE
import org.oppia.android.app.options.ReadingTextSizeActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
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

private const val SMALL_TEXT_SIZE = 0
private const val MEDIUM_TEXT_SIZE = 5
private const val LARGE_TEXT_SIZE = 10

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ReadingTextSizeFragmentTest.TestApplication::class)
class ReadingTextSizeFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

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
      checkTextSize(SMALL_TEXT_SIZE)
      updateTextSize(LARGE_TEXT_SIZE)
      rotateToLandscape()
      checkTextSize(LARGE_TEXT_SIZE)
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  @LooperMode(LooperMode.Mode.PAUSED)
  fun testTextSize_clickTextSize_changeTextSizeToLarge_checkOptionsFragmentIsUpdatedCorrectly() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      testCoroutineDispatchers.runCurrent()
      updateTextSize(MEDIUM_TEXT_SIZE)
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

  private fun clickSeekBar(position: Int): ViewAction {
    return GeneralClickAction(
      Tap.SINGLE,
      CoordinatesProvider { view ->
        val seekBar = view as SeekBar
        val screenPos = IntArray(2)
        seekBar.getLocationInWindow(screenPos)
        val trueWith = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight

        val percentagePos = (position.toFloat() / seekBar.max)
        val screenX = trueWith * percentagePos + screenPos[0] + seekBar.paddingLeft
        val screenY = seekBar.height / 2f + screenPos[1]
        val coordinates = FloatArray(2)
        coordinates[0] = screenX
        coordinates[1] = screenY
        coordinates
      },
      Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0
    )
  }

  private fun seekBarProgress(progress: Int): TypeSafeMatcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description?) {
        description?.appendText("SeekBarProgress")
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as SeekBar).progress == progress
      }
    }
  }

  private fun checkTextSize(value: Int) {
    onView(withId(R.id.reading_text_size_seekBar)).check(matches(seekBarProgress(value)))
    testCoroutineDispatchers.runCurrent()
  }

  private fun updateTextSize(value: Int) {
    onView(withId(R.id.reading_text_size_seekBar)).perform(clickSeekBar(value))
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
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      ImageClickInputModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogUploadWorkerModule::class, FakeOppiaClockModule::class
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
