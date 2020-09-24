package org.oppia.android.domain.classify.rules.fractioninput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.FractionInputRules

/** Module that binds rule classifiers corresponding to the fraction input interaction. */
@Module
class FractionInputModule {
  @Provides
  @IntoMap
  @StringKey("HasDenominatorEqualTo")
  @FractionInputRules
  internal fun provideFractionInputHasDenominatorEqualToRuleClassifier(
    classifierProvider: FractionInputHasDenominatorEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("HasFractionalPartExactlyEqualTo")
  @FractionInputRules
  internal fun provideFractionInputHasFractionalPartExactlyEqualToRuleClassifier(
    classifierProvider: FractionInputHasFractionalPartExactlyEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("HasIntegerPartEqualTo")
  @FractionInputRules
  internal fun provideFractionInputHasIntegerPartEqualToRuleClassifier(
    classifierProvider: FractionInputHasIntegerPartEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("HasNoFractionalPart")
  @FractionInputRules
  internal fun provideFractionInputHasNoFractionalPartRuleClassifier(
    classifierProvider: FractionInputHasNoFractionalPartRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("HasNumeratorEqualTo")
  @FractionInputRules
  internal fun provideFractionInputHasNumeratorEqualToRuleClassifier(
    classifierProvider: FractionInputHasNumeratorEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentToAndInSimplestForm")
  @FractionInputRules
  internal fun provideFractionInputIsEquivalentToAndInSimplestFormRuleClassifier(
    classifierProvider: FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @FractionInputRules
  internal fun provideFractionInputIsEquivalentToRuleClassifier(
    classifierProvider: FractionInputIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsExactlyEqualTo")
  @FractionInputRules
  internal fun provideFractionInputIsExactlyEqualToRuleClassifier(
    classifierProvider: FractionInputIsExactlyEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsGreaterThan")
  @FractionInputRules
  internal fun provideFractionInputIsGreaterThanRuleClassifier(
    classifierProvider: FractionInputIsGreaterThanRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsLessThan")
  @FractionInputRules
  internal fun provideFractionInputIsLessThanRuleClassifier(
    classifierProvider: FractionInputIsLessThanRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
