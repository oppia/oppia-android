package org.oppia.android.testing.math

import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.FractionSubject.Companion.assertThat

// TODO(#4097): Add tests for this class.

/**
 * Truth subject for verifying properties of [Real]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying [Real] proto
 * can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class RealSubject private constructor(
  metadata: FailureMetadata,
  private val actual: Real
) : LiteProtoSubject(metadata, actual) {
  /**
   * Returns a [FractionSubject] to test [Real.getRational]. This will fail if the [Real] pertaining
   * to this subject is not of type rational.
   */
  fun isRationalThat(): FractionSubject {
    verifyTypeToBe(Real.RealTypeCase.RATIONAL)
    return assertThat(actual.rational)
  }

  /**
   * Returns a [DoubleSubject] to test [Real.getIrrational]. This will fail if the [Real] pertaining
   * to this subject is not of type irrational.
   */
  fun isIrrationalThat(): DoubleSubject {
    verifyTypeToBe(Real.RealTypeCase.IRRATIONAL)
    return assertThat(actual.irrational)
  }

  /**
   * Returns a [IntegerSubject] to test [Real.getInteger]. This will fail if the [Real] pertaining
   * to this subject is not of type integer.
   */
  fun isIntegerThat(): IntegerSubject {
    verifyTypeToBe(Real.RealTypeCase.INTEGER)
    return assertThat(actual.integer)
  }

  private fun verifyTypeToBe(expected: Real.RealTypeCase) {
    assertWithMessage("Expected real type to be $expected, not: ${actual.realTypeCase}")
      .that(actual.realTypeCase)
      .isEqualTo(expected)
  }

  companion object {
    /** Returns a new [RealSubject] to verify aspects of the specified [Real] value. */
    fun assertThat(actual: Real): RealSubject = assertAbout(::RealSubject).that(actual)
  }
}
