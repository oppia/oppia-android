package org.oppia.android.domain.classify.rules.numericexpressioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.MathExpression
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.parseNumericExpression
import org.oppia.android.util.math.stripRedundantGroups

/**
 * Provider for a classifier that determines whether a numeric expression is exactly equal to the
 * creator-specific expression defined as the input to this interaction, including any parenthetical
 * groups in the expressions and operand order.
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class NumericExpressionInputMatchesExactlyWithRuleClassifierProvider @Inject constructor(
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
    val answerExpression = parseExpression(answer, ALL_ERRORS) ?: return false
    val inputExpression = parseExpression(input, REQUIRED_ONLY) ?: return false
    return answerExpression.isApproximatelyEqualTo(inputExpression.stripRedundantGroups())
  }

  // TODO: Here & elsewhere: add checks for verifying that rule inputs can include minor errors, including redundant parentheses.
  private fun parseExpression(
    rawExpression: String, checkingMode: ErrorCheckingMode
  ): MathExpression? {
    return when (val expResult = parseNumericExpression(rawExpression, checkingMode)) {
      is MathParsingResult.Success -> expResult.result
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "NumericExpMatchesExact",
          "Encountered expression that failed parsing. Expression: $rawExpression." +
            " Failure: ${expResult.error}."
        )
        null
      }
    }
  }
}
