package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathExpressionParser.ParseContext.AlgebraicExpressionContext
import org.oppia.android.util.math.MathExpressionParser.ParseContext.NumericExpressionContext
import org.oppia.android.util.math.MathParsingError.DisabledVariablesInUseError
import org.oppia.android.util.math.MathParsingError.EquationHasTooManyEqualsError
import org.oppia.android.util.math.MathParsingError.EquationIsMissingEqualsError
import org.oppia.android.util.math.MathParsingError.EquationMissingLhsOrRhsError
import org.oppia.android.util.math.MathParsingError.ExponentIsVariableExpressionError
import org.oppia.android.util.math.MathParsingError.ExponentTooLargeError
import org.oppia.android.util.math.MathParsingError.FunctionNameIncompleteError
import org.oppia.android.util.math.MathParsingError.GenericError
import org.oppia.android.util.math.MathParsingError.HangingSquareRootError
import org.oppia.android.util.math.MathParsingError.InvalidFunctionInUseError
import org.oppia.android.util.math.MathParsingError.MultipleRedundantParenthesesError
import org.oppia.android.util.math.MathParsingError.NestedExponentsError
import org.oppia.android.util.math.MathParsingError.NoVariableOrNumberAfterBinaryOperatorError
import org.oppia.android.util.math.MathParsingError.NoVariableOrNumberBeforeBinaryOperatorError
import org.oppia.android.util.math.MathParsingError.NumberAfterVariableError
import org.oppia.android.util.math.MathParsingError.RedundantParenthesesForIndividualTermsError
import org.oppia.android.util.math.MathParsingError.SingleRedundantParenthesesError
import org.oppia.android.util.math.MathParsingError.SpacesBetweenNumbersError
import org.oppia.android.util.math.MathParsingError.SubsequentBinaryOperatorsError
import org.oppia.android.util.math.MathParsingError.SubsequentUnaryOperatorsError
import org.oppia.android.util.math.MathParsingError.TermDividedByZeroError
import org.oppia.android.util.math.MathParsingError.UnbalancedParenthesesError
import org.oppia.android.util.math.MathParsingError.UnnecessarySymbolsError
import org.oppia.android.util.math.MathParsingError.VariableInNumericExpressionError
import org.oppia.android.util.math.MathTokenizer.Companion.BinaryOperatorToken
import org.oppia.android.util.math.MathTokenizer.Companion.Token
import org.oppia.android.util.math.MathTokenizer.Companion.Token.DivideSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.EqualsSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.ExponentiationSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.FunctionName
import org.oppia.android.util.math.MathTokenizer.Companion.Token.IncompleteFunctionName
import org.oppia.android.util.math.MathTokenizer.Companion.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer.Companion.Token.LeftParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.MinusSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.MultiplySymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PlusSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PositiveInteger
import org.oppia.android.util.math.MathTokenizer.Companion.Token.PositiveRealNumber
import org.oppia.android.util.math.MathTokenizer.Companion.Token.RightParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.SquareRootSymbol
import org.oppia.android.util.math.MathTokenizer.Companion.Token.VariableName
import org.oppia.android.util.math.PeekableIterator.Companion.toPeekableIterator
import kotlin.math.absoluteValue
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

/**
 * Parser for numeric expressions, algebraic expressions, and algebraic equations.
 *
 * Note that this parser is guaranteed to be LL(1), and to perform a series of robust error checks
 * against invalid string expressions. The implementation is specifically designed to ensure an
 * LL(1) grammar for both simplicity and long-term maintainability (as it's likely additional
 * functionality will need to be added to the language).
 *
 * To use the parser:
 * - Call [parseNumericExpression] for numeric expressions
 * - Call [parseAlgebraicExpression] for algebraic expressions
 * - Call [parseAlgebraicEquation] for algebraic equations
 *
 * For the formal grammar specification, see:
 * https://docs.google.com/document/d/1JMpbjqRqdEpye67HvDoqBo_rtScY9oEaB7SwKBBspss/edit#bookmark=id.wtmim9gp20a6.
 */
class MathExpressionParser private constructor(private val parseContext: ParseContext) {
  private fun parseGenericEquationGrammar(): MathParsingResult<MathEquation> {
    // generic_equation_grammar = generic_equation ;
    return parseGenericEquation().maybeFail { equation ->
      checkForLearnerErrors(equation.leftSide) ?: checkForLearnerErrors(equation.rightSide)
    }
  }

  /**
   * Returns a parsed [MathParsingResult] of [MathExpression]  from the current [ParseContext].
   *
   * Note that 'generic' here and elsewhere means that it can either be a 'numeric' or 'algebraic'
   * expression (the specifics are handled lower in the parsing call tree). Generic methods are used
   * to share common parsing logic to reduce the overall size of the parser.
   */
  private fun parseGenericExpressionGrammar(): MathParsingResult<MathExpression> {
    // generic_expression_grammar = generic_expression ;
    return parseGenericExpression().maybeFail { expression -> checkForLearnerErrors(expression) }
  }

  private fun parseGenericEquation(): MathParsingResult<MathEquation> {
    // algebraic_equation = generic_expression , equals_operator , generic_expression ;

    if (parseContext.hasNextTokenOfType<EqualsSymbol>()) {
      // If equals starts the string, then there's no LHS.
      return EquationMissingLhsOrRhsError.toFailure()
    }

    val lhsResult = parseGenericExpression().maybeFail {
      // An equals sign must be present.
      if (!parseContext.hasNextTokenOfType<EqualsSymbol>()) {
        EquationIsMissingEqualsError
      } else null
    }.also {
      parseContext.consumeTokenOfType<EqualsSymbol>()
    }.maybeFail {
      if (!parseContext.hasMoreTokens()) {
        // If there are no tokens following the equals symbol, then there's no RHS.
        EquationMissingLhsOrRhsError
      } else null
    }

    val rhsResult = lhsResult.flatMap { parseGenericExpression() }
    return lhsResult.combineWith(rhsResult) { lhs, rhs ->
      MathEquation.newBuilder().apply {
        leftSide = lhs
        rightSide = rhs
      }.build()
    }
  }

  private fun parseGenericExpression(): MathParsingResult<MathExpression> {
    // generic_expression = generic_add_sub_expression ;
    return parseGenericAddSubExpression()
  }

