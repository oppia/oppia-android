package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.topic.SPOTLIGHT_FRAGMENT_TAG
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl

class ChapterNotStartedContainerConstraintLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private var index: Int = -1
  private var isSpotlit = false

  @Inject
  lateinit var fragment: Fragment

  fun setStoryIndex(index: Int) {
    // Only spotlight the first chapter of the "first" story. We know for sure that for a new user,
    // the first chapter shall be a type of not started chapter view. The index tells which story
    // are we on.
    this.index = index
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

    if (!isSpotlit) {
      isSpotlit = true
      val spotlightTarget = SpotlightTarget(
        this,
        "Tap to start a chapter",
        feature = Spotlight.FeatureCase.FIRST_CHAPTER
      )
      getSpotlightFragment().requestSpotlightOnFirstRecyclerItem(this, index, spotlightTarget)
    }
  }
}
