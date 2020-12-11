package org.oppia.android.domain.classify.rules

import javax.inject.Qualifier

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the continue interaction. */
@Qualifier
annotation class ContinueRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the fraction input interaction. */
@Qualifier
annotation class FractionInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the item selection interaction. */
@Qualifier
annotation class ItemSelectionInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the multiple choice interaction. */
@Qualifier
annotation class MultipleChoiceInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the number with units interaction. */
@Qualifier
annotation class NumberWithUnitsRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the text input interaction. */
@Qualifier
annotation class TextInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the numeric input interaction. */
@Qualifier
annotation class NumericInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the drag drop sort input interaction. */
@Qualifier
annotation class DragDropSortInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the image click input interaction. */
@Qualifier
annotation class ImageClickInputRules

/** Corresponds to [org.oppia.android.domain.classify.RuleClassifier]s that can be used by the ratio input interaction. */
@Qualifier
annotation class RatioExpressionInputRules
