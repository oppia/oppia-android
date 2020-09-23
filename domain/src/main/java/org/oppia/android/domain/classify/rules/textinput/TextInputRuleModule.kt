package org.oppia.android.domain.classify.rules.textinput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.TextInputRules

/** Module that binds rule classifiers corresponding to the text input interaction. */
@Module
class TextInputRuleModule {
  @Provides
  @IntoMap
  @StringKey("CaseSensitiveEquals")
  @TextInputRules
  internal fun provideTextInputCaseSensitiveEqualsRuleClassifier(
    classifierProvider: TextInputCaseSensitiveEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("Contains")
  @TextInputRules
  internal fun provideTextInputContainsRuleClassifier(
    classifierProvider: TextInputContainsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("Equals")
  @TextInputRules
  internal fun provideTextInputEqualsRuleClassifier(
    classifierProvider: TextInputEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("FuzzyEquals")
  @TextInputRules
  internal fun provideTextInputFuzzyEqualsRuleClassifier(
    classifierProvider: TextInputFuzzyEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("StartsWith")
  @TextInputRules
  internal fun provideTextInputStartsWithRuleClassifier(
    classifierProvider: TextInputStartsWithRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
