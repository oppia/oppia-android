package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.topic.SPOTLIGHT_FRAGMENT_TAG
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

class PromotedStoryCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

  @Inject
  lateinit var fragment: Fragment

  fun setIndex(index: Int) {
    if (index == 0) {
      val promotesStorySpotlightTarget = SpotlightTarget(
        this,
        "From now, here you can view stories you might be interested in",
        SpotlightShape.RoundedRectangle,
        Spotlight.FeatureCase.PROMOTED_STORIES
      )
      getSpotlightFragment().requestSpotlight(promotesStorySpotlightTarget)
    }
  }

  private fun getSpotlightFragment(): SpotlightFragment {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SPOTLIGHT_FRAGMENT_TAG
    ) as SpotlightFragment
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val viewComponentFactory =
      FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
  }
}
