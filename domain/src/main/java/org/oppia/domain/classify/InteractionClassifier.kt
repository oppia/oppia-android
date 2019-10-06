package org.oppia.domain.classify

/**
 * An answer classifier for a specific interaction type. Instances of this classifier should be bound to a map of
 * interaction IDs to classifier instances so that they can be used by the [AnswerClassificationController].
 */
interface InteractionClassifier {
  /** Returns the [RuleClassifier] corresponding to the specified rule type. */
  fun getRuleClassifier(ruleType: String): RuleClassifier?
}
