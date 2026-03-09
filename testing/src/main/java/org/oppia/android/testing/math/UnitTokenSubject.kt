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

  fun isDivideSymbol() {
    actual.asVerifiedType<Token.DivideSymbol>()
  }

  fun isDollarPrefixUnit() {
    actual.asVerifiedType<Token.DollarPrefixUnit>()
  }

  fun isDollarSuffixUnit() {
    actual.asVerifiedType<Token.DollarSuffixUnit>()
  }

  fun isCentSuffixUnit() {
    actual.asVerifiedType<Token.CentSuffixUnit>()
  }

  fun isRupeePrefixUnit() {
    actual.asVerifiedType<Token.RupeePrefixUnit>()
  }

  fun isRupeeSuffixUnit() {
    actual.asVerifiedType<Token.RupeeSuffixUnit>()
  }

  fun isPaisaSuffixUnit() {
    actual.asVerifiedType<Token.PaisaSuffixUnit>()
  }

  fun isMeterUnit() {
    actual.asVerifiedType<Token.MeterUnit>()
  }

  fun isInchUnit() {
    actual.asVerifiedType<Token.InchUnit>()
  }

  fun isFootUnit() {
    actual.asVerifiedType<Token.FootUnit>()
  }

  fun isYardUnit() {
    actual.asVerifiedType<Token.YardUnit>()
  }

  fun isGramUnit() {
    actual.asVerifiedType<Token.GramUnit>()
  }

  fun isGrainUnit() {
    actual.asVerifiedType<Token.GrainUnit>()
  }

  fun isOunceUnit() {
    actual.asVerifiedType<Token.OunceUnit>()
  }

  fun isSquareMeterUnit() {
    actual.asVerifiedType<Token.SquareMeterUnit>()
  }

  fun isSquareInchUnit() {
    actual.asVerifiedType<Token.SquareInchUnit>()
  }

  fun isSquareFootUnit() {
    actual.asVerifiedType<Token.SquareFootUnit>()
  }

  fun isSquareYardUnit() {
    actual.asVerifiedType<Token.SquareYardUnit>()
  }

  fun isCubicMeterUnit() {
    actual.asVerifiedType<Token.CubicMeterUnit>()
  }

  fun isLiterUnit() {
    actual.asVerifiedType<Token.LiterUnit>()
  }

  fun isCubicCentimeterUnit() {
    actual.asVerifiedType<Token.CcUnit>()
  }

  fun isCubicInchUnit() {
    actual.asVerifiedType<Token.CubicInchUnit>()
  }

  fun isCubicFootUnit() {
    actual.asVerifiedType<Token.CubicFootUnit>()
  }

  fun isCubicYardUnit() {
    actual.asVerifiedType<Token.CubicYardUnit>()
  }

  fun isKelvinUnit() {
    actual.asVerifiedType<Token.KelvinUnit>()
  }

  fun isCelsiusUnit() {
    actual.asVerifiedType<Token.CelsiusUnit>()
  }

  fun isRadianUnit() {
    actual.asVerifiedType<Token.RadianUnit>()
  }

  fun isDegreeUnit() {
    actual.asVerifiedType<Token.DegreeUnit>()
  }

  fun isSecondUnit() {
    actual.asVerifiedType<Token.SecondUnit>()
  }

  fun isMinuteUnit() {
    actual.asVerifiedType<Token.MinuteUnit>()
  }

  fun isHourUnit() {
    actual.asVerifiedType<Token.HourUnit>()
  }

  fun isInvalidToken() {
    actual.asVerifiedType<Token.InvalidToken>()
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
