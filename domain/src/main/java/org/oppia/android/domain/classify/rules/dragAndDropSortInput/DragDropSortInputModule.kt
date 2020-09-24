package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.DragDropSortInputRules

/** Module that binds rule classifiers corresponding to the drag drop sort input interaction. */
@Module
class DragDropSortInputModule {
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
  @StringKey("IsEqualToOrderingWithOneItemAtIncorrectPosition")
  @DragDropSortInputRules
  internal fun provideDragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionRuleClassifier( // ktlint-disable max-line-length
    classifierProvider:
      DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider
  ): RuleClassifier = classifierProvider.createRuleClassifier()
}
