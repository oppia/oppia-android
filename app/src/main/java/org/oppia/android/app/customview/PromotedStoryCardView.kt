package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import javax.inject.Inject
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl

class PromotedStoryCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
): MaterialCardView(context, attrs, defStyleAttr) {

  private var promotedStoryIndex = -1

  @Inject
  lateinit var spotlightFragment: SpotlightFragment

  @Inject
  lateinit var fragment: Fragment

  fun setIndex(index: Int) {
    this.promotedStoryIndex = index
  }

  override fun onAttachedToWindow() {
      super.onAttachedToWindow()

      val viewComponentFactory =
        FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
      val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
      viewComponent.inject(this)

    this.post {
      if (promotedStoryIndex == 0) {
          val targetList = arrayListOf(
            SpotlightTarget(
              this,
              "From now, here you can view stories you might be interested in",
              SpotlightShape.RoundedRectangle,
              Spotlight.FeatureCase.PROMOTED_STORIES
            )
          )

          spotlightFragment.initialiseTargetList(targetList, 123)
          fragment.childFragmentManager.beginTransaction()
            .add(spotlightFragment, "")
            .commitNow()
      }
    }
  }
}