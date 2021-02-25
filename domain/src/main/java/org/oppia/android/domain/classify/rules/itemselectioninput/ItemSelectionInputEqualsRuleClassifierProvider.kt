package org.oppia.android.domain.classify.rules.itemselectioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.StringList
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an item selection answer has exactly the same elements as an input
 * set per the item selection input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/ItemSelectionInput/directives/item-selection-input-rules.service.ts#L24
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class ItemSelectionInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<StringList> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING,
      "x",
      this
    )
  }

  override fun matches(answer: StringList, input: StringList): Boolean {
    return answer.htmlList.toSet() == input.htmlList.toSet()
  }
}
