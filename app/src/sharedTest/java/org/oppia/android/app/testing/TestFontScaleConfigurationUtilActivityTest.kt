package org.oppia.android.app.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
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
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.FontSizeMatcher.Companion.withFontSize
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
import javax.inject.Singleton

/** Tests for [TestFontScaleConfigurationUtilActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TestFontScaleConfigurationUtilActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TestFontScaleConfigurationUtilActivityTest {
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    context = ApplicationProvider.getApplicationContext()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createFontScaleTestActivityIntent(readingTextSize: String): Intent {
    return TestFontScaleConfigurationUtilActivity.createFontScaleTestActivity(
      context,
      readingTextSize
    )
  }

  @Test
  fun testFontScaleConfigurationUtil_smallTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(ReadingTextSize.SMALL_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.space_16dp)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_mediumTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(ReadingTextSize.MEDIUM_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.space_20dp)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_largeTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(ReadingTextSize.LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.space_24dp)
          )
        )
      )
    }
  }

  @Test
  fun testFontScaleConfigurationUtil_extraLargeTextSize_hasCorrectDimension() {
    launch<TestFontScaleConfigurationUtilActivity>(
      createFontScaleTestActivityIntent(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE.name)
    ).use {
      onView(withId(R.id.font_scale_content_text_view)).check(
        matches(
          withFontSize(
            context.resources.getDimension(R.dimen.space_28dp)
          )
        )
      )
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

    fun inject(
      testFontScaleConfigurationUtilActivityTest: TestFontScaleConfigurationUtilActivityTest
    )
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestFontScaleConfigurationUtilActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(
      testFontScaleConfigurationUtilActivityTest: TestFontScaleConfigurationUtilActivityTest
    ) {
      component.inject(testFontScaleConfigurationUtilActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
