package org.oppia.app.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.viewmodel.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HelpViewModel>
) {
  private var helpCategoryAdapter: HelpCategoryAdapter? = null
  private val arrayList = ArrayList<HelpViewModel>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding: HelpFragmentBinding =
      HelpFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    binding.lifecycleOwner = fragment
    getHelpModel()
    binding.helpFragmentRecyclerView.apply {
      adapter = HelpCategoryAdapter(getRecyclerViewItemList())
      layoutManager = LinearLayoutManager(activity)
    }
    return binding.root
  }

  private fun getHelpModel(): HelpViewModel {
    return viewModelProvider.getForFragment(fragment, HelpViewModel::class.java)
  }

  private fun getRecyclerViewItemList(): ArrayList<HelpViewModel> {
    for (item in HelpItems.values()) {
      if (item == HelpItems.FAQ) {
        val category1 = fragment.getString(R.string.frequently_asked_questions_FAQ)
        val helpViewModel = HelpViewModel(category1, 0, activity)
        arrayList.add(helpViewModel)
      }
    }
    return arrayList
  }
}
