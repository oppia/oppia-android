package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for complete answer
 * classification.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(
  includes = [
    AlgebraicExpressionInputModule::class, ContinueModule::class, DragDropSortInputModule::class,
    FractionInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
    ItemSelectionInputModule::class, MathEquationInputModule::class,
    MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
    NumericExpressionInputModule::class, NumericInputRuleModule::class, RatioInputModule::class,
    TextInputRuleModule::class
  ]
)
interface ClassifyBundleModule
