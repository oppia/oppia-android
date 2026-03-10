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
        'a' -> {
          when (chars.peek()) {
            'm' -> {
              val token = tokenizeExpectedUnit(
                "ampere",
                startIndex,
                chars
              ) { start, end -> Token.AmpereUnit(start, end) }

              if (chars.peek() == 's') chars.next() // amperes
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
              Token.CcUnit(startIndex, chars.getRetrievalCount()) // cc
            }
            'd' -> {
              chars.next()
              Token.CandelaUnit(startIndex, chars.getRetrievalCount()) // cd
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
                          chars.next() // cents
                          Token.CentSuffixUnit(startIndex, chars.getRetrievalCount())
                        }
                        else -> Token.CentSuffixUnit(startIndex, chars.getRetrievalCount()) // cent
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
            else -> Token.SiPrefix(Token.SiPrefixValue.CENTI, startIndex, chars.getRetrievalCount()) // c
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
                      Token.CelsiusUnit(startIndex, chars.getRetrievalCount()) // degC
                    }
                    'r' -> {
                      val token = tokenizeExpectedUnit(
                        "degree",
                        startIndex,
                        chars
                      ) { start, end -> Token.DegreeUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // degrees
                      token
                    }
                    else -> {
                      Token.DegreeUnit(startIndex, chars.getRetrievalCount()) // deg
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
              Token.FootUnit(startIndex, chars.getRetrievalCount()) // "ft"
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

              if (chars.peek() == 's') chars.next() // hours
              token
            }
            'r' -> {
              chars.next()
              when (chars.peek()) {
                's' -> {
                  chars.next()
                  Token.HourUnit(startIndex, chars.getRetrievalCount()) // hrs
                }
                else -> Token.HourUnit(startIndex, chars.getRetrievalCount()) // hr
              }
            }
            else -> Token.HourUnit(startIndex, chars.getRetrievalCount()) // h
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
        'j' -> {
          when (chars.peek()) {
            'o' -> {
              val token = tokenizeExpectedUnit(
                "joule",
                startIndex,
                chars
              ) { start, end -> Token.JouleUnit(start, end) }

              if (chars.peek() == 's') chars.next() // joules
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
            else -> Token.SiPrefix(Token.SiPrefixValue.KILO, startIndex, chars.getRetrievalCount()) // k
          }
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
              chars.next()
              when (chars.peek()) {
                't' -> {
                  val token = tokenizeExpectedUnit(
                    "meter",
                    startIndex,
                    chars
                  ) { start, end -> Token.MeterUnit(start, end) }

                  if (chars.peek() == 's') chars.next() // meters
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

                      if (chars.peek() == 's') chars.next() // minutes
                      token
                    }
                    's' -> {
                      chars.next()
                      Token.MinuteUnit(startIndex, chars.getRetrievalCount()) // mins
                    }
                    else -> Token.MinuteUnit(startIndex, chars.getRetrievalCount()) // min
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

                      if (chars.peek() == 's') chars.next() // moles
                      token
                    }
                    else -> Token.MoleUnit(startIndex, chars.getRetrievalCount()) // mol
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.MeterUnit(startIndex, chars.getRetrievalCount()) // "m"
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

              if (chars.peek() == 's') chars.next() // ohms
              token
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
            'z' -> {
              chars.next()
              Token.OunceUnit(startIndex, chars.getRetrievalCount()) // oz
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
                  chars.next() // Allow for "paisa" & "paise" suffix only.
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

                    if (chars.peek() == 's') chars.next() // radians
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

                      if (chars.peek() == 's') chars.next() // seconds
                      token
                    }

                    else -> {
                      val token = tokenizeExpectedUnit(
                        "sec",
                        startIndex,
                        chars
                      ) { start, end -> Token.SecondUnit(start, end) }

                      if (chars.peek() == 's') chars.next() // secs
                      token
                    }
                  }
                }
                else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
              }
            }
            else -> Token.SecondUnit(startIndex, chars.getRetrievalCount()) // s
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

              if (chars.peek() == 's') chars.next() // watts
              token
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'u' -> {
          Token.SiPrefix(Token.SiPrefixValue.MICRO, startIndex, chars.getRetrievalCount()) // u
        }
        'v' -> {
          when (chars.peek()) {
            'o' -> {
              val token = tokenizeExpectedUnit(
                "volt",
                startIndex,
                chars
              ) { start, end -> Token.VoltUnit(start, end) }

              if (chars.peek() == 's') chars.next() // volts
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

              if (chars.peek() == 's') chars.next() // yards
              token
            }
            'd' -> {
              chars.next()
              Token.YardUnit(startIndex, chars.getRetrievalCount()) // "yd"
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
            else -> Token.SiPrefix(Token.SiPrefixValue.ZEPTO, startIndex, chars.getRetrievalCount()) // z
          }
        }
        'A' -> {
          when (chars.peek()) {
            // Ampere must be lowercase
            'm' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.AmpereUnit(startIndex, chars.getRetrievalCount()) // A
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
        'E' -> Token.SiPrefix(Token.SiPrefixValue.EXA, startIndex, chars.getRetrievalCount()) // E
        'G' -> Token.SiPrefix(Token.SiPrefixValue.GIGA, startIndex, chars.getRetrievalCount()) // G
        'H' -> {
          when (chars.peek()) {
            'z' -> {
              chars.next()
              Token.HertzUnit(startIndex, chars.getRetrievalCount()) // Hz
            }
            else -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
          }
        }
        'J' -> {
          when (chars.peek()) {
            // Joule must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.JouleUnit(startIndex, chars.getRetrievalCount()) // J
          }
        }
        'K' -> {
          when (chars.peek()) {
            // Kelvin must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.KelvinUnit(startIndex, chars.getRetrievalCount()) // K
          }
        }
        'L' -> {
          when (chars.peek()) {
            'i', 't' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.LiterUnit(startIndex, chars.getRetrievalCount())
          }
        }
        'M' -> Token.SiPrefix(Token.SiPrefixValue.MEGA, startIndex, chars.getRetrievalCount()) // M
        'N' -> {
          when (chars.peek()) {
            // Newton must be lowercase
            'e' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.NewtonUnit(startIndex, chars.getRetrievalCount()) // N
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
                else -> Token.PascalUnit(startIndex, chars.getRetrievalCount()) // Pa
              }
            }
            else -> Token.SiPrefix(Token.SiPrefixValue.PETA, startIndex, chars.getRetrievalCount()) // P
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
        'T' -> Token.SiPrefix(Token.SiPrefixValue.TERA, startIndex, chars.getRetrievalCount()) // T
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
            else -> Token.WattUnit(startIndex, chars.getRetrievalCount()) // W
          }
        }
        'V' -> {
          when (chars.peek()) {
            // Volt must be lowercase
            'o' -> Token.InvalidToken(startIndex, chars.getRetrievalCount())
            else -> Token.VoltUnit(startIndex, chars.getRetrievalCount()) // V
          }
        }
        'Y' -> Token.SiPrefix(Token.SiPrefixValue.YOTTA, startIndex, chars.getRetrievalCount()) // Y
        'Z' -> Token.SiPrefix(Token.SiPrefixValue.ZETTA, startIndex, chars.getRetrievalCount()) // Z
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
