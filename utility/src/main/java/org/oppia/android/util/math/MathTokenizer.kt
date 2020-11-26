package org.oppia.android.util.math

import java.lang.IllegalStateException
import java.util.Locale

// Only consider standard horizontal/vertical whitespace.
private val VALID_WHITESPACE = listOf(' ', '\t', '\n', '\r')
private val VALID_OPERATORS = listOf('*', '-', '+', '/', '^')
private val VALID_DIGITS = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
private val VALID_IDENTIFIER_LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray().toList()

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

    /** Corresponds to a variable identifier (single character): [a-z] */
    data class Identifier(
      override val source: String,
      override val column: Int,
      val name: String
    ) : Token() {
      override fun toReadableString(): String = name
    }

    /** Corresponds to an open parenthesis: ( */
    data class OpenParenthesis(override val source: String, override val column: Int) : Token() {
      override fun toReadableString(): String = "("
    }

    /** Corresponds to a close parenthesis: ) */
    data class CloseParenthesis(override val source: String, override val column: Int) : Token() {
      override fun toReadableString(): String = ")"
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
     * Note also that tokenization is done agnostic of casing.
     */
    fun tokenize(rawLiteral: String): Iterable<Token> {
      return object : Iterable<Token> {
        override fun iterator(): Iterator<Token> =
          Tokenizer(rawLiteral.toLowerCase(Locale.getDefault()))
      }
    }

    /**
     * Tokenizer for math expressions. Standard whitespace is ignored (including newlines). See
     * subclasses of the Token class for valid tokens & their corresponding patterns.
     *
     * Note that this class is only safe to access on a single thread.
     */
    private class Tokenizer(private val source: String) : Iterator<Token> {
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

        // Skip all whitespace before looking for new tokens.
        skipWhitespace()
        if (isAtEof()) {
          // Reach the end of the stream.
          return false
        }

        // There's a token to parse. Parse it & continue.
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
        return parseOperator()
          ?: parseDecimalNumber()
          ?: parseWholeNumber()
          ?: parseWholeNumber()
          ?: parseIdentifier()
          ?: parseParenthesis()
          ?: parseInvalidToken()
      }

      /**
       * Returns the next operator token or null if the next token is not an operator. Must not be
       * called at the end of the stream.
       */
      private fun parseOperator(): Token? {
        val parsedIndex = currentIndex
        val potentialOperator = buffer[currentIndex]
        if (potentialOperator !in VALID_OPERATORS) {
          // The next character is not a recognized operator.
          return null
        }

        skipToken()
        return Token.Operator(source, parsedIndex, potentialOperator)
      }

      /**
       * Returns the next decimal token or null if the next token is not a decimal number. Must not
       * be called at the end of the stream.
       */
      private fun parseDecimalNumber(): Token? {
        val parsedIndex = currentIndex
        val decimalIndex = seekUntilCharacterNotFound(VALID_DIGITS)
        if (isEofIndex(decimalIndex) || buffer[decimalIndex] != '.') {
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
       * Returns the next identifier token or null if the next token is not an identifier. Must not
       * be called at the end of the stream.
       */
      private fun parseIdentifier(): Token? {
        val parsedIndex = currentIndex
        val potentialIdentifier = buffer[currentIndex]
        if (potentialIdentifier !in VALID_IDENTIFIER_LETTERS) {
          // The next character is not a recognized identifier letter.
          return null
        }

        skipToken()
        return Token.Identifier(source, parsedIndex, potentialIdentifier.toString())
      }

      /**
       * Returns the next parenthesis token (open or close) or null if the next token is not a
       * parenthesis. Must not be called at the end of the stream.
       */
      private fun parseParenthesis(): Token? {
        val parsedIndex = currentIndex
        return when (buffer[currentIndex]) {
          '(' -> {
            skipToken()
            Token.OpenParenthesis(source, parsedIndex)
          }
          ')' -> {
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
        val errorCharacter = buffer[currentIndex]
        // Skip the error token to try and recover to continue tokenizing.
        skipToken()
        return Token.InvalidToken(source, parsedIndex, errorCharacter.toString())
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
       * Returns the first index not matching the specified list, or the stream length if the rest
       * of the stream matches.
       */
      private fun seekUntilCharacterNotFound(
        matchingChars: List<Char>,
        startIndex: Int = currentIndex
      ): Int {
        var advanceIndex = startIndex
        while (!isEofIndex(advanceIndex) && buffer[advanceIndex] in matchingChars) advanceIndex++
        return advanceIndex
      }
    }
  }
}
