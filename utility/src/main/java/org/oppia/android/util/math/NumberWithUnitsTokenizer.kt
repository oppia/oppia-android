package org.oppia.android.util.math

import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator
import java.lang.StringBuilder

/**
 * A tokenizer for parsing mathematical expressions containing numbers and units.
 *
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
     * @param input the string to tokenize
     * @return a [Sequence] of [Token] objects representing the parsed input
     */
    fun tokenize(input: String): Sequence<Token> = tokenize(input.toCharArray().asSequence())

    /**
     * Tokenizes a sequence of characters into a sequence of [Token] objects.
     *
     * This method processes characters one by one, identifying tokens based on the first
     * character encountered. It handles whitespace automatically and delegates to specific
     * tokenization methods based on the character type.
     *
     * @param input the sequence of characters to tokenize
     * @return a [Sequence] of [Token] objects
     */
    private fun tokenize(input: Sequence<Char>): Sequence<Token> {
      val chars = input.toPeekableIterator()
      return generateSequence {
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
     * This method consumes consecutive digit characters and allows whitespace between digits.
     * It returns null if no digits are found at the current position.
     *
     * @param chars the peekable iterator to parse from
     * @return the parsed integer as a string, or null if no digits were found
     */
    private fun parseInteger(chars: PeekableIterator<Char>): String? {
      val integerBuilder = StringBuilder()
      while (chars.peek() in '0'..'9') {
        integerBuilder.append(chars.next())
        chars.consumeWhitespace()
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
    private fun tokenizeUnit(chars: PeekableIterator<Char>): Token {
      val startIndex = chars.getRetrievalCount()

      return when (chars.next()) {
        'a' -> {
          when (chars.peek()) {
            'm' -> {
              val token = tokenizeExpectedUnit(
                "ampere",
                startIndex,
                chars
              ) { start, end -> Token.AmpereUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            't' -> {
              tokenizeExpectedUnit(
                "atto",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.ATTO, start, end) }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.ATTO, startIndex, chars.getRetrievalCount())
          }
        }
        'c' -> {
          when (chars.peek()) {
            'a' -> {
              val token = tokenizeExpectedUnit(
                "candela",
                startIndex,
                chars
              ) { start, end -> Token.CandelaUnit(start, end) }

              token
            }
            'c' -> {
              chars.next()
              Token.CcUnit(startIndex, chars.getRetrievalCount())
            }
            'd' -> {
              chars.next()
              Token.CandelaUnit(startIndex, chars.getRetrievalCount())
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
                  chars.next()
                  when (chars.peek()) {
                    't' -> {
                      chars.next()
                      when (chars.peek()) {
                        'i' -> {
                          tokenizeExpectedUnit(
                            "centi",
                            startIndex,
                            chars
                          ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.CENTI, start, end) }
                        }
                        's' -> {
                          chars.next()
                          Token.CentSuffixUnit(startIndex, chars.getRetrievalCount())
                        }
                        else -> Token.CentSuffixUnit(startIndex, chars.getRetrievalCount())
                      }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
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
            else -> Token.SiPrefix(Token.SiPrefixValue.CENTI, startIndex, chars.getRetrievalCount())
          }
        }
        'd' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "da",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.DECA, start, end) }
            }
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'c' -> {
                  chars.next()
                  when (chars.peek()) {
                    'a' -> {
                      tokenizeExpectedUnit(
                        "deca",
                        startIndex,
                        chars
                      ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.DECA, start, end) }
                    }
                    'i' -> {
                      tokenizeExpectedUnit(
                        "deci",
                        startIndex,
                        chars
                      ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.DECI, start, end) }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                'g' -> {
                  chars.next()
                  when (chars.peek()) {
                    'C' -> {
                      chars.next()
                      Token.CelsiusUnit(startIndex, chars.getRetrievalCount())
                    }
                    'r' -> {
                      val token = tokenizeExpectedUnit(
                        "degree",
                        startIndex,
                        chars
                      ) { start, end -> Token.DegreeUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    else -> {
                      Token.DegreeUnit(startIndex, chars.getRetrievalCount())
                    }
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
            else -> Token.SiPrefix(Token.SiPrefixValue.DECI, startIndex, chars.getRetrievalCount())
          }
        }
        'e' -> {
          when (chars.peek()) {
            'x' -> {
              tokenizeExpectedUnit(
                "exa",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.EXA, start, end) }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'f' -> {
          when (chars.peek()) {
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'e' -> {
                  val token = tokenizeExpectedUnit(
                    "feet",
                    startIndex,
                    chars
                  ) { start, end -> Token.FootUnit(start, end) }

                  token
                }
                'm' -> {
                  tokenizeExpectedUnit(
                    "femto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.FEMTO, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
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
              Token.FootUnit(startIndex, chars.getRetrievalCount())
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.FEMTO, startIndex, chars.getRetrievalCount())
          }
        }
        'g' -> {
          when (chars.peek()) {
            'i' -> {
              tokenizeExpectedUnit(
                "Giga",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.GIGA, start, end) }
            }
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

                      if (chars.peek() == 's') chars.next()
                      token
                    }

                    'm' -> {
                      val token = tokenizeExpectedUnit(
                        "gram",
                        startIndex,
                        chars
                      ) { start, end -> Token.GramUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.GrainUnit(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.GramUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'h' -> {
          when (chars.peek()) {
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'r' -> {
                  tokenizeExpectedUnit(
                    "hertz",
                    startIndex,
                    chars
                  ) { start, end -> Token.HertzUnit(start, end) }
                }
                'c' -> {
                  tokenizeExpectedUnit(
                    "hecto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.HECTO, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'o' -> {
              val token = tokenizeExpectedUnit(
                "hour",
                startIndex,
                chars
              ) { start, end -> Token.HourUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            'r' -> {
              chars.next()
              when (chars.peek()) {
                's' -> {
                  chars.next()
                  Token.HourUnit(startIndex, chars.getRetrievalCount())
                }
                else -> Token.HourUnit(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.HourUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'i' -> {
          when (chars.peek()) {
            'n' -> {
              chars.next()

              when (chars.peek()) {
                'c' -> {
                  val token = tokenizeExpectedUnit(
                    "inch",
                    startIndex,
                    chars
                  ) { start, end -> Token.InchUnit(start, end) }

                  if (chars.peek() == 'e') {
                    chars.next()
                    if (chars.peek() == 's') chars.next()
                  }

                  token
                }
                else -> Token.InchUnit(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'j' -> {
          when (chars.peek()) {
            'o' -> {
              val token = tokenizeExpectedUnit(
                "joule",
                startIndex,
                chars
              ) { start, end -> Token.JouleUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'k' -> {
          when (chars.peek()) {
            'e' -> {
              tokenizeExpectedUnit(
                "kelvin",
                startIndex,
                chars
              ) { start, end -> Token.KelvinUnit(start, end) }
            }
            'i' -> {
              tokenizeExpectedUnit(
                "kilo",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.KILO, start, end) }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.KILO, startIndex, chars.getRetrievalCount())
          }
        }
        'l' -> {
          when (chars.peek()) {
            't' -> {
              chars.next()
              Token.LiterUnit(startIndex, chars.getRetrievalCount())
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

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    'r' -> {
                      val token = tokenizeExpectedUnit(
                        "litre",
                        startIndex,
                        chars
                      ) { start, end -> Token.LiterUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.LiterUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'm' -> {
          when (chars.peek()) {
            '2' -> {
              chars.next()
              Token.SquareMeterUnit(startIndex, chars.getRetrievalCount())
            }
            '3' -> {
              chars.next()
              Token.CubicMeterUnit(startIndex, chars.getRetrievalCount())
            }
            'e' -> {
              chars.next()
              when (chars.peek()) {
                't' -> {
                  val token = tokenizeExpectedUnit(
                    "meter",
                    startIndex,
                    chars
                  ) { start, end -> Token.MeterUnit(start, end) }

                  if (chars.peek() == 's') chars.next()
                  token
                }
                'g' -> {
                  tokenizeExpectedUnit(
                    "mega",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.MEGA, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'i' -> {
              chars.next()
              when (chars.peek()) {
                'n' -> {
                  chars.next()
                  when (chars.peek()) {
                    'u' -> {
                      val token = tokenizeExpectedUnit(
                        "minute",
                        startIndex,
                        chars
                      ) { start, end -> Token.MinuteUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    's' -> {
                      chars.next()
                      Token.MinuteUnit(startIndex, chars.getRetrievalCount())
                    }
                    else -> Token.MinuteUnit(startIndex, chars.getRetrievalCount())
                  }
                }
                'l' -> {
                  tokenizeExpectedUnit(
                    "milli",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.MILLI, start, end) }
                }
                'c' -> {
                  tokenizeExpectedUnit(
                    "micro",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.MICRO, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'o' -> {
              chars.next()
              when (chars.peek()) {
                'l' -> {
                  chars.next()
                  when (chars.peek()) {
                    'e' -> {
                      val token = tokenizeExpectedUnit(
                        "mole",
                        startIndex,
                        chars
                      ) { start, end -> Token.MoleUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                    else -> Token.MoleUnit(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.MeterUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'n' -> {
          when (chars.peek()) {
            'e' -> {
              val token = tokenizeExpectedUnit(
                "newton",
                startIndex,
                chars
              ) { start, end -> Token.NewtonUnit(start, end) }

              token
            }
            'a' -> {
              tokenizeExpectedUnit(
                "nano",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.NANO, start, end) }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.NANO, startIndex, chars.getRetrievalCount())
          }
        }
        'o' -> {
          when (chars.peek()) {
            'h' -> {
              val token = tokenizeExpectedUnit(
                "ohm",
                startIndex,
                chars
              ) { start, end -> Token.OhmUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            'u' -> {
              val token = tokenizeExpectedUnit(
                "ounce",
                startIndex,
                chars
              ) { start, end -> Token.OunceUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            'z' -> {
              chars.next()
              Token.OunceUnit(startIndex, chars.getRetrievalCount())
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'p' -> {
          when (chars.peek()) {
            'a' -> {
              val paisaToken = tokenizeExpectedUnit(
                "pais",
                startIndex,
                chars
              ) { start, end -> Token.PaisaSuffixUnit(start, end) }

              when (chars.peek()) {
                'a', 'e' -> {
                  chars.next()
                  paisaToken
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'i' -> {
              tokenizeExpectedUnit(
                "pico",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.PICO, start, end) }
            }
            'e' -> {
              tokenizeExpectedUnit(
                "peta",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.PETA, start, end) }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.PICO, startIndex, chars.getRetrievalCount())
          }
        }
        'r' -> {
          when (chars.peek()) {
            'a' -> {
              chars.next()
              when (chars.peek()) {
                'd' -> {
                  val token = tokenizeExpectedUnit(
                    "rad",
                    startIndex,
                    chars
                  ) { start, end -> Token.RadianUnit(start, end) }

                  if (chars.peek() == 'i') {
                    val radianToken = tokenizeExpectedUnit(
                      "radian",
                      startIndex,
                      chars
                    ) { start, end -> Token.RadianUnit(start, end) }

                    if (chars.peek() == 's') chars.next()
                    radianToken
                  } else {
                    token
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'u' -> {
              val token = tokenizeExpectedUnit(
                "rupee",
                startIndex,
                chars
              ) { start, end -> Token.RupeeSuffixUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
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
                      Token.SquareFootUnit(startIndex, chars.getRetrievalCount())
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
                      Token.SquareYardUnit(startIndex, chars.getRetrievalCount())
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
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'c' -> {
                  chars.next()
                  when (chars.peek()) {
                    'o' -> {
                      val token = tokenizeExpectedUnit(
                        "second",
                        startIndex,
                        chars
                      ) { start, end -> Token.SecondUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }

                    else -> {
                      val token = tokenizeExpectedUnit(
                        "sec",
                        startIndex,
                        chars
                      ) { start, end -> Token.SecondUnit(start, end) }

                      if (chars.peek() == 's') chars.next()
                      token
                    }
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.SecondUnit(startIndex, chars.getRetrievalCount())
          }
        }
        't' -> {
          when (chars.peek()) {
            'e' -> {
              tokenizeExpectedUnit(
                "tera",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.TERA, start, end) }
            }

            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'w' -> {
          when (chars.peek()) {
            'a' -> {
              val token = tokenizeExpectedUnit(
                "watt",
                startIndex,
                chars
              ) { start, end -> Token.WattUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'u' -> {
          Token.SiPrefix(Token.SiPrefixValue.MICRO, startIndex, chars.getRetrievalCount())
        }
        'v' -> {
          when (chars.peek()) {
            'o' -> {
              val token = tokenizeExpectedUnit(
                "volt",
                startIndex,
                chars
              ) { start, end -> Token.VoltUnit(start, end) }

              if (chars.peek() == 's') chars.next()
              token
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

              if (chars.peek() == 's') chars.next()
              token
            }
            'd' -> {
              chars.next()
              Token.YardUnit(startIndex, chars.getRetrievalCount())
            }
            'o' -> {
              chars.next()
              when (chars.peek()) {
                'c' -> {
                  tokenizeExpectedUnit(
                    "yocto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.YOCTO, start, end) }
                }
                't' -> {
                  tokenizeExpectedUnit(
                    "yotta",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.YOTTA, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.YOCTO, startIndex, chars.getRetrievalCount())
          }
        }
        'z' -> {
          when (chars.peek()) {
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'p' -> {
                  tokenizeExpectedUnit(
                    "zepto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.ZEPTO, start, end) }
                }
                't' -> {
                  tokenizeExpectedUnit(
                    "zetta",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix(Token.SiPrefixValue.ZETTA, start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.ZEPTO, startIndex, chars.getRetrievalCount())
          }
        }
        'A' -> {
          when (chars.peek()) {
            // Ampere must be lowercase
            'm' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.AmpereUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'C' -> {
          val token = tokenizeExpectedUnit(
            "Cent",
            startIndex,
            chars
          ) { start, end -> Token.CentSuffixUnit(start, end) }
          if (chars.peek() == 's') chars.next()

          token
        }
        'D' -> {
          val token = tokenizeExpectedUnit(
            "Dollar",
            startIndex,
            chars
          ) { start, end -> Token.DollarSuffixUnit(start, end) }
          if (chars.peek() == 's') chars.next()

          token
        }
        'E' -> Token.SiPrefix(Token.SiPrefixValue.EXA, startIndex, chars.getRetrievalCount())
        'G' -> Token.SiPrefix(Token.SiPrefixValue.GIGA, startIndex, chars.getRetrievalCount())
        'H' -> {
          when (chars.peek()) {
            'z' -> {
              chars.next()
              Token.HertzUnit(startIndex, chars.getRetrievalCount())
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'J' -> {
          when (chars.peek()) {
            // Joule must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.JouleUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'K' -> {
          when (chars.peek()) {
            // Kelvin must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.KelvinUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'L' -> {
          when (chars.peek()) {
            'i', 't' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.LiterUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'M' -> Token.SiPrefix(Token.SiPrefixValue.MEGA, startIndex, chars.getRetrievalCount())
        'N' -> {
          when (chars.peek()) {
            // Newton must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.NewtonUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'P' -> {
          when (chars.peek()) {
            'a' -> {
              chars.next()
              when (chars.peek()) {
                'i' -> {
                  val paisaToken = tokenizeExpectedUnit(
                    "Pais",
                    startIndex,
                    chars
                  ) { start, end -> Token.PaisaSuffixUnit(start, end) }

                  when (chars.peek()) {
                    'a', 'e' -> {
                      chars.next()
                      paisaToken
                    }

                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                else -> Token.PascalUnit(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.PETA, startIndex, chars.getRetrievalCount())
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
              if (chars.peek() == 's') chars.next()

              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'T' -> Token.SiPrefix(Token.SiPrefixValue.TERA, startIndex, chars.getRetrievalCount())
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
        'W' -> {
          when (chars.peek()) {
            // Watt must be lowercase
            'a' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.WattUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'V' -> {
          when (chars.peek()) {
            // Volt must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.VoltUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'Y' -> Token.SiPrefix(Token.SiPrefixValue.YOTTA, startIndex, chars.getRetrievalCount())
        'Z' -> Token.SiPrefix(Token.SiPrefixValue.ZETTA, startIndex, chars.getRetrievalCount())
        else -> {
          Token.InvalidToken(startIndex, chars.getRetrievalCount())
        }
      }
    }

    /**
     * Tokenizes an expected unit by verifying the remaining characters match the expected name.
     *
     * This method is used when the tokenizer has already consumed the first few characters of a unit
     * and needs to verify that the remaining characters match the expected unit name. If the match
     * is successful, it creates a token using the provided factory function; otherwise, it returns
     * an invalid token.
     *
     * @param name the complete expected unit name (e.g., "ampere", "meter", "kilogram")
     * @param startIndex the starting index in the input stream where the unit began
     * @param chars the peekable iterator positioned after the already-consumed characters
     * @param factory function that creates the appropriate token given start and end indices
     * @return the token created by the factory function if successful, otherwise an [Token.InvalidToken]
     */
    private fun tokenizeExpectedUnit(
      name: String,
      startIndex: Int,
      chars: PeekableIterator<Char>,
      factory: (Int, Int) -> Token
    ): Token {
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

      /**
       * Represents an SI prefix (e.g. "kilo", "milli", "mega").
       *
       * @property prefixValue the specific SI prefix this token represents
       */
      class SiPrefix(
        val prefixValue: SiPrefixValue,
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Enumerates all recognized SI prefixes. */
      enum class SiPrefixValue {
        DECA, HECTO, KILO, MEGA, GIGA, TERA, PETA, EXA, ZETTA, YOTTA,
        DECI, CENTI, MILLI, MICRO, NANO, PICO, FEMTO, ATTO, ZEPTO, YOCTO
      }

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
