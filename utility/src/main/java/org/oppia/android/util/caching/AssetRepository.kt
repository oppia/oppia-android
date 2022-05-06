package org.oppia.android.util.caching

import com.google.protobuf.MessageLite

/**
 * A generic repository for accessing local APK asset files, and downloading remote binary files.
 * This repository aims to centralize caching management of external asset files to simplify
 * downstream code, and allow assets to be retrieved quickly and synchronously.
 *
 * Implementations of this class can be injected at the application scope and below.
 */
interface AssetRepository {
  /** Returns the whole text contents of the file corresponding to the specified asset name. */
  fun loadTextFileFromLocalAssets(assetName: String): String

  /**
   * Ensures the contents corresponding to the specified asset are available for quick retrieval.
   */
  fun primeTextFileFromLocalAssets(assetName: String)

  /**
   * Returns a new proto of type [T] that is retrieved from the local assets for the given asset
   * name. The [baseMessage] is used to load the proto; its value will never actually be used (so
   * callers are recommended to use [T]'s default instance for this purpose).
   */
  fun <T : MessageLite> loadProtoFromLocalAssets(assetName: String, baseMessage: T): T

  /**
   * A version of [loadProtoFromLocalAssets] which will return the specified default message if the
   * asset doesn't exist locally (rather than throwing an exception).
   */
  fun <T : MessageLite> tryLoadProtoFromLocalAssets(assetName: String, defaultMessage: T): T

  /**
   * A version of [loadProtoFromLocalAssets] that returns null if the specified asset doesn't exist.
   */
  fun <T : MessageLite> maybeLoadProtoFromLocalAssets(assetName: String, baseMessage: T): T?

  /** Returns the size of the specified proto asset, or -1 if the asset doesn't exist. */
  fun getLocalAssetProtoSize(assetName: String): Int

  /**
   * Returns a function to retrieve the stream of the binary asset corresponding to the specified
   * URL, to be called on a background thread.
   */
  fun loadRemoteBinaryAsset(url: String): () -> ByteArray

  /**
   * Returns a function to retrieve the image data corresponding to the specified URL (where the
   * image represented by that URL is assumed to be included in the app's assets directory).
   */
  fun loadImageAssetFromLocalAssets(url: String): () -> ByteArray

  /** Ensures the contents corresponding to the specified URL are available for quick retrieval. */
  fun primeRemoteBinaryAsset(url: String)

  /**
   * Returns whether a binary asset corresponding to the specified URL has already been downloaded.
   */
  fun isRemoteBinaryAssetDownloaded(url: String): Boolean
}
