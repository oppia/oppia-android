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
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

class SurveyOnboardingBackgroundView : View {
  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  private lateinit var paint: Paint
  private var path: Path = Path()
  private var anc0X = 0f
  private var anc0Y = 0f
  private var anc1X = 0f
  private var anc1Y = 0f
  private var strokeWidth = 0f

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  override fun onDraw(canvas: Canvas) {
    if (isRtl)
      rotationY = 180f
    super.onDraw(canvas)

    path.moveTo(0f, 0f)
    path.cubicTo(anc0X, anc0Y, anc1X, anc1Y, this.width.toFloat(), this.height.toFloat())
    canvas.drawPath(path, paint)
  }

  fun setupCurvePaint() {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.apply {
      style = Paint.Style.STROKE
      strokeWidth = this@SurveyOnboardingBackgroundView.strokeWidth
      this.color =
        ContextCompat.getColor(
          context,
          R.color.component_color_survey_onboarding_background_color
        )
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }
}
