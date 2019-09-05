package org.oppia.util.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
class AsyncDataSubscriptionManager @Inject constructor() {
  private val subscriptionMap = ConcurrentQueueMap<Any, ObserveAsyncChange>()
  private val associatedIds = ConcurrentQueueMap<Any, Any>()

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
   * Notifies all subscribers of the specified [DataProvider] id that the provider has been changed and should be
   * re-queried for its latest state.
   */
  @Suppress("DeferredResultUnused") // Exceptions on the main thread will cause app crashes. No action needed.
  suspend fun notifyChange(id: Any) {
    // Ensure observed changes are called specifically on the main thread since that's what NotifiableAsyncLiveData
    // expects.
    // TODO(#90): Update NotifiableAsyncLiveData so that observeChange() can occur on background threads to avoid any
    //  load on the UI thread until the final data value is ready for delivery.
    val scope = CoroutineScope(Dispatchers.Main)
    scope.async {
      subscriptionMap.getQueue(id).forEach { observeChange -> observeChange() }
    }

    // Also notify all children observing this parent.
    associatedIds.getQueue(id).forEach { childId -> notifyChange(childId) }
  }
}
