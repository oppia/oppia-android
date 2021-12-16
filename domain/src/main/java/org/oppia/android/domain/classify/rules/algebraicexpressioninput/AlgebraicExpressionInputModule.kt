package org.oppia.android.domain.classify.rules.algebraicexpressioninput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.FractionInputRules

/** Module that binds rule classifiers corresponding to the algebraic expression input interaction. */
@Module
class AlgebraicExpressionInputModule {
  @Provides
  @IntoMap
  @StringKey("MatchesExactlyWith")
  @FractionInputRules
  internal fun provideAlgebraicExpressionInputMatchesExactlyWithRuleClassifierProvider(
    classifierProvider: AlgebraicExpressionInputMatchesExactlyWithRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("MatchesUpToTrivialManipulations")
  @FractionInputRules
  internal fun provideAlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifier(
    classifierProvider: AlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @FractionInputRules
  internal fun provideAlgebraicExpressionInputIsEquivalentToRuleClassifier(
    classifierProvider: AlgebraicExpressionInputIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
