package org.oppia.domain.classify.rules.textinput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.TextInputRules

/** Module that binds rule classifiers corresponding to the text input interaction. */
@Module
class TextInputRuleModule {
  @Provides
  @IntoMap
  @StringKey("Equals")
  @TextInputRules
  internal fun provideTextInputEqualsRuleClassifier(
    classifierProvider: TextInputEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
