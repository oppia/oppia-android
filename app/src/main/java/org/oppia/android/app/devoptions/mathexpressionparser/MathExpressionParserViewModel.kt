package org.oppia.android.app.devoptions.mathexpressionparser

import android.widget.TextView
import androidx.databinding.ObservableField
import javax.inject.Inject
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
    // TODO: replace this with a variant that doesn't require the GCS properties.
    htmlParserFactory.create("", "", "", false)
  }
  private lateinit var parseResultTextView: TextView
  var mathExpression = ObservableField<String>()
  var allowedVariables = ObservableField<String>("x,y")
  private var parseType = ParseType.NUMERIC_EXPRESSION
  private var resultType = ResultType.MATH_EXPRESSION
  private var useDivAsFractions = false

  fun initialize(parseResultTextView: TextView) {
    this.parseResultTextView = parseResultTextView
    updateParseResult()
  }

  fun onParseButtonClicked() {
    updateParseResult()
  }

  fun onParseTypeSelected(parseType: ParseType) {
    this.parseType = parseType
  }

  fun onResultTypeSelected(resultType: ResultType) {
    this.resultType = resultType
  }

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

  enum class ParseType {
    NUMERIC_EXPRESSION,
    ALGEBRAIC_EXPRESSION,
    ALGEBRAIC_EQUATION
  }

  enum class ResultType {
    MATH_EXPRESSION,
    COMPARABLE_OPERATION_LIST,
    POLYNOMIAL,
    LATEX,
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
        ResultType.COMPARABLE_OPERATION_LIST -> map { it.toComparableOperation() }
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
        ResultType.COMPARABLE_OPERATION_LIST -> map {
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
