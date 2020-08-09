package org.oppia.domain.classify.rules

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.InteractionObject.ObjectTypeCase
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
      return mapOf(
        ObjectTypeCase.NORMALIZED_STRING to createMapping(
          InteractionObject::getNormalizedString
        ),
        ObjectTypeCase.SIGNED_INT to createMapping(
          InteractionObject::getSignedInt
        ),
        ObjectTypeCase.NON_NEGATIVE_INT to createMapping(
          InteractionObject::getNonNegativeInt
        ),
        ObjectTypeCase.REAL to createMapping(
          InteractionObject::getReal
        ),
        ObjectTypeCase.BOOL_VALUE to createMapping(
          InteractionObject::getBoolValue
        ),
        ObjectTypeCase.NUMBER_WITH_UNITS to createMapping(
          InteractionObject::getNumberWithUnits
        ),
        ObjectTypeCase.SET_OF_HTML_STRING to createMapping(
          InteractionObject::getSetOfHtmlString
        ),
        ObjectTypeCase.FRACTION to createMapping(
          InteractionObject::getFraction
        ),
        ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING to createMapping(
          InteractionObject::getListOfSetsOfHtmlString
        ),
        ObjectTypeCase.IMAGE_WITH_REGIONS to createMapping(
          InteractionObject::getImageWithRegions
        ),
        ObjectTypeCase.CLICK_ON_IMAGE to createMapping(
          InteractionObject::getClickOnImage
        )
      )
    }

    private inline fun <reified T : Any> createMapping(
      noinline extractor: (InteractionObject) -> T
    ): ExtractorMapping<T> {
      return ExtractorMapping(T::class, extractor)
    }
  }
}
