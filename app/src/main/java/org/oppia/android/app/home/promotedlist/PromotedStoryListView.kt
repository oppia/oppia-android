package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.application.ApplicationInjectorProvider
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

  init {
    (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
      .createViewComponent(this).inject(this)
    adapter = BindableAdapter.SingleTypeBuilder.newBuilder<PromotedStoryViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.inflatePromotedStoryCardBinding(
            inflater = LayoutInflater.from(context),
            parent = parent,
            attachToParent = false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.providePromotedStoryViewModel(
            view = view,
            viewModel = viewModel
          )
        }
      ).build()

    /*
     * The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
     * the item is completely visible in [HomeFragment] as soon as learner lifts the finger
     * after scrolling.
     */
    val snapHelper = StartSnapHelper()
    this.setOnFlingListener(null)
    snapHelper.attachToRecyclerView(this)
  }
}
