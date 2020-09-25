package org.oppia.android.util.threading

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/** A custom [ConcurrentHashMap] of K to [ConcurrentLinkedQueue] with thread-safe enqueue and dequeue methods. */
typealias ConcurrentQueueMap<K, V> = ConcurrentHashMap<K, ConcurrentLinkedQueue<V>>

/** Enqueues the specified value into the queue corresponding to the specified key. */
fun <K, V> ConcurrentQueueMap<K, V>.enqueue(key: K, value: V) {
  // Queue is guaranteed to be normalized at this point due to putIfAbsent being atomic. However, since this is not
  // synchronized with the map, it's possible for the queue to be removed from the map before making this addition.
  // Also, multiple threads accessing enqueue() at the same time can result in an arbitrary order of elements added to
  // the underlying queue.
  getQueue(key).add(value)
}

/**
 * Dequeues the specified value from the queue corresponding to the specified key, and returns whether it was
 * successful.
 */
fun <K, V> ConcurrentQueueMap<K, V>.dequeue(key: K, value: V): Boolean {
  // See warning in enqueue() for scenarios when this deletion may fail.
  return getQueue(key).remove(value)
}

/** Returns the [ConcurrentLinkedQueue] corresponding to the specified key in a thread-safe way. */
internal fun <K, V> ConcurrentQueueMap<K, V>.getQueue(key: K): ConcurrentLinkedQueue<V> {
  // NB: This is a pre-24 compatible alternative to computeIfAbsent. See: https://stackoverflow.com/a/40665232.
  val queue: ConcurrentLinkedQueue<V>? = get(key)
  if (queue == null) {
    val newQueue = ConcurrentLinkedQueue<V>()
    return putIfAbsent(key, newQueue) ?: newQueue
  }
  return queue
}
