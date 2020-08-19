package org.oppia.util.caching

import android.content.Context
import org.oppia.util.logging.ConsoleLogger
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

// TODO(#169): Leverage this repository or a version of it for caching all topic contents in a proto. It may also be
//  worth keeping a version of this repository for caching audio files within certain size limits for buffering during
//  an exploration.
/**
 * A generic repository for access local APK asset files, and downloading remote binary files. This repository aims to
 * centralize caching management of external asset files to simplify downstream code, and allow assets to be retrieved
 * quickly and synchronously.
 */
@Singleton
class AssetRepository @Inject constructor(
  private val context: Context,
  private val logger: ConsoleLogger
) {
  private val repositoryLock = ReentrantLock()

  /** Map of asset names to file contents for text file assets. */
  private val textFileAssets = mutableMapOf<String, String>()

  /** Returns the whole text contents of the file corresponding to the specified asset name. */
  fun loadTextFileFromLocalAssets(assetName: String): String {
    repositoryLock.withLock {
      primeTextFileFromLocalAssets(assetName)
      return textFileAssets.getValue(assetName)
    }
  }

  /** Ensures the contents corresponding to the specified asset are available for quick retrieval. */
  fun primeTextFileFromLocalAssets(assetName: String) {
    repositoryLock.withLock {
      if (assetName !in textFileAssets) {
        logger.d("AssetRepo", "Caching local text asset: $assetName")
        textFileAssets[assetName] = context.assets.open(assetName).bufferedReader().use {
          it.readText()
        }
      }
    }
  }

  /**
   * Returns a function to retrieve the stream of the binary asset corresponding to the specified URL, to be called on a
   * background thread.
   */
  fun loadRemoteBinaryAsset(url: String): () -> ByteArray {
    return {
      val stream = openLocalCacheFileForRead(url) ?: openCachingStreamToRemoteFile(url)
      stream.use { it.readBytes() }
    }
  }

  /** Ensures the contents corresponding to the specified URL are available for quick retrieval. */
  fun primeRemoteBinaryAsset(url: String) {
    if (!isRemoteBinarAssetDownloaded(url)) {
      // Otherwise, download it remotely and cache it locally.
      logger.d("AssetRepo", "Downloading binary asset: $url")
      val contents = openRemoteStream(url).use { it.readBytes() }
      saveLocalCacheFile(url, contents)
    }
  }

  /**
   * Returns whether a binary asset corresponding to the specified URL has already been downloaded.
   */
  fun isRemoteBinarAssetDownloaded(url: String): Boolean {
    return getLocalCacheFile(url).exists()
  }

  private fun openRemoteStream(url: String): InputStream {
    return URL(url).openStream()
  }

  /** Returns an [InputStream] that also saves its results to a local file. */
  private fun openCachingStreamToRemoteFile(url: String): InputStream {
    val urlInStream = openRemoteStream(url)
    val fileOutStream = openLocalCacheFileForWrite(url)
    return object : InputStream() {
      override fun available(): Int {
        return urlInStream.available()
      }

      override fun read(): Int {
        val byte = urlInStream.read()
        if (byte != -1) {
          fileOutStream.write(byte)
        }
        return byte
      }

      override fun read(b: ByteArray?): Int {
        return read(b, 0, b!!.size)
      }

      override fun read(b: ByteArray?, off: Int, len: Int): Int {
        val count = urlInStream.read(b, off, len)
        if (count > -1) {
          fileOutStream.write(b, off, count)
        }
        return count
      }

      override fun close() {
        super.close()
        fileOutStream.flush()
        fileOutStream.close()
        urlInStream.close()
      }
    }
  }

  private fun openLocalCacheFileForRead(identifier: String): InputStream? {
    val cacheFile = getLocalCacheFile(identifier)
    return if (cacheFile.exists()) cacheFile.inputStream() else null
  }

  private fun saveLocalCacheFile(identifier: String, contents: ByteArray) {
    getLocalCacheFile(identifier).writeBytes(contents)
  }

  private fun openLocalCacheFileForWrite(identifier: String): OutputStream {
    return getLocalCacheFile(identifier).outputStream()
  }

  private fun getLocalCacheFile(identifier: String): File {
    return File(context.cacheDir, convertIdentifierToCacheFileName(identifier))
  }

  private fun convertIdentifierToCacheFileName(identifier: String): String {
    return "${identifier.hashCode()}.cache"
  }
}
