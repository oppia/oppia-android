package org.oppia.android.util.math

import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator
import java.lang.StringBuilder

class NumberWithUnitsTokenizer private constructor() {
  companion object {
    fun tokenize(input: String): Sequence<Token> = tokenize(input.toCharArray().asSequence())

    private fun tokenize(input: Sequence<Char>): Sequence<Token> {
      val chars = input.toPeekableIterator()
      return generateSequence {
        // Consume any whitespace that might precede a valid token.
        chars.consumeWhitespace()

        when (chars.peek()) {
          in '0'..'9' -> tokenizeIntegerOrRealNumber(chars)
          '-', '−', '–' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MinusSymbol(startIndex, endIndex)
          }
          null -> null
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
      val integerEndIndex = chars.getRetrievalCount() // The end index for integers.
      chars.consumeWhitespace() // Whitespace is allowed between digits and the '.'.
      return if (chars.peek() == '.') {
        chars.next() // Parse the "." since it will be re-added later.
        chars.consumeWhitespace() // Whitespace is allowed between the '.' and following digits.

        // Another integer must follow the ".".
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

    private fun parseInteger(chars: PeekableIterator<Char>): String? {
      val integerBuilder = StringBuilder()
      while (chars.peek() in '0'..'9') {
        integerBuilder.append(chars.next())
        chars.consumeWhitespace() // Whitespace is allowed between digits.
      }
      return if (integerBuilder.isNotEmpty()) {
        integerBuilder.toString()
      } else null // Failed to parse; no digits.
    }

    private fun tokenizeSymbol(chars: PeekableIterator<Char>, factory: (Int, Int) -> Token): Token {
      val startIndex = chars.getRetrievalCount()
      chars.next() // Parse the symbol.
      val endIndex = chars.getRetrievalCount()
      return factory(startIndex, endIndex)
    }

    /** Represents a token that may be encountered during tokenization. */
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

      class DollarPrefix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RupeePrefix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class DollarSuffix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CentSuffix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RupeeSuffix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class PaisaSuffix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SiPrefix(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class MeterUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class InchUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class FootUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class YardUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class GramUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class GrainUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class OunceUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SquareMeterUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SquareInchUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SquareFootUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SquareYardUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CubicMeterUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class LiterUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CcUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CubicInchUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CubicFootUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CubicYardUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class KelvinUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CelsiusUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RadianUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class DegreeUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class SecondUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class MinuteUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class HourUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class HertzUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class MoleUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CandelaUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class NewtonUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class JouleUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class WattUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class PascalUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class AmpereUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class VoltUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class OhmUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents an invalid character that doesn't fit any of the other [Token] types. */
      class InvalidToken(override val startIndex: Int, override val endIndex: Int) : Token()
    }

    private fun String.toValidDoubleOrNull(): Double? {
      return toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun Char.isWhitespace(): Boolean = when (this) {
      ' ', '\t', '\n', '\r' -> true
      else -> false
    }

    private fun PeekableIterator<Char>.consumeWhitespace() {
      while (peek()?.isWhitespace() == true) next()
    }
  }
}
