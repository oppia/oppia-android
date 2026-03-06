package org.oppia.android.util.math

class NumberWithUnitsTokenizer private constructor() {
  companion object {
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
  }
}
