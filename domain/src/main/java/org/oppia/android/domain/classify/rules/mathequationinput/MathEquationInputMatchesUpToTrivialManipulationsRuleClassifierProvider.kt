package org.oppia.android.domain.classify.rules.mathequationinput

import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicEquation
import org.oppia.android.util.math.toComparableOperation
import javax.inject.Inject
import org.oppia.android.util.math.isApproximatelyEqualTo

class MathEquationInputMatchesUpToTrivialManipulationsRuleClassifierProvider
@Inject constructor(
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
    val (answerLhs, answerRhs) = parseComparableLists(answer, allowedVariables) ?: return false
    val (inputLhs, inputRhs) = parseComparableLists(input, allowedVariables) ?: return false

    // Sides must match (reordering around the '=' is not allowed by this classifier).
    return answerLhs.isApproximatelyEqualTo(inputLhs) && answerRhs.isApproximatelyEqualTo(inputRhs)
  }

  private fun parseComparableLists(
    rawEquation: String,
    allowedVariables: List<String>
  ): Pair<ComparableOperation, ComparableOperation>? {
    return when (val eqResult = parseAlgebraicEquation(rawEquation, allowedVariables)) {
      is MathParsingResult.Success -> {
        val lhsExp = eqResult.result.leftSide
        val rhsExp = eqResult.result.rightSide
        lhsExp.toComparableOperation() to rhsExp.toComparableOperation()
      }
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraEqTrivialManips",
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
