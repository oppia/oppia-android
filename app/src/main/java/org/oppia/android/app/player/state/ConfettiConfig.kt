package org.oppia.android.app.player.state

import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.oppia.android.R
import javax.inject.Singleton

/**
 * Configuration for the confetti animations used in the state player. See sub-classes for available
 * configurations.
 */
sealed class ConfettiConfig() {
  protected abstract val minSpeed: Float
  protected abstract val maxSpeed: Float
  protected abstract val sizeInDp: Size
  // Confetti pieces with mass make the animation more active and dynamic.
  protected abstract val sizeWithMass: Size
  protected abstract val numPieces: Int
  protected abstract val shapes: Array<Shape>

  fun startConfettiBurst(
    confettiView: KonfettiView,
    xPosition: Float,
    yPosition: Float,
    minAngle: Double,
    maxAngle: Double,
    timeToLiveMs: Long,
    delayMs: Long,
    colorsList: List<Int>
  ) {
    confettiView.build()
      .setDelay(delayMs)
      .setFadeOutEnabled(true)
      .addColors(colorsList)
      .setDirection(minAngle, maxAngle)
      .setSpeed(minSpeed, maxSpeed)
      .setTimeToLive(timeToLiveMs)
      .addShapes(*shapes)
      .addSizes(sizeInDp, sizeWithMass)
      .setPosition(xPosition, yPosition)
      .burst(numPieces)
  }

  /** A configuration for large bursts of confetti. */
  @Singleton
  class LargeConfettiBurst : ConfettiConfig() {
    override val minSpeed = 5f
    override val maxSpeed = 12f
    override val sizeInDp = Size(sizeInDp = 12)
    override val sizeWithMass = Size(sizeInDp = 11, mass = 3f)
    override val numPieces = 60
    override val shapes = arrayOf(Shape.Circle, Shape.Square)
  }

  /** A configuration for medium bursts of confetti. */
  @Singleton
  class MediumConfettiBurst : ConfettiConfig() {
    override val minSpeed = 4f
    override val maxSpeed = 9f
    override val sizeInDp = Size(sizeInDp = 8)
    override val sizeWithMass = Size(sizeInDp = 7, mass = 3f)
    override val numPieces = 35
    override val shapes = arrayOf(Shape.Circle, Shape.Square)
  }

  /** A configuration for mini bursts of confetti. */
  @Singleton
  class MiniConfettiBurst : ConfettiConfig() {
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
  }
}
