package org.oppia.android.testing.math

import com.google.common.truth.ComparableSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.NumberUnitExpression
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.testing.math.FractionSubject.Companion.assertThat

// TODO(#6151): Add tests for this class.

/**
 * Truth subject for verifying properties of [NumberWithUnitsExpression]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
 * [NumberWithUnitsExpression] proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class NumberWithUnitsSubject private constructor(
  metadata: FailureMetadata,
  private val actual: NumberWithUnitsExpression
) : LiteProtoSubject(metadata, actual) {

  /**
   * Verifies that the number type is [NumberWithUnitsExpression.NumberTypeCase.REAL] and returns
   * a
   * [DoubleSubject] to test the real value.
   *
   * This method will fail if the underlying [NumberWithUnitsExpression] does not have a real
   * number type.
   */
  fun hasRealValueThat(): DoubleSubject {
    assertWithMessage(
      "Expected number type to be REAL, not: ${actual.numberTypeCase}"
    )
      .that(actual.numberTypeCase)
      .isEqualTo(NumberWithUnitsExpression.NumberTypeCase.REAL)
    return assertThat(actual.real)
  }

  /**
   * Verifies that the number type is [NumberWithUnitsExpression.NumberTypeCase.FRACTION] and
   * returns a
   * [FractionSubject] to test the fraction value.
   *
   * This method will fail if the underlying [NumberWithUnitsExpression] does not have a fraction
   * number type.
   */
  fun hasFractionValueThat(): FractionSubject {
    assertWithMessage(
      "Expected number type to be FRACTION, not: ${actual.numberTypeCase}"
    )
      .that(actual.numberTypeCase)
      .isEqualTo(NumberWithUnitsExpression.NumberTypeCase.FRACTION)
    return assertThat(actual.fraction)
  }

  /**
   * Returns an [IntegerSubject] to test the total unit count across the expression format.
   */
  fun hasUnitCountThat(): IntegerSubject = assertThat(allUnits().size)

  fun hasPrefixThat(): NumberUnitSubject {
    val units = when (actual.expressionFormatCase) {
      NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_EXPRESSION ->
        actual.prefixValueExpression.prefixUnitsList

      NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_SUFFIX_EXPRESSION ->
        actual.prefixValueSuffixExpression.prefixUnitsList

      else -> emptyList()
    }
    assertWithMessage("Expected to find at least one prefix unit, but found none")
      .that(units)
      .isNotEmpty()
    return NumberUnitSubject.assertThat(units.first())
  }

  fun hasSuffixWithIndexThat(index: Int): NumberUnitSubject {
    val units = when (actual.expressionFormatCase) {
      NumberWithUnitsExpression.ExpressionFormatCase.VALUE_SUFFIX_EXPRESSION ->
        actual.valueSuffixExpression.suffixUnitsList

      NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_SUFFIX_EXPRESSION ->
        actual.prefixValueSuffixExpression.suffixUnitsList

      else -> emptyList()
    }
    assertWithMessage("Expected to find at least one suffix unit, but found none")
      .that(units)
      .isNotEmpty()
    assertWithMessage("Expected suffix unit index to be non-negative, but was: $index")
      .that(index)
      .isAtLeast(0)
    assertWithMessage("Expected suffix unit index $index to be valid for suffix unit count ${units.size}")
      .that(index)
      .isLessThan(units.size)
    return NumberUnitSubject.assertThat(units[index])
  }

  private fun allUnits(): List<NumberUnitExpression> {
    return when (actual.expressionFormatCase) {
      NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_EXPRESSION ->
        actual.prefixValueExpression.prefixUnitsList

      NumberWithUnitsExpression.ExpressionFormatCase.VALUE_SUFFIX_EXPRESSION ->
        actual.valueSuffixExpression.suffixUnitsList

      NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_SUFFIX_EXPRESSION ->
        actual.prefixValueSuffixExpression.prefixUnitsList +
          actual.prefixValueSuffixExpression.suffixUnitsList

      else -> emptyList()
    }
  }

  /**
   * Truth subject for verifying properties of [NumberUnitExpression]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [NumberUnitExpression] proto can be verified through inherited methods.
   *
   * Call [NumberUnitSubject.assertThat] to create the subject.
   */
  class NumberUnitSubject private constructor(
    metadata: FailureMetadata,
    private val actual: NumberUnitExpression
  ) : LiteProtoSubject(metadata, actual) {

    /**
     * Returns a [ComparableSubject] to test [NumberUnitExpression.getUnit].
     */
    fun hasUnitThat(): ComparableSubject<NumberUnitExpression.Unit> = assertThat(actual.unit)

    /**
     * Returns a [ComparableSubject] to test [NumberUnitExpression.getSiPrefix].
     */
    fun hasSiPrefixThat(): ComparableSubject<NumberUnitExpression.SiPrefix> =
      assertThat(actual.siPrefix)

    /**
     * Returns an [IntegerSubject] to test [NumberUnitExpression.getExponent].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the unit.
     */
    fun hasExponentThat(): IntegerSubject = assertThat(actual.exponent)

    companion object {
      /**
       * Returns a new [NumberUnitSubject] to verify aspects of the specified
       * [NumberUnitExpression] value.
       */
      fun assertThat(actual: NumberUnitExpression): NumberUnitSubject =
        assertAbout(::NumberUnitSubject).that(actual)
    }
  }

  companion object {
    /**
     * Returns a new [NumberWithUnitsSubject] to verify aspects of the specified
     * [NumberWithUnitsExpression] value.
     */
    fun assertThat(actual: NumberWithUnitsExpression): NumberWithUnitsSubject =
      assertAbout(::NumberWithUnitsSubject).that(actual)
  }
}
