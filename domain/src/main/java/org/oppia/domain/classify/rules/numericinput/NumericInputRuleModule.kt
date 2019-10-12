package org.oppia.domain.classify.rules.numericinput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.NumericInputRules

/** Module that binds rule classifiers corresponding to the numeric input interaction. */
@Module
class NumericInputRuleModule {
  @Provides
  @IntoMap
  @StringKey("Equals")
  @NumericInputRules
  internal fun provideNumericInputEqualsRuleClassifier(
    classifierProvider: NumericInputEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsGreaterThanOrEqualTo")
  @NumericInputRules
  internal fun provideNumericInputIsGreaterThanOrEqualToRuleClassifier(
    classifierProvider: NumericInputIsGreaterThanOrEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsGreaterThan")
  @NumericInputRules
  internal fun provideNumericInputIsGreaterThanRuleClassifier(
    classifierProvider: NumericInputIsGreaterThanRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsInclusivelyBetween")
  @NumericInputRules
  internal fun provideNumericInputIsInclusivelyBetweenRuleClassifier(
    classifierProvider: NumericInputIsInclusivelyBetweenRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsLessThanOrEqualTo")
  @NumericInputRules
  internal fun provideNumericInputIsLessThanOrEqualToRuleClassifier(
    classifierProvider: NumericInputIsLessThanOrEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsLessThan")
  @NumericInputRules
  internal fun provideNumericInputIsLessThanRuleClassifier(
    classifierProvider: NumericInputIsLessThanRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsWithinTolerance")
  @NumericInputRules
  internal fun provideNumericInputIsWithinToleranceRuleClassifier(
    classifierProvider: NumericInputIsWithinToleranceRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
