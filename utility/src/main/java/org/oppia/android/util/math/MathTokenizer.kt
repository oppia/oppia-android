package org.oppia.android.util.math

import java.lang.IllegalStateException
import java.util.ArrayDeque
import java.util.Locale

private const val DECIMAL_POINT = '.'
private const val LEFT_PARENTHESIS = '('
private const val RIGHT_PARENTHESIS = ')'
private const val CONVENTIONAL_MULTIPLICATION_SIGN = '*'
private const val CONVENTIONAL_DIVISION_SIGN = '/'
private const val FORMAL_MULTIPLICATION_SIGN = '×'
private const val FORMAL_DIVISION_SIGN = '÷'

// Only consider standard horizontal/vertical whitespace.
private val VALID_WHITESPACE = listOf(' ', '\t', '\n', '\r')
private val VALID_OPERATORS = listOf(
  CONVENTIONAL_MULTIPLICATION_SIGN, '-', '+', CONVENTIONAL_DIVISION_SIGN, '^',
  FORMAL_MULTIPLICATION_SIGN, FORMAL_DIVISION_SIGN
)
private val VALID_DIGITS = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

/**
 * Container class for functionality corresponding to tokenization of mathematics expressions.
 *
 * The current tokenization only supports basic polynomials.
 */
class MathTokenizer {
  /** Corresponds to a token that may be found in a tokenized math expression. */
  sealed class Token {
    abstract val source: String
    abstract val column: Int

    /** Returns a human-readable string for this token. */
    abstract fun toReadableString(): String

    /** Corresponds to a valid operator: [*+-/^] */
    data class Operator(
      override val source: String,
      override val column: Int,
      val operator: Char
    ) : Token() {
      override fun toReadableString(): String = operator.toString()
    }

    /** Corresponds to a whole, non-decimal number: [0-9]+ */
    data class WholeNumber(
      override val source: String,
      override val column: Int,
      val value: Int
    ) : Token() {
      override fun toReadableString(): String = value.toString()
    }

    /** Corresponds to a decimal number (note that the decimal is required): ([0-9]*.[0-9]+) */
    data class DecimalNumber(
      override val source: String,
      override val column: Int,
      val value: Double
    ) : Token() {
      override fun toReadableString(): String = value.toString()
    }

    /** Corresponds to an identifier, single or multi-letter (typically for variables). */
    data class Identifier(
      override val source: String,
      override val column: Int,
      val name: String
    ) : Token() {
      override fun toReadableString(): String = name
    }

    /** Corresponds to an open parenthesis: ( */
    data class OpenParenthesis(override val source: String, override val column: Int) : Token() {
      override fun toReadableString(): String = LEFT_PARENTHESIS.toString()
    }

    /** Corresponds to a close parenthesis: ) */
    data class CloseParenthesis(override val source: String, override val column: Int) : Token() {
      override fun toReadableString(): String = RIGHT_PARENTHESIS.toString()
    }

    /** Corresponds to an invalid identifier that was encountered. */
    data class InvalidIdentifier(
      override val source: String,
      override val column: Int,
      val name: String
    ) : Token() {
      override fun toReadableString(): String = "Invalid identifier: $name"
    }

    /** Corresponds to an invalid token that was encountered. */
    data class InvalidToken(
      override val source: String,
      override val column: Int,
      val token: String
    ) : Token() {
      override fun toReadableString(): String = "Invalid token: $token"
    }
  }

