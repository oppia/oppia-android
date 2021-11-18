package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
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
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathTokenizer2.Companion.Token
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.DivideSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.EqualsSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.ExponentiationSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.FunctionName
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.InvalidToken
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.LeftParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.MinusSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.MultiplySymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PlusSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PositiveInteger
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.PositiveRealNumber
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.RightParenthesisSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.SquareRootSymbol
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.VariableName
import org.oppia.android.util.math.NumericExpressionParser.ParseContext.AlgebraicExpressionContext
import org.oppia.android.util.math.NumericExpressionParser.ParseContext.NumericExpressionContext

class NumericExpressionParser private constructor(
  private val rawExpression: String,
  private val parseContext: ParseContext
) {
  private val tokens: PeekableIterator<Token> by lazy {
    PeekableIterator.fromSequence(MathTokenizer2.tokenize(rawExpression))
  }

  // TODO:
  //  - Add helpers to reduce overall parser length.
  //  - Integrate with new errors & update the routines to not rely on exceptions except in actual exceptional cases. Make sure optional errors can be disabled (for testing purposes).
  //  - Rename this to be a generic parser, update the public API, add documentation, remove the old classes, and split up the big test routines into actual separate tests.

  // TODO: implement specific errors.
  // TODO: verify remaining GenericErrors are correct.

  // TODO: document that 'generic' means either 'numeric' or 'algebraic' (ie that the expression is syntactically the same between both grammars).

  private fun parseGenericEquationGrammar(): MathParsingResult<MathEquation> {
    // generic_equation_grammar = generic_equation ;
    return parseGenericEquation().maybeFail { equation ->
      checkForLearnerErrors(equation.leftSide) ?: checkForLearnerErrors(equation.rightSide)
    }
  }

  private fun parseGenericExpressionGrammar(): MathParsingResult<MathExpression> {
    // generic_expression_grammar = generic_expression ;
    return parseGenericExpression().maybeFail { expression -> checkForLearnerErrors(expression) }
  }

  private fun checkForLearnerErrors(expression: MathExpression): MathParsingError? {
    val firstMultiRedundantGroup = expression.findFirstMultiRedundantGroup()
    val nextRedundantGroup = expression.findNextRedundantGroup()
    // Note that the order of checks here is important since errors have precedence, and some are
    // redundant and, in the wrong order, may cause the wrong error to be returned.
    val includeOptionalErrors = parseContext.errorCheckingMode.includesOptionalErrors()
    return when {
      includeOptionalErrors && firstMultiRedundantGroup != null -> {
        val subExpression = parseContext.extractSubexpression(firstMultiRedundantGroup)
        MathParsingError.MultipleRedundantParenthesesError(subExpression, firstMultiRedundantGroup)
      }
      includeOptionalErrors && expression.expressionTypeCase == GROUP ->
        MathParsingError.SingleRedundantParenthesesError(parseContext.rawExpression, expression)
      includeOptionalErrors && nextRedundantGroup != null -> {
        val subExpression = parseContext.extractSubexpression(nextRedundantGroup)
        MathParsingError.RedundantParenthesesForIndividualTermsError(
          subExpression, nextRedundantGroup
        )
      }
      else -> ensureNoRemainingTokens()
    }
  }

  private fun ensureNoRemainingTokens(): MathParsingError? {
    // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate the
    // whole grammar).
    return if (tokens.hasNext()) {
      when (val nextToken = tokens.peek()) {
        is LeftParenthesisSymbol, is RightParenthesisSymbol ->
          MathParsingError.UnbalancedParenthesesError
        is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
        is ExponentiationSymbol, is FunctionName, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
        is SquareRootSymbol, is VariableName, null -> MathParsingError.GenericError
        is InvalidToken -> nextToken.toError()
      }
    } else null
  }

  private fun parseGenericEquation(): MathParsingResult<MathEquation> {
    // algebraic_equation = generic_expression , equals_operator , generic_expression ;
    val lhsResult = parseGenericExpression().also {
      consumeTokenOfType<EqualsSymbol>()
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
      parseLhs = this::parseGenericMultDivExpression,
      parseRhs = { nextToken ->
        // generic_add_sub_expression_rhs =
        //     generic_add_expression_rhs | generic_sub_expression_rhs ;
        when (nextToken) {
          is PlusSymbol ->
            MathBinaryOperation.Operator.ADD to parseGenericAddExpressionRhs()
          is MinusSymbol ->
            MathBinaryOperation.Operator.SUBTRACT to parseGenericSubExpressionRhs()
          is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
          is ExponentiationSymbol, is FunctionName, is InvalidToken, is LeftParenthesisSymbol,
          is MultiplySymbol, is RightParenthesisSymbol, is SquareRootSymbol, is VariableName,
          null -> null
        }
      }
    )
  }

  private fun parseGenericAddExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_add_expression_rhs = plus_operator , generic_mult_div_expression ;
    return consumeTokenOfType<PlusSymbol>().flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun parseGenericSubExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_sub_expression_rhs = minus_operator , generic_mult_div_expression ;
    return consumeTokenOfType<MinusSymbol>().flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun parseGenericMultDivExpression(): MathParsingResult<MathExpression> {
    // generic_mult_div_expression =
    //     generic_exp_expression , { generic_mult_div_expression_rhs } ;
    return parseGenericBinaryExpression(
      parseLhs = this::parseGenericExpExpression,
      parseRhs = { nextToken ->
        // generic_mult_div_expression_rhs =
        //     generic_mult_expression_rhs
        //     | generic_div_expression_rhs
        //     | generic_implicit_mult_expression_rhs ;
        when (nextToken) {
          is MultiplySymbol ->
            MathBinaryOperation.Operator.MULTIPLY to parseGenericMultExpressionRhs()
          is DivideSymbol -> MathBinaryOperation.Operator.DIVIDE to parseGenericDivExpressionRhs()
          is FunctionName, is LeftParenthesisSymbol, is SquareRootSymbol ->
            MathBinaryOperation.Operator.MULTIPLY to parseGenericImplicitMultExpressionRhs()
          is VariableName -> {
            if (parseContext is AlgebraicExpressionContext) {
              MathBinaryOperation.Operator.MULTIPLY to parseGenericImplicitMultExpressionRhs()
            } else null
          }
          // Not a match to the expression.
          is PositiveInteger, is PositiveRealNumber, is EqualsSymbol, is ExponentiationSymbol,
          is InvalidToken, is MinusSymbol, is PlusSymbol, is RightParenthesisSymbol, null -> null
        }
      }
    )
  }

  private fun parseGenericMultExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_mult_expression_rhs = multiplication_operator , generic_exp_expression ;
    return consumeTokenOfType<MultiplySymbol>().flatMap {
      parseGenericExpExpression()
    }
  }

  private fun parseGenericDivExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_div_expression_rhs = division_operator , generic_exp_expression ;
    return consumeTokenOfType<DivideSymbol>().flatMap {
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
    return if (tokens.peek() is ExponentiationSymbol) {
      parseGenericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  private fun parseGenericExpExpression(): MathParsingResult<MathExpression> {
    // generic_exp_expression = generic_term_with_unary , [ generic_exp_expression_tail ] ;
    val possibleLhs = parseGenericTermWithUnary()
    return if (tokens.peek() is ExponentiationSymbol) {
      parseGenericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  // Use tail recursion so that the last exponentiation is evaluated first, and right-to-left
  // associativity can be kept via backtracking.
  private fun parseGenericExpExpressionTail(
    lhsResult: MathParsingResult<MathExpression>
  ): MathParsingResult<MathExpression> {
    // generic_exp_expression_tail = exponentiation_operator , generic_exp_expression ;
    val rhsResult =
      lhsResult.flatMap {
        consumeTokenOfType<ExponentiationSymbol>()
      }.flatMap {
        parseGenericExpExpression()
      }
    return lhsResult.combineWith(rhsResult) { lhs, rhs ->
      MathExpression.newBuilder().apply {
        parseStartIndex = lhs.parseStartIndex
        parseEndIndex = rhs.parseEndIndex
        binaryOperation = MathBinaryOperation.newBuilder().apply {
          operator = MathBinaryOperation.Operator.EXPONENTIATE
          leftOperand = lhs
          rightOperand = rhs
        }.build()
      }.build()
    }
  }

  private fun parseGenericTermWithUnary(): MathParsingResult<MathExpression> {
    // generic_term_with_unary =
    //    number | generic_term_without_unary_without_number | generic_plus_minus_unary_term ;
    return when (val nextToken = tokens.peek()) {
      is MinusSymbol, is PlusSymbol -> parseGenericPlusMinusUnaryTerm()
      is PositiveInteger, is PositiveRealNumber -> parseNumber().takeUnless {
        tokens.peek() is PositiveInteger || tokens.peek() is PositiveRealNumber
      } ?: MathParsingError.SpacesBetweenNumbersError.toFailure()
      is FunctionName, is LeftParenthesisSymbol, is SquareRootSymbol ->
        parseGenericTermWithoutUnaryWithoutNumber()
      is VariableName -> {
        if (parseContext is AlgebraicExpressionContext) {
          parseGenericTermWithoutUnaryWithoutNumber()
        } else MathParsingError.VariableInNumericExpressionError.toFailure()
      }
      is DivideSymbol, is EqualsSymbol, is ExponentiationSymbol, is MultiplySymbol,
      is RightParenthesisSymbol, null -> MathParsingError.GenericError.toFailure()
      is InvalidToken -> nextToken.toFailure()
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
    return when (val nextToken = tokens.peek()) {
      is FunctionName -> parseGenericFunctionExpression()
      is LeftParenthesisSymbol -> parseGenericGroupExpression()
      is SquareRootSymbol -> parseGenericRootedTerm()
      is VariableName -> MathParsingError.VariableInNumericExpressionError.toFailure()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
      is RightParenthesisSymbol, null -> MathParsingError.GenericError.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseAlgebraicTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // algebraic_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term | variable ;
    return when (val nextToken = tokens.peek()) {
      is FunctionName -> parseGenericFunctionExpression()
      is LeftParenthesisSymbol -> parseGenericGroupExpression()
      is SquareRootSymbol -> parseGenericRootedTerm()
      is VariableName -> parseVariable()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is MinusSymbol, is MultiplySymbol, is PlusSymbol,
      is RightParenthesisSymbol, null -> MathParsingError.GenericError.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseGenericFunctionExpression(): MathParsingResult<MathExpression> {
    // generic_function_expression = function_name , left_paren , generic_expression , right_paren ;
    val funcNameResult =
      consumeTokenOfType<FunctionName>().maybeFail { functionName ->
        if (functionName.parsedName != "sqrt") {
          MathParsingError.GenericError
        } else null
      }.also {
        consumeTokenOfType<LeftParenthesisSymbol>()
      }
    val argResult = funcNameResult.flatMap { parseGenericExpression() }
    val rightParenResult =
      argResult.flatMap {
        consumeTokenOfType<RightParenthesisSymbol> { MathParsingError.UnbalancedParenthesesError }
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
    val leftParenResult = consumeTokenOfType<LeftParenthesisSymbol>()
    val expResult =
      leftParenResult.flatMap {
        if (tokens.hasNext()) {
          parseGenericExpression()
        } else MathParsingError.UnbalancedParenthesesError.toFailure()
      }
    val rightParenResult =
      expResult.flatMap {
        consumeTokenOfType<RightParenthesisSymbol> { MathParsingError.UnbalancedParenthesesError }
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
    return when (val nextToken = tokens.peek()) {
      is MinusSymbol -> parseGenericNegatedTerm()
      is PlusSymbol -> parseGenericPositiveTerm()
      is PositiveInteger, is PositiveRealNumber, is DivideSymbol, is EqualsSymbol,
      is ExponentiationSymbol, is FunctionName, is LeftParenthesisSymbol, is MultiplySymbol,
      is RightParenthesisSymbol, is SquareRootSymbol, is VariableName, null ->
        MathParsingError.GenericError.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseGenericNegatedTerm(): MathParsingResult<MathExpression> {
    // generic_negated_term = minus_operator , generic_mult_div_expression ;
    val minusResult = consumeTokenOfType<MinusSymbol>()
    val expResult = minusResult.flatMap { parseGenericMultDivExpression() }
    return minusResult.combineWith(expResult) { minus, op ->
      MathExpression.newBuilder().apply {
        parseStartIndex = minus.startIndex
        parseEndIndex = op.parseEndIndex
        unaryOperation = MathUnaryOperation.newBuilder().apply {
          operator = MathUnaryOperation.Operator.NEGATE
          operand = op
        }.build()
      }.build()
    }
  }

  private fun parseGenericPositiveTerm(): MathParsingResult<MathExpression> {
    // generic_positive_term = plus_operator , generic_mult_div_expression ;
    val plusResult = consumeTokenOfType<PlusSymbol>()
    val expResult = plusResult.flatMap { parseGenericMultDivExpression() }
    return plusResult.combineWith(expResult) { plus, op ->
      MathExpression.newBuilder().apply {
        parseStartIndex = plus.startIndex
        parseEndIndex = op.parseEndIndex
        unaryOperation = MathUnaryOperation.newBuilder().apply {
          operator = MathUnaryOperation.Operator.POSITIVE
          operand = op
        }.build()
      }.build()
    }
  }

  private fun parseGenericRootedTerm(): MathParsingResult<MathExpression> {
    // generic_rooted_term = square_root_operator , generic_term_with_unary ;
    val sqrtResult =
      consumeTokenOfType<SquareRootSymbol>().maybeFail {
        if (!tokens.hasNext()) MathParsingError.HangingSquareRootError else null
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
    return when (val nextToken = tokens.peek()) {
      is PositiveInteger -> {
        consumeTokenOfType<PositiveInteger>().map { positiveInteger ->
          MathExpression.newBuilder().apply {
            parseStartIndex = positiveInteger.startIndex
            parseEndIndex = positiveInteger.endIndex
            constant = positiveInteger.toReal()
          }.build()
        }
      }
      is PositiveRealNumber -> {
        consumeTokenOfType<PositiveRealNumber>().map { positiveRealNumber ->
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
        MathParsingError.GenericError.toFailure()
      is InvalidToken -> nextToken.toFailure()
    }
  }

  private fun parseVariable(): MathParsingResult<MathExpression> {
    val variableNameResult =
      consumeTokenOfType<VariableName>().maybeFail { variableName ->
        if (!parseContext.allowsVariable(variableName.parsedName)) {
          MathParsingError.GenericError
        } else null
      }.maybeFail { variableName ->
        return@maybeFail if (tokens.hasNext()) {
          when (val nextToken = tokens.peek()) {
            is PositiveInteger ->
              MathParsingError.NumberAfterVariableError(nextToken.toReal(), variableName.parsedName)
            is PositiveRealNumber ->
              MathParsingError.NumberAfterVariableError(nextToken.toReal(), variableName.parsedName)
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
    parseRhs: (Token?) -> Pair<MathBinaryOperation.Operator, MathParsingResult<MathExpression>>?
  ): MathParsingResult<MathExpression> {
    var lastLhsResult = parseLhs()
    while (!lastLhsResult.isFailure()) {
      // Compute the next LHS if there are further RHS expressions.
      val (operator, rhsResult) = parseRhs(tokens.peek()) ?: break // Not a match to the expression.
      lastLhsResult = lastLhsResult.combineWith(rhsResult) { lhs, rhs ->
        MathExpression.newBuilder().apply {
          parseStartIndex = lhs.parseStartIndex
          parseEndIndex = rhs.parseEndIndex
          binaryOperation = MathBinaryOperation.newBuilder().apply {
            this.operator = operator
            leftOperand = lhs
            rightOperand = rhs
          }.build()
        }.build()
      }
    }
    return lastLhsResult
  }

  private fun PositiveInteger.toReal(): Real = Real.newBuilder().apply {
    integer = parsedValue
  }.build()

  private fun PositiveRealNumber.toReal(): Real = Real.newBuilder().apply {
    irrational = parsedValue
  }.build()

  private inline fun <reified T : Token> consumeTokenOfType(
    missingError: () -> MathParsingError = { MathParsingError.GenericError }
  ): MathParsingResult<T> {
    val maybeToken = tokens.expectNextMatches { it is T } as? T
    return maybeToken?.let { token ->
      MathParsingResult.Success(token)
    } ?: missingError().toFailure()
  }

  private fun InvalidToken.toError(): MathParsingError =
    MathParsingError.UnnecessarySymbolsError(parseContext.extractSubexpression(this))

  private fun <T> InvalidToken.toFailure(): MathParsingResult<T> = toError().toFailure()

  private sealed class ParseContext(val rawExpression: String) {
    abstract val errorCheckingMode: ErrorCheckingMode

    abstract fun allowsVariable(variableName: String): Boolean

    fun extractSubexpression(token: Token): String {
      return rawExpression.substring(token.startIndex, token.endIndex)
    }

    fun extractSubexpression(expression: MathExpression): String {
      return rawExpression.substring(expression.parseStartIndex, expression.parseEndIndex)
    }

    class NumericExpressionContext(
      rawExpression: String, override val errorCheckingMode: ErrorCheckingMode
    ) : ParseContext(rawExpression) {
      // Numeric expressions never allow variables.
      override fun allowsVariable(variableName: String): Boolean = false
    }

    class AlgebraicExpressionContext(
      rawExpression: String,
      private val allowedVariables: List<String>,
      override val errorCheckingMode: ErrorCheckingMode
    ) : ParseContext(rawExpression) {
      override fun allowsVariable(variableName: String): Boolean = variableName in allowedVariables
    }
  }

  companion object {
    enum class ErrorCheckingMode {
      REQUIRED_ONLY,
      ALL_ERRORS
    }

    sealed class MathParsingResult<T> {
      data class Success<T>(val result: T) : MathParsingResult<T>()

      data class Failure<T>(val error: MathParsingError) : MathParsingResult<T>()
    }

    fun parseNumericExpression(
      rawExpression: String, errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathExpression> =
      createNumericParser(rawExpression, errorCheckingMode).parseGenericExpressionGrammar()

    fun parseAlgebraicExpression(
      rawExpression: String,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathExpression> {
      return createAlgebraicParser(
        rawExpression, allowedVariables, errorCheckingMode
      ).parseGenericExpressionGrammar()
    }

    fun parseAlgebraicEquation(
      rawExpression: String,
      allowedVariables: List<String>,
      errorCheckingMode: ErrorCheckingMode = ErrorCheckingMode.ALL_ERRORS
    ): MathParsingResult<MathEquation> {
      return createAlgebraicParser(
        rawExpression, allowedVariables, errorCheckingMode
      ).parseGenericEquationGrammar()
    }

    private fun createNumericParser(
      rawExpression: String, errorCheckingMode: ErrorCheckingMode
    ): NumericExpressionParser {
      return NumericExpressionParser(
        rawExpression, NumericExpressionContext(rawExpression, errorCheckingMode)
      )
    }

    private fun createAlgebraicParser(
      rawExpression: String, allowedVariables: List<String>, errorCheckingMode: ErrorCheckingMode
    ): NumericExpressionParser {
      return NumericExpressionParser(
        rawExpression,
        AlgebraicExpressionContext(rawExpression, allowedVariables, errorCheckingMode)
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
  }

  private fun MathExpression.findFirstMultiRedundantGroup(): MathExpression? {
    return when (expressionTypeCase) {
      BINARY_OPERATION -> {
        binaryOperation.leftOperand.findFirstMultiRedundantGroup()
          ?: binaryOperation.rightOperand.findFirstMultiRedundantGroup()
      }
      UNARY_OPERATION -> unaryOperation.operand.findFirstMultiRedundantGroup()
      FUNCTION_CALL -> functionCall.argument.findFirstMultiRedundantGroup()
      GROUP -> group.takeIf { it.expressionTypeCase == GROUP }
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
      GROUP -> {
        group.takeIf { it.expressionTypeCase in listOf(CONSTANT, VARIABLE) }
          ?: group.findNextRedundantGroup()
      }
      CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> null
    }
  }
}
