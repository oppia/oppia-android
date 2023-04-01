package org.oppia.android.util.caching.testing

import com.google.protobuf.MessageLite
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.AssetRepositoryImpl
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test-only fake version of [AssetRepository] that, by default, delegates to a production
 * implementation but supports some file overrides.
 *
 * Currently, only text and proto files are supported.
 */
@Singleton
class FakeAssetRepository @Inject constructor(
  private val prodImpl: AssetRepositoryImpl
) : AssetRepository {
  private val trackedAssets = ConcurrentHashMap<String, Any?>()

  override fun loadTextFileFromLocalAssets(assetName: String) = loadTextFile(assetName)

  override fun primeTextFileFromLocalAssets(assetName: String) {
    loadTextFile(assetName)
  }

  override fun <T : MessageLite> loadProtoFromLocalAssets(assetName: String, baseMessage: T): T =
    loadProtoFile(assetName, baseMessage) ?: error("Asset doesn't exist: $assetName")

  override fun <T : MessageLite> tryLoadProtoFromLocalAssets(
    assetName: String,
    defaultMessage: T
  ): T = loadProtoFile(assetName, defaultMessage) ?: defaultMessage

  override fun <T : MessageLite> maybeLoadProtoFromLocalAssets(
    assetName: String,
    baseMessage: T
  ): T? = loadProtoFile(assetName, baseMessage)

  override fun getLocalAssetProtoSize(assetName: String): Int =
    fetchLoadedProtoFile(assetName)?.serializedSize ?: prodImpl.getLocalAssetProtoSize(assetName)

  override fun loadRemoteBinaryAsset(url: String): () -> ByteArray =
    error("Loading remote binary files not supported (for URL: $url).")

  override fun loadImageAssetFromLocalAssets(url: String): () -> ByteArray =
    error("Loading remote images not supported (for URL: $url).")

  override fun primeRemoteBinaryAsset(url: String) =
    error("Remote binary file priming not supported (for URL: $url).")

  override fun isRemoteBinaryAssetDownloaded(url: String): Boolean = false

  /**
   * Sets the proto override for the specified asset given by its [assetName] (changing the return
   * value for [loadProtoFromLocalAssets] & other similar methods.
   *
   * This assumes the asset hasn't already been loaded, otherwise it fails (to avoid potential
   * inconsistencies that could arise by changing an asset partway through a test).
   */
  fun setProtoAssetOverride(assetName: String, proto: MessageLite) {
    check(trackedAssets.putIfAbsent(assetName, proto) == null) {
      "Asset has already been loaded and/or overridden: $assetName."
    }
  }

  private fun loadTextFile(assetName: String): String {
    return trackedAssets.computeIfAbsent(assetName) {
      prodImpl.loadTextFileFromLocalAssets(assetName)
    } as? String ?: error("Asset doesn't exist: $assetName")
  }

  private fun <T : MessageLite> loadProtoFile(assetName: String, defaultMessage: T): T? {
    return trackedAssets.computeIfAbsent(assetName) {
      prodImpl.maybeLoadProtoFromLocalAssets(assetName, defaultMessage)
    }?.let { protoAsset ->
      @Suppress("UNCHECKED_CAST") // This should fail if the cast doesn't fit.
      protoAsset as? T
    }
  }

  private fun fetchLoadedProtoFile(assetName: String) = trackedAssets[assetName] as? MessageLite
}
