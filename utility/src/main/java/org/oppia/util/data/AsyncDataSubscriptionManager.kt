package org.oppia.util.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.ConcurrentQueueMap
import org.oppia.util.threading.dequeue
import org.oppia.util.threading.enqueue
import org.oppia.util.threading.getQueue
import javax.inject.Inject
import javax.inject.Singleton

internal typealias ObserveAsyncChange = suspend () -> Unit

/**
 * A subscription manager for all [DataProvider]s. This should only be used outside of this package for notifying
 * changes to custom [DataProvider]s.
 */
@Singleton
class AsyncDataSubscriptionManager @Inject constructor(
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private val subscriptionMap = ConcurrentQueueMap<Any, ObserveAsyncChange>()
  private val associatedIds = ConcurrentQueueMap<Any, Any>()
  private val backgroundCoroutineScope = CoroutineScope(backgroundDispatcher)

  /** Subscribes the specified callback function to the specified [DataProvider] ID. */
  internal fun subscribe(id: Any, observeChange: ObserveAsyncChange) {
    subscriptionMap.enqueue(id, observeChange)
  }

  /** Unsubscribes the specified callback function from the specified [DataProvider] ID. */
  internal fun unsubscribe(id: Any, observeChange: ObserveAsyncChange): Boolean {
    // TODO(#91): Determine a way to safely fully remove the queue once it's empty. This may require a custom data
    //  structure or external locking for proper thread safety (e.g. to handle the case where multiple
    //  subscribes/notifies happen shortly after the queue is removed).
    return subscriptionMap.dequeue(id, observeChange)
  }

  /**
   * Creates an association such that change notifications via [notifyChange] to the parent ID will also notify
   * observers of the child ID.
   */
  internal fun associateIds(childId: Any, parentId: Any) {
    // TODO(#6): Ensure this graph is acyclic to avoid infinite recursion during notification. Compile-time deps should
    //  make this impossible in practice unless data provider users try to use the same key for multiple inter-dependent
    //  data providers.
    // TODO(#6): Find a way to determine parent-child ID associations during subscription time to avoid needing to store
    //  long-lived references to IDs prior to subscriptions.
    associatedIds.enqueue(parentId, childId)
  }

  /**
   * Removes the specified association between parent and child IDs to prevent notifications to the parent ID from also
   * notifying observers of the child ID.
   */
  internal fun dissociateIds(childId: Any, parentId: Any) {
    associatedIds.dequeue(parentId, childId)
  }

  /**
   * Notifies all subscribers of the specified [DataProvider] id that the provider has been changed and should be
   * re-queried for its latest state.
   */
  suspend fun notifyChange(id: Any) {
    // Notify subscribers.
    subscriptionMap.getQueue(id).forEach { observeChange -> observeChange() }

    // Also notify all children observing this parent.
    associatedIds.getQueue(id).forEach { childId -> notifyChange(childId) }
  }

  /**
   * Same as [notifyChange] except this may be called on the main thread since it will notify changes on a background
   * thread.
   */
  fun notifyChangeAsync(id: Any) {
    backgroundCoroutineScope.launch { notifyChange(id) }
  }
}
