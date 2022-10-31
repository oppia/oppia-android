package org.oppia.android.scripts.emulator

import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.oppia.android.scripts.proto.DeviceHardwareProfile

data class AndroidVirtualDevice(
  val hardwareName: String,
  val diagonalSizeInches: Double,
  val resolutionWidthPx: Int,
  val resolutionHeightPx: Int,
  val aspectRatio: Pair<Int, Int>,
  val sdCardSizeMebibytes: Long,
  val displayDensityPpi: Int,
  val emulatorDensityPpi: Int,
  val densityQualifier: DensityQualifier
) {
  enum class DensityQualifier {
    LDPI,
    MDPI,
    HDPI,
    XHDPI,
    XXHDPI,
    XXXHDPI,
    NODPI
  }

  companion object {
    // The emulator only supports specific density types and will fail to start if not using one
    // of these.
    private val EMULATOR_EXPECTED_DENSITIES = listOf(
      120, 140, 160, 180, 213, 240, 280, 320, 340, 360, 400, 420, 440, 480, 560, 640
    )

    private const val DEFAULT_SD_CARD_SIZE_MEBIBYTES = 100L

    fun createFromHardwareProfile(
      hardwareProfile: DeviceHardwareProfile,
      sdCardSizeMebibytes: Long = DEFAULT_SD_CARD_SIZE_MEBIBYTES
    ): AndroidVirtualDevice {
      // Find the smallest expected density bucket that's larger or the same as the target density
      // (to ensure a close-to-correct fit).
      val ppiDensity = hardwareProfile.computeDensity()
      val forcedPpiDensity = EMULATOR_EXPECTED_DENSITIES.first { it >= ppiDensity }

      // See https://developer.android.com/training/multiscreen/screendensities#TaskProvideAltBmp
      // which provides a table of different density qualifiers.
      val densityQualifier = when {
        forcedPpiDensity <= 120 -> DensityQualifier.LDPI
        forcedPpiDensity <= 160 -> DensityQualifier.MDPI
        forcedPpiDensity <= 240 -> DensityQualifier.HDPI
        forcedPpiDensity <= 320 -> DensityQualifier.XHDPI
        forcedPpiDensity <= 480 -> DensityQualifier.XXHDPI
        forcedPpiDensity <= 640 -> DensityQualifier.XXXHDPI
        else -> DensityQualifier.NODPI
      }

      return AndroidVirtualDevice(
        hardwareName = hardwareProfile.name,
        diagonalSizeInches = hardwareProfile.diagonalSizeInches,
        resolutionWidthPx = hardwareProfile.resolutionWidthPx,
        resolutionHeightPx = hardwareProfile.resolutionHeightPx,
        aspectRatio = hardwareProfile.computeAspectRatio(),
        sdCardSizeMebibytes = sdCardSizeMebibytes,
        displayDensityPpi = ppiDensity,
        emulatorDensityPpi = forcedPpiDensity,
        densityQualifier = densityQualifier
      )
    }

    private fun DeviceHardwareProfile.computeDensity(): Int {
      val diagonalPx = computeDiagonal(resolutionWidthPx.toDouble(), resolutionHeightPx.toDouble())
      return (diagonalPx / diagonalSizeInches).roundToInt()
    }

    private fun DeviceHardwareProfile.computeAspectRatio(): Pair<Int, Int> {
      val commonDivisor = gcd(resolutionWidthPx, resolutionHeightPx)
      return (resolutionWidthPx / commonDivisor) to (resolutionHeightPx / commonDivisor)
    }

    private fun computeDiagonal(x: Double, y: Double) = sqrt(x * x + y * y)

    private fun gcd(x: Int, y: Int): Int = if (y == 0) x else gcd(y, x % y)
  }
}
