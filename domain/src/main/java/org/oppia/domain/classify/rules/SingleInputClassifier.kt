package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import javax.inject.Inject

/**
 * A convenience [RuleClassifier] which performs parameter extraction and sanitation to simplify classifiers, with the
 * assumption that just one parameter is provided as input. This classifier assumes that the type of the input parameter
 * must be the same as the answer being matched.
 *
 * Child classes must ensure the specified type [T] properly corresponds to the type to which the specified
 * [InteractionObject.ObjectTypeCase] also corresponds.
 */
internal class SingleInputClassifier<T: Any> private constructor(
  private val expectedObjectType: InteractionObject.ObjectTypeCase,
  private val inputParameterName: String,
  private val matcher: SingleInputMatcher<T>,
  private val extractObject: (InteractionObject) -> T
): RuleClassifier {
  override fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean {
    check(answer.objectTypeCase == expectedObjectType) {
      "Expected answer to be of type ${expectedObjectType.name} not ${answer.objectTypeCase.name}"
    }
    val input = checkNotNull(inputs[inputParameterName]) {
      "Expected classifier inputs to contain parameter with name '$inputParameterName' but had: ${inputs.keys}"
    }
    check(input.objectTypeCase == expectedObjectType) {
      "Expected input value to be of type ${expectedObjectType.name} not ${input.objectTypeCase.name}"
    }
    return matcher.matches(extractObject(answer), extractObject(input))
  }

  internal interface SingleInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the single validated and extracted input parameter per
     * the specification of this classifier.
     */
    fun matches(answer: T, input: T): Boolean
  }

  /** Creates new [SingleInputClassifier]s. */
  internal class Factory @Inject constructor(
    private val interactionObjectTypeExtractorRepository: InteractionObjectTypeExtractorRepository
  ) {
    /** Returns a new [SingleInputClassifier] for the specified object type, parameter name, and value matcher. */
    inline fun <reified T: Any> create(
      expectedObjectType: InteractionObject.ObjectTypeCase, inputParameterName: String, matcher: SingleInputMatcher<T>
    ): SingleInputClassifier<T> {
      val objectExtractor = interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      return SingleInputClassifier(expectedObjectType, inputParameterName, matcher, objectExtractor)
    }
  }
}
