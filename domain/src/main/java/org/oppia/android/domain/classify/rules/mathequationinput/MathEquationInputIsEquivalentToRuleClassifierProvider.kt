package org.oppia.android.domain.classify.rules.mathequationinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicEquation
import org.oppia.android.util.math.toPolynomial
import javax.inject.Inject
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.math.minus
import org.oppia.android.util.math.sort
import org.oppia.android.util.math.unaryMinus

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
    val (answerLhs, answerRhs) = parsePolynomials(answer, allowedVariables) ?: return false
    val (inputLhs, inputRhs) = parsePolynomials(input, allowedVariables) ?: return false

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
    return newAnswerLhs.isApproximatelyEqualTo(newInputLhs)
      || negativeAnswerLhs.isApproximatelyEqualTo(newInputLhs)
      || newAnswerLhs.isApproximatelyEqualTo(negativeInputLhs)
  }

  private fun parsePolynomials(
    rawEquation: String,
    allowedVariables: List<String>
  ): Pair<Polynomial, Polynomial>? {
    return when (val eqResult = parseAlgebraicEquation(rawEquation, allowedVariables)) {
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
