package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathTokenizer.Token.DecimalNumber
import org.oppia.android.util.math.MathTokenizer.Token.Identifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidIdentifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Token.OpenParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.Operator
import org.oppia.android.util.math.MathTokenizer.Token.WholeNumber
import java.util.ArrayDeque
import java.util.Stack

/**
 * Contains functionality for parsing mathematical expressions, including both numeric and
 * polynomial-based algebraic expressions (functions are not currently supported).
 */
class MathExpressionParser {
  companion object {
    /** The result of parsing an expression. See the subclasses for the different possibilities. */
    sealed class ParseResult {
      /** Indicates a successful parse with a corresponding [MathExpression]. */
      data class Success(val mathExpression: MathExpression) : ParseResult()

      // TODO(BenHenning): Replace this with an enum so that UI code can show a reasonable error to
      //  the user.
      /** Indicates the parse failed with a developer-readable string. */
      data class Failure(val failureReason: String) : ParseResult()
    }

    /**
     * Parses the specified raw expression literal with the list of allowed variables and returns a
     * [ParseResult] with either the parsed expression tree or a failure if the expression has an
     * error.
     *
     * Note that this parsing will include some cases of implied multiplication. For example, each
     * of the following cases will result in a valid parse with a multiplication operator despite
     * one not being explicitly present in the expression:
     * - 2x -> 2 * x (note that '2 x' will have the same result)
     * - x2 -> x * 2
     * - (x+1)(x+2) -> (x+1) * (x+2) (note that whitespace between the groups is ignored)
     * - xy -> x * y (note that 'x y' will have the same result)
     * - x(x+1) -> x * (x+1)
     * - 2(x+1) -> 2 * (x+1)
     * - (x+1)x -> (x+1) * x
     * - (x+1)2 -> (x+1) * 2
     *
     * No other cases will result in implied multiplication (including '2 2' which is an invalid
     * expression). However, sometimes these invalid cases can be made valid (e.g. (2)(3) should
     * result in a proper, implied multiplication scenario since this is treated as polynomial
     * multiplication). Note also that long variables (e.g. 'lambda' will be treated the same except
     * they can never have implied multiplication with other variables since the parser cannot
     * reasonably distinguish between different variables that are more than one letter long).
     */
    fun parseExpression(literalExpression: String, allowedVariables: List<String>): ParseResult {
      // An implementation of the Shunting Yard algorithm adapted to support variables, different
      // number types, and the unary negate operator. References:
      // - https://en.wikipedia.org/wiki/Shunting-yard_algorithm#The_algorithm_in_detail
      // - https://wcipeg.com/wiki/Shunting_yard_algorithm#Unary_operators
      val operatorStack = Stack<ParsedToken.Groupable>()
      val outputQueue = ArrayDeque<ParsedToken.Groupable.Computable>()
      var lastToken: MathTokenizer.Token? = null
      for (token in tokenize(literalExpression, allowedVariables)) {
        when (val parsedToken = ParsedToken.parseToken(token, lastToken)) {
          is ParsedToken.Groupable.Computable.Operand -> outputQueue += parsedToken
          is ParsedToken.Groupable.Computable.Operator -> {
            val parsedOperator = parsedToken.parsedOperator
            val precedence = parsedOperator.precedence
            val isUnaryOperator = parsedOperator is ParsedOperator.UnaryOperator
            while (operatorStack.isNotEmpty()) {
              val top = operatorStack.peek()
              if (top !is ParsedToken.Groupable.Computable.Operator) break
              val topPrecedence = top.parsedOperator.precedence
              if (topPrecedence < precedence) break
              if (isUnaryOperator) break // Unary operators do not pop operators.
              if (topPrecedence == precedence &&
                parsedOperator is ParsedOperator.BinaryOperator &&
                parsedOperator.associativity != ParsedOperator.Associativity.LEFT
              ) break
              operatorStack.pop()
              outputQueue += top
            }
            operatorStack.push(parsedToken)
          }
          is ParsedToken.Groupable.OpenParenthesis -> {
            operatorStack.push(parsedToken)
          }
          is ParsedToken.CloseParenthesis -> {
            while (operatorStack.isNotEmpty()) {
              val top = operatorStack.peek()
              // The only non-computable, groupable token is OpenParenthesis.
              if (top !is ParsedToken.Groupable.Computable) break
              operatorStack.pop()
              outputQueue += top
            }
            if (operatorStack.isEmpty() ||
              operatorStack.peek() !is ParsedToken.Groupable.OpenParenthesis
            ) {
              return ParseResult.Failure(
                "Encountered unexpected close parenthesis at index ${token.column} in " +
                  token.source
              )
            }
            // Discard the open parenthesis since it's be finished.
            operatorStack.pop()
          }
          is ParsedToken.FailedToken -> return ParseResult.Failure(parsedToken.getFailureReason())
        }
        lastToken = token
      }

      while (operatorStack.isNotEmpty()) {
        when (val top = operatorStack.peek()) {
          // The only non-computable, groupable token is OpenParenthesis.
          !is ParsedToken.Groupable.Computable -> {
            val openParenthesis = top as ParsedToken.Groupable.OpenParenthesis
            return ParseResult.Failure(
              "Encountered unexpected open parenthesis at index ${openParenthesis.token.column}"
            )
          }
          else -> {
            operatorStack.pop()
            outputQueue += top
          }
        }
      }

      // We could alternatively reverse the token stream above & parse prefix notation immediately
      // to avoid a second pass over the tokens (since then the expressions could be created
      // in-line). However, two passes is simpler (and by using postfix notation we can avoid
      // processing tokens that aren't needed if an error occurs during parsing).
      val operandStack = Stack<MathExpression>()
      for (parsedToken in outputQueue) {
        when (parsedToken) {
          is ParsedToken.Groupable.Computable.Operand ->
            operandStack.push(parsedToken.toMathExpression())
          is ParsedToken.Groupable.Computable.Operator -> when (parsedToken.parsedOperator) {
            is ParsedOperator.UnaryOperator -> {
              if (operandStack.isEmpty()) {
                return ParseResult.Failure("Encountered unary operator without operand")
              }
              operandStack.push(parsedToken.parsedOperator.toMathExpression(operandStack.pop()))
            }
            is ParsedOperator.BinaryOperator -> {
              if (operandStack.size < 2) {
                return ParseResult.Failure("Encountered binary operator with missing operand(s)")
              }
              val rightOperand = operandStack.pop()
              val leftOperand = operandStack.pop()
              operandStack.push(
                parsedToken.parsedOperator.toMathExpression(leftOperand, rightOperand)
              )
            }
          }
        }
      }

      if (operandStack.size != 1) {
        return ParseResult.Failure("Failed to resolve expression tree: $operandStack")
      }
      return ParseResult.Success(operandStack.firstElement())
    }

    /**
     * Returns an iterable of tokens by tokenizing the provided expression & accounting for the list
     * of allowed variables. This uses [MathTokenizer] & augments it by providing selective support
     * for implied multiplication scenarios.
     */
    private fun tokenize(
      literalExpression: String,
      allowedVariables: List<String>
    ): Iterable<MathTokenizer.Token> {
      return MathTokenizer.tokenize(
        rawLiteral = literalExpression, allowedIdentifiers = allowedVariables
      ).adaptTokenStreamForImpliedMultiplication()
    }

    /**
     * Returns a new [Iterable] wrapped around the specified one with additional support for
     * injecting synthesized tokens, as needed, into the token stream in order to support implied
     * multiplication scenarios. See [ImpliedMultiplicationIteratorAdapter] for specifics on how
     * this is implemented & the cases supported.
     */
    private fun Iterable<MathTokenizer.Token>.adaptTokenStreamForImpliedMultiplication():
      Iterable<MathTokenizer.Token> {
        val baseIterable = this
        return object : Iterable<MathTokenizer.Token> {
          override fun iterator(): Iterator<MathTokenizer.Token> =
            ImpliedMultiplicationIteratorAdapter(baseIterable.iterator())
        }
      }

    /**
     * Returns whether this token, as a previous token (potentially null for the first token of the
     * stream) indicates that the token immediately following it could be a unary operator (if other
     * sufficient conditions are met, such as the operator matches an expected unary operator
     * symbol).
     */
    private fun MathTokenizer.Token?.doesSuggestNegationInNextToken(): Boolean {
      // A minus operator at the beginning of the stream, after a group is opened, and after
      // another operator is always a unary negate operator.
      return this == null || this is OpenParenthesis || this is Operator
    }

    /**
     * Corresponds to an interpreted parsing of a mathematical token that can be analyzed and, in
     * some cases, converted to a [MathExpression].
     *
     * Note that this class & its subclasses heavily rely on various levels of sealed class
     * inheritance to tighten the contracts around all types of tokens to facilitate writing highly
     * robust code. See the separate classes to get an idea on how the structure is laid out.
     */
    private sealed class ParsedToken {
      companion object {
        /**
         * Returns a new [ParsedToken] for the specified token (& potentially considering the
         * previous token), or null if the token corresponds to an operator that isn't recognized.
         */
        fun parseToken(token: MathTokenizer.Token, lastToken: MathTokenizer.Token?): ParsedToken? {
          return when (token) {
            is WholeNumber -> Groupable.Computable.Operand.WholeNumber(token.value)
            is DecimalNumber -> Groupable.Computable.Operand.DecimalNumber(token.value)
            is Identifier -> Groupable.Computable.Operand.Identifier(token.name)
            is Operator ->
              Groupable.Computable.Operator(
                ParsedOperator.parseOperator(token, lastToken)
                  ?: return FailedToken.InvalidOperator(token.operator)
              )
            is OpenParenthesis -> Groupable.OpenParenthesis(token)
            is MathTokenizer.Token.CloseParenthesis -> CloseParenthesis
            is InvalidIdentifier -> FailedToken.InvalidIdentifier(token.name)
            is InvalidToken -> FailedToken.InvalidToken(token)
          }
        }
      }

      /**
       * Corresponds to a set of tokens that represent groupable components (e.g. open parenthesis,
       * constants, etc.). Closed parenthesis is not included since that ends a group rather than
       * begins/participates in one.
       *
       * This class exists to simplify error-handling in the shunting-yard algorithm.
       */
      sealed class Groupable : ParsedToken() {
        /**
         * Corresponds to tokens that are computable (that is, can be converted to
         * [MathExpression]s).
         */
        sealed class Computable : Groupable() {
          /** Corresponds to an operand for a unary or binary operation. */
          sealed class Operand : Computable() {
            /** Returns a [MathExpression] representation of this operand. */
            abstract fun toMathExpression(): MathExpression

            /** An operand that is a whole number (e.g. '2'). */
            data class WholeNumber(private val value: Int) : Operand() {
              override fun toMathExpression(): MathExpression =
                MathExpression.newBuilder()
                  .setConstant(
                    Real.newBuilder().setRational(
                      Fraction.newBuilder().setWholeNumber(value).setDenominator(1)
                    )
                  ).build()
            }

            /** An operand that's a decimal (e.g. '3.14'). */
            data class DecimalNumber(private val value: Double) : Operand() {
              override fun toMathExpression(): MathExpression =
                MathExpression.newBuilder()
                  .setConstant(Real.newBuilder().setIrrational(value))
                  .build()
            }

            /**
             * An operand that's an identifier (e.g. 'x') which will likely be treated as a
             * variable.
             */
            data class Identifier(private val name: String) : Operand() {
              override fun toMathExpression(): MathExpression =
                MathExpression.newBuilder().setVariable(name).build()
            }
          }

          /**
           * Corresponds to an operator (binary or unary). See [ParsedOperator] for supported
           * operators.
           */
          data class Operator(val parsedOperator: ParsedOperator) : Computable()
        }

        /** Corresponds to an open parenthesis token which begins a grouped expression. */
        data class OpenParenthesis(val token: MathTokenizer.Token) : Groupable()
      }

      /** Corresponds to a close parenthesis token which ends a grouped expression. */
      object CloseParenthesis : ParsedToken()

      /** Corresponds to a token that represents a failure during tokenization or parsing. */
      sealed class FailedToken : ParsedToken() {
        /**
         * Returns the reason the failure token was created. This is not meant to be shown to end
         * users, only developers.
         */
        abstract fun getFailureReason(): String

        /**
         * Indicates an invalid operator was encountered. This typically means the tokenizer
         * supports operators that the parser does not.
         */
        data class InvalidOperator(val operator: Char) : FailedToken() {
          override fun getFailureReason(): String = "Encountered unexpected operator: $operator"
        }

        /**
         * Indicates an identifier was encountered that doesn't correspond to any of the allowed
         * variables passed to the parser during parsing time.
         */
        data class InvalidIdentifier(val name: String) : FailedToken() {
          override fun getFailureReason(): String = "Encountered invalid identifier: $name"
        }

        /** Indicates an invalid token was encountered during tokenization. */
        data class InvalidToken(private val token: MathTokenizer.Token) : FailedToken() {
          override fun getFailureReason(): String =
            "Encountered unexpected symbol at index ${token.column} in ${token.source}"
        }
      }
    }

    /** Corresponds to an operator parsed from an operator token with defined precedence. */
    private sealed class ParsedOperator(val precedence: Int) {
      companion object {
        /**
         * Returns a new [ParsedOperator] given the specified [Operator], or null if the operator is
         * not recognized.
         *
         * This uses the previous token to determine whether this operator is unary or binary.
         */
        fun parseOperator(operator: Operator, lastToken: MathTokenizer.Token?): ParsedOperator? {
          return when (operator.operator) {
            '+' -> Add
            '-' -> if (lastToken.doesSuggestNegationInNextToken()) Negate else Subtract
            '*' -> Multiply
            '/' -> Divide
            '^' -> Exponentiate
            else -> null
          }
        }
      }

      /**
       * Corresponds to relative associativity for other encountered operations whose operators are
       * at the same level of precedence.
       */
      enum class Associativity {
        LEFT,
        RIGHT,
      }

      /** Corresponds to a binary operation (e.g. 'x + y'). */
      abstract class BinaryOperator(
        precedence: Int,
        val associativity: Associativity,
        private val protoOperator: MathBinaryOperation.Operator
      ) : ParsedOperator(precedence) {
        /** Returns a [MathExpression] representation of this parsed operator. */
        fun toMathExpression(
          leftOperand: MathExpression,
          rightOperand: MathExpression
        ): MathExpression =
          MathExpression.newBuilder()
            .setBinaryOperation(
              MathBinaryOperation.newBuilder()
                .setOperator(protoOperator)
                .setLeftOperand(leftOperand)
                .setRightOperand(rightOperand)
            ).build()
      }

      /** Corresponds to a unary operation (e.g. '-x'). */
      abstract class UnaryOperator(
        precedence: Int,
        private val protoOperator: MathUnaryOperation.Operator
      ) : ParsedOperator(precedence) {
        /** Returns a [MathExpression] representation of this parsed operator. */
        fun toMathExpression(operand: MathExpression): MathExpression =
          MathExpression.newBuilder()
            .setUnaryOperation(
              MathUnaryOperation.newBuilder()
                .setOperator(protoOperator)
                .setOperand(operand)
            ).build()
      }

      /** Corresponds to the addition operation, e.g.: 1 + 2. */
      object Add : BinaryOperator(
        precedence = 1,
        associativity = Associativity.LEFT,
        protoOperator = MathBinaryOperation.Operator.ADD
      )

      /** Corresponds to the subtraction operation, e.g.: 1 - 2. */
      object Subtract : BinaryOperator(
        precedence = Add.precedence,
        associativity = Associativity.LEFT,
        protoOperator = MathBinaryOperation.Operator.SUBTRACT
      )

      /** Corresponds to the multiplication operation, e.g.: 1 * 2. */
      object Multiply : BinaryOperator(
        precedence = Add.precedence + 1,
        associativity = Associativity.LEFT,
        protoOperator = MathBinaryOperation.Operator.MULTIPLY
      )

      /** Corresponds to the division operation, e.g.: 1 / 2. */
      object Divide : BinaryOperator(
        precedence = Multiply.precedence,
        associativity = Associativity.LEFT,
        protoOperator = MathBinaryOperation.Operator.DIVIDE
      )

      /** Corresponds to unary negation, e.g.: -1. */
      object Negate : UnaryOperator(
        precedence = Multiply.precedence + 1,
        protoOperator = MathUnaryOperation.Operator.NEGATE
      )

      /** Corresponds to the exponentiation operation, e.g.: 1 ^ 2. */
      object Exponentiate : BinaryOperator(
        precedence = Negate.precedence + 1,
        associativity = Associativity.RIGHT,
        protoOperator = MathBinaryOperation.Operator.EXPONENTIATE
      )
    }

    /**
     * An adapter of the iterator returned by [MathTokenizer] that will synthesize operators in
     * cases when there's implied multiplication (e.g. 2xy should be interpreted as 2*x*y).
     */
    private class ImpliedMultiplicationIteratorAdapter(
      private val baseIterator: Iterator<MathTokenizer.Token>
    ) : Iterator<MathTokenizer.Token> {
      private var lastToken: MathTokenizer.Token? = null
      private var nextToken: MathTokenizer.Token? = null

      // The base iterator's hasNext() is sufficient since this adapter will only ever synthesize
      // new tokens *before* another token (since the synthesized tokens are always binary
      // operators), except in the end-of-stream case (since the adapter might have a next token
      // saved).
      override fun hasNext(): Boolean = baseIterator.hasNext() || nextToken != null

      override fun next(): MathTokenizer.Token {
        val (currentToken, newNextToken) =
          computeCurrentTokenState(lastToken, nextToken ?: baseIterator.next())
        nextToken = newNextToken
        lastToken = currentToken
        return currentToken
      }

      /**
       * Returns a destructible data object of two elements defining first, the next token to
       * return, and second, the token that should be used the next time next() is called, or null
       * if none should be used (meaning a new element should be retrieved from the backing iterator
       * on the next call to next()).
       *
       * @param lastToken the previous token provided via next(), or null if none
       * @param nextToken the next token that should be provided to the user
       */
      private fun computeCurrentTokenState(
        lastToken: MathTokenizer.Token?,
        nextToken: MathTokenizer.Token
      ): NewTokenState {
        return when {
          lastToken.impliesMultiplicationWith(nextToken) -> NewTokenState(
            currentToken = synthesizeMultiplicationOperatorToken(), nextToken = nextToken
          )
          else -> NewTokenState(currentToken = nextToken, nextToken = null)
        }
      }

      /**
       * Returns a new multiplication operator token to enable the parser to imply multiplication in
       * certain contexts.
       */
      private fun synthesizeMultiplicationOperatorToken(): MathTokenizer.Token {
        return Operator(source = "<synthesized>", column = 0, operator = '*')
      }

      /** Returns whether this token is constant (e.g. a whole number or variable). */
      private fun MathTokenizer.Token.isConstant(): Boolean {
        return this is WholeNumber || this is DecimalNumber
      }

      /**
       * Returns whether this token is a variable (which, at the moment, is determined based on
       * whether it's an identifier since identifiers aren't currently used for any other purpose).
       */
      private fun MathTokenizer.Token.isVariable(): Boolean {
        return this is Identifier
      }

      /**
       * Returns whether this token is a variable or constant (see the corresponding functions above
       * for specifics on what each means in practice).
       */
      private fun MathTokenizer.Token.isVariableOrConstant(): Boolean {
        return this.isVariable() || this.isConstant()
      }

      /**
       * Returns whether this token, in conjunction with the specified token, indicates a scenario
       * where multiplication should be implied. See the implementation for specifics.
       */
      private fun MathTokenizer.Token?.impliesMultiplicationWith(
        nextToken: MathTokenizer.Token
      ): Boolean {
        // Two consecutive tokens imply multiplication iff they are both variables, or one is a
        // variable and the other is a constant. Or, a variable/constant is followed by an open
        // parenthesis or a close parenthesis is followed by a variable/constant. Finally, two
        // consecutive sets of parentheses also imply multiplication.
        return when {
          this == null -> false
          this.isVariable() && nextToken.isVariable() -> true
          this.isConstant() && nextToken.isVariable() -> true
          this.isVariable() && nextToken.isConstant() -> true
          this.isVariableOrConstant() && nextToken is OpenParenthesis -> true
          this is MathTokenizer.Token.CloseParenthesis && nextToken.isVariableOrConstant() -> true
          this is MathTokenizer.Token.CloseParenthesis && nextToken is OpenParenthesis -> true
          else -> false
        }
      }

      /**
       * Temporary data object to signal to the adapter which token to return to the parser & which,
       * if any, to cache for future calls to the iterator.
       */
      private data class NewTokenState(
        val currentToken: MathTokenizer.Token,
        val nextToken: MathTokenizer.Token?
      )
    }
  }
}
