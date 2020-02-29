package org.oppia.app.help

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.CategoryBinding
import org.oppia.app.help.faq.FAQActivity

/** The adapter to set up the recycler view in the [HelpFragment] */
class HelpCategoryAdapter(private val context: Context, private val arrayList: ArrayList<HelpViewModel>) :
  RecyclerView.Adapter<HelpCategoryAdapter.CustomView>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpCategoryAdapter.CustomView {
    val layoutInflater = LayoutInflater.from(parent.context)
    val categoryBinding: CategoryBinding = DataBindingUtil.inflate(layoutInflater , R.layout.help_recyclerview_single_item_layout,parent,false)
    return CustomView(categoryBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: HelpCategoryAdapter.CustomView, position: Int) {
    val categoryViewModel = arrayList[position]
    holder.bind(categoryViewModel)
    holder.itemView.setOnClickListener {
      if(position == 0){
        val intent = FAQActivity.createFAQActivityIntent(context)
        context.startActivity(intent)
      }
    }
  }

  class CustomView(val categoryBinding: CategoryBinding):RecyclerView.ViewHolder(categoryBinding.root){
    fun bind(helpViewModel: HelpViewModel){
      this.categoryBinding.helpmodel = helpViewModel
      categoryBinding.executePendingBindings()
    }
  }
}