package org.oppia.android.domain.classify.rules.numericexpressioninput

import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.toComparableOperationList
import javax.inject.Inject

class NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider
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
    writtenTranslationContext: WrittenTranslationContext
  ): Boolean {
    val answerExpression = parseComparableOperationList(answer) ?: return false
    val inputExpression = parseComparableOperationList(input) ?: return false
    return answerExpression == inputExpression
  }

  private fun parseComparableOperationList(rawExpression: String): ComparableOperationList? {
    return when (val expResult = MathExpressionParser.parseNumericExpression(rawExpression)) {
      is MathParsingResult.Success -> expResult.result.toComparableOperationList()
      is MathParsingResult.Failure -> {
        consoleLogger.e(
          "NumericExpTrivialManips",
          "Encountered expression that failed parsing. Expression: $rawExpression." +
            " Failure: ${expResult.error}."
        )
        null
      }
    }
  }
}
