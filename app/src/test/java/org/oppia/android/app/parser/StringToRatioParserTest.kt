package org.oppia.android.app.parser

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.time.FakeOppiaClockModule
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

/** Tests for [StringToRatioParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StringToRatioParserTest.TestApplication::class, qualifiers = "port-xxhdpi")
class StringToRatioParserTest {

  @Inject
  lateinit var context: Context

  private lateinit var stringToRatioParser: StringToRatioParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    stringToRatioParser = StringToRatioParser()
  }

  @Test
  fun testParser_realtimeError_answerWithAlphabets_returnsInvalidCharsError() {
    val error =
      stringToRatioParser.getRealTimeAnswerError("abc").getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(
      "Please write a ratio that consists of digits separated by colons (e.g. 1:2 or 1:2:3)."
    )
  }

  @Test
  fun testParser_realtimeError_answerWithTwoAdjacentColons_returnsInvalidColonsError() {
    val error = stringToRatioParser.getRealTimeAnswerError("1::2")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Your answer has two colons (:) next to each other.")
  }

  @Test
  fun testParser_realtimeError_answerWithCorrectRatio_returnsValid() {
    val error = stringToRatioParser.getRealTimeAnswerError("1:2:3")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsZero_returnsValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 0)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsThree_returnsInvalidSizeError() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 3)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Number of terms is not equal to the required terms.")
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFour_returnsValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFive_returnsInvalidSizeError() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 5)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Number of terms is not equal to the required terms.")
  }

  @Test
  fun testParser_submitTimeError_answerWithOneExtraColon_returnInvalidFormatError() {
    val error =
      stringToRatioParser.getSubmitTimeError("1:2:3:", numberOfTerms = 3)
        .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid ratio (e.g. 1:2 or 1:2:3).")
  }

  @Test
  fun testParser_realtimeError_answerWithMixedFrationRatio_returnInvalidFormatError() {
    val error = stringToRatioParser.getSubmitTimeError("1/2:3:4", 0)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid ratio (e.g. 1:2 or 1:2:3).")
  }

  @Test
  fun testParser_submitTimeError_answerWithZeroComponent_returnsIncludesZero() {
    val error =
      stringToRatioParser.getSubmitTimeError("1:2:0", numberOfTerms = 3)
        .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Ratios cannot have 0 as an element.")
  }

  @Test
  fun testParser_submitTimeError_returnsValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_parseRatioOrNull_returnsRatioExpression() {
    val parsedRatio = stringToRatioParser.parseRatioOrNull("1:2:3:4")
    val constructedRatio = createRatio(listOf(1, 2, 3, 4))
    assertThat(parsedRatio).isEqualTo(constructedRatio)
  }

  @Test
  fun testParser_parseRatioOrNull_returnNull() {
    val parsedRatio = stringToRatioParser.parseRatioOrNull("1:2:3:4:")
    assertThat(parsedRatio).isEqualTo(null)
  }

  @Test
  fun testParser_parseRatioOrThrow_ratioWithWhiteSpaces_returnRatioExpression() {
    val parsedRatio = stringToRatioParser.parseRatioOrThrow("1   :   2   : 3: 4")
    val constructedRatio = createRatio(listOf(1, 2, 3, 4))
    assertThat(parsedRatio).isEqualTo(constructedRatio)
  }

  @Test
  fun testParser_parseRatioOrThrow_ratioWithInvalidRatio_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      stringToRatioParser.parseRatioOrThrow("a:b:c")
    }
    assertThat(exception)
      .hasMessageThat()
      .contains("Incorrectly formatted ratio: a:b:c")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(stringToRatioParserTest: StringToRatioParserTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStringToRatioParserTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stringToRatioParserTest: StringToRatioParserTest) {
      component.inject(stringToRatioParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
