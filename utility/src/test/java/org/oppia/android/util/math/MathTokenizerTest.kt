package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.math.MathTokenizer.Token.CloseParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.DecimalNumber
import org.oppia.android.util.math.MathTokenizer.Token.Identifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidIdentifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Token.OpenParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.Operator
import org.oppia.android.util.math.MathTokenizer.Token.WholeNumber
import org.robolectric.annotation.LooperMode

/** Tests for [MathTokenizer]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTokenizerTest {
  private val ALLOWED_XYZ_VARIABLES = listOf("x", "y", "z")
  private val ALLOWED_XYZ_WITH_LAMBDA_VARIABLES = ALLOWED_XYZ_VARIABLES + listOf("lambda")
  private val ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES = ALLOWED_XYZ_VARIABLES + listOf("xyz")

  @Test
  fun testTokenize_emptyString_producesNoTokens() {
    val tokens = MathTokenizer.tokenize("", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  fun testTokenize_wholeNumber_oneDigit_producesWholeNumberToken() {
    val tokens = MathTokenizer.tokenize("1", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(1)
  }

  @Test
  fun testTokenize_wholeNumber_multipleDigits_producesWholeNumberToken() {
    val tokens = MathTokenizer.tokenize("913", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(913)
  }

  @Test
  fun testTokenize_wholeNumber_zeroLeadingNumber_producesCorrectBase10WholeNumberToken() {
    val tokens = MathTokenizer.tokenize("0913", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens.first() as WholeNumber).value).isEqualTo(913)
  }

  @Test
  fun testTokenize_decimalNumber_decimalLessThanOne_noZero_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize(".14", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-5).of(0.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalLessThanOne_withZero_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize("0.14", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-5).of(0.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalGreaterThanOne_producesCorrectDecimalNumberToken() {
    val tokens = MathTokenizer.tokenize("3.14", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens.first() as DecimalNumber).value).isWithin(1e-5).of(3.14)
  }

  @Test
  fun testTokenize_decimalNumber_decimalPointOnly_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize(".", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidToken::class.java)
    assertThat((tokens.first() as InvalidToken).token).isEqualTo(".")
  }

  @Test
  fun testTokenize_openParenthesis_producesOpenParenthesisToken() {
    val tokens = MathTokenizer.tokenize("(", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(OpenParenthesis::class.java)
  }

  @Test
  fun testTokenize_closeParenthesis_producesCloseParenthesisToken() {
    val tokens = MathTokenizer.tokenize(")", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(CloseParenthesis::class.java)
  }

  @Test
  fun testTokenize_plusSign_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("+", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('+')
  }

  @Test
  fun testTokenize_minusSign_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("-", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('-')
  }

  @Test
  fun testTokenize_asterisk_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("*", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('*')
  }

  @Test
  fun testTokenize_formalMultiplicationSign_producesAsteriskOperatorToken() {
    val tokens = MathTokenizer.tokenize("×", ALLOWED_XYZ_VARIABLES).toList()

    // The formal math multiplication symbol is translated to the conventional one for simplicity.
    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('*')
  }

  @Test
  fun testTokenize_forwardSlash_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("/", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('/')
  }

  @Test
  fun testTokenize_formalDivisionSign_producesForwardSlashOperatorToken() {
    val tokens = MathTokenizer.tokenize("÷", ALLOWED_XYZ_VARIABLES).toList()

    // The formal math division symbol is translated to the conventional one for simplicity.
    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('/')
  }

  @Test
  fun testTokenize_caret_producesOperatorToken() {
    val tokens = MathTokenizer.tokenize("^", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Operator::class.java)
    assertThat((tokens.first() as Operator).operator).isEqualTo('^')
  }

  @Test
  fun testTokenize_exclamation_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("!", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidToken::class.java)
    assertThat((tokens.first() as InvalidToken).token).isEqualTo("!")
  }

  @Test
  fun testTokenize_validIdentifier_withAllowedIds_producesIdentifierToken() {
    val tokens = MathTokenizer.tokenize("x", allowedIdentifiers = listOf("x")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_validIdentifier_withNoIdentifiersProvided_producesInvalidIdentifierToken() {
    val tokens = MathTokenizer.tokenize("x", allowedIdentifiers = listOf()).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidIdentifier::class.java)
    assertThat((tokens.first() as InvalidIdentifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_withInvalidAllowedIdentifiers_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      MathTokenizer.tokenize("x", allowedIdentifiers = listOf("valid", "invalid!")).toList()
    }

    assertThat(exception).hasMessageThat().contains("contains non-letters: invalid!")
  }

  @Test
  fun testTokenize_withEmptyAllowedIdentifier_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      MathTokenizer.tokenize("x", allowedIdentifiers = listOf("valid", "")).toList()
    }

    assertThat(exception).hasMessageThat().contains("Encountered empty identifier")
  }

  @Test
  fun testTokenize_withAllowedIdentifiers_producesIdentifierToken() {
    val tokens = MathTokenizer.tokenize("z", allowedIdentifiers = listOf("z")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_expressionWithIdLowercase_withAllowedIdentifiersUpper_producesIdToken() {
    val tokens = MathTokenizer.tokenize("z", allowedIdentifiers = listOf("Z")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_expressionWithIdUppercase_withAllowedIdentifiersLower_producesIdToken() {
    val tokens = MathTokenizer.tokenize("Z", allowedIdentifiers = listOf("z")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_expressionWithIdUppercase_withAllowedIdentifiersUpper_producesIdToken() {
    val tokens = MathTokenizer.tokenize("Z", allowedIdentifiers = listOf("Z")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_greekLetterIdentifier_withAllowedIdentifiers_producesIdentifierToken() {
    val tokens = MathTokenizer.tokenize("π", allowedIdentifiers = listOf("π")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("π")
  }

  @Test
  fun testTokenize_greekLetterIdentifier_withAllowedIdentifiersUppercase_producesIdentifierToken() {
    val tokens = MathTokenizer.tokenize("π", allowedIdentifiers = listOf("Π")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("π")
  }

  @Test
  fun testTokenize_multipleIdentifiers_withoutSpaces_producesIdentifierTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("xyz", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_validMultiWordIdentifier_producesSingleIdentifierToken() {
    val tokens = MathTokenizer.tokenize("lambda", allowedIdentifiers = listOf("lambda")).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("lambda")
  }

  @Test
  fun testTokenize_invalidMultiWordIdentifier_missingFromAllowedList_producesInvalidIdToken() {
    val tokens = MathTokenizer.tokenize("xyz", allowedIdentifiers = listOf()).toList()

    // Note that even though 'x' and 'y' are valid single-letter variables, because 'z' is
    // encountered the whole set of letters is considered a single invalid variable.
    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidIdentifier::class.java)
    assertThat((tokens.first() as InvalidIdentifier).name).isEqualTo("xyz")
  }

  @Test
  fun testTokenize_multipleIdentifiers_singleLetter_withSpaces_producesIdTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("x y z", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_multipleIdentifiers_multiLetter_withSpaces_producesIdTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("abc def", listOf("abc", "def")).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("abc")
    assertThat((tokens[1] as Identifier).name).isEqualTo("def")
  }

  @Test
  fun testTokenize_multipleIdentifiers_mixed_withSpaces_producesIdTokensForEachInOrder() {
    val tokens = MathTokenizer.tokenize("a lambda", listOf("a", "lambda")).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("a")
    assertThat((tokens[1] as Identifier).name).isEqualTo("lambda")
  }

  @Test
  fun testTokenize_multiplyTwoVariables_singleLetter_producesCorrectTokens() {
    val tokens = MathTokenizer.tokenize("x*y", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Operator::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Operator).operator).isEqualTo('*')
    assertThat((tokens[2] as Identifier).name).isEqualTo("y")
  }

  @Test
  fun testTokenize_multiplyTwoVariables_multiLetter_producesCorrectTokens() {
    val tokens = MathTokenizer.tokenize("abc*def", listOf("abc", "def")).toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Operator::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("abc")
    assertThat((tokens[1] as Operator).operator).isEqualTo('*')
    assertThat((tokens[2] as Identifier).name).isEqualTo("def")
  }

  @Test
  fun testTokenize_multipleMultiLetterVar_withConsecutiveSingleLetter_producesTokensForAllIds() {
    val tokens = MathTokenizer.tokenize("lambda*xyz", ALLOWED_XYZ_WITH_LAMBDA_VARIABLES).toList()

    // The 'lambda' is a single variable, but the individual 'xyz' are separate variables that each
    // show up separately (which allows interpreting implicit multiplication).
    assertThat(tokens).hasSize(5)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Operator::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[3]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[4]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("lambda")
    assertThat((tokens[1] as Operator).operator).isEqualTo('*')
    assertThat((tokens[2] as Identifier).name).isEqualTo("x")
    assertThat((tokens[3] as Identifier).name).isEqualTo("y")
    assertThat((tokens[4] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_ambiguousMutliSingleLetterIds_producesIdForPreferredMultiLetterId() {
    val tokens = MathTokenizer.tokenize("xyz", ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES).toList()

    // A single identifier should be parsed since the combined variable is encountered, and that
    // takes precedent.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("xyz")
  }

  @Test
  fun testTokenize_ambiguousMutliSingleLetterIds_singleLetterIdsAlone_producesIdTokens() {
    val tokens = MathTokenizer.tokenize("x y z", ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("x")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_ambiguousMutliSingleLetterIds_multiIdSubstring_producesIndividualIdTokens() {
    val tokens = MathTokenizer.tokenize("yz", ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES).toList()

    // Partial substring of 'xyz' produces separate tokens since the whole token isn't present.
    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("y")
    assertThat((tokens[1] as Identifier).name).isEqualTo("z")
  }

  @Test
  fun testTokenize_ambiguousMutliSingleLetterIds_outOfOrder_producesIdTokens() {
    val tokens = MathTokenizer.tokenize("zyx", ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES).toList()

    // Reversing the tokens doesn't match the overall variable, so return separate variables.
    assertThat(tokens).hasSize(3)
    assertThat(tokens[0]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat(tokens[2]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Identifier).name).isEqualTo("z")
    assertThat((tokens[1] as Identifier).name).isEqualTo("y")
    assertThat((tokens[2] as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_ambiguousMutliSingleLetterIds_multiWord_withInvalidLetter_producesInvalidId() {
    val tokens = MathTokenizer.tokenize("xyzw", ALLOWED_XYZ_WITH_COMBINED_XYZ_VARIABLES).toList()

    // A single letter is sufficient to lead to an error ID.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInstanceOf(InvalidIdentifier::class.java)
    assertThat((tokens[0] as InvalidIdentifier).name).isEqualTo("xyzw")
  }

  @Test
  fun testTokenize_identifier_whitespaceBefore_isIgnored() {
    val tokens = MathTokenizer.tokenize(" \r\t\n x", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_identifier_whitespaceAfter_isIgnored() {
    val tokens = MathTokenizer.tokenize("x \r\t\n ", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(Identifier::class.java)
    assertThat((tokens.first() as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_identifierAndOperator_whitespaceBetween_isIgnored() {
    val tokens = MathTokenizer.tokenize("- \r\t\n x", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInstanceOf(Operator::class.java)
    assertThat(tokens[1]).isInstanceOf(Identifier::class.java)
    assertThat((tokens[0] as Operator).operator).isEqualTo('-')
    assertThat((tokens[1] as Identifier).name).isEqualTo("x")
  }

  @Test
  fun testTokenize_digits_withSpaces_producesMultipleWholeNumberTokens() {
    val tokens = MathTokenizer.tokenize("1 23 4", ALLOWED_XYZ_VARIABLES).toList()

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
    val tokens =
      MathTokenizer.tokenize("133 + 3.14 * x / (11 - 15) ^ 2 ^ 3", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(15)

    assertThat(tokens[0]).isInstanceOf(WholeNumber::class.java)
    assertThat((tokens[0] as WholeNumber).value).isEqualTo(133)

    assertThat(tokens[1]).isInstanceOf(Operator::class.java)
    assertThat((tokens[1] as Operator).operator).isEqualTo('+')

    assertThat(tokens[2]).isInstanceOf(DecimalNumber::class.java)
    assertThat((tokens[2] as DecimalNumber).value).isWithin(1e-5).of(3.14)

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

  @Test
  fun testTokenize_integralSymbol_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("∫", ALLOWED_XYZ_VARIABLES).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens.first()).isInstanceOf(InvalidToken::class.java)
    assertThat((tokens.first() as InvalidToken).token).isEqualTo("∫")
  }
}
