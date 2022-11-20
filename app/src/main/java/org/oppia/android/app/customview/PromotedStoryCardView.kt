package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import org.oppia.android.R
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

/** [MaterialCardView] that represents stories promoted to the learner. */
class PromotedStoryCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

  @Inject
  lateinit var fragment: Fragment

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var isSpotlit = false

  /** Sets the index at which this custom view is located inside the recycler view. */
  fun setIndex(index: Int) {
    // This view can get attached multiple times and we must make sure that the spotlight is
    // requested only once. Only spotlight the item at the first index of the recycler view.
    if (!isSpotlit && index == 0) {
      isSpotlit = true
      val spotlightTarget = SpotlightTarget(
        this,
        resourceHandler.getStringInLocale(R.string.promoted_story_spotlight_hint),
        feature = Spotlight.FeatureCase.PROMOTED_STORIES
      )
      checkNotNull(getSpotlightFragment()).requestSpotlightViewWithDelayedLayout(spotlightTarget)
    }
  }

  private fun getSpotlightFragment(): SpotlightManager? {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory =
      FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }
}
