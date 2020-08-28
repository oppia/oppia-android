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
  fun testParser_realtimeError_returnInvalidCharsError() {
    val error =
      stringToRatioParser.getRealTimeAnswerError("abc").getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(
      "Please write a ratio that consists of digits separated by colons (e.g. 1:2 or 1:2:3)."
    )
  }

  @Test
  fun testParser_realtimeError_returnInvalidFormatError() {
    val error =
      stringToRatioParser.getRealTimeAnswerError("1:2:3:").getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid ratio (e.g. 1:2 or 1:2:3)")
  }

  @Test
  fun testParser_realtimeError_returnInvalidColonsError() {
    val error = stringToRatioParser.getRealTimeAnswerError("1::2")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Your answer has two colons (:) next to each other.")
  }

  @Test
  fun testParser_realtimeError_returnValid() {
    val error = stringToRatioParser.getRealTimeAnswerError("1:2:3").name
    assertThat(error).isEqualTo("VALID")
  }

  @Test
  fun testParser_submitTimeError_returnInvalidSizeError() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", 5)
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Number of terms is less than required terms.")
  }

  @Test
  fun testParser_submitTimeError_returnValid() {
    val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", 4).name
    assertThat(error).isEqualTo("VALID")
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
    val parsedRatio = stringToRatioParser.parseRatioOrNull("1   :   2   : 3: 4 :")
    val constructedRatio = createRatio(listOf(1, 2, 3, 4))
    assertThat(parsedRatio).isEqualTo(constructedRatio)
  }

  @Test
  fun testRatio_ratio_returnAccessibleRatioString() {
    val ratio = createRatio(listOf(1, 2, 3))
    assertThat(
      ratio.toAccessibleAnswerString(
        context
      )
    ).isEqualTo("1 to 2 to 3")
  }

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
  }

}