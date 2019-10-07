package org.oppia.domain.classify.rules.numberwithunits

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberWithUnits
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import org.oppia.domain.util.approximatelyEquals
import java.lang.IllegalArgumentException
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are effectively equal per the number with
 * units interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumberWithUnits/directives/number-with-units-rules.service.ts#L48
 */
internal class NumberWithUnitsIsEquivalentToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS, "f", this)
  }

  // TODO(#209): Determine whether additional normalization of the input is necessary here.
  override fun matches(answer: NumberWithUnits, input: NumberWithUnits): Boolean {
    // Units must match, but in different orders is fine.
    if (answer.unitsList.toSet() != input.unitsList.toSet()) {
      return false
    }

    // Verify the float version of the value for approximate comparison.
    return extractRealValue(input).approximatelyEquals(extractRealValue(answer))
  }

  private fun extractRealValue(number: NumberWithUnits): Float {
    return when (number.numberTypeCase) {
      NumberWithUnits.NumberTypeCase.REAL -> number.real
      NumberWithUnits.NumberTypeCase.FRACTION -> convertFractionToReal(number.fraction)
      else -> throw IllegalArgumentException("Invalid number type: ${number.numberTypeCase.name}")
    }
  }

  // See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L73
  private fun convertFractionToReal(fraction: Fraction): Float {
    val totalParts = ((fraction.wholeNumber * fraction.denominator) + fraction.numerator).toFloat()
    val floatVal = totalParts.toFloat() / fraction.denominator.toFloat()
    return if (fraction.isNegative) -floatVal else floatVal
  }
}
