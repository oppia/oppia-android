package org.oppia.util.parser

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

/**
 * Custom Bullet Span implementation (based on [BulletSpan])
 * Default implementation doesn't allow for radius modification
 */
class BulletSpanWithRadius(bulletRadius: Int, gapWidth: Int) : LeadingMarginSpan {
  private val gapWidth: Int = gapWidth
  private val bulletRadius: Int = bulletRadius

  override fun getLeadingMargin(first: Boolean): Int {
    return 2 * bulletRadius + gapWidth
  }

  override fun drawLeadingMargin(
    c: Canvas,
    p: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    text: CharSequence,
    start: Int,
    end: Int,
    first: Boolean,
    l: Layout
  ) {
    if ((text as Spanned).getSpanStart(this) == start) {
      val style = p.style
      p.style = Paint.Style.FILL
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && c.isHardwareAccelerated) {
        if (bulletPath == null) {
          bulletPath = Path()
          // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
          bulletPath!!.addCircle(
            0.0f,
            0.0f,
            1.2f * bulletRadius,
            Path.Direction.CW
          )
        }
        c.save()
        c.translate(x + dir * (bulletRadius * 1.2f + 1), (top + bottom) / 2.0f)
        c.drawPath(bulletPath, p)
        c.restore()
      } else {
        c.drawCircle(
          x + dir * (bulletRadius + 1).toFloat(),
          (top + bottom) / 2.0f,
          bulletRadius.toFloat(),
          p
        )
      }
      p.style = style
    }
  }

  companion object {
    private var bulletPath: Path? = null
  }
}
