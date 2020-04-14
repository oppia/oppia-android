package org.oppia.domain.topic

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller for saving all image assets to internal storage for all topics.
 * This controller can be removed once entire topic download feature is in place.
 */
@Singleton
class TopicAssetController @Inject constructor() {

  fun copyAllAssetImagesToInternalStorage(context: Context) {
    copyDirOrFileFromAssetManager(
      "richtextimages",
      "oppia/exploration/$FRACTIONS_EXPLORATION_ID_0/assets/image",
      context
    )
  }

  @Throws(IOException::class)
  fun copyDirOrFileFromAssetManager(
    arg_assetDir: String,
    arg_destinationDir: String,
    context: Context
  ): String? {
    val sdCardPath = Environment.getExternalStorageDirectory()
    val destinationDirectoryPath = sdCardPath.toString() + addLeadingSlash(arg_destinationDir)
    val destinationDirectory = File(destinationDirectoryPath)
    createDir(destinationDirectory)
    val assetManager: AssetManager = context.assets
    val imageList = assetManager.list(arg_assetDir)
    for (i in imageList!!.indices) {
      val absoluteAssetFilePath = addTrailingSlash(arg_assetDir) + imageList[i]
      val subImageList = assetManager.list(absoluteAssetFilePath)
      if (subImageList!!.isEmpty()) {
        val destinationFilePath = addTrailingSlash(destinationDirectoryPath) + imageList[i]
        copyAssetFile(absoluteAssetFilePath, destinationFilePath, context)
      } else {
        copyDirOrFileFromAssetManager(
          absoluteAssetFilePath,
          addTrailingSlash(arg_destinationDir) + imageList[i], context
        )
      }
    }
    return destinationDirectoryPath
  }

  @Throws(IOException::class)
  private fun copyAssetFile(
    assetFilePath: String,
    destinationFilePath: String?,
    context: Context
  ) {
    val inputStream: InputStream = context.assets.open(assetFilePath)
    val outputStream: OutputStream = FileOutputStream(destinationFilePath)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
    inputStream.close()
    outputStream.close()
  }

  private fun addTrailingSlash(path: String): String {
    return if (path[path.length - 1] != '/') {
      "$path/"
    } else {
      path
    }
  }

  private fun addLeadingSlash(path: String): String {
    return if (path[0] != '/') {
      "/$path"
    } else {
      path
    }
  }

  @Throws(IOException::class)
  private fun createDir(directory: File) {
    if (directory.exists()) {
      if (!directory.isDirectory) {
        throw IOException("Can't create directory, a file is in the way")
      }
    } else {
      directory.mkdir()
      if (!directory.isDirectory) {
        throw IOException("Unable to create directory")
      }
    }
  }
}
