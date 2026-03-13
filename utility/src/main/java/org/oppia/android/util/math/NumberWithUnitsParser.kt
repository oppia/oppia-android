package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
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
  private fun parseNumberWithUnits(): NumberWithUnitsParsingResult<NumberWithUnits> {
    if (!tokens.hasNext()) {
      return NumberWithUnitsParsingError.EmptyExpressionError.toFailure()
    }

    val result = when (tokens.peek()) {
      is Token.DollarPrefixUnit, is Token.RupeePrefixUnit -> parsePrefixFormattedValue()
      is Token.PositiveInteger, is Token.PositiveRealNumber, is Token.MinusSymbol ->
        parseSuffixFormattedValue()

      is Token.InvalidToken -> {
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
  private fun parsePrefixFormattedValue(): NumberWithUnitsParsingResult<NumberWithUnits> {
    val prefixUnit = parseCurrencyPrefixUnit()
      ?: return NumberWithUnitsParsingError.GenericError.toFailure()

    val numberResult = parseNumber()
    if (numberResult is NumberWithUnitsParsingResult.Failure) return numberResult

    val builder = (numberResult as NumberWithUnitsParsingResult.Success).result
    builder.addUnit(prefixUnit)

    // Optional compound_unit
    if (isAtSuffixUnit()) {
      val compoundUnits = parseCompoundUnit()
      if (compoundUnits is NumberWithUnitsParsingResult.Failure) {
        return compoundUnits
      }
      val suffixUnits = (compoundUnits as NumberWithUnitsParsingResult.Success).result
      // Check for duplicate currency: a currency prefix was already added, so if any suffix
      // unit is also a currency unit the expression is ambiguous (e.g. "$5 dollars").
      if (suffixUnits.any { isCurrencyUnit(it.unit) }) {
        return NumberWithUnitsParsingError.DuplicateCurrencyError.toFailure()
      }
      builder.addAllUnit(suffixUnits)
    }

    return NumberWithUnitsParsingResult.Success(builder.build())
  }

  /**
   * Grammar:
   * ```
   * suffix_formatted_value = number , compound_unit ;
   * ```
   */
  private fun parseSuffixFormattedValue(): NumberWithUnitsParsingResult<NumberWithUnits> {
    val numberResult = parseNumber()
    if (numberResult is NumberWithUnitsParsingResult.Failure) return numberResult

    val builder = (numberResult as NumberWithUnitsParsingResult.Success).result

    // compound_unit is required
    if (!isAtSuffixUnit()) {
      return NumberWithUnitsParsingError.UnitExpectedError.toFailure()
    }

    val compoundUnits = parseCompoundUnit()
    if (compoundUnits is NumberWithUnitsParsingResult.Failure) return compoundUnits
    builder.addAllUnit((compoundUnits as NumberWithUnitsParsingResult.Success).result)

    return NumberWithUnitsParsingResult.Success(builder.build())
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
  private fun parseNumber(): NumberWithUnitsParsingResult<NumberWithUnits.Builder> {
    var isNegative = false
    if (tokens.peek() is Token.MinusSymbol) {
      tokens.next() // consume '-'
      isNegative = true
    }

    val firstToken = tokens.peek()
    if (firstToken !is Token.PositiveInteger && firstToken !is Token.PositiveRealNumber) {
      return NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError.toFailure()
    }

    return when (firstToken) {
      is Token.PositiveRealNumber -> {
        tokens.next()
        val value = if (isNegative) -firstToken.parsedValue else firstToken.parsedValue
        val builder = NumberWithUnits.newBuilder().apply { real = value }
        NumberWithUnitsParsingResult.Success(builder)
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
            val builder = NumberWithUnits.newBuilder().apply {
              fraction = Fraction.newBuilder().apply {
                this.isNegative = isNegative
                wholeNumber = 0
                numerator = firstToken.parsedValue
                denominator = denomToken.parsedValue
              }.build()
            }
            NumberWithUnitsParsingResult.Success(builder)
          } else {
            NumberWithUnitsParsingError.MissingDenominatorError.toFailure()
          }
        } else {
          val value = if (isNegative) {
            -firstToken.parsedValue.toDouble()
          } else {
            firstToken.parsedValue.toDouble()
          }
          val builder = NumberWithUnits.newBuilder().apply { real = value }
          NumberWithUnitsParsingResult.Success(builder)
        }
      }

      else -> NumberWithUnitsParsingError.NumberExpectedError.toFailure()
    }
  }

  /**
   * Grammar:
   * ```
   * compound_unit = units_multiplied , [ division_operator , denominator_expression ] ;
   * ```
   */
  private fun parseCompoundUnit(): NumberWithUnitsParsingResult<List<NumberUnit>> {
    val numeratorUnits = parseUnitsMultiplied()
    if (numeratorUnits is NumberWithUnitsParsingResult.Failure) return numeratorUnits

    val allUnits =
      (numeratorUnits as NumberWithUnitsParsingResult.Success).result.toMutableList()

    // Optional: division_operator , denominator_expression
    if (tokens.peek() is Token.DivideSymbol) {
      tokens.next() // consume '/'
      val denomResult = parseDenominatorExpression()
      if (denomResult is NumberWithUnitsParsingResult.Failure) return denomResult
      allUnits.addAll((denomResult as NumberWithUnitsParsingResult.Success).result)
    }

    return NumberWithUnitsParsingResult.Success(allUnits)
  }

  /**
   * Grammar:
   * ```
   * denominator_expression =
   *     ( left_paren , units_multiplied , right_paren ) | units_multiplied ;
   * ```
   */
  private fun parseDenominatorExpression(): NumberWithUnitsParsingResult<List<NumberUnit>> {
    val hasParens = tokens.peek() is Token.LeftParenthesisSymbol
    if (hasParens) tokens.next() // consume '('

    if (!isAtSuffixUnit()) {
      return NumberWithUnitsParsingError.UnitExpectedAfterDivisionError.toFailure()
    }

    val unitsResult = parseUnitsMultiplied()
    if (unitsResult is NumberWithUnitsParsingResult.Failure) return unitsResult

    if (hasParens) {
      if (tokens.peek() !is Token.RightParenthesisSymbol) {
        return NumberWithUnitsParsingError.UnbalancedParenthesesError.toFailure()
      }
      tokens.next() // consume ')'
    }

    // Negate exponents for denominator units
    val negated = (unitsResult as NumberWithUnitsParsingResult.Success).result.map { unit ->
      unit.toBuilder().setExponent(-unit.exponent).build()
    }
    return NumberWithUnitsParsingResult.Success(negated)
  }

  /**
   * Grammar:
   * ```
   * units_multiplied = unit_with_exponent , { unit_with_exponent } ;
   * ```
   */
  private fun parseUnitsMultiplied(): NumberWithUnitsParsingResult<List<NumberUnit>> {
    val units = mutableListOf<NumberUnit>()

    val firstUnit = parseUnitWithExponent()
    if (firstUnit is NumberWithUnitsParsingResult.Failure) return firstUnit
    units.add((firstUnit as NumberWithUnitsParsingResult.Success).result)

    // Continue consuming units while we see suffix units (but not '/', ')' or end)
    while (isAtSuffixUnit()) {
      val nextUnit = parseUnitWithExponent()
      if (nextUnit is NumberWithUnitsParsingResult.Failure) return nextUnit
      units.add((nextUnit as NumberWithUnitsParsingResult.Success).result)
    }

    return NumberWithUnitsParsingResult.Success(units)
  }

  /**
   * Grammar:
   * ```
   * unit_with_exponent = suffix_unit , [ exponent_operator , negatable_number ] ;
   * ```
   */
  private fun parseUnitWithExponent(): NumberWithUnitsParsingResult<NumberUnit> {
    val unitName = parseSuffixUnit()
    if (unitName is NumberWithUnitsParsingResult.Failure) return unitName
    val name = (unitName as NumberWithUnitsParsingResult.Success).result

    var exponent = 1
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
        exponent = if (negativeExponent) -expToken.parsedValue else expToken.parsedValue
      } else {
        return NumberWithUnitsParsingError.MissingExponentError.toFailure()
      }
    }

    val unit = NumberUnit.newBuilder().apply {
      this.unit = name
      this.exponent = exponent
    }.build()
    return NumberWithUnitsParsingResult.Success(unit)
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
  private fun parseSuffixUnit(): NumberWithUnitsParsingResult<String> {
    return when (val token = tokens.peek()) {
      is Token.SiPrefix -> {
        tokens.next()
        val prefixName = siPrefixToString(token.prefixValue)
        // A base unit must follow
        val baseToken = tokens.peek()
        val baseUnit = baseUnitTokenToString(baseToken)
        if (baseUnit != null) {
          tokens.next()
          NumberWithUnitsParsingResult.Success(prefixName + baseUnit)
        } else {
          NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError(prefixName).toFailure()
        }
      }

      else -> {
        val unitName = anyUnitTokenToString(token)
        if (unitName != null) {
          tokens.next()
          NumberWithUnitsParsingResult.Success(unitName)
        } else {
          NumberWithUnitsParsingError.UnitExpectedError.toFailure()
        }
      }
    }
  }

  /** Parses a currency prefix token and returns it as a [NumberUnit] with exponent 1. */
  private fun parseCurrencyPrefixUnit(): NumberUnit? {
    return when (tokens.peek()) {
      is Token.DollarPrefixUnit -> {
        tokens.next()
        NumberUnit.newBuilder().setUnit("dollar").setExponent(1).build()
      }

      is Token.RupeePrefixUnit -> {
        tokens.next()
        NumberUnit.newBuilder().setUnit("rupee").setExponent(1).build()
      }

      else -> null
    }
  }

  /**
   * Returns the canonical name for base unit tokens that can follow an SI prefix.
   * Returns null if the token is not a valid base unit for SI prefixing.
   */
  private fun baseUnitTokenToString(token: Token?): String? = when (token) {
    // length
    is Token.MeterUnit -> "m"
    // mass
    is Token.GramUnit -> "g"
    // area
    is Token.SquareMeterUnit -> "m2"
    // volume
    is Token.CubicMeterUnit -> "m3"
    is Token.LiterUnit -> "l"
    // angle
    is Token.RadianUnit -> "rad"
    is Token.DegreeUnit -> "deg"
    // time
    is Token.SecondUnit -> "s"
    // frequency
    is Token.HertzUnit -> "Hz"
    // amount of substance
    is Token.MoleUnit -> "mol"
    // luminous intensity
    is Token.CandelaUnit -> "cd"
    // force
    is Token.NewtonUnit -> "N"
    // energy
    is Token.JouleUnit -> "J"
    // power
    is Token.WattUnit -> "W"
    // pressure
    is Token.PascalUnit -> "Pa"
    // electricity
    is Token.AmpereUnit -> "A"
    is Token.VoltUnit -> "V"
    is Token.OhmUnit -> "ohm"
    else -> null
  }

  /**
   * Returns the canonical name for any unit token (base units, non-prefixable units,
   * and currency suffix units). Returns null if the token is not a unit.
   */
  private fun anyUnitTokenToString(token: Token?): String? {
    // Try base unit first
    baseUnitTokenToString(token)?.let { return it }

    return when (token) {
      // length
      is Token.InchUnit -> "in"
      is Token.FootUnit -> "ft"
      is Token.YardUnit -> "yd"
      // mass
      is Token.GrainUnit -> "gr"
      is Token.OunceUnit -> "oz"
      // area
      is Token.SquareInchUnit -> "sqin"
      is Token.SquareFootUnit -> "sqft"
      is Token.SquareYardUnit -> "sqyd"
      // volume
      is Token.CcUnit -> "cc"
      is Token.CubicInchUnit -> "cuin"
      is Token.CubicFootUnit -> "cuft"
      is Token.CubicYardUnit -> "cuyd"
      // temperature
      is Token.KelvinUnit -> "K"
      is Token.CelsiusUnit -> "degC"
      // time
      is Token.MinuteUnit -> "min"
      is Token.HourUnit -> "hr"
      // currency suffix
      is Token.DollarSuffixUnit -> "dollar"
      is Token.CentSuffixUnit -> "cent"
      is Token.RupeeSuffixUnit -> "rupee"
      is Token.PaisaSuffixUnit -> "paise"
      else -> null
    }
  }

  /** Returns the abbreviated string for an [Token.SiPrefixValue]. */
  private fun siPrefixToString(prefix: Token.SiPrefixValue): String = when (prefix) {
    Token.SiPrefixValue.DECA -> "da"
    Token.SiPrefixValue.HECTO -> "h"
    Token.SiPrefixValue.KILO -> "k"
    Token.SiPrefixValue.MEGA -> "M"
    Token.SiPrefixValue.GIGA -> "G"
    Token.SiPrefixValue.TERA -> "T"
    Token.SiPrefixValue.PETA -> "P"
    Token.SiPrefixValue.EXA -> "E"
    Token.SiPrefixValue.ZETTA -> "Z"
    Token.SiPrefixValue.YOTTA -> "Y"
    Token.SiPrefixValue.DECI -> "d"
    Token.SiPrefixValue.CENTI -> "c"
    Token.SiPrefixValue.MILLI -> "m"
    Token.SiPrefixValue.MICRO -> "u"
    Token.SiPrefixValue.NANO -> "n"
    Token.SiPrefixValue.PICO -> "p"
    Token.SiPrefixValue.FEMTO -> "f"
    Token.SiPrefixValue.ATTO -> "a"
    Token.SiPrefixValue.ZEPTO -> "z"
    Token.SiPrefixValue.YOCTO -> "y"
  }

  /**
   * Returns whether the current token represents the beginning of a suffix unit
   * (physical unit, SI prefix, or currency suffix unit).
   */
  private fun isAtSuffixUnit(): Boolean {
    val token = tokens.peek() ?: return false
    return token is Token.SiPrefix || anyUnitTokenToString(token) != null
  }

  /** Returns whether [unitName] is a currency unit canonical name. */
  private fun isCurrencyUnit(unitName: String): Boolean =
    unitName == "dollar" || unitName == "cent" || unitName == "rupee" || unitName == "paise"

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
    ): NumberWithUnitsParsingResult<NumberWithUnits> {
      val normalized = rawExpression.normalizeWhitespace()
      val tokens = NumberWithUnitsTokenizer.tokenize(normalized).toPeekableIterator()
      return NumberWithUnitsParser(normalized, tokens).parseNumberWithUnits()
    }

    /** Creates a failed parse result with [this] error. */
    private fun NumberWithUnitsParsingError.toFailure(): NumberWithUnitsParsingResult<Nothing> =
      NumberWithUnitsParsingResult.Failure(this)

    /**
     * Potentially changes [this] result into a failure based on the provided [operation].
     * The operation is only called if [this] result is currently successful; the returned result
     * will only be in a failing state if [operation] returns a non-null error.
     */
    private fun <T> NumberWithUnitsParsingResult<T>.maybeFail(
      operation: (T) -> NumberWithUnitsParsingError?
    ): NumberWithUnitsParsingResult<T> {
      return when (this) {
        is NumberWithUnitsParsingResult.Success -> {
          val error = operation(result)
          error?.toFailure() ?: this
        }

        is NumberWithUnitsParsingResult.Failure -> this
      }
    }
  }
}
