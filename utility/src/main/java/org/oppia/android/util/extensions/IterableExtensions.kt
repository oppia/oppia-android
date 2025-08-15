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

/** Executes the given [action] for each element in this array. */
inline fun <T> Array<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this sequence. */
inline fun <T> Sequence<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this list. */
inline fun <T> List<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this set. */
inline fun <T> Set<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this collection. */
inline fun <T> Collection<T>.safeForEach(action: (T) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each key-value pair in this map. */
inline fun <K, V> Map<K, V>.safeForEach(action: (Map.Entry<K, V>) -> Unit) {
  for (entry in this.entries) action(entry)
}

/** Executes the given [action] for each element in this byte array. */
inline fun ByteArray.safeForEach(action: (Byte) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this short array. */
inline fun ShortArray.safeForEach(action: (Short) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this int array. */
inline fun IntArray.safeForEach(action: (Int) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this long array. */
inline fun LongArray.safeForEach(action: (Long) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this float array. */
inline fun FloatArray.safeForEach(action: (Float) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this double array. */
inline fun DoubleArray.safeForEach(action: (Double) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this boolean array. */
inline fun BooleanArray.safeForEach(action: (Boolean) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each element in this char array. */
inline fun CharArray.safeForEach(action: (Char) -> Unit) {
  for (element in this) action(element)
}

/** Executes the given [action] for each menu item in this menu. */
inline fun android.view.Menu.safeForEach(action: (android.view.MenuItem) -> Unit) {
  for (i in 0 until size()) {
    action(getItem(i))
  }
}

/** Executes the given [action] for each child view in this view group. */
inline fun android.view.ViewGroup.safeForEach(action: (android.view.View) -> Unit) {
  for (i in 0 until childCount) {
    action(getChildAt(i))
  }
}

/** Executes the given [action] for each character in this string. */
inline fun String.safeForEach(action: (Char) -> Unit) {
  for (element in this) action(element)
}
