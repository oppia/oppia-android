package org.oppia.android.app.spotlight

import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.databinding.OverlayOverLeftBinding
import org.oppia.android.databinding.OverlayOverRightBinding
import org.oppia.android.databinding.OverlayUnderLeftBinding
import org.oppia.android.databinding.OverlayUnderRightBinding
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

class SpotlightFragment @Inject constructor(
  val activity: AppCompatActivity,
  val spotlightStateController: SpotlightStateController
) : Fragment(), SpotlightNavigationListener {
  var targetList = ArrayList<com.takusemba.spotlight.Target>()

  var spotlightTargetList = ArrayList<SpotlightTarget>()

  private lateinit var spotlight: Spotlight
  private val overlay = "overlay"

  private var screenHeight: Int = 0
  private var screenWidth: Int = 0
  private lateinit var anchorPosition: AnchorPosition
  private lateinit var overlayBinding: Any

  private lateinit var itemToSpotlight: View
  private var isRTL = false
  private var hintText = ""

  init {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels

    isRTL = checkIsRTL()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    lifecycleScope.launch {
      for (i in spotlightTargetList) {
        checkSpotlightViewState(i)
      }
      startSpotlight()
    }
    super.onViewCreated(view, savedInstanceState)
  }



  private fun checkSpotlightViewState(spotlightTarget: SpotlightTarget) {

    val profileId = ProfileId.newBuilder()
      .setInternalId(123)
      .build()

    val featureViewStateLiveData =
      spotlightStateController.retrieveSpotlightViewState(
        profileId,
        spotlightTarget.feature
      ).toLiveData()

    featureViewStateLiveData.observe(
      viewLifecycleOwner,
      object : Observer<AsyncResult<Any>> {
        override fun onChanged(it: AsyncResult<Any>?) {
          if (it is AsyncResult.Success) {
            featureViewStateLiveData.removeObserver(this)
            val viewState = (it.value as SpotlightViewState)
            if (viewState == SpotlightViewState.SPOTLIGHT_SEEN) {
              return
            } else if (viewState == SpotlightViewState.SPOTLIGHT_VIEW_STATE_UNSPECIFIED || viewState == SpotlightViewState.SPOTLIGHT_NOT_SEEN) {

              createTarget(spotlightTarget)
            }
          }
        }
      }

    )
  }

  private fun createTarget(
    spotlightTarget: SpotlightTarget
  ) {
    initialiseAnchor(spotlightTarget.anchor)
    initialiseHintText(hintText)

    val target = Target.Builder()
      .setAnchor(spotlightTarget.anchor)
      .setShape(getShape(spotlightTarget.shape))
      .setOverlay(requestOverlayResource())
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {

        }

        override fun onEnded() {
          val profileId = ProfileId.newBuilder()
            .setInternalId(123)
            .build()
          spotlightStateController.recordSpotlightCheckpoint(
            profileId,
            spotlightTarget.feature,
            SpotlightViewState.SPOTLIGHT_SEEN
          )
        }
      })
      .build()

    targetList.add(target)
  }

  private fun startSpotlight() {
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

  private fun checkIsRTL(): Boolean {
    val locale = Locale.getDefault()
    val directionality: Byte = Character.getDirectionality(locale.displayName[0].toInt())
    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
  }

  private fun getArrowHeight(): Float {
    return this.resources.getDimension(R.dimen.arrow_height)
  }

  private fun getArrowWidth(): Float {
    return this.resources.getDimension(R.dimen.arrow_width)
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

  private fun requestOverlayResource(): View {
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

  private sealed class AnchorPosition {
    object TopLeft : AnchorPosition()
    object TopRight : AnchorPosition()
    object BottomLeft : AnchorPosition()
    object BottomRight : AnchorPosition()
  }

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    spotlight.next()
  }

  private fun configureBottomLeftOverlay(): View {
    overlayBinding = OverlayOverLeftBinding.inflate(this.layoutInflater)
    (overlayBinding as OverlayOverLeftBinding).let {
      it.lifecycleOwner = this
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
    overlayBinding = OverlayOverRightBinding.inflate(this.layoutInflater)
    (overlayBinding as OverlayOverRightBinding).let {
      it.lifecycleOwner = this
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
    overlayBinding = OverlayUnderRightBinding.inflate(layoutInflater)
    (overlayBinding as OverlayUnderRightBinding).let {
      it.lifecycleOwner = this
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
    overlayBinding = OverlayUnderLeftBinding.inflate(this.layoutInflater)
    (overlayBinding as OverlayUnderLeftBinding).let {
      it.lifecycleOwner = this
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

  private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

data class SpotlightTarget(
  val anchor: View,
  val hint: String = "",
  val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
  val feature: org.oppia.android.app.model.Spotlight.FeatureCase
)