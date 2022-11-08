package org.oppia.android.domain.exploration.testing

import org.oppia.android.app.model.Exploration
import org.oppia.android.domain.exploration.ExplorationRetriever
import org.oppia.android.domain.exploration.ExplorationRetrieverImpl
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test-specific implementation of [ExplorationRetriever] that, by default, delegates functionality
 * to a real [ExplorationRetriever] implementation but supports selective overriding.
 */
@Singleton
class FakeExplorationRetriever @Inject constructor(
  private val productionImpl: ExplorationRetrieverImpl
) : ExplorationRetriever {
  private val explorationProxies = ConcurrentHashMap<String, String>()

  override suspend fun loadExploration(explorationId: String): Exploration {
    val expIdToLoad = explorationProxies[explorationId] ?: explorationId
    return productionImpl.loadExploration(expIdToLoad)
  }

  /**
   * Sets the exploration ID that should be loaded in place of [expIdToLoad] on all future calls to
   * [loadExploration].
   *
   * Additional calls to this function will overwrite any previous bindings set with [expIdToLoad].
   * The set proxy can be cleared via a call to [clearExplorationProxy].
   */
  fun setExplorationProxy(expIdToLoad: String, expIdToLoadInstead: String) {
    explorationProxies[expIdToLoad] = expIdToLoadInstead
  }

  /**
   * Clears the proxy corresponding to [expIdToLoad] that has been set in [setExplorationProxy],
   * returning [loadExploration]'s behavior to normal for this exploration.
   *
   * This function does nothing if there's no such binding for the specified exploration ID.
   */
  fun clearExplorationProxy(expIdToLoad: String) {
    explorationProxies.remove(expIdToLoad)
  }
}
