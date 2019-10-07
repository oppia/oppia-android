package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import javax.inject.Inject

/**
 * TODO(BenHenning): Complete.
 */
internal class MultiTypeSingleInputClassifier<AT: Any, IT: Any> private constructor(
  private val expectedAnswerObjectType: InteractionObject.ObjectTypeCase,
  private val expectedInputObjectType: InteractionObject.ObjectTypeCase,
  private val inputParameterName: String,
  private val matcher: MultiTypeSingleInputMatcher<AT, IT>,
  private val extractAnswerObject: (InteractionObject) -> AT,
  private val extractInputObject: (InteractionObject) -> IT
): RuleClassifier {
  // TODO(BenHenning): Consolidate this with SingleInputClassifier. Since SingleInputMatcher is being passed in, this
  // can be generalized to a variable number of types.

  override fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean {
    check(answer.objectTypeCase == expectedAnswerObjectType) {
      "Expected answer to be of type ${expectedAnswerObjectType.name} not ${answer.objectTypeCase.name}"
    }
    val input = checkNotNull(inputs[inputParameterName]) {
      "Expected classifier inputs to contain parameter with name '$inputParameterName' but had: ${inputs.keys}"
    }
    check(input.objectTypeCase == expectedInputObjectType) {
      "Expected input value to be of type ${expectedInputObjectType.name} not ${input.objectTypeCase.name}"
    }
    return matcher.matches(extractAnswerObject(answer), extractInputObject(input))
  }

  internal interface MultiTypeSingleInputMatcher<AT, IT> {
    /**
     * Returns whether the validated and extracted answer matches the single validated and extracted input parameter per
     * the specification of this classifier.
     */
    fun matches(answer: AT, input: IT): Boolean
  }

  /** Creates new [MultiTypeSingleInputClassifier]s. */
  internal class Factory @Inject constructor(
    private val interactionObjectTypeExtractorRepository: InteractionObjectTypeExtractorRepository
  ) {
    /** Returns a new [MultiTypeSingleInputClassifier] for the specified object type, parameter name, and value matcher. */
    inline fun <reified AT: Any, reified IT: Any> create(
      expectedAnswerObjectType: InteractionObject.ObjectTypeCase,
      expectedInputObjectType: InteractionObject.ObjectTypeCase, inputParameterName: String,
      matcher: MultiTypeSingleInputMatcher<AT, IT>
    ): MultiTypeSingleInputClassifier<AT, IT> {
      val answerObjectExtractor = interactionObjectTypeExtractorRepository.getExtractor<AT>(expectedAnswerObjectType)
      val inputObjectExtractor = interactionObjectTypeExtractorRepository.getExtractor<IT>(expectedInputObjectType)
      return MultiTypeSingleInputClassifier(
        expectedAnswerObjectType, expectedInputObjectType, inputParameterName, matcher, answerObjectExtractor,
        inputObjectExtractor)
    }
  }
}
