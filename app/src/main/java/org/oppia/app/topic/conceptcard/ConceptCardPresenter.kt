package org.oppia.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.ConceptCardExampleViewBinding
import org.oppia.app.databinding.ConceptCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ConceptCardPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>
){

  /** Sets up data binding and adapter for RecyclerView */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, skillId: String): View? {
    val viewModel = getConceptCardViewModel()
    viewModel.setSkillId(skillId)
    val binding = ConceptCardFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.conceptCardToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.conceptCardToolbar.setNavigationOnClickListener {
      (fragment as? DialogFragment)?.dismiss()
    }
    binding.workedExamples.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getConceptCardViewModel(): ConceptCardViewModel {
    return viewModelProvider.getForFragment(fragment, ConceptCardViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<SubtitledHtml> {
    return BindableAdapter.Builder
      .newBuilder<SubtitledHtml>()
      .registerViewDataBinder(
        inflateDataBinding = ConceptCardExampleViewBinding::inflate,
        setViewModel = ConceptCardExampleViewBinding::setSubtitledHtml)
      .build()
  }
}
