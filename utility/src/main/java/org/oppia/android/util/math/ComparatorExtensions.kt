package org.oppia.android.util.math

import com.google.protobuf.MessageLite

fun <U> Comparator<U>.compareIterables(first: Iterable<U>, second: Iterable<U>): Int {
  // Reference: https://stackoverflow.com/a/30107086.
  val firstIter = first.sortedWith(this).iterator()
  val secondIter = second.sortedWith(this).iterator()
  while (firstIter.hasNext() && secondIter.hasNext()) {
    val comparison = this.compare(firstIter.next(), secondIter.next())
    if (comparison != 0) return comparison // Found a different item.
  }

  // Everything is equal up to here, see if the lists are different length.
  return when {
    firstIter.hasNext() -> 1 // The first list is longer, therefore "greater."
    secondIter.hasNext() -> -1 // Ditto, but for the second list.
    else -> 0 // Otherwise, they're the same length with all equal items (and are thus equal).
  }
}

fun <T: MessageLite> Comparator<T>.compareProtos(left: T, right: T): Int {
  val defaultValue = left.defaultInstanceForType
  val leftIsDefault = left == defaultValue
  val rightIsDefault = right == defaultValue
  return when {
    leftIsDefault && rightIsDefault -> 0 // Both are default, therefore equal.
    leftIsDefault -> 1 // right > left since it's initialized.
    rightIsDefault -> 1 // left > right since it's initialized.
    else -> compare(left, right) // Both are initialized; perform a deep-comparison.
  }
}
