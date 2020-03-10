package org.oppia.app.help.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.FaqContentBinding

/** The Recycler View adapter in the [FAQFragment]. */
class FAQCategoryAdapter(
  private val arrayList: ArrayList<FAQViewModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private enum class FAQItemViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CONTENT
  }

  override fun getItemCount(): Int {
    return arrayList.size + 1
  }

  class HeaderViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)

  class FAQContentView(private val faqContentBinding: FaqContentBinding) :
    RecyclerView.ViewHolder(faqContentBinding.root) {
    fun bind(
      faqViewModel: FAQViewModel,
      position: Int,
      arrayListSize: Int
    ) {
      this.faqContentBinding.viewModel = faqViewModel
      if (position == arrayListSize) {
        faqViewModel.showDivider.set(false)
      } else {
        faqViewModel.showDivider.set(true)
      }
      faqContentBinding.executePendingBindings()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == 0) {
      FAQItemViewType.VIEW_TYPE_HEADER.ordinal
    } else {
      FAQItemViewType.VIEW_TYPE_CONTENT.ordinal
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == FAQItemViewType.VIEW_TYPE_HEADER.ordinal) {
      val view: View = LayoutInflater.from(parent.context).inflate(
        R.layout.faq_item_header,
        parent,
        /* attachToParent= */ false
      )
      HeaderViewHolder(view)
    } else {
      val layoutInflater = LayoutInflater.from(parent.context)
      val faqContentBinding = FaqContentBinding.inflate(
        layoutInflater,
        parent,
        /* attachToParent= */ false
      )
      FAQContentView(faqContentBinding)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (getItemViewType(position) == FAQItemViewType.VIEW_TYPE_HEADER.ordinal) {
      (holder as HeaderViewHolder)
    } else {
      (holder as FAQContentView).bind(arrayList[position - 1], position, arrayList.size)
    }
  }
}
