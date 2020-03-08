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
    val questions: Array<String> = activity.resources.getStringArray(R.array.faq_questions)
    for (question in questions) {
      val faqViewModel = FAQViewModel(question)
      arrayList.add(faqViewModel)
    }
    return arrayList
  }
}