  private fun parseGenericAddSubExpression(): MathParsingResult<MathExpression> {
    // generic_add_sub_expression =
    //     generic_mult_div_expression , { generic_add_sub_expression_rhs } ;
    return parseGenericBinaryExpression(
      parseLhs = this::parseGenericMultDivExpression
    ) { nextToken ->
      // generic_add_sub_expression_rhs =
      //     generic_add_expression_rhs | generic_sub_expression_rhs ;
      when (nextToken) {
        is PlusSymbol -> BinaryOperationRhs(
          operator = ADD,
          rhsResult = parseGenericAddExpressionRhs()
        )
        is MinusSymbol -> BinaryOperationRhs(
          operator = SUBTRACT,
          rhsResult = parseGenericSubExpressionRhs()
        )
        is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
        is ExponentiationSymbol, is FunctionName, is InvalidToken, is LeftParenthesisSymbol,
        is MultiplySymbol, is RightParenthesisSymbol, is SquareRootSymbol, is VariableName,
        is IncompleteFunctionName, null -> null
      }
    }
  }

  private fun parseGenericAddExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_add_expression_rhs = plus_operator , generic_mult_div_expression ;
    return parseContext.consumeTokenOfType<PlusSymbol>().maybeFail { token ->
      if (!parseContext.hasMoreTokens()) {
        NoVariableOrNumberAfterBinaryOperatorError(ADD, parseContext.extractSubexpression(token))
      } else null
    }.flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun parseGenericSubExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_sub_expression_rhs = minus_operator , generic_mult_div_expression ;
    return parseContext.consumeTokenOfType<MinusSymbol>().maybeFail { token ->
      if (!parseContext.hasMoreTokens()) {
        NoVariableOrNumberAfterBinaryOperatorError(
          SUBTRACT, parseContext.extractSubexpression(token)
        )
      } else null
    }.flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun parseGenericMultDivExpression(): MathParsingResult<MathExpression> {
    // generic_mult_div_expression =
    //     generic_exp_expression , { generic_mult_div_expression_rhs } ;
    return parseGenericBinaryExpression(
      parseLhs = this::parseGenericExpExpression
    ) { nextToken ->
      // generic_mult_div_expression_rhs =
      //     generic_mult_expression_rhs
      //     | generic_div_expression_rhs
      //     | generic_implicit_mult_expression_rhs ;
      when (nextToken) {
        is MultiplySymbol -> BinaryOperationRhs(
          operator = MULTIPLY,
          rhsResult = parseGenericMultExpressionRhs()
        )
        is DivideSymbol -> BinaryOperationRhs(
          operator = DIVIDE,
          rhsResult = parseGenericDivExpressionRhs()
        )
        is FunctionName, is LeftParenthesisSymbol, is SquareRootSymbol -> BinaryOperationRhs(
          operator = MULTIPLY,
          rhsResult = parseGenericImplicitMultExpressionRhs(),
          isImplicit = true
        )
        is VariableName -> {
          if (parseContext is AlgebraicExpressionContext) {
            BinaryOperationRhs(
              operator = MULTIPLY,
              rhsResult = parseGenericImplicitMultExpressionRhs(),
              isImplicit = true
            )
          } else null
        }
        // Not a match to the expression.
        is PositiveInteger, is PositiveRealNumber, is EqualsSymbol, is ExponentiationSymbol,
        is InvalidToken, is MinusSymbol, is PlusSymbol, is RightParenthesisSymbol,
        is IncompleteFunctionName, null -> null
      }
    }
  }

