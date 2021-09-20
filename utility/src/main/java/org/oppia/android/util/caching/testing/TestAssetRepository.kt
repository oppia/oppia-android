package org.oppia.android.util.caching.testing

import com.google.protobuf.MessageLite
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.caching.AssetRepository

/**
 * Test-only implementation of [AssetRepository]. This helper class provides support to fake local
 * assets for the purpose of testing specific filesystem scenarios.
 *
 * This class is not safe to interact with across multiple simultaneous threads.
 */
@Singleton
class TestAssetRepository @Inject constructor(): AssetRepository {
  private val urlFileMap = mutableMapOf<String, String>()
  private val localTextAssets = mutableMapOf<String, String>()
  private val remoteBinaryAssets = mutableMapOf<String, ByteArray>()
  private val localBinaryAssets = mutableMapOf<String, ByteArray>()

  /**
   * Adds a local text asset that can be loaded using [loadTextFileFromLocalAssets].
   *
   * Multiple local assets cannot be bound to the same asset name.
   *
   * @param assetFileName the name to associate with the asset contents
   * @param assetContents the file contents to bind to the specific name
   */
  fun addLocalTextAsset(assetFileName: String, assetContents: String) {
    check(assetFileName !in localTextAssets) { "Local text asset already exists: $assetFileName" }
    check(assetFileName !in localBinaryAssets) {
      "Asset already exists as a binary asset: $assetFileName"
    }
    localTextAssets[assetFileName] = assetContents
  }

  /**
   * Removes the local asset with the specified file name (as registered with [addLocalTextAsset].
   * This method fails if the specified asset is not registered.
   */
  fun removeLocalTextAsset(assetFileName: String) {
    check(assetFileName in localTextAssets) { "Local text asset has not been add: $assetFileName" }
    localTextAssets.remove(assetFileName)
  }

  /**
   * Adds a local binary asset that can be loaded using [loadImageAssetFromLocalAssets],
   * [loadProtoFromLocalAssets], and [tryLoadProtoFromLocalAssets].
   *
   * Multiple local assets cannot be bound to the same asset name.
   *
   * @param assetFileName the name to associate with the asset contents
   * @param assetContents the file contents to bind to the specific name
   */
  fun addLocalBinaryAsset(assetFileName: String, assetContents: ByteArray) {
    check(assetFileName !in localBinaryAssets) {
      "Local binary asset already exists: $assetFileName"
    }
    check(assetFileName !in localTextAssets) {
      "Asset already exists as a binary asset: $assetFileName"
    }
    localBinaryAssets[assetFileName] = assetContents
  }

  /**
   * Removes the local asset with the specified file name (as registered with [addLocalBinaryAsset].
   * This method fails if the specified asset is not registered.
   */
  fun removeLocalBinaryAsset(assetFileName: String) {
    check(assetFileName in localBinaryAssets) {
      "Local binary asset has not been add: $assetFileName"
    }
    localBinaryAssets.remove(assetFileName)
  }

  /**
   * Registers a binary asset similarly to [addLocalBinaryAsset] except the asset must first be
   * downloaded using [primeRemoteBinaryAsset] or [loadRemoteBinaryAsset].
   *
   * Undownloaded assets can be removed using [removeRemoteBinaryAsset].
   */
  fun addRemoteBinaryAsset(url: String, assetContents: ByteArray) {
    check(url !in urlFileMap) { "Remote asset URL already registered: $url" }
    val fileName = extractFileName(url)
    check(fileName !in remoteBinaryAssets) { "Remote asset already present: $fileName" }
    check(fileName !in localBinaryAssets) { "Remote asset already downloaded: $fileName" }
    urlFileMap[url] = fileName
    remoteBinaryAssets[fileName] = assetContents
  }

  /**
   * Removes the specified URL from the list of available assets to "download" (as registered with
   * [addRemoteBinaryAsset]). Note that this method will fail if the specified URL is not
   * registered, or if the asset was downloaded.
   */
  fun removeRemoteBinaryAsset(url: String) {
    check(url in urlFileMap) { "Remote asset URL not registered: $url" }
    val fileName = extractFileName(url)
    check(fileName !in localBinaryAssets) { "Remote asset was downloaded, can't remove: $fileName" }
    check(fileName in remoteBinaryAssets) { "Remote asset is not registered: $fileName" }
    urlFileMap.remove(url)
    remoteBinaryAssets.remove(fileName)
  }

