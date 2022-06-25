package org.oppia.android.app.spotlight

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import java.util.*
import org.oppia.android.R
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.databinding.OverlayOverLeftBinding
import org.oppia.android.databinding.OverlayOverRightBinding
import org.oppia.android.databinding.OverlayUnderLeftBinding
import org.oppia.android.databinding.OverlayUnderRightBinding

class SpotlightOverlayPositionAutomator(
  private val fragment: Fragment,
  private val activity: Activity
) : SpotlightNavigationListener {

  private lateinit var overlayBinding: Any
  private lateinit var spotlight: Spotlight

  //  these will determine what arrow resource we will use (top, bottom, left, right facing)
  private lateinit var overlayHintVerticalPosition: OverlayHintVerticalPosition
  private lateinit var overlayHintHorizontalPosition: OverlayHintHorizontalPosition

  private fun getAnchorTopMargin(anchor: View): Int {
    return anchor.y.toInt()
  }

  private fun getAnchorLeftMargin(anchor: View): Int {
    return anchor.x.toInt()
  }

  private fun getAnchorHeight(anchor: View): Int {
    return anchor.height
  }

  private fun getAnchorWidth(anchor: View): Int {
    return anchor.width
  }

  private fun calculateOverlayVerticalHintPosition(anchor: View) {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    val screenHeight = displayMetrics.heightPixels
    val screenWidth = displayMetrics.widthPixels


    overlayHintVerticalPosition =
      if ((screenHeight / 2).toInt() > (getAnchorTopMargin(anchor) + getAnchorHeight(anchor) / 2).toInt()) {
        OverlayHintVerticalPosition.UNDER
      } else OverlayHintVerticalPosition.OVER
  }

  private fun calculateOverlayHorizontalHintPosition(anchor: View) {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    val screenWidth = displayMetrics.widthPixels

    overlayHintHorizontalPosition =
      if ((getAnchorLeftMargin(anchor) + getAnchorWidth(anchor) / 2).toInt() > (screenWidth / 2).toInt()) {
        OverlayHintHorizontalPosition.RIGHT
      } else OverlayHintHorizontalPosition.LEFT
  }

  private fun calculateArrowTopMargin(anchor: View): Int {

    calculateOverlayVerticalHintPosition(anchor)
    return if (overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER) {
      getAnchorTopMargin(anchor) + getAnchorHeight(anchor)
    } else {
      getAnchorTopMargin(anchor) - fragment.resources.getDimension(R.dimen.arrow_height).toInt()
    }
  }

  private fun calculateArrowLeftMargin(anchor: View): Int {
    calculateOverlayHorizontalHintPosition(anchor)
    return if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT) {
      getAnchorLeftMargin(anchor) + getAnchorWidth(anchor)
    } else {
      getAnchorLeftMargin(anchor)
    }
  }

  fun getSpotlightOverlay(anchor: View): View? {

    calculateOverlayHorizontalHintPosition(anchor)
    calculateOverlayVerticalHintPosition(anchor)


    when {
      overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT &&
        overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER
      -> {
        overlayBinding = OverlayUnderRightBinding.inflate(fragment.layoutInflater)
        (overlayBinding as OverlayUnderRightBinding).let {
          it.lifecycleOwner = fragment
          it.presenter = this
        }

        (overlayBinding as OverlayUnderRightBinding).arrow.x = anchor.right.dp.toFloat()
        (overlayBinding as OverlayUnderRightBinding).arrow.y = anchor.bottom.dp.toFloat()
        (overlayBinding as OverlayUnderRightBinding).customText.x = anchor.x
        (overlayBinding as OverlayUnderRightBinding).customText.y = anchor.bottom.dp.toFloat()

        return (overlayBinding as OverlayUnderRightBinding).root
      }
      overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT &&
        overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER
      -> {
        overlayBinding = OverlayOverRightBinding.inflate(fragment.layoutInflater)
        (overlayBinding as OverlayOverRightBinding).let {
          it.lifecycleOwner = fragment
          it.presenter = this
        }

        (overlayBinding as OverlayUnderLeftBinding).arrow.x = anchor.x
        (overlayBinding as OverlayUnderLeftBinding).arrow.y = anchor.bottom.dp.toFloat()
        (overlayBinding as OverlayUnderLeftBinding).customText.x = anchor.x
        (overlayBinding as OverlayUnderLeftBinding).customText.y = anchor.bottom.dp.toFloat()

        return (overlayBinding as OverlayOverRightBinding).root
      }
      overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT &&
        overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER
      -> {
        overlayBinding = OverlayOverLeftBinding.inflate(fragment.layoutInflater)
        (overlayBinding as OverlayOverLeftBinding).let {
          it.lifecycleOwner = fragment
          it.presenter = this
        }

        (overlayBinding as OverlayOverLeftBinding).arrow.x = anchor.x
        (overlayBinding as OverlayOverLeftBinding).arrow.y = anchor.bottom.dp.toFloat()
        (overlayBinding as OverlayOverLeftBinding).customText.x = anchor.x
        (overlayBinding as OverlayOverLeftBinding).customText.y = anchor.bottom.dp.toFloat()

        return (overlayBinding as OverlayOverLeftBinding).root
      }
      overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT &&
        overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER
      -> {
        overlayBinding = OverlayUnderLeftBinding.inflate(fragment.layoutInflater)
        (overlayBinding as OverlayUnderLeftBinding).let {
          it.lifecycleOwner = fragment
          it.presenter = this
        }


        // translate x is used because it changes the positions of views after they have been layed out.
        //     layout margin using
        (overlayBinding as OverlayUnderLeftBinding).arrow.x = anchor.x
        (overlayBinding as OverlayUnderLeftBinding).arrow.y = anchor.bottom.dp.toFloat()
        (overlayBinding as OverlayUnderLeftBinding).customText.x = anchor.x
        (overlayBinding as OverlayUnderLeftBinding).customText.y = anchor.bottom.dp.toFloat()

        return (overlayBinding as OverlayUnderLeftBinding).root
      }
      else -> return null
    }

  }

  sealed class OverlayHintVerticalPosition {
    object OVER : OverlayHintVerticalPosition()
    object UNDER : OverlayHintVerticalPosition()
  }

  sealed class OverlayHintHorizontalPosition {
    object RIGHT : OverlayHintHorizontalPosition()
    object LEFT : OverlayHintHorizontalPosition()
  }

  val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    spotlight.next()
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
}