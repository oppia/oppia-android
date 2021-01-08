package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.databinding.ComingSoonTopicViewBinding

/**
 * A custom [RecyclerView] for displaying a variable list of Upcoming topics that snaps to
 * a fixed position when being scrolled.
 */
class ComingSoonTopicsListView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  init {
    (context.applicationContext as ApplicationInjectorProvider).getApplicationInjector()
      .injectComingSoonTopicsListView(this)

    adapter = BindableAdapter.SingleTypeBuilder.newBuilder<ComingSoonTopicsViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ComingSoonTopicViewBinding::inflate,
        setViewModel = ComingSoonTopicViewBinding::setViewModel
      ).build()

    /*
     * The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
     * the item is completely visible in [HomeFragment] as soon as learner lifts the finger
     * after scrolling.
     */
    val snapHelper = StartSnapHelper()
    this.onFlingListener = null
    snapHelper.attachToRecyclerView(this)
  }
}