  override fun loadTextFileFromLocalAssets(assetName: String): String =
    loadLocalTextAsset(assetName) ?: error("Text asset has not been add: $assetName.")

  override fun primeTextFileFromLocalAssets(assetName: String) {
    // Nothing to actually do for the test implementation, but verify that the file exists to find
    // potential production mistakes early.
    verifyLocalTextFileExists(assetName)
  }

  override fun <T : MessageLite> loadProtoFromLocalAssets(assetName: String, baseMessage: T): T {
    return loadLocalProtoAsset(assetName, baseMessage)
      ?: error("Proto asset has not been initialized: $assetName.")
  }

  override fun <T : MessageLite> tryLoadProtoFromLocalAssets(
    assetName: String,
    defaultMessage: T
  ): T = loadLocalProtoAsset(assetName, defaultMessage) ?: defaultMessage

  override fun getLocalAssetProtoSize(assetName: String): Int =
    loadLocalProtoAssetBlob(assetName)?.size ?: -1

  override fun loadRemoteBinaryAsset(url: String): () -> ByteArray {
    verifyUrlMapping(url)
    return {
      val filename = urlFileMap[url]
        ?: error("URL mapping removed prior to callback execution: $url")
      downloadRemoteBinaryAsset(filename)
        ?: error("Remote binary asset removed prior to callback execution: $filename")
    }
  }

  override fun loadImageAssetFromLocalAssets(url: String): () -> ByteArray {
    val filename = extractFileName(url)
    verifyLocalBinaryFileExists(filename)
    return {
      loadLocalBinaryAsset(filename)
        ?: error("Binary asset removed prior to callback execution: $filename")
    }
  }

  override fun primeRemoteBinaryAsset(url: String) {
    verifyUrlMapping(url)
    downloadRemoteBinaryAsset(urlFileMap.getValue(url)) ?: error("Failed to download asset: $url")
  }

  override fun isRemoteBinaryAssetDownloaded(url: String): Boolean =
    extractFileName(url) in localBinaryAssets

  private fun <T : MessageLite> loadLocalProtoAsset(assetName: String, baseMessage: T): T? {
    return loadLocalProtoAssetBlob(assetName)?.let { binaryBlob ->
      @Suppress("UNCHECKED_CAST") // Safe type-cast per newBuilderForType's contract.
      baseMessage.newBuilderForType().mergeFrom(binaryBlob).build() as? T
    }
  }

  private fun loadLocalProtoAssetBlob(assetName: String): ByteArray? =
    loadLocalBinaryAsset("$assetName.pb")

  private fun loadLocalTextAsset(assetFileName: String): String? = localTextAssets[assetFileName]

  private fun loadLocalBinaryAsset(assetFileName: String): ByteArray? =
    localBinaryAssets[assetFileName]

  private fun downloadRemoteBinaryAsset(assetFileName: String): ByteArray? {
    return remoteBinaryAssets.remove(assetFileName)?.let { binaryBlob ->
      addLocalBinaryAsset(assetFileName, binaryBlob)
      return@let loadLocalBinaryAsset(assetFileName)
    }
  }

  private fun verifyLocalBinaryFileExists(assetFileName: String) {
    check(assetFileName in localTextAssets) { "Local binary asset does not exist: $assetFileName" }
  }

  private fun verifyLocalTextFileExists(assetFileName: String) {
    check(assetFileName in localTextAssets) { "Local text asset does not exist: $assetFileName" }
  }

  private fun verifyUrlMapping(url: String) {
    check(url in urlFileMap) { "Error: URL not registered to an asset file: $url" }
    val fileName = urlFileMap.getValue(url)
    check(fileName in remoteBinaryAssets || fileName in localBinaryAssets) {
      "File corresponding to URL is not available locally or remotely: $url"
    }
  }

  private fun extractFileName(url: String): String = url.substringAfterLast('/')
}
