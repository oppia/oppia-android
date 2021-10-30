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
import org.oppia.android.util.math.MathTokenizer2.Companion.Token
import org.robolectric.annotation.LooperMode

/** Tests for [MathTokenizer]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTokenizerTest {
  @Test
  fun testLotsOfCases() {
    // TODO: split this up
    val tokens1 = MathTokenizer2.tokenize("    ").toList()
    assertThat(tokens1).isEmpty()

    val tokens2 = MathTokenizer2.tokenize("   2 ").toList()
    assertThat(tokens2).hasSize(1)
    assertThat(tokens2.first()).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens3 = MathTokenizer2.tokenize("   2.5 ").toList()
    assertThat(tokens3).hasSize(1)
    assertThat(tokens3.first()).isPositiveRealNumberWhoseValue().isWithin(1e-5).of(2.5)

    val tokens4 = MathTokenizer2.tokenize("   x ").toList()
    assertThat(tokens4).hasSize(1)
    assertThat(tokens4.first()).isVariableWhoseName().isEqualTo("x")

    val tokens5 = MathTokenizer2.tokenize(" z  x ").toList()
    assertThat(tokens5).hasSize(2)
    assertThat(tokens5[0]).isVariableWhoseName().isEqualTo("z")
    assertThat(tokens5[1]).isVariableWhoseName().isEqualTo("x")

    val tokens6 = MathTokenizer2.tokenize("2^3^2").toList()
    assertThat(tokens6).hasSize(5)
    assertThat(tokens6[0]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens6[1]).isExponentiationSymbol()
    assertThat(tokens6[2]).isPositiveIntegerWhoseValue().isEqualTo(3)
    assertThat(tokens6[3]).isExponentiationSymbol()
    assertThat(tokens6[4]).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens7 = MathTokenizer2.tokenize("sqrt(2)").toList()
    assertThat(tokens7).hasSize(4)
    assertThat(tokens7[0]).isFunctionWhoseName().isEqualTo("sqrt")
    assertThat(tokens7[1]).isLeftParenthesisSymbol()
    assertThat(tokens7[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens7[3]).isRightParenthesisSymbol()

    val tokens8 = MathTokenizer2.tokenize("sqr(2)").toList()
    assertThat(tokens8).hasSize(4)
    assertThat(tokens8[0]).isInvalidToken()
    assertThat(tokens8[1]).isLeftParenthesisSymbol()
    assertThat(tokens8[2]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens8[3]).isRightParenthesisSymbol()

    val tokens9 = MathTokenizer2.tokenize("xyz(2)").toList()
    assertThat(tokens9).hasSize(6)
    assertThat(tokens9[0]).isVariableWhoseName().isEqualTo("x")
    assertThat(tokens9[1]).isVariableWhoseName().isEqualTo("y")
    assertThat(tokens9[2]).isVariableWhoseName().isEqualTo("z")
    assertThat(tokens9[3]).isLeftParenthesisSymbol()
    assertThat(tokens9[4]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens9[5]).isRightParenthesisSymbol()

    val tokens10 = MathTokenizer2.tokenize("732").toList()
    assertThat(tokens10).hasSize(1)
    assertThat(tokens10.first()).isPositiveIntegerWhoseValue().isEqualTo(732)

    val tokens11 = MathTokenizer2.tokenize("73 2").toList()
    assertThat(tokens11).hasSize(2)
    assertThat(tokens11[0]).isPositiveIntegerWhoseValue().isEqualTo(73)
    assertThat(tokens11[1]).isPositiveIntegerWhoseValue().isEqualTo(2)

    val tokens12 = MathTokenizer2.tokenize("1*2-3+4^7-8/3*2+7").toList()
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

    val tokens13 = MathTokenizer2.tokenize("x = √2 × 7 ÷ 4").toList()
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

  /*private val ALLOWED_XYZ_VARIABLES = listOf("x", "y", "z")
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
  }*/

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
