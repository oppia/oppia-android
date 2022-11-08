package org.oppia.android.domain.exploration.testing

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.Exploration
import org.oppia.android.domain.exploration.ExplorationRetriever
import org.oppia.android.domain.exploration.ExplorationRetrieverImpl

@Singleton
class FakeExplorationRetriever @Inject constructor(
  private val productionImpl: ExplorationRetrieverImpl
): ExplorationRetriever {
  private val explorationProxies = ConcurrentHashMap<String, String>()

  override suspend fun loadExploration(explorationId: String): Exploration {
    val expIdToLoad = explorationProxies[explorationId] ?: explorationId
    return productionImpl.loadExploration(expIdToLoad)
  }

  fun setExplorationProxy(expIdToLoad: String, expIdToLoadInstead: String) {
    explorationProxies[expIdToLoad] = expIdToLoadInstead
  }

  fun clearExplorationProxy(expIdToLoad: String) {
    explorationProxies.remove(expIdToLoad)
  }
}
