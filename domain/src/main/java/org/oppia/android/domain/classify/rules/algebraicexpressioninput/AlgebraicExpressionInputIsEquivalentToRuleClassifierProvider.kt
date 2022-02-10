package org.oppia.android.domain.classify.rules.algebraicexpressioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.math.toPolynomial
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an algebraic expression is mathematically
 * equivalent to the creator-specific expression defined as the input to this interaction.
 *
 * Note that both expressions are assumed and parsed as polynomials.
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class AlgebraicExpressionInputIsEquivalentToRuleClassifierProvider @Inject constructor(
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
    val answerExpression = parsePolynomial(answer, allowedVariables) ?: return false
    val inputExpression = parsePolynomial(input, allowedVariables) ?: return false
    return answerExpression.isApproximatelyEqualTo(inputExpression)
  }

  private fun parsePolynomial(rawExpression: String, allowedVariables: List<String>): Polynomial? {
    return when (val expResult = parseAlgebraicExpression(rawExpression, allowedVariables)) {
      is MathParsingResult.Success -> {
        expResult.result.toPolynomial().also {
          if (it == null) {
            consoleLogger.w(
              "AlgebraExpEquivalent", "Expression is not a supported polynomial: $rawExpression."
            )
          }
        }
      }
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraExpEquivalent",
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
