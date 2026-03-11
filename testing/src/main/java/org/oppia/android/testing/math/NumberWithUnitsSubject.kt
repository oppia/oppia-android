package org.oppia.android.testing.math

import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.IterableSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.testing.math.FractionSubject.Companion.assertThat
import org.oppia.android.testing.math.NumberWithUnitsSubject.Companion.assertThat

// TODO: Add tests for this class.

/**
 * Truth subject for verifying properties of [NumberWithUnits]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
 * [NumberWithUnits] proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class NumberWithUnitsSubject private constructor(
  metadata: FailureMetadata,
  private val actual: NumberWithUnits
) : LiteProtoSubject(metadata, actual) {

  /**
   * Verifies that the number type is [NumberWithUnits.NumberTypeCase.REAL] and returns a
   * [DoubleSubject] to test the real value.
   *
   * This method will fail if the underlying [NumberWithUnits] does not have a real number type.
   */
  fun hasRealValueThat(): DoubleSubject {
    assertWithMessage("Expected number type to be REAL, not: ${actual.numberTypeCase}")
      .that(actual.numberTypeCase)
      .isEqualTo(NumberWithUnits.NumberTypeCase.REAL)
    return assertThat(actual.real)
  }

  /**
   * Verifies that the number type is [NumberWithUnits.NumberTypeCase.FRACTION] and returns a
   * [FractionSubject] to test the fraction value.
   *
   * This method will fail if the underlying [NumberWithUnits] does not have a fraction number type.
   */
  fun hasFractionValueThat(): FractionSubject {
    assertWithMessage("Expected number type to be FRACTION, not: ${actual.numberTypeCase}")
      .that(actual.numberTypeCase)
      .isEqualTo(NumberWithUnits.NumberTypeCase.FRACTION)
    return assertThat(actual.fraction)
  }

  /**
   * Returns an [IntegerSubject] to test [NumberWithUnits.getUnitCount].
   *
   * This method never fails since the underlying property defaults to 0 if there are no units
   * defined.
   */
  fun hasUnitCountThat(): IntegerSubject = assertThat(actual.unitCount)

  /**
   * Returns an [IterableSubject] to test the unit names of [NumberWithUnits.getUnitList].
   *
   * This is useful for verifying which units are present without relying on indices. For example:
   * ```
   * hasUnitNamesThat().containsExactly("meter", "second")
   * ```
   */
  fun hasUnitNamesThat(): IterableSubject = assertThat(actual.unitList.map { it.unit })

  /**
   * Returns a [NumberUnitSubject] for the [NumberUnit] whose [NumberUnit.getUnit] matches the
   * specified [unitName].
   *
   * This provides a more robust way to verify unit properties without relying on index-based
   * access. For example:
   * ```
   * hasUnit("meter").hasExponentThat().isEqualTo(1)
   * hasUnit("second").hasExponentThat().isEqualTo(-2)
   * ```
   *
   * This method will fail if no unit with the given name is found in the [NumberWithUnits].
   */
  fun hasUnit(unitName: String): NumberUnitSubject {
    val matchingUnit = actual.unitList.firstOrNull { it.unit == unitName }
    assertWithMessage(
      "Expected to find unit with name '$unitName', but only found: " +
        actual.unitList.map { it.unit }
    ).that(matchingUnit).isNotNull()
    return NumberUnitSubject.assertThat(matchingUnit!!)
  }

  /**
   * Returns a [NumberUnitSubject] to test the [NumberUnit] at the specified [index] in
   * [NumberWithUnits.getUnitList].
   *
   * This method throws if the index doesn't correspond to a valid unit. Callers should first verify
   * the unit count using [hasUnitCountThat].
   *
   * Prefer [hasUnit] for name-based lookup when the order of units is not significant.
   */
  fun unit(index: Int): NumberUnitSubject {
    assertWithMessage("Expected unit index $index to be valid for unit count ${actual.unitCount}")
      .that(index)
      .isLessThan(actual.unitCount)
    return NumberUnitSubject.assertThat(actual.unitList[index])
  }

  /**
   * Truth subject for verifying properties of [NumberUnit]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [NumberUnit] proto can be verified through inherited methods.
   *
   * Call [NumberUnitSubject.assertThat] to create the subject.
   */
  class NumberUnitSubject private constructor(
    metadata: FailureMetadata,
    private val actual: NumberUnit
  ) : LiteProtoSubject(metadata, actual) {

    /**
     * Returns a [StringSubject] to test [NumberUnit.getUnit].
     *
     * This method never fails since the underlying property defaults to an empty string if it's not
     * defined in the unit.
     */
    fun hasUnitThat(): StringSubject = assertThat(actual.unit)

    /**
     * Returns an [IntegerSubject] to test [NumberUnit.getExponent].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the unit.
     */
    fun hasExponentThat(): IntegerSubject = assertThat(actual.exponent)

    companion object {
      /**
       * Returns a new [NumberUnitSubject] to verify aspects of the specified [NumberUnit] value.
       */
      fun assertThat(actual: NumberUnit): NumberUnitSubject =
        assertAbout(::NumberUnitSubject).that(actual)
    }
  }

  companion object {
    /**
     * Returns a new [NumberWithUnitsSubject] to verify aspects of the specified [NumberWithUnits]
     * value.
     */
    fun assertThat(actual: NumberWithUnits): NumberWithUnitsSubject =
      assertAbout(::NumberWithUnitsSubject).that(actual)
  }
}
