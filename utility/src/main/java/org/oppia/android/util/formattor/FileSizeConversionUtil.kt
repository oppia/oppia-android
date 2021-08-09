package org.oppia.android.util.formattor

import android.content.Context
import org.oppia.android.util.R
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to calculate file size. */
@Singleton
class FileSizeConversionUtil @Inject constructor(
  private val context: Context
) {
  /**
   * Format the size in Kb, Mb, Gb using Bytes.
   *
   * @param [sizeInBytes]: Size in bytes.
   *
   * @return a [String] indicating the file size in Bytes, Kb, Mb or Gb.
   */
  fun formatSizeUnits(sizeInBytes: Int): String {
    val sizeInKb = sizeInBytes / 1024
    val sizeInMb = sizeInKb / 1024
    val sizeInGb = sizeInMb / 1024
    return when {
      sizeInGb >= 1 -> context.getString(R.string.size_gb, roundUpToHundreds(sizeInGb))
      sizeInMb >= 1 -> context.getString(R.string.size_mb, roundUpToHundreds(sizeInMb))
      sizeInKb >= 1 -> context.getString(R.string.size_kb, roundUpToHundreds(sizeInKb))
      else -> context.getString(R.string.size_bytes, roundUpToHundreds(sizeInBytes))
    }
  }

  private fun roundUpToHundreds(intValue: Int): Int {
    return ((intValue + 9) / 10) * 10
  }
}
