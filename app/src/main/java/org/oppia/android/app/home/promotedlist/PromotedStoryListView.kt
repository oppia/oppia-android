package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.shim.ViewComponentFactory
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a variable list of promoted lesson stories that snaps to
 * a fixed position when being scrolled.
 */
class PromotedStoryListView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  @Inject
  lateinit var bindingInterface: ViewBindingShim
  private var dataList: List<PromotedStoryViewModel> = listOf()

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
      .createViewComponent(this).inject(this)

    /**
     * The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
     * the item is completely visible in [HomeFragment] as soon as learner lifts the finger
     * after scrolling.
     */
    val snapHelper = StartSnapHelper()
    this.onFlingListener = null
    snapHelper.attachToRecyclerView(this)
  }

  /* Sets the list of promoted stories that this view shows to the learner. */
  fun setDataList(newDataList: List<PromotedStoryViewModel>) {
    // Update the adapter and the story list only if the list is new. The parent presenter should
    // not render promoted stories if the list is empty, but default to showing the last list.
    if (newDataList != null && !newDataList.isEmpty() && newDataList != dataList) {
      dataList = newDataList
      adapter = createAdapter()
      (adapter as BindableAdapter<PromotedStoryViewModel>).setDataUnchecked(newDataList)
    }
  }

  private fun createAdapter(): BindableAdapter<PromotedStoryViewModel> {
    return BindableAdapter.SingleTypeBuilder.newBuilder<PromotedStoryViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.providePromotedStoryCardInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.providePromotedStoryViewModel(
            view,
            viewModel
          )
        }
      ).build()
  }
}

/**
 * Sets the list of promoted items for a specific [PromotedStoryListView] to show to the learner
 * via data-binding.
 * */
@BindingAdapter("dataList")
fun setDataList(
  promotedStoryListView: PromotedStoryListView,
  newDataList: List<PromotedStoryViewModel>
) = promotedStoryListView.setDataList(newDataList)
