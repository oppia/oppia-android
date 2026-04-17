package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.NumberUnitExpression
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.app.model.PrefixValueExpression
import org.oppia.android.app.model.PrefixValueSuffixExpression
import org.oppia.android.app.model.ValueSuffixExpression
import org.oppia.android.util.extensions.normalizeWhitespace
import org.oppia.android.util.math.NumberWithUnitsTokenizer.Companion.Token
import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator

/**
 * Parser for number-with-units expressions.
 *
 * For the formal grammar specification, refer to the doc:
 * https://docs.google.com/document/d/1PF1LdzBUwNO3tSqucCoMdYpPOWUySI3yS4m_knfLdtE.
 */
class NumberWithUnitsParser private constructor(
  private val rawExpression: String,
  private val tokens: PeekableIterator<Token>
) {
  /**
   * Grammar:
   * ```
   * number_with_units = prefix_formatted_value | suffix_formatted_value ;
   * ```
   */
  private fun parseNumberWithUnits(): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
    if (!tokens.hasNext()) {
      return NumberWithUnitsParsingError.EmptyExpressionError.toFailure()
    }
    val nextToken = tokens.peek()
    val result = when {
      isCurrencyPrefixToken(nextToken) -> parsePrefixFormattedValue()
      nextToken is Token.PositiveInteger ||
        nextToken is Token.PositiveRealNumber ||
        nextToken is Token.MinusSymbol -> parseSuffixFormattedValue()

      nextToken is Token.InvalidToken -> {
        val token = tokens.next()
        NumberWithUnitsParsingError.InvalidTokenError(
          rawExpression.substring(token.startIndex, token.endIndex)
        ).toFailure()
      }

      else -> NumberWithUnitsParsingError.NumberExpectedError.toFailure()
    }

    // Ensure all tokens were consumed.
    return result.maybeFail {
      if (tokens.hasNext()) {
        NumberWithUnitsParsingError.TrailingTokensError
      } else null
    }
  }

  /**
   * Grammar:
   * ```
   * prefix_formatted_value = currency_unit_prefix , number , [ compound_unit ] ;
   * ```
   */
  private fun parsePrefixFormattedValue(): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
    val prefixUnit = parseCurrencyPrefixUnit()
      ?: return NumberWithUnitsParsingError.GenericError.toFailure()
    val expressionBuilder = NumberWithUnitsExpression.newBuilder()

    return parseNumber(expressionBuilder).flatMap {
      if (isAtSuffixUnit()) {
        parseCompoundUnit().maybeFail { suffixUnits ->
          // Check for duplicate currency: a currency prefix was already added, so if any suffix
          // unit is also a currency unit the expression is ambiguous (e.g. "$5 dollars").
          if (suffixUnits.any { isCurrencyUnit(it.unit) }) {
            NumberWithUnitsParsingError.DuplicateCurrencyError
          } else null
        }.map { suffixUnits ->
          expressionBuilder.prefixValueSuffixExpression =
            PrefixValueSuffixExpression.newBuilder().apply {
              addPrefixUnits(prefixUnit)
              addAllSuffixUnits(suffixUnits)
            }.build()
          expressionBuilder.build()
        }
      } else {
        expressionBuilder.prefixValueExpression = PrefixValueExpression.newBuilder().apply {
          addPrefixUnits(prefixUnit)
        }.build()
        NumberWithUnitsParsingResult.Success(expressionBuilder.build())
      }
    }
  }

  /**
   * Grammar:
   * ```
   * suffix_formatted_value = number , compound_unit ;
   * ```
   */
  private fun parseSuffixFormattedValue(): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
    val expressionBuilder = NumberWithUnitsExpression.newBuilder()
    return parseNumber(expressionBuilder).flatMap {
      // compound_unit is required
      if (!isAtSuffixUnit()) {
        NumberWithUnitsParsingError.UnitExpectedError.toFailure()
      } else {
        parseCompoundUnit().map { suffixUnits ->
          expressionBuilder.apply {
            valueSuffixExpression = ValueSuffixExpression.newBuilder().apply {
              addAllSuffixUnits(suffixUnits)
            }.build()
          }.build()
        }
      }
    }
  }

  /**
   * Grammar:
   * ```
   * negatable_number = [ "-" ] , number ;
   * number = positive_integer , [ ( "/" , positive_integer ) | ( "." , positive_integer ) ] ;
   * ```
   *
   * Notes that [Token.PositiveRealNumber] is already tokenized as a single token, so decimal
   * parsing does not happen here. `integer/integer` is always interpreted as a fractional numeric
   * literal at this stage. Unit division (for example `m/s`) is parsed later as part of
   * `compound_unit`.
   */
  private fun parseNumber(
    expressionBuilder: NumberWithUnitsExpression.Builder
  ): NumberWithUnitsParsingResult<Unit> {
    val isNegative = if (tokens.peek() is Token.MinusSymbol) {
      true.also { tokens.next() } // consume '-'
    } else false

    return when (val firstToken = tokens.peek()) {
      is Token.PositiveRealNumber -> {
        tokens.next()
        expressionBuilder.real = if (isNegative) {
          -firstToken.parsedValue
        } else {
          firstToken.parsedValue
        }
        NumberWithUnitsParsingResult.Success(Unit)
      }

      is Token.PositiveInteger -> {
        tokens.next()
        // Check for fraction continuation.
        if (tokens.peek() is Token.DivideSymbol) {
          // Peek ahead: could be a fraction (integer/integer) if followed by PositiveInteger
          // and then a unit or end-of-input. But we need to distinguish "1/2 kg" (fraction)
          // from "5 m/s" (unit division). Since we are in parseNumber, the divide here always
          // means a fractional number.
          tokens.next() // consume '/'
          val denomToken = tokens.peek()
          if (denomToken is Token.PositiveInteger) {
            tokens.next()
            val fraction = Fraction.newBuilder().apply {
              this.isNegative = isNegative
              wholeNumber = 0
              numerator = firstToken.parsedValue
              denominator = denomToken.parsedValue
            }.build()
            expressionBuilder.fraction = fraction
            NumberWithUnitsParsingResult.Success(Unit)
          } else {
            NumberWithUnitsParsingError.MissingDenominatorError.toFailure()
          }
        } else {
          expressionBuilder.real = if (isNegative) {
            -firstToken.parsedValue.toDouble()
          } else {
            firstToken.parsedValue.toDouble()
          }
          NumberWithUnitsParsingResult.Success(Unit)
        }
      }

      else -> NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError.toFailure()
    }
  }

  /**
   * Grammar:
   * ```
   * compound_unit = units_multiplied , [ division_operator , denominator_expression ] ;
   * ```
   */
  private fun parseCompoundUnit(): NumberWithUnitsParsingResult<List<NumberUnitExpression>> {
    return parseUnitsMultiplied().flatMap { numeratorUnits ->
      val allUnits = numeratorUnits.toMutableList()

      // Optional: division_operator , denominator_expression
      if (tokens.peek() is Token.DivideSymbol) {
        tokens.next() // consume '/'
        parseDenominatorExp().map { denomUnits ->
          allUnits.addAll(denomUnits)
          allUnits
        }
      } else {
        NumberWithUnitsParsingResult.Success(allUnits)
      }
    }
  }

  /**
   * Grammar:
   * ```
   * denominator_expression =
   *     ( left_paren , units_multiplied , right_paren ) | units_multiplied ;
   * ```
   */
  private fun parseDenominatorExp(): NumberWithUnitsParsingResult<List<NumberUnitExpression>> {
    val hasParens = tokens.peek() is Token.LeftParenthesisSymbol
    if (hasParens) tokens.next() // consume '('

    if (!isAtSuffixUnit()) {
      return NumberWithUnitsParsingError.UnitExpectedAfterDivisionError.toFailure()
    }

    return parseUnitsMultiplied().flatMap { units ->
      if (hasParens) {
        if (tokens.peek() !is Token.RightParenthesisSymbol) {
          return@flatMap NumberWithUnitsParsingError.UnbalancedParenthesesError.toFailure()
        }
        tokens.next() // consume ')'
      }

      // Negate exponents for denominator units
      val negated = units.map { unit ->
        unit.toBuilder().setExponent(-unit.exponent).build()
      }
      NumberWithUnitsParsingResult.Success(negated)
    }
  }

  /**
   * Grammar:
   * ```
   * units_multiplied = unit_with_exponent , { unit_with_exponent } ;
   * ```
   */
  private fun parseUnitsMultiplied(): NumberWithUnitsParsingResult<List<NumberUnitExpression>> {
    val units = mutableListOf<NumberUnitExpression>()
    return parseUnitWithExponent().flatMap { firstUnit ->
      units.add(firstUnit)

      var currentResult: NumberWithUnitsParsingResult<Unit> =
        NumberWithUnitsParsingResult.Success(Unit)
      // Continue consuming units while we see suffix units (but not '/', ')' or end)
      while (isAtSuffixUnit() && !currentResult.isFailure()) {
        currentResult = currentResult.flatMap {
          parseUnitWithExponent().map { nextUnit ->
            units.add(nextUnit)
            Unit
          }
        }
      }
      currentResult.map { units }
    }
  }

  /**
   * Grammar:
   * ```
   * unit_with_exponent = suffix_unit , [ exponent_operator , negatable_number ] ;
   * ```
   */
  private fun parseUnitWithExponent(): NumberWithUnitsParsingResult<NumberUnitExpression> {
    return parseSuffixUnit().flatMap { parsedUnit ->
      var finalExponent = parsedUnit.exponent
      if (tokens.peek() is Token.ExponentiationSymbol) {
        tokens.next() // consume '^'

        var negativeExponent = false
        if (tokens.peek() is Token.MinusSymbol) {
          tokens.next() // consume '-'
          negativeExponent = true
        }

        val expToken = tokens.peek()
        if (expToken is Token.PositiveInteger) {
          tokens.next()
          val exponentMultiplier = if (negativeExponent)
            -expToken.parsedValue
          else
            expToken.parsedValue
          finalExponent *= exponentMultiplier
        } else {
          return@flatMap NumberWithUnitsParsingError.MissingExponentError.toFailure()
        }
      }
      NumberWithUnitsParsingResult.Success(
        parsedUnit.toBuilder().setExponent(finalExponent).build()
      )
    }
  }

  /**
   * Grammar:
   * ```
   * suffix_unit = physical_unit | currency_unit_suffix ;
   * physical_unit = length_unit | mass_unit | area_unit | volume_unit | ... ;
   * ```
   *
   * The token stream may deliver an [Token.SiPrefix] followed by a base unit token,
   * or directly a unit token, or a currency suffix token.
   *
   * @return the canonical unit name string
   */
  private fun parseSuffixUnit(): NumberWithUnitsParsingResult<NumberUnitExpression> {
    return when (val token = tokens.peek()) {
      is Token.SiPrefix -> {
        tokens.next()
        val prefix = parseSiPrefix(token.prefix)
          ?: return NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError(token.prefix)
            .toFailure()
        val baseUnit = parsePrefixableBaseUnit(tokens.peek())
        if (baseUnit != null) {
          tokens.next()
          NumberWithUnitsParsingResult.Success(
            baseUnit.toBuilder().setSiPrefix(prefix).build()
          )
        } else {
          NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError(token.prefix).toFailure()
        }
      }

      else -> {
        val parsedUnit = parseAnyUnit(tokens.peek())
        if (parsedUnit != null) {
          tokens.next()
          NumberWithUnitsParsingResult.Success(parsedUnit)
        } else {
          NumberWithUnitsParsingError.UnitExpectedError.toFailure()
        }
      }
    }
  }

  /** Parses a currency prefix token and returns it as a [NumberUnit] with exponent 1. */
  private fun parseCurrencyPrefixUnit(): NumberUnitExpression? {
    val rawUnit = (tokens.peek() as? Token.Unit)?.unit ?: return null
    val currencyUnit = when (rawUnit) {
      "$" -> NumberUnitExpression.Unit.DOLLAR
      "₹", "Rs" -> NumberUnitExpression.Unit.RUPEE
      else -> return null
    }
    tokens.next()
    return createUnitExpression(currencyUnit)
  }

  private fun parsePrefixableBaseUnit(token: Token?): NumberUnitExpression? {
    val parsed = parseAnyUnit(token) ?: return null
    return when (parsed.unit) {
      NumberUnitExpression.Unit.METER,
      NumberUnitExpression.Unit.GRAM,
      NumberUnitExpression.Unit.LITER,
      NumberUnitExpression.Unit.RADIAN,
      NumberUnitExpression.Unit.DEGREE,
      NumberUnitExpression.Unit.SECOND,
      NumberUnitExpression.Unit.HERTZ,
      NumberUnitExpression.Unit.MOLE,
      NumberUnitExpression.Unit.CANDELA,
      NumberUnitExpression.Unit.NEWTON,
      NumberUnitExpression.Unit.JOULE,
      NumberUnitExpression.Unit.WATT,
      NumberUnitExpression.Unit.PASCAL,
      NumberUnitExpression.Unit.AMPERE,
      NumberUnitExpression.Unit.VOLT,
      NumberUnitExpression.Unit.OHM -> parsed

      else -> null
    }
  }

  private fun parseAnyUnit(token: Token?): NumberUnitExpression? {
    val raw = (token as? Token.Unit)?.unit ?: return null
    return parseMeterUnit(raw)
      ?: parseInchUnit(raw)
      ?: parseFootUnit(raw)
      ?: parseYardUnit(raw)
      ?: parseGramUnit(raw)
      ?: parseGrainUnit(raw)
      ?: parseOunceUnit(raw)
      ?: parseLiterUnit(raw)
      ?: parseSecondUnit(raw)
      ?: parseMinuteUnit(raw)
      ?: parseHourUnit(raw)
      ?: parseKelvinUnit(raw)
      ?: parseCelsiusUnit(raw)
      ?: parseHertzUnit(raw)
      ?: parseRadianUnit(raw)
      ?: parseDegreeUnit(raw)
      ?: parseMoleUnit(raw)
      ?: parseCandelaUnit(raw)
      ?: parseNewtonUnit(raw)
      ?: parseJouleUnit(raw)
      ?: parseWattUnit(raw)
      ?: parsePascalUnit(raw)
      ?: parseAmpereUnit(raw)
      ?: parseVoltUnit(raw)
      ?: parseOhmUnit(raw)
      ?: parseDollarUnit(raw)
      ?: parseCentUnit(raw)
      ?: parseRupeeUnit(raw)
      ?: parsePaisaUnit(raw)
  }

  private fun parseMeterUnit(raw: String): NumberUnitExpression? = when (raw) {
    "m", "meter", "meters" -> createUnitExpression(NumberUnitExpression.Unit.METER)
    "m2" -> createUnitExpression(NumberUnitExpression.Unit.SQUARE_METER)
    "m3" -> createUnitExpression(NumberUnitExpression.Unit.CUBIC_METER)
    else -> null
  }

  private fun parseInchUnit(raw: String): NumberUnitExpression? = when (raw) {
    "in", "inch", "inches" -> createUnitExpression(NumberUnitExpression.Unit.INCH)
    "sqin", "sqinch" -> createUnitExpression(NumberUnitExpression.Unit.SQUARE_INCH)
    "cuin" -> createUnitExpression(NumberUnitExpression.Unit.CUBIC_INCH)
    else -> null
  }

  private fun parseFootUnit(raw: String): NumberUnitExpression? = when (raw) {
    "ft", "foot", "feet" -> createUnitExpression(NumberUnitExpression.Unit.FOOT)
    "sqft", "sqfeet" -> createUnitExpression(NumberUnitExpression.Unit.SQUARE_FOOT)
    "cuft" -> createUnitExpression(NumberUnitExpression.Unit.CUBIC_FOOT)
    else -> null
  }

  private fun parseYardUnit(raw: String): NumberUnitExpression? = when (raw) {
    "yd", "yard", "yards" -> createUnitExpression(NumberUnitExpression.Unit.YARD)
    "sqyd", "sqyard" -> createUnitExpression(NumberUnitExpression.Unit.SQUARE_YARD)
    "cuyd" -> createUnitExpression(NumberUnitExpression.Unit.CUBIC_YARD)
    else -> null
  }

  private fun parseGramUnit(raw: String): NumberUnitExpression? = when (raw) {
    "g", "gram", "grams" -> createUnitExpression(NumberUnitExpression.Unit.GRAM)
    else -> null
  }

  private fun parseGrainUnit(raw: String): NumberUnitExpression? = when (raw) {
    "gr", "grain", "grains" -> createUnitExpression(NumberUnitExpression.Unit.GRAIN)
    else -> null
  }

  private fun parseOunceUnit(raw: String): NumberUnitExpression? = when (raw) {
    "oz", "ounce", "ounces" -> createUnitExpression(NumberUnitExpression.Unit.OUNCE)
    else -> null
  }

  private fun parseLiterUnit(raw: String): NumberUnitExpression? = when (raw) {
    "l", "L", "lt", "liter", "litre", "liters", "litres" ->
      createUnitExpression(NumberUnitExpression.Unit.LITER)

    "cc" -> createUnitExpression(NumberUnitExpression.Unit.CUBIC_CENTIMETER)

    else -> null
  }

  private fun parseSecondUnit(raw: String): NumberUnitExpression? = when (raw) {
    "s", "sec", "secs", "second", "seconds" ->
      createUnitExpression(NumberUnitExpression.Unit.SECOND)

    else -> null
  }

  private fun parseMinuteUnit(raw: String): NumberUnitExpression? = when (raw) {
    "min", "mins", "minute", "minutes" -> createUnitExpression(NumberUnitExpression.Unit.MINUTE)
    else -> null
  }

  private fun parseHourUnit(raw: String): NumberUnitExpression? = when (raw) {
    "h", "hr", "hrs", "hour", "hours" -> createUnitExpression(NumberUnitExpression.Unit.HOUR)
    else -> null
  }

  private fun parseKelvinUnit(raw: String): NumberUnitExpression? = when (raw) {
    "K", "kelvin" -> createUnitExpression(NumberUnitExpression.Unit.KELVIN)
    else -> null
  }

  private fun parseCelsiusUnit(raw: String): NumberUnitExpression? = when (raw) {
    "degC", "celsius" -> createUnitExpression(NumberUnitExpression.Unit.CELSIUS)
    else -> null
  }

  private fun parseHertzUnit(raw: String): NumberUnitExpression? = when (raw) {
    "Hz", "hertz" -> createUnitExpression(NumberUnitExpression.Unit.HERTZ)
    else -> null
  }

  private fun parseRadianUnit(raw: String): NumberUnitExpression? = when (raw) {
    "rad", "radian", "radians" -> createUnitExpression(NumberUnitExpression.Unit.RADIAN)
    else -> null
  }

  private fun parseDegreeUnit(raw: String): NumberUnitExpression? = when (raw) {
    "deg", "degree", "degrees" -> createUnitExpression(NumberUnitExpression.Unit.DEGREE)
    else -> null
  }

  private fun parseMoleUnit(raw: String): NumberUnitExpression? = when (raw) {
    "mol", "mole", "moles" -> createUnitExpression(NumberUnitExpression.Unit.MOLE)
    else -> null
  }

  private fun parseCandelaUnit(raw: String): NumberUnitExpression? = when (raw) {
    "cd", "candela" -> createUnitExpression(NumberUnitExpression.Unit.CANDELA)
    else -> null
  }

  private fun parseNewtonUnit(raw: String): NumberUnitExpression? = when (raw) {
    "N", "newton", "newtons" -> createUnitExpression(NumberUnitExpression.Unit.NEWTON)
    else -> null
  }

  private fun parseJouleUnit(raw: String): NumberUnitExpression? = when (raw) {
    "J", "joule", "joules" -> createUnitExpression(NumberUnitExpression.Unit.JOULE)
    else -> null
  }

  private fun parseWattUnit(raw: String): NumberUnitExpression? = when (raw) {
    "W", "watt", "watts" -> createUnitExpression(NumberUnitExpression.Unit.WATT)
    else -> null
  }

  private fun parsePascalUnit(raw: String): NumberUnitExpression? = when (raw) {
    "Pa", "pascal", "pascals" -> createUnitExpression(NumberUnitExpression.Unit.PASCAL)
    else -> null
  }

  private fun parseAmpereUnit(raw: String): NumberUnitExpression? = when (raw) {
    "A", "ampere", "amperes" -> createUnitExpression(NumberUnitExpression.Unit.AMPERE)
    else -> null
  }

  private fun parseVoltUnit(raw: String): NumberUnitExpression? = when (raw) {
    "V", "volt", "volts" -> createUnitExpression(NumberUnitExpression.Unit.VOLT)
    else -> null
  }

  private fun parseOhmUnit(raw: String): NumberUnitExpression? = when (raw) {
    "ohm", "ohms" -> createUnitExpression(NumberUnitExpression.Unit.OHM)
    else -> null
  }

  private fun parseDollarUnit(raw: String): NumberUnitExpression? = when (raw) {
    "USD", "dollar", "dollars", "Dollar", "Dollars" ->
      createUnitExpression(NumberUnitExpression.Unit.DOLLAR)

    else -> null
  }

  private fun parseCentUnit(raw: String): NumberUnitExpression? = when (raw) {
    "¢", "cent", "cents", "Cent", "Cents" -> createUnitExpression(NumberUnitExpression.Unit.CENT)
    else -> null
  }

  private fun parseRupeeUnit(raw: String): NumberUnitExpression? = when (raw) {
    "rupee", "rupees", "Rupee", "Rupees" -> createUnitExpression(NumberUnitExpression.Unit.RUPEE)
    else -> null
  }

  private fun parsePaisaUnit(raw: String): NumberUnitExpression? = when (raw) {
    "paisa", "paise", "Paisa", "Paise" -> createUnitExpression(NumberUnitExpression.Unit.PAISA)
    else -> null
  }

  private fun parseSiPrefix(symbol: String): NumberUnitExpression.SiPrefix? = when (symbol) {
    "da" -> NumberUnitExpression.SiPrefix.DECA
    "h" -> NumberUnitExpression.SiPrefix.HECTO
    "k" -> NumberUnitExpression.SiPrefix.KILO
    "M" -> NumberUnitExpression.SiPrefix.MEGA
    "G" -> NumberUnitExpression.SiPrefix.GIGA
    "T" -> NumberUnitExpression.SiPrefix.TERA
    "P" -> NumberUnitExpression.SiPrefix.PETA
    "E" -> NumberUnitExpression.SiPrefix.EXA
    "Z" -> NumberUnitExpression.SiPrefix.ZETTA
    "Y" -> NumberUnitExpression.SiPrefix.YOTTA
    "d" -> NumberUnitExpression.SiPrefix.DECI
    "c" -> NumberUnitExpression.SiPrefix.CENTI
    "m" -> NumberUnitExpression.SiPrefix.MILLI
    "u" -> NumberUnitExpression.SiPrefix.MICRO
    "n" -> NumberUnitExpression.SiPrefix.NANO
    "p" -> NumberUnitExpression.SiPrefix.PICO
    "f" -> NumberUnitExpression.SiPrefix.FEMTO
    "a" -> NumberUnitExpression.SiPrefix.ATTO
    "z" -> NumberUnitExpression.SiPrefix.ZEPTO
    "y" -> NumberUnitExpression.SiPrefix.YOCTO
    else -> null
  }

  private fun isAtSuffixUnit(): Boolean {
    val token = tokens.peek() ?: return false
    return token is Token.SiPrefix || parseAnyUnit(token) != null
  }

  private fun isCurrencyUnit(unit: NumberUnitExpression.Unit): Boolean =
    unit == NumberUnitExpression.Unit.DOLLAR ||
      unit == NumberUnitExpression.Unit.CENT ||
      unit == NumberUnitExpression.Unit.RUPEE ||
      unit == NumberUnitExpression.Unit.PAISA

  private fun isCurrencyPrefixToken(token: Token?): Boolean {
    val rawUnit = (token as? Token.Unit)?.unit ?: return false
    return rawUnit == "$" || rawUnit == "₹" || rawUnit == "Rs"
  }

  private fun createUnitExpression(
    unit: NumberUnitExpression.Unit,
    exponent: Int = 1,
    siPrefix: NumberUnitExpression.SiPrefix =
      NumberUnitExpression.SiPrefix.SI_PREFIX_UNSPECIFIED
  ): NumberUnitExpression {
    return NumberUnitExpression.newBuilder().apply {
      this.unit = unit
      this.exponent = exponent
      this.siPrefix = siPrefix
    }.build()
  }

  companion object {
    /** The result of attempting to parse a raw number-with-units expression. */
    sealed class NumberWithUnitsParsingResult<out T> {
      /** Indicates that the parse was successful with a value of [result]. */
      data class Success<out T>(val result: T) : NumberWithUnitsParsingResult<T>()

      /** Indicates that the parse failed with the specified [error]. */
      data class Failure(
        val error: NumberWithUnitsParsingError
      ) : NumberWithUnitsParsingResult<Nothing>()
    }

    /**
     * Parses a [rawExpression] as a number-with-units expression.
     *
     * The expression is first normalized (trimmed / whitespace collapsed) before tokenization.
     *
     * @return the result of attempting to parse the specified expression
     */
    fun parseNumberWithUnits(
      rawExpression: String
    ): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
      val normalized = rawExpression.normalizeWhitespace()
      val tokens = NumberWithUnitsTokenizer.tokenize(normalized).toPeekableIterator()
      return NumberWithUnitsParser(normalized, tokens).parseNumberWithUnits()
    }

    /** Creates a failed parse result with [this] error. */
    private fun NumberWithUnitsParsingError.toFailure(): NumberWithUnitsParsingResult<Nothing> =
      NumberWithUnitsParsingResult.Failure(this)

    /** Returns whether [this] result is a failure. */
    private fun <T> NumberWithUnitsParsingResult<T>.isFailure() =
      this is NumberWithUnitsParsingResult.Failure

    /**
     * Transforms the successful result of [this] using [operation], or returns the failure if
     * [this] is a failure.
     */
    private fun <T1, T2> NumberWithUnitsParsingResult<T1>.map(
      operation: (T1) -> T2
    ): NumberWithUnitsParsingResult<T2> = flatMap { result ->
      NumberWithUnitsParsingResult.Success(operation(result))
    }

    /**
     * Transforms the successful result of [this] using [operation], or returns the failure if
     * [this] is a failure. The difference between this and [map] is that [operation] can also
     * return a failure, which will be returned by this method if it occurs.
     */
    private fun <T1, T2> NumberWithUnitsParsingResult<T1>.flatMap(
      operation: (T1) -> NumberWithUnitsParsingResult<T2>
    ): NumberWithUnitsParsingResult<T2> {
      return when (this) {
        is NumberWithUnitsParsingResult.Success -> operation(result)
        is NumberWithUnitsParsingResult.Failure -> error.toFailure()
      }
    }

    /**
     * Potentially changes [this] result into a failure based on the provided [operation].
     * The operation is only called if [this] result is currently successful; the returned result
     * will only be in a failing state if [operation] returns a non-null error.
     */
    private fun <T> NumberWithUnitsParsingResult<T>.maybeFail(
      operation: (T) -> NumberWithUnitsParsingError?
    ): NumberWithUnitsParsingResult<T> = flatMap { result ->
      operation(result)?.toFailure() ?: this
    }
  }
}
