package org.oppia.android.testing.math

import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.oppia.android.util.math.NumberWithUnitsTokenizer.Companion.Token

class UnitTokenSubject(
  metadata: FailureMetadata,
  private val actual: Token
) : Subject(metadata, actual) {
  fun isPositiveIntegerWhoseValue(): IntegerSubject {
    return Truth.assertThat(actual.asVerifiedType<Token.PositiveInteger>().parsedValue)
  }

  fun isPositiveRealNumberWhoseValue(): DoubleSubject {
    return Truth.assertThat(actual.asVerifiedType<Token.PositiveRealNumber>().parsedValue)
  }

  fun isMinusSymbol() {
    actual.asVerifiedType<Token.MinusSymbol>()
  }

  companion object {
    /** Returns a new [UnitTokenSubject] to verify aspects of the specified [Token] value. */
    fun assertThat(actual: Token): UnitTokenSubject =
      Truth.assertAbout(::UnitTokenSubject).that(actual)

    private inline fun <reified T : Token> Token.asVerifiedType(): T {
      assertThat(this).isInstanceOf(T::class.java)
      return this as T
    }
  }
}
