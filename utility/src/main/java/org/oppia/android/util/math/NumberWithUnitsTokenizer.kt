package org.oppia.android.util.math

import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator

/**
 * A tokenizer for parsing mathematical expressions containing numbers and units.
 *
 * The tokenizer supports whitespace between tokens and can handle various unit formats
 * including both singular and plural forms, abbreviated forms, and different naming conventions.
 *
 * See https://docs.google.com/document/d/1PF1LdzBUwNO3tSqucCoMdYpPOWUySI3yS4m_knfLdtE for the
 * various units supported by this tokenizer.
 */
class NumberWithUnitsTokenizer private constructor() {
  companion object {
    /**
     * Tokenizes a string input into a sequence of [Token] objects.
     *
     * This method processes characters one by one, identifying tokens based on the first
     * character encountered. It handles whitespace automatically and delegates to specific
     * tokenization methods based on the character type.
     *
     * @param input the string to tokenize
     * @return a [Sequence] of [Token] objects
     */
    fun tokenize(input: String): Sequence<Token> {
      val chars = input.toCharArray().asSequence().toPeekableIterator()
      return generateSequence {
        chars.consumeWhitespace()

        when (chars.peek()) {
          in '0'..'9' -> tokenizeIntegerOrRealNumber(chars)
          in 'a'..'z', in 'A'..'Z' -> tokenizeUnit(input, chars)
          '-', '−', '–' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MinusSymbol(startIndex, endIndex)
          }

          '/' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.DivideSymbol(startIndex, endIndex)
          }

          '₹', '$', '¢' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            rawUnitToken(input, startIndex, endIndex)
          }

          '^' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.ExponentiationSymbol(startIndex, endIndex)
          }

