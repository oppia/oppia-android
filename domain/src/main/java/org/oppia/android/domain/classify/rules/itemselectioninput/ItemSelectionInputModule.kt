package org.oppia.domain.classify.rules.itemselectioninput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.ItemSelectionInputRules

/** Module that binds rule classifiers corresponding to the item selection choice input interaction. */
@Module
class ItemSelectionInputModule {
  @Provides
  @IntoMap
  @StringKey("ContainsAtLeastOneOf")
  @ItemSelectionInputRules
  internal fun provideItemSelectionInputContainsAtLeastOneOfRuleClassifier(
    classifierProvider: ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("DoesNotContainAtLeastOneOf")
  @ItemSelectionInputRules
  internal fun provideItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifier(
    classifierProvider: ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("Equals")
  @ItemSelectionInputRules
  internal fun provideItemSelectionInputEqualsRuleClassifier(
    classifierProvider: ItemSelectionInputEqualsRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsProperSubsetOf")
  @ItemSelectionInputRules
  internal fun provideItemSelectionInputIsProperSubsetOfRuleClassifier(
    classifierProvider: ItemSelectionInputIsProperSubsetOfRuleClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
