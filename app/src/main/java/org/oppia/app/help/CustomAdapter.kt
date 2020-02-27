package org.oppia.app.help

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.CategoryBinding

class CustomAdapter(private val context: Context, private val arrayList: ArrayList<CategoryViewModel>) :
  RecyclerView.Adapter<CustomAdapter.CustomView>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.CustomView {
    val layoutInflater = LayoutInflater.from(parent.context)
    val categoryBinding:CategoryBinding = DataBindingUtil.inflate(layoutInflater , R.layout.help_recyclerview_single_item_layout,parent,false)
    return CustomView(categoryBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: CustomAdapter.CustomView, position: Int) {
    val categoryViewModel = arrayList[position]
    holder.bind(categoryViewModel)
  }

  class CustomView(val categoryBinding: CategoryBinding):RecyclerView.ViewHolder(categoryBinding.root){
    fun bind(categoryViewModel: CategoryViewModel){
      this.categoryBinding.categorymodel = categoryViewModel
      categoryBinding.executePendingBindings()
    }
  }
}