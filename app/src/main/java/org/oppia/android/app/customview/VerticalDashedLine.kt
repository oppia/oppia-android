package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi

class VerticalDashedLine : View {

  private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val path: Path = Path()

  constructor(context: Context?) : super(context) {
    initialize()
  }

  constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
    initialize()
  }

  constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    initialize()
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(
    context: Context?,
    @Nullable attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
  ) : super(context, attrs, defStyleAttr, defStyleRes) {
    initialize()
  }

  private fun initialize() {
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = 10F
    paint.color = Color.GRAY
    paint.pathEffect = DashPathEffect(floatArrayOf(20f, 25f), 20F)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    path.reset()
    path.moveTo((width / 2).toFloat(), 0F)
    path.quadTo((width / 2).toFloat(), height.toFloat() / 2,
      (width / 2).toFloat(), height.toFloat()
    )
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredWidth = 10
    val desiredHeight = 100

    val widthMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    val width: Int
    val height: Int

    //Measure Width


    width = if (widthMode == View.MeasureSpec.EXACTLY) {
      //Must be this size
      widthSize
    } else if (widthMode == View.MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      Math.min(desiredWidth, widthSize)
    } else {
      //Be whatever you want
      desiredWidth
    }

    //Measure Height

    height = if (heightMode == View.MeasureSpec.EXACTLY) {
      //Must be this size
      heightSize
    } else if (heightMode == View.MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      heightSize
    } else {
      //Be whatever you want
      desiredHeight
    }

    setMeasuredDimension(width, height)
  }

}
