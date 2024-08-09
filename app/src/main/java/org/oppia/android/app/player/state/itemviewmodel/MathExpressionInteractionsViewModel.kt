package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
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
import org.oppia.android.util.math.toPlainText
import org.oppia.android.util.math.toRawLatex
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
  private val interactionType: InteractionType,
  userAnswerState: UserAnswerState
) : StateItemViewModel(interactionType.viewType), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null

  /**
   * Defines the current answer text being entered by the learner. This is expected to be directly
   * bound to the corresponding edit text.
   */
  var answerText: CharSequence = userAnswerState.textInputAnswer
    // The value of ths field is set from the Binding and from the TextWatcher. Any
    // programmatic modification needs to be done here, so that the Binding and the TextWatcher
    // do not step on each other.
    set(value) {
      field = value.toString().trim()
    }

  private var answerErrorCetegory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR

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
            inputAnswerAvailable = true // Allow blank answer submission.
          )
        }
      }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)

    // Initializing with default values so that submit button is enabled by default.
    errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      pendingAnswerError = null,
      inputAnswerAvailable = true
    )
    checkPendingAnswerError(userAnswerState.answerErrorCategory)
  }

  override fun getUserAnswerState(): UserAnswerState {
    return UserAnswerState.newBuilder().apply {
      this.textInputAnswer = answerText.toString()
      this.answerErrorCategory = answerErrorCetegory
    }.build()
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
    answerErrorCetegory = category
    pendingAnswerError = when (category) {
      // There's no support for real-time errors.
      AnswerErrorCategory.REAL_TIME -> null
      AnswerErrorCategory.SUBMIT_TIME -> {
        interactionType.computeSubmitTimeError(
          answerText.toString(), allowedVariables, resourceHandler
        )
      }
      else -> null
    }
    errorMessage.set(pendingAnswerError)
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
        answerText = answer
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
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState
    ): StateItemViewModel {
      return MathExpressionInteractionsViewModel(
        interaction,
        hasConversationView,
        answerErrorReceiver,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        mathExpressionAccessibilityUtil,
        interactionType,
        userAnswerState
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
    /**
     * Enum class representing different types of interactions in a mathematical expression input field.
     */
    enum class InteractionType(
      val viewType: ViewType,
      @StringRes val defaultHintTextStringId: Int,
      val hasPlaceholder: Boolean,
      val hasCustomVariables: Boolean
    ) {
      /** Defines the view model behaviors corresponding to numeric expressions. */
      NUMERIC_EXPRESSION(
        ViewType.NUMERIC_EXPRESSION_INPUT_INTERACTION,
        defaultHintTextStringId = R.string.numeric_expression_default_hint_text,
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
        defaultHintTextStringId = R.string.algebraic_expression_default_hint_text,
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
        defaultHintTextStringId = R.string.math_equation_default_hint_text,
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
        if (answerText.isBlank()) {
          return when (this) {
            NUMERIC_EXPRESSION -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.numeric_expression_error_empty_input
              )
            }
            ALGEBRAIC_EXPRESSION -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.algebraic_expression_error_empty_input
              )
            }
            MATH_EQUATION -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_equation_error_empty_input
              )
            }
          }
        }
        return when (val parseResult = parseAnswer(answerText, allowedVariables)) {
          is MathParsingResult.Failure -> when (val error = parseResult.error) {
            is DisabledVariablesInUseError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_invalid_variable,
                error.variables.joinToString(separator = ", ")
              )
            }
            EquationIsMissingEqualsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_missing_equals
              )
            }
            EquationHasTooManyEqualsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_more_than_one_equals
              )
            }
            EquationMissingLhsOrRhsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_hanging_equals
              )
            }
            ExponentIsVariableExpressionError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_exponent_has_variable
              )
            }
            ExponentTooLargeError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_exponent_too_large
              )
            }
            FunctionNameIncompleteError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_incomplete_function_name
              )
            }
            GenericError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_generic
              )
            }
            HangingSquareRootError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_hanging_square_root
              )
            }
            is InvalidFunctionInUseError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_unsupported_function, error.functionName
              )
            }
            is MultipleRedundantParenthesesError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_multiple_redundant_parentheses, error.rawExpression
              )
            }
            NestedExponentsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_nested_exponent
              )
            }
            is NoVariableOrNumberAfterBinaryOperatorError -> when (error.operator) {
              UnaryOperator.ADD -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_rhs_for_addition_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.SUBTRACT -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_rhs_for_subtraction_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.MULTIPLY -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_rhs_for_multiplication_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.DIVIDE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_rhs_for_division_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.EXPONENTIATE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_rhs_for_exponentiation_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED -> {
                appLanguageResourceHandler.getStringInLocale(
                  R.string.math_expression_error_generic
                )
              }
            }
            is NoVariableOrNumberBeforeBinaryOperatorError -> when (error.operator) {
              UnaryOperator.ADD -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_lhs_for_addition_operator,
                  error.operatorSymbol
                )
              }
              // Subtraction can't happen since these cases are treated as negation.
              UnaryOperator.SUBTRACT -> error("This case should never happen.")
              UnaryOperator.MULTIPLY -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_lhs_for_multiplication_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.DIVIDE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_lhs_for_division_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.EXPONENTIATE -> {
                appLanguageResourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_expression_error_missing_lhs_for_exponentiation_operator,
                  error.operatorSymbol
                )
              }
              UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED -> {
                appLanguageResourceHandler.getStringInLocale(
                  R.string.math_expression_error_generic
                )
              }
            }
            is NumberAfterVariableError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_number_after_var_term,
                error.variable,
                error.number.toPlainText()
              )
            }
            is RedundantParenthesesForIndividualTermsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_redundant_parentheses_individual_term,
                error.rawExpression
              )
            }
            is SingleRedundantParenthesesError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_single_redundant_parentheses, error.rawExpression
              )
            }
            SpacesBetweenNumbersError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_spaces_in_numerical_input
              )
            }
            is SubsequentBinaryOperatorsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_consecutive_binary_operators,
                error.operator1,
                error.operator2
              )
            }
            is SubsequentUnaryOperatorsError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_consecutive_unary_operators
              )
            }
            TermDividedByZeroError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_term_divided_by_zero
              )
            }
            UnbalancedParenthesesError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_unbalanced_parentheses
              )
            }
            is UnnecessarySymbolsError -> {
              appLanguageResourceHandler.getStringInLocaleWithWrapping(
                R.string.math_expression_error_unnecessary_symbols, error.invalidSymbol
              )
            }
            VariableInNumericExpressionError -> {
              appLanguageResourceHandler.getStringInLocale(
                R.string.math_expression_error_variable_in_numeric_expression
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
