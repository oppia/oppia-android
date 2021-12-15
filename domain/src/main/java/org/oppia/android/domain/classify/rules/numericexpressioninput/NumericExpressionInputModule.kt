package org.oppia.android.domain.classify.rules.numericexpressioninput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.FractionInputRules

/** Module that binds rule classifiers corresponding to the numeric expression input interaction. */
@Module
class NumericExpressionInputModule {
  @Provides
  @IntoMap
  @StringKey("MatchesExactlyWith")
  @FractionInputRules
  internal fun provideNumericExpressionInputMatchesExactlyWithRuleClassifierProvider(
    classifierProvider: NumericExpressionInputMatchesExactlyWithRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("MatchesUpToTrivialManipulations")
  @FractionInputRules
  internal fun provideNumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifier(
    classifierProvider: NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @FractionInputRules
  internal fun provideNumericExpressionInputIsEquivalentToRuleClassifier(
    classifierProvider: NumericExpressionInputIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
