package org.oppia.android.app.player.state

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.oppia.android.R

/** A sealed class defining the standard configurations for various confetti animations */
sealed class ConfettiConfig() {
  internal abstract val context: Context
  internal abstract val confettiView: KonfettiView
  internal abstract val minSpeed: Float
  internal abstract val maxSpeed: Float
  internal abstract val sizeInDp: Size

  // Confetti pieces with mass make the animation more active and dynamic.
  internal abstract val sizeWithMass: Size
  internal abstract val numPieces: Int
  internal abstract val shapes: Array<Shape>

  /** A configuration for large bursts of confetti. */
  class LargeConfettiBurst(
    override val context: Context,
    override val confettiView: KonfettiView
  ) : ConfettiConfig() {
    override val minSpeed = 5f
    override val maxSpeed = 12f
    override val sizeInDp = Size(sizeInDp = 12)
    override val sizeWithMass = Size(sizeInDp = 11, mass = 3f)
    override val numPieces = 60
    override val shapes = arrayOf(Shape.Circle, Shape.Square)
  }

  /** A configuration for medium bursts of confetti. */
  class MediumConfettiBurst(
    override val context: Context,
    override val confettiView: KonfettiView
  ) : ConfettiConfig() {
    override val minSpeed = 4f
    override val maxSpeed = 9f
    override val sizeInDp = Size(sizeInDp = 8)
    override val sizeWithMass = Size(sizeInDp = 7, mass = 3f)
    override val numPieces = 35
    override val shapes = arrayOf(Shape.Circle, Shape.Square)
  }

  /** A configuration for mini bursts of confetti. */
  class MiniConfettiBurst(
    override val context: Context,
    override val confettiView: KonfettiView
  ) : ConfettiConfig() {
    override val minSpeed = 2f
    override val maxSpeed = 4f
    override val sizeInDp = Size(sizeInDp = 8)
    override val sizeWithMass = Size(sizeInDp = 7, mass = 3f)
    override val numPieces = 7
    override val shapes: Array<Shape> = arrayOf(Shape.Circle)
  }

  companion object {
    /** Primary colors to use for the confetti. */
    val primaryColors: List<Int> = listOf(
      R.color.confetti_red,
      R.color.confetti_yellow,
      R.color.confetti_blue
    )

    fun startConfettiBurst(
      config: ConfettiConfig,
      xPosition: Float,
      yPosition: Float,
      timeToLiveMs: Long,
      delayMs: Long,
      minAngle: Double,
      maxAngle: Double
    ) {
      val confettiView = config.confettiView
      val colorsList = primaryColors.map { getColor(config.context, it) }
      confettiView.build()
        .setDelay(delayMs)
        .setFadeOutEnabled(true)
        .addColors(colorsList)
        .setDirection(minAngle, maxAngle)
        .setSpeed(config.minSpeed, config.maxSpeed)
        .setTimeToLive(timeToLiveMs)
        .addShapes(*config.shapes)
        .addSizes(config.sizeInDp, config.sizeWithMass)
        .setPosition(xPosition, yPosition)
        .burst(config.numPieces)
    }
  }
}