  private fun parseGenericMultExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_mult_expression_rhs = multiplication_operator , generic_exp_expression ;
    return parseContext.consumeTokenOfType<MultiplySymbol>().maybeFail { token ->
      if (!parseContext.hasMoreTokens()) {
        NoVariableOrNumberAfterBinaryOperatorError(
          MULTIPLY, parseContext.extractSubexpression(token)
        )
      } else null
    }.flatMap {
      parseGenericExpExpression()
    }
  }

  private fun parseGenericDivExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_div_expression_rhs = division_operator , generic_exp_expression ;
    return parseContext.consumeTokenOfType<DivideSymbol>().maybeFail { token ->
      if (!parseContext.hasMoreTokens()) {
        NoVariableOrNumberAfterBinaryOperatorError(DIVIDE, parseContext.extractSubexpression(token))
      } else null
    }.flatMap {
      parseGenericExpExpression()
    }
  }

  private fun parseGenericImplicitMultExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_implicit_mult_expression_rhs is either numeric_implicit_mult_expression_rhs or
    // algebraic_implicit_mult_or_exp_expression_rhs depending on the current parser context.
    return when (parseContext) {
      is NumericExpressionContext -> parseNumericImplicitMultExpressionRhs()
      is AlgebraicExpressionContext -> parseAlgebraicImplicitMultOrExpExpressionRhs()
    }
  }

  private fun parseNumericImplicitMultExpressionRhs(): MathParsingResult<MathExpression> {
    // numeric_implicit_mult_expression_rhs = generic_term_without_unary_without_number ;
    return parseGenericTermWithoutUnaryWithoutNumber()
  }

  private fun parseAlgebraicImplicitMultOrExpExpressionRhs(): MathParsingResult<MathExpression> {
    // algebraic_implicit_mult_or_exp_expression_rhs =
    //     generic_term_without_unary_without_number , [ generic_exp_expression_tail ] ;
    val possibleLhs = parseGenericTermWithoutUnaryWithoutNumber()
    return if (parseContext.hasNextTokenOfType<ExponentiationSymbol>()) {
      parseGenericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  private fun parseGenericExpExpression(): MathParsingResult<MathExpression> {
    // generic_exp_expression = generic_term_with_unary , [ generic_exp_expression_tail ] ;
    val possibleLhs = parseGenericTermWithUnary()
    return if (parseContext.hasNextTokenOfType<ExponentiationSymbol>()) {
      parseGenericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  // Use tail recursion so that the last exponentiation is evaluated first, and right-to-left
  // associativity can be kept via backtracking.
  private fun parseGenericExpExpressionTail(
    lhsResult: MathParsingResult<MathExpression>
  ): MathParsingResult<MathExpression> {
    // generic_exp_expression_tail = exponentiation_operator , generic_exp_expression ;
    return BinaryOperationRhs(
      operator = EXPONENTIATE,
      rhsResult = lhsResult.flatMap {
        parseContext.consumeTokenOfType<ExponentiationSymbol>()
      }.maybeFail { token ->
        if (!parseContext.hasMoreTokens()) {
          NoVariableOrNumberAfterBinaryOperatorError(
            EXPONENTIATE, parseContext.extractSubexpression(token)
          )
        } else null
      }.flatMap {
        parseGenericExpExpression()
      }
    ).computeBinaryOperationExpression(lhsResult)
  }

  private fun parseGenericTermWithUnary(): MathParsingResult<MathExpression> {
    // generic_term_with_unary =
    //    number | generic_term_without_unary_without_number | generic_plus_minus_unary_term ;
    return when (val nextToken = parseContext.peekToken()) {
      is MinusSymbol, is PlusSymbol -> parseGenericPlusMinusUnaryTerm()
      is PositiveInteger, is PositiveRealNumber -> parseNumber().takeUnless {
        parseContext.hasNextTokenOfType<PositiveInteger>() ||
          parseContext.hasNextTokenOfType<PositiveRealNumber>()
      } ?: SpacesBetweenNumbersError.toFailure()
      is FunctionName, is LeftParenthesisSymbol, is SquareRootSymbol, is VariableName ->
        parseGenericTermWithoutUnaryWithoutNumber()
      is DivideSymbol, is ExponentiationSymbol, is MultiplySymbol -> {
        val previousToken = parseContext.getPreviousToken()
        when {
          previousToken is BinaryOperatorToken -> {
            SubsequentBinaryOperatorsError(
              operator1 = parseContext.extractSubexpression(previousToken),
              operator2 = parseContext.extractSubexpression(nextToken)
            ).toFailure()
          }
          nextToken is BinaryOperatorToken -> {
            NoVariableOrNumberBeforeBinaryOperatorError(
              operator = nextToken.getBinaryOperator(),
              operatorSymbol = parseContext.extractSubexpression(nextToken)
            ).toFailure()
          }
          else -> GenericError.toFailure()
        }
      }
      is EqualsSymbol -> {
        if (parseContext is AlgebraicExpressionContext && parseContext.isPartOfEquation) {
          EquationHasTooManyEqualsError.toFailure()
        } else GenericError.toFailure()
      }
      is IncompleteFunctionName -> nextToken.toFailure()
      is InvalidToken -> nextToken.toFailure()
      is RightParenthesisSymbol, null -> GenericError.toFailure()
    }
  }

  private fun parseGenericTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // generic_term_without_unary_without_number is either numeric_term_without_unary_without_number
    // or algebraic_term_without_unary_without_number based the current parser context.
    return when (parseContext) {
      is NumericExpressionContext -> parseNumericTermWithoutUnaryWithoutNumber()
      is AlgebraicExpressionContext -> parseAlgebraicTermWithoutUnaryWithoutNumber()
    }
  }

  private fun parseNumericTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // numeric_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term ;
    return when (val nextToken = parseContext.peekToken()) {
      is FunctionName -> parseGenericFunctionExpression()
      is LeftParenthesisSymbol -> parseGenericGroupExpression()
      is SquareRootSymbol -> parseGenericRootedTerm()
      is VariableName -> VariableInNumericExpressionError.toFailure()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
      is RightParenthesisSymbol, null -> GenericError.toFailure()
      is IncompleteFunctionName -> nextToken.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseAlgebraicTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // algebraic_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term | variable ;
    return when (val nextToken = parseContext.peekToken()) {
      is FunctionName -> parseGenericFunctionExpression()
      is LeftParenthesisSymbol -> parseGenericGroupExpression()
      is SquareRootSymbol -> parseGenericRootedTerm()
      is VariableName -> parseVariable()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
      is RightParenthesisSymbol, null -> GenericError.toFailure()
      is IncompleteFunctionName -> nextToken.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseGenericFunctionExpression(): MathParsingResult<MathExpression> {
    // generic_function_expression = function_name , left_paren , generic_expression , right_paren ;
    val funcNameResult =
      parseContext.consumeTokenOfType<FunctionName>().maybeFail { functionName ->
        when {
          !functionName.isAllowedFunction -> InvalidFunctionInUseError(functionName.parsedName)
          functionName.parsedName == "sqrt" -> null
          else -> GenericError
        }
      }.also {
        parseContext.consumeTokenOfType<LeftParenthesisSymbol>()
      }
    val argResult = funcNameResult.flatMap { parseGenericExpression() }
    val rightParenResult =
      argResult.flatMap {
        parseContext.consumeTokenOfType<RightParenthesisSymbol> { UnbalancedParenthesesError }
      }
    return funcNameResult.combineWith(argResult, rightParenResult) { funcName, arg, rightParen ->
      MathExpression.newBuilder().apply {
        parseStartIndex = funcName.startIndex
        parseEndIndex = rightParen.endIndex
        functionCall = MathFunctionCall.newBuilder().apply {
          functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
          argument = arg
        }.build()
      }.build()
    }
  }

  private fun parseGenericGroupExpression(): MathParsingResult<MathExpression> {
    // generic_group_expression = left_paren , generic_expression , right_paren ;
    val leftParenResult = parseContext.consumeTokenOfType<LeftParenthesisSymbol>()
    val expResult =
      leftParenResult.flatMap {
        if (parseContext.hasMoreTokens()) {
          parseGenericExpression()
        } else UnbalancedParenthesesError.toFailure()
      }
    val rightParenResult =
      expResult.flatMap {
        parseContext.consumeTokenOfType<RightParenthesisSymbol> { UnbalancedParenthesesError }
      }
    return leftParenResult.combineWith(expResult, rightParenResult) { leftParen, exp, rightParen ->
      MathExpression.newBuilder().apply {
        parseStartIndex = leftParen.startIndex
        parseEndIndex = rightParen.endIndex
        group = exp
      }.build()
    }
  }

  private fun parseGenericPlusMinusUnaryTerm(): MathParsingResult<MathExpression> {
    // generic_plus_minus_unary_term = generic_negated_term | generic_positive_term ;
    return when (val nextToken = parseContext.peekToken()) {
      is MinusSymbol -> parseGenericNegatedTerm()
      is PlusSymbol -> parseGenericPositiveTerm()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is FunctionName, is LeftParenthesisSymbol, is MultiplySymbol,
      is RightParenthesisSymbol, is SquareRootSymbol, is VariableName, null ->
        GenericError.toFailure()
      is IncompleteFunctionName -> nextToken.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseGenericNegatedTerm(): MathParsingResult<MathExpression> {
    // generic_negated_term = minus_operator , generic_exp_expression ;
    val minusResult = parseContext.consumeTokenOfType<MinusSymbol>()
    val expResult = minusResult.flatMap { parseGenericExpExpression() }
    return minusResult.combineWith(expResult) { minus, op ->
      MathExpression.newBuilder().apply {
        parseStartIndex = minus.startIndex
        parseEndIndex = op.parseEndIndex
        unaryOperation = MathUnaryOperation.newBuilder().apply {
          operator = NEGATE
          operand = op
        }.build()
      }.build()
    }
  }

  private fun parseGenericPositiveTerm(): MathParsingResult<MathExpression> {
    // generic_positive_term = plus_operator , generic_exp_expression ;
    val plusResult = parseContext.consumeTokenOfType<PlusSymbol>()
    val expResult = plusResult.flatMap { parseGenericExpExpression() }
    return plusResult.combineWith(expResult) { plus, op ->
      MathExpression.newBuilder().apply {
        parseStartIndex = plus.startIndex
        parseEndIndex = op.parseEndIndex
        unaryOperation = MathUnaryOperation.newBuilder().apply {
          operator = POSITIVE
          operand = op
        }.build()
      }.build()
    }
  }

  private fun parseGenericRootedTerm(): MathParsingResult<MathExpression> {
    // generic_rooted_term = square_root_operator , generic_term_with_unary ;
    val sqrtResult =
      parseContext.consumeTokenOfType<SquareRootSymbol>().maybeFail {
        if (!parseContext.hasMoreTokens()) HangingSquareRootError else null
      }
    val expResult = sqrtResult.flatMap { parseGenericTermWithUnary() }
    return sqrtResult.combineWith(expResult) { sqrtSymbol, op ->
      MathExpression.newBuilder().apply {
        parseStartIndex = sqrtSymbol.startIndex
        parseEndIndex = op.parseEndIndex
        functionCall = MathFunctionCall.newBuilder().apply {
          functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
          argument = op
        }.build()
      }.build()
    }
  }

  private fun parseNumber(): MathParsingResult<MathExpression> {
    // number = positive_real_number | positive_integer ;
    return when (val nextToken = parseContext.peekToken()) {
      is PositiveInteger -> {
        parseContext.consumeTokenOfType<PositiveInteger>().map { positiveInteger ->
          MathExpression.newBuilder().apply {
            parseStartIndex = positiveInteger.startIndex
            parseEndIndex = positiveInteger.endIndex
            constant = positiveInteger.toReal()
          }.build()
        }
      }
      is PositiveRealNumber -> {
        parseContext.consumeTokenOfType<PositiveRealNumber>().map { positiveRealNumber ->
          MathExpression.newBuilder().apply {
            parseStartIndex = positiveRealNumber.startIndex
            parseEndIndex = positiveRealNumber.endIndex
            constant = positiveRealNumber.toReal()
          }.build()
        }
      }
      is DivideSymbol, is EqualsSymbol, is ExponentiationSymbol, is FunctionName,
      is LeftParenthesisSymbol, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
      is RightParenthesisSymbol, is SquareRootSymbol, is VariableName, null ->
        GenericError.toFailure()
      is IncompleteFunctionName -> nextToken.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseVariable(): MathParsingResult<MathExpression> {
    val variableNameResult =
      parseContext.consumeTokenOfType<VariableName>().maybeFail { variableName ->
        return@maybeFail if (parseContext.hasMoreTokens()) {
          when (val nextToken = parseContext.peekToken()) {
            is PositiveInteger ->
              NumberAfterVariableError(nextToken.toReal(), variableName.parsedName)
            is PositiveRealNumber ->
              NumberAfterVariableError(nextToken.toReal(), variableName.parsedName)
            else -> null
          }
        } else null
      }
    return variableNameResult.map { variableName ->
      MathExpression.newBuilder().apply {
        parseStartIndex = variableName.startIndex
        parseEndIndex = variableName.endIndex
        variable = variableName.parsedName
      }.build()
    }
  }

  private fun parseGenericBinaryExpression(
    parseLhs: () -> MathParsingResult<MathExpression>,
    parseRhs: (Token?) -> BinaryOperationRhs?
  ): MathParsingResult<MathExpression> {
    var lastLhsResult = parseLhs()
    while (!lastLhsResult.isFailure()) {
      // Compute the next LHS if there are further RHS expressions.
      lastLhsResult =
        parseRhs(parseContext.peekToken())
        ?.computeBinaryOperationExpression(lastLhsResult)
        ?: break // Not a match to the expression.
    }
    return lastLhsResult
  }

  private fun checkForLearnerErrors(expression: MathExpression): MathParsingError? {
    // Note that the order of checks here is important since errors have precedence, and some are
    // redundant and, in the wrong order, may cause the wrong error to be returned.
    val includeOptionalErrors = parseContext.errorCheckingMode.includesOptionalErrors()
    val optionalError = if (includeOptionalErrors) {
      checkForFirstRedundantGroupError(expression)
        ?: checkForWholeExpressionGroupRedundancy(expression)
        ?: checkForRedundantGroupError(expression)
        ?: checkForRedundantUnaryOperation(expression)
        ?: checkForExponentVariablePowers(expression)
        ?: checkForTooLargeExponentPower(expression)
        ?: checkForNestedExponentiations(expression)
        ?: checkForDivisionByZero(expression)
        ?: checkForDisallowedVariables(expression)
        ?: checkForUnaryPlus(expression)
    } else null
    return optionalError ?: checkForRemainingTokens()
  }

  private fun checkForFirstRedundantGroupError(expression: MathExpression): MathParsingError? {
    return expression.findFirstMultiRedundantGroup()?.let { firstMultiRedundantGroup ->
      val subExpression = parseContext.extractSubexpression(firstMultiRedundantGroup)
      MultipleRedundantParenthesesError(subExpression, firstMultiRedundantGroup)
    }
  }

  private fun checkForWholeExpressionGroupRedundancy(
    expression: MathExpression
  ): MathParsingError? {
    return if (expression.expressionTypeCase == GROUP) {
      SingleRedundantParenthesesError(parseContext.extractSubexpression(expression), expression)
    } else null
  }

  private fun checkForRedundantGroupError(expression: MathExpression): MathParsingError? {
    return expression.findNextRedundantGroup()?.let { nextRedundantGroup ->
      val subExpression = parseContext.extractSubexpression(nextRedundantGroup)
      RedundantParenthesesForIndividualTermsError(subExpression, nextRedundantGroup)
    }
  }

  private fun checkForRedundantUnaryOperation(expression: MathExpression): MathParsingError? {
    return expression.findNextRedundantUnaryOperation()?.let { SubsequentUnaryOperatorsError }
  }

  private fun checkForExponentVariablePowers(expression: MathExpression): MathParsingError? {
    return expression.findNextExponentiationWithVariablePower()?.let {
      ExponentIsVariableExpressionError
    }
  }

  private fun checkForTooLargeExponentPower(expression: MathExpression): MathParsingError? {
    return expression.findNextExponentiationWithTooLargePower()?.let { ExponentTooLargeError }
  }

  private fun checkForNestedExponentiations(expression: MathExpression): MathParsingError? {
    return expression.findNextNestedExponentiation()?.let { NestedExponentsError }
  }

  private fun checkForDivisionByZero(expression: MathExpression): MathParsingError? {
    return expression.findNextDivisionByZero()?.let { TermDividedByZeroError }
  }

  private fun checkForDisallowedVariables(expression: MathExpression): MathParsingError? {
    return expression.findAllDisallowedVariables(parseContext).takeIf { it.isNotEmpty() }?.let {
      DisabledVariablesInUseError(it.toList())
    }
  }

  private fun checkForUnaryPlus(expression: MathExpression): MathParsingError? {
    return expression.findNextUnaryPlus()?.let {
      // The operatorSymbol can't be trivially extracted, so just force it to '+' for correctness.
      NoVariableOrNumberBeforeBinaryOperatorError(ADD, operatorSymbol = "+")
    }
  }

  private fun checkForRemainingTokens(): MathParsingError? {
    // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate the
    // whole grammar).
    return if (parseContext.hasMoreTokens()) {
      when (val nextToken = parseContext.peekToken()) {
        is LeftParenthesisSymbol, is RightParenthesisSymbol -> UnbalancedParenthesesError
        is EqualsSymbol -> {
          if (parseContext is AlgebraicExpressionContext && parseContext.isPartOfEquation) {
            EquationHasTooManyEqualsError
          } else GenericError
        }
        is IncompleteFunctionName -> nextToken.toError()
        is InvalidToken -> nextToken.toError()
        is VariableName -> if (parseContext !is AlgebraicExpressionContext) {
          VariableInNumericExpressionError
        } else GenericError
        is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is ExponentiationSymbol,
        is FunctionName, is MinusSymbol, is MultiplySymbol, is PlusSymbol, is SquareRootSymbol,
        null -> GenericError
      }
    } else null
  }

  private fun PositiveInteger.toReal(): Real = Real.newBuilder().apply {
    integer = parsedValue
  }.build()

  private fun PositiveRealNumber.toReal(): Real = Real.newBuilder().apply {
    irrational = parsedValue
  }.build()

  @Suppress("unused") // The receiver is behaving as a namespace.
  private fun IncompleteFunctionName.toError(): MathParsingError = FunctionNameIncompleteError

  private fun InvalidToken.toError(): MathParsingError =
    UnnecessarySymbolsError(parseContext.extractSubexpression(this))

  private fun <T> IncompleteFunctionName.toFailure(): MathParsingResult<T> = toError().toFailure()

  private fun <T> InvalidToken.toFailure(): MathParsingResult<T> = toError().toFailure()

  /**
   * Specification of context while parsing math expressions and equations.
   *
   * @property rawExpression the whole raw math expression/equation currently being parsed
   */
  private sealed class ParseContext(val rawExpression: String) {
    private val tokens: PeekableIterator<Token> by lazy {
      MathTokenizer.tokenize(rawExpression).toPeekableIterator()
    }
    private var previousToken: Token? = null

    /** Specifies the [ErrorCheckingMode] for the current parsing context. */
    abstract val errorCheckingMode: ErrorCheckingMode

    /** Returns whether there are more [Token]s to parse. */
    fun hasMoreTokens(): Boolean = tokens.hasNext()

    /** Returns the next [Token] available to parse. */
    fun peekToken(): Token? = tokens.peek()

    /**
     * Returns the last token consumed by [consumeTokenOfType], or null if none. Note: this should
     * only be used for error reporting purposes, not for parsing. Using this for parsing would, in
     * certain cases, allow for a non-LL(1) grammar which is against one design goal for this
     * parser.
     */
    fun getPreviousToken(): Token? = previousToken

    /** Returns whether the next available token is type [T] (implies there is a token to parse). */
    inline fun <reified T : Token> hasNextTokenOfType(): Boolean = peekToken() is T

    /**
     * Consumes the next [Token] (which is assumed to be type [T], otherwise the error provided by
     * [missingError] is used) and returns the result.
     */
    inline fun <reified T : Token> consumeTokenOfType(
      missingError: () -> MathParsingError = { GenericError }
    ): MathParsingResult<T> {
      val maybeToken = tokens.expectNextMatches { it is T } as? T
      return maybeToken?.let { token ->
        previousToken = token
        MathParsingResult.Success(token)
      } ?: missingError().toFailure()
    }

    /** Returns the raw string sub-expression corresponding to the specified [Token]. */
    fun extractSubexpression(token: Token): String {
      return rawExpression.substring(token.startIndex, token.endIndex)
    }

    /** Returns the raw string sub-expression corresponding to the specified [MathExpression]. */
    fun extractSubexpression(expression: MathExpression): String {
      return rawExpression.substring(expression.parseStartIndex, expression.parseEndIndex)
    }

    /** The [ParseContext] corresponding to parsing numeric expressions. */
    class NumericExpressionContext(
      rawExpression: String,
      override val errorCheckingMode: ErrorCheckingMode
    ) : ParseContext(rawExpression)

    /**
     * The [ParseContext] corresponding to parsing algebraic expressions & equations.
     *
     * @property isPartOfEquation whether this context is part of parsing an equation
     * @property allowedVariables the list of variables allowed to be used within this context
     */
    class AlgebraicExpressionContext(
      rawExpression: String,
      val isPartOfEquation: Boolean,
      private val allowedVariables: List<String>,
      override val errorCheckingMode: ErrorCheckingMode
    ) : ParseContext(rawExpression) {
      /** Returns whether the specified variable is allowed to be used per this context. */
      fun allowsVariable(variableName: String): Boolean = variableName in allowedVariables
    }
  }

  companion object {
    /** The level of error detection strictness that should be enabled during parsing. */
    enum class ErrorCheckingMode {
      /**
       * Indicates that only only irrecoverable errors should be detected.
       *
       * See the documentation for specific [MathParsingError]s to determine which are
       * irrecoverable.
       */
      REQUIRED_ONLY,

      /**
       * Indicates that both irrecoverable and optional errors should be detected (the strictest
       * setting).
       *
       * Note that 'optional' errors are those that correspond to syntaxes that can still be
       * correctly represented as a math expression or equation (but may indicate a learner
       * misunderstanding).
       *
       * See the documentation for specific [MathParsingError]s to determine which are optional.
       */
      ALL_ERRORS
    }

    /** The result of attempting to parse a raw math expression or equation. */
    sealed class MathParsingResult<T> {
      /** Indicates that the parse was successful with a value of [result]. */
      data class Success<T>(val result: T) : MathParsingResult<T>()

      /** Indicates that the parse failed with the specified [error]. */
      data class Failure<T>(val error: MathParsingError) : MathParsingResult<T>()
    }

    /**
     * Parses a [rawExpression] as a numeric expression
     *
     * Note that the returned expression will have all of its parsing information stripped.
     *
     * @param errorCheckingMode specifies what level of error detection should be enabled during
     *     parsing. The default is [ErrorCheckingMode.ALL_ERRORS].
     * @return the result of attempting to parse the specified numeric expression
     */
    fun parseNumericExpression(
      rawExpression: String,
      errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathExpression> {
      return createNumericParser(rawExpression, errorCheckingMode)
        .parseGenericExpressionGrammar()
        .map { it.stripParseInfo() }
    }

    /**
     * Parses a [rawExpression] as an algebraic expression
     *
     * Note that the returned expression will have all of its parsing information stripped.
     *
     * @param allowedVariables the list of case-sensitive variables allowed in the expression (any
     *     variables encountered that are not within the list will result in an error)
     * @param errorCheckingMode specifies what level of error detection should be enabled during
     *     parsing. The default is [ErrorCheckingMode.ALL_ERRORS].
     * @return the result of attempting to parse the specified algebraic expression
     */
    fun parseAlgebraicExpression(
      rawExpression: String,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathExpression> {
      return createAlgebraicParser(
        rawExpression, isPartOfEquation = false, allowedVariables, errorCheckingMode
      ).parseGenericExpressionGrammar().map { it.stripParseInfo() }
    }

    /**
     * Parses a [rawExpression] as an algebraic equation
     *
     * Note that the returned expression will have all of its parsing information stripped.
     *
     * @param allowedVariables the list of case-sensitive variables allowed in the expression (any
     *     variables encountered that are not within the list will result in an error)
     * @param errorCheckingMode specifies what level of error detection should be enabled during
     *     parsing. The default is [ErrorCheckingMode.ALL_ERRORS].
     * @return the result of attempting to parse the specified algebraic equation
     */
    fun parseAlgebraicEquation(
      rawExpression: String,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathEquation> {
      return createAlgebraicParser(
        rawExpression, isPartOfEquation = true, allowedVariables, errorCheckingMode
      ).parseGenericEquationGrammar().map { it.stripParseInfo() }
    }

    private fun createNumericParser(
      rawExpression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathExpressionParser =
      MathExpressionParser(NumericExpressionContext(rawExpression, errorCheckingMode))

    private fun createAlgebraicParser(
      rawExpression: String,
      isPartOfEquation: Boolean,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode
    ): MathExpressionParser {
      return MathExpressionParser(
        AlgebraicExpressionContext(
          rawExpression, isPartOfEquation, allowedVariables, errorCheckingMode
        )
      )
    }

    private fun ErrorCheckingMode.includesOptionalErrors() = this == ErrorCheckingMode.ALL_ERRORS

    private fun <T> MathParsingError.toFailure(): MathParsingResult<T> =
      MathParsingResult.Failure(this)

    private fun <T> MathParsingResult<T>.isFailure() = this is MathParsingResult.Failure

    /**
     * Maps [this] result to a new value. Note that this lazily uses the provided function (i.e.
     * it's only used if [this] result is passing, otherwise the method will short-circuit a failure
     * state so that [this] result's failure is preserved).
     *
     * @param operation computes a new success result given the current successful result value
     * @return a new [MathParsingResult] with a successful result provided by the operation, or the
     *     preserved failure of [this] result
     */
    private fun <T1, T2> MathParsingResult<T1>.map(
      operation: (T1) -> T2
    ): MathParsingResult<T2> = flatMap { result -> MathParsingResult.Success(operation(result)) }

    /**
     * Maps [this] result to a new value. Note that this lazily uses the provided function (i.e.
     * it's only used if [this] result is passing, otherwise the method will short-circuit a failure
     * state so that [this] result's failure is preserved).
     *
     * @param operation computes a new result (either a success or failure) given the current
     *     successful result value
     * @return a new [MathParsingResult] with either a result provided by the operation, or the
     *     preserved failure of [this] result
     */
    private fun <T1, T2> MathParsingResult<T1>.flatMap(
      operation: (T1) -> MathParsingResult<T2>
    ): MathParsingResult<T2> {
      return when (this) {
        is MathParsingResult.Success -> operation(result)
        is MathParsingResult.Failure -> error.toFailure()
      }
    }

    /**
     * Potentially changes [this] result into a failure based on the provided [operation]. Note that
     * this function lazily uses the operation (i.e. it's only called if [this] result is in a
     * passing state), and the returned result will only be in a failing state if [operation]
     * returns a non-null error.
     *
     * @param operation computes a failure error, or null if no error was determined, given the
     *     current successful result value
     * @return either [this] or a failing result if [operation] was called & returned a non-null
     *     error
     */
    private fun <T> MathParsingResult<T>.maybeFail(
      operation: (T) -> MathParsingError?
    ): MathParsingResult<T> = flatMap { result -> operation(result)?.toFailure() ?: this }

    /**
     * Calls an operation if [this] operation isn't already failing, and returns a failure only if
     * that operation's result is a failure (otherwise returns [this] result). This function can be
     * useful to ensure that subsequent operations are successful even when those operations'
     * results are never directly used.
     *
     * @param operation computes a new result that, when failing, will result in a failing result
     *     returned from this function. This is only called if [this] result is currently
     *     successful.
     * @return either [this] (iff either this result is failing, or the result of [operation] is a
     *     success), or the failure returned by [operation]
     */
    private fun <T1, T2> MathParsingResult<T1>.also(
      operation: () -> MathParsingResult<T2>
    ): MathParsingResult<T1> = flatMap {
      when (val other = operation()) {
        is MathParsingResult.Success -> this
        is MathParsingResult.Failure -> other.error.toFailure()
      }
    }

    /**
     * Combines [this] result with another result, given a specific combination function.
     *
     * @param other the result to combine with [this] result
     * @param combine computes a new value given the result from [this] and [other]. Note that this
     *     is only called if both results are successful, and the corresponding successful values
     *     are provided in-order ([this] result's value is the first parameter, and [other]'s is the
     *     second).
     * @return either [this] result's or [other]'s failure, if either are failing, or a successful
     *     result containing the value computed by [combine]
     */
    private fun <O, I1, I2> MathParsingResult<I1>.combineWith(
      other: MathParsingResult<I2>,
      combine: (I1, I2) -> O,
    ): MathParsingResult<O> {
      return flatMap { result ->
        other.map { otherResult ->
          combine(result, otherResult)
        }
      }
    }

    /**
     * Performs the same operation as the other [combineWith] function, except with three
     * [MathParsingResult]s, instead.
     */
    private fun <O, I1, I2, I3> MathParsingResult<I1>.combineWith(
      other1: MathParsingResult<I2>,
      other2: MathParsingResult<I3>,
      combine: (I1, I2, I3) -> O,
    ): MathParsingResult<O> {
      return flatMap { result ->
        other1.flatMap { otherResult1 ->
          other2.map { otherResult2 ->
            combine(result, otherResult1, otherResult2)
          }
        }
      }
    }

    /**
     * Represents the right-hand side of a binary operation.
     *
     * @property operator the operator corresponding to the operation
     * @property rhsResult the pending result for parsing the right-hand side
     * @property isImplicit whether this is an implicit operation (such as implicit multiplication)
     */
    private data class BinaryOperationRhs(
      val operator: MathBinaryOperation.Operator,
      val rhsResult: MathParsingResult<MathExpression>,
      val isImplicit: Boolean = false
    ) {
      /**
       * Returns the result of combining the left & right-hand sides of the operation into a single
       * [MathExpression] representing the entire binary operation (or a failure if either the
       * left-hand or right-hand sides failed).
       */
      fun computeBinaryOperationExpression(
        lhsResult: MathParsingResult<MathExpression>
      ): MathParsingResult<MathExpression> {
        return lhsResult.combineWith(rhsResult) { lhs, rhs ->
          MathExpression.newBuilder().apply {
            parseStartIndex = lhs.parseStartIndex
            parseEndIndex = rhs.parseEndIndex
            binaryOperation = MathBinaryOperation.newBuilder().apply {
              operator = this@BinaryOperationRhs.operator
              leftOperand = lhs
              rightOperand = rhs
              isImplicit = this@BinaryOperationRhs.isImplicit
            }.build()
          }.build()
        }
      }
    }

    private fun MathExpression.findFirstMultiRedundantGroup(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          binaryOperation.leftOperand.findFirstMultiRedundantGroup()
            ?: binaryOperation.rightOperand.findFirstMultiRedundantGroup()
        }
        UNARY_OPERATION -> unaryOperation.operand.findFirstMultiRedundantGroup()
        FUNCTION_CALL -> functionCall.argument.findFirstMultiRedundantGroup()
        GROUP ->
          group.takeIf { it.expressionTypeCase == GROUP }
            ?: group.findFirstMultiRedundantGroup()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextRedundantGroup(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          binaryOperation.leftOperand.findNextRedundantGroup()
            ?: binaryOperation.rightOperand.findNextRedundantGroup()
        }
        UNARY_OPERATION -> unaryOperation.operand.findNextRedundantGroup()
        FUNCTION_CALL -> functionCall.argument.findNextRedundantGroup()
        GROUP -> group.takeIf {
          it.expressionTypeCase in listOf(CONSTANT, VARIABLE)
        } ?: group.findNextRedundantGroup()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextRedundantUnaryOperation(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          binaryOperation.leftOperand.findNextRedundantUnaryOperation()
            ?: binaryOperation.rightOperand.findNextRedundantUnaryOperation()
        }
        UNARY_OPERATION -> unaryOperation.operand.takeIf {
          it.expressionTypeCase == UNARY_OPERATION
        } ?: unaryOperation.operand.findNextRedundantUnaryOperation()
        FUNCTION_CALL -> functionCall.argument.findNextRedundantUnaryOperation()
        GROUP -> group.findNextRedundantUnaryOperation()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextExponentiationWithVariablePower(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          takeIf {
            binaryOperation.operator == EXPONENTIATE &&
              binaryOperation.rightOperand.isVariableExpression()
          } ?: binaryOperation.leftOperand.findNextExponentiationWithVariablePower()
            ?: binaryOperation.rightOperand.findNextExponentiationWithVariablePower()
        }
        UNARY_OPERATION -> unaryOperation.operand.findNextExponentiationWithVariablePower()
        FUNCTION_CALL -> functionCall.argument.findNextExponentiationWithVariablePower()
        GROUP -> group.findNextExponentiationWithVariablePower()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextExponentiationWithTooLargePower(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          takeIf {
            binaryOperation.operator == EXPONENTIATE &&
              binaryOperation.rightOperand.expressionTypeCase == CONSTANT &&
              binaryOperation.rightOperand.constant.toDouble() > 5.0
          } ?: binaryOperation.leftOperand.findNextExponentiationWithTooLargePower()
            ?: binaryOperation.rightOperand.findNextExponentiationWithTooLargePower()
        }
        UNARY_OPERATION -> unaryOperation.operand.findNextExponentiationWithTooLargePower()
        FUNCTION_CALL -> functionCall.argument.findNextExponentiationWithTooLargePower()
        GROUP -> group.findNextExponentiationWithTooLargePower()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextNestedExponentiation(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          takeIf {
            binaryOperation.operator == EXPONENTIATE &&
              binaryOperation.rightOperand.containsExponentiation()
          } ?: binaryOperation.leftOperand.findNextNestedExponentiation()
            ?: binaryOperation.rightOperand.findNextNestedExponentiation()
        }
        UNARY_OPERATION -> unaryOperation.operand.findNextNestedExponentiation()
        FUNCTION_CALL -> functionCall.argument.findNextNestedExponentiation()
        GROUP -> group.findNextNestedExponentiation()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findNextDivisionByZero(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          takeIf {
            binaryOperation.operator == DIVIDE &&
              binaryOperation.rightOperand.expressionTypeCase == CONSTANT &&
              binaryOperation.rightOperand.constant
                .toDouble().absoluteValue.isApproximatelyEqualTo(0.0)
          } ?: binaryOperation.leftOperand.findNextDivisionByZero()
            ?: binaryOperation.rightOperand.findNextDivisionByZero()
        }
        UNARY_OPERATION -> unaryOperation.operand.findNextDivisionByZero()
        FUNCTION_CALL -> functionCall.argument.findNextDivisionByZero()
        GROUP -> group.findNextDivisionByZero()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.findAllDisallowedVariables(context: ParseContext): Set<String> {
      return if (context is AlgebraicExpressionContext) {
        findAllDisallowedVariablesAux(context)
      } else setOf()
    }

    private fun MathExpression.findAllDisallowedVariablesAux(
      context: AlgebraicExpressionContext
    ): Set<String> {
      return when (expressionTypeCase) {
        VARIABLE -> if (context.allowsVariable(variable)) setOf() else setOf(variable)
        BINARY_OPERATION -> {
          binaryOperation.leftOperand.findAllDisallowedVariablesAux(context) +
            binaryOperation.rightOperand.findAllDisallowedVariablesAux(context)
        }
        UNARY_OPERATION -> unaryOperation.operand.findAllDisallowedVariablesAux(context)
        FUNCTION_CALL -> functionCall.argument.findAllDisallowedVariablesAux(context)
        GROUP -> group.findAllDisallowedVariablesAux(context)
        CONSTANT, EXPRESSIONTYPE_NOT_SET, null -> setOf()
      }
    }

    private fun MathExpression.findNextUnaryPlus(): MathExpression? {
      return when (expressionTypeCase) {
        BINARY_OPERATION ->
          binaryOperation.leftOperand.findNextUnaryPlus()
            ?: binaryOperation.rightOperand.findNextUnaryPlus()
        UNARY_OPERATION -> when (unaryOperation.operator) {
          POSITIVE -> this
          NEGATE, UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null ->
            unaryOperation.operand.findNextUnaryPlus()
        }
        FUNCTION_CALL -> functionCall.argument.findNextUnaryPlus()
        GROUP -> group.findNextUnaryPlus()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathExpression.isVariableExpression(): Boolean {
      return when (expressionTypeCase) {
        VARIABLE -> true
        BINARY_OPERATION -> {
          binaryOperation.leftOperand.isVariableExpression() ||
            binaryOperation.rightOperand.isVariableExpression()
        }
        UNARY_OPERATION -> unaryOperation.operand.isVariableExpression()
        FUNCTION_CALL -> functionCall.argument.isVariableExpression()
        GROUP -> group.isVariableExpression()
        CONSTANT, EXPRESSIONTYPE_NOT_SET, null -> false
      }
    }

    private fun MathExpression.containsExponentiation(): Boolean {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          binaryOperation.operator == EXPONENTIATE ||
            binaryOperation.leftOperand.containsExponentiation() ||
            binaryOperation.rightOperand.containsExponentiation()
        }
        UNARY_OPERATION -> unaryOperation.operand.containsExponentiation()
        FUNCTION_CALL -> functionCall.argument.containsExponentiation()
        GROUP -> group.containsExponentiation()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> false
      }
    }

    private fun MathExpression.stripParseInfo(): MathExpression {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> {
          toBuilder().apply {
            binaryOperation = this@stripParseInfo.binaryOperation.toBuilder().apply {
              leftOperand = this@stripParseInfo.binaryOperation.leftOperand.stripParseInfo()
              rightOperand = this@stripParseInfo.binaryOperation.rightOperand.stripParseInfo()
            }.build()
          }.build()
        }
        UNARY_OPERATION -> {
          toBuilder().apply {
            unaryOperation = this@stripParseInfo.unaryOperation.toBuilder().apply {
              operand = this@stripParseInfo.unaryOperation.operand.stripParseInfo()
            }.build()
          }.build()
        }
        FUNCTION_CALL -> {
          toBuilder().apply {
            functionCall = this@stripParseInfo.functionCall.toBuilder().apply {
              argument = this@stripParseInfo.functionCall.argument.stripParseInfo()
            }.build()
          }.build()
        }
        GROUP -> {
          toBuilder().apply {
            group = this@stripParseInfo.group.stripParseInfo()
          }.build()
        }
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
      }.toBuilder().apply {
        parseStartIndex = 0
        parseEndIndex = 0
      }.build()
    }

    private fun MathEquation.stripParseInfo(): MathEquation = toBuilder().apply {
      leftSide = this@stripParseInfo.leftSide.stripParseInfo()
      rightSide = this@stripParseInfo.rightSide.stripParseInfo()
    }.build()
  }
}
