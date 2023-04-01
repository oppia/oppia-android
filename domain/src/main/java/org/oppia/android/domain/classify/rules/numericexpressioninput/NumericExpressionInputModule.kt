package org.oppia.android.domain.classify.rules.numericexpressioninput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.NumericExpressionInputRules

/** Module that binds rule classifiers corresponding to the numeric expression input interaction. */
@Module
class NumericExpressionInputModule {
  @Provides
  @IntoMap
  @StringKey("MatchesExactlyWith")
  @NumericExpressionInputRules
  internal fun provideNumericExpressionInputMatchesExactlyWithRuleClassifier(
    classifierProvider: NumericExpressionInputMatchesExactlyWithRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("MatchesUpToTrivialManipulations")
  @NumericExpressionInputRules
  internal fun provideNumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifier(
    classifierProvider: NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @NumericExpressionInputRules
  internal fun provideNumericExpressionInputIsEquivalentToRuleClassifier(
    classifierProvider: NumericExpressionInputIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
