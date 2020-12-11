package org.oppia.android.app.home.topiclist

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.databinding.PromotedStoryCardBinding

/**
 * A custom [RecyclerView] for displaying a variable list of promoted lesson stories that snaps to
 * a fixed position on the device.
 */
class PromotedStoryListView @JvmOverloads constructor (
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    adapter = BindableAdapter.SingleTypeBuilder.newBuilder<PromotedStoryViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = PromotedStoryCardBinding::inflate,
        setViewModel = PromotedStoryCardBinding::setViewModel
      ).build()

    /*
     * The StartSnapHelper is used to snap between items rather than smooth scrolling,
     * so that the item is completely visible in [HomeFragment] as soon as learner lifts the finger after scrolling.
     */
    val snapHelper = StartSnapHelper()
    this.setOnFlingListener(null)
    snapHelper.attachToRecyclerView(this)
  }
}
