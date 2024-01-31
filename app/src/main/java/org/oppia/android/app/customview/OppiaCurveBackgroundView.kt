package org.oppia.android.app.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

/**
 * CustomView to add a background to views that require a bezier curve background.
 *
 * Reference: // https://proandroiddev.com/how-i-drew-custom-shapes-in-bottom-bar-c4539d86afd7 and
 * // https://ciechanow.ski/drawing-bezier-curves/
 */
class OppiaCurveBackgroundView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  private var customBackgroundColor = Color.WHITE // Default color

  private lateinit var paint: Paint
  private lateinit var path: Path
  private var strokeWidth = 2f

  init {
    val typedArray: TypedArray =
      context.obtainStyledAttributes(attrs, R.styleable.OppiaCurveBackgroundView)
    customBackgroundColor =
      typedArray.getColor(R.styleable.OppiaCurveBackgroundView_customBackgroundColor, Color.WHITE)
    typedArray.recycle()
    setupCurvePaint()
  }

  override fun onDraw(canvas: Canvas) {
    if (isRtl)
      rotationY = 180f
    super.onDraw(canvas)

    canvas.drawPath(path, paint)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    val width = this.width.toFloat()
    val height = this.height.toFloat()

    val controlPoint1X = width * 0.4f
    val controlPoint1Y = 0f

    val controlPoint2X = width * 0.5f
    val controlPoint2Y = height * 0.15f

    val controlPoint3X = width * 1.2f
    val controlPoint3Y = height * 0.1f

    path.reset()
    path.moveTo(0f, height * 0.1f)
    path.cubicTo(
      controlPoint1X,
      controlPoint1Y,
      controlPoint2X,
      controlPoint2Y,
      controlPoint3X,
      controlPoint3Y
    )

    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.lineTo(0f, 0f)
  }

  private fun setupCurvePaint() {
    path = Path()
    paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.apply {
      style = Paint.Style.FILL_AND_STROKE
      strokeWidth = this@OppiaCurveBackgroundView.strokeWidth
      color = customBackgroundColor
    }
    setBackgroundColor(Color.TRANSPARENT)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }
}
