package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.R
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

/** Custom view to hold the chapter that has not yet been started. */
class ChapterNotStartedContainerConstraintLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private var index: Int = -1
  private var isSpotlit = false

  @Inject
  lateinit var fragment: Fragment

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  /** Set the index of the story of which this custom view is a part of. */
  fun setStoryIndex(index: Int) {
    // Only spotlight the first chapter of the "first" story. We know for sure that for a new user,
    // the first chapter shall be a type of not started chapter view. The index tells which story
    // are we on.
    this.index = index
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

    if (!isSpotlit) {
      isSpotlit = true
      val spotlightTarget = SpotlightTarget(
        this,
        resourceHandler.getStringInLocale(R.string.first_chapter_spotlight_hint),
        feature = Spotlight.FeatureCase.FIRST_CHAPTER
      )
      if (index == 0) {
        checkNotNull(getSpotlightFragment()).requestSpotlightViewWithDelayedLayout(spotlightTarget)
      }
    }
  }
}
