package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathTokenizer2.Companion.Token
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.DivideSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.ExponentiationSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.FunctionName
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.LeftParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.MinusSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.MultiplySymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PlusSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PositiveInteger
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PositiveRealNumber
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.RightParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.SquareRootSymbol

class NumericExpressionParser(private val rawExpression: String) {
  private val tokens: PeekableIterator<Token> by lazy {
    PeekableIterator.fromSequence(MathTokenizer2.tokenize(rawExpression))
  }

  fun parse(): MathExpression {
    return parseNumericExpression().also {
      // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate
      // the whole expression).
      if (tokens.hasNext()) throw ParseException()
    }
  }

  private fun parseNumericExpression(): MathExpression {
    // numeric_expression = numeric_add_sub_expression ;
    return parseNumericAddSubExpression()
  }

  // TODO: consider consolidating this with other binary parsing to reduce the overall parser.
  private fun parseNumericAddSubExpression(): MathExpression {
    // numeric_add_sub_expression =
    //     numeric_mult_div_expression , { numeric_add_sub_expression_rhs } ;
    var lastLhs = parseNumericMultDivExpression()
    while (hasNextNumericAddSubExpressionRhs()) {
      // numeric_add_sub_expression_rhs =
      //     numeric_add_expression_rhs | numeric_sub_expression_rhs ;
      val (operator, rhs) = when (tokens.peek()) {
        is PlusSymbol -> MathBinaryOperation.Operator.ADD to parseNumericAddExpressionRhs()
        is MinusSymbol -> MathBinaryOperation.Operator.SUBTRACT to parseNumericSubExpressionRhs()
        else -> throw ParseException()
      }

      // Compute the next LHS if there is further addition/subtraction.
      lastLhs = MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          this.operator = operator
          leftOperand = lastLhs
          rightOperand = rhs
        }.build()
      }.build()
    }
    return lastLhs
  }

  private fun hasNextNumericAddSubExpressionRhs() = when (tokens.peek()) {
    is PlusSymbol, is MinusSymbol -> true
    else -> false
  }

  private fun parseNumericAddExpressionRhs(): MathExpression {
    // numeric_add_expression_rhs = plus_operator , numeric_mult_div_expression ;
    consumeTokenOfType { PlusSymbol }
    return parseNumericMultDivExpression()
  }

  private fun parseNumericSubExpressionRhs(): MathExpression {
    // numeric_sub_expression_rhs = minus_operator , numeric_mult_div_expression ;
    consumeTokenOfType { MinusSymbol }
    return parseNumericMultDivExpression()
  }

  private fun parseNumericMultDivExpression(): MathExpression {
    // numeric_mult_div_expression =
    //     numeric_implicit_mult_expression , { numeric_mult_div_expression_rhs } ;
    var lastLhs = parseNumericExpExpression()
    while (hasNextNumericMultDivExpressionRhs()) {
      // numeric_mult_div_expression_rhs =
      //     numeric_mult_expression_rhs | numeric_div_expression_rhs ;
      val (operator, rhs) = when (tokens.peek()) {
        is MultiplySymbol ->
          MathBinaryOperation.Operator.MULTIPLY to parseNumericMultExpressionRhs()
        is DivideSymbol -> MathBinaryOperation.Operator.DIVIDE to parseNumericDivExpressionRhs()
        else -> MathBinaryOperation.Operator.MULTIPLY to parseNumericImplicitMultExpressionRhs()
      }

      // Compute the next LHS if there is further multiplication/division.
      lastLhs = MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          this.operator = operator
          leftOperand = lastLhs
          rightOperand = rhs
        }.build()
      }.build()
    }
    return lastLhs
  }

  private fun hasNextNumericMultDivExpressionRhs() = when (tokens.peek()) {
    is MultiplySymbol, is DivideSymbol, is FunctionName, is LeftParenthesisSymbol,
    is SquareRootSymbol -> true
    else -> false
  }

  private fun parseNumericMultExpressionRhs(): MathExpression {
    // numeric_mult_expression_rhs =
    //     multiplication_operator , numeric_implicit_mult_expression ;
    consumeTokenOfType { MultiplySymbol }
    return parseNumericExpExpression()
  }

  private fun parseNumericDivExpressionRhs(): MathExpression {
    // numeric_div_expression_rhs =
    //     division_operator , numeric_implicit_mult_expression ;
    consumeTokenOfType { DivideSymbol }
    return parseNumericExpExpression()
  }

  private fun parseNumericImplicitMultExpressionRhs(): MathExpression {
    return parseNumericTermWithoutUnaryWithoutNumber()
  }

  /*private fun parseNumericImplicitMultExpression(): MathExpression {
    // numeric_implicit_mult_expression =
    //     numeric_exp_expression | numeric_term_implicit_mult_expression ;
    // TODO: fix
    val possibleLhs = parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
    return when {
      tokens.peek() is ExponentiationSymbol ->
        parseNumericExpExpression(numericTermWithoutImplicitMultWithNumberWithUnary = possibleLhs)
      hasNextNumericTermImplicitMultExpression() ->
        parseNumericTermImplicitMultExpression(
          numericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus = possibleLhs
        )
      else -> possibleLhs // Nothing follows the term in this production rule.
    }
  }

  private fun parseNumericExpExpression(
    numericTermWithoutImplicitMultWithNumberWithUnary: MathExpression
  ): MathExpression {
    // numeric_exp_expression =
    //     numeric_term_without_implicit_mult_with_number_with_unary_plus_minus
    //     , [ numeric_exp_expression_tail ] ;
    @Suppress("UnnecessaryVariable") // The variable adds extra context for readability.
    val possibleLhs = numericTermWithoutImplicitMultWithNumberWithUnary
    return if (tokens.peek() is ExponentiationSymbol) {
      parseNumericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  // Use tail recursion so that the last exponentiation is evaluated first, and right-to-left
  // associativity can be kept via backtracking.
  private fun parseNumericExpExpressionTail(lhs: MathExpression): MathExpression {
    // numeric_exp_expression_tail =
    //     exponentiation_operator , numeric_exp_expression ;
    consumeTokenOfType { ExponentiationSymbol }
    return MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.EXPONENTIATE
        leftOperand = lhs
        rightOperand =
          parseNumericExpExpression(
            parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
          )
      }.build()
    }.build()
  }

  private fun parseNumericTermImplicitMultExpression(): MathExpression {
    // numeric_term_implicit_mult_expression =
    //     numeric_term_implicit_mult_left_number_expansion_expression
    //     | numeric_term_implicit_mult_right_number_expansion_expression ;

    // TODO: verify associativity and maybe flip it since the recursion probably will result in
    //  right associativity.
    return when {
      hasNextNumber() -> parseNumericTermImplicitMultLeftNumberExpansionExpression()
      else -> parseNumericTermImplicitMultRightNumberExpansionExpression()
    }

//    var lastLhs = parseNumericTermImplicitMultInitialSubexpression().toStandaloneExpression()
//    while (hasNextNumericTermImplicitMultLaterSubexpression()) {
//      val operands = parseNumericTermWithoutImplicitMultWithoutNumberOrUnaryPlusMinus()
//    }
//    do {
      // Compute the next LHS if there is further implicit multiplication.
//      lastLhs = MathExpression.newBuilder().apply {
//        binaryOperation = MathBinaryOperation.newBuilder().apply {
//          operator = MathBinaryOperation.Operator.MULTIPLY
//          leftOperand = lastLhs
//          rightOperand = rhs
//        }.build()
//      }.build()
//    } while (hasNextNumericTermWithoutImplicitMultWithoutNumber())
//    return lastLhs
  }

  private fun parseNumericTermImplicitMultLeftNumberExpansionExpression(): MathExpression {
    // numeric_term_implicit_mult_left_number_expansion_expression =
    //     numeric_term_implicit_mult_expression_with_number_initial_lhs
    //     , numeric_term_implicit_mult_without_number_expression_tail ;
    // TODO: consider consolidating this with below.
    return MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.MULTIPLY
        leftOperand = parseNumericTermImplicitMultExpressionWithNumberInitialLhs()
        rightOperand = parseNumericTermImplicitMultWithoutNumberExpressionTail()
      }.build()
    }.build()
  }

  private fun parseNumericTermImplicitMultRightNumberExpansionExpression(): MathExpression {
    // numeric_term_implicit_mult_right_number_expansion_expression =
    //     numeric_term_implicit_mult_expression_without_number_initial_lhs
    //     , numeric_term_implicit_mult_with_number_expression_tail ;
    return MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.MULTIPLY
        leftOperand = parseNumericTermImplicitMultExpressionWithoutNumberInitialLhs()
        rightOperand = parseNumericTermImplicitMultWithNumberExpressionTail()
      }.build()
    }.build()
  }

//  private fun parseNumericTermImplicitMultInitialSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_initial_subexpression =
    //     numeric_term_implicit_mult_initial_left_number_subexpression
    //     | numeric_term_implicit_mult_initial_right_number_subexpression ;
//    return if (hasNextNumber()) {
//      parseNumericTermImplicitMultInitialLeftNumberSubexpression()
//    } else parseNumericTermImplicitMultInitialRightNumberSubexpression()
//  }

//  private fun parseNumericTermImplicitMultInitialLeftNumberSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_initial_left_number_subexpression =
    //     numeric_term_implicit_mult_expression_with_number_initial_lhs
    //     , numeric_term_implicit_mult_expression_without_number_rhs ;
//    return MultiplicationOperands(
//      first = parseNumericTermImplicitMultExpressionWithNumberInitialLhs(),
//      second = parseNumericTermImplicitMultExpressionWithoutNumberRhs()
//    )
//  }

//  private fun parseNumericTermImplicitMultInitialRightNumberSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_initial_right_number_subexpression =
    //     numeric_term_implicit_mult_expression_without_number_initial_lhs
    //     , numeric_term_implicit_mult_expression_with_number_rhs ;
//    return MultiplicationOperands(
//      first = parseNumericTermImplicitMultExpressionWithoutNumberInitialLhs(),
//      second = parseNumericTermImplicitMultExpressionWithNumberRhs()
//    )
//  }

//  private fun parseNumericTermImplicitMultLaterSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_later_subexpression =
    //     numeric_term_implicit_mult_later_left_number_subexpression
    //     | numeric_term_implicit_mult_later_right_number_subexpression ;
//    return if (hasNextNumber()) {
//      parseNumericTermImplicitMultLaterLeftNumberSubexpression()
//    } else parseNumericTermImplicitMultLaterRightNumberSubexpression()
//  }

//  private fun parseNumericTermImplicitMultLaterLeftNumberSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_later_left_number_subexpression =
    //     numeric_term_implicit_mult_expression_with_number_later_lhs
    //     , numeric_term_implicit_mult_expression_without_number_rhs ;
//    return MultiplicationOperands(
//      first = parseNumericTermImplicitMultExpressionWithNumberLaterLhs(),
//      second = parseNumericTermImplicitMultExpressionWithoutNumberRhs()
//    )
//  }

  // TODO: consider consolidating this with the other implicit multiplication cases.
//  private fun parseNumericTermImplicitMultLaterRightNumberSubexpression(): MultiplicationOperands {
    // numeric_term_implicit_mult_later_right_number_subexpression =
    //     numeric_term_implicit_mult_expression_without_number_later_lhs
    //     , numeric_term_implicit_mult_expression_with_number_rhs ;
//    return MultiplicationOperands(
//      first = parseNumericTermImplicitMultExpressionWithoutNumberLaterLhs(),
//      second = parseNumericTermImplicitMultExpressionWithNumberRhs()
//    )
//  }

  private fun parseNumericTermImplicitMultWithoutNumberExpressionTail(): MathExpression {
    // numeric_term_implicit_mult_without_number_expression_tail =
    //     numeric_term_implicit_mult_expression_without_number_rhs ;
    return parseNumericTermImplicitMultExpressionWithoutNumberRhs()
  }

  private fun parseNumericTermImplicitMultWithNumberExpressionTail(): MathExpression {
    // numeric_term_implicit_mult_with_number_expression_tail =
    //     numeric_term_implicit_mult_expression_with_number_rhs ;
    return parseNumericTermImplicitMultExpressionWithNumberRhs()
  }

  // TODO: consider inlining these for simplicity.
  private fun parseNumericTermImplicitMultExpressionWithNumberInitialLhs(): MathExpression {
    // numeric_term_implicit_mult_expression_with_number_initial_lhs =
    //     numeric_term_without_implicit_mult_with_number_with_unary_plus_minus ;
    return parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
  }

  private fun parseNumericTermImplicitMultExpressionWithoutNumberInitialLhs(): MathExpression {
    // numeric_term_implicit_mult_expression_without_number_initial_lhs =
    //     numeric_term_without_implicit_mult_no_number_with_unary_plus_minus ;
    return parseNumericTermWithoutImplicitMultNoNumberWithUnaryPlusMinus()
  }

  private fun parseNumericTermImplicitMultExpressionWithNumberLaterLhs(): MathExpression {
    // numeric_term_implicit_mult_expression_with_number_later_lhs =
    //     number | numeric_term_implicit_mult_expression_without_number_later_lhs ;
    return when {
      hasNextNumber() -> parseNumber()
      else -> parseNumericTermImplicitMultExpressionWithoutNumberLaterLhs()
    }
  }

  private fun parseNumericTermImplicitMultExpressionWithoutNumberLaterLhs(): MathExpression {
    // numeric_term_implicit_mult_expression_without_number_later_lhs =
    //     numeric_term_without_implicit_mult_no_number_no_unary_plus_minus ;
    return parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
  }

  private fun parseNumericTermImplicitMultExpressionWithNumberRhs(): MathExpression {
    // numeric_term_implicit_mult_expression_with_number_rhs =
    //     ( number | numeric_term_without_implicit_mult_no_number_no_unary_plus_minus )
    //     , [ numeric_term_implicit_mult_without_number_expression_tail ] ;
    val possibleLhs = when {
      hasNextNumber() -> parseNumber()
      else -> parseNumericTermImplicitMultExpressionWithoutNumberRhs()
    }
    // TODO: consider consolidating this with the other rhs method.
    return if (hasNextNumericTermImplicitMultWithoutNumberExpressionTail()) {
      MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          operator = MathBinaryOperation.Operator.MULTIPLY
          leftOperand = possibleLhs
          rightOperand = parseNumericTermImplicitMultWithoutNumberExpressionTail()
        }.build()
      }.build()
    } else possibleLhs
  }

  private fun parseNumericTermImplicitMultExpressionWithoutNumberRhs(): MathExpression {
    // numeric_term_implicit_mult_expression_without_number_rhs =
    //     numeric_term_without_implicit_mult_no_number_no_unary_plus_minus
    //     , [ numeric_term_implicit_mult_with_number_expression_tail ] ;
    val possibleLhs = parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
    return if (hasNextNumericTermImplicitMultWithNumberExpressionTail()) {
      MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          operator = MathBinaryOperation.Operator.MULTIPLY
          leftOperand = possibleLhs
          rightOperand = parseNumericTermImplicitMultWithNumberExpressionTail()
        }.build()
      }.build()
    } else possibleLhs
  }

  private fun parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus(): MathExpression {
    // numeric_term_without_implicit_mult_with_number_with_unary_plus_minus =
    //     number
    //     | numeric_term_without_implicit_mult_no_number_no_unary_plus_minus
    //     | numeric_plus_minus_unary_term_with_number ;
    return when (tokens.peek()) {
      is PositiveInteger, is PositiveRealNumber -> parseNumber()
      is PlusSymbol, is MinusSymbol -> parseNumericPlusMinusUnaryTermWithNumber()
      else -> parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
    }
  }

  private fun parseNumericTermWithoutImplicitMultNoNumberWithUnaryPlusMinus(): MathExpression {
    // numeric_term_without_implicit_mult_no_number_with_unary_plus_minus =
    //     numeric_term_without_implicit_mult_no_number_no_unary_plus_minus
    //     | numeric_plus_minus_unary_term_without_number ;
    return when (tokens.peek()) {
      is PlusSymbol, is MinusSymbol -> parseNumericPlusMinusUnaryTermWithoutNumber()
      else -> parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
    }
  }

  private fun parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus(): MathExpression {
    // numeric_term_without_implicit_mult_no_number_no_unary_plus_minus =
    //     numeric_function_expression
    //     | numeric_group_expression
    //     | numeric_rooted_term ;
    return when (tokens.peek()) {
      is FunctionName -> parseNumericFunctionExpression()
      is LeftParenthesisSymbol -> parseNumericGroupExpression()
      is SquareRootSymbol -> parseNumericRootedTerm()
      else -> throw ParseException()
    }
  }

//  private fun parseNumericTermWithImplicitMult(
//    numericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus: MathExpression
//  ): MathExpression {
    // numeric_term_with_implicit_mult =
    //     numeric_term_implicit_mult_expression_lhs
    //     , { numeric_term_implicit_mult_expression_rhs }- ;
    // numeric_term_implicit_mult_expression_lhs =
    //     numeric_term_without_implicit_mult_with_number_or_unary_plus_minus
//    var lastLhs = numericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus
//    do {
      // numeric_term_implicit_mult_expression_rhs =
      //     numeric_term_without_implicit_mult_without_number_or_unary_plus_minus
//      val rhs = parseNumericTermWithoutImplicitMultWithoutNumberOrUnaryPlusMinus()
//
      // Compute the next LHS if there is further implicit multiplication.
//      lastLhs = MathExpression.newBuilder().apply {
//        binaryOperation = MathBinaryOperation.newBuilder().apply {
//          operator = MathBinaryOperation.Operator.MULTIPLY
//          leftOperand = lastLhs
//          rightOperand = rhs
//        }.build()
//      }.build()
//    } while (hasNextNumericTermWithoutImplicitMultWithoutNumber())
//    return lastLhs
//  }

//  private fun parseNumericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus(): MathExpression {
    // numeric_term_without_implicit_mult_with_number_or_unary_plus_minus =
    //    number
    //    | numeric_term_without_implicit_mult_without_number
    //    | numeric_plus_minus_unary_term;
//    return when (tokens.peek()) {
//      is PositiveInteger, is PositiveRealNumber -> parseNumber()
//      is PlusSymbol, is MinusSymbol -> parseNumericPlusMinusUnaryTerm()
//      else -> parseNumericTermWithoutImplicitMultWithoutNumberOrUnaryPlusMinus()
//    }
//  }

//  private fun parseNumericTermWithoutImplicitMultWithoutNumberOrUnaryPlusMinus(): MathExpression {
    // numeric_term_without_implicit_mult_without_number_or_unary_plus_minus =
    //    numeric_function_expression
    //    | numeric_group_expression
    //    | numeric_rooted_term;
//    return when (tokens.peek()) {
//      is FunctionName -> parseNumericFunctionExpression()
//      is LeftParenthesisSymbol -> parseNumericGroupExpression()
//      is SquareRootSymbol -> parseNumericRootedTerm()
//      else -> throw ParseException()
//    }
//  }

//  private fun hasNextNumericTermWithoutImplicitMultWithoutNumber(): Boolean = when (tokens.peek()) {
//    is FunctionName, is LeftParenthesisSymbol, is SquareRootSymbol -> true
//    else -> false
//  }

  private fun parseNumber(): MathExpression {
    // number = positive_real_number | positive_integer ;
    return MathExpression.newBuilder().apply {
      constant = when (
        val numberToken = consumeNextTokenMatching {
          it is PositiveInteger || it is PositiveRealNumber
        }
      ) {
        is PositiveInteger -> Real.newBuilder().apply {
          integer = numberToken.parsedValue
        }.build()
        is PositiveRealNumber -> Real.newBuilder().apply {
          irrational = numberToken.parsedValue
        }.build()

        // TODO: add error that one of the above was expected. Other error handling should maybe
        //  happen in the same way.
        else -> throw ParseException() // Something went wrong.
      }
    }.build()
  }

  private fun hasNextNumber(): Boolean = when (tokens.peek()) {
    is PositiveInteger, is PositiveRealNumber -> true
    else -> false
  }

  private fun parseNumericFunctionExpression(): MathExpression {
    // numeric_function_expression =
    //     function_name , left_paren , numeric_expression , right_paren ;
    return MathExpression.newBuilder().apply {
      val functionName = expectNextTokenWithType<FunctionName>()
      if (functionName.parsedName != "sqrt") throw ParseException()
      consumeTokenOfType { LeftParenthesisSymbol }
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseNumericExpression()
      }.build()
      consumeTokenOfType { RightParenthesisSymbol }
    }.build()
  }

  private fun parseNumericGroupExpression(): MathExpression {
    // numeric_group_expression = left_paren , numeric_expression , right_paren ;
    consumeTokenOfType { LeftParenthesisSymbol }
    return parseNumericExpression().also {
      consumeTokenOfType { RightParenthesisSymbol }
    }
  }

  private fun parseNumericRootedTerm(): MathExpression {
    // numeric_rooted_term =
    //     square_root_operator
    //     , numeric_term_without_implicit_mult_with_number_with_unary_plus_minus ;
    consumeTokenOfType { SquareRootSymbol }
    return MathExpression.newBuilder().apply {
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
      }.build()
    }.build()
  }

  private fun parseNumericPlusMinusUnaryTermWithNumber(): MathExpression {
    // numeric_plus_minus_unary_term_with_number =
    //     numeric_negated_term_with_number | numeric_positive_term_with_number ;
    return if (tokens.peek() is MinusSymbol) {
      parseNumericNegatedTermWithNumber()
    } else parseNumericPositiveTermWithNumber()
  }

  private fun parseNumericPlusMinusUnaryTermWithoutNumber(): MathExpression {
    // numeric_plus_minus_unary_term_without_number =
    //     numeric_negated_term_without_number
    //     | numeric_positive_term_without_number ;
    return if (tokens.peek() is MinusSymbol) {
      parseNumericNegatedTermWithoutNumber()
    } else parseNumericPositiveTermWithoutNumber()
  }

  // TODO: consider consolidating the similar negated/positive methods.
  private fun parseNumericNegatedTermWithNumber(): MathExpression {
    // numeric_negated_term_with_number =
    //     minus_operator
    //     , numeric_term_without_implicit_mult_with_number_with_unary_plus_minus ;
    consumeTokenOfType { MinusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.NEGATE
        operand = parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
      }.build()
    }.build()
  }

  private fun parseNumericNegatedTermWithoutNumber(): MathExpression {
    // numeric_negated_term_without_number =
    //     minus_operator
    //     , numeric_term_without_implicit_mult_no_number_no_unary_plus_minus ;
    consumeTokenOfType { MinusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.NEGATE
        operand = parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
      }.build()
    }.build()
  }

  private fun parseNumericPositiveTermWithNumber(): MathExpression {
    // numeric_positive_term_with_number =
    //     plus_operator
    //     , numeric_term_without_implicit_mult_with_number_with_unary_plus_minus ;
    consumeTokenOfType { PlusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.POSITIVE
        operand = parseNumericTermWithoutImplicitMultWithNumberWithUnaryPlusMinus()
      }.build()
    }.build()
  }

  private fun parseNumericPositiveTermWithoutNumber(): MathExpression {
    // numeric_positive_term_without_number =
    //     plus_operator
    //     , numeric_term_without_implicit_mult_no_number_no_unary_plus_minus ;
    consumeTokenOfType { PlusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.POSITIVE
        operand = parseNumericTermWithoutImplicitMultNoNumberNoUnaryPlusMinus()
      }.build()
    }.build()
  }

//  private fun parseNumericPlusMinusUnaryTerm(): MathExpression {
    // numeric_plus_minus_unary_term = numeric_negated_term | numeric_positive_term ;
//    return if (tokens.peek() is MinusSymbol) {
//      parseNumericNegatedTerm()
//    } else parseNumericPositiveTerm()
//  }

//  private fun parseNumericNegatedTerm(): MathExpression {
    // numeric_negated_term =
    //     minus_operator , numeric_term_without_implicit_mult_with_number_or_unary_plus_minus ;
//    consumeTokenOfType { MinusSymbol }
//    return MathExpression.newBuilder().apply {
//      unaryOperation = MathUnaryOperation.newBuilder().apply {
//        operator = MathUnaryOperation.Operator.NEGATE
//        operand = parseNumericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus()
//      }.build()
//    }.build()
//  }

//  private fun parseNumericPositiveTerm(): MathExpression {
    // numeric_positive_term =
    //     plus_operator , numeric_term_without_implicit_mult_with_number_or_unary_plus_minus ;
//    consumeTokenOfType { PlusSymbol }
//    return MathExpression.newBuilder().apply {
//      unaryOperation = MathUnaryOperation.newBuilder().apply {
//        operator = MathUnaryOperation.Operator.POSITIVE
//        operand = parseNumericTermWithoutImplicitMultWithNumberOrUnaryPlusMinus()
//      }.build()
//    }.build()
//  }*/

  private fun parseNumericImplicitMultExpression2(): MathExpression {
    // numeric_implicit_mult_expression =
    //     numeric_exp_expression , { numeric_term_without_unary_without_number } ;
    var lastLhs = parseNumericExpExpression()
    while (hasNextNumericTermWithoutUnary()) {
      // Compute the next LHS if there is further implicit multiplication.
      lastLhs = MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          operator = MathBinaryOperation.Operator.MULTIPLY
          leftOperand = lastLhs
          rightOperand = parseNumericTermWithoutUnaryWithoutNumber()
        }.build()
      }.build()
    }
    return lastLhs
  }

  private fun parseNumericExpExpression(): MathExpression {
    // numeric_exp_expression = numeric_term_with_unary , [ numeric_exp_expression_tail ] ;
    val possibleLhs = parseNumericTermWithUnary()
    return when (tokens.peek()) {
      is ExponentiationSymbol -> parseNumericExpExpressionTail(possibleLhs)
      else -> possibleLhs
    }
  }

  // Use tail recursion so that the last exponentiation is evaluated first, and right-to-left
  // associativity can be kept via backtracking.
  private fun parseNumericExpExpressionTail(lhs: MathExpression): MathExpression {
    // numeric_exp_expression_tail = exponentiation_operator , numeric_exp_expression ;
    consumeTokenOfType { ExponentiationSymbol }
    return MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.EXPONENTIATE
        leftOperand = lhs
        rightOperand = parseNumericExpExpression()
      }.build()
    }.build()
  }

  private fun parseNumericTermWithUnary(): MathExpression {
    // numeric_term_with_unary =
    //    number | numeric_term_without_unary_without_number | numeric_plus_minus_unary_term ;
    return when (tokens.peek()) {
      is MinusSymbol, is PlusSymbol -> parseNumericPlusMinusUnaryTerm()
      is PositiveInteger, is PositiveRealNumber -> parseNumber()
      else -> parseNumericTermWithoutUnaryWithoutNumber()
    }
  }

  private fun parseNumericTermWithoutUnaryWithoutNumber(): MathExpression {
    // numeric_term_without_unary_without_number =
    //     numeric_function_expression | numeric_group_expression | numeric_rooted_term ;
    return when (tokens.peek()) {
      is FunctionName -> parseNumericFunctionExpression()
      is LeftParenthesisSymbol -> parseNumericGroupExpression()
      is SquareRootSymbol -> parseNumericRootedTerm()
      else -> throw ParseException()
    }
  }

  private fun hasNextNumericTermWithoutUnary(): Boolean = when (tokens.peek()) {
    is PositiveInteger, is PositiveRealNumber, is FunctionName, is LeftParenthesisSymbol,
    is SquareRootSymbol -> true
    else -> false
  }

  private fun parseNumber(): MathExpression {
    // number = positive_real_number | positive_integer ;
    return MathExpression.newBuilder().apply {
      constant = when (
        val numberToken = consumeNextTokenMatching {
          it is PositiveInteger || it is PositiveRealNumber
        }
      ) {
        is PositiveInteger -> Real.newBuilder().apply {
          integer = numberToken.parsedValue
        }.build()
        is PositiveRealNumber -> Real.newBuilder().apply {
          irrational = numberToken.parsedValue
        }.build()

        // TODO: add error that one of the above was expected. Other error handling should maybe
        //  happen in the same way.
        else -> throw ParseException() // Something went wrong.
      }
    }.build()
  }

  private fun parseNumericFunctionExpression(): MathExpression {
    // numeric_function_expression = function_name , left_paren , numeric_expression , right_paren ;
    return MathExpression.newBuilder().apply {
      val functionName = expectNextTokenWithType<FunctionName>()
      if (functionName.parsedName != "sqrt") throw ParseException()
      consumeTokenOfType { LeftParenthesisSymbol }
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseNumericExpression()
      }.build()
      consumeTokenOfType { RightParenthesisSymbol }
    }.build()
  }

  private fun parseNumericGroupExpression(): MathExpression {
    // numeric_group_expression = left_paren , numeric_expression , right_paren ;
    consumeTokenOfType { LeftParenthesisSymbol }
    return parseNumericExpression().also {
      consumeTokenOfType { RightParenthesisSymbol }
    }
  }

  private fun parseNumericPlusMinusUnaryTerm(): MathExpression {
    // numeric_plus_minus_unary_term = numeric_negated_term | numeric_positive_term ;
    return when (tokens.peek()) {
      is MinusSymbol -> parseNumericNegatedTerm()
      is PlusSymbol -> parseNumericPositiveTerm()
      else -> throw ParseException()
    }
  }

  private fun parseNumericNegatedTerm(): MathExpression {
    // numeric_negated_term = minus_operator , numeric_expression ;
    consumeTokenOfType { MinusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.NEGATE
        operand = parseNumericMultDivExpression()
      }.build()
    }.build()
  }

  private fun parseNumericPositiveTerm(): MathExpression {
    // numeric_positive_term = plus_operator , numeric_expression ;
    consumeTokenOfType { PlusSymbol }
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.POSITIVE
        operand = parseNumericMultDivExpression()
      }.build()
    }.build()
  }

  private fun parseNumericRootedTerm(): MathExpression {
    // numeric_rooted_term = square_root_operator , numeric_term_with_unary ;
    consumeTokenOfType { SquareRootSymbol }
    return MathExpression.newBuilder().apply {
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseNumericTermWithUnary()
      }.build()
    }.build()
  }

  private inline fun <reified T : Token> expectNextTokenWithType(): T {
    return (tokens.next() as? T) ?: throw ParseException()
  }

  private inline fun <reified T : Token> consumeTokenOfType(noinline expected: () -> T): T {
    return (tokens.expectNextValue(expected) as? T) ?: throw ParseException()
  }

  private fun consumeNextTokenMatching(predicate: (Token) -> Boolean): Token {
    return tokens.expectNextMatches(predicate) ?: throw ParseException()
  }

  // TODO: do error handling better than this (& in a way that works better with the types of errors
  //  that we want to show users).
  class ParseException : Exception()

  private data class MultiplicationOperands(
    val first: MathExpression,
    val second: MathExpression
  ) {
    fun toStandaloneExpression(): MathExpression = MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.MULTIPLY
        leftOperand = first
        rightOperand = second
      }.build()
    }.build()
  }
}
