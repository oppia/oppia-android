package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
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
  //  - Add support for equations + tests.

  // TODO: document that 'generic' means either 'numeric' or 'algebraic' (ie that the expression is syntactically the same between both grammars).

  private fun parseGenericEquationGrammar(): MathEquation {
    // generic_equation_grammar = generic_equation ;
    return parseGenericEquation().also { ensureNoRemainingTokens() }
  }

  private fun parseGenericExpressionGrammar(): MathExpression {
    // generic_expression_grammar = generic_expression ;
    return parseGenericExpression().also { ensureNoRemainingTokens() }
  }

  private fun ensureNoRemainingTokens() {
    // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate the
    // whole grammar).
    if (tokens.hasNext()) throw ParseException()
  }

  private fun parseGenericEquation(): MathEquation {
    // algebraic_equation = generic_expression , equals_operator , generic_expression ;
    val lhs = parseGenericExpression()
    consumeTokenOfType<EqualsSymbol>()
    val rhs = parseGenericExpression()
    return MathEquation.newBuilder().apply {
      leftSide = lhs
      rightSide = rhs
    }.build()
  }

  private fun parseGenericExpression(): MathExpression {
    // generic_expression = generic_add_sub_expression ;
    return parseGenericAddSubExpression()
  }

  // TODO: consider consolidating this with other binary parsing to reduce the overall parser.
  private fun parseGenericAddSubExpression(): MathExpression {
    // generic_add_sub_expression =
    //     generic_mult_div_expression , { generic_add_sub_expression_rhs } ;
    var lastLhs = parseGenericMultDivExpression()
    while (hasNextGenericAddSubExpressionRhs()) {
      // generic_add_sub_expression_rhs = generic_add_expression_rhs | generic_sub_expression_rhs ;
      val (operator, rhs) = when {
        hasNextGenericAddExpressionRhs() ->
          MathBinaryOperation.Operator.ADD to parseGenericAddExpressionRhs()
        hasNextGenericSubExpressionRhs() ->
          MathBinaryOperation.Operator.SUBTRACT to parseGenericSubExpressionRhs()
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

  private fun hasNextGenericAddSubExpressionRhs() = hasNextGenericAddExpressionRhs()
    || hasNextGenericSubExpressionRhs()

  private fun hasNextGenericAddExpressionRhs(): Boolean = tokens.peek() is PlusSymbol

  private fun parseGenericAddExpressionRhs(): MathExpression {
    // generic_add_expression_rhs = plus_operator , generic_mult_div_expression ;
    consumeTokenOfType<PlusSymbol>()
    return parseGenericMultDivExpression()
  }

  private fun hasNextGenericSubExpressionRhs(): Boolean = tokens.peek() is MinusSymbol

  private fun parseGenericSubExpressionRhs(): MathExpression {
    // generic_sub_expression_rhs = minus_operator , generic_mult_div_expression ;
    consumeTokenOfType<MinusSymbol>()
    return parseGenericMultDivExpression()
  }

  private fun parseGenericMultDivExpression(): MathExpression {
    // generic_mult_div_expression =
    //     generic_exp_expression , { generic_mult_div_expression_rhs } ;
    var lastLhs = parseGenericExpExpression()
    while (hasNextGenericMultDivExpressionRhs()) {
      // generic_mult_div_expression_rhs =
      //     generic_mult_expression_rhs
      //     | generic_div_expression_rhs
      //     | generic_implicit_mult_expression_rhs ;
      val (operator, rhs) = when {
        hasNextGenericMultExpressionRhs() ->
          MathBinaryOperation.Operator.MULTIPLY to parseGenericMultExpressionRhs()
        hasNextGenericDivExpressionRhs() ->
          MathBinaryOperation.Operator.DIVIDE to parseGenericDivExpressionRhs()
        hasNextGenericImplicitMultExpressionRhs() ->
          MathBinaryOperation.Operator.MULTIPLY to parseGenericImplicitMultExpressionRhs()
        else -> throw ParseException()
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

  private fun hasNextGenericMultDivExpressionRhs(): Boolean =
    hasNextGenericMultExpressionRhs()
      || hasNextGenericDivExpressionRhs()
      || hasNextGenericImplicitMultExpressionRhs()

  private fun hasNextGenericMultExpressionRhs(): Boolean = tokens.peek() is MultiplySymbol

  private fun parseGenericMultExpressionRhs(): MathExpression {
    // generic_mult_expression_rhs = multiplication_operator , generic_exp_expression ;
    consumeTokenOfType<MultiplySymbol>()
    return parseGenericExpExpression()
  }

  private fun hasNextGenericDivExpressionRhs(): Boolean = tokens.peek() is DivideSymbol

  private fun parseGenericDivExpressionRhs(): MathExpression {
    // generic_div_expression_rhs = division_operator , generic_exp_expression ;
    consumeTokenOfType<DivideSymbol>()
    return parseGenericExpExpression()
  }

  private fun hasNextGenericImplicitMultExpressionRhs(): Boolean {
    return when (parseContext) {
      NumericExpressionContext -> hasNextNumericImplicitMultExpressionRhs()
      is AlgebraicExpressionContext -> hasNextAlgebraicImplicitMultOrExpExpressionRhs()
    }
  }

  private fun parseGenericImplicitMultExpressionRhs(): MathExpression {
    // generic_implicit_mult_expression_rhs is either numeric_implicit_mult_expression_rhs or
    // algebraic_implicit_mult_or_exp_expression_rhs depending on the current parser context.
    return when (parseContext) {
      NumericExpressionContext -> parseNumericImplicitMultExpressionRhs()
      is AlgebraicExpressionContext -> parseAlgebraicImplicitMultOrExpExpressionRhs()
    }
  }

  private fun hasNextNumericImplicitMultExpressionRhs(): Boolean =
    hasNextGenericTermWithoutUnaryWithoutNumber()

  private fun parseNumericImplicitMultExpressionRhs(): MathExpression {
    // numeric_implicit_mult_expression_rhs = generic_term_without_unary_without_number ;
    return parseGenericTermWithoutUnaryWithoutNumber()
  }

  private fun hasNextAlgebraicImplicitMultOrExpExpressionRhs(): Boolean =
    hasNextGenericTermWithoutUnaryWithoutNumber()

  private fun parseAlgebraicImplicitMultOrExpExpressionRhs(): MathExpression {
    // algebraic_implicit_mult_or_exp_expression_rhs =
    //     generic_term_without_unary_without_number , [ generic_exp_expression_tail ] ;
    val possibleLhs = parseGenericTermWithoutUnaryWithoutNumber()
    return if (tokens.peek() is ExponentiationSymbol) {
      parseGenericExpExpressionTail(possibleLhs)
    } else possibleLhs
  }

  private fun parseGenericExpExpression(): MathExpression {
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
  private fun parseGenericExpExpressionTail(lhs: MathExpression): MathExpression {
    // generic_exp_expression_tail = exponentiation_operator , generic_exp_expression ;
    consumeTokenOfType<ExponentiationSymbol>()
    return MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.newBuilder().apply {
        operator = MathBinaryOperation.Operator.EXPONENTIATE
        leftOperand = lhs
        rightOperand = parseGenericExpExpression()
      }.build()
    }.build()
  }

  private fun parseGenericTermWithUnary(): MathExpression {
    // generic_term_with_unary =
    //    number | generic_term_without_unary_without_number | generic_plus_minus_unary_term ;
    return when {
      hasNextGenericPlusMinusUnaryTerm() -> parseGenericPlusMinusUnaryTerm()
      hasNextNumber() -> parseNumber()
      hasNextGenericTermWithoutUnaryWithoutNumber() -> parseGenericTermWithoutUnaryWithoutNumber()
      else -> throw ParseException()
    }
  }

  private fun hasNextGenericTermWithoutUnaryWithoutNumber(): Boolean {
    return when (parseContext) {
      NumericExpressionContext -> hasNextNumericTermWithoutUnaryWithoutNumber()
      is AlgebraicExpressionContext -> hasNextAlgebraicTermWithoutUnaryWithoutNumber()
    }
  }

  private fun parseGenericTermWithoutUnaryWithoutNumber(): MathExpression {
    // generic_term_without_unary_without_number is either numeric_term_without_unary_without_number
    // or algebraic_term_without_unary_without_number based the current parser context.
    return when (parseContext) {
      NumericExpressionContext -> parseNumericTermWithoutUnaryWithoutNumber()
      is AlgebraicExpressionContext -> parseAlgebraicTermWithoutUnaryWithoutNumber()
    }
  }

  private fun hasNextNumericTermWithoutUnaryWithoutNumber(): Boolean =
    hasNextGenericFunctionExpression()
      || hasNextGenericGroupExpression()
      || hasNextGenericRootedTerm()

  private fun parseNumericTermWithoutUnaryWithoutNumber(): MathExpression {
    // numeric_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term ;
    return when {
      hasNextGenericFunctionExpression() -> parseGenericFunctionExpression()
      hasNextGenericGroupExpression() -> parseGenericGroupExpression()
      hasNextGenericRootedTerm() -> parseGenericRootedTerm()
      else -> throw ParseException()
    }
  }

  private fun hasNextAlgebraicTermWithoutUnaryWithoutNumber(): Boolean =
    hasNextGenericFunctionExpression()
      || hasNextGenericGroupExpression()
      || hasNextGenericRootedTerm()
      || hasNextVariable()

  private fun parseAlgebraicTermWithoutUnaryWithoutNumber(): MathExpression {
    // algebraic_term_without_unary_without_number =
    //     generic_function_expression | generic_group_expression | generic_rooted_term | variable ;
    return when {
      hasNextGenericFunctionExpression() -> parseGenericFunctionExpression()
      hasNextGenericGroupExpression() -> parseGenericGroupExpression()
      hasNextGenericRootedTerm() -> parseGenericRootedTerm()
      hasNextVariable() -> parseVariable()
      else -> throw ParseException()
    }
  }

  private fun hasNextGenericFunctionExpression(): Boolean = tokens.peek() is FunctionName

  private fun parseGenericFunctionExpression(): MathExpression {
    // generic_function_expression = function_name , left_paren , generic_expression , right_paren ;
    return MathExpression.newBuilder().apply {
      val functionName = consumeTokenOfType<FunctionName>()
      if (functionName.parsedName != "sqrt") throw ParseException()
      consumeTokenOfType<LeftParenthesisSymbol>()
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseGenericExpression()
      }.build()
      consumeTokenOfType<RightParenthesisSymbol>()
    }.build()
  }

  private fun hasNextGenericGroupExpression(): Boolean = tokens.peek() is LeftParenthesisSymbol

  private fun parseGenericGroupExpression(): MathExpression {
    // generic_group_expression = left_paren , generic_expression , right_paren ;
    consumeTokenOfType<LeftParenthesisSymbol>()
    return parseGenericExpression().also {
      consumeTokenOfType<RightParenthesisSymbol>()
    }
  }

  private fun hasNextGenericPlusMinusUnaryTerm(): Boolean =
    hasNextGenericNegatedTerm() || hasNextGenericPositiveTerm()

  private fun parseGenericPlusMinusUnaryTerm(): MathExpression {
    // generic_plus_minus_unary_term = generic_negated_term | generic_positive_term ;
    return when {
      hasNextGenericNegatedTerm() -> parseGenericNegatedTerm()
      hasNextGenericPositiveTerm() -> parseGenericPositiveTerm()
      else -> throw ParseException()
    }
  }

  private fun hasNextGenericNegatedTerm(): Boolean = tokens.peek() is MinusSymbol

  private fun parseGenericNegatedTerm(): MathExpression {
    // generic_negated_term = minus_operator , generic_mult_div_expression ;
    consumeTokenOfType<MinusSymbol>()
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.NEGATE
        operand = parseGenericMultDivExpression()
      }.build()
    }.build()
  }

  private fun hasNextGenericPositiveTerm(): Boolean = tokens.peek() is PlusSymbol

  private fun parseGenericPositiveTerm(): MathExpression {
    // generic_positive_term = plus_operator , generic_mult_div_expression ;
    consumeTokenOfType<PlusSymbol>()
    return MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.newBuilder().apply {
        operator = MathUnaryOperation.Operator.POSITIVE
        operand = parseGenericMultDivExpression()
      }.build()
    }.build()
  }

  private fun hasNextGenericRootedTerm(): Boolean = tokens.peek() is SquareRootSymbol

  private fun parseGenericRootedTerm(): MathExpression {
    // generic_rooted_term = square_root_operator , generic_term_with_unary ;
    consumeTokenOfType<SquareRootSymbol>()
    return MathExpression.newBuilder().apply {
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseGenericTermWithUnary()
      }.build()
    }.build()
  }

  private fun hasNextNumber(): Boolean = hasNextPositiveInteger() || hasNextPositiveRealNumber()

  private fun parseNumber(): MathExpression {
    // number = positive_real_number | positive_integer ;
    return MathExpression.newBuilder().apply {
      constant = when {
        hasNextPositiveInteger() -> Real.newBuilder().apply {
          integer = consumeTokenOfType<PositiveInteger>().parsedValue
        }.build()
        hasNextPositiveRealNumber() -> Real.newBuilder().apply {
          irrational = consumeTokenOfType<PositiveRealNumber>().parsedValue
        }.build()
        // TODO: add error that one of the above was expected. Other error handling should maybe
        //  happen in the same way.
        else -> throw ParseException() // Something went wrong.
      }
    }.build()
  }

  private fun hasNextPositiveInteger(): Boolean = tokens.peek() is PositiveInteger

  private fun hasNextPositiveRealNumber(): Boolean = tokens.peek() is PositiveRealNumber

  private fun hasNextVariable(): Boolean = tokens.peek() is VariableName

  private fun parseVariable(): MathExpression {
    val variableName = consumeTokenOfType<VariableName>()
    if (!parseContext.allowsVariable(variableName.parsedName)) {
      throw ParseException()
    }
    return MathExpression.newBuilder().apply {
      variable = variableName.parsedName
    }.build()
  }

  private inline fun <reified T : Token> consumeTokenOfType(): T {
    return (tokens.expectNextMatches { it is T } as? T) ?: throw ParseException()
  }

  // TODO: do error handling better than this (& in a way that works better with the types of errors
  //  that we want to show users).
  class ParseException : Exception()

  private sealed class ParseContext {
    abstract fun allowsVariable(variableName: String): Boolean

    object NumericExpressionContext : ParseContext() {
      // Numeric expressions never allow variables.
      override fun allowsVariable(variableName: String): Boolean = false
    }

    data class AlgebraicExpressionContext(
      private val allowedVariables: List<String>
    ) : ParseContext() {
      override fun allowsVariable(variableName: String): Boolean = variableName in allowedVariables
    }
  }

  companion object {
    fun parseNumericExpression(rawExpression: String): MathExpression =
      createNumericParser(rawExpression).parseGenericExpressionGrammar()

    fun parseAlgebraicExpression(
      rawExpression: String, allowedVariables: List<String>
    ): MathExpression =
      createAlgebraicParser(rawExpression, allowedVariables).parseGenericExpressionGrammar()

    fun parseAlgebraicEquation(
      rawExpression: String,
      allowedVariables: List<String>
    ): MathEquation =
      createAlgebraicParser(rawExpression, allowedVariables).parseGenericEquationGrammar()

    private fun createNumericParser(rawExpression: String) =
      NumericExpressionParser(rawExpression, NumericExpressionContext)

    private fun createAlgebraicParser(rawExpression: String, allowedVariables: List<String>) =
      NumericExpressionParser(rawExpression, AlgebraicExpressionContext(allowedVariables))
  }
}
