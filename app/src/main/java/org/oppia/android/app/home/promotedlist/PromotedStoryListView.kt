package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

private const val PROMOTED_STORY_LIST_VIEW_TAG = "PromotedStoryListView"

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

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fragment: Fragment

  lateinit var promotedDataList: List<PromotedStoryViewModel>

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    try {
      super.onAttachedToWindow()

      val viewComponentFactory =
        FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
      val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
      viewComponent.inject(this)

      // The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
      // the item is completely visible in [HomeFragment] as soon as learner lifts the finger
      // after scrolling.
      val snapHelper = StartSnapHelper()
      onFlingListener = null
      snapHelper.attachToRecyclerView(this)

      checkIfComponentsInitialized()
    } catch (e: IllegalStateException) {
      if (::oppiaLogger.isInitialized)
        oppiaLogger.e(
          "LessonThumbnailImageView",
          "Throws exception on attach to window",
          e
        )
    }
  }

  private fun checkIfComponentsInitialized() {
    if (::fragment.isInitialized &&
      ::oppiaLogger.isInitialized
    ) {
      bindDataToAdapter()
    } else {
      oppiaLogger.w(PROMOTED_STORY_LIST_VIEW_TAG, "One of components not initialized")
    }
  }

  private fun bindDataToAdapter() {
    // To reliably bind data only after the adapter is created, we manually set the data so we can first
    // check for the adapter; when using an existing [RecyclerViewBindingAdapter] there is no reliable
    // way to check that the adapter is created.
    // This ensures that the adapter will only be created once and correctly rebinds the data.
    // For more context: https://github.com/oppia/oppia-android/pull/2246#pullrequestreview-565964462
    if (adapter == null) {
      adapter = createAdapter()
    }
    if (::promotedDataList.isInitialized) {
      (adapter as BindableAdapter<*>).setDataUnchecked(promotedDataList)
    } else {
      oppiaLogger.w(PROMOTED_STORY_LIST_VIEW_TAG, "Failed to resolve new story list data")
    }
  }

  /**
   * Sets the list of promoted stories that this view shows to the learner.
   *
   * @param newDataList the new list of stories to present
   */
  fun setPromotedStoryList(newDataList: List<PromotedStoryViewModel>?) {
    if (newDataList != null) {
      promotedDataList = newDataList
      oppiaLogger.w(PROMOTED_STORY_LIST_VIEW_TAG, promotedDataList.size.toString())
    } else {
      if (::oppiaLogger.isInitialized) {
        oppiaLogger.w(PROMOTED_STORY_LIST_VIEW_TAG, "new story list data empty")
      } else {
        Log.e(PROMOTED_STORY_LIST_VIEW_TAG, "new story list data empty")
      }
    }
  }

  private fun createAdapter(): BindableAdapter<PromotedStoryViewModel> {
    return BindableAdapter.SingleTypeBuilder.Factory(fragment).create<PromotedStoryViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.providePromotedStoryCardInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            attachToParent = false
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
