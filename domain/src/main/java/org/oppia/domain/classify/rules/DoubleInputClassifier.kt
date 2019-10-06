package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import javax.inject.Inject

/**
 * A convenience [RuleClassifier] which performs parameter extraction and sanitation to simplify classifiers, with the
 * assumption that two parameters are provided as input. This classifier assumes that the type of both input parameters
 * must be the same as the answer being matched.
 *
 * Child classes must ensure the specified type [T] properly corresponds to the type to which the specified
 * [InteractionObject.ObjectTypeCase] also corresponds.
 */
internal class DoubleInputClassifier<T: Any> private constructor(
  private val expectedObjectType: InteractionObject.ObjectTypeCase,
  private val firstInputParameterName: String,
  private val secondInputParameterName: String,
  private val matcher: DoubleInputMatcher<T>,
  private val extractObject: (InteractionObject) -> T
): RuleClassifier {
  // TODO(BenHenning): Consolidate this with SingleInputClassifier. Since SingleInputMatcher is being passed in, this
  // can be generalized to a variable number of parameters.

  override fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean {
    check(answer.objectTypeCase == expectedObjectType) {
      "Expected answer to be of type ${expectedObjectType.name} not ${answer.objectTypeCase.name}"
    }
    val firstInput = checkNotNull(inputs[firstInputParameterName]) {
      "Expected classifier inputs to contain parameter with name '$firstInputParameterName' but had: ${inputs.keys}"
    }
    val secondInput = checkNotNull(inputs[secondInputParameterName]) {
      "Expected classifier inputs to contain parameter with name '$secondInputParameterName' but had: ${inputs.keys}"
    }
    check(firstInput.objectTypeCase == expectedObjectType) {
      "Expected first input value to be of type ${expectedObjectType.name} not ${firstInput.objectTypeCase.name}"
    }
    check(secondInput.objectTypeCase == expectedObjectType) {
      "Expected second input value to be of type ${expectedObjectType.name} not ${secondInput.objectTypeCase.name}"
    }
    return matcher.matches(extractObject(answer), extractObject(firstInput), extractObject(secondInput))
  }

  internal interface DoubleInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the two validated and extracted input parameters per
     * the specification of this classifier.
     */
    fun matches(answer: T, firstInput: T, secondInput: T): Boolean
  }

  /** Creates new [DoubleInputClassifier]s. */
  internal class Factory @Inject constructor(
    private val interactionObjectTypeExtractorRepository: InteractionObjectTypeExtractorRepository
  ) {
    /** Returns a new [DoubleInputClassifier] for the specified object type, parameter name, and value matcher. */
    inline fun <reified T: Any> create(
      expectedObjectType: InteractionObject.ObjectTypeCase, firstInputParameterName: String,
      secondInputParameterName: String, matcher: DoubleInputMatcher<T>
    ): DoubleInputClassifier<T> {
      val objectExtractor = interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      return DoubleInputClassifier(
        expectedObjectType, firstInputParameterName, secondInputParameterName, matcher, objectExtractor)
    }
  }
}
