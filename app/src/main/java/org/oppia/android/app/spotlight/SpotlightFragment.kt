package org.oppia.android.app.spotlight

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import com.takusemba.spotlight.Target
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.BottomLeftOverlayBinding
import org.oppia.android.databinding.BottomRightOverlayBinding
import org.oppia.android.databinding.TopLeftOverlayBinding
import org.oppia.android.databinding.TopRightOverlayBinding
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import java.util.LinkedList
import javax.inject.Inject

/**
 * Fragment to hold spotlights on elements. This fragment provides a single place for all the
 * spotlight interactions, and handles lifecycle related functionality for spotlights, such as
 * surviving orientation changes and marking spotlights as seen.
 */
class SpotlightFragment : InjectableFragment(), SpotlightNavigationListener, SpotlightManager {
  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var accessibilityService: AccessibilityService

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var targetList = LinkedList<Target>()
  private lateinit var spotlight: Spotlight
  private var screenHeight: Int = 0
  private var screenWidth: Int = 0
  private lateinit var overlayBinding: Any
  private var internalProfileId: Int = -1
  private var isSpotlightActive = false
  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  // Since this fragment does not have any view to inflate yet, all the tasks should be done here.
  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
    internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY) ?: -1
    calculateScreenSize()
  }

  override fun requestSpotlightViewWithDelayedLayout(spotlightTarget: SpotlightTarget) {
    // For the spotlights to work correctly, we must know the [SpotlightTarget]'s anchor's size and
    // position. For a view that is laid out after an animation, we must wait until the final size
    // and positions of the anchor view are measured, which can be achieved by using doOnPreDraw:
    // https://betterprogramming.pub/stop-using-post-postdelayed-in-your-android-views-9d1c8eeaadf2
    spotlightTarget.anchor.doOnPreDraw {
      val targetAfterViewFullyDrawn = SpotlightTarget(
        it,
        spotlightTarget.hint,
        spotlightTarget.shape,
        spotlightTarget.feature
      )
      requestSpotlight(targetAfterViewFullyDrawn)
    }
    spotlightTarget.anchor.invalidate()
    spotlightTarget.anchor.requestLayout()
  }

  override fun requestSpotlight(spotlightTarget: SpotlightTarget) {
    // When Talkback is turned on, do not show spotlights since they are visual tools and can
    // potentially make the app experience difficult for a non-sighted user.
    if (accessibilityService.isScreenReaderEnabled()) return
    val profileId = ProfileId.newBuilder()
      .setInternalId(internalProfileId)
      .build()

    val featureViewStateLiveData =
      spotlightStateController.retrieveSpotlightViewState(
        profileId,
        spotlightTarget.feature
      ).toLiveData()

    // Use activity as observer because this fragment's view hasn't been created yet.
    featureViewStateLiveData.observe(
      activity,
      object : Observer<AsyncResult<SpotlightViewState>> {
        override fun onChanged(it: AsyncResult<SpotlightViewState>?) {
          if (it is AsyncResult.Success) {
            if (it.value == SpotlightViewState.SPOTLIGHT_SEEN) return
            createTarget(spotlightTarget)
            featureViewStateLiveData.removeObserver(this)
          }
        }
      }
    )
  }

  override fun clickOnDone() {
    spotlight.next()
  }

  private fun createTarget(spotlightTarget: SpotlightTarget) {
    val target = Target.Builder()
      .setShape(getShape(spotlightTarget))
      .setOverlay(requestOverlayResource(spotlightTarget))
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {
        }

        override fun onEnded() {
          targetList.pop()
          val profileId = ProfileId.newBuilder()
            .setInternalId(internalProfileId)
            .build()
          spotlightStateController.markSpotlightViewed(profileId, spotlightTarget.feature)
        }
      })
      .build(spotlightTarget.anchor)

    targetList.add(target)
    if (!isSpotlightActive) startSpotlight()
  }

  private fun startSpotlight() {
    if (targetList.isNullOrEmpty()) return
    spotlight = Spotlight.Builder(activity)
      .setTargets(targetList)
      .setBackgroundColorRes(R.color.spotlight_overlay_background)
      .setDuration(1000L)
      .setAnimation(DecelerateInterpolator(2f))
      .setOnSpotlightListener(object : OnSpotlightListener {
        override fun onStarted() {
          isSpotlightActive = true
        }

        override fun onEnded() {
          isSpotlightActive = false
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
          Circle((spotlightTarget.anchorHeight / 2.0f))
        } else {
          Circle((spotlightTarget.anchorWidth / 2.0f))
        }
      }
    }
  }

  private fun getArrowWidth(): Float {
    return this.resources.getDimension(R.dimen.arrow_width)
  }

  private fun getArrowHeight(): Float {
    return this.resources.getDimension(R.dimen.arrow_height)
  }

  private fun getScreenCentreY(): Int {
    return screenHeight / 2
  }

  private fun getScreenCentreX(): Int {
    return screenWidth / 2
  }

  private fun calculateAnchorPosition(spotlightTarget: SpotlightTarget): AnchorPosition {
    return if (spotlightTarget.anchorCentreX > getScreenCentreX()) {
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
  }

  private fun requestOverlayResource(spotlightTarget: SpotlightTarget): View {
    val anchorPosition = calculateAnchorPosition(spotlightTarget)

    return when (anchorPosition) {
      AnchorPosition.TopLeft -> {
        if (isRtl) {
          configureTopRightOverlay(spotlightTarget)
        } else {
          configureTopLeftOverlay(spotlightTarget)
        }
      }
      AnchorPosition.TopRight -> {
        if (isRtl) {
          configureTopLeftOverlay(spotlightTarget)
        } else {
          configureTopRightOverlay(spotlightTarget)
        }
      }
      AnchorPosition.BottomRight -> {
        if (isRtl) {
          configureBottomLeftOverlay(spotlightTarget)
        } else {
          configureBottomRightOverlay(spotlightTarget)
        }
      }
      AnchorPosition.BottomLeft -> {
        if (isRtl) {
          configureBottomRightOverlay(spotlightTarget)
        } else {
          configureBottomLeftOverlay(spotlightTarget)
        }
      }
    }
  }

  private fun configureBottomLeftOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = BottomLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }
    (overlayBinding as BottomLeftOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as BottomLeftOverlayBinding).spotlightOverlayArrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRtl) {
      arrowParams.setMargins(
        screenWidth - spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    } else {
      arrowParams.setMargins(
        spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    }
    (overlayBinding as BottomLeftOverlayBinding).spotlightOverlayArrow.layoutParams = arrowParams
    return (overlayBinding as BottomLeftOverlayBinding).root
  }

  private fun configureBottomRightOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = BottomRightOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }
    (overlayBinding as BottomRightOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as BottomRightOverlayBinding).spotlightOverlayArrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRtl) {
      arrowParams.setMargins(
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        screenWidth -
          (spotlightTarget.anchorLeft + getArrowWidth()).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    } else {
      arrowParams.setMargins(
        (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        (spotlightTarget.anchorTop.toInt() - getArrowHeight()).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    }
    (overlayBinding as BottomRightOverlayBinding).spotlightOverlayArrow.layoutParams = arrowParams
    return (overlayBinding as BottomRightOverlayBinding).root
  }

  private fun configureTopRightOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = TopRightOverlayBinding.inflate(layoutInflater)
    (overlayBinding as TopRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }
    (overlayBinding as TopRightOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as TopRightOverlayBinding).spotlightOverlayArrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRtl) {
      arrowParams.setMargins(
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight).toInt(),
        screenWidth - (spotlightTarget.anchorLeft + getArrowWidth()).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    } else {
      arrowParams.setMargins(
        (spotlightTarget.anchorLeft + spotlightTarget.anchorWidth - getArrowWidth()).toInt(),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    }
    (overlayBinding as TopRightOverlayBinding).spotlightOverlayArrow.layoutParams = arrowParams
    return (overlayBinding as TopRightOverlayBinding).root
  }

  private fun configureTopLeftOverlay(spotlightTarget: SpotlightTarget): View {
    overlayBinding = TopLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as TopLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }
    (overlayBinding as TopLeftOverlayBinding).customText.text = spotlightTarget.hint

    val arrowParams = (overlayBinding as TopLeftOverlayBinding).spotlightOverlayArrow.layoutParams
      as ViewGroup.MarginLayoutParams
    if (isRtl) {
      arrowParams.setMargins(
        screenWidth - spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    } else {
      arrowParams.setMargins(
        spotlightTarget.anchorLeft.toInt(),
        (spotlightTarget.anchorTop + spotlightTarget.anchorHeight).toInt(),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin),
        resources.getDimensionPixelSize(R.dimen.spotlight_overlay_arrow_left_margin)
      )
    }
    (overlayBinding as TopLeftOverlayBinding).spotlightOverlayArrow.layoutParams = arrowParams
    return (overlayBinding as TopLeftOverlayBinding).root
  }

  private fun calculateScreenSize() {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels
  }

  private sealed class AnchorPosition {
    /** The position corresponding to the anchor when it is on the top left of the screen. */
    object TopLeft : AnchorPosition()
    /** The position corresponding to the anchor when it is on the top right of the screen. */
    object TopRight : AnchorPosition()
    /** The position corresponding to the anchor when it is on the bottom left of the screen. */
    object BottomLeft : AnchorPosition()
    /** The position corresponding to the anchor when it is on the bottom right of the screen. */
    object BottomRight : AnchorPosition()
  }
}
