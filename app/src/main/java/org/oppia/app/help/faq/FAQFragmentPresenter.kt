package org.oppia.app.help.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.R
import org.oppia.app.databinding.FaqFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [FAQFragment]. */
@FragmentScope
class FAQFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private val arrayList = ArrayList<FAQViewModel>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = FaqFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    binding.faqFragmentRecyclerView.apply {
      adapter = FAQCategoryAdapter(getRecyclerViewItemList())
      layoutManager = LinearLayoutManager(activity)
    }
    return binding.root
  }

  private fun getRecyclerViewItemList(): ArrayList<FAQViewModel> {
    val category1 = fragment.getString(R.string.frequently_asked_questions_FAQ)
    val faqViewModel = FAQViewModel(category1)
    arrayList.add(faqViewModel)
    return arrayList
  }
}
