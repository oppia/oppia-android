package org.oppia.android.util.extensions

/**
 * Iterates over the list safely by creating a copy of the list before iteration.
 * This is useful to avoid ConcurrentModificationException if the list is modified during iteration.
 */
fun <T> List<T>.safeForEach(action: (T) -> Unit) {
  ArrayList(this).forEach(action)
}
