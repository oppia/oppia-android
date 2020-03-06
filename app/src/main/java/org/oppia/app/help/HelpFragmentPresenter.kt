package org.oppia.app.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.R
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private val arrayList = ArrayList<HelpViewModel>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding: HelpFragmentBinding =
      HelpFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    binding.lifecycleOwner = fragment
    binding.helpFragmentRecyclerView.apply {
      adapter = HelpCategoryAdapter(activity, getRecyclerViewItemList())
      layoutManager = LinearLayoutManager(activity)
    }
    return binding.root
  }

  private fun getRecyclerViewItemList(): ArrayList<HelpViewModel> {
    for (item in HelpItems.values()) {
      if (item == HelpItems.FAQ) {
        val category1 = fragment.getString(R.string.frequently_asked_questions_FAQ)
        val helpViewModel = HelpViewModel(category1)
        arrayList.add(helpViewModel)
      }
    }
    return arrayList
  }
}
