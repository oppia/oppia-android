package org.oppia.android.app.customview

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.lang.RuntimeException
import kotlin.math.abs

/**
 * Provides a simple marquee effect for a single [android.widget.TextView].
 * Reference: https://github.com/ened/Android-MarqueeView/blob/master/library/src/main/java/asia/ivity/android/marqueeview/MarqueeView.java
 */
class MarqueeView : LinearLayout {
  private lateinit var mTextField: TextView
  private lateinit var mScrollView: ScrollView
  private lateinit var mMoveTextOut: Animation
  private lateinit var mMoveTextIn: Animation
  private lateinit var mPaint: Paint
  private var mMarqueeNeeded = false
  private var mTextDifference = 0f
  private var mAutoStart = false
  private var mInterpolator: Interpolator = LinearInterpolator()
  private var mCancelled = false
  private lateinit var mAnimationStartRunnable: Runnable
  private var mStarted = false

  private val textviewVirtualWidth = 2000

  /**Control the speed. The lower this value, the faster it will scroll.*/
  private var mSpeed = 15

  /**Control the pause between the animations.*/
  private var mAnimationPause = 1000

  /**Init the Marquee when class in invoked takes param [Context].*/
  constructor(context: Context?) : super(context) {
    init()
  }
  /**Init the Marquee when class in invoked takes param [Context] [AttributeSet].*/
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    init()
  }

  /**Init the Marquee when class in invoked takes param [Context] [AttributeSet] [Int].*/
  constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init()
  }

  private fun init() {
    // init helper
    mPaint = Paint()
    mPaint.isAntiAlias = true
    mPaint.strokeWidth = 1f
    mPaint.strokeCap = Paint.Cap.ROUND
    mInterpolator = LinearInterpolator()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    if (childCount != 1) {
      throw RuntimeException("MarqueeView must have exactly one child element.")
    }
    if (changed && ::mScrollView.isInitialized.not()) {
      if (getChildAt(0) !is TextView) {
        throw RuntimeException("The child view of this MarqueeView must be a TextView instance.")
      }
      initView(context)
      prepareAnimation()
      if (mAutoStart) {
        startMarquee()
      }
    }
  }

  /**
   * Starts the configured marquee effect.
   */
  fun startMarquee() {
    if (mMarqueeNeeded) {
      startTextFieldAnimation()
    }
    mCancelled = false
    mStarted = true
  }

  /**
   * Animates the child Textfield with the provided animation params.
   */
  private fun startTextFieldAnimation() {
    mAnimationStartRunnable = Runnable { mTextField.startAnimation(mMoveTextOut) }
    postDelayed(mAnimationStartRunnable, mAnimationPause.toLong())
  }

  /**
   * Disables the animations.
   */
  private fun reset() {
    mCancelled = true
    if (::mAnimationStartRunnable.isInitialized) {
      removeCallbacks(mAnimationStartRunnable)
    }
    mTextField.clearAnimation()
    mStarted = false
    mMoveTextOut.reset()
    mMoveTextIn.reset()
    cutTextView()
    invalidate()
  }

  /**
   * Prepare the animation to render the marquee effect.
   */
  private fun prepareAnimation() {
    // Measure
    mPaint.textSize = mTextField.textSize
    mPaint.typeface = mTextField.typeface
    val mTextWidth = mPaint.measureText(mTextField.text.toString())

    // See how much functions are needed at all
    mMarqueeNeeded = mTextWidth > measuredWidth
    mTextDifference = abs(mTextWidth - measuredWidth)

    val duration = (mTextDifference * mSpeed).toInt()
    mMoveTextOut = TranslateAnimation(0f, -mTextDifference, 0f, 0f)
    mMoveTextOut.duration = duration.toLong()
    mMoveTextOut.interpolator = mInterpolator
    mMoveTextOut.fillAfter = true
    mMoveTextIn = TranslateAnimation(-mTextDifference, 0f, 0f, 0f)
    mMoveTextIn.duration = duration.toLong()
    mMoveTextIn.startOffset = mAnimationPause.toLong()
    mMoveTextIn.interpolator = mInterpolator
    mMoveTextIn.fillAfter = true
    mMoveTextOut.setAnimationListener(object : Animation.AnimationListener {
      override fun onAnimationStart(animation: Animation) {
        expandTextView()
      }

      override fun onAnimationEnd(animation: Animation) {
        if (mCancelled) {
          reset()
          return
        }
        mTextField.startAnimation(mMoveTextIn)
      }

      override fun onAnimationRepeat(animation: Animation) {}
    })
    mMoveTextIn.setAnimationListener(object : Animation.AnimationListener {
      override fun onAnimationStart(animation: Animation) {}
      override fun onAnimationEnd(animation: Animation) {
        cutTextView()
        if (mCancelled) {
          reset()
          return
        }
      }

      override fun onAnimationRepeat(animation: Animation) {}
    })
  }

  /**
   * Initiate the view by adding scrollview and adding view items.
   */
  private fun initView(context: Context) {
    // Scroll View
    val svLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    svLayoutParams.gravity = Gravity.CENTER
    mScrollView = ScrollView(context)

    // Scroll View - Text Field
    mTextField = getChildAt(0) as TextView
    removeView(mTextField)
    mScrollView.addView(
      mTextField,
      FrameLayout.LayoutParams(mTextField.measuredWidth, LayoutParams.WRAP_CONTENT)
    )
    mTextField.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
      override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
      override fun afterTextChanged(editable: Editable) {
        val continueAnimation = mStarted
        reset()
        prepareAnimation()
        cutTextView()
        post {
          if (continueAnimation) {
            startMarquee()
          }
        }
      }
    })
    addView(mScrollView, svLayoutParams)
  }

  /**
   * Expand textview to show the rest of hidden text.
   */
  private fun expandTextView() {
    val lp = mTextField.layoutParams
    lp.width = textviewVirtualWidth
    mTextField.layoutParams = lp
  }

  /**
   * Revert back text to initial cut state after animation finishes.
   */
  private fun cutTextView() {
    if (mTextField.width != measuredWidth) {
      val lp = mTextField.layoutParams
      lp.width = measuredWidth
      mTextField.layoutParams = lp
    }
  }
}