          '*' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MultiplySymbol(startIndex, endIndex)
          }

          '(' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.LeftParenthesisSymbol(startIndex, endIndex)
          }

          ')' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.RightParenthesisSymbol(startIndex, endIndex)
          }

          null -> null
          else -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.InvalidToken(startIndex, endIndex)
          }
        }
      }
    }

    /**
     * Tokenizes a number (either integer or real) starting from the current position.
     *
     * This method handles both integers (e.g., "123") and real numbers with decimal points (e.g., "123.45").
     * Whitespace is allowed between digits and around the decimal point.
     *
     * @param chars the peekable iterator positioned at the start of a number
     * @return a [Token.PositiveInteger], [Token.PositiveRealNumber], or [Token.InvalidToken]
     */
    private fun tokenizeIntegerOrRealNumber(chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()
      val integerPart1 =
        parseInteger(chars)
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
      val integerEndIndex = chars.getRetrievalCount()
      chars.consumeWhitespace()

      return if (chars.peek() == '.') {
        chars.next()
        chars.consumeWhitespace()

        val integerPart2 = parseInteger(chars)
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())

        val doubleValue = "$integerPart1.$integerPart2".toValidDoubleOrNull()
          ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount())
        Token.PositiveRealNumber(doubleValue, startIndex, endIndex = chars.getRetrievalCount())
      } else {
        Token.PositiveInteger(
          integerPart1.toIntOrNull()
            ?: return Token.InvalidToken(startIndex, endIndex = chars.getRetrievalCount()),
          startIndex,
          integerEndIndex
        )
      }
    }

    /**
     * Parses a sequence of digits into a string representation of an integer.
     *
     * @param chars the peekable iterator to parse from
     * @return the parsed integer as a string, or null if no digits were found
     */
    private fun parseInteger(chars: PeekableIterator<Char>): String? {
      val integerBuilder = StringBuilder()
      while (chars.peek() in '0'..'9') {
        integerBuilder.append(chars.next())
      }
      return if (integerBuilder.isNotEmpty()) {
        integerBuilder.toString()
      } else null
    }

    /**
     * Tokenizes a single-character symbol using the provided factory function.
     *
     * @param chars the peekable iterator positioned at the symbol
     * @param factory function that creates the appropriate token given start and end indices
     * @return the token created by the factory function
     */
    private fun tokenizeSymbol(chars: PeekableIterator<Char>, factory: (Int, Int) -> Token): Token {
      val startIndex = chars.getRetrievalCount()
      chars.next()
      val endIndex = chars.getRetrievalCount()
      return factory(startIndex, endIndex)
    }

    /**
     * Tokenizes a unit or SI prefix starting from the current position.
     *
     * The method supports both singular and plural forms, abbreviated forms,
     * and different case conventions where applicable.
     *
     * @param chars the [PeekableIterator] positioned at the start of a unit
     * @return the appropriate [Token] representing the unit or an [Token.InvalidToken]
     */
    private fun tokenizeUnit(input: String, chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()
      while (
        chars.peek()?.let {
          it in 'a'..'z' ||
            it in 'A'..'Z' ||
            it in '2'..'3' // Needed to tokenize units like m2 and m3
        } == true
      ) {
        chars.next()
      }
      return rawUnitToken(input, startIndex, chars.getRetrievalCount())
    }

    /** Represents a token for parsing a number with units. */
    sealed class Token {
      /** The (inclusive) index in the input stream at which point this token begins. */
      abstract val startIndex: Int

      /** The (exclusive) index in the input stream at which point this token ends. */
      abstract val endIndex: Int

      /**
       * Represents a positive integer (i.e. no decimal point, and no negative sign).
       *
       * @property parsedValue the parsed value of the integer
       */
      class PositiveInteger(
        val parsedValue: Int,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /**
       * Represents a positive real number (i.e. contains a decimal point, but no negative sign).
       *
       * @property parsedValue the parsed value of the real number as a [Double]
       */
      class PositiveRealNumber(
        val parsedValue: Double,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a minus sign, e.g. '-'. */
      class MinusSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a multiply sign, e.g. '*'. */
      class MultiplySymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a divide sign, e.g. '/'. */
      class DivideSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents an exponent sign, i.e. '^'. */
      class ExponentiationSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a left parenthesis symbol, i.e. '('. */
      class LeftParenthesisSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a right parenthesis symbol, i.e. ')'. */
      class RightParenthesisSymbol(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents any recognized unit using its original matched input substring. */
      class Unit(
        val unit: String,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents an invalid character that doesn't fit any of the other [Token] types. */
      class InvalidToken(override val startIndex: Int, override val endIndex: Int) : Token()
    }

    /** Converts a string to a [Token.Unit]. */
    private fun rawUnitToken(input: String, startIndex: Int, endIndex: Int): Token.Unit =
      Token.Unit(input.substring(startIndex, endIndex), startIndex, endIndex)

    /**
     * Converts a string to a Double, ensuring the result is finite (not infinite or NaN).
     *
     * @return the parsed Double if valid and finite, null otherwise
     */
    private fun String.toValidDoubleOrNull(): Double? {
      return toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /**
     * Determines if a character is considered whitespace for tokenization purposes.
     *
     * @return true if the character is a space, tab, newline, or carriage return
     */
    private fun Char.isWhitespace(): Boolean = when (this) {
      ' ', '\t', '\n', '\r' -> true
      else -> false
    }

    /**
     * Consumes all consecutive whitespace characters from the iterator.
     *
     * This method advances the iterator past any whitespace characters until
     * a non-whitespace character is encountered or the end is reached.
     */
    private fun PeekableIterator<Char>.consumeWhitespace() {
      while (peek()?.isWhitespace() == true) next()
    }

    /**
     * Verifies that the next characters in the iterator exactly match the expected string.
     *
     * This method is used to validate unit names by checking that the remaining characters
     * in the iterator match the expected unit suffix. It consumes the characters if they match.
     *
     * @param chars the expected string to match
     * @return true if all characters match exactly, false otherwise
     */
    private fun PeekableIterator<Char>.expectNextCharsForUnit(
      chars: String
    ): Boolean {
      for (c in chars) {
        expectNextValue { c } ?: return false
      }
      return true
    }
  }
}
