package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl

class SurveyOnboardingBackgroundView : View {
  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  private lateinit var paint: Paint
  private var path: Path = Path()
  private var strokeWidth = 2f

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  init {
    initialize()
  }

  private fun initialize() {
    setupCurvePaint()
  }

  override fun onDraw(canvas: Canvas) {
    if (isRtl)
      rotationY = 180f
    super.onDraw(canvas)

    path.reset()
    val width = this.width.toFloat()
    val height = this.height.toFloat()

    val controlPoint1X = width * 0.25f
    val controlPoint1Y = 0f

    val controlPoint2X = width * 0.5f
    val controlPoint2Y = height * 0.2f

    val controlPoint3X = width * 1f
    val controlPoint3Y = height * 0.1f

    path.moveTo(0f, height * 0.1f)
    path.cubicTo(
      controlPoint1X,
      controlPoint1Y,
      controlPoint2X,
      controlPoint2Y,
      controlPoint3X,
      controlPoint3Y
    )

    canvas.drawPath(path, paint)
  }

  private fun setupCurvePaint() {
    paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.apply {
      style = Paint.Style.STROKE
      strokeWidth = this@SurveyOnboardingBackgroundView.strokeWidth
      color = ContextCompat.getColor(
        context,
        R.color.component_color_survey_onboarding_background_color
      )
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }
}
