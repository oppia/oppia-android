package org.oppia.android.app.player.state

import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.oppia.android.R

/**
 * Configuration for the confetti animations used in the state player. See enum types for available
 * configurations.
 */
enum class ConfettiConfig(
  private val minSpeed: Float,
  private val maxSpeed: Float,
  private val sizeInDp: Size,
  // Confetti pieces with mass make the animation more active and dynamic.
  private val sizeWithMass: Size,
  private val numPieces: Int,
  private val shapes: Array<Shape>
) {

  LARGE_CONFETTI_BURST(
    minSpeed = 5f,
    maxSpeed = 12f,
    sizeInDp = Size(sizeInDp = 12),
    sizeWithMass = Size(sizeInDp = 11, mass = 3f),
    numPieces = 60,
    shapes = arrayOf(Shape.Circle, Shape.Square)
  ),
  MEDIUM_CONFETTI_BURST(
    minSpeed = 4f,
    maxSpeed = 9f,
    sizeInDp = Size(sizeInDp = 8),
    sizeWithMass = Size(sizeInDp = 7, mass = 3f),
    numPieces = 35,
    shapes = arrayOf(Shape.Circle, Shape.Square)
  ),
  MINI_CONFETTI_BURST(
    minSpeed = 2f,
    maxSpeed = 4f,
    sizeInDp = Size(sizeInDp = 8),
    sizeWithMass = Size(sizeInDp = 7, mass = 3f),
    numPieces = 7,
    shapes = arrayOf(Shape.Circle)
  );

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

  companion object {
    /** Primary colors to use for the confetti. */
    val primaryColors: List<Int> = listOf(
      R.color.confetti_red,
      R.color.confetti_yellow,
      R.color.confetti_blue
    )
  }
}
