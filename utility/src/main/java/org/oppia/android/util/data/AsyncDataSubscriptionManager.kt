package org.oppia.android.util.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.android.util.threading.BackgroundDispatcher
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

typealias ObserveAsyncChange = suspend (Set<Any>) -> Unit

/**
 * A subscription manager for all [DataProvider]s. This should only be used outside of this package
 * for notifying changes to custom [DataProvider]s.
 */
@Singleton
class AsyncDataSubscriptionManager @Inject constructor(
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private val subscriptionLock = ReentrantLock()
  private val subscriptionMap = mutableMapOf<Any, MutableSet<ObserveAsyncChange>>()
  private val associatedIds = mutableMapOf<Any, MutableSet<Any>>()

  /**
   * Subscribes the specified callback function to the specified [DataProvider] ID.
   *
   * Note that the immediate parents that have either directly or indirectly changed will be passed
   * to [observeChange] whenever the provider corresponding to [id] may need an update.
   */
  fun subscribe(id: Any, observeChange: ObserveAsyncChange) {
    subscriptionLock.withLock {
      subscriptionMap.getOrPut(id) { mutableSetOf() } += observeChange
    }
  }

  /** Unsubscribes the specified callback function from the specified [DataProvider] ID. */
  fun unsubscribe(id: Any, observeChange: ObserveAsyncChange): Boolean {
    return subscriptionLock.withLock { subscriptionMap[id]?.remove(observeChange) } ?: false
  }

  /**
   * Creates an association such that change notifications via [notifyChange] to the parent ID will
   * also notify observers of the child ID.
   *
   * Duplicate subscriptions are ignored.
   */
  fun associateIds(childId: Any, parentId: Any) {
    // TODO(#3625): Find a way to determine parent-child ID associations during subscription time to
    //  avoid needing to store long-lived references to IDs prior to subscriptions.
    subscriptionLock.withLock {
      // First, verify that child isn't already subscribed.
      val isSubscribed = associatedIds[parentId]?.let { childId in it } ?: false

      if (!isSubscribed) {
        // Second, verify that there are no cycles.
        verifyNoCyclesInClosure(parentId, childId)?.let { failingId ->
          val parentSubscriptionTree = computeSubscriptionTreeString(parentId)
          val childSubscriptionTree = computeSubscriptionTreeString(childId)
          throw IllegalStateException(
            "Encountered cycle when trying to add '$childId' to '$parentId' (at " +
              "'$failingId'). Subscription trees:\n$parentSubscriptionTree\n\n" +
              childSubscriptionTree
          )
        }

        // Next, actually add the child.
        associatedIds.getOrPut(parentId) { mutableSetOf() } += childId
      }
    }
  }

  /**
   * Removes the specified association between parent and child IDs to prevent notifications to the
   * parent ID from also notifying observers of the child ID.
   */
  fun dissociateIds(childId: Any, parentId: Any) {
    subscriptionLock.withLock { associatedIds[parentId]?.remove(childId) }
  }

  /**
   * Notifies all subscribers of the specified [DataProvider] id that the provider has been changed
   * and should be re-queried for its latest state.
   */
  suspend fun notifyChange(id: Any) {
    // First, retrieve subscribers & the closure of child IDs to notify.
    val subscriptions = subscriptionLock.withLock { computeSubscriptionClosure(id) }

    // Notify all subscribers (both directly for this parent & all child IDs).
    subscriptions.forEach { (affectedParentIds, observeChange) -> observeChange(affectedParentIds) }
  }

  /**
   * Same as [notifyChange] except this may be called on the main thread since it will notify
   * changes on a background thread.
   */
  fun notifyChangeAsync(id: Any) {
    CoroutineScope(backgroundDispatcher).launch { notifyChange(id) }
  }

  /**
   * Verifies that a new association between [parentId] and [newChildId] will not result in a cycle
   * when computing the parent's ID closure (i.e. with [computeNotificationClosure]).
   *
   * This should only be called within a lock to [subscriptionLock].
   *
   * @return the ID that first demonstrates a cycle, or null if there are no cycles when trying to
   *     add the new child ID
   */
  private fun verifyNoCyclesInClosure(parentId: Any, newChildId: Any): Any? {
    val currentChildren = associatedIds[parentId] ?: listOf()
    return verifyNoCyclesInClosureAux(
      parentId, mutableSetOf(), mutableSetOf(), currentChildren + newChildId
    )
  }

  private fun verifyNoCyclesInClosureAux(
    nextId: Any,
    currentBranch: MutableSet<Any>,
    visitedIds: MutableSet<Any>,
    children: Iterable<Any>
  ): Any? {
    if (nextId in currentBranch) {
      // This ID has already been encountered in the current branch, so a cycle must exist.
      return nextId
    }
    currentBranch += nextId

    if (nextId in visitedIds) {
      // This ID has already been traversed (& determined to not have a cycle).
      return null
    }
    visitedIds += nextId

    // Check this ID's children for cycles.
    for (childId in children) {
      val newChildIds = associatedIds[childId] ?: listOf()
      val failure = verifyNoCyclesInClosureAux(childId, currentBranch, visitedIds, newChildIds)
      if (failure != null) {
        return failure
      }
    }

    // Remove the ID from the current branch since it's been fully explored.
    currentBranch -= nextId

    return null
  }

  /**
   * Returns the closure of subscriptions for the specified [parentId]. Note that the associations
   * follow the behavior of [computeNotificationClosure], and direct subscriptions to this parent
   * are returned first in the set. No guarantee of order exists for indirect subscriptions, though
   * subscriptions "closer" to direct subscriptions are attempted to be resolved first.
   *
   * This should only be called within a lock to [subscriptionLock].
   */
  private fun computeSubscriptionClosure(parentId: Any): Set<TrackableObserveAsyncChange> {
    val subscriptions = mutableSetOf<CombinedId>()

    // Represent direct subscriptions (which have no affected parent in this case).
    subscriptions += CombinedId(parentId = parentId, childId = parentId)

    // Add indirect subscriptions.
    subscriptions += computeNotificationClosure(parentId)

    // The 'group by' here ensures that children which can be reached multiple ways are only
    // notified once, but that all of their affected parents are included in the notification.
    // Finally, collect all changes into a single set (maintaining the general order of the
    // subscriptions from earliest to latest).
    return subscriptions.groupBy { it.childId }.mapValues { (childId, combinedIds) ->
      val parentIds = combinedIds.map { it.parentId }.toSet()
      subscriptionMap[childId]?.let { directSubs ->
        directSubs.map { TrackableObserveAsyncChange(parentIds, it) }
      } ?: listOf()
    }.flatMap(Map.Entry<Any, List<TrackableObserveAsyncChange>>::value).toSet()
  }

  /**
   * Returns the closure of all IDs that need to be notified in association with the specified
   * parent ID. Note that this follows all parent-child pathways to form a complete closure of IDs
   * to notify.
   *
   * This should only be called within a lock to [subscriptionLock].
   */
  private fun computeNotificationClosure(parentId: Any): Set<CombinedId> {
    return mutableSetOf<CombinedId>().also { idsToNotify ->
      computeNotificationClosureAux(parentId, idsToNotify)
    }
  }

  private fun computeNotificationClosureAux(
    nextParentId: Any, idsToNotify: MutableSet<CombinedId>
  ) {
    associatedIds[nextParentId]?.let { childIds ->
      idsToNotify += childIds.map { CombinedId(nextParentId, it) }
      childIds.forEach { childId -> computeNotificationClosureAux(childId, idsToNotify) }
    }
  }

  /**
   * Returns a human-readable, multi-line string representation of the subscription tree
   * corresponding to the specified parent.
   *
   * This should only be called within a lock to [subscriptionLock].
   */
  private fun computeSubscriptionTreeString(parentId: Any): String =
    StringBuilder().computeSubscriptionTreeStringAux(parentId, indent = 0).toString()

  private fun StringBuilder.computeSubscriptionTreeStringAux(
    parentId: Any,
    indent: Int
  ): StringBuilder {
    appendSpacing(indent).append(parentId)
    associatedIds[parentId]?.let { childIds ->
      append(" ->")
      childIds.forEach { childId ->
        appendLine().computeSubscriptionTreeStringAux(childId, indent + 2)
      }
    }
    return this
  }

  private fun StringBuilder.appendSpacing(indent: Int): StringBuilder = append(" ".repeat(indent))

  private data class CombinedId(val parentId: Any, val childId: Any)

  private data class TrackableObserveAsyncChange(
    val originParentIds: Set<Any>, val observeAsyncChange: ObserveAsyncChange
  )
}
