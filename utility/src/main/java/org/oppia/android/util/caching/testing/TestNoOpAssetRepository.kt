package org.oppia.android.util.caching.testing

import com.google.protobuf.MessageLite
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test-only implementation of [AssetRepository] that provides no support for loading any assets
 * (i.e. it sets up the environment as though there are no local or remote assets available).
 *
 * This class is safe to interact with across multiple simultaneous threads.
 */
@Singleton
class TestNoOpAssetRepository @Inject constructor() : AssetRepository {
  override fun loadTextFileFromLocalAssets(assetName: String): String {
    error("Local text asset doesn't exist: $assetName")
  }

  override fun primeTextFileFromLocalAssets(assetName: String) {
    // Do nothing.
  }

  override fun <T : MessageLite> loadProtoFromLocalAssets(assetName: String, baseMessage: T): T {
    error("Local proto asset doesn't exist: $assetName")
  }

  override fun <T : MessageLite> tryLoadProtoFromLocalAssets(
    assetName: String,
    defaultMessage: T
  ): T = defaultMessage // Just return default since the asset doesn't exist.

  override fun <T : MessageLite> maybeLoadProtoFromLocalAssets(
    assetName: String,
    baseMessage: T
  ): T? = null // The asset doesn't exist.

  override fun getLocalAssetProtoSize(assetName: String): Int = -1 // Asset doesn't exist.

  override fun loadRemoteBinaryAsset(url: String): () -> ByteArray {
    error("Remote asset doesn't exist: $url")
  }

  override fun loadImageAssetFromLocalAssets(url: String): () -> ByteArray {
    error("Local image asset doesn't exist: $url")
  }

  override fun primeRemoteBinaryAsset(url: String) {
    // Do nothing.
  }

  override fun isRemoteBinaryAssetDownloaded(url: String): Boolean = false // No local assets exist.
}
