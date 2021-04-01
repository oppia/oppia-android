package org.oppia.android.app.mydownloads.downloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DownloadsFragmentBinding
import org.oppia.android.databinding.DownloadsSortbyBinding
import org.oppia.android.databinding.DownloadsTopicCardBinding
import javax.inject.Inject

const val TAG_DOWNLOAD_TOPIC_DELETE_DIALOG = "DOWNLOAD_TOPIC_DELETE_DIALOG"

/** The presenter for [DownloadsFragment]. */
@FragmentScope
class DownloadsFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {

  private var internalProfileId: Int = -1

  @Inject
  lateinit var downloadsViewModel: DownloadsViewModel

  lateinit var binding: DownloadsFragmentBinding

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    binding = DownloadsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.internalProfileId = internalProfileId
    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = downloadsViewModel
    }
    downloadsViewModel.setInternalProfileId(internalProfileId)
    downloadsViewModel.setProfileId(internalProfileId)
    binding.downloadsRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<DownloadsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<DownloadsItemViewModel, ViewType> { downloadsItemViewModel ->
        when (downloadsItemViewModel) {
          is DownloadsSortByViewModel -> ViewType.VIEW_TYPE_SORT_BY
          is DownloadsTopicViewModel -> ViewType.VIEW_TYPE_TOPIC
          else -> throw IllegalArgumentException(
            "Encountered unexpected view model: $downloadsItemViewModel"
          )
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SORT_BY,
        inflateDataBinding = DownloadsSortbyBinding::inflate,
        setViewModel = this::bindDownloadsSortBy,
        transformViewModel = { it as DownloadsSortByViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TOPIC,
        inflateDataBinding = DownloadsTopicCardBinding::inflate,
        setViewModel = this::bindDownloadsTopicCard,
        transformViewModel = { it as DownloadsTopicViewModel }
      )
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_SORT_BY,
    VIEW_TYPE_TOPIC
  }

  private fun bindDownloadsSortBy(
    binding: DownloadsSortbyBinding,
    viewModel: DownloadsSortByViewModel
  ) {
    binding.viewModel = viewModel
    val items = listOf("Newest", "Alphabetical", "Download Size", "Option 4")
    val adapter = ArrayAdapter(fragment.requireContext(), R.layout.downloads_sortby_menu, items)
    binding.sortByMenu.setInputType(0)
    binding.sortByMenu.setText(adapter.getItem(0).toString(), false)
    binding.sortByMenu.setAdapter(adapter)
  }

  private fun bindDownloadsTopicCard(
    binding: DownloadsTopicCardBinding,
    viewModel: DownloadsTopicViewModel
  ) {
    binding.viewModel = viewModel

    var isDeleteExpanded = false
    binding.isDeleteExpanded = isDeleteExpanded

    binding.expandListIcon.setOnClickListener {
      isDeleteExpanded = when (isDeleteExpanded) {
        true -> {
          false
        }
        else -> {
          true
        }
      }
      binding.isDeleteExpanded = isDeleteExpanded
    }

    binding.deleteImageView.setOnClickListener {
      downloadsViewModel.isAllowedDownloadAccess.observe(
        fragment,
        Observer { isAllowedDownloadAccess ->
          val dialogFragment = DownloadsTopicDeleteDialogFragment
            .newInstance(internalProfileId, isAllowedDownloadAccess)
          dialogFragment.showNow(fragment.childFragmentManager, TAG_DOWNLOAD_TOPIC_DELETE_DIALOG)
        }
      )
    }
  }
}
