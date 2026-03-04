package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

// TODO(#59): Make this class inaccessible outside of the domain package except for tests. UI code should not be allowed
//  to depend on this utility.

/** Implementation of [ExplorationRetriever] that loads explorations from the app's assets. */
// TODO(#1580): Re-restrict access using Bazel visibilities
class ExplorationRetrieverImpl @Inject constructor(
  private val assetRepository: AssetRepository,
) : ExplorationRetriever {
  override suspend fun loadExploration(explorationId: String): Exploration {
    return assetRepository.loadProtoFromLocalAssets(explorationId, Exploration.getDefaultInstance())
  }
}
