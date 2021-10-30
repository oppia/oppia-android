package org.oppia.android.util.math

import java.lang.StringBuilder

// TODO: rename to MathTokenizer & add documentation.
// TODO: consider a more efficient implementation that uses 1 underlying buffer (which could still
//  be sequence-backed) with a forced lookahead-of-1 API, to also avoid rebuffering when parsing
//  sequences of characters like for integers.

// TODO: add customization to omit certain symbols (such as variables for numeric expressions?)
class MathTokenizer2 private constructor() {
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
          '√' -> tokenizeSymbol(chars) { Token.SquareRootSymbol }
          '+' -> tokenizeSymbol(chars) { Token.PlusSymbol }
          // TODO: add tests for different subtraction/minus symbols.
          '-', '−' -> tokenizeSymbol(chars) { Token.MinusSymbol }
          '*', '×' -> tokenizeSymbol(chars) { Token.MultiplySymbol }
          '/', '÷' -> tokenizeSymbol(chars) { Token.DivideSymbol }
          '^' -> tokenizeSymbol(chars) { Token.ExponentiationSymbol }
          '=' -> tokenizeSymbol(chars) { Token.EqualsSymbol }
          '(' -> tokenizeSymbol(chars) { Token.LeftParenthesisSymbol }
          ')' -> tokenizeSymbol(chars) { Token.RightParenthesisSymbol }
          null -> null // End of stream.
          else -> { // Invalid character.
            chars.next() // Parse the invalid character.
            Token.InvalidToken
          }
        }
      }
    }

    private fun tokenizeIntegerOrRealNumber(chars: PeekableIterator<Char>): Token {
      val integerPart1 = parseInteger(chars) ?: return Token.InvalidToken
      chars.consumeWhitespace() // Whitespace is allowed between digits and the '.'.
      return if (chars.peek() == '.') {
        chars.next() // Parse the "." since it will be re-added later.
        chars.consumeWhitespace() // Whitespace is allowed between the '.' and following digits.

        // Another integer must follow the ".".
        val integerPart2 = parseInteger(chars) ?: return Token.InvalidToken

        val doubleValue = "$integerPart1.$integerPart2".toDoubleOrNull()
          ?: return Token.InvalidToken
        Token.PositiveRealNumber(doubleValue)
      } else {
        Token.PositiveInteger(integerPart1.toIntOrNull() ?: return Token.InvalidToken)
      }
    }

    private fun tokenizeVariableOrFunctionName(chars: PeekableIterator<Char>): Token {
      val firstChar = chars.next()
      val nextChar = chars.peek()
      return if (firstChar == 's' && nextChar == 'q') {
        // With 'sq' next to each other, 'rt' is expected to follow.
        chars.expectNextValue { 'q' } ?: return Token.InvalidToken
        chars.expectNextValue { 'r' } ?: return Token.InvalidToken
        chars.expectNextValue { 't' } ?: return Token.InvalidToken
        Token.FunctionName("sqrt")
      } else Token.VariableName(firstChar.toString())
    }

    private fun tokenizeSymbol(chars: PeekableIterator<Char>, factory: () -> Token): Token {
      chars.next() // Parse the symbol.
      return factory()
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

    sealed class Token {
      class PositiveInteger(val parsedValue: Int) : Token()

      class PositiveRealNumber(val parsedValue: Double) : Token()

      class VariableName(val parsedName: String) : Token()

      class FunctionName(val parsedName: String) : Token()

      object MinusSymbol : Token()

      object SquareRootSymbol : Token()

      object PlusSymbol : Token()

      object MultiplySymbol : Token()

      object DivideSymbol : Token()

      object ExponentiationSymbol : Token()

      object EqualsSymbol : Token()

      object LeftParenthesisSymbol : Token()

      object RightParenthesisSymbol : Token()

      // TODO: add context to line & index, and enum for context on failure.
      object InvalidToken : Token()
    }

    // TODO: consider whether to use the more correct & expensive Java Char.isWhitespace().
    private fun Char.isWhitespace(): Boolean = when (this) {
      ' ', '\t', '\n', '\r' -> true
      else -> false
    }

    private fun PeekableIterator<Char>.consumeWhitespace() {
      while (peek()?.isWhitespace() == true) next()
    }
  }
}
