package org.oppia.domain.classify.rules

import org.oppia.domain.classify.RuleClassifier

/** Provider for [RuleClassifier]s. */
interface RuleClassifierProvider {
  /** Returns a new [RuleClassifier]. */
  fun createRuleClassifier(): RuleClassifier
}
