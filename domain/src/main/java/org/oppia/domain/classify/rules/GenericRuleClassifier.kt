package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import javax.inject.Inject

/**
 * A convenience [RuleClassifier] which performs parameter extraction and sanitation to simplify classifiers, with the
 * possible configurations for zero, one, or two parameters with the same or differing types compared to the answer
 * being matched.
 *
 * Child classes must ensure all specified types properly correspond to the type to which the parameter's specified
 * [InteractionObject.ObjectTypeCase] also corresponds.
 */
internal class GenericRuleClassifier private constructor(
  private val expectedAnswerObjectType: InteractionObject.ObjectTypeCase,
  private val orderedExpectedParameterTypes: LinkedHashMap<
    String, InteractionObject.ObjectTypeCase>,
  private val matcherDelegate: MatcherDelegate
) : RuleClassifier {
  override fun matches(answer: InteractionObject, inputs: Map<String, InteractionObject>): Boolean {
    check(answer.objectTypeCase == expectedAnswerObjectType) {
      "Expected answer to be of type ${expectedAnswerObjectType.name} " +
        "not ${answer.objectTypeCase.name}"
    }
    val parameterInputs =
      orderedExpectedParameterTypes.toList()
        .map { (parameterName, expectedObjectType) ->
          retrieveInputObject(parameterName, expectedObjectType, inputs)
        }
    return matcherDelegate.matches(answer, parameterInputs)
  }

  private fun retrieveInputObject(
    parameterName: String,
    expectedObjectType: InteractionObject.ObjectTypeCase,
    inputs: Map<String, InteractionObject>
  ): InteractionObject {
    val input = checkNotNull(inputs[parameterName]) {
      "Expected classifier inputs to contain parameter with name " +
        "'$parameterName' but had: ${inputs.keys}"
    }
    check(input.objectTypeCase == expectedObjectType) {
      "Expected input value to be of type ${expectedObjectType.name} " +
        "not ${input.objectTypeCase.name}"
    }
    return input
  }

  internal interface NoInputInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the expectations per the specification of this
     * classifier.
     */
    fun matches(answer: T): Boolean
  }

  internal interface SingleInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the single validated and extracted input parameter per
     * the specification of this classifier.
     */
    fun matches(answer: T, input: T): Boolean
  }

  internal interface MultiTypeSingleInputMatcher<AT, IT> {
    /**
     * Returns whether the validated and extracted answer matches the single validated and extracted input parameter per
     * the specification of this classifier.
     */
    fun matches(answer: AT, input: IT): Boolean
  }

  internal interface MultiTypeDoubleInputMatcher<AT, ITF, ITS> {
    /**
     * Returns whether the validated and extracted answer matches the two validated and extracted input parameters per
     * the specification of this classifier.
     */
    fun matches(answer: AT, firstInput: ITF, secondInput: ITS): Boolean
  }

  internal interface DoubleInputMatcher<T> {
    /**
     * Returns whether the validated and extracted answer matches the two validated and extracted input parameters per
     * the specification of this classifier.
     */
    fun matches(answer: T, firstInput: T, secondInput: T): Boolean
  }

  internal sealed class MatcherDelegate {

    abstract fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean

    internal class NoInputMatcherDelegate<T : Any>(
      private val matcher: NoInputInputMatcher<T>,
      private val extractObject: (InteractionObject) -> T
    ) : MatcherDelegate() {
      override fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean {
        check(inputs.isEmpty())
        return matcher.matches(extractObject(answer))
      }
    }

    internal class SingleInputMatcherDelegate<T : Any>(
      private val matcher: SingleInputMatcher<T>,
      private val extractObject: (InteractionObject) -> T
    ) : MatcherDelegate() {
      override fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean {
        check(inputs.size == 1)
        return matcher.matches(extractObject(answer), extractObject(inputs.first()))
      }
    }

    internal class MultiTypeSingleInputMatcherDelegate<AT : Any, IT : Any>(
      private val matcher: MultiTypeSingleInputMatcher<AT, IT>,
      private val extractAnswerObject: (InteractionObject) -> AT,
      private val extractInputObject: (InteractionObject) -> IT
    ) : MatcherDelegate() {
      override fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean {
        check(inputs.size == 1)
        return matcher.matches(extractAnswerObject(answer), extractInputObject(inputs.first()))
      }
    }

    internal class DoubleInputMatcherDelegate<T : Any>(
      private val matcher: DoubleInputMatcher<T>,
      private val extractObject: (InteractionObject) -> T
    ) : MatcherDelegate() {
      override fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean {
        check(inputs.size == 2)
        return matcher.matches(
          extractObject(answer),
          extractObject(inputs[0]),
          extractObject(inputs[1])
        )
      }
    }

    internal class MultiTypeDoubleInputMatcherDelegate<AT : Any, ITF : Any, ITS : Any>(
      private val matcher: MultiTypeDoubleInputMatcher<AT, ITF, ITS>,
      private val extractAnswerObject: (InteractionObject) -> AT,
      private val extractFirstParamObject: (InteractionObject) -> ITF,
      private val extractSecondParamObject: (InteractionObject) -> ITS
    ) : MatcherDelegate() {
      override fun matches(answer: InteractionObject, inputs: List<InteractionObject>): Boolean {
        check(inputs.size == 2)
        return matcher.matches(
          extractAnswerObject(answer),
          extractFirstParamObject(inputs[0]),
          extractSecondParamObject(inputs[1])
        )
      }
    }
  }

  /** Factory to create new [GenericRuleClassifier]s. */
  internal class Factory @Inject constructor(
    private val interactionObjectTypeExtractorRepository: InteractionObjectTypeExtractorRepository
  ) {
    /** Returns a new [GenericRuleClassifier] for an answer that is not matched to any input values. */
    inline fun <reified T : Any> createNoInputClassifier(
      expectedObjectType: InteractionObject.ObjectTypeCase,
      matcher: NoInputInputMatcher<T>
    ): GenericRuleClassifier {
      val objectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      return GenericRuleClassifier(
        expectedObjectType,
        LinkedHashMap(),
        MatcherDelegate.NoInputMatcherDelegate(matcher, objectExtractor)
      )
    }

    /**
     * Returns a new [GenericRuleClassifier] for a single input value with the same type as the answer being classified.
     */
    inline fun <reified T : Any> createSingleInputClassifier(
      expectedObjectType: InteractionObject.ObjectTypeCase,
      inputParameterName: String,
      matcher: SingleInputMatcher<T>
    ): GenericRuleClassifier {
      val objectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      return GenericRuleClassifier(
        expectedObjectType, linkedMapOf(inputParameterName to expectedObjectType),
        MatcherDelegate.SingleInputMatcherDelegate(matcher, objectExtractor)
      )
    }

    /**
     * Returns a new [GenericRuleClassifier] for a single input value that has a different type than the answer being
     * classified.
     */
    inline fun <reified AT : Any, reified IT : Any> createMultiTypeSingleInputClassifier(
      expectedAnswerObjectType: InteractionObject.ObjectTypeCase,
      expectedInputObjectType: InteractionObject.ObjectTypeCase,
      inputParameterName: String,
      matcher: MultiTypeSingleInputMatcher<AT, IT>
    ): GenericRuleClassifier {
      val answerObjectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<AT>(expectedAnswerObjectType)
      val inputObjectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<IT>(expectedInputObjectType)
      return GenericRuleClassifier(
        expectedAnswerObjectType, linkedMapOf(inputParameterName to expectedInputObjectType),
        MatcherDelegate.MultiTypeSingleInputMatcherDelegate(
          matcher,
          answerObjectExtractor,
          inputObjectExtractor
        )
      )
    }

    /**
     * Returns a new [GenericRuleClassifier] for two input values of different types, possibly also different from
     * the answer's type.
     */
    inline fun <reified AT : Any, reified ITF : Any, reified ITS : Any> createDoubleInputClassifier(
      expectedAnswerObjectType: InteractionObject.ObjectTypeCase,
      expectedObjectType1: InteractionObject.ObjectTypeCase,
      firstInputParameterName: String,
      expectedObjectType2: InteractionObject.ObjectTypeCase,
      secondInputParameterName: String,
      matcher: MultiTypeDoubleInputMatcher<AT, ITF, ITS>
    ): GenericRuleClassifier {
      val answerObjectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<AT>(expectedAnswerObjectType)
      val objectExtractorFirst =
        interactionObjectTypeExtractorRepository.getExtractor<ITF>(expectedObjectType1)
      val objectExtractorSecond =
        interactionObjectTypeExtractorRepository.getExtractor<ITS>(expectedObjectType2)
      val parameters: LinkedHashMap<String, InteractionObject.ObjectTypeCase> = linkedMapOf(
        firstInputParameterName to expectedObjectType1,
        secondInputParameterName to expectedObjectType2
      )
      return GenericRuleClassifier(
        expectedAnswerObjectType,
        parameters,
        MatcherDelegate.MultiTypeDoubleInputMatcherDelegate(
          matcher, answerObjectExtractor,
          objectExtractorFirst, objectExtractorSecond
        )
      )
    }

    /** Returns a new [GenericRuleClassifier] for two input values of the same type as the answer it classifies. */
    inline fun <reified T : Any> createDoubleInputClassifier(
      expectedObjectType: InteractionObject.ObjectTypeCase,
      firstInputParameterName: String,
      secondInputParameterName: String,
      matcher: DoubleInputMatcher<T>
    ): GenericRuleClassifier {
      val objectExtractor =
        interactionObjectTypeExtractorRepository.getExtractor<T>(expectedObjectType)
      val parameters = linkedMapOf(
        firstInputParameterName to expectedObjectType,
        secondInputParameterName to expectedObjectType
      )
      return GenericRuleClassifier(
        expectedObjectType,
        parameters,
        MatcherDelegate.DoubleInputMatcherDelegate(matcher, objectExtractor)
      )
    }
  }
}
