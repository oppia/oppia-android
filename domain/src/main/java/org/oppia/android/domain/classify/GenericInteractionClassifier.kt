package org.oppia.android.domain.classify

/** A general-purpose [InteractionClassifier] that utilizes a Dagger-bound [RuleClassifier] map. */
internal class GenericInteractionClassifier(
  private val ruleClassifiers: Map<String, RuleClassifier>
) : InteractionClassifier {
  override fun getRuleTypes(): Set<String> {
    return ruleClassifiers.keys
  }

  override fun getRuleClassifier(ruleType: String): RuleClassifier? {
    return ruleClassifiers[ruleType]
  }
}
