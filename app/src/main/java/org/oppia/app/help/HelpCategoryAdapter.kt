package org.oppia.app.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.HelpItemBinding

/** The Recycler View adapter in the [HelpFragment]. */
class HelpCategoryAdapter(
  private val activity: AppCompatActivity,
  private val arrayList: ArrayList<HelpViewModel>
) :
  RecyclerView.Adapter<HelpCategoryAdapter.HelpItemView>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): HelpItemView {
    val layoutInflater = LayoutInflater.from(parent.context)
    val helpItemBinding = HelpItemBinding.inflate(
      layoutInflater,
      parent,
      /* attachToParent= */ false
    )
    return HelpItemView(helpItemBinding)
  }

  override fun getItemCount(): Int {
    return arrayList.size
  }

  override fun onBindViewHolder(holder: HelpItemView, position: Int) {
    holder.bind(arrayList[position], position, activity)
  }

  class HelpItemView(private val helpItemBinding: HelpItemBinding) :
    RecyclerView.ViewHolder(helpItemBinding.root) {
    fun bind(helpViewModel: HelpViewModel, position: Int, activity: AppCompatActivity) {
      this.helpItemBinding.viewModel = helpViewModel
      helpItemBinding.root.setOnClickListener {
        when (HelpItems.getHelpItemForPosition(position)) {
          HelpItems.FAQ -> {
            val routeToFAQListener = activity as RouteToFAQListListener
            routeToFAQListener.onRouteToFAQList()
          }
        }
      }
      helpItemBinding.executePendingBindings()
    }
  }
}
