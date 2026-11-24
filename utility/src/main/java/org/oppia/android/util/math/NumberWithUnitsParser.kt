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
      nextToken.isCurrencyPrefixToken() -> parsePrefixFormattedValue()
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

    return parseNumber(hasCurrencyPrefix = true).flatMap { expressionBuilder ->
      if (isAtSuffixUnit()) {
        parseCompoundUnit().maybeFail { suffixUnits ->
          // Check for duplicate currency: a currency prefix was already added, so if any suffix
          // unit is also a currency unit the expression is ambiguous (e.g. "$5 dollars").
          if (suffixUnits.any { it.unit.isCurrencyUnit() }) {
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
   * suffix_formatted_value = number , [ compound_unit ] ;
   * ```
   */
  private fun parseSuffixFormattedValue(): NumberWithUnitsParsingResult<NumberWithUnitsExpression> {
    return parseNumber().flatMap { expressionBuilder ->
      if (isAtSuffixUnit()) {
        parseCompoundUnit().map { suffixUnits ->
          expressionBuilder.apply {
            valueSuffixExpression = ValueSuffixExpression.newBuilder().apply {
              addAllSuffixUnits(suffixUnits)
            }.build()
          }.build()
        }
      } else {
        NumberWithUnitsParsingResult.Success(expressionBuilder.build())
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
    hasCurrencyPrefix: Boolean = false
  ): NumberWithUnitsParsingResult<NumberWithUnitsExpression.Builder> {
    val isNegative = if (tokens.peek() is Token.MinusSymbol) {
      true.also { tokens.next() } // consume '-'
    } else false

    val expressionBuilder = NumberWithUnitsExpression.newBuilder()

    return when (val firstToken = tokens.peek()) {
      is Token.PositiveRealNumber -> {
        tokens.next()
        expressionBuilder.real = if (isNegative) {
          -firstToken.parsedValue
        } else {
          firstToken.parsedValue
        }
        NumberWithUnitsParsingResult.Success(expressionBuilder)
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
            NumberWithUnitsParsingResult.Success(expressionBuilder)
          } else {
            NumberWithUnitsParsingError.MissingDenominatorError.toFailure()
          }
        } else {
          expressionBuilder.real = if (isNegative) {
            -firstToken.parsedValue.toDouble()
          } else {
            firstToken.parsedValue.toDouble()
          }
          NumberWithUnitsParsingResult.Success(expressionBuilder)
        }
      }

      else -> {
        if (hasCurrencyPrefix) {
          NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError.toFailure()
        } else {
          NumberWithUnitsParsingError.NumberExpectedError.toFailure()
        }
      }
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
          allUnits + denomUnits
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
    return parseUnitsFromNextToken().flatMap { firstUnits ->
      units.addAll(firstUnits)

      var currentResult: NumberWithUnitsParsingResult<Unit> =
        NumberWithUnitsParsingResult.Success(Unit)
      // Continue consuming units while we see suffix units (but not '/', ')' or end)
      while (isAtSuffixUnit() && !currentResult.isFailure()) {
        currentResult = currentResult.flatMap {
          parseUnitsFromNextToken().map { nextUnits ->
            units.addAll(nextUnits)
            Unit
          }
        }
      }
      currentResult.map { units }
    }
  }

  /**
   * Parses a single unit token, returning one or more [NumberUnitExpression]s.
   *
   * First attempts standard single-unit parsing (with optional exponent). If that fails due to
   * an unrecognized unit, attempts to decompose the token into multiple known units.
   *
   * When decomposition succeeds and a trailing exponent is present, the exponent
   * is applied only to the last unit in the decomposed list.
   */
  private fun parseUnitsFromNextToken(): NumberWithUnitsParsingResult<List<NumberUnitExpression>> {
    val initialRetrievalCount = tokens.getRetrievalCount()
    val singleResult = parseUnitWithExponent()
    if (singleResult !is NumberWithUnitsParsingResult.Failure) {
      return singleResult.map { listOf(it) }
    }

    // Single-unit parsing failed. Try decomposing compound unit tokens
    // only if the single-unit parse didn't consume any tokens.
    if (tokens.getRetrievalCount() == initialRetrievalCount) {
      val token = tokens.peek()
      if (token is Token.Unit) {
        val decomposed = tryDecomposeCompoundUnit(token.unit, token)
        if (decomposed != null && decomposed.size > 1) {
          tokens.next() // Consume the compound token.

          // Look ahead for an exponent extension following the compound token.
          // If present, apply the exponent only to the last unit in the decomposed list.
          return parseExponentExtension().map { exponentMultiplier ->
            if (exponentMultiplier != 1) {
              val mutableDecomposed = decomposed.toMutableList()
              val lastUnit = mutableDecomposed.last()
              mutableDecomposed[mutableDecomposed.lastIndex] = lastUnit.toBuilder()
                .setExponent(lastUnit.exponent * exponentMultiplier)
                .build()
              mutableDecomposed
            } else {
              decomposed
            }
          }
        }
      }
    }

    // Return the original single-unit error.
    return NumberWithUnitsParsingResult.Failure(singleResult.error)
  }

  /**
   * Grammar:
   * ```
   * unit_with_exponent = suffix_unit , [ exponent_operator , negatable_number ] ;
   * ```
   */
  private fun parseUnitWithExponent(): NumberWithUnitsParsingResult<NumberUnitExpression> {
    return parseSuffixUnit().flatMap { parsedUnit ->
      parseExponentExtension().map { exponentMultiplier ->
        parsedUnit.toBuilder()
          .setExponent(parsedUnit.exponent * exponentMultiplier)
          .build()
      }
    }
  }

  /**
   * Parses an exponent extension if one follows the current position in the token stream.
   *
   * @return a [NumberWithUnitsParsingResult] containing the exponent multiplier value (1 if no
   *   exponent is present)
   */
  private fun parseExponentExtension(): NumberWithUnitsParsingResult<Int> {
    if (tokens.peek() is Token.ExponentiationSymbol) {
      tokens.next() // consume '^'

      val negativeExponent = if (tokens.peek() is Token.MinusSymbol) {
        true.also { tokens.next() } // consume '-'
      } else false

      val expToken = tokens.peek()
      return if (expToken is Token.PositiveInteger) {
        tokens.next()
        val exponentMultiplier = if (negativeExponent) {
          -expToken.parsedValue
        } else {
          expToken.parsedValue
        }
        NumberWithUnitsParsingResult.Success(exponentMultiplier)
      } else {
        NumberWithUnitsParsingError.MissingExponentError.toFailure()
      }
    }
    return NumberWithUnitsParsingResult.Success(1)
  }

  /**
   * Attempts to decompose a compound unit string into a list of individual [NumberUnitExpression]s.
   *
   * Each substring is first tried as a plain unit, then as an SI-prefix and base unit combination.
   *
   * @param unitStr the unit string to decompose
   * @param originalToken the original [Token.Unit] (used for index tracking)
   * @return the list of resolved units, or null if decomposition is not possible
   */
  private fun tryDecomposeCompoundUnit(
    unitStr: String,
    originalToken: Token.Unit
  ): List<NumberUnitExpression>? {
    if (unitStr.isEmpty()) return emptyList()

    // Try longest prefix first to find the best match.
    for (len in unitStr.length downTo 1) {
      val candidate = unitStr.substring(0, len)
      val resolved = resolveUnitSubstring(candidate, originalToken)
      if (resolved != null) {
        val remaining = unitStr.substring(len)
        val remainingToken = Token.Unit(
          remaining, originalToken.startIndex + len, originalToken.endIndex
        )
        val rest = tryDecomposeCompoundUnit(remaining, remainingToken)
        if (rest != null) return listOf(resolved) + rest
      }
    }
    return null
  }

  /**
   * Tries to resolve a unit substring as either a plain unit or an SI-prefix and base unit.
   *
   * @param candidate the substring to resolve
   * @param originalToken the original token (for index computation)
   * @return the resolved [NumberUnitExpression], or null if not resolvable
   */
  private fun resolveUnitSubstring(
    candidate: String,
    originalToken: Token.Unit
  ): NumberUnitExpression? {
    val syntheticToken = Token.Unit(
      candidate, originalToken.startIndex, originalToken.startIndex + candidate.length
    )

    // Try as a plain unit first.
    val plainUnit = parseAnyUnit(syntheticToken)
    if (plainUnit != null) return createUnitExpression(plainUnit)

    // Try as SI-prefix and base unit.
    val siPrefixIndex = syntheticToken.extractUnitStartIndex()
    if (siPrefixIndex > 0 && siPrefixIndex < candidate.length) {
      val siPrefixStr = candidate.substring(0, siPrefixIndex)
      val siPrefix = parseSiPrefix(siPrefixStr)
      val baseUnitStr = candidate.substring(siPrefixIndex)
      val baseToken = Token.Unit(
        baseUnitStr, syntheticToken.startIndex + siPrefixIndex, syntheticToken.endIndex
      )
      val baseUnit = parseAnyUnit(baseToken)
      if (baseUnit != null && baseUnit.isPrefixable() && siPrefix != null) {
        return createUnitExpression(baseUnit, siPrefix = siPrefix)
      }
    }
    return null
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
    val token = tokens.peek()
    if (token !is Token.Unit || token.unit.isEmpty()) {
      return NumberWithUnitsParsingError.UnitExpectedError.toFailure()
    }

    val unitStartIndex = token.extractUnitStartIndex()
    if (unitStartIndex == 0) {
      // No SI prefix, so attempt to parse the entire token as a unit.
      val parsedUnit = parseAnyUnit(token)
      return if (parsedUnit != null) {
        NumberWithUnitsParsingResult.Success(parsedUnit)
      } else {
        NumberWithUnitsParsingError.InvalidUnitError(token.unit).toFailure()
      }.map { unit ->
        tokens.next()
        createUnitExpression(unit)
      }
    } else {
      // SI prefix present, so attempt to parse the base unit after the prefix.
      val siPrefixStr = token.unit.substring(0, unitStartIndex)
      val siPrefix = parseSiPrefix(siPrefixStr)
      val baseUnitStr = token.unit.substring(unitStartIndex)
      val baseUnit = parseAnyUnit(
        Token.Unit(baseUnitStr, token.startIndex + unitStartIndex, token.endIndex)
      )
      return if (baseUnit == null || !baseUnit.isPrefixable()) {
        // Might not be SI prefix, but entirely unit. Attempt to parse entire token as unit
        // before failing with UnitExpectedAfterSiPrefixError.
        val parsedUnit = parseAnyUnit(token)
        if (parsedUnit != null) {
          NumberWithUnitsParsingResult.Success(parsedUnit)
        } else {
          NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError(siPrefixStr).toFailure()
        }.map { unit ->
          tokens.next()
          createUnitExpression(unit)
        }
      } else {
        NumberWithUnitsParsingResult.Success(baseUnit).map { unit ->
          tokens.next()
          createUnitExpression(
            unit,
            siPrefix = siPrefix ?: NumberUnitExpression.SiPrefix.SI_PREFIX_UNSPECIFIED
          ).toBuilder().build()
        }
      }
    }
  }

  /** Parses a currency prefix token and returns it as a [NumberUnitExpression] with exponent 1. */
  private fun parseCurrencyPrefixUnit(): NumberUnitExpression? {
    val token = tokens.peek() as? Token.Unit ?: return null
    val currencyUnit = CURRENCY_UNITS_BY_PREFIX_ALIAS[token.unit] ?: return null
    tokens.next()
    return createUnitExpression(currencyUnit)
  }

  private fun parseAnyUnit(token: Token.Unit): NumberUnitExpression.Unit? =
    UNITS_BY_SUFFIX_ALIAS[token.unit]

  private fun NumberUnitExpression.Unit.isPrefixable(): Boolean {
    return this == NumberUnitExpression.Unit.METER ||
      this == NumberUnitExpression.Unit.GRAM ||
      this == NumberUnitExpression.Unit.LITER ||
      this == NumberUnitExpression.Unit.RADIAN ||
      this == NumberUnitExpression.Unit.DEGREE ||
      this == NumberUnitExpression.Unit.SECOND ||
      this == NumberUnitExpression.Unit.HERTZ ||
      this == NumberUnitExpression.Unit.MOLE ||
      this == NumberUnitExpression.Unit.CANDELA ||
      this == NumberUnitExpression.Unit.NEWTON ||
      this == NumberUnitExpression.Unit.JOULE ||
      this == NumberUnitExpression.Unit.WATT ||
      this == NumberUnitExpression.Unit.PASCAL ||
      this == NumberUnitExpression.Unit.AMPERE ||
      this == NumberUnitExpression.Unit.VOLT ||
      this == NumberUnitExpression.Unit.OHM
  }

  private fun Token.Unit.extractUnitStartIndex(): Int {
    val match = SI_PREFIX_ALIASES_BY_LENGTH_DESCENDING
      .firstOrNull { prefix ->
        this.unit.length > prefix.length && this.unit.startsWith(prefix)
      }

    return match?.length ?: 0
  }

  private fun parseSiPrefix(symbol: String): NumberUnitExpression.SiPrefix? =
    SI_PREFIXES_BY_ALIAS[symbol]

  private fun isAtSuffixUnit(): Boolean {
    return tokens.peek() is Token.Unit
  }

  private fun NumberUnitExpression.Unit.isCurrencyUnit(): Boolean =
    this == NumberUnitExpression.Unit.DOLLAR ||
      this == NumberUnitExpression.Unit.CENT ||
      this == NumberUnitExpression.Unit.RUPEE ||
      this == NumberUnitExpression.Unit.PAISA

  private fun Token?.isCurrencyPrefixToken(): Boolean =
    this is Token.Unit && unit in CURRENCY_UNITS_BY_PREFIX_ALIAS

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

    /**
     * Returns the canonical string representation of [this] unit, e.g. "km" for a kilo-prefixed
     * meter or "$" for a dollar.
     *
     * This is the inverse of the unit parsing performed by [parseNumberWithUnits]: it uses the same
     * alias tables, so the returned string is always parsed back to an equivalent unit.
     */
    fun NumberUnitExpression.toUnitString(): String {
      val prefixString = SI_PREFIX_ALIASES[siPrefix]?.first() ?: ""
      val unitString =
        (CURRENCY_PREFIX_ALIASES[unit] ?: SUFFIX_UNIT_ALIASES[unit])?.first() ?: ""
      return prefixString + unitString
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

    /**
     * Map of each supported unit to the raw strings that may follow a number to represent it. The
     * first alias of each unit is its canonical form (see [toUnitString]).
     */
    private val SUFFIX_UNIT_ALIASES: Map<NumberUnitExpression.Unit, List<String>> = mapOf(
      NumberUnitExpression.Unit.METER to listOf("m", "meter", "meters"),
      NumberUnitExpression.Unit.SQUARE_METER to listOf("m2"),
      NumberUnitExpression.Unit.CUBIC_METER to listOf("m3"),
      NumberUnitExpression.Unit.INCH to listOf("in", "inch", "inches"),
      NumberUnitExpression.Unit.SQUARE_INCH to listOf("sqin", "sqinch"),
      NumberUnitExpression.Unit.CUBIC_INCH to listOf("cuin"),
      NumberUnitExpression.Unit.FOOT to listOf("ft", "foot", "feet"),
      NumberUnitExpression.Unit.SQUARE_FOOT to listOf("sqft", "sqfeet"),
      NumberUnitExpression.Unit.CUBIC_FOOT to listOf("cuft"),
      NumberUnitExpression.Unit.YARD to listOf("yd", "yard", "yards"),
      NumberUnitExpression.Unit.SQUARE_YARD to listOf("sqyd", "sqyard"),
      NumberUnitExpression.Unit.CUBIC_YARD to listOf("cuyd"),
      NumberUnitExpression.Unit.GRAM to listOf("g", "gram", "grams"),
      NumberUnitExpression.Unit.GRAIN to listOf("gr", "grain", "grains"),
      NumberUnitExpression.Unit.OUNCE to listOf("oz", "ounce", "ounces"),
      NumberUnitExpression.Unit.LITER to
        listOf("L", "l", "lt", "liter", "litre", "liters", "litres"),
      NumberUnitExpression.Unit.CUBIC_CENTIMETER to listOf("cc"),
      NumberUnitExpression.Unit.SECOND to listOf("s", "sec", "secs", "second", "seconds"),
      NumberUnitExpression.Unit.MINUTE to listOf("min", "mins", "minute", "minutes"),
      NumberUnitExpression.Unit.HOUR to listOf("h", "hr", "hrs", "hour", "hours"),
      NumberUnitExpression.Unit.KELVIN to listOf("K", "kelvin"),
      NumberUnitExpression.Unit.CELSIUS to listOf("degC", "celsius"),
      NumberUnitExpression.Unit.HERTZ to listOf("Hz", "hertz"),
      NumberUnitExpression.Unit.RADIAN to listOf("rad", "radian", "radians"),
      NumberUnitExpression.Unit.DEGREE to listOf("deg", "degree", "degrees"),
      NumberUnitExpression.Unit.MOLE to listOf("mol", "mole", "moles"),
      NumberUnitExpression.Unit.CANDELA to listOf("cd", "candela"),
      NumberUnitExpression.Unit.NEWTON to listOf("N", "newton", "newtons"),
      NumberUnitExpression.Unit.JOULE to listOf("J", "joule", "joules"),
      NumberUnitExpression.Unit.WATT to listOf("W", "watt", "watts"),
      NumberUnitExpression.Unit.PASCAL to listOf("Pa", "pascal", "pascals"),
      NumberUnitExpression.Unit.AMPERE to listOf("A", "ampere", "amperes"),
      NumberUnitExpression.Unit.VOLT to listOf("V", "volt", "volts"),
      NumberUnitExpression.Unit.OHM to listOf("ohm", "ohms"),
      NumberUnitExpression.Unit.DOLLAR to
        listOf("USD", "dollar", "dollars", "Dollar", "Dollars"),
      NumberUnitExpression.Unit.CENT to listOf("¢", "cent", "cents", "Cent", "Cents"),
      NumberUnitExpression.Unit.RUPEE to listOf("rupee", "rupees", "Rupee", "Rupees"),
      NumberUnitExpression.Unit.PAISA to listOf("paisa", "paise", "Paisa", "Paise")
    )

    /**
     * Map of each currency unit to the raw strings that may precede a number to represent it. The
     * first alias of each currency is its canonical form (see [toUnitString]), and takes precedence
     * over [SUFFIX_UNIT_ALIASES].
     */
    private val CURRENCY_PREFIX_ALIASES: Map<NumberUnitExpression.Unit, List<String>> = mapOf(
      NumberUnitExpression.Unit.DOLLAR to listOf("$"),
      NumberUnitExpression.Unit.RUPEE to listOf("Rs", "₹")
    )

    /**
     * Map of each supported SI prefix to its abbreviated and full names. The first alias of each
     * prefix is its canonical form (see [toUnitString]).
     */
    private val SI_PREFIX_ALIASES: Map<NumberUnitExpression.SiPrefix, List<String>> = mapOf(
      NumberUnitExpression.SiPrefix.DECA to listOf("da", "deca"),
      NumberUnitExpression.SiPrefix.HECTO to listOf("h", "hecto"),
      NumberUnitExpression.SiPrefix.KILO to listOf("k", "kilo"),
      NumberUnitExpression.SiPrefix.MEGA to listOf("M", "mega"),
      NumberUnitExpression.SiPrefix.GIGA to listOf("G", "giga"),
      NumberUnitExpression.SiPrefix.TERA to listOf("T", "tera"),
      NumberUnitExpression.SiPrefix.PETA to listOf("P", "peta"),
      NumberUnitExpression.SiPrefix.EXA to listOf("E", "exa"),
      NumberUnitExpression.SiPrefix.ZETTA to listOf("Z", "zetta"),
      NumberUnitExpression.SiPrefix.YOTTA to listOf("Y", "yotta"),
      NumberUnitExpression.SiPrefix.DECI to listOf("d", "deci"),
      NumberUnitExpression.SiPrefix.CENTI to listOf("c", "centi"),
      NumberUnitExpression.SiPrefix.MILLI to listOf("m", "milli"),
      NumberUnitExpression.SiPrefix.MICRO to listOf("u", "micro"),
      NumberUnitExpression.SiPrefix.NANO to listOf("n", "nano"),
      NumberUnitExpression.SiPrefix.PICO to listOf("p", "pico"),
      NumberUnitExpression.SiPrefix.FEMTO to listOf("f", "femto"),
      NumberUnitExpression.SiPrefix.ATTO to listOf("a", "atto"),
      NumberUnitExpression.SiPrefix.ZEPTO to listOf("z", "zepto"),
      NumberUnitExpression.SiPrefix.YOCTO to listOf("y", "yocto")
    )

    private val UNITS_BY_SUFFIX_ALIAS = SUFFIX_UNIT_ALIASES.invertAliases()
    private val CURRENCY_UNITS_BY_PREFIX_ALIAS = CURRENCY_PREFIX_ALIASES.invertAliases()
    private val SI_PREFIXES_BY_ALIAS = SI_PREFIX_ALIASES.invertAliases()
    private val SI_PREFIX_ALIASES_BY_LENGTH_DESCENDING =
      SI_PREFIXES_BY_ALIAS.keys.sortedByDescending { it.length }

    /** Returns a map from each alias in [this] map to the value it's an alias of. */
    private fun <T> Map<T, List<String>>.invertAliases(): Map<String, T> =
      entries.flatMap { (value, aliases) -> aliases.map { alias -> alias to value } }.toMap()
  }
}
