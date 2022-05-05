package org.oppia.android.domain.classify.rules.numericexpressioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Real
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.evaluateAsNumericExpression
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.parseNumericExpression

/**
 * Provider for a classifier that determines whether a numeric expression is numerically equivalent
 * to the creator-specific expression defined as the input to this interaction.
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class NumericExpressionInputIsEquivalentToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory,
  private val consoleLogger: ConsoleLogger
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<String> {
  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      expectedObjectType = InteractionObject.ObjectTypeCase.MATH_EXPRESSION,
      inputParameterName = "x",
      matcher = this
    )
  }

  override fun matches(
    answer: String,
    input: String,
    classificationContext: ClassificationContext
  ): Boolean {
    val answerValue = evaluateNumericExpression(answer, ALL_ERRORS) ?: return false
    val inputValue = evaluateNumericExpression(input, REQUIRED_ONLY) ?: return false
    return answerValue.isApproximatelyEqualTo(inputValue)
  }

  private fun evaluateNumericExpression(
    rawExpression: String, checkingMode: ErrorCheckingMode
  ): Real? {
    return when (val expResult = parseNumericExpression(rawExpression, checkingMode)) {
      is MathParsingResult.Success -> {
        expResult.result.evaluateAsNumericExpression().also {
          if (it == null) {
            consoleLogger.w(
              "NumericExpEquivalent", "Expression failed to evaluate: $rawExpression."
            )
          }
        }
      }
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "NumericExpEquivalent",
          "Encountered expression that failed parsing. Expression: $rawExpression." +
            " Failure: ${expResult.error}."
        )
        null
      }
    }
  }
}
