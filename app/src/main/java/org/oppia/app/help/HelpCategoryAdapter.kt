package org.oppia.app.help

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.HelpBinding
import org.oppia.app.help.faq.FAQActivity

/** The adapter to set up the recycler view in the [HelpFragment] */
class HelpCategoryAdapter(
  private val context: Context,
  private val arrayList: ArrayList<HelpViewModel>
) :
  RecyclerView.Adapter<HelpCategoryAdapter.HelpItemView>() {
  private lateinit var helpBinding: HelpBinding
  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): HelpCategoryAdapter.HelpItemView {
    val layoutInflater = LayoutInflater.from(parent.context)
    helpBinding = DataBindingUtil.inflate(
      layoutInflater,
      R.layout.help_recyclerview_single_item_layout,
      parent,
      false
    )
    return HelpItemView(helpBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: HelpCategoryAdapter.HelpItemView, position: Int) {
    val helpCategoryViewModel = arrayList[position]
    holder.bind(helpCategoryViewModel)
    holder.itemView.setOnClickListener {
      when (HelpItems.getHelpItemForPosition(position)) {
        HelpItems.FAQ -> {
          val intent = FAQActivity.createFAQActivityIntent(context)
          context.startActivity(intent)
        }
      }
    }
  }

  class HelpItemView(val helpBinding: HelpBinding) : RecyclerView.ViewHolder(helpBinding.root) {
    fun bind(helpViewModel: HelpViewModel) {
      this.helpBinding.helpmodel = helpViewModel
      helpBinding.executePendingBindings()
    }
  }
}
