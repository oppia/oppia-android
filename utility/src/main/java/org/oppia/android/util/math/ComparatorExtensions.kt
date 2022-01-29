package org.oppia.android.util.math

import com.google.protobuf.MessageLite

/**
 * Compares two [Iterable]s based on an item [Comparator] and returns the result.
 *
 * The two [Iterable]s are iterated in an order determined by [Comparator], and then compared
 * element-by-element. If any element is different, the difference of that item becomes the overall
 * difference. If the lists are different sizes (but otherwise match up to the boundary of one
 * list), then the longer list will be considered "greater".
 *
 * This means that two [Iterable]s are only equal if they have the same number of elements, and that
 * all of their items are equal per this [Comparator], including duplicates.
 */
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

/**
 * Compares two protos of type [T] ([left] and [right]) using this [Comparator] and returns the
 * result.
 *
 * This adds behavior above the standard ``compare`` function by short-circuiting if either proto is
 * equal to the default instance (in which case "defined" is always considered larger, and this
 * [Comparator] isn't used). This short-circuiting behavior can be useful when comparing recursively
 * infinite proto structures to avoid stack overflows..
 */
fun <T : MessageLite> Comparator<T>.compareProtos(left: T, right: T): Int {
  val defaultValue = left.defaultInstanceForType
  val leftIsDefault = left == defaultValue
  val rightIsDefault = right == defaultValue
  return when {
    leftIsDefault && rightIsDefault -> 0 // Both are default, therefore equal.
    leftIsDefault -> -1 // right > left since it's initialized.
    rightIsDefault -> 1 // left > right since it's initialized.
    else -> compare(left, right) // Both are initialized; perform a deep-comparison.
  }
}
