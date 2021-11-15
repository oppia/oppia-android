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
import org.oppia.android.util.math.MathTokenizer2.Companion.Token.VariableName
import org.oppia.android.util.math.NumericExpressionParser.Companion.ProductionRuleDefinition.Companion.getFirstAsMatchedRule
import org.oppia.android.util.math.NumericExpressionParser.Companion.ProductionRuleDefinition.Companion.getFirstAsToken
import org.oppia.android.util.math.NumericExpressionParser.Companion.ProductionRuleDefinition.Companion.getMatchedRule
import org.oppia.android.util.math.NumericExpressionParser.Companion.TestClass.ProductionRules.*

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

  private fun parseGeneric(): MathExpression {
    return parseGenericExpression().also {
      // Make sure all tokens were consumed (otherwise there are trailing tokens which invalidate
      // the whole expression).
      if (tokens.hasNext()) throw ParseException()
    }
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
    tokens.consumeTokenOfType<PlusSymbol>()
    return parseGenericMultDivExpression()
  }

  private fun hasNextGenericSubExpressionRhs(): Boolean = tokens.peek() is MinusSymbol

  private fun parseGenericSubExpressionRhs(): MathExpression {
    // generic_sub_expression_rhs = minus_operator , generic_mult_div_expression ;
    tokens.consumeTokenOfType<MinusSymbol>()
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
    tokens.consumeTokenOfType<MultiplySymbol>()
    return parseGenericExpExpression()
  }

  private fun hasNextGenericDivExpressionRhs(): Boolean = tokens.peek() is DivideSymbol

  private fun parseGenericDivExpressionRhs(): MathExpression {
    // generic_div_expression_rhs = division_operator , generic_exp_expression ;
    tokens.consumeTokenOfType<DivideSymbol>()
    return parseGenericExpExpression()
  }

  private fun hasNextGenericImplicitMultExpressionRhs(): Boolean {
    return when (parseContext) {
      ParseContext.NumericExpressionContext -> hasNextNumericImplicitMultExpressionRhs()
      is ParseContext.AlgebraicExpressionContext -> hasNextAlgebraicImplicitMultOrExpExpressionRhs()
    }
  }

  private fun parseGenericImplicitMultExpressionRhs(): MathExpression {
    // generic_implicit_mult_expression_rhs is either numeric_implicit_mult_expression_rhs or
    // algebraic_implicit_mult_or_exp_expression_rhs depending on the current parser context.
    return when (parseContext) {
      ParseContext.NumericExpressionContext -> parseNumericImplicitMultExpressionRhs()
      is ParseContext.AlgebraicExpressionContext -> parseAlgebraicImplicitMultOrExpExpressionRhs()
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
    tokens.consumeTokenOfType<ExponentiationSymbol>()
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
      ParseContext.NumericExpressionContext -> hasNextNumericTermWithoutUnaryWithoutNumber()
      is ParseContext.AlgebraicExpressionContext -> hasNextAlgebraicTermWithoutUnaryWithoutNumber()
    }
  }

  private fun parseGenericTermWithoutUnaryWithoutNumber(): MathExpression {
    // generic_term_without_unary_without_number is either numeric_term_without_unary_without_number
    // or algebraic_term_without_unary_without_number based the current parser context.
    return when (parseContext) {
      ParseContext.NumericExpressionContext -> parseNumericTermWithoutUnaryWithoutNumber()
      is ParseContext.AlgebraicExpressionContext -> parseAlgebraicTermWithoutUnaryWithoutNumber()
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
      val functionName = tokens.consumeTokenOfType<FunctionName>()
      if (functionName.parsedName != "sqrt") throw ParseException()
      tokens.consumeTokenOfType<LeftParenthesisSymbol>()
      functionCall = MathFunctionCall.newBuilder().apply {
        functionType = MathFunctionCall.FunctionType.SQUARE_ROOT
        argument = parseGenericExpression()
      }.build()
      tokens.consumeTokenOfType<RightParenthesisSymbol>()
    }.build()
  }

  private fun hasNextGenericGroupExpression(): Boolean = tokens.peek() is LeftParenthesisSymbol

  private fun parseGenericGroupExpression(): MathExpression {
    // generic_group_expression = left_paren , generic_expression , right_paren ;
    tokens.consumeTokenOfType<LeftParenthesisSymbol>()
    return parseGenericExpression().also {
      tokens.consumeTokenOfType<RightParenthesisSymbol>()
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
    tokens.consumeTokenOfType<MinusSymbol>()
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
    tokens.consumeTokenOfType<PlusSymbol>()
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
    tokens.consumeTokenOfType<SquareRootSymbol>()
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
          integer = tokens.consumeTokenOfType<PositiveInteger>().parsedValue
        }.build()
        hasNextPositiveRealNumber() -> Real.newBuilder().apply {
          irrational = tokens.consumeTokenOfType<PositiveRealNumber>().parsedValue
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
    val variableName = tokens.consumeTokenOfType<VariableName>()
    if (!parseContext.allowsVariable(variableName.parsedName)) {
      throw ParseException()
    }
    return MathExpression.newBuilder().apply {
      variable = variableName.parsedName
    }.build()
  }

  // TODO: do error handling better than this (& in a way that works better with the types of errors
  //  that we want to show users).
  class ParseException : Exception()

  sealed class ParseContext {
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
    fun parseNumericExpression(rawExpression: String): MathExpression {
//      return NumericExpressionParser(rawExpression, ParseContext.NumericExpressionContext).parseGeneric()
      return TestClass.createGrammar().parse(rawExpression, ParseContext.NumericExpressionContext)
    }

    fun parseAlgebraicExpression(
      rawExpression: String, allowedVariables: List<String>
    ): MathExpression {
      val parser =
        NumericExpressionParser(
          rawExpression, ParseContext.AlgebraicExpressionContext(allowedVariables)
        )
      return parser.parseGeneric()
    }

    interface Grammar {
      fun parse(rawExpression: String, parseContext: ParseContext): MathExpression
    }

    @DslMarker
    private annotation class ProductionRuleMarker

    @ProductionRuleMarker
    private class GrammarDefinition<T: Enum<T>> private constructor() {
      private val definitions = mutableMapOf<T, ProductionRuleDefinition<T>>()

      // TODO: factor this into the static func.
      fun defineConcatenationRule(
        name: T, init: ProductionRuleDefinition.Concatenation<T>.() -> Unit
      ) {
        verifyRuleNameIsUnused(name)
        definitions[name] = ProductionRuleDefinition.Concatenation(name).also(init)
      }

      fun defineAlternationRule(name: T, init: ProductionRuleDefinition.Alternation<T>.() -> Unit) {
        verifyRuleNameIsUnused(name)
        definitions[name] = ProductionRuleDefinition.Alternation(name).also(init)
      }

      fun defineSingletonRule(name: T, singletonProducer: () -> T) {
        verifyRuleNameIsUnused(name)
        definitions[name] = ProductionRuleDefinition.Singleton(name, singletonProducer())
      }

      private fun initializeRoot(
        name: T
      ): ProductionRuleDefinition.Companion.ProductionRule.NonTerminal<T> {
        val rules = definitions.mapValues { (_, definition) -> definition.toProductionRule() }
        val rootRule = rules[name] ?: error("No rule defined for name: $name")
        rootRule.initialize(rules)
        return rootRule
      }

      private fun verifyRuleNameIsUnused(name: T) {
        check(name !in definitions) { "Production rule with $name already defined" }
      }

      companion object {
        inline fun <T: Enum<T>> defineGrammar(
          rootRuleName: T,
          init: GrammarDefinition<T>.() -> Unit
        ): Grammar {
          val rootRule = GrammarDefinition<T>().also(init).initializeRoot(rootRuleName)
          return object : Grammar {
            override fun parse(rawExpression: String, parseContext: ParseContext): MathExpression {
              val tokens = PeekableIterator.fromSequence(MathTokenizer2.tokenize(rawExpression))
              val results = rootRule.parse(tokens, parseContext)
              return rootRule.computeMathExpression(results)
            }
          }
        }
      }
    }

    class TestClass {
      companion object {
        fun createGrammar(): Grammar {
          return GrammarDefinition.defineGrammar(rootRuleName = numeric_expression_grammar) {
            defineSingletonRule(numeric_expression_grammar) { numeric_expression }

            defineSingletonRule(numeric_expression) { numeric_add_sub_expression }

            defineConcatenationRule(numeric_add_sub_expression) {
              numeric_mult_div_expression and repeated(numeric_add_sub_expression_rhs)
              evaluatesToExpression { results ->
                var lastLhs = results.getFirstAsMatchedRule().computeMathExpression()
                for (index in 1..results.size) {
                  val matchedRule = results.getMatchedRule(index)
                  lastLhs = MathExpression.newBuilder().apply {
                    binaryOperation = MathBinaryOperation.newBuilder().apply {
                      operator = when (val rule = matchedRule.getChildAsRule(index = 0).ruleName) {
                        numeric_mult_div_expression -> MathBinaryOperation.Operator.ADD
                        numeric_add_sub_expression_rhs -> MathBinaryOperation.Operator.SUBTRACT
                        else -> error("Encountered invalid rule in add/sub exp: $rule")
                      }
                      this.operator = operator
                      leftOperand = lastLhs
                      rightOperand = matchedRule.computeMathExpression()
                    }.build()
                  }.build()
                }
                return@evaluatesToExpression lastLhs
              }
            }

            defineAlternationRule(numeric_add_sub_expression_rhs) {
              numeric_add_expression_rhs or numeric_sub_expression_rhs
              evaluatesToExpression { it.getFirstAsMatchedRule().computeMathExpression() }
            }

            defineConcatenationRule(numeric_add_expression_rhs) {
              token<PlusSymbol>() and numeric_mult_div_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineConcatenationRule(numeric_sub_expression_rhs) {
              token<MinusSymbol>() and numeric_mult_div_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineConcatenationRule(numeric_mult_div_expression) {
              numeric_exp_expression and repeated(numeric_mult_div_expression_rhs)
              evaluatesToExpression { results ->
                var lastLhs = results.getFirstAsMatchedRule().computeMathExpression()
                for (index in 1..results.size) {
                  val matchedRule = results.getMatchedRule(index)
                  lastLhs = MathExpression.newBuilder().apply {
                    binaryOperation = MathBinaryOperation.newBuilder().apply {
                      operator = when (val rule = matchedRule.getChildAsRule(index = 0).ruleName) {
                        numeric_mult_expression_rhs, numeric_implicit_mult_expression_rhs ->
                          MathBinaryOperation.Operator.MULTIPLY
                        numeric_div_expression_rhs -> MathBinaryOperation.Operator.DIVIDE
                        else -> error("Encountered invalid rule in mult/div exp: $rule")
                      }
                      this.operator = operator
                      leftOperand = lastLhs
                      rightOperand = matchedRule.computeMathExpression()
                    }.build()
                  }.build()
                }
                return@evaluatesToExpression lastLhs
              }
            }

            defineAlternationRule(numeric_mult_div_expression_rhs) {
              numeric_mult_expression_rhs or
                numeric_div_expression_rhs or
                numeric_implicit_mult_expression_rhs
              evaluatesToExpression { it.getFirstAsMatchedRule().computeMathExpression() }
            }

            defineConcatenationRule(numeric_mult_expression_rhs) {
              token<MultiplySymbol>() and numeric_exp_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineConcatenationRule(numeric_div_expression_rhs) {
              token<DivideSymbol>() and numeric_exp_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineSingletonRule(numeric_implicit_mult_expression_rhs) {
              numeric_term_without_unary_without_number
            }

            defineConcatenationRule(numeric_exp_expression) {
              numeric_term_with_unary and optional(numeric_exp_expression_tail)
              evaluatesToExpression { results ->
                val possibleLhs = results.getFirstAsMatchedRule().computeMathExpression()
                return@evaluatesToExpression if (results.size > 1) {
                  MathExpression.newBuilder().apply {
                    binaryOperation = MathBinaryOperation.newBuilder().apply {
                      operator = MathBinaryOperation.Operator.EXPONENTIATE
                      leftOperand = possibleLhs
                      rightOperand = results.getMatchedRule(index = 1).computeMathExpression()
                    }.build()
                  }.build()
                } else possibleLhs
              }
            }

            defineConcatenationRule(numeric_exp_expression_tail) {
              token<ExponentiationSymbol>() and numeric_exp_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineAlternationRule(numeric_term_with_unary) {
              number or numeric_term_without_unary_without_number or numeric_plus_minus_unary_term
              evaluatesToExpression { it.getFirstAsMatchedRule().computeMathExpression() }
            }

            defineAlternationRule(numeric_term_without_unary_without_number) {
              numeric_function_expression or numeric_group_expression or numeric_rooted_term
              evaluatesToExpression { it.getFirstAsMatchedRule().computeMathExpression() }
            }

            defineConcatenationRule(numeric_function_expression) {
              token<FunctionName>() and
                token<LeftParenthesisSymbol>() and
                numeric_expression and
                token<RightParenthesisSymbol>()
              evaluatesToExpression { it.getMatchedRule(index = 2).computeMathExpression() }
            }

            defineConcatenationRule(numeric_group_expression) {
              token<LeftParenthesisSymbol>() and
                numeric_expression and
                token<RightParenthesisSymbol>()
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineAlternationRule(numeric_plus_minus_unary_term) {
              numeric_negated_term or numeric_positive_term
              evaluatesToExpression { it.getFirstAsMatchedRule().computeMathExpression() }
            }

            defineConcatenationRule(numeric_negated_term) {
              token<MinusSymbol>() and numeric_mult_div_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineConcatenationRule(numeric_positive_term) {
              token<PlusSymbol>() and numeric_mult_div_expression
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineConcatenationRule(numeric_rooted_term) {
              token<SquareRootSymbol>() and numeric_term_with_unary
              evaluatesToExpression { it.getMatchedRule(index = 1).computeMathExpression() }
            }

            defineAlternationRule(number) {
              token<PositiveInteger>() or token<PositiveRealNumber>()

              evaluatesToExpression { results ->
                MathExpression.newBuilder().apply {
                  constant = Real.newBuilder().apply {
                    when (val token = results.getFirstAsToken()) {
                      is PositiveInteger -> integer = token.parsedValue
                      is PositiveRealNumber -> irrational = token.parsedValue
                      else -> error("Encountered invalid token during expression creation: $token")
                    }
                  }.build()
                }.build()
              }
            }
          }
        }
      }

      enum class ProductionRules {
        numeric_expression_grammar,
        numeric_expression,
        numeric_add_sub_expression,
        numeric_add_sub_expression_rhs,
        numeric_add_expression_rhs,
        numeric_sub_expression_rhs,
        numeric_mult_div_expression,
        numeric_mult_div_expression_rhs,
        numeric_mult_expression_rhs,
        numeric_div_expression_rhs,
        numeric_implicit_mult_expression_rhs,
        numeric_exp_expression,
        numeric_exp_expression_tail,
        numeric_term_with_unary,
        numeric_term_without_unary_without_number,
        numeric_function_expression,
        numeric_group_expression,
        numeric_plus_minus_unary_term,
        numeric_negated_term,
        numeric_positive_term,
        numeric_rooted_term,
        number
      }
    }

    @ProductionRuleMarker
    sealed class ProductionRuleDefinition<T: Enum<T>>(private val name: T) {
      protected val rules = mutableListOf<ProductionRule<T>>()
      private var expressionEvaluator: ExpressionEvaluator<T>? = null

      inline fun <reified V : Token> token(): ProductionRule<T> =
        ProductionRule.Terminal.create<T, V>()

      fun optional(name: T): ProductionRule<T> = ProductionRule.Optional(name)

      fun repeated(name: T): ProductionRule<T> = ProductionRule.Repeated(name)

      fun evaluatesToExpression(evaluator: ExpressionEvaluator<T>) {
        check(expressionEvaluator == null) { "Expected evaluator to not already be defined." }
        expressionEvaluator = evaluator
      }

      abstract fun toProductionRule(): ProductionRule.NonTerminal<T>

      protected fun verifyHasRules() {
        check(rules.isNotEmpty()) { "Expected at least one definition in rule: $name." }
      }

      protected fun ensureHasExpressionEvaluator(): ExpressionEvaluator<T> {
        return checkNotNull(expressionEvaluator) {
          "evaluatesToExpression {} must be set up for rule: $name."
        }
      }

      class Singleton<T: Enum<T>>(
        name: T, private val value: T
      ): ProductionRuleDefinition<T>(name) {
        override fun toProductionRule(): ProductionRule.NonTerminal<T> =
          ProductionRule.NonTerminal.Reference(value)
      }

      @ProductionRuleMarker
      class Concatenation<T : Enum<T>>(
        private val name: T
      ) : ProductionRuleDefinition<T>(name) {
        infix fun <A> A.and(rhs: T) = this.and(ProductionRule.Singleton(rhs))

        infix fun <A> A.and(rhs: ProductionRule<T>) {
          rules += rhs
        }

        override fun toProductionRule(): ProductionRule.NonTerminal<T> {
          verifyHasRules()
          return ProductionRule.NonTerminal.Concatenation(
            name, rules, ensureHasExpressionEvaluator()
          )
        }
      }

      @ProductionRuleMarker
      class Alternation<T : Enum<T>>(
        private val name: T
      ) : ProductionRuleDefinition<T>(name) {
        infix fun <A> A.or(rhs: T) = this.or(ProductionRule.Singleton(rhs))

        infix fun <A> A.or(rhs: ProductionRule<T>) {
          rules += rhs
        }

        override fun toProductionRule(): ProductionRule.NonTerminal<T> {
          verifyHasRules()
          return ProductionRule.NonTerminal.Alternation(name, rules, ensureHasExpressionEvaluator())
        }
      }

      companion object {
        sealed class ProductionRule<T : Enum<T>> {
          abstract fun initialize(rules: Map<T, ProductionRule<T>>)

          abstract fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean

          // TODO: consider putting the tokens iterator in the context if we go with this impl approach.
          abstract fun parse(
            tokens: PeekableIterator<Token>,
            parseContext: ParseContext
          ): List<ProductionMatchResult<T>>

          class Terminal<T : Enum<T>, V : Token>(
            private val checkNextTokenMatchesExpectedType: PeekableIterator<Token>.() -> Boolean,
            private val consumeToken: PeekableIterator<Token>.() -> V
          ) : ProductionRule<T>() {
            override fun initialize(rules: Map<T, ProductionRule<T>>) {
              // Nothing to do.
            }

            override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
              tokens.checkNextTokenMatchesExpectedType()

            override fun parse(
              tokens: PeekableIterator<Token>,
              parseContext: ParseContext
            ): List<ProductionMatchResult<T>> =
              listOf(ProductionMatchResult.MatchedToken(tokens.consumeToken()))

            companion object {
              inline fun <T : Enum<T>, reified V : Token> create(): Terminal<T, V> =
                Terminal({ peek() is V }, { consumeTokenOfType() })
            }
          }

          class Singleton<T : Enum<T>>(private val name: T) : ProductionRule<T>() {
            private lateinit var rule: ProductionRule<T>

            override fun initialize(rules: Map<T, ProductionRule<T>>) {
              if (!::rule.isInitialized) {
                rule = rules.getValue(name)
                rule.initialize(rules)
              }
            }

            override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
              rule.hasNext(tokens, parseContext)

            override fun parse(
              tokens: PeekableIterator<Token>,
              parseContext: ParseContext
            ): List<ProductionMatchResult<T>> = rule.parse(tokens, parseContext)
          }

          class Optional<T : Enum<T>>(private val name: T) : ProductionRule<T>() {
            private lateinit var rule: ProductionRule<T>

            override fun initialize(rules: Map<T, ProductionRule<T>>) {
              if (!::rule.isInitialized) {
                rule = rules.getValue(name)
                rule.initialize(rules)
              }
            }

            override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
              rule.hasNext(tokens, parseContext)

            override fun parse(
              tokens: PeekableIterator<Token>,
              parseContext: ParseContext
            ): List<ProductionMatchResult<T>> {
              // Can be "parsed" even if it's absent (such as in concatenation groups).
              return if (hasNext(tokens, parseContext)) {
                rule.parse(tokens, parseContext)
              } else listOf()
            }
          }

          class Repeated<T : Enum<T>>(private val name: T) : ProductionRule<T>() {
            private lateinit var rule: ProductionRule<T>

            override fun initialize(rules: Map<T, ProductionRule<T>>) {
              if (!::rule.isInitialized) {
                rule = rules.getValue(name)
                rule.initialize(rules)
              }
            }

            override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
              rule.hasNext(tokens, parseContext)

            override fun parse(
              tokens: PeekableIterator<Token>,
              parseContext: ParseContext
            ): List<ProductionMatchResult<T>> {
              return generateSequence {
                if (hasNext(tokens, parseContext)) {
                  rule.parse(tokens, parseContext)
                } else listOf()
              }.flatten().toList()
            }
          }

          sealed class NonTerminal<T : Enum<T>>(): ProductionRule<T>() {
            abstract fun computeMathExpression(
              results: List<ProductionMatchResult<T>>
            ): MathExpression

            class Reference<T : Enum<T>>(private val name: T) : NonTerminal<T>() {
              private lateinit var rule: NonTerminal<T>

              override fun initialize(rules: Map<T, ProductionRule<T>>) {
                if (!::rule.isInitialized) {
                  rule = rules.getValue(name) as NonTerminal<T>
                  rule.initialize(rules)
                }
              }

              override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
                rule.hasNext(tokens, parseContext)

              override fun parse(
                tokens: PeekableIterator<Token>,
                parseContext: ParseContext
              ): List<ProductionMatchResult<T>> = rule.parse(tokens, parseContext)

              override fun computeMathExpression(
                results: List<ProductionMatchResult<T>>
              ): MathExpression = rule.computeMathExpression(results)
            }

            class Concatenation<T : Enum<T>>(
              private val name: T,
              private val rules: List<ProductionRule<T>>,
              private val expressionEvaluator: ExpressionEvaluator<T>
            ) : NonTerminal<T>() {
              private var isInitialized = false

              override fun initialize(rules: Map<T, ProductionRule<T>>) {
                if (!isInitialized) {
                  isInitialized = true
                  this.rules.forEach { it.initialize(rules) }
                }
              }

              override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
                rules.first().hasNext(tokens, parseContext)

              override fun parse(
                tokens: PeekableIterator<Token>,
                parseContext: ParseContext
              ): List<ProductionMatchResult<T>> {
                val results = rules.flatMap { it.parse(tokens, parseContext) }
                return listOf(ProductionMatchResult.MatchedRule(name, this, results))
              }

              override fun computeMathExpression(
                results: List<ProductionMatchResult<T>>
              ): MathExpression = expressionEvaluator(results)
            }

            class Alternation<T : Enum<T>>(
              private val name: T,
              private val rules: List<ProductionRule<T>>,
              private val expressionEvaluator: ExpressionEvaluator<T>
            ) : NonTerminal<T>() {
              private var isInitialized = false

              override fun initialize(rules: Map<T, ProductionRule<T>>) {
                if (!isInitialized) {
                  isInitialized = true
                  this.rules.forEach { it.initialize(rules) }
                }
              }

              override fun hasNext(tokens: PeekableIterator<Token>, parseContext: ParseContext): Boolean =
                rules.any { it.hasNext(tokens, parseContext) }

              override fun parse(
                tokens: PeekableIterator<Token>,
                parseContext: ParseContext
              ): List<ProductionMatchResult<T>> {
                val firstMatchingRule = rules.find { it.hasNext(tokens, parseContext) }
                // TODO: add context for the failure for error classification.
                val results = firstMatchingRule?.parse(tokens, parseContext)
                  ?: throw ParseException()
                return listOf(ProductionMatchResult.MatchedRule(name, this, results))
              }

              override fun computeMathExpression(
                results: List<ProductionMatchResult<T>>
              ): MathExpression = expressionEvaluator(results)
            }
          }
        }

        sealed class ProductionMatchResult<T : Enum<T>> {
          abstract val ruleName: T?

          class MatchedToken<T : Enum<T>>(val token: Token): ProductionMatchResult<T>() {
            override val ruleName: T? = null
          }

          class MatchedRule<T: Enum<T>>(
            override val ruleName: T,
            private val parent: ProductionRule.NonTerminal<T>,
            private val children: List<ProductionMatchResult<T>>
          ): ProductionMatchResult<T>() {
            fun computeMathExpression(): MathExpression = parent.computeMathExpression(children)

            fun getChildAsRule(index: Int): MatchedRule<T> {
              check(children.size > index) {
                "Expected child list to be at least size ${index + 1}"
              }
              val result = children[index]
              return result as? MatchedRule<T> ?: error("Expected MatchedRule type for $result")
            }
          }
        }

        fun <T: Enum<T>> List<ProductionMatchResult<T>>.getFirstAsToken(): Token =
          getToken(index = 0)

        fun <T: Enum<T>> List<ProductionMatchResult<T>>.getFirstAsMatchedRule(): ProductionMatchResult.MatchedRule<T> =
          getMatchedRule(index = 0)

        fun <T: Enum<T>> List<ProductionMatchResult<T>>.getToken(index: Int): Token =
          getResultWithType<T, ProductionMatchResult.MatchedToken<T>>(index).token

        fun <T: Enum<T>> List<ProductionMatchResult<T>>.getMatchedRule(index: Int): ProductionMatchResult.MatchedRule<T> =
          getResultWithType(index)

        private inline fun <
          E: Enum<E>, reified T: ProductionMatchResult<E>
        > List<ProductionMatchResult<E>>.getResultWithType(index: Int): T {
          check(size > index) { "Expected result list to be at least size ${index + 1}" }
          val result = this[index]
          return result as? T ?: error("Expected different type for $result")
        }
      }
    }
  }
}

// TODO: make this not bad (e.g. by extracting the generic stuff to a separate package/class).
private typealias ExpressionEvaluator<T> = (List<NumericExpressionParser.Companion.ProductionRuleDefinition.Companion.ProductionMatchResult<T>>) -> MathExpression

inline fun <reified T : Token> PeekableIterator<Token>.consumeTokenOfType(): T {
  return (expectNextMatches { it is T } as? T) ?: throw NumericExpressionParser.ParseException()
}
