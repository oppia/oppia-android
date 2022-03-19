package org.oppia.android.util.caching

import android.content.Context
import com.google.protobuf.MessageLite
import org.oppia.android.util.logging.ConsoleLogger
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

// TODO(#169): Leverage this repository or a version of it for caching all topic contents in a
//  proto. It may also be worth keeping a version of this repository for caching audio files within
//  certain size limits for buffering during an exploration.
/** Implementation of [AssetRepository]. */
@Singleton
class AssetRepositoryImpl @Inject constructor(
  private val context: Context,
  private val logger: ConsoleLogger
) : AssetRepository {
  private val repositoryLock = ReentrantLock()

  /** Map of asset names to file contents for text file assets. */
  private val textFileAssets = mutableMapOf<String, String>()

  /** Map of asset names to file contents for proto file assets. */
  private val protoFileAssets = mutableMapOf<String, ByteArray?>()

  override fun loadTextFileFromLocalAssets(assetName: String): String {
    repositoryLock.withLock {
      primeTextFileFromLocalAssets(assetName)
      return textFileAssets.getValue(assetName)
    }
  }

  override fun primeTextFileFromLocalAssets(assetName: String) {
    repositoryLock.withLock {
      if (assetName !in textFileAssets) {
        logger.d("AssetRepo", "Caching local text asset: $assetName")
        try {
          textFileAssets[assetName] = context.assets.open(assetName).bufferedReader().use {
            it.readText()
          }
        } catch (e: FileNotFoundException) {
          // Catch & rethrow for consistency with the proto asset codepath.
          error("Asset doesn't exist: $assetName")
        }
      }
    }
  }

  override fun <T : MessageLite> loadProtoFromLocalAssets(assetName: String, baseMessage: T): T {
    return maybeLoadProtoFromLocalAssets(assetName, baseMessage)
      ?: error("Asset doesn't exist: $assetName")
  }

  override fun <T : MessageLite> tryLoadProtoFromLocalAssets(
    assetName: String,
    defaultMessage: T
  ): T {
    return maybeLoadProtoFromLocalAssets(assetName, defaultMessage) ?: defaultMessage
  }

  override fun <T : MessageLite> maybeLoadProtoFromLocalAssets(
    assetName: String,
    baseMessage: T
  ): T? {
    return loadProtoBlobFromLocalAssets(assetName)?.let { serializedProto ->
      @Suppress("UNCHECKED_CAST") // Safe type-cast per newBuilderForType's contract.
      return baseMessage.newBuilderForType()
        .mergeFrom(serializedProto)
        .build() as T
    }
  }

  override fun getLocalAssetProtoSize(assetName: String): Int {
    return loadProtoBlobFromLocalAssets(assetName)?.size ?: -1
  }

  private fun loadProtoBlobFromLocalAssets(assetName: String): ByteArray? {
    primeProtoBlobFromLocalAssets(assetName)
    return protoFileAssets.getValue(assetName)
  }

  private fun primeProtoBlobFromLocalAssets(assetName: String) {
    repositoryLock.withLock {
      if (assetName !in protoFileAssets) {
        val files = context.assets.list(/* path= */ "")?.toList() ?: listOf()
        val assetNameFile = "$assetName.pb"
        protoFileAssets[assetName] = if (assetNameFile in files) {
          context.assets.open(assetNameFile).use { it.readBytes() }
        } else null
      }
    }
  }

  override fun loadRemoteBinaryAsset(url: String): () -> ByteArray {
    return {
      logger.d("AssetRepo", "Loading binary asset: $url")
      val stream = openLocalCacheFileForRead(url) ?: openCachingStreamToRemoteFile(url)
      stream.use { it.readBytes() }
    }
  }

  override fun loadImageAssetFromLocalAssets(url: String): () -> ByteArray {
    return {
      val filename = url.substringAfterLast('/')
      context.assets.open("images/$filename").use { it.readBytes() }
    }
  }

  override fun primeRemoteBinaryAsset(url: String) {
    if (!isRemoteBinaryAssetDownloaded(url)) {
      // Otherwise, download it remotely and cache it locally.
      logger.d("AssetRepo", "Downloading binary asset: $url")
      val contents = openRemoteStream(url).use { it.readBytes() }
      saveLocalCacheFile(url, contents)
    }
  }

  override fun isRemoteBinaryAssetDownloaded(url: String): Boolean {
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
