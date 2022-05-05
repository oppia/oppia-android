package org.oppia.android.domain.classify.rules.mathequationinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.math.minus
import org.oppia.android.util.math.sort
import org.oppia.android.util.math.toPolynomial
import org.oppia.android.util.math.unaryMinus
import javax.inject.Inject
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicEquation as parseEquation

/**
 * Provider for a classifier that determines whether a math equation expression is mathematically
 * equivalent to the creator-specific equation defined as the input to this interaction.
 *
 * Note that both equations are assumed and parsed as polynomial equations. Furthermore, this
 * classifier allows the two sides of the equations to be rearranged in any way on either side of
 * the '=' sign (but they can't be multiples of each other).
 *
 * See this class's tests for a list of supported cases (both for matching and not matching).
 */
class MathEquationInputIsEquivalentToRuleClassifierProvider @Inject constructor(
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
    val (answerLhs, answerRhs) =
      parsePolynomials(answer, allowedVariables, ALL_ERRORS) ?: return false
    val (inputLhs, inputRhs) =
      parsePolynomials(input, allowedVariables, REQUIRED_ONLY) ?: return false

    val newAnswerLhs = (answerLhs - answerRhs).sort()
    val newInputLhs = (inputLhs - inputRhs).sort()
    val negativeAnswerLhs = (-newAnswerLhs).sort()
    val negativeInputLhs = (-newInputLhs).sort()

    // By subtracting the right-hand sides of both equations with their left-hand sides, the
    // right-hand side becomes zero for both and implicitly equal. If the new simplified left-hand
    // sides are equal then the equations are equivalent regardless of how they were originally
    // arranged. Furthermore, the '-1' check is correct since the order of the equation can flip
    // depending on how it was inputted, and '-1 * 0=0' so the new right-hand side remains
    // unaffected.
    return newAnswerLhs.isApproximatelyEqualTo(newInputLhs) ||
      negativeAnswerLhs.isApproximatelyEqualTo(newInputLhs) ||
      newAnswerLhs.isApproximatelyEqualTo(negativeInputLhs)
  }

  private fun parsePolynomials(
    rawEquation: String,
    allowedVariables: List<String>,
    checkingMode: ErrorCheckingMode
  ): Pair<Polynomial, Polynomial>? {
    return when (val eqResult = parseEquation(rawEquation, allowedVariables, checkingMode)) {
      is MathParsingResult.Success -> {
        val lhsExp = eqResult.result.leftSide.toPolynomial()
        val rhsExp = eqResult.result.rightSide.toPolynomial()
        if (lhsExp != null && rhsExp != null) {
          lhsExp to rhsExp
        } else {
          consoleLogger.w(
            "AlgebraEqEquivalent", "Equation is not a supported polynomial: $rawEquation."
          )
          null
        }
      }
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraEqEquivalent",
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
  }
}
