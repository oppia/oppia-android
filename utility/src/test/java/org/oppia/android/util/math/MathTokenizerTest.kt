package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.math.TokenSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode

/** Tests for [MathTokenizer]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathTokenizerTest {
  @Parameter lateinit var variableName: String
  @Parameter lateinit var funcName: String
  @Parameter lateinit var token: String

  @Test
  fun testTokenize_emptyString_producesNoTokens() {
    val tokens = MathTokenizer.tokenize("").toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  fun testTokenize_onlyWhitespace_producesNoTokens() {
    val tokens = MathTokenizer.tokenize("     ").toList()

    assertThat(tokens).isEmpty()
  }

  @Test
  fun testTokenize_singleDigit_producesPositiveIntegerToken() {
    val tokens = MathTokenizer.tokenize("1").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
  }

  @Test
  fun testTokenize_digits_producesPositiveIntegerToken() {
    val tokens = MathTokenizer.tokenize("927").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(927)
  }

  @Test
  fun testTokenize_digits_withSpaces_spacesAreIgnored() {
    val tokens = MathTokenizer.tokenize("  927   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(927)
  }

  @Test
  fun testTokenize_positiveInteger_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  927   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(5)
  }

  @Test
  fun testTokenize_positiveInteger_veryLargeNumber_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("9823190830924801923845").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_decimal_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize(".").toList()

    // A decimal by itself is invalid.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_digitsWithDecimal_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("12.").toList()

    // The decimal is incomplete. Note that this is one token since the '12.' is considered a single
    // invalid unit.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_decimalWithDigits_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize(".34").toList()

    // The decimal is incomplete. Note that this results in 2 tokens since the '.' is encountered as
    // an isolated and unexpected symbol.
    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_digitsWithDecimalWithDigits_producesPositiveRealNumberToken() {
    val tokens = MathTokenizer.tokenize("12.34").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPositiveRealNumberWhoseValue().isWithin(1e-5).of(12.34)
  }

  @Test
  fun testTokenize_positiveRealNumber_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  12.34    ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(7)
  }

  @Test
  fun testTokenize_positiveRealNumber_veryLargeNumber_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("1${"0".repeat(400)}.12345").toList()

    // The number is too large to represent as a double (so it's treated as infinity).
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_variable_singleLetter_producesVariableToken() {
    val tokens = MathTokenizer.tokenize("x").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("x")
  }

  @Test
  fun testTokenize_variable_twoLetters_producesMultipleVariableTokens() {
    val tokens = MathTokenizer.tokenize("xy").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("x")
    assertThat(tokens[1]).isVariableWhoseName().isEqualTo("y")
  }

  @Test
  fun testTokenize_variable_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  x   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  @RunParameterized(
    Iteration("a", "variableName=a"), Iteration("A", "variableName=A"),
    Iteration("b", "variableName=b"), Iteration("B", "variableName=B"),
    Iteration("c", "variableName=c"), Iteration("C", "variableName=C"),
    Iteration("d", "variableName=d"), Iteration("D", "variableName=D"),
    Iteration("e", "variableName=e"), Iteration("E", "variableName=E"),
    Iteration("f", "variableName=f"), Iteration("F", "variableName=F"),
    Iteration("g", "variableName=g"), Iteration("G", "variableName=G"),
    Iteration("h", "variableName=h"), Iteration("H", "variableName=H"),
    Iteration("i", "variableName=i"), Iteration("I", "variableName=I"),
    Iteration("j", "variableName=j"), Iteration("J", "variableName=J"),
    Iteration("k", "variableName=k"), Iteration("K", "variableName=K"),
    Iteration("l", "variableName=l"), Iteration("L", "variableName=L"),
    Iteration("m", "variableName=m"), Iteration("M", "variableName=M"),
    Iteration("n", "variableName=n"), Iteration("N", "variableName=N"),
    Iteration("o", "variableName=o"), Iteration("O", "variableName=O"),
    Iteration("p", "variableName=p"), Iteration("P", "variableName=P"),
    Iteration("q", "variableName=q"), Iteration("Q", "variableName=Q"),
    Iteration("r", "variableName=r"), Iteration("R", "variableName=R"),
    Iteration("s", "variableName=s"), Iteration("S", "variableName=S"),
    Iteration("t", "variableName=t"), Iteration("T", "variableName=T"),
    Iteration("u", "variableName=u"), Iteration("U", "variableName=U"),
    Iteration("v", "variableName=v"), Iteration("V", "variableName=V"),
    Iteration("w", "variableName=w"), Iteration("W", "variableName=W"),
    Iteration("x", "variableName=x"), Iteration("X", "variableName=X"),
    Iteration("y", "variableName=y"), Iteration("Y", "variableName=Y"),
    Iteration("z", "variableName=z"), Iteration("Z", "variableName=Z")
  )
  fun testTokenize_variable_allLatinAlphabetCharactersAreValidVariables() {
    val tokens = MathTokenizer.tokenize(variableName).toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo(variableName)
  }

  @Test
  fun testTokenize_sqrtFunction_producesAllowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("sqrt").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("sqrt")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isTrue()
  }

  @Test
  fun testTokenize_sqrtFunction_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  sqrt   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(6)
  }

  @Test
  fun testTokenize_expFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("exp").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("exp")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_logFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("log").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("log")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_log10Function_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("log10").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("log10")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_lnFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("ln").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("ln")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_sinFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("sin").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("sin")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_cosFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("cos").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("cos")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_tanFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("tan").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("tan")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_cotFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("cot").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("cot")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_cscFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("csc").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("csc")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_secFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("sec").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("sec")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_atanFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("atan").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("atan")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_asinFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("asin").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("asin")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_acosFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("acos").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("acos")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_absFunction_producesDisallowedFunctionNameToken() {
    val tokens = MathTokenizer.tokenize("abs").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isFunctionNameThat().hasNameThat().isEqualTo("abs")
    assertThat(tokens[0]).isFunctionNameThat().hasIsAllowedPropertyThat().isFalse()
  }

  @Test
  fun testTokenize_squareRoot_producesSquareRootSymbol() {
    val tokens = MathTokenizer.tokenize("√").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isSquareRootSymbol()
  }

  @Test
  fun testTokenize_squareRootSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  √   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_hyphen_producesMinusSymbol() {
    val tokens = MathTokenizer.tokenize("-").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isMinusSymbol()
  }

  @Test
  fun testTokenize_mathMinusSymbol_producesMinusSymbol() {
    val tokens = MathTokenizer.tokenize("−").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isMinusSymbol()
  }

  @Test
  fun testTokenize_minusSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  −   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_plus_producesPlusSymbol() {
    val tokens = MathTokenizer.tokenize("+").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isPlusSymbol()
  }

  @Test
  fun testTokenize_plusSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  +   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_asterisk_producesMultiplySymbol() {
    val tokens = MathTokenizer.tokenize("*").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isMultiplySymbol()
  }

  @Test
  fun testTokenize_mathTimesSymbol_producesMultiplySymbol() {
    val tokens = MathTokenizer.tokenize("×").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isMultiplySymbol()
  }

  @Test
  fun testTokenize_multiplySymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  ×   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_forwardSlash_producesDivideSymbol() {
    val tokens = MathTokenizer.tokenize("/").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isDivideSymbol()
  }

  @Test
  fun testTokenize_mathDivideSymbol_producesDivideSymbol() {
    val tokens = MathTokenizer.tokenize("÷").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isDivideSymbol()
  }

  @Test
  fun testTokenize_divideSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  ÷   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_caret_producesExponentiationSymbol() {
    val tokens = MathTokenizer.tokenize("^").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isExponentiationSymbol()
  }

  @Test
  fun testTokenize_exponentiationSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  ^   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_equals_producesEqualsSymbol() {
    val tokens = MathTokenizer.tokenize("=").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isEqualsSymbol()
  }

  @Test
  fun testTokenize_equalsSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  =   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_leftParenthesis_producesLeftParenthesisSymbol() {
    val tokens = MathTokenizer.tokenize("(").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isLeftParenthesisSymbol()
  }

  @Test
  fun testTokenize_leftParenthesisSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  (   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_rightParenthesis_producesRightParenthesisSymbol() {
    val tokens = MathTokenizer.tokenize(")").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenize_rightParenthesisSymbol_withSpaces_tokenHasCorrectIndices() {
    val tokens = MathTokenizer.tokenize("  )   ").toList()

    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).hasStartIndexThat().isEqualTo(2)
    assertThat(tokens[0]).hasEndIndexThat().isEqualTo(3)
  }

  @Test
  fun testTokenize_firstLetterOfFunctionNameOnly_shouldParseAsVariable() {
    val tokens = MathTokenizer.tokenize("a").toList()

    // Although there are functions starting with 'a', 'a' by itself is a variable name since
    // there's no context to indicate that it's part of a function name.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("a")
  }

  @Test
  @RunParameterized(
    Iteration("aa", "funcName=aa"), Iteration("ad", "funcName=ad"), Iteration("al", "funcName=al"),
    Iteration("ca", "funcName=ca"), Iteration("ce", "funcName=ce"), Iteration("cr", "funcName=cr"),
    Iteration("ea", "funcName=ea"), Iteration("ef", "funcName=ef"), Iteration("er", "funcName=er"),
    Iteration("la", "funcName=la"), Iteration("lz", "funcName=lz"), Iteration("le", "funcName=le"),
    Iteration("sa", "funcName=sa"), Iteration("sp", "funcName=sp"), Iteration("sz", "funcName=sz"),
    Iteration("te", "funcName=te"), Iteration("to", "funcName=to"), Iteration("tr", "funcName=tr")
  )
  fun testTokenize_twoVarsSharingOnlyFirstWithFunctionNames_shouldParseAsVariables() {
    val tokens = MathTokenizer.tokenize(funcName).toList()

    // This test covers many cases where the first letter can be shared with function names without
    // triggering a failure. Note that it doesn't cover all cases for simplicity.
    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isVariableWhoseName().isNotEmpty()
    assertThat(tokens[1]).isVariableWhoseName().isNotEmpty()
  }

  @Test
  @RunParameterized(
    Iteration("ab", "funcName=ab"), Iteration("ac", "funcName=ac"),
    Iteration("aco", "funcName=aco"), Iteration("as", "funcName=as"),
    Iteration("asi", "funcName=asi"), Iteration("at", "funcName=at"),
    Iteration("ata", "funcName=ata"), Iteration("co", "funcName=co"),
    Iteration("cs", "funcName=cs"), Iteration("ex", "funcName=ex"),
    Iteration("lo", "funcName=lo"), Iteration("log1", "funcName=log1"),
    Iteration("se", "funcName=se"), Iteration("si", "funcName=si"),
    Iteration("sq", "funcName=sq"), Iteration("ta", "funcName=ta")
  )
  fun testTokenize_twoVarsSharedWithFunctionNames_shouldParseAsIncompleteFuncName() {
    val tokens = MathTokenizer.tokenize(funcName).toList()

    // This test covers all cases where sharing the first few letters of a function name triggers a
    // failure due to the grammar being limited to LL(1) parsing.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isIncompleteFunctionName()
  }

  @Test
  fun testTokenize_sqrtWithCapitalLetters_isInterpretedAsVariables() {
    val tokens = MathTokenizer.tokenize("Sqrt").toList()

    // Function names can't be capitalized, so 'Sqrt' is treated as 4 consecutive variables.
    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("S")
    assertThat(tokens[1]).isVariableWhoseName().isEqualTo("q")
    assertThat(tokens[2]).isVariableWhoseName().isEqualTo("r")
    assertThat(tokens[3]).isVariableWhoseName().isEqualTo("t")
  }

  @Test
  fun testTokenize_sqrtWithSpaces_isInterpretedAsVariables() {
    val tokens = MathTokenizer.tokenize("s qrt").toList()

    // Spaces break the function name, so the letters must be variables.
    assertThat(tokens).hasSize(4)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("s")
    assertThat(tokens[1]).isVariableWhoseName().isEqualTo("q")
    assertThat(tokens[2]).isVariableWhoseName().isEqualTo("r")
    assertThat(tokens[3]).isVariableWhoseName().isEqualTo("t")
  }

  @Test
  fun testTokenize_sameTokenTwice_parsesTwice() {
    val tokens = MathTokenizer.tokenize("aa").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isVariableWhoseName().isEqualTo("a")
    assertThat(tokens[1]).isVariableWhoseName().isEqualTo("a")
  }

  @Test
  fun testTokenize_exclamationPoint_producesInvalidToken() {
    val tokens = MathTokenizer.tokenize("!").toList()

    // '!' is not yet supported by the tokenizer, so it's an invalid token.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  @RunParameterized(
    Iteration("α", "token=α"), Iteration("Α", "token=Α"),
    Iteration("β", "token=β"), Iteration("Β", "token=Β"),
    Iteration("γ", "token=γ"), Iteration("Γ", "token=Γ"),
    Iteration("δ", "token=δ"), Iteration("Δ", "token=Δ"),
    Iteration("ϵ", "token=ϵ"), Iteration("Ε", "token=Ε"),
    Iteration("ζ", "token=ζ"), Iteration("Ζ", "token=Ζ"),
    Iteration("η", "token=η"), Iteration("Η", "token=Η"),
    Iteration("θ", "token=θ"), Iteration("Θ", "token=Θ"),
    Iteration("ι", "token=ι"), Iteration("Ι", "token=Ι"),
    Iteration("κ", "token=κ"), Iteration("Κ", "token=Κ"),
    Iteration("λ", "token=λ"), Iteration("Λ", "token=Λ"),
    Iteration("μ", "token=μ"), Iteration("Μ", "token=Μ"),
    Iteration("ν", "token=ν"), Iteration("Ν", "token=Ν"),
    Iteration("ξ", "token=ξ"), Iteration("Ξ", "token=Ξ"),
    Iteration("ο", "token=ο"), Iteration("Ο", "token=Ο"),
    Iteration("π", "token=π"), Iteration("Π", "token=Π"),
    Iteration("ρ", "token=ρ"), Iteration("Ρ", "token=Ρ"),
    Iteration("σ", "token=σ"), Iteration("Σ", "token=Σ"),
    Iteration("τ", "token=τ"), Iteration("Τ", "token=Τ"),
    Iteration("υ", "token=υ"), Iteration("Υ", "token=Υ"),
    Iteration("ϕ", "token=ϕ"), Iteration("Φ", "token=Φ"),
    Iteration("χ", "token=χ"), Iteration("Χ", "token=Χ"),
    Iteration("ψ", "token=ψ"), Iteration("Ψ", "token=Ψ"),
    Iteration("ω", "token=ω"), Iteration("Ω", "token=Ω"),
    Iteration("ς", "token=ς")
  )
  fun testTokenize_greekLetters_produceInvalidTokens() {
    val tokens = MathTokenizer.tokenize(token).toList()

    // Greek letters are not yet supported.
    assertThat(tokens).hasSize(1)
    assertThat(tokens[0]).isInvalidToken()
  }

  @Test
  fun testTokenize_manyOtherUnicodeValues_produceInvalidTokens() {
    // Build a large list of unicode characters minus those which are actually allowed. The ASCII
    // range is excluded from this list.
    val characters = ('\u007f' .. '\uffff').filterNot {
      it in listOf('×', '÷', '−', '√')
    }
    val charStr = characters.joinToString("")

    val tokens = MathTokenizer.tokenize(charStr).toList()

    // Verify that all of the unicode characters cover in this range are invalid.
    assertThat(tokens).hasSize(charStr.length)
    tokens.forEach { assertThat(it).isInvalidToken() }
    // Sanity check to ensure that the tokens are actually populated.
    assertThat(tokens.size).isGreaterThan(0x7fff)
  }

  @Test
  fun testTokenize_validAndInvalidTokens_tokenizerContinues() {
    val tokens = MathTokenizer.tokenize("2*7!/|-9|").toList()

    assertThat(tokens).hasSize(9)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[1]).isMultiplySymbol()
    assertThat(tokens[2]).isPositiveIntegerWhoseValue().isEqualTo(7)
    assertThat(tokens[3]).isInvalidToken()
    assertThat(tokens[4]).isDivideSymbol()
    assertThat(tokens[5]).isInvalidToken()
    assertThat(tokens[6]).isMinusSymbol()
    assertThat(tokens[7]).isPositiveIntegerWhoseValue().isEqualTo(9)
    assertThat(tokens[8]).isInvalidToken()
  }

  @Test
  fun testTokenize_manyTokenTypes_parseCorrectlyAndInOrder() {
    val tokens = MathTokenizer.tokenize("1 * (√2 - 3.14) + 4^7-8/3×-2 + sqrt(7)÷3").toList()

    assertThat(tokens).hasSize(26)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(1)
    assertThat(tokens[1]).isMultiplySymbol()
    assertThat(tokens[2]).isLeftParenthesisSymbol()
    assertThat(tokens[3]).isSquareRootSymbol()
    assertThat(tokens[4]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[5]).isMinusSymbol()
    assertThat(tokens[6]).isPositiveRealNumberWhoseValue().isWithin(1e-5).of(3.14)
    assertThat(tokens[7]).isRightParenthesisSymbol()
    assertThat(tokens[8]).isPlusSymbol()
    assertThat(tokens[9]).isPositiveIntegerWhoseValue().isEqualTo(4)
    assertThat(tokens[10]).isExponentiationSymbol()
    assertThat(tokens[11]).isPositiveIntegerWhoseValue().isEqualTo(7)
    assertThat(tokens[12]).isMinusSymbol()
    assertThat(tokens[13]).isPositiveIntegerWhoseValue().isEqualTo(8)
    assertThat(tokens[14]).isDivideSymbol()
    assertThat(tokens[15]).isPositiveIntegerWhoseValue().isEqualTo(3)
    assertThat(tokens[16]).isMultiplySymbol()
    assertThat(tokens[17]).isMinusSymbol()
    assertThat(tokens[18]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[19]).isPlusSymbol()
    assertThat(tokens[20]).isFunctionNameThat().hasNameThat().isEqualTo("sqrt")
    assertThat(tokens[21]).isLeftParenthesisSymbol()
    assertThat(tokens[22]).isPositiveIntegerWhoseValue().isEqualTo(7)
    assertThat(tokens[23]).isRightParenthesisSymbol()
    assertThat(tokens[24]).isDivideSymbol()
    assertThat(tokens[25]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }

  @Test
  fun testTokenize_allFormsOfWhiteSpaceAreIgnored() {
    val tokens = MathTokenizer.tokenize("  \n\t2\r\n 3 \n").toList()

    assertThat(tokens).hasSize(2)
    assertThat(tokens[0]).isPositiveIntegerWhoseValue().isEqualTo(2)
    assertThat(tokens[1]).isPositiveIntegerWhoseValue().isEqualTo(3)
  }
}
