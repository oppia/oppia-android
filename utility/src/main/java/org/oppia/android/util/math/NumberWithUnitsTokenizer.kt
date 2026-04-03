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
          '₹' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.RupeePrefixUnit(startIndex, endIndex)
          }
          '$' -> tokenizeSymbol(chars) { startIndex, endIndex ->
            Token.DollarPrefixUnit(startIndex, endIndex)
          }
          '¢' -> tokenizeSymbol(chars) { startIndex, endIndex ->
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

      return when (chars.next()) {
        'a' -> {
          when (chars.peek()) {
            'm' -> {
              tokenizeExpectedUnit(
                "ampere",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }

            't' -> {
              tokenizeExpectedUnit(
                "atto",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("a", start, end) }
            }

            else -> Token.SiPrefix("a", startIndex, chars.getRetrievalCount())
          }
        }
        'c' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "candela",
                startIndex,
                chars
              ) { start, end -> rawUnitToken(input, start, end) }
            }
            'c' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            'd' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            'e' -> {
              chars.next()
              when (chars.peek()) {
                'l' -> {
                  tokenizeExpectedUnit(
                    "celsius",
                    startIndex,
                    chars
                  ) { start, end -> rawUnitToken(input, start, end) }
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
                          ) { start, end -> Token.SiPrefix("c", start, end) }
                        }

                        's' -> {
                          chars.next()
                          rawUnitToken(input, startIndex, chars.getRetrievalCount())
                        }

                        else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
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
                  ) { start, end -> rawUnitToken(input, start, end) }
                }
                'f' -> {
                  tokenizeExpectedUnit(
                    "cuft",
                    startIndex,
                    chars
                  ) { start, end -> rawUnitToken(input, start, end) }
                }

                'y' -> {
                  tokenizeExpectedUnit(
                    "cuyd",
                    startIndex,
                    chars
                  ) { start, end -> rawUnitToken(input, start, end) }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> Token.SiPrefix("c", startIndex, chars.getRetrievalCount())
          }
        }
        'd' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "da",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("da", start, end) }
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
                      ) { start, end -> Token.SiPrefix("da", start, end) }
                    }
                    'i' -> {
                      tokenizeExpectedUnit(
                        "deci",
                        startIndex,
                        chars
                      ) { start, end -> Token.SiPrefix("d", start, end) }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                'g' -> {
                  chars.next()
                  when (chars.peek()) {
                    'C' -> {
                      chars.next()
                      rawUnitToken(input, startIndex, chars.getRetrievalCount())
                    }
                    'r' -> {
                      tokenizeExpectedUnit(
                        "degree",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }
                    else -> {
                      rawUnitToken(input, startIndex, chars.getRetrievalCount())
                    }
                  }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            'o' -> {
              tokenizeExpectedUnit(
                "dollar",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }

            else -> Token.SiPrefix("d", startIndex, chars.getRetrievalCount())
          }
        }
        'e' -> {
          when (chars.peek()) {
            'x' -> {
              tokenizeExpectedUnit(
                "exa",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("E", start, end) }
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
                  tokenizeExpectedUnit(
                    "feet",
                    startIndex,
                    chars
                  ) { start, end -> rawUnitToken(input, start, end) }
                }
                'm' -> {
                  tokenizeExpectedUnit(
                    "femto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("f", start, end) }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'o' -> {
              tokenizeExpectedUnit(
                "foot",
                startIndex,
                chars
              ) { start, end -> rawUnitToken(input, start, end) }
            }

            't' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }

            else -> Token.SiPrefix("f", startIndex, chars.getRetrievalCount())
          }
        }
        'g' -> {
          when (chars.peek()) {
            'i' -> {
              tokenizeExpectedUnit(
                "Giga",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("G", start, end) }
            }
            'r' -> {
              chars.next()

              when (chars.peek()) {
                'a' -> {
                  chars.next()
                  when (chars.peek()) {
                    'i' -> {
                      tokenizeExpectedUnit(
                        "grain",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    'm' -> {
                      tokenizeExpectedUnit(
                        "gram",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }

                else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
              }
            }

            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
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
                  ) { start, end -> rawUnitToken(input, start, end) }
                }
                'c' -> {
                  tokenizeExpectedUnit(
                    "hecto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("h", start, end) }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            'o' -> {
              tokenizeExpectedUnit(
                "hour",
                startIndex,
                chars
              ) { start, end ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, end)
              }
            }

            'r' -> {
              chars.next()
              when (chars.peek()) {
                's' -> {
                  chars.next()
                  rawUnitToken(input, startIndex, chars.getRetrievalCount())
                }

                else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
              }
            }

            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        'i' -> {
          when (chars.peek()) {
            'n' -> {
              chars.next()

              when (chars.peek()) {
                'c' -> {
                  tokenizeExpectedUnit(
                    "inch",
                    startIndex,
                    chars
                  ) { start, _ ->
                    if (chars.peek() == 'e') {
                      chars.next()
                      if (chars.peek() == 's') chars.next()
                    }
                    rawUnitToken(input, start, chars.getRetrievalCount())
                  }
                }

                else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'j' -> {
          when (chars.peek()) {
            'o' -> {
              tokenizeExpectedUnit(
                "joule",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
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
              ) { start, end -> rawUnitToken(input, start, end) }
            }

            'i' -> {
              tokenizeExpectedUnit(
                "kilo",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("k", start, end) }
            }

            else -> Token.SiPrefix("k", startIndex, chars.getRetrievalCount())
          }
        }
        'l' -> {
          when (chars.peek()) {
            't' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            'i' -> {
              chars.next()
              when (chars.peek()) {
                't' -> {
                  chars.next()
                  when (chars.peek()) {
                    'e' -> {
                      tokenizeExpectedUnit(
                        "liter",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    'r' -> {
                      tokenizeExpectedUnit(
                        "litre",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        'm' -> {
          when (chars.peek()) {
            '2' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            '3' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            'e' -> {
              chars.next()
              when (chars.peek()) {
                't' -> {
                  tokenizeExpectedUnit(
                    "meter",
                    startIndex,
                    chars
                  ) { start, _ ->
                    if (chars.peek() == 's') chars.next()
                    rawUnitToken(input, start, chars.getRetrievalCount())
                  }
                }
                'g' -> {
                  tokenizeExpectedUnit(
                    "mega",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("M", start, end) }
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
                      tokenizeExpectedUnit(
                        "minute",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    's' -> {
                      chars.next()
                      rawUnitToken(input, startIndex, chars.getRetrievalCount())
                    }

                    else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
                  }
                }
                'l' -> {
                  tokenizeExpectedUnit(
                    "milli",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("m", start, end) }
                }
                'c' -> {
                  tokenizeExpectedUnit(
                    "micro",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("u", start, end) }
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
                      tokenizeExpectedUnit(
                        "mole",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
                  }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        'n' -> {
          when (chars.peek()) {
            'e' -> {
              tokenizeExpectedUnit(
                "newton",
                startIndex,
                chars
              ) { start, end -> rawUnitToken(input, start, end) }
            }

            'a' -> {
              tokenizeExpectedUnit(
                "nano",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("n", start, end) }
            }

            else -> Token.SiPrefix("n", startIndex, chars.getRetrievalCount())
          }
        }
        'o' -> {
          when (chars.peek()) {
            'h' -> {
              tokenizeExpectedUnit(
                "ohm",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }
            'u' -> {
              tokenizeExpectedUnit(
                "ounce",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }
            'z' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'p' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "pais",
                startIndex,
                chars
              ) { start, _ ->
                when (chars.peek()) {
                  'a', 'e' -> {
                    chars.next()
                    rawUnitToken(input, start, chars.getRetrievalCount())
                  }

                  else -> Token.InvalidToken(start, chars.getRetrievalCount())
                }
              }
            }
            'i' -> {
              tokenizeExpectedUnit(
                "pico",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("p", start, end) }
            }

            'e' -> {
              tokenizeExpectedUnit(
                "peta",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("P", start, end) }
            }

            else -> Token.SiPrefix("p", startIndex, chars.getRetrievalCount())
          }
        }
        'r' -> {
          when (chars.peek()) {
            'a' -> {
              chars.next()
              when (chars.peek()) {
                'd' -> {
                  tokenizeExpectedUnit(
                    "rad",
                    startIndex,
                    chars
                  ) { radStart, end ->
                    if (chars.peek() == 'i') {
                      tokenizeExpectedUnit(
                        "radian",
                        startIndex,
                        chars
                      ) { radianStart, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, radianStart, chars.getRetrievalCount())
                      }
                    } else {
                      rawUnitToken(input, radStart, end)
                    }
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            'u' -> {
              tokenizeExpectedUnit(
                "rupee",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
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
                      rawUnitToken(input, startIndex, chars.getRetrievalCount())
                    }
                    'e' -> {
                      tokenizeExpectedUnit(
                        "sqfeet",
                        startIndex,
                        chars
                      ) { start, end -> rawUnitToken(input, start, end) }
                    }
                    else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
                  }
                }
                'i' -> {
                  tokenizeExpectedUnit(
                    "sqinch",
                    startIndex,
                    chars
                  ) { start, end -> rawUnitToken(input, start, end) }
                }
                'y' -> {
                  chars.next()
                  when (chars.peek()) {
                    'd' -> {
                      chars.next()
                      rawUnitToken(input, startIndex, chars.getRetrievalCount())
                    }
                    'a' -> {
                      tokenizeExpectedUnit(
                        "sqyard",
                        startIndex,
                        chars
                      ) { start, end -> rawUnitToken(input, start, end) }
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
                      tokenizeExpectedUnit(
                        "second",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }

                    else -> {
                      tokenizeExpectedUnit(
                        "sec",
                        startIndex,
                        chars
                      ) { start, _ ->
                        if (chars.peek() == 's') chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }
                    }
                  }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        't' -> {
          when (chars.peek()) {
            'e' -> {
              tokenizeExpectedUnit(
                "tera",
                startIndex,
                chars
              ) { start, end -> Token.SiPrefix("T", start, end) }
            }

            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'w' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "watt",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'u' -> {
          Token.SiPrefix("u", startIndex, chars.getRetrievalCount())
        }
        'v' -> {
          when (chars.peek()) {
            'o' -> {
              tokenizeExpectedUnit(
                "volt",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'y' -> {
          when (chars.peek()) {
            'a' -> {
              tokenizeExpectedUnit(
                "yard",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }
            'd' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }
            'o' -> {
              chars.next()
              when (chars.peek()) {
                'c' -> {
                  tokenizeExpectedUnit(
                    "yocto",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("y", start, end) }
                }

                't' -> {
                  tokenizeExpectedUnit(
                    "yotta",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("Y", start, end) }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> Token.SiPrefix("y", startIndex, chars.getRetrievalCount())
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
                  ) { start, end -> Token.SiPrefix("z", start, end) }
                }

                't' -> {
                  tokenizeExpectedUnit(
                    "zetta",
                    startIndex,
                    chars
                  ) { start, end -> Token.SiPrefix("Z", start, end) }
                }

                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }

            else -> Token.SiPrefix("z", startIndex, chars.getRetrievalCount())
          }
        }
        'A' -> {
          when (chars.peek()) {
            // Ampere must be lowercase
            'm' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        'C' -> {
          tokenizeExpectedUnit(
            "Cent",
            startIndex,
            chars
          ) { start, _ ->
            if (chars.peek() == 's') chars.next()
            rawUnitToken(input, start, chars.getRetrievalCount())
          }
        }

        'D' -> {
          tokenizeExpectedUnit(
            "Dollar",
            startIndex,
            chars
          ) { start, _ ->
            if (chars.peek() == 's') chars.next()
            rawUnitToken(input, start, chars.getRetrievalCount())
          }
        }

        'E' -> Token.SiPrefix("E", startIndex, chars.getRetrievalCount())
        'G' -> Token.SiPrefix("G", startIndex, chars.getRetrievalCount())
        'H' -> {
          when (chars.peek()) {
            'z' -> {
              chars.next()
              rawUnitToken(input, startIndex, chars.getRetrievalCount())
            }

            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }

        'J' -> {
          when (chars.peek()) {
            // Joule must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }
        'K' -> {
          when (chars.peek()) {
            // Kelvin must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }

        'L' -> {
          when (chars.peek()) {
            'i', 't' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }

        'M' -> Token.SiPrefix("M", startIndex, chars.getRetrievalCount())
        'N' -> {
          when (chars.peek()) {
            // Newton must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }

        'P' -> {
          when (chars.peek()) {
            'a' -> {
              chars.next()
              when (chars.peek()) {
                'i' -> {
                  tokenizeExpectedUnit(
                    "Pais",
                    startIndex,
                    chars
                  ) { start, _ ->
                    when (chars.peek()) {
                      'a', 'e' -> {
                        chars.next()
                        rawUnitToken(input, start, chars.getRetrievalCount())
                      }

                      else -> Token.InvalidToken(start, chars.getRetrievalCount())
                    }
                  }
                }

                else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
              }
            }

            else -> Token.SiPrefix("P", startIndex, chars.getRetrievalCount())
          }
        }
        'R' -> {
          when (chars.next()) {
            's' -> {
              Token.RupeePrefixUnit(startIndex, chars.getRetrievalCount())
            }
            'u' -> {
              tokenizeExpectedUnit(
                "Rupee",
                startIndex,
                chars
              ) { start, _ ->
                if (chars.peek() == 's') chars.next()
                rawUnitToken(input, start, chars.getRetrievalCount())
              }
            }

            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }

        'T' -> Token.SiPrefix("T", startIndex, chars.getRetrievalCount())
        'U' -> {
          when (chars.next()) {
            'S' -> {
              when (chars.next()) {
                'D' -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
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
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }

        'V' -> {
          when (chars.peek()) {
            // Volt must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> rawUnitToken(input, startIndex, chars.getRetrievalCount())
          }
        }

        'Y' -> Token.SiPrefix("Y", startIndex, chars.getRetrievalCount())
        'Z' -> Token.SiPrefix("Z", startIndex, chars.getRetrievalCount())
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

      /** Represents a dollar currency prefix, e.g. '$'. */
      class DollarPrefixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /** Represents a rupee currency prefix, e.g. 'Rs' or '₹'. */
      class RupeePrefixUnit(
        override val startIndex: Int,
        override val endIndex: Int
      ) : Token()

      /**
       * Represents an SI prefix (e.g. "k", "M", "m").
       *
       * @property prefix the symbolic SI prefix representation used by parser composition
       */
      class SiPrefix(
        val prefix: String,
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
