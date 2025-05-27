package org.oppia.android.app.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** Adds a bezier curve background to a view. */
@Composable
fun WavyBackground(backgroundColorResId: Int) {
  val whiteColor = colorResource(backgroundColorResId)
  Canvas(
    modifier = Modifier.fillMaxSize(),
    onDraw = {
      val canvasWidth = size.width
      val canvasHeight = size.height

      val wavyPath = Path().apply {
        moveTo(0f, canvasHeight)

        val amplitude = 50.dp.toPx() // Height of the waves.

        // Waves start at ~50% of the screen height.
        val wavesStartY = canvasHeight * 0.5f
        lineTo(0f, wavesStartY)

        var x = 0f
        val stepX = canvasWidth / 100

        while (x <= canvasWidth) {
          val y = wavesStartY - amplitude * sin(x * (2f * Math.PI / canvasWidth)).toFloat()
          lineTo(x, y)
          x += stepX
        }

        lineTo(canvasWidth, canvasHeight)
        close()
      }

      drawPath(
        path = wavyPath,
        color = whiteColor
      )
    }
  )
}
