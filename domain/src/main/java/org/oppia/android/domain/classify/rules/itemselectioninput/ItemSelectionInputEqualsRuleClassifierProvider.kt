package org.oppia.android.domain.classify.rules.itemselectioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.getContentIdSet
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an item selection answer has exactly the same
 * elements as an input set per the item selection input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/ItemSelectionInput/directives/item-selection-input-rules.service.ts#L24
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class ItemSelectionInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.SingleInputMatcher<SetOfTranslatableHtmlContentIds> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS,
      "x",
      this
    )
  }

  override fun matches(
    answer: SetOfTranslatableHtmlContentIds,
    input: SetOfTranslatableHtmlContentIds,
    classificationContext: ClassificationContext
  ): Boolean = answer.getContentIdSet() == input.getContentIdSet()
}
