package org.oppia.android.testing.math

import com.google.common.truth.BooleanSubject
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.Fraction
import org.oppia.android.util.math.toDouble

/**
 * Truth subject for verifying properties of [Fraction]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying [Fraction]
 * proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class FractionSubject private constructor(
  metadata: FailureMetadata,
  private val actual: Fraction
) : LiteProtoSubject(metadata, actual) {
  /**
   * Returns a [BooleanSubject] to test [Fraction.getIsNegative]. This method never fails since the
   * underlying property defaults to false if it's not defined in the fraction.
   */
  fun hasNegativePropertyThat(): BooleanSubject = assertThat(actual.isNegative)

  /**
   * Returns an [IntegerSubject] to test [Fraction.getWholeNumber]. This method never fails since
   * the underlying property defaults to 0 if it's not defined in the fraction.
   */
  fun hasWholeNumberThat(): IntegerSubject = assertThat(actual.wholeNumber)

  /**
   * Returns an [IntegerSubject] to test [Fraction.getNumerator]. This method never fails since the
   * underlying property defaults to 0 if it's not defined in the fraction.
   */
  fun hasNumeratorThat(): IntegerSubject = assertThat(actual.numerator)

  /**
   * Returns an [IntegerSubject] to test [Fraction.getDenominator]. This method never fails since
   * the underlying property defaults to 0 if it's not defined in the fraction.
   */
  fun hasDenominatorThat(): IntegerSubject = assertThat(actual.denominator)

  /**
   * Returns a [DoubleSubject] to test the converted double version of the fraction being
   * represented by this subject.
   */
  fun evaluatesToDoubleThat(): DoubleSubject = assertThat(actual.toDouble())

  companion object {
    /** Returns a new [FractionSubject] to verify aspects of the specified [Fraction] value. */
    fun assertThat(actual: Fraction): FractionSubject = assertAbout(::FractionSubject).that(actual)
  }
}
