package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.math.MathTokenizer.Companion.Token
import org.robolectric.annotation.LooperMode

/** Tests for [MathTokenizer]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTokenizerTest {
  @Test
  fun testLotsOfCases() {
    // TODO: split this up
    // testTokenize_emptyString_producesNoTokens
    val tokens1 = MathTokenizer.tokenize("    ").toList()
    assertThat(tokens1).isEmpty()

    val tokens2 = MathTokenizer.tokenize("   2 ").toList()
    assertThat(tokens2).hasSize(1)
    assertThat(tokens2.first()).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens3 = MathTokenizer.tokenize("   2.5 ").toList()
    assertThat(tokens3).hasSize(1)
    assertThat(tokens3.first()).isPositiveRealNumberWhoseValue().isWithin(1e-5).of(2.5)

    val tokens4 = MathTokenizer.tokenize("   x ").toList()
    assertThat(tokens4).hasSize(1)
    assertThat(tokens4.first()).isVariableWhoseName().isEqualTo("x")

    val tokens5 = MathTokenizer.tokenize(" z  x ").toList()
    assertThat(tokens5).hasSize(2)
    assertThat(tokens5[0]).isVariableWhoseName().isEqualTo("z")
    assertThat(tokens5[1]).isVariableWhoseName().isEqualTo("x")

    val tokens6 = MathTokenizer.tokenize("2^3^2").toList()
    assertThat(tokens6).hasSize(5)
    assertThat(tokens6[0]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens6[1]).isExponentiationSymbol()
    assertThat(tokens6[2]).isPositiveIntegerWhoseValue().isEqualTo(3)
    assertThat(tokens6[3]).isExponentiationSymbol()
    assertThat(tokens6[4]).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens7 = MathTokenizer.tokenize("sqrt(2)").toList()
    assertThat(tokens7).hasSize(4)
    assertThat(tokens7[0]).isFunctionWhoseName().isEqualTo("sqrt")
    assertThat(tokens7[1]).isLeftParenthesisSymbol()
    assertThat(tokens7[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens7[3]).isRightParenthesisSymbol()

    val tokens8 = MathTokenizer.tokenize("sqr(2)").toList()
    assertThat(tokens8).hasSize(4)
    assertThat(tokens8[0]).isIncompleteFunctionName()
    assertThat(tokens8[1]).isLeftParenthesisSymbol()
    assertThat(tokens8[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens8[3]).isRightParenthesisSymbol()

    val tokens9 = MathTokenizer.tokenize("xyz(2)").toList()
    assertThat(tokens9).hasSize(6)
    assertThat(tokens9[0]).isVariableWhoseName().isEqualTo("x")
    assertThat(tokens9[1]).isVariableWhoseName().isEqualTo("y")
    assertThat(tokens9[2]).isVariableWhoseName().isEqualTo("z")
    assertThat(tokens9[3]).isLeftParenthesisSymbol()
    assertThat(tokens9[4]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens9[5]).isRightParenthesisSymbol()

    val tokens10 = MathTokenizer.tokenize("732").toList()
    assertThat(tokens10).hasSize(1)
    assertThat(tokens10.first()).isPositiveIntegerWhoseValue().isEqualTo(732)

    val tokens11 = MathTokenizer.tokenize("73 2").toList()
    assertThat(tokens11).hasSize(2)
    assertThat(tokens11[0]).isPositiveIntegerWhoseValue().isEqualTo(73)
    assertThat(tokens11[1]).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens12 = MathTokenizer.tokenize("1*2-3+4^7-8/3*2+7").toList()
    assertThat(tokens12).hasSize(17)
    assertThat(tokens12[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens12[1]).isMultiplySymbol()
    assertThat(tokens12[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens12[3]).isMinusSymbol()
    assertThat(tokens12[4]).isPositiveIntegerWhoseValue().isEqualTo(3)
    assertThat(tokens12[5]).isPlusSymbol()
    assertThat(tokens12[6]).isPositiveIntegerWhoseValue().isEqualTo(4)
    assertThat(tokens12[7]).isExponentiationSymbol()
    assertThat(tokens12[8]).isPositiveIntegerWhoseValue().isEqualTo(7)
    assertThat(tokens12[9]).isMinusSymbol()
    assertThat(tokens12[10]).isPositiveIntegerWhoseValue().isEqualTo(8)
    assertThat(tokens12[11]).isDivideSymbol()
    assertThat(tokens12[12]).isPositiveIntegerWhoseValue().isEqualTo(3)
    assertThat(tokens12[13]).isMultiplySymbol()
    assertThat(tokens12[14]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens12[15]).isPlusSymbol()
    assertThat(tokens12[16]).isPositiveIntegerWhoseValue().isEqualTo(7)

    val tokens13 = MathTokenizer.tokenize("x = √2 × 7 ÷ 4").toList()
    assertThat(tokens13).hasSize(8)
    assertThat(tokens13[0]).isVariableWhoseName().isEqualTo("x")
    assertThat(tokens13[1]).isEqualsSymbol()
    assertThat(tokens13[2]).isSquareRootSymbol()
    assertThat(tokens13[3]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens13[4]).isMultiplySymbol()
    assertThat(tokens13[5]).isPositiveIntegerWhoseValue().isEqualTo(7)
    assertThat(tokens13[6]).isDivideSymbol()
    assertThat(tokens13[7]).isPositiveIntegerWhoseValue().isEqualTo(4)
  }

  private class TokenSubject<in T : Token>(
    metadata: FailureMetadata,
    private val actual: T
  ) : Subject(metadata, actual) {
    fun isPositiveIntegerWhoseValue(): IntegerSubject {
      return assertThat(actual.asVerifiedType<Token.PositiveInteger>().parsedValue)
    }

    fun isPositiveRealNumberWhoseValue(): DoubleSubject {
      return assertThat(actual.asVerifiedType<Token.PositiveRealNumber>().parsedValue)
    }

    fun isVariableWhoseName(): StringSubject {
      return assertThat(actual.asVerifiedType<Token.VariableName>().parsedName)
    }

    fun isFunctionWhoseName(): StringSubject {
      return assertThat(actual.asVerifiedType<Token.FunctionName>().parsedName)
    }

    fun isMinusSymbol() {
      actual.asVerifiedType<Token.MinusSymbol>()
    }

    fun isSquareRootSymbol() {
      actual.asVerifiedType<Token.SquareRootSymbol>()
    }

    fun isPlusSymbol() {
      actual.asVerifiedType<Token.PlusSymbol>()
    }

    fun isMultiplySymbol() {
      actual.asVerifiedType<Token.MultiplySymbol>()
    }

    fun isDivideSymbol() {
      actual.asVerifiedType<Token.DivideSymbol>()
    }

    fun isExponentiationSymbol() {
      actual.asVerifiedType<Token.ExponentiationSymbol>()
    }

    fun isEqualsSymbol() {
      actual.asVerifiedType<Token.EqualsSymbol>()
    }

    fun isLeftParenthesisSymbol() {
      actual.asVerifiedType<Token.LeftParenthesisSymbol>()
    }

    fun isRightParenthesisSymbol() {
      actual.asVerifiedType<Token.RightParenthesisSymbol>()
    }

    fun isInvalidToken() {
      actual.asVerifiedType<Token.InvalidToken>()
    }

    fun isIncompleteFunctionName() {
      actual.asVerifiedType<Token.IncompleteFunctionName>()
    }

    private companion object {
      private inline fun <reified T : Token> Token.asVerifiedType(): T {
        assertThat(this).isInstanceOf(T::class.java)
        return this as T
      }
    }
  }

  private companion object {
    private fun <T : Token> assertThat(actual: T): TokenSubject<T> =
      assertAbout(createTokenSubjectFactory()).that(actual)

    private fun <T : Token> createTokenSubjectFactory() =
      Subject.Factory<TokenSubject<T>, T>(::TokenSubject)
  }
}
