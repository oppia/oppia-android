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

class RealSubject(
  metadata: FailureMetadata,
  private val actual: Real
) : LiteProtoSubject(metadata, actual) {
  fun isRationalThat(): FractionSubject {
    verifyTypeToBe(Real.RealTypeCase.RATIONAL)
    return assertThat(actual.rational)
  }

  fun isIrrationalThat(): DoubleSubject {
    verifyTypeToBe(Real.RealTypeCase.IRRATIONAL)
    return assertThat(actual.irrational)
  }

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
    fun assertThat(actual: Real): RealSubject = assertAbout(::RealSubject).that(actual)
  }
}
