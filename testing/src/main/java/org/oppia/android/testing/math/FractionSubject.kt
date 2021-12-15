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

class FractionSubject(
  metadata: FailureMetadata,
  private val actual: Fraction
) : LiteProtoSubject(metadata, actual) {
  fun hasNegativePropertyThat(): BooleanSubject = assertThat(actual.isNegative)

  fun hasWholeNumberThat(): IntegerSubject = assertThat(actual.wholeNumber)

  fun hasNumeratorThat(): IntegerSubject = assertThat(actual.numerator)

  fun hasDenominatorThat(): IntegerSubject = assertThat(actual.denominator)

  fun evaluatesToRealThat(): DoubleSubject = assertThat(actual.toDouble())

  companion object {
    fun assertThat(actual: Fraction): FractionSubject = assertAbout(::FractionSubject).that(actual)
  }
}
