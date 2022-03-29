package org.oppia.android.domain.classify.rules

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * A convenience utility for mapping [ObjectTypeCase] constants to methods that can extract the corresponding value
 * from instances of [InteractionObject]. This utility is preferred over manually maintaining the mapping since it's a
 * single source of truth that allows code requiring an extraction method to rely only on the type enum rather than
 * managing the enum->method relationship directly.
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
@Singleton // Avoid recomputing the mapping multiple times.
class InteractionObjectTypeExtractorRepository @Inject constructor() {
  val extractors: Map<ObjectTypeCase, ExtractorMapping<*>> by lazy {
    computeExtractorMap()
  }

  /**
   * Returns a function that can be used to extract an element of type [T] from an [InteractionObject] where [T]
   * corresponds to the specified [ObjectTypeCase]. Note that referencing the wrong type will result in a runtime type
   * check failure.
   */
  inline fun <reified T> getExtractor(objectTypeCase: ObjectTypeCase): (InteractionObject) -> T {
    val (extractionType, genericExtractor) =
      checkNotNull(extractors[objectTypeCase]) {
        "No mapping found for interaction object type: ${objectTypeCase.name}. " +
          "Was it not yet registered?"
      }
    check(extractionType.java.isAssignableFrom(T::class.java)) {
      "Trying to retrieve incompatible extractor type: ${T::class.java} " +
        "expected: ${extractionType.java}"
    }
    // Note that a new conversion method is returned since it's not clear whether it's safe to simply cast the
    // extractor.
    return { interactionObject ->
      genericExtractor(interactionObject) as T // The runtime check above makes this cast safe.
    }
  }

  // TODO(#1580): Re-restrict access using Bazel visibilities
  data class ExtractorMapping<T : Any>(
    val extractionType: KClass<T>,
    val genericExtractor: (InteractionObject) -> T
  )

  private companion object {
    private fun computeExtractorMap(): Map<ObjectTypeCase, ExtractorMapping<*>> {
      return ObjectTypeCase.values().associate { it to computeExtractorMapping(it) }
    }

    private fun computeExtractorMapping(objectTypeCase: ObjectTypeCase): ExtractorMapping<*> {
      // Use a 'return' to ensure future type cases are added.
      return when (objectTypeCase) {
        ObjectTypeCase.NORMALIZED_STRING -> createMapping(InteractionObject::getNormalizedString)
        ObjectTypeCase.SIGNED_INT -> createMapping(InteractionObject::getSignedInt)
        ObjectTypeCase.NON_NEGATIVE_INT -> createMapping(InteractionObject::getNonNegativeInt)
        ObjectTypeCase.REAL -> createMapping(InteractionObject::getReal)
        ObjectTypeCase.BOOL_VALUE -> createMapping(InteractionObject::getBoolValue)
        ObjectTypeCase.NUMBER_WITH_UNITS -> createMapping(InteractionObject::getNumberWithUnits)
        ObjectTypeCase.SET_OF_HTML_STRING -> createMapping(InteractionObject::getSetOfHtmlString)
        ObjectTypeCase.FRACTION -> createMapping(InteractionObject::getFraction)
        ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING ->
          createMapping(InteractionObject::getListOfSetsOfHtmlString)
        ObjectTypeCase.IMAGE_WITH_REGIONS -> createMapping(InteractionObject::getImageWithRegions)
        ObjectTypeCase.CLICK_ON_IMAGE -> createMapping(InteractionObject::getClickOnImage)
        ObjectTypeCase.RATIO_EXPRESSION -> createMapping(InteractionObject::getRatioExpression)
        ObjectTypeCase.TRANSLATABLE_HTML_CONTENT_ID ->
          createMapping(InteractionObject::getTranslatableHtmlContentId)
        ObjectTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS ->
          createMapping(InteractionObject::getSetOfTranslatableHtmlContentIds)
        ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS ->
          createMapping(InteractionObject::getListOfSetsOfTranslatableHtmlContentIds)
        ObjectTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING ->
          createMapping(InteractionObject::getTranslatableSetOfNormalizedString)
        ObjectTypeCase.MATH_EXPRESSION -> createMapping(InteractionObject::getMathExpression)
        ObjectTypeCase.OBJECTTYPE_NOT_SET -> createMapping { error("Invalid object type") }
      }
    }

    private inline fun <reified T : Any> createMapping(
      noinline extractor: (InteractionObject) -> T
    ): ExtractorMapping<T> {
      return ExtractorMapping(T::class, extractor)
    }
  }
}
