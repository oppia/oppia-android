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
          in 'a'..'z', in 'A'..'Z' -> tokenizeUnit(chars)
          '-', '−', '–' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.MinusSymbol(startIndex, endIndex)
          }
          '/' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.DivideSymbol(startIndex, endIndex)
          }
          '₹' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.RupeePrefixUnit(startIndex, endIndex)
          }
          '$' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.DollarPrefixUnit(startIndex, endIndex)
          }
          '¢' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.CentSuffixUnit(startIndex, endIndex)
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

    private fun tokenizeUnit(chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()

      return when (chars.next()) {
        'c' -> {
          when (chars.peek()) {
            'c' -> {
              chars.next()
              Token.CcUnit(startIndex, chars.getRetrievalCount()) // cc
            }
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'l' -> {
                  val token = tokenizeExpectedUnit(
                    "celsius",
                    startIndex,
                    chars
                  ) { start, end -> Token.CelsiusUnit(start, end) }

                  token
                }

                'n' -> {
                  val token = tokenizeExpectedUnit(
                    "cent",
                    startIndex,
                    chars
                  ) { start, end -> Token.CentSuffixUnit(start, end) }

                  if (chars.peek() == 's') chars.next()
                  token
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'u' -> {
              chars.next()
              when (chars.peek()) {
                'i' -> {
                  tokenizeExpectedUnit(
                    "cuin",
                    startIndex,
                    chars
                  ) { start, end -> Token.CubicInchUnit(start, end) }
                }
                'f' -> {
                  tokenizeExpectedUnit(
                    "cuft",
                    startIndex,
                    chars
                  ) { start, end -> Token.CubicFootUnit(start, end) }
                }
                'y' -> {
                  tokenizeExpectedUnit(
                    "cuyd",
                    startIndex,
                    chars
                  ) { start, end -> Token.CubicYardUnit(start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'd' -> {
          when (chars.peek()) {
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'g' -> {
                  chars.next()
                  when (chars.peek()) {
                    'C' -> {
                      chars.next()
                      Token.CelsiusUnit(startIndex, chars.getRetrievalCount()) // degC
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'o' -> {
              val token = tokenizeExpectedUnit(
                "dollar",
                startIndex,
                chars
              ) { start, end -> Token.DollarSuffixUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'f' -> {
          when (chars.peek()) {
            'e' -> {
              val token = tokenizeExpectedUnit(
                "feet",
                startIndex,
                chars
              ) { start, end -> Token.FootUnit(start, end) }

              token
            }
            'o' -> {
              val token = tokenizeExpectedUnit(
                "foot",
                startIndex,
                chars
              ) { start, end -> Token.FootUnit(start, end) }

              token
            }
            't' -> {
              chars.next()
              Token.FootUnit(startIndex, chars.getRetrievalCount()) // "ft"
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'g' -> {
          when (chars.peek()) {
            'r' -> {
              chars.next()

              when (chars.peek()) {
                'a' -> {
                  chars.next()
                  when (chars.peek()) {
                    'i' -> {

                      val token = tokenizeExpectedUnit(
                        "grain",
                        startIndex,
                        chars
                      ) { start, end -> Token.GrainUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // grains
                      token
                    }

                    'm' -> {
                      val token = tokenizeExpectedUnit(
                        "gram",
                        startIndex,
                        chars
                      ) { start, end -> Token.GramUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // grams
                      token
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.GrainUnit(startIndex, chars.getRetrievalCount())
              }
            }

            else -> Token.GramUnit(startIndex, chars.getRetrievalCount()) // g
          }
        }
        'i' -> {
          when (chars.peek()) {
            'n' -> {
              chars.next() // consume 'n'

              when (chars.peek()) {
                'c' -> {
                  val token = tokenizeExpectedUnit(
                    "inch",
                    startIndex,
                    chars
                  ) { start, end -> Token.InchUnit(start, end) }

                  if (chars.peek() == 'e') { // inches
                    chars.next()
                    if (chars.peek() == 's') chars.next()
                  }

                  token
                }
                else -> Token.InchUnit(startIndex, chars.getRetrievalCount()) // "in"
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'k' -> {
          tokenizeExpectedUnit(
            "kelvin",
            startIndex,
            chars
          ) { start, end -> Token.KelvinUnit(start, end) }
        }
        'l' -> {
          when (chars.peek()) {
            't' -> {
              chars.next()
              Token.LiterUnit(startIndex, chars.getRetrievalCount()) // lt
            }
            'i' -> {
              chars.next()
              when (chars.peek()) {
                't' -> {
                  chars.next()
                  when (chars.peek()) {
                    'e' -> {
                      val token = tokenizeExpectedUnit(
                        "liter",
                        startIndex,
                        chars
                      ) { start, end -> Token.LiterUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // liters
                      token
                    }
                    'r' -> {
                      val token = tokenizeExpectedUnit(
                        "litre",
                        startIndex,
                        chars
                      ) { start, end -> Token.LiterUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // litres
                      token
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.LiterUnit(startIndex, chars.getRetrievalCount()) // l
          }
        }
        'm' -> {
          when (chars.peek()) {
            '2' -> {
              chars.next()
              Token.SquareMeterUnit(startIndex, chars.getRetrievalCount()) // m2
            }
            '3' -> {
              chars.next()
              Token.CubicMeterUnit(startIndex, chars.getRetrievalCount()) // m3
            }
            'e' -> {
              val token = tokenizeExpectedUnit(
                "meter",
                startIndex,
                chars
              ) { start, end -> Token.MeterUnit(start, end) }

              if (chars.peek() == 's') chars.next() // meters
              token
            }
            else -> Token.MeterUnit(startIndex, chars.getRetrievalCount()) // "m"
          }
        }
        'o' -> {
          when (chars.peek()) {
            'z' -> {
              chars.next()
              Token.OunceUnit(startIndex, chars.getRetrievalCount()) // oz
            }

            'u' -> {
              val token = tokenizeExpectedUnit(
                "ounce",
                startIndex,
                chars
              ) { start, end -> Token.OunceUnit(start, end) }

              if (chars.peek() == 's') chars.next() // ounces
              token
            }

            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'p' -> {
          val paisaToken = tokenizeExpectedUnit(
            "pais",
            startIndex,
            chars
          ) { start, end -> Token.PaisaSuffixUnit(start, end) }

          when (chars.peek()) {
            'a', 'e' -> {
              chars.next() // Allow for "paisa" & "paise" suffix only.
              paisaToken
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'r' -> {
          val token = tokenizeExpectedUnit(
            "rupee",
            startIndex,
            chars
          ) { start, end -> Token.RupeeSuffixUnit(start, end) }
          if (chars.peek() == 's') chars.next() // Allow for plural suffix (rupees).

          token
        }
        's' -> {
          when (chars.peek()) {
            'q' -> {
              chars.next()
              when (chars.peek()) {
                'f' -> {
                  chars.next()
                  when (chars.peek()) {
                    't' -> {
                      chars.next()
                      Token.SquareFootUnit(startIndex, chars.getRetrievalCount()) // sqft
                    }
                    'e' -> {
                      tokenizeExpectedUnit(
                        "sqfeet",
                        startIndex,
                        chars
                      ) { start, end -> Token.SquareFootUnit(start, end) }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                'i' -> {
                  tokenizeExpectedUnit(
                    "sqinch",
                    startIndex,
                    chars
                  ) { start, end -> Token.SquareInchUnit(start, end) }
                }
                'y' -> {
                  chars.next()
                  when (chars.peek()) {
                    'd' -> {
                      chars.next()
                      Token.SquareYardUnit(startIndex, chars.getRetrievalCount()) // sqyd
                    }
                    'a' -> {
                      tokenizeExpectedUnit(
                        "sqyard",
                        startIndex,
                        chars
                      ) { start, end -> Token.SquareYardUnit(start, end) }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'y' -> {
          when (chars.peek()) {
            'a' -> {
              val token = tokenizeExpectedUnit(
                "yard",
                startIndex,
                chars
              ) { start, end -> Token.YardUnit(start, end) }

              if (chars.peek() == 's') chars.next() // yards
              token
            }
            'd' -> {
              chars.next()
              Token.YardUnit(startIndex, chars.getRetrievalCount()) // "yd"
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'C' -> {
          val token = tokenizeExpectedUnit(
            "Cent",
            startIndex,
            chars
          ) { start, end -> Token.CentSuffixUnit(start, end) }
          if (chars.peek() == 's') chars.next() // Allow for plural suffix (Cents).

          token
        }
        'D' -> {
          val token = tokenizeExpectedUnit(
            "Dollar",
            startIndex,
            chars
          ) { start, end -> Token.DollarSuffixUnit(start, end) }
          if (chars.peek() == 's') chars.next() // Allow for plural suffix (Dollars).

          token
        }
        'K' -> {
          when (chars.peek()) {
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount()) // Kelvin must be lowercase
            else -> Token.KelvinUnit(startIndex, chars.getRetrievalCount()) // K
          }
        }
        'L' -> {
          when (chars.peek()) {
            'i', 't' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.LiterUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'P' -> {
          val paisaToken = tokenizeExpectedUnit(
            "Pais",
            startIndex,
            chars
          ) { start, end -> Token.PaisaSuffixUnit(start, end) }

          when (chars.peek()) {
            'a', 'e' -> {
              chars.next() // Allow for "Paisa" & "Paise" suffix only.
              paisaToken
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'R' -> {
          when (chars.next()) {
            's' -> {
              Token.RupeePrefixUnit(startIndex, chars.getRetrievalCount())
            }
            'u' -> {
              val token = tokenizeExpectedUnit(
                "Rupee",
                startIndex,
                chars
              ) { start, end -> Token.RupeeSuffixUnit(start, end) }
              if (chars.peek() == 's') chars.next() // Allow for plural suffix (Rupees).

              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'U' -> {
          when (chars.next()) {
            'S' -> {
              when (chars.next()) {
                'D' -> Token.DollarSuffixUnit(startIndex, chars.getRetrievalCount())
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        else -> {
          Token.InvalidToken(startIndex, chars.getRetrievalCount())
        }
      }
    }

    private fun tokenizeExpectedUnit(
      name: String,
      startIndex: Int,
      chars: PeekableIterator<Char>,
      factory: (Int, Int) -> Token
    ): Token {
      // Only check the remaining characters in the unit name, since the first few characters has already been scanned.
      val remainingUnit = name.substring(chars.getRetrievalCount() - startIndex)
      return if (chars.expectNextCharsForUnit(remainingUnit)) {
        factory(startIndex, chars.getRetrievalCount())
      } else {
        Token.InvalidToken(startIndex, chars.getRetrievalCount())
      }
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

      class DollarPrefixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RupeePrefixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class DollarSuffixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class CentSuffixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class RupeeSuffixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      class PaisaSuffixUnit(
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
