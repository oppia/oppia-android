package org.oppia.android.testing.math

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.util.math.MathTokenizer.Companion.Token

/** Tests for [TokenSubject]. */
@RunWith(JUnit4::class)
class TokenSubjectTest {

  @Test
  fun testTokenSubject_passesWithCorrectStartIndex() {
    val token = Token.PositiveInteger(42, 10, 15)
    TokenSubject.assertThat(token).hasStartIndexThat().isEqualTo(10)
  }

  @Test
  fun testTokenSubject_failsWithIncorrectStartIndex() {
    val token = Token.PositiveInteger(42, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).hasStartIndexThat().isEqualTo(11)
    }
  }

  @Test
  fun testTokenSubject_passesWithCorrectEndIndex() {
    val token = Token.PositiveInteger(42, 10, 15)
    TokenSubject.assertThat(token).hasEndIndexThat().isEqualTo(15)
  }

  @Test
  fun testTokenSubject_failsWithIncorrectEndIndex() {
    val token = Token.PositiveInteger(42, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).hasEndIndexThat().isEqualTo(14)
    }
  }

  @Test
  fun testTokenSubject_passesWithCorrectPositiveIntegerValue() {
    val token = Token.PositiveInteger(42, 10, 15)
    TokenSubject.assertThat(token).isPositiveIntegerWhoseValue().isEqualTo(42)
  }

  @Test
  fun testTokenSubject_failsWithIncorrectPositiveIntegerValue() {
    val token = Token.PositiveInteger(42, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).isPositiveIntegerWhoseValue().isEqualTo(45)
    }
  }

  @Test
  fun testTokenSubject_passesWithCoreectPositiveRealNumberValue() {
    val token = Token.PositiveRealNumber(3.14, 10, 15)
    TokenSubject.assertThat(token).isPositiveRealNumberWhoseValue().isEqualTo(3.14)
  }

  fun testTokenSubject_failsWithIncorrectPositiveRealNumberValue() {
    val token = Token.PositiveRealNumber(3.14, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).isPositiveRealNumberWhoseValue().isEqualTo(25)
    }
  }

  @Test
  fun testTokenSubject_passesWithCorrectVariableName() {
    val token = Token.VariableName("x", 10, 15)
    TokenSubject.assertThat(token).isVariableWhoseName().isEqualTo("x")
  }

  @Test
  fun testTokenSubject_isVariableWhoseName_incorrectName_fails() {
    val token = Token.VariableName("x", 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).isVariableWhoseName().isEqualTo("y")
    }
  }

  @Test
  fun testTokenSubject_passesWithCorrectFunctionNameAndAllowedStatus() {
    val token = Token.FunctionName("sqrt", true, 10, 15)
    TokenSubject.assertThat(token)
      .isFunctionNameThat()
      .hasNameThat().isEqualTo("sqrt")
  }

  @Test
  fun testTokenSubject_failsWithIncorrectFunctionNameAndAllowedStatus() {
    val token = Token.FunctionName("sqrt", true, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token)
        .isFunctionNameThat()
        .hasNameThat().isEqualTo("sine")
    }
  }

  @Test
  fun testTokenSubject_symbolIsMinusSymbol() {
    val token = Token.MinusSymbol(10, 11)
    TokenSubject.assertThat(token).isMinusSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsSquareRootSymbol() {
    val token = Token.SquareRootSymbol(10, 11)
    TokenSubject.assertThat(token).isSquareRootSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsPlusSymbol() {
    val token = Token.PlusSymbol(10, 11)
    TokenSubject.assertThat(token).isPlusSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsMultiplySymbol() {
    val token = Token.MultiplySymbol(10, 11)
    TokenSubject.assertThat(token).isMultiplySymbol()
  }

  @Test
  fun testTokenSubject_symbolIsDivideSymbol() {
    val token = Token.DivideSymbol(10, 11)
    TokenSubject.assertThat(token).isDivideSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsExponentiationSymbol() {
    val token = Token.ExponentiationSymbol(10, 11)
    TokenSubject.assertThat(token).isExponentiationSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsEqualsSymbol() {
    val token = Token.EqualsSymbol(10, 11)
    TokenSubject.assertThat(token).isEqualsSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsLeftParenthesisSymbol() {
    val token = Token.LeftParenthesisSymbol(10, 11)
    TokenSubject.assertThat(token).isLeftParenthesisSymbol()
  }

  @Test
  fun testTokenSubject_symbolIsRightParenthesisSymbol() {
    val token = Token.RightParenthesisSymbol(10, 11)
    TokenSubject.assertThat(token).isRightParenthesisSymbol()
  }

  @Test
  fun testTokenSubject_failsWithIncorrectSymbol() {
    val token = Token.RightParenthesisSymbol(10, 11)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).isMinusSymbol()
    }
  }

  @Test
  fun testTokenSubject_checkIsInvalidToken_passes() {
    val token = Token.InvalidToken(10, 11)
    TokenSubject.assertThat(token).isInvalidToken()
  }

  @Test
  fun testTokenSubject_checkIsInvalidToken_fails() {
    val token = Token.PositiveInteger(10, 11, 42)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token).isInvalidToken()
    }
  }

  @Test
  fun testTokenSubject_checkIsIncompleteFunctionName() {
    val token = Token.IncompleteFunctionName(10, 11)
    TokenSubject.assertThat(token).isIncompleteFunctionName()
  }

  @Test
  fun testTokenSubject_functionNameSubject_nameCheck_passes() {
    val token = Token.FunctionName("sqrt", true, 10, 15)
    TokenSubject.assertThat(token)
      .isFunctionNameThat()
      .hasNameThat().isEqualTo("sqrt")
  }

  @Test
  fun testTokenSubject_functionNameSubject_nameCheck_fails() {
    val token = Token.FunctionName("sqrt", true, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token)
        .isFunctionNameThat()
        .hasNameThat().isEqualTo("sin")
    }
  }

  @Test
  fun testTokenSubject_functionNameSubject_allowedPropertyCheck_passes() {
    val token = Token.FunctionName("sqrt", true, 10, 15)
    TokenSubject.assertThat(token)
      .isFunctionNameThat()
      .hasIsAllowedPropertyThat().isTrue()
  }

  @Test
  fun testTokenSubject_functionNameSubject_allowedPropertyCheck_fails() {
    val token = Token.FunctionName("sqrt", false, 10, 15)
    assertThrows(AssertionError::class.java) {
      TokenSubject.assertThat(token)
        .isFunctionNameThat()
        .hasIsAllowedPropertyThat().isTrue()
    }
  }
}
