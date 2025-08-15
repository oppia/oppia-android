package org.oppia.android.util.extensions

/** Executes the given [action] for each element in this iterable, safely ignoring null elements. */
// TODO(#5734): Remove this function once lint no longer falsely triggers on Iterable#forEach.
inline fun <T> Iterable<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}
