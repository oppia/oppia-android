package org.oppia.android.util.enumfilter

/**
 * Filters a collection based on a condition applied to an enum property of each element.
 *
 * @param E The type of enum values.
 * @param T The type of elements in the collection.
 * @param collection The collection of elements to filter.
 * @param enumExtractor A function that extracts the enum value from each element.
 * @param condition A predicate function that determines if an enum value should be included in the result.
 * @return A list of elements from the collection that satisfy the condition when their enum property is evaluated.
 */

inline fun <E : Enum<E>, T> filterByEnumCondition(
  collection: Collection<T>,
  enumExtractor: (T) -> E,
  condition: (E) -> Boolean
): List<T> {
  return collection.filter { condition(enumExtractor(it)) }
}
