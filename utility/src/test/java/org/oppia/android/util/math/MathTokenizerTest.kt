package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.math.MathTokenizer.Token.CloseParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.DecimalNumber
import org.oppia.android.util.math.MathTokenizer.Token.Identifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Token.OpenParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.Operator
import org.oppia.android.util.math.MathTokenizer.Token.WholeNumber
import org.robolectric.annotation.LooperMode

/** Tests for [MathTokenizer]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTokenizerTest {
  @Test
  fun testTokenize_emptyString_producesNoTokens() {
    val tokens = MathTokenizer.tokenize("").toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  fun testTokenize_wholeNumber_oneDigit_producesWholeNumberToken() {
    val tokens = MathTokenizer.tokenize("1").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(1)
  }

  @Test
  fun testTokenize_wholeNumber_multipleDigits_producesWholeNumberToken() {
    val tokens = MathTokenizer.tokenize("913").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(913)
  }

  @Test
  fun testTokenize_wholeNumber_zeroLeadingNumber_producesCorrectBase10WholeNumberToken() {
    val tokens = MathTokenizer.tokenize("0913").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(913)
  }

  @Test
  fun testTokenize_decimalNumber_decimalLessThanOne_noZero_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize(".14").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-3).of(0.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalLessThanOne_withZero_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize("0.14").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-3).of(0.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalGreaterThanOne_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize("3.14").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-3).of(3.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalPointOnly_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize(".").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidToken::class.java)
    assertThat((tokens.first() as InvalidToken).token).isEqualTo(".")
  }

  @Test
  fun testTokenize_openParenthesis_producesOpenParenthesisToken() {
    val tokens = MathTokenizer.tokenize("(").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(OpenParenthesis::class.java)
  }

  @Test
  fun testTokenize_closeParenthesis_producesCloseParenthesisToken() {
    val tokens = MathTokenizer.tokenize(")").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(CloseParenthesis::class.java)
  }

  @Test
  fun testTokenize_plusSign_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("+").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('+')
  }

  @Test
  fun testTokenize_minusSign_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("-").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('-')
  }

  @Test
  fun testTokenize_asterisk_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("*").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('*')
  }

  @Test
  fun testTokenize_forwardSlash_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("/").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('/')
  }

  @Test
  fun testTokenize_caret_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("^").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('^')
  }

  @Test
  fun testTokenize_exclamation_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("!").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidToken::class.java)
    assertThat((tokens.first() as InvalidToken).token).isEqualTo("!")
  }

  @Test
  fun testTokenize_identifier_producesIdentifierToken() {
    val tokens = MathTokenizer.tokenize("x").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_multipleIdentifiers_withoutSpaces_producesIdentifierTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("xyz").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_multipleIdentifiers_withSpaces_producesIdentifierTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("x y z").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_identifier_whitespaceBefore_isIgnored() {
    val tokens = MathTokenizer.tokenize(" \r\t\n x").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_identifier_whitespaceAfter_isIgnored() {
    val tokens = MathTokenizer.tokenize("x \r\t\n ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_identifierAndOperator_whitespaceBetween_isIgnored() {
    val tokens = MathTokenizer.tokenize("- \r\t\n x").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInstanceOf(Operator::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Operator).operator).isEqualTo('-')
    assertThat((tokens[1] as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_digits_withSpaces_producesMultipleWholeNumberTokens() {
    val tokens = MathTokenizer.tokenize("1 23 4").toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(WholeNumber::class.java)
    assertThat(tokens[1]).isInstanceOf(WholeNumber::class.java)
    assertThat(tokens[2]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[0] as WholeNumber).value).isEqualTo(1)
    assertThat((tokens[1] as WholeNumber).value).isEqualTo(23)
    assertThat((tokens[2] as WholeNumber).value).isEqualTo(4)
  }

  @Test
  fun testTokenize_complexExpressionWithAllTokenTypes_tokenizesEverythingInOrder() {
    val tokens = MathTokenizer.tokenize("133 + 3.14 * x / (11 - 15) ^ 2 ^ 3").toList()

    assertThat(tokens).hasSize(15)

    assertThat(tokens[0]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[0] as WholeNumber).value).isEqualTo(133)

    assertThat(tokens[1]).isInstanceOf(Operator::class.java)
    assertThat((tokens[1] as Operator).operator).isEqualTo('+')

    assertThat(tokens[2]).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens[2] as DecimalNumber).value).isWithin(1e-3).of(3.14)

    assertThat(tokens[3]).isInstanceOf(Operator::class.java)
    assertThat((tokens[3] as Operator).operator).isEqualTo('*')

    assertThat(tokens[4]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[4] as Identifier).name).isEqualTo("x")

    assertThat(tokens[5]).isInstanceOf(Operator::class.java)
    assertThat((tokens[5] as Operator).operator).isEqualTo('/')

    assertThat(tokens[6]).isInstanceOf(OpenParenthesis::class.java)

    assertThat(tokens[7]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[7] as WholeNumber).value).isEqualTo(11)

    assertThat(tokens[8]).isInstanceOf(Operator::class.java)
    assertThat((tokens[8] as Operator).operator).isEqualTo('-')

    assertThat(tokens[9]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[9] as WholeNumber).value).isEqualTo(15)

    assertThat(tokens[10]).isInstanceOf(CloseParenthesis::class.java)

    assertThat(tokens[11]).isInstanceOf(Operator::class.java)
    assertThat((tokens[11] as Operator).operator).isEqualTo('^')

    assertThat(tokens[12]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[12] as WholeNumber).value).isEqualTo(2)

    assertThat(tokens[13]).isInstanceOf(Operator::class.java)
    assertThat((tokens[13] as Operator).operator).isEqualTo('^')

    assertThat(tokens[14]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[14] as WholeNumber).value).isEqualTo(3)
  }
}
