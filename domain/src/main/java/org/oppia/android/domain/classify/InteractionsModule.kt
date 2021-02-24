package org.oppia.android.domain.classify

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.rules.ContinueRules
import org.oppia.android.domain.classify.rules.DragDropSortInputRules
import org.oppia.android.domain.classify.rules.FractionInputRules
import org.oppia.android.domain.classify.rules.ImageClickInputRules
import org.oppia.android.domain.classify.rules.ItemSelectionInputRules
import org.oppia.android.domain.classify.rules.MultipleChoiceInputRules
import org.oppia.android.domain.classify.rules.NumberWithUnitsRules
import org.oppia.android.domain.classify.rules.NumericInputRules
import org.oppia.android.domain.classify.rules.RatioExpressionInputRules
import org.oppia.android.domain.classify.rules.TextInputRules

/** Module that provides a map of [InteractionClassifier]s. */
@Module
class InteractionsModule {
  @Provides
  @IntoMap
  @StringKey("Continue")
  fun provideContinueInteractionClassifier(
    @ContinueRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("FractionInput")
  fun provideFractionInputInteractionClassifier(
    @FractionInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("ItemSelectionInput")
  fun provideItemSelectionInputInteractionClassifier(
    @ItemSelectionInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("MultipleChoiceInput")
  fun provideMultipleChoiceInputInteractionClassifier(
    @MultipleChoiceInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("NumberWithUnits")
  fun provideNumberWithUnitsInteractionClassifier(
    @NumberWithUnitsRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("NumericInput")
  fun provideNumericInputInteractionClassifier(
    @NumericInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("TextInput")
  fun provideTextInputInteractionClassifier(
    @TextInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("DragAndDropSortInput")
  fun provideDragAndDropSortInputInteractionClassifier(
    @DragDropSortInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("ImageClickInput")
  fun provideImageClickInputInteractionClassifier(
    @ImageClickInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }

  @Provides
  @IntoMap
  @StringKey("RatioExpressionInput")
  fun provideRatioExpressionInputInteractionClassifier(
    @RatioExpressionInputRules ruleClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>
  ): InteractionClassifier {
    return GenericInteractionClassifier(ruleClassifiers)
  }
}
