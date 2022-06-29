package org.oppia.android.app.spotlight

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import java.util.*
import org.oppia.android.R
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.databinding.OverlayOverLeftBinding
import org.oppia.android.databinding.OverlayOverRightBinding
import org.oppia.android.databinding.OverlayUnderLeftBinding
import org.oppia.android.databinding.OverlayUnderRightBinding

//todo: check bottom wale

class OverlayPositionAutomator(private val activity: Activity, private val fragment: Fragment) :
  SpotlightNavigationListener {
  private lateinit var spotlight: Spotlight
  private var screenHeight: Int = 0
  private var screenWidth: Int = 0
  private val overlay = "overlay"

  private lateinit var anchorPosition: AnchorPosition
  private lateinit var overlayBinding: Any

  private lateinit var itemToSpotlight: View

  private var targetList = ArrayList<Target>()

  private var isRTL = false
  private var hintText = ""

  init {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels

    isRTL = checkIsRTL()
  }

  private fun checkIsRTL(): Boolean {
    val locale = Locale.getDefault()
    val directionality: Byte = Character.getDirectionality(locale.displayName[0].toInt())
    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
  }

  private fun getArrowHeight(): Float {
    return fragment.resources.getDimension(R.dimen.arrow_height)
  }

  private fun getArrowWidth(): Float {
    return fragment.resources.getDimension(R.dimen.arrow_width)
  }

  private fun initialiseAnchor(itemToSpotlight: View) {
    this.itemToSpotlight = itemToSpotlight
  }

  private fun initialiseHintText(hintText: String) {
    this.hintText = hintText
  }

  private fun getAnchorLeft(): Float {
    val location = IntArray(2)
    itemToSpotlight.getLocationOnScreen(location)
    val x = location[0]
    return x.toFloat()
  }

  private fun getAnchorTop(): Float {
    val location = IntArray(2)
    itemToSpotlight.getLocationOnScreen(location)
    val y = location[1]
    return y.toFloat()
  }

  private fun getAnchorHeight(): Int {
    return itemToSpotlight.height
  }

  private fun getAnchorWidth(): Int {
    return itemToSpotlight.width
  }

  private fun getAnchorCentreX(): Float {
    Log.d(overlay + "anchorCentre X ", (getAnchorLeft() + getAnchorWidth() / 2).toString())
    return getAnchorLeft() + getAnchorWidth() / 2
  }

  private fun getAnchorCentreY(): Float {
    Log.d(overlay + "anchorCentre Y ", (getAnchorTop() + getAnchorHeight() / 2).toString())
    return getAnchorTop() + getAnchorHeight() / 2
  }

  private fun getScreenCentreX(): Int {
    Log.d("overlay screenCentre X", (screenWidth / 2).toString())
    return screenWidth / 2
  }

  private fun getScreenCentreY(): Int {
    Log.d("overlay screenCentre Y", (screenHeight / 2).toString())
    return screenHeight / 2
  }

  private fun calculateAnchorPosition() {
    anchorPosition = if (getAnchorCentreX() > getScreenCentreX()) {
      if (getAnchorCentreY() > getScreenCentreY()) {
        AnchorPosition.BottomRight
      } else {
        AnchorPosition.TopRight
      }
    } else if (getAnchorCentreY() > getScreenCentreY()) {
      AnchorPosition.BottomLeft
    } else {
      AnchorPosition.TopLeft
    }

    Log.d(overlay, anchorPosition.toString())
  }

  private fun setOverlayPosition(): View {
    calculateAnchorPosition()

    return when (anchorPosition) {
      AnchorPosition.TopLeft -> {
        if (isRTL) {
          configureTopRightOverlay()
        } else {
          configureTopLeftOverlay()
        }
      }
      AnchorPosition.TopRight -> {
        configureTopRightOverlay()
      }
      AnchorPosition.BottomRight -> {
        configureBottomRightOverlay()
      }
      AnchorPosition.BottomLeft -> {
        configureBottomLeftOverlay()
      }
    }
  }

  private fun configureBottomLeftOverlay(): View {
    overlayBinding = OverlayOverLeftBinding.inflate(fragment.layoutInflater)
    (overlayBinding as OverlayOverLeftBinding).let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    (overlayBinding as OverlayOverLeftBinding).customText.text = hintText

    val arrowParams = (overlayBinding as OverlayOverLeftBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    arrowParams.setMargins(
      getAnchorLeft().toInt(),
      (getAnchorTop().toInt() - getArrowHeight()).toInt(),
      10.dp,
      10.dp
    )
//    arrowParams.marginStart = getAnchorLeft().toInt()
//    arrowParams.marginEnd = 10.dp
    (overlayBinding as OverlayOverLeftBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as OverlayOverLeftBinding).root
  }

  private fun configureBottomRightOverlay(): View {
    overlayBinding = OverlayOverRightBinding.inflate(fragment.layoutInflater)
    (overlayBinding as OverlayOverRightBinding).let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    (overlayBinding as OverlayOverRightBinding).customText.text = hintText

    val arrowParams = (overlayBinding as OverlayOverRightBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    arrowParams.setMargins(
      (getAnchorLeft() + getAnchorWidth() - getArrowWidth()).toInt(),
      (getAnchorTop().toInt() - getArrowHeight()).toInt(),
      10.dp,
      10.dp
    )
//    arrowParams.marginStart = (getAnchorLeft() + getAnchorWidth() - getArrowWidth()).toInt()
//    arrowParams.marginEnd = 10.dp
    (overlayBinding as OverlayOverRightBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as OverlayOverRightBinding).root
  }

  private fun configureTopRightOverlay(): View {
    overlayBinding = OverlayUnderRightBinding.inflate(fragment.layoutInflater)
    (overlayBinding as OverlayUnderRightBinding).let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    (overlayBinding as OverlayUnderRightBinding).customText.text = hintText

    val arrowParams = (overlayBinding as OverlayUnderRightBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    arrowParams.setMargins(
      (getAnchorLeft() + getAnchorWidth() - getArrowWidth()).toInt(),
      (getAnchorTop() + getAnchorHeight()).toInt(),
      10.dp,
      10.dp
    )
//    arrowParams.marginStart = (getAnchorLeft() + getAnchorWidth() - getArrowWidth()).toInt()
//    arrowParams.marginEnd = 10.dp
    (overlayBinding as OverlayUnderRightBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as OverlayUnderRightBinding).root
  }

  private fun configureTopLeftOverlay(): View {
    overlayBinding = OverlayUnderLeftBinding.inflate(fragment.layoutInflater)
    (overlayBinding as OverlayUnderLeftBinding).let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    (overlayBinding as OverlayUnderLeftBinding).customText.text = hintText

    val arrowParams = (overlayBinding as OverlayUnderLeftBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    arrowParams.setMargins(
      getAnchorLeft().toInt(),
      (getAnchorTop() + getAnchorHeight()).toInt(),
      10.dp,
      10.dp
    )
//    arrowParams.marginStart = getAnchorLeft().toInt()
//    arrowParams.marginEnd = 10.dp
    (overlayBinding as OverlayUnderLeftBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as OverlayUnderLeftBinding).root
  }

  fun createTarget(
    anchor: View,
    hintText: String = "",
    shape: SpotlightShape = SpotlightShape.RoundedRectangle,
  ) {
    initialiseAnchor(anchor)
    initialiseHintText(hintText)

    val target = Target.Builder()
      .setAnchor(anchor)
      .setShape(getShape(shape))
      .setOverlay(setOverlayPosition())
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {
        }

        override fun onEnded() {

        }
      })
      .build()

    targetList.add(target)
  }

  private fun getShape(shape: SpotlightShape): Shape {
    return when (shape) {
      SpotlightShape.RoundedRectangle -> {
        RoundedRectangle(itemToSpotlight.height.toFloat(), itemToSpotlight.width.toFloat(), 24f)
      }
      SpotlightShape.Circle -> {
        return if (getAnchorHeight() > getAnchorWidth()) {
          Circle((getAnchorHeight() / 2).toFloat())
        } else {
          Circle((getAnchorWidth() / 2).toFloat())
        }
      }
    }
  }

  fun startSpotlight() {
    spotlight = Spotlight.Builder(activity)
      .setTargets(targetList)
      .setBackgroundColorRes(R.color.spotlightBackground)
      .setDuration(1000L)
      .setAnimation(DecelerateInterpolator(2f))
      .setOnSpotlightListener(object : OnSpotlightListener {
        override fun onStarted() {
        }

        override fun onEnded() {
        }
      })
      .build()

    spotlight.start()
  }

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    spotlight.next()
  }

  private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

  sealed class AnchorPosition {
    object TopLeft : AnchorPosition()
    object TopRight : AnchorPosition()
    object BottomLeft : AnchorPosition()
    object BottomRight : AnchorPosition()
  }

  companion object {
    sealed class SpotlightShape {
      object RoundedRectangle : SpotlightShape()
      object Circle : SpotlightShape()
    }
  }
}