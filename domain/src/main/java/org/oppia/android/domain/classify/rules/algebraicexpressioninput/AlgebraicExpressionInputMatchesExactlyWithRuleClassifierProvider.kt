package org.oppia.android.domain.classify.rules.algebraicexpressioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.MathExpression
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an algebraic expression is exactly equal to the
 * creator-specific expression defined as the input to this interaction, including any parenthetical
 * groups in the expressions and operand order.
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class AlgebraicExpressionInputMatchesExactlyWithRuleClassifierProvider @Inject constructor(
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
    val allowedVariables = classificationContext.extractAllowedVariables()
    val answerExpression = parseExpression(answer, allowedVariables) ?: return false
    val inputExpression = parseExpression(input, allowedVariables) ?: return false
    return answerExpression.isApproximatelyEqualTo(inputExpression)
  }

  private fun parseExpression(
    rawExpression: String,
    allowedVariables: List<String>
  ): MathExpression? {
    return when (val expResult = parseAlgebraicExpression(rawExpression, allowedVariables)) {
      is MathParsingResult.Success -> expResult.result
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraExpMatchesExact",
          "Encountered expression that failed parsing. Expression: $rawExpression." +
            " Failure: ${expResult.error}."
        )
        null
      }
    }
  }

  private companion object {
    private fun ClassificationContext.extractAllowedVariables(): List<String> {
      return customizationArgs["customOskLetters"]
        ?.schemaObjectList
        ?.schemaObjectList
        ?.map { it.normalizedString }
        ?: listOf()
    }
  }
}
