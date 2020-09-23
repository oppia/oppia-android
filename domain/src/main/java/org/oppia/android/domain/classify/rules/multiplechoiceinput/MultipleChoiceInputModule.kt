package org.oppia.android.domain.classify.rules.multiplechoiceinput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.MultipleChoiceInputRules

/** Module that binds rule classifiers corresponding to the multiple choice input interaction. */
@Module
class MultipleChoiceInputModule {
  @Provides
  @IntoMap
  @StringKey("Equals")
  @MultipleChoiceInputRules
  internal fun provideMultipleChoiceInputEqualsRuleClassifier(
    classifierProvider: MultipleChoiceInputEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
