package org.oppia.app.topic.train

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.TopicTrainFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.app.databinding.TopicTrainSkillViewBinding
import javax.inject.Inject

/** The presenter for [TopicTrainFragment]. */
@FragmentScope
class TopicTrainFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicTrainViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicTrainFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.skillRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
      layoutManager = LinearLayoutManager(context)
    }
    binding.let {
      it.viewModel = getTopicTrainViewModel()
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<String> {
    return BindableAdapter.Builder
      .newBuilder<String>()
      .registerViewDataBinder(
        inflateDataBinding = TopicTrainSkillViewBinding::inflate,
        setViewModel = TopicTrainSkillViewBinding::setSkill)
      .build()
  }

  private fun getTopicTrainViewModel(): TopicTrainViewModel {
    return viewModelProvider.getForFragment(fragment, TopicTrainViewModel::class.java)
  }
}
