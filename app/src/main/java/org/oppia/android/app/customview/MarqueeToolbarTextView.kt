package org.oppia.android.app.customview

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

/** The custom Textview class for toolbar with Marquee effect. */
class MarqueeToolbarTextView : AppCompatTextView, View.OnClickListener {

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  init {
    setOnClickListener(this)
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = 1
    isHorizontalFadingEdgeEnabled = true
    isHorizontalScrollBarEnabled = true
    isFocusable = false
    isFocusableInTouchMode = true
    setFadingEdgeLength(20)
    isSingleLine = true
  }

  override fun onClick(v: View?) {
    isSelected = true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    if (isRtl) {
      textDirection = View.TEXT_DIRECTION_RTL
    } else {
      textDirection = View.TEXT_DIRECTION_LTR
    }
  }
}
