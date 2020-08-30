package org.oppia.app.parser

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.RatioExpression
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [StringToRatioParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class StringToRatioParserTest {

  private lateinit var stringToRatioParser: StringToRatioParser
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    stringToRatioParser = StringToRatioParser()
  }

  @Test
  fun testParser_realtimeError_answerWithAlphabets_returnInvalidCharsError() {
    val error =
      stringToRatioParser.getRealTimeAnswerError("abc").getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(
      "Please write a ratio that consists of digits separated by colons (e.g. 1:2 or 1:2:3)."
    )
  }

  @Test
  fun testParser_realtimeError_answerWithTwoAdjacentColons_returnInvalidColonsError() {
    val error = stringToRatioParser.getRealTimeAnswerError("1::2")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Your answer has two colons (:) next to each other.")
  }

  @Test
  fun testParser_realtimeError_answerWithCorrectRatio_returnValid() {
    val error = stringToRatioParser.getRealTimeAnswerError("1:2:3")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsZero_returnValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 0)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsThree_returnInvalidSizeError() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 3)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Number of terms is not equal to the required terms.")
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFour_returnInvalidSizeError() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFive_returnInvalidSizeError() {
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
  fun testParser_submitTimeError_answerWithZeroComponent_returnIncludesZero() {
    val error =
      stringToRatioParser.getSubmitTimeError("1:2:0", numberOfTerms = 3)
        .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Ratios cannot have 0 as a element.")
  }

  @Test
  fun testParser_submitTimeError_returnValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_parseRatioOrNull_returnRatioExpression() {
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

  // TODO(#89): Move to a common test library.
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
  }
}
