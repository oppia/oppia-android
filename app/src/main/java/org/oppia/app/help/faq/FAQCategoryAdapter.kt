package org.oppia.app.help.faq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.FAQItemBinding

/** The Recycler View adapter in the [FAQFragment]. */
class FAQCategoryAdapter(
  private val arrayList: ArrayList<FAQViewModel>
) :
  RecyclerView.Adapter<FAQCategoryAdapter.FAQItemView>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): FAQItemView {
    val layoutInflater = LayoutInflater.from(parent.context)
    val faqItemBinding = FAQItemBinding.inflate(
      layoutInflater,
      parent,
      /* attachToParent= */ false
    )
    return FAQItemView(faqItemBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: FAQItemView, position: Int) {
    holder.bind(arrayList[position])
  }

  class FAQItemView(private val faqItemBinding: FAQItemBinding) :
    RecyclerView.ViewHolder(faqItemBinding.root) {
    fun bind(faqViewModel: FAQViewModel) {
      this.faqItemBinding.viewmodel = faqViewModel
      faqItemBinding.executePendingBindings()
    }
  }
}
