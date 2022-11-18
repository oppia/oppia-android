package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler

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

  fun setIndex(index: Int) {
    if (!isSpotlit) {
      isSpotlit = true
      val spotlightTarget = SpotlightTarget(
        this,
        resourceHandler.getStringInLocale(R.string.promoted_story_spotlight_hint),
        feature = Spotlight.FeatureCase.PROMOTED_STORIES
      )
      if (index == 0) {
        checkNotNull(getSpotlightFragment()).requestSpotlightViewWithDelayedLayout(spotlightTarget)
      }
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
