package org.oppia.android.app.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import javax.inject.Inject

/** A custom [RecyclerView] for displaying a list of profiles as a carousel. */
class ProfileListView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  @Inject
  lateinit var bindingInterface: ViewBindingShim

  @Inject
  lateinit var singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory

  private lateinit var profileDataList: List<ProfileItemViewModel>

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory = FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    maybeInitializeAdapter()
  }

  private fun maybeInitializeAdapter() {
    if (::bindingInterface.isInitialized &&
      ::singleTypeBuilderFactory.isInitialized &&
      ::profileDataList.isInitialized
    ) {
      bindDataToAdapter()
    }
  }

  private fun bindDataToAdapter() {
    // We manually set the data so we can first check for the adapter unlike when using an existing
    // [RecyclerViewBindingAdapter].
    // This ensures that the adapter will only be created once and correctly rebinds the data.
    // For more context: https://github.com/oppia/oppia-android/pull/2246#pullrequestreview-565964462
    if (adapter == null) {
      adapter = createAdapter()
    }

    (adapter as BindableAdapter<*>).setDataUnchecked(profileDataList)
  }

  private fun createAdapter(): BindableAdapter<ProfileItemViewModel> {
    return singleTypeBuilderFactory.create<ProfileItemViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.provideProfileItemInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            attachToParent = false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.provideProfileItemViewModel(
            view,
            viewModel
          )
        }
      ).build()
  }

  /**
   * Sets the list of profiles that this view shows.
   * @param newDataList the new list of profiles to present
   */

  fun setProfileList(newDataList: List<ProfileItemViewModel>?) {
    if (newDataList != null) {
      profileDataList = newDataList
      maybeInitializeAdapter()
    }
  }
}
