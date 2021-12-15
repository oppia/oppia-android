package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import java.lang.StringBuilder

// TODO: rename to MathTokenizer & add documentation.
// TODO: consider a more efficient implementation that uses 1 underlying buffer (which could still
//  be sequence-backed) with a forced lookahead-of-1 API, to also avoid rebuffering when parsing
//  sequences of characters like for integers.

// TODO: add customization to omit certain symbols (such as variables for numeric expressions?)
class MathTokenizer private constructor() {
  companion object {
    fun tokenize(input: String): Sequence<Token> = tokenize(input.toCharArray().asSequence())

    fun tokenize(input: Sequence<Char>): Sequence<Token> {
      val chars = PeekableIterator.fromSequence(input)
      return generateSequence {
        // Consume any whitespace that might precede a valid token.
        chars.consumeWhitespace()

        // Parse the next token from the underlying sequence.
        when (chars.peek()) {
          in '0'..'9' -> tokenizeIntegerOrRealNumber(chars)
          in 'a'..'z', in 'A'..'Z' -> tokenizeVariableOrFunctionName(chars)
          '√' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.SquareRootSymbol(startIndex, endIndex)
          }
          '+' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.PlusSymbol(startIndex, endIndex)
          }
          // TODO: add tests for different subtraction/minus symbols.
          '-', '−' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MinusSymbol(startIndex, endIndex)
          }
          '*', '×' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MultiplySymbol(startIndex, endIndex)
          }
          '/', '÷' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.DivideSymbol(startIndex, endIndex)
          }
          '^' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.ExponentiationSymbol(startIndex, endIndex)
          }
          '=' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.EqualsSymbol(startIndex, endIndex)
          }
          '(' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.LeftParenthesisSymbol(startIndex, endIndex)
          }
          ')' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.RightParenthesisSymbol(startIndex, endIndex)
          }
          null -> null // End of stream.
          // Invalid character.
          else -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.InvalidToken(startIndex, endIndex)
          }
        }
      }
    }

    private fun tokenizeIntegerOrRealNumber(chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()
      val integerPart1 =
        parseInteger(chars)
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
      chars.consumeWhitespace() // Whitespace is allowed between digits and the '.'.
      return if (chars.peek() == '.') {
        chars.next() // Parse the "." since it will be re-added later.
        chars.consumeWhitespace() // Whitespace is allowed between the '.' and following digits.

        // Another integer must follow the ".".
        val integerPart2 = parseInteger(chars)
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())

        // TODO: validate that the result isn't NaN or INF.
        val doubleValue = "$integerPart1.$integerPart2".toDoubleOrNull()
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
        Token.PositiveRealNumber(doubleValue, startIndex, endIndex = chars.getRetrievalCount())
      } else {
        Token.PositiveInteger(
          integerPart1.toIntOrNull()
            ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount()),
          startIndex,
          endIndex = chars.getRetrievalCount()
        )
      }
    }

    private fun tokenizeVariableOrFunctionName(chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()
      val firstChar = chars.next()

      // latin_letter = lowercase_latin_letter | uppercase_latin_letter ;
      // variable = latin_letter ;
      return tokenizeFunctionName(firstChar, startIndex, chars)
        ?: Token.VariableName(
          firstChar.toString(), startIndex, endIndex = chars.getRetrievalCount()
        )
    }

    private fun tokenizeFunctionName(
      currChar: Char,
      startIndex: Int,
      chars: PeekableIterator<Char>
    ): Token? {
      // allowed_function_name = "sqrt" ;
      // disallowed_function_name =
      //     "exp" | "log" | "log10" | "ln" | "sin" | "cos" | "tan" | "cot" | "csc"
      //     | "sec" | "atan" | "asin" | "acos" | "abs" ;
      // function_name = allowed_function_name | disallowed_function_name ;
      val nextChar = chars.peek()
      return when (currChar) {
        'a' -> {
          // abs, acos, asin, atan, or variable.
          when (nextChar) {
            'b' ->
              tokenizeExpectedFunction(name = "abs", isAllowedFunction = false, startIndex, chars)
            'c' ->
              tokenizeExpectedFunction(name = "acos", isAllowedFunction = false, startIndex, chars)
            's' ->
              tokenizeExpectedFunction(name = "asin", isAllowedFunction = false, startIndex, chars)
            't' ->
              tokenizeExpectedFunction(name = "atan", isAllowedFunction = false, startIndex, chars)
            else -> null // Must be a variable.
          }
        }
        'c' -> {
          // cos, cot, csc, or variable.
          when (nextChar) {
            'o' -> {
              chars.next() // Skip the 'o' to go to the last character.
              val name = if (chars.peek() == 's') {
                chars.expectNextMatches { it == 's' }
                  ?: return Token.IncompleteFunctionName(
                    startIndex, endIndex = chars.getRetrievalCount()
                  )
                "cos"
              } else {
                // Otherwise, it must be 'c' for 'cot' since the parser can't backtrack.
                chars.expectNextMatches { it == 't' }
                  ?: return Token.IncompleteFunctionName(
                    startIndex, endIndex = chars.getRetrievalCount()
                  )
                "cot"
              }
              Token.FunctionName(
                name, isAllowedFunction = false, startIndex, endIndex = chars.getRetrievalCount()
              )
            }
            's' ->
              tokenizeExpectedFunction(name = "csc", isAllowedFunction = false, startIndex, chars)
            else -> null // Must be a variable.
          }
        }
        'e' -> {
          // exp or variable.
          if (nextChar == 'x') {
            tokenizeExpectedFunction(name = "exp", isAllowedFunction = false, startIndex, chars)
          } else null // Must be a variable.
        }
        'l' -> {
          // ln, log, log10, or variable.
          when (nextChar) {
            'n' ->
              tokenizeExpectedFunction(name = "ln", isAllowedFunction = false, startIndex, chars)
            'o' -> {
              // Skip the 'o'. Following the 'o' must be a 'g' since the parser can't backtrack.
              chars.next()
              chars.expectNextMatches { it == 'g' }
                ?: return Token.IncompleteFunctionName(
                  startIndex, endIndex = chars.getRetrievalCount()
                )
              val name = if (chars.peek() == '1') {
                // '10' must be next for 'log10'.
                chars.expectNextMatches { it == '1' }
                  ?: return Token.IncompleteFunctionName(
                    startIndex, endIndex = chars.getRetrievalCount()
                  )
                chars.expectNextMatches { it == '0' }
                  ?: return Token.IncompleteFunctionName(
                    startIndex, endIndex = chars.getRetrievalCount()
                  )
                "log10"
              } else "log"
              Token.FunctionName(
                name, isAllowedFunction = false, startIndex, endIndex = chars.getRetrievalCount()
              )
            }
            else -> null // Must be a variable.
          }
        }
        's' -> {
          // sec, sin, sqrt, or variable.
          when (nextChar) {
            'e' ->
              tokenizeExpectedFunction(name = "sec", isAllowedFunction = false, startIndex, chars)
            'i' ->
              tokenizeExpectedFunction(name = "sin", isAllowedFunction = false, startIndex, chars)
            'q' ->
              tokenizeExpectedFunction(name = "sqrt", isAllowedFunction = true, startIndex, chars)
            else -> null // Must be a variable.
          }
        }
        't' -> {
          // tan or variable.
          if (nextChar == 'a') {
            tokenizeExpectedFunction(name = "tan", isAllowedFunction = false, startIndex, chars)
          } else null // Must be a variable.
        }
        else -> null // Must be a variable since no known functions match the first character.
      }
    }

    private fun tokenizeExpectedFunction(
      name: String,
      isAllowedFunction: Boolean,
      startIndex: Int,
      chars: PeekableIterator<Char>
    ): Token {
      return chars.expectNextCharsForFunctionName(name.substring(1), startIndex)
        ?: Token.FunctionName(
          name, isAllowedFunction, startIndex, endIndex = chars.getRetrievalCount()
        )
    }

    private fun tokenizeSymbol(chars: PeekableIterator<Char>, factory: (Int, Int) -> Token): Token {
      val startIndex = chars.getRetrievalCount()
      chars.next() // Parse the symbol.
      val endIndex = chars.getRetrievalCount()
      return factory(startIndex, endIndex)
    }

    private fun parseInteger(chars: PeekableIterator<Char>): String? {
      val integerBuilder = StringBuilder()
      while (chars.peek() in '0'..'9') {
        integerBuilder.append(chars.next())
      }
      return if (integerBuilder.isNotEmpty()) {
        integerBuilder.toString()
      } else null // Failed to parse; no digits.
    }

    interface UnaryOperatorToken {
      fun getUnaryOperator(): MathUnaryOperation.Operator
    }

    interface BinaryOperatorToken {
      fun getBinaryOperator(): MathBinaryOperation.Operator
    }

    sealed class Token {
      /** The index in the input stream at which point this token begins. */
      abstract val startIndex: Int

      /** The (exclusive) index in the input stream at which point this token ends. */
      abstract val endIndex: Int

      class PositiveInteger(
        val parsedValue: Int,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class PositiveRealNumber(
        val parsedValue: Double,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class VariableName(
        val parsedName: String,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class FunctionName(
        val parsedName: String,
        val isAllowedFunction: Boolean,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class MinusSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token(), UnaryOperatorToken, BinaryOperatorToken {
        override fun getUnaryOperator(): MathUnaryOperation.Operator = NEGATE

        override fun getBinaryOperator(): MathBinaryOperation.Operator = SUBTRACT
      }

      class SquareRootSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class PlusSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token(), UnaryOperatorToken, BinaryOperatorToken {
        override fun getUnaryOperator(): MathUnaryOperation.Operator = POSITIVE

        override fun getBinaryOperator(): MathBinaryOperation.Operator = ADD
      }

      class MultiplySymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token(), BinaryOperatorToken {
        override fun getBinaryOperator(): MathBinaryOperation.Operator = MULTIPLY
      }

      class DivideSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token(), BinaryOperatorToken {
        override fun getBinaryOperator(): MathBinaryOperation.Operator = DIVIDE
      }

      class ExponentiationSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token(), BinaryOperatorToken {
        override fun getBinaryOperator(): MathBinaryOperation.Operator = EXPONENTIATE
      }

      class EqualsSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class LeftParenthesisSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RightParenthesisSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class IncompleteFunctionName(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class InvalidToken(override val startIndex: Int, override val endIndex: Int) : Token()
    }

    // TODO: consider whether to use the more correct & expensive Java Char.isWhitespace().
    private fun Char.isWhitespace(): Boolean = when (this) {
      ' ', '\t', '\n', '\r' -> true
      else -> false
    }

    private fun PeekableIterator<Char>.consumeWhitespace() {
      while (peek()?.isWhitespace() == true) next()
    }

    /**
     * Expects each of the characters to be next in the token stream, in the order of the string.
     * All characters must be present in [this] iterator. Returns non-null if a failure occurs,
     * otherwise null if all characters were confirmed to be present. If null is returned, [this]
     * iterator will be at the token that comes after the last confirmed character in the string.
     */
    private fun PeekableIterator<Char>.expectNextCharsForFunctionName(
      chars: String,
      startIndex: Int
    ): Token? {
      for (c in chars) {
        expectNextValue { c }
          ?: return Token.IncompleteFunctionName(startIndex, endIndex = getRetrievalCount())
      }
      return null
    }
  }
}
