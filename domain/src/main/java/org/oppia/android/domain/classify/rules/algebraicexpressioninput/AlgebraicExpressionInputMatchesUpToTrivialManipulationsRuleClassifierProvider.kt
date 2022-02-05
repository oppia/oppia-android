package org.oppia.android.domain.classify.rules.algebraicexpressioninput

import javax.inject.Inject
import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.math.toComparableOperation

class AlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider
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
    val answerExpression = parseComparableOperation(answer, allowedVariables) ?: return false
    val inputExpression = parseComparableOperation(input, allowedVariables) ?: return false
    return answerExpression.isApproximatelyEqualTo(inputExpression)
  }

  private fun parseComparableOperation(
    rawExpression: String,
    allowedVariables: List<String>
  ): ComparableOperation? {
    return when (val expResult = parseAlgebraicExpression(rawExpression, allowedVariables)) {
      is MathParsingResult.Success -> expResult.result.toComparableOperation()
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "AlgebraExpTrivialManips",
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
