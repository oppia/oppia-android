package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
  private lateinit var listType: PromotedActivityType

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
      .createViewComponent(this).inject(this)

    /*
     * The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
     * the item is completely visible in [HomeFragment] as soon as learner lifts the finger
     * after scrolling.
     */
    val snapHelper = StartSnapHelper()
    this.onFlingListener = null
    snapHelper.attachToRecyclerView(this)
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

  fun setListType(type: PromotedActivityType) {
    this.listType = type
    adapter = createAdapter()
  }
}
