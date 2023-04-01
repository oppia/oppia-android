package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableBoolean
import org.oppia.android.R
import org.oppia.android.app.hintsandsolution.HintsAndSolutionViewModel.Factory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.BOOL_VALUE
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.CLICK_ON_IMAGE
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.FRACTION
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.IMAGE_WITH_REGIONS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.MATH_EXPRESSION
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NORMALIZED_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.OBJECTTYPE_NOT_SET
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.REAL
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SIGNED_INT
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.TRANSLATABLE_HTML_CONTENT_ID
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.math.MathExpressionAccessibilityUtil
import org.oppia.android.app.utility.toAccessibleAnswerString
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicEquation
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.MathExpressionParser.Companion.parseNumericExpression
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.math.toAnswerString
import org.oppia.android.util.math.toPlainString
import org.oppia.android.util.math.toRawLatex
import org.oppia.android.util.parser.html.CustomHtmlContentHandler
import javax.inject.Inject

/**
 * [HintsAndSolutionItemViewModel] that represents a solution that the user may reveal.
 *
 * Instances of this class are created using its [Factory].
 *
 * @property solutionSummary the solution's explanation text (which may contain HTML)
 * @property isSolutionRevealed whether the solution is currently expanded and viewable
 */
class SolutionViewModel private constructor(
  val solutionSummary: String,
  private val correctAnswer: InteractionObject,
  val isSolutionRevealed: ObservableBoolean,
  isSolutionExclusive: Boolean,
  private val interaction: Interaction,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
) : HintsAndSolutionItemViewModel() {
  /**
   * A screenreader-friendly version of [solutionSummary] that should be used for readout, in place
   * of the original summary.
   */
  val solutionSummaryContentDescription by lazy {
    CustomHtmlContentHandler.fromHtml(
      solutionSummary,
      imageRetriever = null,
      customTagHandlers = mapOf()
    ).toString()
  }

  /** A displayable HTML representation of the correct answer presented by this model's solution. */
  val correctAnswerHtml: String by lazy { computeCorrectAnswerHtml() }

  /** A screenreader-friendly readable version of [correctAnswerHtml]. */
  val correctAnswerContentDescription: String by lazy { computeCorrectAnswerContentDescription() }

  private val correctAnswerTextStringResId = if (isSolutionExclusive) {
    R.string.hints_list_exclusive_solution_text
  } else R.string.hints_list_possible_solution_text

  private fun computeCorrectAnswerHtml(): String {
    val answerTextHtml = when (correctAnswer.objectTypeCase) {
      NORMALIZED_STRING -> correctAnswer.normalizedString
      REAL -> correctAnswer.real.toSimplifiedPlainString()
      FRACTION -> correctAnswer.fraction.toAnswerString()
      RATIO_EXPRESSION -> correctAnswer.ratioExpression.toAnswerString()
      MATH_EXPRESSION -> when (interaction.id) {
        "NumericExpressionInput" -> correctAnswer.mathExpression.toNumericExpressionHtml()
        "AlgebraicExpressionInput" -> correctAnswer.mathExpression.toAlgebraicExpressionHtml()
        "MathEquationInput" -> correctAnswer.mathExpression.toAlgebraicEquationHtml()
        else -> error("Interaction ID not valid for math expressions: ${interaction.id}.")
      }
      LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS, SIGNED_INT, NON_NEGATIVE_INT,
      TRANSLATABLE_SET_OF_NORMALIZED_STRING, TRANSLATABLE_HTML_CONTENT_ID,
      SET_OF_TRANSLATABLE_HTML_CONTENT_IDS, SET_OF_HTML_STRING, LIST_OF_SETS_OF_HTML_STRING,
      IMAGE_WITH_REGIONS, CLICK_ON_IMAGE, NUMBER_WITH_UNITS, BOOL_VALUE, OBJECTTYPE_NOT_SET, null ->
        error("Invalid answer type for solution: $correctAnswer")
    }
    return appLanguageResourceHandler.getStringInLocaleWithWrapping(
      correctAnswerTextStringResId, answerTextHtml
    )
  }

  private fun computeCorrectAnswerContentDescription(): String {
    val readableAnswerText = when (correctAnswer.objectTypeCase) {
      NORMALIZED_STRING -> correctAnswer.normalizedString
      REAL -> correctAnswer.real.toSimplifiedReadableString()
      FRACTION -> correctAnswer.fraction.toAnswerString()
      RATIO_EXPRESSION ->
        correctAnswer.ratioExpression.toAccessibleAnswerString(appLanguageResourceHandler)
      MATH_EXPRESSION -> when (interaction.id) {
        "NumericExpressionInput" -> correctAnswer.mathExpression.toReadableNumericExpression()
        "AlgebraicExpressionInput" -> correctAnswer.mathExpression.toReadableAlgebraicExpression()
        "MathEquationInput" -> correctAnswer.mathExpression.toReadableAlgebraicEquation()
        else -> error("Interaction ID not valid for math expressions: ${interaction.id}.")
      }
      LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS, SIGNED_INT, NON_NEGATIVE_INT,
      TRANSLATABLE_SET_OF_NORMALIZED_STRING, TRANSLATABLE_HTML_CONTENT_ID,
      SET_OF_TRANSLATABLE_HTML_CONTENT_IDS, SET_OF_HTML_STRING, LIST_OF_SETS_OF_HTML_STRING,
      IMAGE_WITH_REGIONS, CLICK_ON_IMAGE, NUMBER_WITH_UNITS, BOOL_VALUE, OBJECTTYPE_NOT_SET, null ->
        error("Invalid answer type for solution: $correctAnswer")
    }
    return appLanguageResourceHandler.getStringInLocaleWithWrapping(
      correctAnswerTextStringResId, readableAnswerText
    )
  }

  private fun String.toNumericExpressionHtml() =
    parseNumericExpression().expectSuccess().toRawLatex().wrapAsLatexHtml()

  private fun String.toReadableNumericExpression() =
    parseNumericExpression().expectSuccess().toReadableString() ?: this

  private fun String.toAlgebraicExpressionHtml() =
    parseAlgebraicExpression().expectSuccess().toRawLatex().wrapAsLatexHtml()

  private fun String.toReadableAlgebraicExpression() =
    parseAlgebraicExpression().expectSuccess().toReadableString() ?: this

  private fun String.toAlgebraicEquationHtml() =
    parseAlgebraicEquation().expectSuccess().toRawLatex().wrapAsLatexHtml()

  private fun String.toReadableAlgebraicEquation() =
    parseAlgebraicEquation().expectSuccess().toReadableString() ?: this

  private fun String.parseAlgebraicExpression(): MathParsingResult<MathExpression> {
    return parseAlgebraicExpression(
      rawExpression = this,
      allowedVariables = interaction.extractAllowedVariables(),
      errorCheckingMode = REQUIRED_ONLY
    )
  }

  private fun String.parseAlgebraicEquation(): MathParsingResult<MathEquation> {
    return parseAlgebraicEquation(
      rawExpression = this,
      allowedVariables = interaction.extractAllowedVariables(),
      errorCheckingMode = REQUIRED_ONLY
    )
  }

  private fun MathExpression.toRawLatex() =
    toRawLatex(divAsFraction = interaction.extractUseFractionsForDivision())

  private fun MathEquation.toRawLatex() =
    toRawLatex(divAsFraction = interaction.extractUseFractionsForDivision())

  private fun MathExpression.toReadableString(): String? {
    return mathExpressionAccessibilityUtil.convertToHumanReadableString(
      expression = this,
      language = writtenTranslationContext.language,
      divAsFraction = interaction.extractUseFractionsForDivision()
    )
  }

  private fun MathEquation.toReadableString(): String? {
    return mathExpressionAccessibilityUtil.convertToHumanReadableString(
      equation = this,
      language = writtenTranslationContext.language,
      divAsFraction = interaction.extractUseFractionsForDivision()
    )
  }

  private fun Double.toSimplifiedReadableString(): String {
    val longPart = toLong()
    return if (isApproximatelyEqualTo(longPart.toDouble())) {
      appLanguageResourceHandler.formatLong(longPart)
    } else appLanguageResourceHandler.formatDouble(this)
  }

  private companion object {
    /**
     * Returns a plain-string representation of this [Double] with a preference toward dropping the
     * decimal if it isn't needed for the final answer.
     */
    private fun Double.toSimplifiedPlainString(): String {
      val longPart = toLong()
      return if (isApproximatelyEqualTo(longPart.toDouble())) {
        longPart.toString()
      } else toPlainString()
    }

    private fun String.parseNumericExpression() =
      parseNumericExpression(rawExpression = this, errorCheckingMode = REQUIRED_ONLY)

    private fun <T> MathParsingResult<T>.expectSuccess(): T {
      return when (this) {
        is MathParsingResult.Success -> result
        is MathParsingResult.Failure -> error("Invalid parsing result: $error.")
      }
    }

    private fun String.wrapAsLatexHtml(): String {
      val mathContentValue =
        "{&amp;quot;raw_latex&amp;quot;:&amp;quot;${this.replace("\\", "\\\\")}&amp;quot;}"
      return "<oppia-noninteractive-math render-type=\"inline\"" +
        " math_content-with-value=\"$mathContentValue\" />"
    }

    private fun Interaction.extractAllowedVariables(): List<String> {
      return customizationArgsMap["customOskLetters"]
        ?.schemaObjectList
        ?.schemaObjectList
        ?.map { it.normalizedString }
        ?: listOf()
    }

    private fun Interaction.extractUseFractionsForDivision() =
      customizationArgsMap["useFractionForDivision"]?.boolValue ?: false
  }

  /** Application-injectable factory to create [SolutionViewModel]s (see [create]). */
  class Factory @Inject constructor(
    private val appLanguageResourceHandler: AppLanguageResourceHandler,
    private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
  ) {
    /**
     * Returns a new [SolutionViewModel] with the specified summary HTML text, correct answer,
     * [isSolutionRevealed] tracking [ObservableBoolean], whether the solution is exclusive, and its
     * answer context (such as the [interaction] that the answer can be submitted to and the
     * displayed state's current [writtenTranslationContext] for localization).
     */
    fun create(
      solutionSummary: String,
      correctAnswer: InteractionObject,
      isSolutionRevealed: ObservableBoolean,
      isSolutionExclusive: Boolean,
      interaction: Interaction,
      writtenTranslationContext: WrittenTranslationContext
    ): SolutionViewModel {
      return SolutionViewModel(
        solutionSummary,
        correctAnswer,
        isSolutionRevealed,
        isSolutionExclusive,
        interaction,
        writtenTranslationContext,
        appLanguageResourceHandler,
        mathExpressionAccessibilityUtil
      )
    }
  }
}
