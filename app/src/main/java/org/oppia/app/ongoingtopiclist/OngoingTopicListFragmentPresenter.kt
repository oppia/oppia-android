package org.oppia.app.ongoingtopiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.OngoingTopicItemBinding
import org.oppia.app.databinding.OngoingTopicListFragmentBinding
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [OngoingTopicListFragment]. */
class OngoingTopicListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OngoingTopicListViewModel>
) {

  private lateinit var binding: OngoingTopicListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val viewModel = getOngoingTopicListViewModel()
    binding = OngoingTopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    viewModel.setProfileId(internalProfileId)

    binding.ongoingTopicListToolbar.setNavigationOnClickListener {
      (activity as OngoingTopicListActivity).finish()
    }

    binding.ongoingTopicList.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = createRecyclerViewAdapter()
    }

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<OngoingTopicItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<OngoingTopicItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = OngoingTopicItemBinding::inflate,
        setViewModel = OngoingTopicItemBinding::setViewModel
      )
      .build()
  }

  private fun getOngoingTopicListViewModel(): OngoingTopicListViewModel {
    return viewModelProvider.getForFragment(fragment, OngoingTopicListViewModel::class.java)
  }
}
