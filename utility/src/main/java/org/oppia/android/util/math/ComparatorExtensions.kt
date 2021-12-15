package org.oppia.android.util.math

import java.util.SortedSet

fun <T, U> comparingDeferred(
  keySelector: (T) -> U,
  comparatorSelector: () -> Comparator<U>
): Comparator<T> {
  // Store as captured val for memoization.
  val comparator by lazy { comparatorSelector() }
  return Comparator.comparing(keySelector) { o1, o2 ->
    comparator.compare(o1, o2)
  }
}

fun <T, U : Comparable<U>> Comparator<T>.thenComparingReversed(
  keySelector: (T) -> U
): Comparator<T> = thenComparing(Comparator.comparing(keySelector).reversed())

fun <T, E : Enum<E>> Comparator<T>.thenSelectAmong(
  enumSelector: (T) -> E,
  vararg comparators: Pair<E, Comparator<T>>
): Comparator<T> {
  val comparatorMap = comparators.toMap()
  return thenComparing(
    Comparator { o1, o2 ->
      val enum1 = enumSelector(o1)
      val enum2 = enumSelector(o2)
      check(enum1 == enum2) {
        "Expected objects to have the same enum values: $o1 ($enum1), $o2 ($enum2)"
      }
      val comparator =
        checkNotNull(comparatorMap[enum1]) { "No comparator for matched enum: $enum1" }
      return@Comparator comparator.compare(o1, o2)
    }
  )
}

fun <T> Comparator<T>.toSetComparator(): Comparator<SortedSet<T>> {
  val itemComparator = this
  return Comparator { first, second ->
    // Reference: https://stackoverflow.com/a/30107086.
    val firstIter = first.iterator()
    val secondIter = second.iterator()
    while (firstIter.hasNext() && secondIter.hasNext()) {
      val comparison = itemComparator.compare(firstIter.next(), secondIter.next())
      if (comparison != 0) return@Comparator comparison // Found a different item.
    }

    // Everything is equal up to here, see if the lists are different length.
    return@Comparator when {
      firstIter.hasNext() -> 1 // The first list is longer, therefore "greater."
      secondIter.hasNext() -> -1 // Ditto, but for the second list.
      else -> 0 // Otherwise, they're the same length with all equal items (and are thus equal).
    }
  }
}
