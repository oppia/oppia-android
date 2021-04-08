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

const val DELETE_DOWNLOAD_TOPIC_DIALOG_TAG =
  "DownloadsTopicDeleteDialogFragment.delete_download_topic_dialog_tag"
const val ADMIN_PIN_CONFIRMATION_DIALOG_TAG =
  "DownloadsAccessDialogFragment.admin_pin_confirmation_dialog_tag"

/** The presenter for [DownloadsFragment]. */
@FragmentScope
class DownloadsFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {

  private var internalProfileId: Int = -1

  private lateinit var sortByListIndexListener: SortByListIndexListener

  @Inject
  lateinit var downloadsViewModel: DownloadsViewModel

  private lateinit var downloadsRecyclerViewAdapter: BindableAdapter<DownloadsItemViewModel>
  private lateinit var binding: DownloadsFragmentBinding
  private var previousSortTypeIndex: Int = -1

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    previousSortTypeIndex: Int,
    sortByListIndexListener: SortByListIndexListener
  ): View? {
    binding = DownloadsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.previousSortTypeIndex = if (previousSortTypeIndex == -1) 0 else previousSortTypeIndex
    this.internalProfileId = internalProfileId
    this.sortByListIndexListener = sortByListIndexListener

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = downloadsViewModel
    }

    downloadsViewModel.setInternalProfileId(internalProfileId)
    downloadsViewModel.setProfileId(internalProfileId)

    binding.downloadsRecyclerView.apply {
      downloadsRecyclerViewAdapter = createRecyclerViewAdapter()
      adapter = downloadsRecyclerViewAdapter
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
    val sortByItemsList = mutableListOf<String>()
    SortByItems.values().forEach {
      sortByItemsList.add(fragment.getString(it.value))
    }
    val sortItemAdapter = ArrayAdapter(
      fragment.requireContext(),
      R.layout.downloads_sortby_menu,
      sortByItemsList
    )
    // setting input type to zero makes AutoCompleteTextView no editable
    binding.sortByMenu.inputType = 0
    binding.sortByMenu.setText(
      sortItemAdapter.getItem(previousSortTypeIndex).toString(),
      /* filter =*/ false
    )
    binding.sortByMenu.setAdapter(sortItemAdapter)

    // TODO(#552): orientation change, keep list sorted as per previousSortTypeIndex

    binding.sortByMenu.setOnItemClickListener { parent, _, position, _ ->
      if (previousSortTypeIndex != position) {
        when (parent.getItemAtPosition(position)) {
          SortByItems.NEWEST -> {
            // TODO(#552)
          }
          SortByItems.ALPHABETICAL -> {
            sortTopicAlphabetically()
          }
          SortByItems.DOWNLOAD_SIZE -> {
            // TODO(#552)
          }
        }
        previousSortTypeIndex = position
        sortByListIndexListener.onSortByItemClicked(previousSortTypeIndex)
        binding.sortByMenu.setText(
          sortItemAdapter.getItem(position).toString(),
          /* filter =*/ false
        )
      }
    }
  }

  private fun sortTopicAlphabetically() {
    val sortedTopicNameList = mutableListOf<String>()
    val downloadsItemViewModelList = downloadsViewModel.downloadsViewModelLiveData.value
    downloadsItemViewModelList?.forEach { downloadsItemViewModel ->
      if (downloadsItemViewModel is DownloadsTopicViewModel) {
        sortedTopicNameList.add(downloadsItemViewModel.topicSummary.name)
      }
    }
    sortedTopicNameList.sort()

    val sortedDownloadsItemViewModel = mutableListOf<DownloadsItemViewModel>()
    sortedDownloadsItemViewModel.add(downloadsItemViewModelList!![0])
    sortedTopicNameList.forEach { topicName ->
      downloadsItemViewModelList.forEach { downloadsItemViewModel ->
        if (downloadsItemViewModel is DownloadsTopicViewModel &&
          topicName == downloadsItemViewModel.topicSummary.name
        ) {
          sortedDownloadsItemViewModel.add(downloadsItemViewModel)
        }
      }
    }
    downloadsRecyclerViewAdapter.setData(sortedDownloadsItemViewModel)
  }

  private fun bindDownloadsTopicCard(
    binding: DownloadsTopicCardBinding,
    viewModel: DownloadsTopicViewModel
  ) {
    binding.viewModel = viewModel

    var isDeleteExpanded = false
    binding.isDeleteExpanded = isDeleteExpanded

    binding.expandListIcon.setOnClickListener {
      isDeleteExpanded = !isDeleteExpanded
      binding.isDeleteExpanded = isDeleteExpanded
    }

    binding.deleteImageView.setOnClickListener {
      downloadsViewModel.profileLiveData.observe(
        fragment,
        Observer { profile ->
          when {
            profile.allowDownloadAccess -> {
              openDeleteConfirmationDialog(profile.allowDownloadAccess)
            }
            else -> {
              openAdminPinInputDialog(profile.allowDownloadAccess)
            }
          }
        }
      )
    }
  }

  private fun openDeleteConfirmationDialog(allowDownloadAccess: Boolean) {
    val dialogFragment = DownloadsTopicDeleteDialogFragment
      .newInstance(internalProfileId, allowDownloadAccess)
    dialogFragment.showNow(fragment.childFragmentManager, DELETE_DOWNLOAD_TOPIC_DIALOG_TAG)
  }

  private fun openAdminPinInputDialog(allowDownloadAccess: Boolean) {
    val adminPin = downloadsViewModel.adminPin
    val dialogFragment = DownloadsAccessDialogFragment
      .newInstance(adminPin, internalProfileId, allowDownloadAccess)
    dialogFragment.showNow(fragment.childFragmentManager, ADMIN_PIN_CONFIRMATION_DIALOG_TAG)
  }
}
