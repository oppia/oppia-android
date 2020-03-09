package org.oppia.app.help.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.FAQItemBinding

/** The Recycler View adapter in the [FAQFragment]. */
class FAQCategoryAdapter(
  private val arrayList: ArrayList<FAQViewModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val header = 1
  private val content = 2

  override fun getItemCount(): Int {
    return arrayList.size
  }

  class HeaderViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)

  class FAQItemView(private val faqItemBinding: FAQItemBinding) :
    RecyclerView.ViewHolder(faqItemBinding.root) {
    fun bind(faqViewModel: FAQViewModel) {
      this.faqItemBinding.viewmodel = faqViewModel
      faqItemBinding.executePendingBindings()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == 0) {
      header;
    } else {
      content;
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == header) {
      val view: View = LayoutInflater.from(parent.context).inflate(
        R.layout.faq_item_header,
        parent,
        /* attachToParent= */false
      )
      HeaderViewHolder(view)
    } else {
      val layoutInflater = LayoutInflater.from(parent.context)
      val faqItemBinding = FAQItemBinding.inflate(
        layoutInflater,
        parent,
        /* attachToParent= */ false
      )
      FAQItemView(faqItemBinding)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (getItemViewType(position) == header) {
      (holder as HeaderViewHolder)
    } else {
      (holder as FAQItemView).bind(arrayList[position])
    }
  }
}

