package org.oppia.android.util.filesize

import android.content.Context
import org.oppia.android.util.R
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to get the size of the file. */
@Singleton
class FileSizeUtil @Inject constructor(private val context: Context) {

  fun calculateTopicSizeWithBytes(diskSizeBytes: Long?): String {
    return diskSizeBytes?.let {
      val sizeInBytes: Int = it.toInt()
      val sizeInKb = sizeInBytes / 1024
      val sizeInMb = sizeInKb / 1024
      val sizeInGb = sizeInMb / 1024
      return@let when {
        sizeInGb >= 1 -> context.getString(R.string.size_gb, roundUpToHundreds(sizeInGb))
        sizeInMb >= 1 -> context.getString(R.string.size_mb, roundUpToHundreds(sizeInMb))
        sizeInKb >= 1 -> context.getString(R.string.size_kb, roundUpToHundreds(sizeInKb))
        else -> context.getString(R.string.size_bytes, roundUpToHundreds(sizeInBytes))
      }
    } ?: context.getString(R.string.unknown_size)
  }

  private fun roundUpToHundreds(intValue: Int): Int {
    return ((intValue + 9) / 10) * 10
  }
}