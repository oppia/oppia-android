package org.oppia.android.app.topicdownloaded

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicDownloadedFragmentBinding
import javax.inject.Inject

/** The presenter for [TopicDownloadedFragment]. */
@FragmentScope
class TopicDownloadedFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicDownloadedViewModel>,
) {

  private val topicDownloadedViewModel = getTopicDownloadedViewModel()

  /** Bind TopicDownloadedFragmentBinding with the TopicDownloadedFragment */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    topicName: String
  ): View? {
    val binding = TopicDownloadedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = topicDownloadedViewModel
    }
    topicDownloadedViewModel.topicName.set(topicName)
    topicDownloadedViewModel.internalProfileId = internalProfileId
    topicDownloadedViewModel.topicId = topicId
    return binding.root
  }

  private fun getTopicDownloadedViewModel(): TopicDownloadedViewModel {
    return viewModelProvider.getForFragment(fragment, TopicDownloadedViewModel::class.java)
  }
}
