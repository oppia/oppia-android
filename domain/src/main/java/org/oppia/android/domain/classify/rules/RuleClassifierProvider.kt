package org.oppia.android.domain.classify.rules

import org.oppia.android.domain.classify.RuleClassifier

/** Provider for [RuleClassifier]s. */
interface RuleClassifierProvider {
  /** Returns a new [RuleClassifier]. */
  fun createRuleClassifier(): RuleClassifier
}
