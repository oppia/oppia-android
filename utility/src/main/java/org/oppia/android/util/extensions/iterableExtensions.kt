package org.oppia.android.util.extensions

/**
 * Safe forEach extensions to replace direct forEach usage and avoid lint false positives on API < 24.
 * These extensions provide the same functionality as the standard forEach methods but use
 * traditional for loops internally to bypass lint issues.
 */
// TODO(#5734): Remove these function once lint no longer falsely triggers on forEach.

/** Executes the given [action] for each element in this iterable. */
inline fun <T> Iterable<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}
