package org.oppia.android.domain.classify.rules.numericexpressioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.toPolynomial
import javax.inject.Inject
import org.oppia.android.util.math.approximatelyEquals

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
    writtenTranslationContext: WrittenTranslationContext
  ): Boolean {
    val answerExpression = parsePolynomial(answer) ?: return false
    val inputExpression = parsePolynomial(input) ?: return false
    return answerExpression.approximatelyEquals(inputExpression)
  }

  private fun parsePolynomial(rawExpression: String): Polynomial? {
    return when (val expResult = MathExpressionParser.parseNumericExpression(rawExpression)) {
      is MathParsingResult.Success -> {
        expResult.result.toPolynomial().also {
          if (it == null) {
            consoleLogger.w(
              "NumericExpEquivalent", "Expression is not a supported polynomial: $rawExpression."
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
