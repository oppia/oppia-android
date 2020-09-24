package org.oppia.domain.classify.rules.numberwithunits

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.NumberWithUnitsRules

/** Module that binds rule classifiers corresponding to the number with units interaction. */
@Module
class NumberWithUnitsRuleModule {
  @Provides
  @IntoMap
  @StringKey("IsEqualTo")
  @NumberWithUnitsRules
  internal fun provideNumberWithUnitsIsEqualToRuleClassifier(
    classifierProvider: NumberWithUnitsIsEqualToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEquivalentTo")
  @NumberWithUnitsRules
  internal fun provideNumberWithUnitsIsEquivalentToRuleClassifier(
    classifierProvider: NumberWithUnitsIsEquivalentToRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
