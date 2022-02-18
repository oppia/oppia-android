package org.oppia.android.app.devoptions.mathexpressionparser

import android.widget.TextView
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.math.MathExpressionAccessibilityUtil
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.toComparableOperation
import org.oppia.android.util.math.toPolynomial
import org.oppia.android.util.math.toRawLatex
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/**
 * View model that provides different debugging scenarios for math expressions, equations, and
 * numeric expressions.
 */
@FragmentScope
class MathExpressionParserViewModel @Inject constructor(
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
  private val htmlParserFactory: HtmlParser.Factory
) : ObservableViewModel() {
  private val htmlParser by lazy {
    // TODO(#4206): Replace this with the variant that doesn't require GCS properties.
    htmlParserFactory.create(
      gcsResourceName = "",
      entityType = "",
      entityId = "",
      imageCenterAlign = false
    )
  }
  private lateinit var parseResultTextView: TextView

  /**
   * Specifies the math expression currently being entered by the user. This is expected to be
   * directly bound to the UI.
   */
  var mathExpression = ObservableField<String>()

  /**
   * Specifies the comma-separated list of variables allowed for algebraic expressions/equations, as
   * specified by the user. This is expected to be directly bound to the UI.
   */
  var allowedVariables = ObservableField("x,y")
  private var parseType = ParseType.NUMERIC_EXPRESSION
  private var resultType = ResultType.MATH_EXPRESSION
  private var useDivAsFractions = false

  /** Initializes the view model to use [parseResultTextView] for displaying the parse result. */
  fun initialize(parseResultTextView: TextView) {
    this.parseResultTextView = parseResultTextView
    updateParseResult()
  }

  /** Callback for the UI to recompute the parse result. */
  fun onParseButtonClicked() {
    updateParseResult()
  }

  /** Callback for the UI to update the current [ParseType] used. */
  fun onParseTypeSelected(parseType: ParseType) {
    this.parseType = parseType
  }

  /** Callback for the UI to update the current [ResultType] used. */
  fun onResultTypeSelected(resultType: ResultType) {
    this.resultType = resultType
  }

  /**
   * Callback for the UI to update whether divisions should be treated as fractions for relevant
   * [ResultType]s.
   */
  fun onChangedUseDivAsFractions(useDivAsFractions: Boolean) {
    this.useDivAsFractions = useDivAsFractions
  }

  private fun updateParseResult() {
    val newText = computeParseResult()
    // Only parse HTML if there is HTML to preserve formatting.
    parseResultTextView.text = if ("oppia-noninteractive-math" in newText) {
      htmlParser.parseOppiaHtml(newText.replace("\n", "<br />"), parseResultTextView)
    } else newText
  }

  private fun computeParseResult(): String {
    val expression = mathExpression.get()
    val allowedVariables = allowedVariables.get()
      ?.split(",")
      ?.map { variable ->
        machineLocale.run {
          variable.toMachineLowerCase().trim()
        }
      } ?: listOf()
    if (expression == null) {
      return appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.math_expression_parse_result_label, "Uninitialized"
      )
    }
    val parseResult = when (parseType) {
      ParseType.NUMERIC_EXPRESSION -> {
        MathExpressionParser.parseNumericExpression(expression)
          .transformExpression(resultType, useDivAsFractions, mathExpressionAccessibilityUtil)
      }
      ParseType.ALGEBRAIC_EXPRESSION -> {
        MathExpressionParser.parseAlgebraicExpression(expression, allowedVariables)
          .transformExpression(resultType, useDivAsFractions, mathExpressionAccessibilityUtil)
      }
      ParseType.ALGEBRAIC_EQUATION -> {
        MathExpressionParser.parseAlgebraicEquation(expression, allowedVariables)
          .transformEquation(resultType, useDivAsFractions, mathExpressionAccessibilityUtil)
      }
    }
    val parseResultStr = when (parseResult) {
      is MathParsingResult.Failure -> parseResult.error.toString()
      is MathParsingResult.Success -> parseResult.result
    }
    return appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.math_expression_parse_result_label, "\n$parseResultStr"
    )
  }

  /** Defines how text expressions should be parsed. */
  enum class ParseType {
    /** Indicates that the user-inputted text should be parsed as a numeric expression. */
    NUMERIC_EXPRESSION,

    /** Indicates that the user-inputted text should be parsed as an algebraic expression. */
    ALGEBRAIC_EXPRESSION,

    /** Indicates that the user-inputted text should be parsed as an algebraic/math equation. */
    ALGEBRAIC_EQUATION
  }

  /** Defines how the parsed expression/equation should be processed and displayed. */
  enum class ResultType {
    /** Indicates that the raw parsed expression/equation proto should be displayed. */
    MATH_EXPRESSION,

    /**
     * Indicates that the comparable operation representation proto of the expression/equation
     * should be displayed.
     */
    COMPARABLE_OPERATION,

    /**
     * Indicates that the polynomial representation proto of the expression/equation should be
     * displayed.
     */
    POLYNOMIAL,

    /** Indicates that the expression should be converted to LaTeX and rendered as an image. */
    LATEX,

    /**
     * Indicates that the expression should be converted to a human-readable accessibility string
     * and displayed.
     */
    HUMAN_READABLE_STRING
  }

  private companion object {
    private fun <I, O> MathParsingResult<I>.map(transform: (I) -> O): MathParsingResult<O> {
      return when (this) {
        is MathParsingResult.Failure -> MathParsingResult.Failure(error)
        is MathParsingResult.Success -> MathParsingResult.Success(transform(result))
      }
    }

    private fun MathParsingResult<MathExpression>.transformExpression(
      resultType: ResultType,
      useDivAsFractions: Boolean,
      mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
    ): MathParsingResult<String> {
      return when (resultType) {
        ResultType.MATH_EXPRESSION -> this
        ResultType.COMPARABLE_OPERATION -> map { it.toComparableOperation() }
        ResultType.POLYNOMIAL -> map { it.toPolynomial() }
        ResultType.LATEX -> map { it.toRawLatex(useDivAsFractions).wrapAsLatexHtml() }
        ResultType.HUMAN_READABLE_STRING -> map {
          mathExpressionAccessibilityUtil.convertToHumanReadableString(
            it, OppiaLanguage.ENGLISH, useDivAsFractions
          )
        }
      }.map { it.toString() }
    }

    private fun MathParsingResult<MathEquation>.transformEquation(
      resultType: ResultType,
      useDivAsFractions: Boolean,
      mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
    ): MathParsingResult<String> {
      return when (resultType) {
        ResultType.MATH_EXPRESSION -> this
        ResultType.COMPARABLE_OPERATION -> map {
          "Left side: ${it.leftSide.toComparableOperation()}" +
            "\n\nRight side: ${it.rightSide.toComparableOperation()}"
        }
        ResultType.POLYNOMIAL -> map {
          "Left side: ${it.leftSide.toPolynomial()}\n\nRight side: ${it.rightSide.toPolynomial()}"
        }
        ResultType.LATEX -> map { it.toRawLatex(useDivAsFractions).wrapAsLatexHtml() }
        ResultType.HUMAN_READABLE_STRING -> map {
          mathExpressionAccessibilityUtil.convertToHumanReadableString(
            it, OppiaLanguage.ENGLISH, useDivAsFractions
          )
        }
      }.map { it.toString() }
    }

    private fun String.wrapAsLatexHtml(): String {
      val mathContentValue =
        "{&amp;quot;raw_latex&amp;quot;:&amp;quot;${this.replace("\\", "\\\\")}&amp;quot;}"
      return "<oppia-noninteractive-math render-type=\"block\"" +
        " math_content-with-value=\"$mathContentValue\" />"
    }
  }
}
