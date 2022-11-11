package org.oppia.android.app.spotlight

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.databinding.BottomLeftOverlayBinding
import org.oppia.android.databinding.BottomRightOverlayBinding
import org.oppia.android.databinding.TopLeftOverlayBinding
import org.oppia.android.databinding.TopRightOverlayBinding
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.util.accessibility.AccessibilityServiceImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject

class SpotlightFragment : InjectableFragment(), SpotlightNavigationListener {
  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var accessibilityServiceImpl: AccessibilityServiceImpl

  private var targetList = LinkedList<Target>()
  private lateinit var spotlight: Spotlight
  private var screenHeight: Int = 0
  private var screenWidth: Int = 0
  private lateinit var anchorPosition: AnchorPosition
  private lateinit var overlayBinding: Any
  private var internalProfileId: Int = -1
  private val isSpotlightActive = MutableLiveData(false)
  private var isRTL = false

  private fun calculateScreenSize() {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels
  }

  fun getSpotlightStatusLiveData(): MutableLiveData<Boolean> {
    return isSpotlightActive
  }

  // since this fragment does not have any view to inflate yet, all the tasks should be done here.
  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)

    internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY) ?: -1
    calculateScreenSize()
    checkIsRTL()
  }

  fun requestSpotlightViewWithDelayedLayout(spotlightTarget: SpotlightTarget) {
    spotlightTarget.anchor.doOnPreDraw {
      val targetAfterViewFullyDrawn = SpotlightTarget(
        it,
        spotlightTarget.hint,
        spotlightTarget.shape,
        spotlightTarget.feature
      )
      requestSpotlight(targetAfterViewFullyDrawn)
    }
  }

  fun requestSpotlightOnFirstRecyclerItem(customView: View, index: Int, spotlightTarget: SpotlightTarget) {
    customView.doOnPreDraw {
      if (index == 0) {
        val targetAfterViewFullyDrawn = SpotlightTarget(
          it,
          spotlightTarget.hint,
          spotlightTarget.shape,
          spotlightTarget.feature
        )
        requestSpotlight(targetAfterViewFullyDrawn)
      }
    }
  }

  /**
   * Requests a spotlight to be shown on the [SpotlightTarget]. The spotlight is enqueued if it
   * hasn't been shown before in a FIFO buffer.
   *
   * @param spotlightTarget The [SpotlightTarget] for which the spotlight is requested
   */
  fun requestSpotlight(spotlightTarget: SpotlightTarget) {

    if (accessibilityServiceImpl.isScreenReaderEnabled()) return

    val profileId = ProfileId.newBuilder()
      .setInternalId(internalProfileId)
      .build()

    val featureViewStateLiveData =
      spotlightStateController.retrieveSpotlightViewState(
        profileId,
        spotlightTarget.feature
      ).toLiveData()

    // use activity as observer because this fragment's view hasn't been created yet.
    featureViewStateLiveData.observe(
      activity,
      object : Observer<AsyncResult<SpotlightViewState>> {
        override fun onChanged(it: AsyncResult<SpotlightViewState>?) {
          if (it is AsyncResult.Success) {
            val viewState = (it.value)
            if (viewState == SpotlightViewState.SPOTLIGHT_SEEN) {
              return
            } else if (viewState == SpotlightViewState.SPOTLIGHT_NOT_SEEN) {
              createTarget(spotlightTarget)
              featureViewStateLiveData.removeObserver(this)
            }
          }
        }
      }
    )
  }

  private fun createTarget(
    spotlightTarget: SpotlightTarget
  ) {

    val target = Target.Builder()
      .setShape(getShape(spotlightTarget))
      .setOverlay(requestOverlayResource(spotlightTarget))
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {

          Log.d("overlay", "target  start")
        }

        override fun onEnded() {
          Log.d("overlay", "target  end")
          targetList.pop()
          val profileId = ProfileId.newBuilder()
            .setInternalId(internalProfileId)
            .build()
          spotlightStateController.markSpotlightViewed(profileId, spotlightTarget.feature)
        }
      })
      .build(spotlightTarget.anchor)

    targetList.add(target)
    if (!isSpotlightActive.value!!) {
      startSpotlight()
    }
  }

  private fun startSpotlight() {
    if (targetList.isNullOrEmpty()) return
    spotlight = Spotlight.Builder(activity)
      .setTargets(targetList)
      .setBackgroundColorRes(R.color.spotlightBackground)
      .setDuration(1000L)
      .setAnimation(DecelerateInterpolator(2f))
      .setOnSpotlightListener(object : OnSpotlightListener {
        override fun onStarted() {
          Log.d("overlay", "spotlight  start")

          isSpotlightActive.value = true
        }

        override fun onEnded() {
          Log.d("overlay", "spotlight  end")

          isSpotlightActive.value = false
          startSpotlight()
        }
      })
      .build()

    spotlight.start()
  }

  private fun getShape(spotlightTarget: SpotlightTarget): Shape {
    return when (spotlightTarget.shape) {
      SpotlightShape.RoundedRectangle -> {
        RoundedRectangle(
          spotlightTarget.anchorHeight.toFloat(),
          spotlightTarget.anchorWidth.toFloat(),
          24f
        )
      }
      SpotlightShape.Circle -> {
        return if (spotlightTarget.anchorHeight > spotlightTarget.anchorWidth) {
          Circle((spotlightTarget.anchorHeight / 2).toFloat())
        } else {
          Circle((spotlightTarget.anchorWidth / 2).toFloat())
        }
      }
    }
  }

  private fun checkIsRTL() {
    val locale = Locale.getDefault()
    val directionality: Byte = Character.getDirectionality(locale.displayName[0].toInt())
    isRTL = directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
      directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
  }

  private fun getArrowWidth(): Float {
    return this.resources.getDimension(R.dimen.arrow_width)
  }

  private fun getArrowHeight(): Float {
    return this.resources.getDimension(R.dimen.arrow_height)
  }

  private fun getScreenCentreY(): Int {
    Log.d("overlay screenCentre Y", (screenHeight / 2).toString())
    return screenHeight / 2
  }

  private fun getScreenCentreX(): Int {
    Log.d("overlay screenCentre X", (screenWidth / 2).toString())
    return screenWidth / 2
  }

  private fun calculateAnchorPosition(spotlightTarget: SpotlightTarget) {
    anchorPosition = if (spotlightTarget.anchorCentreX > getScreenCentreX()) {
      if (spotlightTarget.anchorCentreY > getScreenCentreY()) {
        AnchorPosition.BottomRight
      } else {
        AnchorPosition.TopRight
      }
    } else if (spotlightTarget.anchorCentreY > getScreenCentreY()) {
      AnchorPosition.BottomLeft
    } else {
      AnchorPosition.TopLeft
    }

    Log.d("overlay", anchorPosition.toString())
  }

  private fun requestOverlayResource(spotlightTarget: SpotlightTarget): View {
    calculateAnchorPosition(spotlightTarget)

    return when (anchorPosition) {
      AnchorPosition.TopLeft -> {
        if (isRTL) {
          configureTopRightOverlay(spotlightTarget)
        } else {
          configureTopLeftOverlay(spotlightTarget)
        }
      }
      AnchorPosition.TopRight -> {
        configureTopRightOverlay(spotlightTarget)
      }
      AnchorPosition.BottomRight -> {
        if (isRTL) {
          configureBottomLeftOverlay(spotlightTarget)
        } else {
          configureBottomRightOverlay(spotlightTarget)
        }
      }
      AnchorPosition.BottomLeft -> {
        if (isRTL) {
          configureBottomRightOverlay(spotlightTarget)
        } else {
          configureBottomLeftOverlay(spotlightTarget)
        }
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

  private fun configureBottomLeftOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = BottomLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.presenter = this
    }

    (overlayBinding as BottomLeftOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as BottomLeftOverlayBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRTL) {
      arrowParams.setMargins(
        10.dp,
        (spotlightTarget.anchorTop.toInt() - getArrowHeight() - 5.dp).toInt(),
        screenWidth - spotlightTarget.anchorLeft.toInt(),
        10.dp
      )
    } else {
      arrowParams.setMargins(
        spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight() - 5.dp).toInt(),
        10.dp,
        10.dp
      )
    }
    (overlayBinding as BottomLeftOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as BottomLeftOverlayBinding).root
  }

  private fun configureBottomRightOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = BottomRightOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.presenter = this
    }

    (overlayBinding as BottomRightOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as BottomRightOverlayBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRTL) {
      arrowParams.setMargins(
        10.dp,
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        screenWidth -
          (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        10.dp
      )
    } else {
      arrowParams.setMargins(
        (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        10.dp,
        10.dp
      )
    }
    (overlayBinding as BottomRightOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as BottomRightOverlayBinding).root
  }

  private fun configureTopRightOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = TopRightOverlayBinding.inflate(layoutInflater)
    (overlayBinding as TopRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.presenter = this
    }

    (overlayBinding as TopRightOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as TopRightOverlayBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRTL) {
      arrowParams.setMargins(
        10.dp,
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight + 5.dp).toInt(),
        screenWidth -
          (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        10.dp
      )
    } else {
      arrowParams.setMargins(
        (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight).toInt(),
        10.dp,
        10.dp
      )
    }
    (overlayBinding as TopRightOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as TopRightOverlayBinding).root
  }

  private fun configureTopLeftOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = TopLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as TopLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.presenter = this
    }

    (overlayBinding as TopLeftOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as TopLeftOverlayBinding).arrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRTL) {
      arrowParams.setMargins(
        10.dp,
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight + 5.dp).toInt(),
        screenWidth - spotlightTarget.anchorLeft.toInt(),
        10.dp
      )
    } else {
      arrowParams.setMargins(
        spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight + 5.dp).toInt(),
        10.dp,
        10.dp
      )
    }
    (overlayBinding as TopLeftOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as TopLeftOverlayBinding).root
  }

  private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}
