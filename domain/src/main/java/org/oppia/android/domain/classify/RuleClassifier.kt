package org.oppia.android.domain.classify

import org.oppia.android.app.model.InteractionObject

/** An answer classifier for a specific interaction rule. */
interface RuleClassifier {
  /**
   * Returns whether the specified answer matches the rule's parameter inputs per this rule's classification strategy.
   */
  fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean
}
