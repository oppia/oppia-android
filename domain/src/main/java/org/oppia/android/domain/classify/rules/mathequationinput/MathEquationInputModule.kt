package org.oppia.android.domain.classify.rules.mathequationinput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.MathEquationInputRules

/** Module that binds rule classifiers corresponding to the math equation input interaction. */
@Module
class MathEquationInputModule {
  @Provides
  @IntoMap
  @StringKey("MatchesExactlyWith")
  @MathEquationInputRules
  internal fun provideMathEquationInputMatchesExactlyWithRuleClassifier(
    classifierProvider: MathEquationInputMatchesExactlyWithRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("MatchesUpToTrivialManipulations")
  @MathEquationInputRules
  internal fun provideMathEquationInputMatchesUpToTrivialManipulationsRuleClassifier(
    classifierProvider:
      MathEquationInputMatchesUpToTrivialManipulationsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @MathEquationInputRules
  internal fun provideMathEquationInputIsEquivalentToRuleClassifier(
    classifierProvider: MathEquationInputIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
