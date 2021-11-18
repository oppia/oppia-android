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
        val subExpression =
          parseContext.rawExpression.substring(
            firstMultiRedundantGroup.parseStartIndex, firstMultiRedundantGroup.parseEndIndex
          )
        MathParsingError.MultipleRedundantParenthesesError(subExpression, firstMultiRedundantGroup)
      }
      includeOptionalErrors && expression.expressionTypeCase == GROUP ->
        MathParsingError.SingleRedundantParenthesesError(parseContext.rawExpression, expression)
      includeOptionalErrors && nextRedundantGroup != null -> {
        val subExpression =
          parseContext.rawExpression.substring(
            nextRedundantGroup.parseStartIndex, nextRedundantGroup.parseEndIndex
          )
        MathParsingError.RedundantParenthesesForIndividualTermsError(
          subExpression, nextRedundantGroup
        )
      }
      else -> ensureNoRemainingTokens()
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
      GROUP -> group.takeIf { it.expressionTypeCase == GROUP } ?: group.findFirstMultiRedundantGroup()
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

  private fun ensureNoRemainingTokens(): MathParsingError? {
    // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate the
    // whole grammar).
    return if (tokens.hasNext()) {
      when (tokens.peek()) {
        is LeftParenthesisSymbol, is RightParenthesisSymbol ->
          MathParsingError.UnbalancedParenthesesError
        else -> MathParsingError.GenericError
      }
    } else null
  }

  private fun parseGenericEquation(): MathParsingResult<MathEquation> {
    // algebraic_equation = generic_expression , equals_operator , generic_expression ;
    val lhsResult = parseGenericExpression().also {
      consumeTokenOfType<EqualsSymbol> { MathParsingError.GenericError }
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

  // TODO: consider consolidating this with other binary parsing to reduce the overall parser.
  private fun parseGenericAddSubExpression(): MathParsingResult<MathExpression> {
    // generic_add_sub_expression =
    //     generic_mult_div_expression , { generic_add_sub_expression_rhs } ;
    var lastLhsResult = parseGenericMultDivExpression()
    while (!lastLhsResult.isFailure() && hasNextGenericAddSubExpressionRhs()) {
      // generic_add_sub_expression_rhs = generic_add_expression_rhs | generic_sub_expression_rhs ;
      val (operator, rhsResult) = when {
        hasNextGenericAddExpressionRhs() ->
          MathBinaryOperation.Operator.ADD to parseGenericAddExpressionRhs()
        hasNextGenericSubExpressionRhs() ->
          MathBinaryOperation.Operator.SUBTRACT to parseGenericSubExpressionRhs()
        else -> return MathParsingError.GenericError.toFailure()
      }

      lastLhsResult = lastLhsResult.combineWith(rhsResult) { lhs, rhs ->
        // Compute the next LHS if there is further addition/subtraction.
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

  private fun hasNextGenericAddSubExpressionRhs() =
    hasNextGenericAddExpressionRhs() || hasNextGenericSubExpressionRhs()

  private fun hasNextGenericAddExpressionRhs(): Boolean = tokens.peek() is PlusSymbol

  private fun parseGenericAddExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_add_expression_rhs = plus_operator , generic_mult_div_expression ;
    return consumeTokenOfType<PlusSymbol> {
      MathParsingError.GenericError
    }.flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun hasNextGenericSubExpressionRhs(): Boolean = tokens.peek() is MinusSymbol

  private fun parseGenericSubExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_sub_expression_rhs = minus_operator , generic_mult_div_expression ;
    return consumeTokenOfType<MinusSymbol> {
      MathParsingError.GenericError
    }.flatMap {
      parseGenericMultDivExpression()
    }
  }

  private fun parseGenericMultDivExpression(): MathParsingResult<MathExpression> {
    // generic_mult_div_expression =
    //     generic_exp_expression , { generic_mult_div_expression_rhs } ;
    var lastLhsResult = parseGenericExpExpression()
    while (!lastLhsResult.isFailure() && hasNextGenericMultDivExpressionRhs()) {
      // generic_mult_div_expression_rhs =
      //     generic_mult_expression_rhs
      //     | generic_div_expression_rhs
      //     | generic_implicit_mult_expression_rhs ;
      val (operator, rhsResult) = when {
        hasNextGenericMultExpressionRhs() ->
          MathBinaryOperation.Operator.MULTIPLY to parseGenericMultExpressionRhs()
        hasNextGenericDivExpressionRhs() ->
          MathBinaryOperation.Operator.DIVIDE to parseGenericDivExpressionRhs()
        hasNextGenericImplicitMultExpressionRhs() ->
          MathBinaryOperation.Operator.MULTIPLY to parseGenericImplicitMultExpressionRhs()
        else -> return MathParsingError.GenericError.toFailure()
      }

      // Compute the next LHS if there is further multiplication/division.
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

  private fun hasNextGenericMultDivExpressionRhs(): Boolean =
    hasNextGenericMultExpressionRhs()
      || hasNextGenericDivExpressionRhs()
      || hasNextGenericImplicitMultExpressionRhs()

  private fun hasNextGenericMultExpressionRhs(): Boolean = tokens.peek() is MultiplySymbol

  private fun parseGenericMultExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_mult_expression_rhs = multiplication_operator , generic_exp_expression ;
    return consumeTokenOfType<MultiplySymbol> {
      MathParsingError.GenericError
    }.flatMap {
      parseGenericExpExpression()
    }
  }

  private fun hasNextGenericDivExpressionRhs(): Boolean = tokens.peek() is DivideSymbol

  private fun parseGenericDivExpressionRhs(): MathParsingResult<MathExpression> {
    // generic_div_expression_rhs = division_operator , generic_exp_expression ;
    return consumeTokenOfType<DivideSymbol> {
      MathParsingError.GenericError
    }.flatMap {
      parseGenericExpExpression()
    }
  }

  private fun hasNextGenericImplicitMultExpressionRhs(): Boolean {
    return when (parseContext) {
      is NumericExpressionContext -> hasNextNumericImplicitMultExpressionRhs()
      is AlgebraicExpressionContext -> hasNextAlgebraicImplicitMultOrExpExpressionRhs()
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

  private fun hasNextNumericImplicitMultExpressionRhs(): Boolean =
    hasNextGenericTermWithoutUnaryWithoutNumber()

  private fun parseNumericImplicitMultExpressionRhs(): MathParsingResult<MathExpression> {
    // numeric_implicit_mult_expression_rhs = generic_term_without_unary_without_number ;
    return parseGenericTermWithoutUnaryWithoutNumber()
  }

  private fun hasNextAlgebraicImplicitMultOrExpExpressionRhs(): Boolean =
    hasNextGenericTermWithoutUnaryWithoutNumber()

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
    return when {
      hasNextGenericExpExpressionTail() -> parseGenericExpExpressionTail(possibleLhs)
      else -> possibleLhs
    }
  }

  private fun hasNextGenericExpExpressionTail(): Boolean = tokens.peek() is ExponentiationSymbol

  // Use tail recursion so that the last exponentiation is evaluated first, and right-to-left
  // associativity can be kept via backtracking.
  private fun parseGenericExpExpressionTail(
    lhsResult: MathParsingResult<MathExpression>
  ): MathParsingResult<MathExpression> {
    // generic_exp_expression_tail = exponentiation_operator , generic_exp_expression ;
    val rhsResult =
      lhsResult.flatMap {
        consumeTokenOfType<ExponentiationSymbol> { MathParsingError.GenericError }
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
    return when {
      hasNextGenericPlusMinusUnaryTerm() -> parseGenericPlusMinusUnaryTerm()
      hasNextNumber() -> parseNumber().takeUnless {
        hasNextNumber()
      } ?: MathParsingError.SpacesBetweenNumbersError.toFailure()
      hasNextGenericTermWithoutUnaryWithoutNumber() -> parseGenericTermWithoutUnaryWithoutNumber()
      else -> MathParsingError.GenericError.toFailure()
    }
  }

  private fun hasNextGenericTermWithoutUnaryWithoutNumber(): Boolean {
    return when (parseContext) {
      is NumericExpressionContext -> hasNextNumericTermWithoutUnaryWithoutNumber()
      is AlgebraicExpressionContext -> hasNextAlgebraicTermWithoutUnaryWithoutNumber()
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

  private fun hasNextNumericTermWithoutUnaryWithoutNumber(): Boolean =
    hasNextGenericFunctionExpression()
      || hasNextGenericGroupExpression()
      || hasNextGenericRootedTerm()

  private fun parseNumericTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // numeric_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term ;
    return when {
      hasNextGenericFunctionExpression() -> parseGenericFunctionExpression()
      hasNextGenericGroupExpression() -> parseGenericGroupExpression()
      hasNextGenericRootedTerm() -> parseGenericRootedTerm()
      else -> MathParsingError.GenericError.toFailure()
    }
  }

  private fun hasNextAlgebraicTermWithoutUnaryWithoutNumber(): Boolean =
    hasNextGenericFunctionExpression()
      || hasNextGenericGroupExpression()
      || hasNextGenericRootedTerm()
      || hasNextVariable()

  private fun parseAlgebraicTermWithoutUnaryWithoutNumber(): MathParsingResult<MathExpression> {
    // algebraic_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term | variable ;
    return when {
      hasNextGenericFunctionExpression() -> parseGenericFunctionExpression()
      hasNextGenericGroupExpression() -> parseGenericGroupExpression()
      hasNextGenericRootedTerm() -> parseGenericRootedTerm()
      hasNextVariable() -> parseVariable()
      else -> MathParsingError.GenericError.toFailure()
    }
  }

  private fun hasNextGenericFunctionExpression(): Boolean = tokens.peek() is FunctionName

  private fun parseGenericFunctionExpression(): MathParsingResult<MathExpression> {
    // generic_function_expression = function_name , left_paren , generic_expression , right_paren ;
    val funcNameResult =
      consumeTokenOfType<FunctionName> {
        MathParsingError.GenericError
      }.maybeFail { functionName ->
        if (functionName.parsedName != "sqrt") {
          MathParsingError.GenericError
        } else null
      }.also {
        consumeTokenOfType<LeftParenthesisSymbol> { MathParsingError.GenericError }
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

  private fun hasNextGenericGroupExpression(): Boolean = tokens.peek() is LeftParenthesisSymbol

  private fun parseGenericGroupExpression(): MathParsingResult<MathExpression> {
    // generic_group_expression = left_paren , generic_expression , right_paren ;
    val leftParenResult =
      consumeTokenOfType<LeftParenthesisSymbol> {
        MathParsingError.GenericError
      }
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

  private fun hasNextGenericPlusMinusUnaryTerm(): Boolean =
    hasNextGenericNegatedTerm() || hasNextGenericPositiveTerm()

  private fun parseGenericPlusMinusUnaryTerm(): MathParsingResult<MathExpression> {
    // generic_plus_minus_unary_term = generic_negated_term | generic_positive_term ;
    return when {
      hasNextGenericNegatedTerm() -> parseGenericNegatedTerm()
      hasNextGenericPositiveTerm() -> parseGenericPositiveTerm()
      else -> MathParsingError.GenericError.toFailure()
    }
  }

  private fun hasNextGenericNegatedTerm(): Boolean = tokens.peek() is MinusSymbol

  private fun parseGenericNegatedTerm(): MathParsingResult<MathExpression> {
    // generic_negated_term = minus_operator , generic_mult_div_expression ;
    val minusResult = consumeTokenOfType<MinusSymbol> { MathParsingError.GenericError }
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

  private fun hasNextGenericPositiveTerm(): Boolean = tokens.peek() is PlusSymbol

  private fun parseGenericPositiveTerm(): MathParsingResult<MathExpression> {
    // generic_positive_term = plus_operator , generic_mult_div_expression ;
    val plusResult = consumeTokenOfType<PlusSymbol> { MathParsingError.GenericError }
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

  private fun hasNextGenericRootedTerm(): Boolean = tokens.peek() is SquareRootSymbol

  private fun parseGenericRootedTerm(): MathParsingResult<MathExpression> {
    // generic_rooted_term = square_root_operator , generic_term_with_unary ;
    val sqrtResult = consumeTokenOfType<SquareRootSymbol> { MathParsingError.GenericError }
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

  private fun hasNextNumber(): Boolean = hasNextPositiveInteger() || hasNextPositiveRealNumber()

  private fun parseNumber(): MathParsingResult<MathExpression> {
    // number = positive_real_number | positive_integer ;
    return when {
      hasNextPositiveInteger() -> {
        consumeTokenOfType<PositiveInteger> {
          MathParsingError.GenericError
        }.map { int ->
          MathExpression.newBuilder().apply {
            parseStartIndex = int.startIndex
            parseEndIndex = int.endIndex
            constant = Real.newBuilder().apply {
              integer = int.parsedValue
            }.build()
          }.build()
        }
      }
      hasNextPositiveRealNumber() -> {
        consumeTokenOfType<PositiveRealNumber> {
          MathParsingError.GenericError
        }.map { real ->
          MathExpression.newBuilder().apply {
            parseStartIndex = real.startIndex
            parseEndIndex = real.endIndex
            constant = Real.newBuilder().apply {
              irrational = real.parsedValue
            }.build()
          }.build()
        }
      }
      // TODO: add error that one of the above was expected. Other error handling should maybe
      //  happen in the same way.
      else -> MathParsingError.GenericError.toFailure()
    }
  }

  private fun hasNextPositiveInteger(): Boolean = tokens.peek() is PositiveInteger

  private fun hasNextPositiveRealNumber(): Boolean = tokens.peek() is PositiveRealNumber

  private fun hasNextVariable(): Boolean = tokens.peek() is VariableName

  private fun parseVariable(): MathParsingResult<MathExpression> {
    val variableNameResult =
      consumeTokenOfType<VariableName> {
        MathParsingError.GenericError
      }.maybeFail { variableName ->
        if (!parseContext.allowsVariable(variableName.parsedName)) {
          MathParsingError.GenericError
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

  private inline fun <reified T : Token> consumeTokenOfType(
    missingError: () -> MathParsingError
  ): MathParsingResult<T> {
    val maybeToken = tokens.expectNextMatches { it is T } as? T
    return maybeToken?.let { token ->
      MathParsingResult.Success(token)
    } ?: missingError().toFailure()
  }

  private sealed class ParseContext(val rawExpression: String) {
    abstract val errorCheckingMode: ErrorCheckingMode

    abstract fun allowsVariable(variableName: String): Boolean

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
}
