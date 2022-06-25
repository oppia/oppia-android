package org.oppia.android.app.spotlight

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import java.util.*
import org.oppia.android.R
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.databinding.OverlayBinding

class OverlayPositionAutomator(private val activity: Activity, private val fragment: Fragment) :
  SpotlightNavigationListener {
  private lateinit var spotlight: Spotlight
  private var screenHeight: Int = 0
  private var screenWidth: Int = 0
  private val overlay = "overlay"

  private lateinit var anchorPosition: AnchorPosition
  private lateinit var overlayBinding: OverlayBinding
  private lateinit var anchor: View

  init {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels
  }

  private fun getArrowHeight(): Float {
    return fragment.resources.getDimension(R.dimen.arrow_height)
  }

  private fun getArrowWidth(): Float {
    return fragment.resources.getDimension(R.dimen.arrow_width)
  }

  private fun initialiseAnchor(itemToSpotlight: View) {
    anchor = itemToSpotlight
  }

  private fun getAnchorLeft(): Float {
    return anchor.x
  }

  private fun getAnchorTop(): Float {
    return anchor.y
  }

  private fun getAnchorBottom(): Float {
    return anchor.bottom.dp.toFloat()
  }

  private fun getAnchorHeight(): Int {
    return anchor.height
  }

  private fun getAnchorWidth(): Int {
    return anchor.width
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
    Log.d("overlay screenCentre X" , (screenHeight / 2).toString())
    return screenWidth / 2
  }

  private fun getScreenCentreY(): Int {
    Log.d("overlay screenCentre Y" , (screenHeight / 2).toString())
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

  private fun setOverlayPosition() {
    calculateAnchorPosition()

    when (anchorPosition) {
      AnchorPosition.TopLeft -> {
        overlayBinding.arrow.x = getAnchorLeft()
        overlayBinding.arrow.y = getAnchorBottom()
        overlayBinding.customText.x = getAnchorLeft()
        overlayBinding.customText.y =
          getAnchorBottom() + getArrowHeight()
      }
      AnchorPosition.TopRight -> {
        overlayBinding.arrow.x = getAnchorLeft() + getAnchorWidth() - getArrowWidth()
        overlayBinding.arrow.y = getAnchorBottom()
        overlayBinding.customText.right = (getAnchorLeft() + getAnchorWidth()).toInt()
        overlayBinding.customText.y =
          getAnchorBottom() + getArrowHeight()
      }
      else -> {

      }
    }
  }

  fun getSpotlightOverlay(anchor: View): View {
    initialiseAnchor(anchor)

    overlayBinding = OverlayBinding.inflate(fragment.layoutInflater)
    overlayBinding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    setOverlayPosition()

    return overlayBinding.root
  }

  sealed class AnchorPosition {
    object TopLeft : AnchorPosition()
    object TopRight : AnchorPosition()
    object BottomLeft : AnchorPosition()
    object BottomRight : AnchorPosition()
  }

  fun startSpotlight(targets: ArrayList<Target>) {
    spotlight = Spotlight.Builder(activity)
      .setTargets(targets)
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

  val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

}