  companion object {
    /**
     * Returns an iterable that provides a lazy iterator over tokens that will only parse tokens as
     * requested.
     *
     * Note that the returned iterable is thread-safe, but the iterator it provides is not. Callers
     * should fully tokenize the stream by copying the iterable to list before performing
     * multi-threaded operations on the results, or synchronize access to the iterator.
     *
     * Note also that tokenization is done in a case-insensitive manner.
     *
     * Note also that both single-letter and multi-letter identifiers are supported, but their
     * behaviors differ. Single-letter identifiers support implicit multiplication (e.g. 'xy' is
     * equivalent to 'x*y') but multi-letter identifiers do not (e.g. pilambda) since in the latter
     * case 'pilambda' is treated as a single, unknown identifier. Note that in cases of ambiguity,
     * multi-letter identifiers take precedence. For example, if the provided identifiers are 'p',
     * 'i', and 'pi', then encounters of 'pi' will use the multi-letter identifier rather than p*i.
     *
     * @param allowedIdentifiers a list of acceptable identifiers that can be parsed (these may be
     *     more than one letter long). Any identifiers encountered that aren't part of this list
     *     will result in an invalid identifier token being returned. Note that identifiers must
     *     only contain strings with letters (per the definition of Character.isLetter()). This list
     *     can be empty (in which case all encountered identifiers will be presumed invalid).
     */
    fun tokenize(
      rawLiteral: String,
      allowedIdentifiers: List<String>
    ): Iterable<Token> {
      // Verify that the provided identifiers are all valid.
      for (identifier in allowedIdentifiers) {
        if (identifier.any(Char::isNotLetter)) {
          throw IllegalArgumentException("Identifier contains non-letters: $identifier")
        }
        if (identifier.isEmpty()) {
          throw IllegalArgumentException("Encountered empty identifier in allowed identifier list")
        }
      }

      val lowercaseLiteral = rawLiteral.toLowerCase(Locale.getDefault())
      val lowercaseIdentifiers = allowedIdentifiers.map {
        it.toLowerCase(Locale.getDefault())
      }.toSet()
      return object : Iterable<Token> {
        override fun iterator(): Iterator<Token> =
          Tokenizer(lowercaseLiteral, lowercaseIdentifiers.toList())
      }
    }

    /**
     * Tokenizer for math expressions. Standard whitespace is ignored (including newlines). See
     * subclasses of the Token class for valid tokens & their corresponding patterns.
     *
     * Note that this class is only safe to access on a single thread.
     */
    private class Tokenizer(
      private val source: String,
      private val allowedIdentifiers: List<String>
    ) : Iterator<Token> {
      private val singleLetterIdentifiers: List<Char> by lazy {
        allowedIdentifiers.filter { it.length == 1 }.map(String::first)
      }
      private val multiLetterIdentifiers: List<String> by lazy {
        allowedIdentifiers.filter { it.length > 1 }
      }
      private val parsedIdentifierCache = ArrayDeque<Token>()
      private val buffer = source.toCharArray()
      private var currentIndex = 0
      private var nextToken: Token? = null

      override fun hasNext(): Boolean = maybeParseNextToken()

      override fun next(): Token {
        if (!hasNext()) {
          throw IllegalStateException("Reach end-of-stream")
        }
        val token = checkNotNull(nextToken) {
          "Encountered comodification in iterator: iterator modified during tokenization"
        }

        // Reset the token so the next one can be parsed.
        nextToken = null

        return token
      }

      private fun maybeParseNextToken(): Boolean {
        if (nextToken != null) {
          // There's already a token parsed.
          return true
        }

        // Note that there's hidden caching built in for identifiers: identifier parsing can yield
        // multiple identifiers and only one token is returned at a time, any previously parsed
        // identifiers take top precedent.
        if (parsedIdentifierCache.isEmpty()) {
          // Skip all whitespace before looking for new tokens.
          skipWhitespace()
          if (isAtEof()) {
            // Reach the end of the stream.
            return false
          }
        }

        // Otherwise, there's a token to parse (either there's a pending variable or the
        // end-of-stream has not yet been reached). Parse it & continue.
        nextToken = parseNextToken()
        return true
      }

      /** Returns whether the tokenizer has reached the end of the stream. */
      private fun isAtEof(): Boolean = isEofIndex(currentIndex)

      /** Skips whitespace. May be called if already at the end of the stream. */
      private fun skipWhitespace() {
        advanceIndexTo(seekUntilCharacterNotFound(VALID_WHITESPACE))
      }

      /** Returns the next parsed token. Must not be called at the end of the stream. */
      private fun parseNextToken(): Token {
        // Parse the next token in an order to avoid potential ambiguities (such as between whole &
        // decimal numbers).
        return retrieveNextParsedIdentifier()
          ?: parseOperator()
          ?: parseDecimalNumber()
          ?: parseWholeNumber()
          ?: parseWholeNumber()
          ?: parseIdentifiersAndReturnFirst()
          ?: parseParenthesis()
          ?: parseInvalidToken()
      }

      /**
       * Returns the next identifier in the local cache of parsed identifiers, or null if there are
       * none left/available.
       */
      private fun retrieveNextParsedIdentifier(): Token? = parsedIdentifierCache.poll()

      /**
       * Returns the next operator token or null if the next token is not an operator. Must not be
       * called at the end of the stream.
       */
      private fun parseOperator(): Token? {
        val parsedIndex = currentIndex
        val potentialOperator = peekCharacter()
        if (potentialOperator !in VALID_OPERATORS) {
          // The next character is not a recognized operator.
          return null
        }

        // When interpreting the operator, translate the unicode symbols to conventional symbols to
        // simplify upstream parsing.
        val parsedOperator = when (potentialOperator) {
          FORMAL_MULTIPLICATION_SIGN -> CONVENTIONAL_MULTIPLICATION_SIGN
          FORMAL_DIVISION_SIGN -> CONVENTIONAL_DIVISION_SIGN
          else -> potentialOperator
        }

        skipToken()
        return Token.Operator(source, parsedIndex, parsedOperator)
      }

      /**
       * Returns the next decimal token or null if the next token is not a decimal number. Must not
       * be called at the end of the stream.
       */
      private fun parseDecimalNumber(): Token? {
        val parsedIndex = currentIndex
        val decimalIndex = seekUntilCharacterNotFound(VALID_DIGITS)
        if (isEofIndex(decimalIndex) || buffer[decimalIndex] != DECIMAL_POINT) {
          // There is nothing in the stream looking like: [0-9]*\\.
          return null
        }

        val numberEndIndex = seekUntilCharacterNotFound(VALID_DIGITS, startIndex = decimalIndex + 1)
        if (numberEndIndex == decimalIndex + 1) {
          // There are no digits following the period so this isn't a valid decimal. This may
          // indicate an incorrectly formatted decimal, but the '.' will be picked up in a later
          // token pass.
          return null
        }

        // Either the decimal is something.something, or just .something
        val value = source.substring(
          startIndex = currentIndex,
          endIndex = numberEndIndex
        ).toDouble()
        advanceIndexTo(numberEndIndex)
        return Token.DecimalNumber(source, parsedIndex, value)
      }

      /**
       * Returns the next whole number token or null if the next token is not a whole number. Must
       * not be called at the end of the stream.
       */
      private fun parseWholeNumber(): Token? {
        val parsedIndex = currentIndex
        val numberEndIndex = seekUntilCharacterNotFound(VALID_DIGITS)
        if (currentIndex == numberEndIndex) {
          // The next character is not a digit, so this can't be a whole number.
          return null
        }

        // Ensure the decimal is parsed in base 10 (in case it starts with 0--that shouldn't be
        // interpreted as Octal).
        val value = source.substring(
          startIndex = currentIndex,
          endIndex = numberEndIndex
        ).toInt(radix = 10)
        advanceIndexTo(numberEndIndex)
        return Token.WholeNumber(source, parsedIndex, value)
      }

      /**
       * Parses the next one or more identifiers and returns the first one, caching the others, or
       * returns null if the immediate next token is not an identifier.
       */
      private fun parseIdentifiersAndReturnFirst(): Token? {
        parsedIdentifierCache += parseIdentifiers() ?: return null
        return retrieveNextParsedIdentifier()
      }

      /**
       * Returns the next identifier tokens or null if the next character itself is not a token, or
       * does not indicate one or more following identifier tokens. Must not be called at the end of
       * the stream.
       */
      private fun parseIdentifiers(): List<Token>? {
        val parsedIndex = currentIndex
        val nextNonIdentifierIndex = seekUntil { it.isNotLetter() }
        return when (nextNonIdentifierIndex - parsedIndex) {
          // The next character is something other than a potential identifier.
          0 -> null
          // Trivial case: there's a single letter identifier.
          1 -> listOf(parseSingleLetterIdentifier())
          // Complex case: either this is one multi-letter identifier, multiple single-letter
          // identifiers with implied multiplication, or an invalid multi-letter identifier.
          else ->
            parseValidMultiLetterIdentifier(nextNonIdentifierIndex)
              ?: parseMultipleSingleLetterIdentifiers(nextNonIdentifierIndex)
              ?: listOf(parseInvalidMultiLetterIdentifier(nextNonIdentifierIndex))
        }
      }

      /**
       * Returns the next token of the buffer as a single-letter identifier, or an invalid
       * identifier if the character does not correspond to an allowed single-letter identifier.
       */
      private fun parseSingleLetterIdentifier(): Token {
        val parsedIndex = currentIndex
        val potentialIdentifier = peekCharacter()
        skipToken()
        return maybeParseSingleLetterIdentifier(parsedIndex)
          ?: Token.InvalidIdentifier(source, parsedIndex, potentialIdentifier.toString())
      }

      /**
       * Returns the next token of the buffer as a single-letter identifier, null if the character
       * is not a valid single-letter identifier. Note that this does not change the underlying
       * stream state (i.e. it does not skip the parsed token)--the caller is expected to do that.
       * The caller is also expected to guarantee that the provided parsedIndex is within the
       * buffer.
       */
      private fun maybeParseSingleLetterIdentifier(parsedIndex: Int): Token? {
        val potentialIdentifier = buffer[parsedIndex]
        return if (potentialIdentifier in singleLetterIdentifiers) {
          Token.Identifier(source, parsedIndex, potentialIdentifier.toString())
        } else null
      }

      /**
       * Returns the next set of characters up to nextNonIdentifierIndex as a multi-letter
       * identifier, or null if those characters do not correspond to a valid multi-letter
       * identifier. Note that the returned list will always contain a single token corresponding to
       * the multi-letter identifier, or the whole list will be null if parsing failed.
       */
      private fun parseValidMultiLetterIdentifier(nextNonIdentifierIndex: Int): List<Token>? {
        val parsedIndex = currentIndex
        val potentialIdentifier = extractSubBufferString(
          startIndex = currentIndex,
          endIndex = nextNonIdentifierIndex
        )
        return if (potentialIdentifier in multiLetterIdentifiers) {
          advanceIndexTo(nextNonIdentifierIndex)
          listOf(Token.Identifier(source, parsedIndex, potentialIdentifier))
        } else null
      }

      /**
       * Returns a list of single-letter identifiers for all characters up to the specified index,
       * or null if any characters encountered are not valid single-letter identifiers.
       */
      private fun parseMultipleSingleLetterIdentifiers(nextNonIdentifierIndex: Int): List<Token>? {
        val singleLetterIdentifiers = mutableListOf<Token>()
        for (parsedIndex in currentIndex until nextNonIdentifierIndex) {
          singleLetterIdentifiers += maybeParseSingleLetterIdentifier(parsedIndex) ?: return null
        }
        // Skip all of the characters encountered if each one corresponds to a valid identifier.
        advanceIndexTo(nextNonIdentifierIndex)
        return singleLetterIdentifiers
      }

      /**
       * Returns a token indicating that all characters from the current index to the specified
       * index (but not including that index) correspond to an invalid multi-letter identifier.
       */
      private fun parseInvalidMultiLetterIdentifier(nextNonIdentifierIndex: Int): Token {
        // Assume all characters between currentIndex and nextNonIdentifierIndex (exclusive)
        // comprise a single, unknown multi-letter identifier.
        val parsedIndex = currentIndex
        advanceIndexTo(nextNonIdentifierIndex)
        return Token.InvalidIdentifier(
          source,
          parsedIndex,
          extractSubBufferString(startIndex = parsedIndex, endIndex = nextNonIdentifierIndex)
        )
      }

      /**
       * Returns the next parenthesis token (open or close) or null if the next token is not a
       * parenthesis. Must not be called at the end of the stream.
       */
      private fun parseParenthesis(): Token? {
        val parsedIndex = currentIndex
        return when (peekCharacter()) {
          LEFT_PARENTHESIS -> {
            skipToken()
            Token.OpenParenthesis(source, parsedIndex)
          }
          RIGHT_PARENTHESIS -> {
            skipToken()
            Token.CloseParenthesis(source, parsedIndex)
          }
          else -> null
        }
      }

      /**
       * Returns an invalid token for the next character in the stream. Must not be called at the
       * end of the stream.
       */
      private fun parseInvalidToken(): Token {
        val parsedIndex = currentIndex
        val errorCharacter = peekCharacter()
        // Skip the error token to try and recover to continue tokenizing.
        skipToken()
        return Token.InvalidToken(source, parsedIndex, errorCharacter.toString())
      }

      /**
       * Returns the next character in the buffer. Should only be called when not at the end of the
       * stream.
       */
      private fun peekCharacter(): Char = buffer[currentIndex]

      /**
       * Returns a string representation of a cut of the stream buffer starting at the specified
       * index and up to, but not including, the specified end index. It's assumed the caller
       * ensures that 0 <= startIndex <= endIndex < buffer.size.
       */
      private fun extractSubBufferString(startIndex: Int, endIndex: Int): String {
        return String(chars = buffer, offset = startIndex, length = endIndex - startIndex)
      }

      /** Returns whether the specified index as at the end of the stream. */
      private fun isEofIndex(index: Int): Boolean = index == buffer.size

      /**
       * Advances the current index to the specified index (must be bigger than or equal to current
       * index) and should not exceed the length of the stream.
       */
      private fun advanceIndexTo(newIndex: Int) {
        currentIndex = newIndex.coerceIn(currentIndex..buffer.size)
      }

      /** Skips the next token in the stream. */
      private fun skipToken() = advanceIndexTo(currentIndex + 1)

      /**
       * Returns the index of the first character not matching the specified predicate, or the
       * stream length if the rest of the stream matches.
       */
      private fun seekUntil(startIndex: Int = currentIndex, predicate: (Char) -> Boolean): Int {
        var advanceIndex = startIndex
        while (!isEofIndex(advanceIndex) && !predicate(buffer[advanceIndex])) advanceIndex++
        return advanceIndex
      }

      /**
       * Returns the first index not matching the specified list, or the stream length if the rest
       * of the stream matches.
       */
      private fun seekUntilCharacterNotFound(
        matchingChars: List<Char>,
        startIndex: Int = currentIndex
      ): Int = seekUntil(startIndex) { it !in matchingChars }
    }
  }
}

private fun Char.isNotLetter(): Boolean {
  return !isLetter()
}
