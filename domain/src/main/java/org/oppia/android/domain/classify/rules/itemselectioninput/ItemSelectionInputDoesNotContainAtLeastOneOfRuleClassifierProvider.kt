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
 * Provider for a classifier that determines whether an item selection answer does not contain at
 * least one element of a set of options per the item selection input interaction, that is, this
 * classifier only matches if an answer contains all of the input options (plus optional extras).
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/ItemSelectionInput/directives/item-selection-input-rules.service.ts#L41
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider
@Inject constructor(
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
  ): Boolean = (input.getContentIdSet() - answer.getContentIdSet()).isNotEmpty()
}
