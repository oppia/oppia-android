package org.oppia.android.app.spotlight

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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
 * Fragment to hold the spotlights on elements. This fragments provides a single place for all the
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
  private val isSpotlightActive = MutableLiveData(false)

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  private fun calculateScreenSize() {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

    screenHeight = displayMetrics.heightPixels
    screenWidth = displayMetrics.widthPixels
  }

  /** LiveData to monitor spotlight activity status. */
  fun getSpotlightStatusLiveData(): MutableLiveData<Boolean> {
    return isSpotlightActive
  }

  // since this fragment does not have any view to inflate yet, all the tasks should be done here.
  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)

    internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY) ?: -1
    calculateScreenSize()
  }

  /**
   * Requests a spotlight to be shown on the [SpotlightTarget]. The spotlight is enqueued if it
   * [SpotlightTarget] hasn't been shown before in a FIFO buffer. This API can ensure proper
   * spotlighting of a which is laid out after an animation. For the spotlights to work correctly,
   * we must know the [SpotlightTarget]'s anchor's size and position. For a view that is laid out
   * after an animation, we must wait until the final size and positions of the anchor view are
   * measured, which can be achieved by using doOnPreDraw. Refer:
   * https://betterprogramming.pub/stop-using-post-postdelayed-in-your-android-views-9d1c8eeaadf2
   *
   * @param spotlightTarget The [SpotlightTarget] for which the spotlight is requested
   */
  override fun requestSpotlightViewWithDelayedLayout(spotlightTarget: SpotlightTarget) {
    spotlightTarget.anchor.doOnPreDraw {
      if (it.visibility != View.VISIBLE) {
        return@doOnPreDraw
      }
      val targetAfterViewFullyDrawn = SpotlightTarget(
        it,
        spotlightTarget.hint,
        spotlightTarget.shape,
        spotlightTarget.feature
      )
      requestSpotlight(targetAfterViewFullyDrawn)
    }
  }

  /**
   * Requests a spotlight to be shown on the [SpotlightTarget]. The spotlight is enqueued if it
   * hasn't been shown before in a FIFO buffer. This API can ensure proper spotlighting of a [SpotlightTarget]
   * when it is laid out late due to a data provider call. It cannot ensure the same if the view has to be
   * spotlight immediately after an animation. It also cannot spotlight targets which are a part of a
   * recycler view which are laid out after a data provider call. If TalkBack is turned on, no spotlight shall
   * be shown.
   *
   * @param spotlightTarget The [SpotlightTarget] for which the spotlight is requested
   */
  override fun requestSpotlight(spotlightTarget: SpotlightTarget) {

    if (accessibilityService.isScreenReaderEnabled()) return

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
          isSpotlightActive.value = true
        }

        override fun onEnded() {
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

  private fun calculateAnchorPosition(spotlightTarget: SpotlightTarget): AnchorPosition {
    Log.d("overlay", "checking anchor position. RTL = $isRtl")
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
    Log.d("overlay", anchorPosition.toString())

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

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    spotlight.next()
  }

  private fun configureBottomLeftOverlay(spotlightTarget: SpotlightTarget): View {
    Log.d("overlay", "bottom left overlay")

    overlayBinding = BottomLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }

    (overlayBinding as BottomLeftOverlayBinding).customText.text =
      getSpannableHint(spotlightTarget.hint)

    val arrowParams = (overlayBinding as BottomLeftOverlayBinding).arrow.layoutParams
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
    (overlayBinding as BottomLeftOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as BottomLeftOverlayBinding).root
  }

  private fun configureBottomRightOverlay(spotlightTarget: SpotlightTarget): View {
    Log.d("overlay", "bottom right overlay")
    overlayBinding = BottomRightOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as BottomRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }

    (overlayBinding as BottomRightOverlayBinding).customText.text =
      getSpannableHint(spotlightTarget.hint)

    val arrowParams = (overlayBinding as BottomRightOverlayBinding).arrow.layoutParams
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
    (overlayBinding as BottomRightOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as BottomRightOverlayBinding).root
  }

  private fun getSpannableHint(hint: String): SpannableString {
    val spannable = SpannableString(hint)
    spannable.setSpan(
      BackgroundColorSpan(resources.getColor(R.color.spotlight_hint_background)),
      0, // start
      spannable.length, // end
      Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
    return spannable
  }

  private fun configureTopRightOverlay(spotlightTarget: SpotlightTarget): View {
    Log.d("overlay", "top right overlay")

    overlayBinding = TopRightOverlayBinding.inflate(layoutInflater)
    (overlayBinding as TopRightOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }

    (overlayBinding as TopRightOverlayBinding).customText.text =
      getSpannableHint(spotlightTarget.hint)

    val arrowParams = (overlayBinding as TopRightOverlayBinding).arrow.layoutParams
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
    (overlayBinding as TopRightOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as TopRightOverlayBinding).root
  }

  private fun configureTopLeftOverlay(spotlightTarget: SpotlightTarget): View {
    Log.d("overlay", "top left overlay")

    overlayBinding = TopLeftOverlayBinding.inflate(this.layoutInflater)
    (overlayBinding as TopLeftOverlayBinding).let {
      it.lifecycleOwner = this
      it.listener = this
    }

    (overlayBinding as TopLeftOverlayBinding).customText.text =
      getSpannableHint(spotlightTarget.hint)

    val arrowParams = (overlayBinding as TopLeftOverlayBinding).arrow.layoutParams
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
    (overlayBinding as TopLeftOverlayBinding).arrow.layoutParams = arrowParams

    return (overlayBinding as TopLeftOverlayBinding).root
  }
}
