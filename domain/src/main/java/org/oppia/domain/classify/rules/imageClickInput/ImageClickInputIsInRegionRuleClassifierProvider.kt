package org.oppia.domain.classify.rules.imageClickInput

import org.oppia.app.model.ClickOnImage
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a clicked region of the image is valid or not.
 *
 * https://github.com/oppia/oppia/blob/37285a9b0e7866cbcdf5f8193a6ac2fab64458a7/extensions/interactions/ImageClickInput/directives/image-click-input-rules.service.ts#L29
 */
class ImageClickInputIsInRegionRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeSingleInputMatcher<ClickOnImage, String> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createMultiTypeSingleInputClassifier(
      InteractionObject.ObjectTypeCase.CLICK_ON_IMAGE,
      InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      "x",
      this
    )
  }

  override fun matches(answer: ClickOnImage, input: String): Boolean {
    return answer.clickedRegionsList.indexOf(input) != -1
  }
}
