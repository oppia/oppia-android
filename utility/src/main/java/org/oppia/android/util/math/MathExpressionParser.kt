package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathTokenizer.Token.CloseParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.DecimalNumber
import org.oppia.android.util.math.MathTokenizer.Token.Identifier
import org.oppia.android.util.math.MathTokenizer.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Token.OpenParenthesis
import org.oppia.android.util.math.MathTokenizer.Token.Operator
import org.oppia.android.util.math.MathTokenizer.Token.WholeNumber
import java.util.ArrayDeque
import java.util.Stack

private val OPERATOR_PRECEDENCES = mapOf('^' to 4, '*' to 3, '/' to 3, '+' to 2, '-' to 2)
private val LEFT_ASSOCIATIVE_OPERATORS = listOf('*', '/', '+', '-')

class MathExpressionParser {
  companion object {
    sealed class ParseResult {
      data class Success(val mathExpression: MathExpression) : ParseResult()

      data class Failure(val failureReason: String) : ParseResult()
    }

    // TODO: update to support implied multiplication, e.g.: 2x^2.
    fun parseExpression(literalExpression: String): ParseResult {
      // An implementation of the Shunting Yard algorithm adapted to support variables, different
      // number types, and the unary negation operator. References:
      // - https://en.wikipedia.org/wiki/Shunting-yard_algorithm#The_algorithm_in_detail
      // - https://wcipeg.com/wiki/Shunting_yard_algorithm#Unary_operators
      val operatorStack = Stack<ParsedToken>()
      val outputQueue = ArrayDeque<ParsedToken>()
      var lastToken: MathTokenizer.Token? = null
      for (token in MathTokenizer.tokenize(literalExpression)) {
        when (token) {
          is WholeNumber, is DecimalNumber, is Identifier -> {
            outputQueue += ParsedToken(token)
          }
          is Operator -> {
            val precedence = token.getPrecedence()
              ?: return ParseResult.Failure("Encountered unexpected operator: ${token.operator}")
            while (operatorStack.isNotEmpty()) {
              val top = operatorStack.peek()
              if (top.token !is Operator) break
              val topPrecedence = top.token.getPrecedence()
                ?: return ParseResult.Failure(
                  "Encountered unexpected operator: ${top.token.operator}"
                )
              if (topPrecedence < precedence) break
              if (topPrecedence == precedence && !token.isLeftAssociative()) break
              outputQueue += operatorStack.pop()
            }
            // TODO: fix unary.
            if (token.isMinusOperator() && lastToken.doesPreviousTokenIndicateNegation()) {
              operatorStack.push(ParsedToken(token, isUnary = true))
            } else {
              operatorStack.push(ParsedToken(token))
            }
          }
          is OpenParenthesis -> {
            operatorStack.push(ParsedToken(token))
          }
          is CloseParenthesis -> {
            while (operatorStack.isNotEmpty()) {
              val top = operatorStack.peek()
              if (top.token is OpenParenthesis) break
              outputQueue += operatorStack.pop()
            }
            if (operatorStack.isEmpty() || operatorStack.peek().token !is OpenParenthesis) {
              return ParseResult.Failure(
                "Encountered unexpected close parenthesis at index ${token.column} in " +
                  token.source
              )
            }
            // Discard the open parenthesis since it's be finished.
            operatorStack.pop()
          }
          is InvalidToken -> {
            return ParseResult.Failure(
              "Encountered unexpected symbol at index ${token.column} in ${token.source}"
            )
          }
        }
        lastToken = token
      }

      while (operatorStack.isNotEmpty()) {
        when (val top = operatorStack.peek().token) {
          is OpenParenthesis -> return ParseResult.Failure(
            "Encountered unexpected close parenthesis at index ${top.column} in ${top.source}"
          )
          else -> outputQueue += operatorStack.pop()
        }
      }

      // We could alternatively reverse the token stream above & parse prefix notation immediately
      // to avoid a second pass over the tokens (since then the expressions could be created
      // in-line). However, two passes is simpler (and by using postfix notation we can avoid
      // processing tokens that aren't needed if an error occurs during parsing).
      val operandStack = Stack<TokenOrExpression>()
      for (parsedToken in outputQueue) {
        when (parsedToken.token) {
          is WholeNumber, is DecimalNumber, is Identifier -> {
            operandStack.push(TokenOrExpression.TokenWrapper(parsedToken.token))
          }
          is Operator -> {
            if (parsedToken.isUnary) {
              if (parsedToken.token.operator != '-') {
                return ParseResult.Failure(
                  "Encountered unexpected non-negation unary operator: " +
                    parsedToken.token.operator
                )
              }
              val unaryOperationExpression =
                MathExpression.newBuilder()
                  .setUnaryOperation(
                    MathUnaryOperation.newBuilder()
                      .setOperator(MathUnaryOperation.Operator.NEGATION)
                      .assignOperand(operandStack.pop(), MathUnaryOperation.Builder::setOperand)
                  ).build()
              operandStack.push(TokenOrExpression.ExpressionWrapper(unaryOperationExpression))
            } else {
              val rightOperand = operandStack.pop()
              val leftOperand = operandStack.pop()
              val operator = parseBinaryOperator(parsedToken.token)
                ?: return ParseResult.Failure(
                  "Encountered unexpected binary operator: ${parsedToken.token.operator}"
                )
              val binaryOperationExpression =
                MathExpression.newBuilder()
                  .setBinaryOperation(
                    MathBinaryOperation.newBuilder()
                      .setOperator(operator)
                      .assignOperand(leftOperand, MathBinaryOperation.Builder::setLeftOperand)
                      .assignOperand(rightOperand, MathBinaryOperation.Builder::setRightOperand)
                  ).build()
              operandStack.push(TokenOrExpression.ExpressionWrapper(binaryOperationExpression))
            }
          }
          else ->
            return ParseResult.Failure(
              "Encountered unexpected token during parsing: ${parsedToken.token}"
            )
        }
      }

      val finalElement = operandStack.getFinalElement()
          ?: return ParseResult.Failure("Failed to resolve expression tree: $operandStack")
      return ParseResult.Success(finalElement.expression)
    }

    /**
     * Returns the final element of the stack (which should only contain that element) which itself
     * should be an expression, or null if something failed during parsing.
     */
    private fun Stack<TokenOrExpression>.getFinalElement(): TokenOrExpression.ExpressionWrapper? {
      return if (size != 1 || firstElement() !is TokenOrExpression.ExpressionWrapper) null
      else firstElement() as TokenOrExpression.ExpressionWrapper
    }

    private fun <B> B.assignOperand(
      operand: TokenOrExpression,
      setter: B.(MathExpression) -> B
    ): B {
      return when (operand) {
        is TokenOrExpression.TokenWrapper -> this.setter(computeConstantOperand(operand.token))
        is TokenOrExpression.ExpressionWrapper -> this.setter(operand.expression)
      }
    }

    private fun parseBinaryOperator(operator: Operator): MathBinaryOperation.Operator? {
      return when (operator.operator) {
        '+' -> MathBinaryOperation.Operator.ADD
        '-' -> MathBinaryOperation.Operator.SUBTRACT
        '*' -> MathBinaryOperation.Operator.MULTIPLY
        '/' -> MathBinaryOperation.Operator.DIVIDE
        '^' -> MathBinaryOperation.Operator.EXPONENTIATE
        else -> null
      }
    }

    private fun computeConstantOperand(token: MathTokenizer.Token): MathExpression {
      return when (token) {
        is WholeNumber ->
          MathExpression.newBuilder()
            .setConstant(
              Real.newBuilder().setRational(
                  Fraction.newBuilder().setWholeNumber(token.value).setDenominator(1)
              )
            ).build()
        is DecimalNumber ->
          MathExpression.newBuilder()
            .setConstant(Real.newBuilder().setIrrational(token.value))
            .build()
        is Identifier -> MathExpression.newBuilder().setVariable(token.name).build()
        else -> MathExpression.getDefaultInstance() // This case should never happen.
      }
    }

    private sealed class TokenOrExpression {

      data class TokenWrapper(val token: MathTokenizer.Token) : TokenOrExpression()

      data class ExpressionWrapper(val expression: MathExpression) : TokenOrExpression()
    }

    private data class ParsedToken(val token: MathTokenizer.Token, val isUnary: Boolean = false)

    /**
     * Returns whether this token, as a previous token (potentially null for the first token of the
     * stream) indicates that the token immediately following it could be a unary negation operator
     * (if it's the minus operator).
     */
    private fun MathTokenizer.Token?.doesPreviousTokenIndicateNegation(): Boolean {
      // A minus operator at the beginning of the stream, after a group is opened, and after
      // another operator is always a unary negation operator.
      return this == null || this is OpenParenthesis || this is Operator
    }

    private fun Operator.isMinusOperator(): Boolean {
      return operator == '-'
    }

    private fun Operator.getPrecedence(): Int? {
      return OPERATOR_PRECEDENCES[operator]
    }

    private fun Operator.isLeftAssociative(): Boolean {
      return operator in LEFT_ASSOCIATIVE_OPERATORS
    }
  }
}
