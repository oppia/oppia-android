package org.oppia.android.util.enumfilter
// This function is used to filter the list of items based on the enum condition.
// This function takes the collection of items, enumExtractor function which extracts the enum from the item and condition function which checks the condition on the enum.
inline fun<E:Enum<E>,T>filterByEnumCondition(
collection:Collection<T>,
enumExtractor:(T)->E,
condition:(E)->Boolean
):List<T>{
  return collection.filter{condition(enumExtractor(it))}
}
