package org.oppia.domain.classify.rules.dragAndDropSortInput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.DragDropSortInputRules

/** Module that binds rule classifiers corresponding to the drag drop sort input interaction. */
@Module
class DragDropSortInputModule{
  @Provides
  @IntoMap
  @StringKey("HasElementXAtPositionY")
  @DragDropSortInputRules
  internal fun provideDragDropSortInputHasElementXAtPositionYRuleClassifier(
    classifierProvider: DragDropSortInputHasElementXAtPositionYClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEqualToOrdering")
  @DragDropSortInputRules
  internal fun provideDragDropSortInputIsEqualToOrderingRuleClassifier(
    classifierProvider: DragDropSortInputIsEqualToOrderingClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("HasElementXBeforeElementY")
  @DragDropSortInputRules
  internal fun provideDragDropSortInputHasElementXBeforeElementYRuleClassifier(
    classifierProvider: DragDropSortInputHasElementXBeforeElementYClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()

  @Provides
  @IntoMap
  @StringKey("IsEqualToOrderingWithOneItemIncorrect")
  @DragDropSortInputRules
  internal fun provideDragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionRuleClassifier(
    classifierProvider: DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
