package org.oppia.android.util.enumfilter

/**
 * Filters a collection based on a condition applied to an enum property of each element.
 *
 * @param E the type of enum values.
 * @param T the type of elements in the collection.
 * @param collection the collection of elements to filter.
 * @param enumExtractor a function that extracts the enum value from each element.
 * @param condition a predicate function that determines if an enum value should be included in the result.
 * @return a list of elements from the collection that satisfy the condition when their enum property is evaluated.
 */

inline fun <E : Enum<E>, T> filterByEnumCondition(
  collection: Collection<T>,
  enumExtractor: (T) -> E,
  condition: (E) -> Boolean
): List<T> {
  return collection.filter { condition(enumExtractor(it)) }
}
