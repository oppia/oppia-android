package org.oppia.android.domain.classify.rules.numberwithunits

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.NumberUnitExpression
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.getAllUnits
import org.oppia.android.domain.util.toRawExpression
import org.oppia.android.util.math.NumberWithUnitsParser
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are equal per the numbers with units
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumberWithUnits/directives/number-with-units-rules.service.ts#L34
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class NumberWithUnitsIsEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS,
      "f",
      this
    )
  }

  // TODO(#209): Determine whether additional sanitation of the input is necessary here.
  override fun matches(
    answer: NumberWithUnits,
    input: NumberWithUnits,
    classificationContext: ClassificationContext
  ): Boolean {
    val answerExpression = parseExpression(answer.toRawExpression()) ?: return false
    val inputExpression = parseExpression(input.toRawExpression()) ?: return false

    // The number types must match.
    if (answerExpression.numberTypeCase != inputExpression.numberTypeCase) {
      return false
    }
    // Units must match, but in different orders is fine. But duplicates must be preserved.
    if (answerExpression.getSortedUnits() != inputExpression.getSortedUnits()) {
      return false
    }
    // Otherwise, verify the value itself matches.
    return when (answerExpression.numberTypeCase) {
      NumberWithUnitsExpression.NumberTypeCase.REAL ->
        inputExpression.real.isApproximatelyEqualTo(answerExpression.real)
      NumberWithUnitsExpression.NumberTypeCase.FRACTION ->
        inputExpression.fraction == answerExpression.fraction
      else -> false // Unknown type never matches.
    }
  }

  private fun parseExpression(rawExpression: String): NumberWithUnitsExpression? {
    return when (val result = NumberWithUnitsParser.parseNumberWithUnits(rawExpression)) {
      is NumberWithUnitsParsingResult.Success -> result.result
      is NumberWithUnitsParsingResult.Failure -> null
    }
  }

  private fun NumberWithUnitsExpression.getSortedUnits(): List<NumberUnitExpression> =
    getAllUnits().sortedWith(UNIT_COMPARATOR)

  private companion object {
    private val UNIT_COMPARATOR =
      compareBy<NumberUnitExpression>({ it.unitValue }, { it.siPrefixValue }, { it.exponent })
  }
}
