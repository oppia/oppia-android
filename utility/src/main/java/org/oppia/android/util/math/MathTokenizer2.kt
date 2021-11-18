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
      val nextChar = chars.peek()
      return if (firstChar == 's' && nextChar == 'q') {
        // With 'sq' next to each other, 'rt' is expected to follow.
        chars.expectNextValue { 'q' }
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
        chars.expectNextValue { 'r' }
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
        chars.expectNextValue { 't' }
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
        Token.FunctionName("sqrt", startIndex, endIndex = chars.getRetrievalCount())
      } else {
        Token.VariableName(firstChar.toString(), startIndex, endIndex = chars.getRetrievalCount())
      }
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

    sealed class Token {
      /** The index in the input stream at which point this token begins. */
      abstract val startIndex: Int

      /** The (exclusive) index in the input stream at which point this token ends. */
      abstract val endIndex: Int

      class PositiveInteger(
        val parsedValue: Int, override val startIndex: Int, override val endIndex: Int
      ) : Token()

      class PositiveRealNumber(
        val parsedValue: Double, override val startIndex: Int, override val endIndex: Int
      ) : Token()

      class VariableName(
        val parsedName: String, override val startIndex: Int, override val endIndex: Int
      ) : Token()

      class FunctionName(
        val parsedName: String, override val startIndex: Int, override val endIndex: Int
      ) : Token()

      class MinusSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class SquareRootSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class PlusSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class MultiplySymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class DivideSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class ExponentiationSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class EqualsSymbol(override val startIndex: Int, override val endIndex: Int) : Token()

      class LeftParenthesisSymbol(
        override val startIndex: Int, override val endIndex: Int
      ) : Token()

      class RightParenthesisSymbol(
        override val startIndex: Int, override val endIndex: Int
      ) : Token()

      // TODO: add context to line & index, and enum for context on failure.
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
  }
}
