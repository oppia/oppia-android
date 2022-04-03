package org.oppia.android.domain.classify.rules.mathequationinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicEquation
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a math equation is exactly equal to the
 * creator-specific equation defined as the input to this interaction, including any parenthetical
 * groups in the equations and operand order.
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class MathEquationInputMatchesExactlyWithRuleClassifierProvider @Inject constructor(
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
    val answerEquation = parseEquation(answer, allowedVariables) ?: return false
    val inputEquation = parseEquation(input, allowedVariables) ?: return false
    return answerEquation.approximatelyEquals(inputEquation)
  }

  private fun parseEquation(
    rawEquation: String,
    allowedVariables: List<String>
  ): MathEquation? {
    return when (val eqResult = parseAlgebraicEquation(rawEquation, allowedVariables)) {
      is MathParsingResult.Success -> eqResult.result
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraEqMatchesExact",
          "Encountered equation that failed parsing. Equation: $rawEquation." +
            " Failure: ${eqResult.error}."
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

    private fun MathEquation.approximatelyEquals(other: MathEquation): Boolean {
      return leftSide.isApproximatelyEqualTo(other.leftSide) &&
        rightSide.isApproximatelyEqualTo(other.rightSide)
    }
  }
}
