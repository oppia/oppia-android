package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.math.MathExpressionAccessibilityUtil
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
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
import javax.inject.Inject
import org.oppia.android.app.model.MathBinaryOperation.Operator as UnaryOperator

/**
 * [StateItemViewModel] for input for numeric expressions, algebraic expressions, and math
 * (algebraic) equations.
 */
class MathExpressionInteractionsViewModel private constructor(
  interaction: Interaction,
  val hasConversationView: Boolean,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
  private val interactionType: InteractionType
) : StateItemViewModel(interactionType.viewType), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null

  /**
   * Defines the current answer text being entered by the learner. This is expected to be directly
   * bound to the corresponding edit text.
   */
  var answerText: CharSequence = ""

  /**
   * Defines whether an answer is currently available to parse. This is expected to be directly
   * bound to the UI.
   */
  var isAnswerAvailable = ObservableField(false)

  /**
   * Specifies the current error caused by the current answer (if any; this is empty if there is no
   * error). This is expected to be directly bound to the UI.
   */
  var errorMessage = ObservableField("")

  /** Specifies the text to show in the answer box when no text is entered. */
  val hintText: CharSequence = deriveHintText(interaction)

  private val allowedVariables = retrieveAllowedVariables(interaction)
  private val useFractionsForDivision =
    interaction.customizationArgsMap["useFractionForDivision"]?.boolValue ?: false

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError,
            answerText.isNotEmpty()
          )
        }
      }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      answer = InteractionObject.newBuilder().apply {
        mathExpression = answerTextString
      }.build()

      // Since the LaTeX is embedded without a JSON object, backslashes need to be double escaped.
      val answerAsLatex =
        interactionType.computeLatex(
          answerTextString, useFractionsForDivision, allowedVariables
        )?.replace("\\", "\\\\")
      if (answerAsLatex != null) {
        val mathContentValue = "{&amp;quot;raw_latex&amp;quot;:&amp;quot;$answerAsLatex&amp;quot;}"
        htmlAnswer =
          "<oppia-noninteractive-math render-type=\"block\"" +
          " math_content-with-value=\"$mathContentValue\" />"
      } else plainAnswer = answerTextString

      contentDescription =
        interactionType.computeHumanReadableString(
        answerTextString,
        useFractionsForDivision,
        allowedVariables,
        mathExpressionAccessibilityUtil,
        this@MathExpressionInteractionsViewModel.writtenTranslationContext.language
      ) ?: answerTextString

      this.writtenTranslationContext =
        this@MathExpressionInteractionsViewModel.writtenTranslationContext
    }
  }.build()

  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      pendingAnswerError = when (category) {
        // There's no support for real-time errors.
        AnswerErrorCategory.REAL_TIME -> null
        AnswerErrorCategory.SUBMIT_TIME -> {
          interactionType.computeSubmitTimeError(
            answerText.toString(), allowedVariables, resourceHandler
          )
        }
      }
      errorMessage.set(pendingAnswerError)
    }
    return pendingAnswerError
  }

  /**
   * Returns the [TextWatcher] which helps track the current pending answer and whether there is one
   * presently being entered.
   */
  fun getAnswerTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        answerText = answer.toString().trim()
        val isAnswerTextAvailable = answerText.isNotEmpty()
        if (isAnswerTextAvailable != isAnswerAvailable.get()) {
          isAnswerAvailable.set(isAnswerTextAvailable)
        }
        checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    // The subtitled unicode can apparently exist in the structure in two different formats.
    if (interactionType.hasPlaceholder) {
      val placeholderUnicodeOption1 =
        interaction.customizationArgsMap["placeholder"]?.subtitledUnicode
      val placeholderUnicodeOption2 =
        interaction.customizationArgsMap["placeholder"]?.customSchemaValue?.subtitledUnicode
      val customPlaceholder1 =
        placeholderUnicodeOption1?.let { unicode ->
          translationController.extractString(unicode, writtenTranslationContext)
        } ?: ""
      val customPlaceholder2 =
        placeholderUnicodeOption2?.let { unicode ->
          translationController.extractString(unicode, writtenTranslationContext)
        } ?: ""
      return when {
        customPlaceholder1.isNotEmpty() -> customPlaceholder1
        customPlaceholder2.isNotEmpty() -> customPlaceholder2
        else -> resourceHandler.getStringInLocale(interactionType.defaultHintTextStringId)
      }
    } else return resourceHandler.getStringInLocale(interactionType.defaultHintTextStringId)
  }

  private fun retrieveAllowedVariables(interaction: Interaction): List<String> {
    return if (interactionType.hasCustomVariables) {
      interaction.customizationArgsMap["customOskLetters"]
        ?.schemaObjectList
        ?.schemaObjectList
        ?.map { it.normalizedString }
        ?: listOf()
    } else listOf()
  }

  /**
   * Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. Note that
   * instances of this class must be created by injecting [FactoryFactoryImpl].
   */
  class FactoryImpl private constructor(
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController,
    private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
    private val interactionType: InteractionType
  ) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?
    ): StateItemViewModel {
      return MathExpressionInteractionsViewModel(
        interaction,
        hasConversationView,
        answerErrorReceiver,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        mathExpressionAccessibilityUtil,
        interactionType
      )
    }

    /** A factory for [FactoryImpl]s based on for which interaction the factory is needed. */
    class FactoryFactoryImpl @Inject constructor(
      private val resourceHandler: AppLanguageResourceHandler,
      private val translationController: TranslationController,
      private val mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil
    ) {
      /** Returns a new instance of [FactoryImpl] for NumericExpressionInput. */
      fun createFactoryForNumericExpression(): InteractionItemFactory {
        return FactoryImpl(
          resourceHandler,
          translationController,
          mathExpressionAccessibilityUtil,
          InteractionType.NUMERIC_EXPRESSION
        )
      }

      /** Returns a new instance of [FactoryImpl] for AlgebraicExpressionInput. */
      fun createFactoryForAlgebraicExpression(): InteractionItemFactory {
        return FactoryImpl(
          resourceHandler,
          translationController,
          mathExpressionAccessibilityUtil,
          InteractionType.ALGEBRAIC_EXPRESSION
        )
      }

      /** Returns a new instance of [FactoryImpl] for MathEquationInput. */
      fun createFactoryForMathEquation(): InteractionItemFactory {
        return FactoryImpl(
          resourceHandler,
          translationController,
          mathExpressionAccessibilityUtil,
          InteractionType.MATH_EQUATION
        )
      }
    }
  }

  private companion object {
    private enum class InteractionType(
      val viewType: ViewType,
      @StringRes val defaultHintTextStringId: Int,
      val hasPlaceholder: Boolean,
      val hasCustomVariables: Boolean
    ) {
      /** Defines the view model behaviors corresponding to numeric expressions. */
      NUMERIC_EXPRESSION(
        ViewType.NUMERIC_EXPRESSION_INPUT_INTERACTION,
        defaultHintTextStringId = R.string.state_fragment_numeric_input_interaction_default_hint,
        hasPlaceholder = true,
        hasCustomVariables = false
      ) {
        override fun computeLatex(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>
        ): String? {
          return parseAnswer(answerText, allowedVariables)
            .getResult()
            ?.toRawLatex(useFractionsForDivision)
        }

        override fun computeHumanReadableString(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>,
          mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
          language: OppiaLanguage
        ): String? {
          return parseAnswer(answerText, allowedVariables).getResult()?.let { exp ->
            mathExpressionAccessibilityUtil.convertToHumanReadableString(
              exp, language, useFractionsForDivision
            )
          }
        }

        override fun parseAnswer(
          answerText: String,
          allowedVariables: List<String>
        ): MathParsingResult<MathExpression> {
          return MathExpressionParser.parseNumericExpression(answerText)
        }
      },

      /** Defines the view model behaviors corresponding to algebraic expressions. */
      ALGEBRAIC_EXPRESSION(
        ViewType.ALGEBRAIC_EXPRESSION_INPUT_INTERACTION,
        defaultHintTextStringId = R.string.state_fragment_algebraic_input_interaction_default_hint,
        hasPlaceholder = false,
        hasCustomVariables = true
      ) {
        override fun computeLatex(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>
        ): String? {
          return parseAnswer(answerText, allowedVariables)
            .getResult()
            ?.toRawLatex(useFractionsForDivision)
        }

        override fun computeHumanReadableString(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>,
          mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
          language: OppiaLanguage
        ): String? {
          return parseAnswer(answerText, allowedVariables).getResult()?.let { exp ->
            mathExpressionAccessibilityUtil.convertToHumanReadableString(
              exp, language, useFractionsForDivision
            )
          }
        }

        override fun parseAnswer(
          answerText: String,
          allowedVariables: List<String>
        ): MathParsingResult<MathExpression> =
          MathExpressionParser.parseAlgebraicExpression(answerText, allowedVariables)
      },

      /** Defines the view model behaviors corresponding to math equations. */
      MATH_EQUATION(
        ViewType.MATH_EQUATION_INPUT_INTERACTION,
        defaultHintTextStringId = R.string.state_fragment_math_equation_default_hint,
        hasPlaceholder = false,
        hasCustomVariables = true
      ) {
        override fun computeLatex(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>
        ): String? {
          return parseAnswer(answerText, allowedVariables)
            .getResult()
            ?.toRawLatex(useFractionsForDivision)
        }

        override fun computeHumanReadableString(
          answerText: String,
          useFractionsForDivision: Boolean,
          allowedVariables: List<String>,
          mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
          language: OppiaLanguage
        ): String? {
          return parseAnswer(answerText, allowedVariables).getResult()?.let { exp ->
            mathExpressionAccessibilityUtil.convertToHumanReadableString(
              exp, language, useFractionsForDivision
            )
          }
        }

        override fun parseAnswer(
          answerText: String,
          allowedVariables: List<String>
        ): MathParsingResult<MathEquation> =
          MathExpressionParser.parseAlgebraicEquation(answerText, allowedVariables)
      };

      /**
       * Computes and returns the human-readable error corresponding to the specified answer and
       * context, or null if there the answer has no errors.
       */
      fun computeSubmitTimeError(
        answerText: String,
        allowedVariables: List<String>,
        appLanguageResourceHandler: AppLanguageResourceHandler
      ): String? {
        return when (val parseResult = parseAnswer(answerText, allowedVariables)) {
          is MathParsingResult.Failure -> when (val error = parseResult.error) {
            is DisabledVariablesInUseError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_invalid_variable_error,
                error.variables.joinToString(separator = ", ")
              )
            }
            EquationIsMissingEqualsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_missing_equals_error
              )
            }
            EquationHasTooManyEqualsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_more_than_one_equals_error
              )
            }
            EquationMissingLhsOrRhsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_hanging_equals_error
              )
            }
            ExponentIsVariableExpressionError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_exponent_has_variable_error
              )
            }
            ExponentTooLargeError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_exponent_too_large_error
              )
            }
            FunctionNameIncompleteError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_incomplete_function_name_error
              )
            }
            GenericError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_generic_error
              )
            }
            HangingSquareRootError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_hanging_square_root_error
              )
            }
            is InvalidFunctionInUseError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_unsupported_function_error,
                error.functionName
              )
            }
            is MultipleRedundantParenthesesError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_multiple_redundant_parentheses_error,
                error.rawExpression
              )
            }
            NestedExponentsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_nested_exponent_error
              )
            }
            is NoVariableOrNumberAfterBinaryOperatorError -> when (error.operator) {
              UnaryOperator.ADD -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_rhs_for_addition_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.SUBTRACT -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_rhs_for_subtraction_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.MULTIPLY -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_rhs_for_multiplication_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.DIVIDE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_rhs_for_division_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.EXPONENTIATE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_rhs_for_exponentiation_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED -> {
                appLanguageResourceHandler.getStringInLocale(
                  R.string.state_fragment_math_expression_generic_error
                )
              }
            }
            is NoVariableOrNumberBeforeBinaryOperatorError -> when (error.operator) {
              UnaryOperator.ADD -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_lhs_for_addition_error,
                  error.operatorSymbol
                )
              }
              // Subtraction can't happen since these cases are treated as negation.
              UnaryOperator.SUBTRACT -> error("This case should never happen.")
              UnaryOperator.MULTIPLY -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_lhs_for_multiplication_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.DIVIDE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_lhs_for_division_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.EXPONENTIATE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.state_fragment_math_expression_missing_lhs_for_exponentiation_error,
                  error.operatorSymbol
                )
              }
              UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED -> {
                appLanguageResourceHandler.getStringInLocale(
                  R.string.state_fragment_math_expression_generic_error
                )
              }
            }
            is NumberAfterVariableError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_number_after_var_error,
                error.variable,
                error.number.toPlainText()
              )
            }
            is RedundantParenthesesForIndividualTermsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_individual_redundant_parentheses_error,
                error.rawExpression
              )
            }
            is SingleRedundantParenthesesError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_single_redundant_parentheses_error,
                error.rawExpression
              )
            }
            SpacesBetweenNumbersError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_space_error
              )
            }
            is SubsequentBinaryOperatorsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_consecutive_binary_operators_error,
                error.operator1,
                error.operator2
              )
            }
            is SubsequentUnaryOperatorsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_consecutive_unary_operators_error
              )
            }
            TermDividedByZeroError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_divided_by_zero_error
              )
            }
            UnbalancedParenthesesError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_unbalanced_parentheses_error
              )
            }
            is UnnecessarySymbolsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.state_fragment_math_expression_unnecessary_symbols_error,
                error.invalidSymbol
              )
            }
            VariableInNumericExpressionError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.state_fragment_math_expression_variable_in_numeric_error
              )
            }
          }
          is MathParsingResult.Success -> null // No errors.
        }
      }

      /**
       * Returns the LaTeX representation of the specified answer with potential customization for
       * treating divisions as fractions per [useFractionsForDivision].
       */
      abstract fun computeLatex(
        answerText: String,
        useFractionsForDivision: Boolean,
        allowedVariables: List<String>
      ): String?

      /**
       * Returns the human-readable accessibility string corresponding to the specified answer with
       * potential customization for treating divisions as fractions per [useFractionsForDivision].
       */
      abstract fun computeHumanReadableString(
        answerText: String,
        useFractionsForDivision: Boolean,
        allowedVariables: List<String>,
        mathExpressionAccessibilityUtil: MathExpressionAccessibilityUtil,
        language: OppiaLanguage
      ): String?

      /** Attempts to parse the provided raw answer and return the [MathParsingResult]. */
      protected abstract fun parseAnswer(
        answerText: String,
        allowedVariables: List<String>
      ): MathParsingResult<*>

      protected companion object {
        /**
         * Returns the successful result from this [MathParsingResult] or null if it's a failure.
         */
        fun <T> MathParsingResult<T>.getResult(): T? = when (this) {
          is MathParsingResult.Success -> result
          is MathParsingResult.Failure -> null
        }
      }
    }
  }
}
