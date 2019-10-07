package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import javax.inject.Inject

/**
 * TODO(BenHenning): Complete.
 */
internal class NoInputClassifier<T: Any> private constructor(
  private val expectedObjectType: InteractionObject.ObjectTypeCase,
  private val matcher: NoInputInputMatcher<T>,
  private val extractObject: (InteractionObject) -> T
): RuleClassifier {
  // TODO(BenHenning): Consolidate this with SingleInputClassifier. Since SingleInputMatcher is being passed in, this
  // can be generalized to a variable number of parameters.

  override fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean {
    check(answer.objectTypeCase == expectedObjectType) {
      "Expected answer to be of type ${expectedObjectType.name} not ${answer.objectTypeCase.name}"
    }
    return matcher.matches(extractObject(answer))
  }

  internal interface NoInputInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the expectations per the specification of this
     * classifier.
     */
    fun matches(answer: T): Boolean
  }

  /** Creates new [NoInputClassifier]s. */
  internal class Factory @Inject constructor(
    private val interactionObjectTypeExtractorRepository: InteractionObjectTypeExtractorRepository
  ) {
    /** Returns a new [NoInputClassifier] for the specified object type and value matcher. */
    inline fun <reified T: Any> create(
      expectedObjectType: InteractionObject.ObjectTypeCase, matcher: NoInputInputMatcher<T>
    ): NoInputClassifier<T> {
      val objectExtractor = interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      return NoInputClassifier(expectedObjectType, matcher, objectExtractor)
    }
  }
}
