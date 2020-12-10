package org.oppia.android.app.home.topiclist

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.databinding.PromotedStoryCardBinding

/** Adapter to bind promoted stories to [RecyclerView] inside [HomeFragment] to create carousel. */
class PromotedStoryListAdapter(
  private val activity: AppCompatActivity,
  private val itemList: List<PromotedStoryViewModel>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val orientation = Resources.getSystem().configuration.orientation

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding =
      PromotedStoryCardBinding.inflate(
        inflater,
        parent,
        /* attachToParent= */ false
      )
    return PromotedStoryViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as PromotedStoryViewHolder).bind(itemList[position])
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  inner class PromotedStoryViewHolder(
    val binding: PromotedStoryCardBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(promotedStoryViewModel: PromotedStoryViewModel) {
      binding.viewModel = promotedStoryViewModel
      val layoutParams = binding.promotedStoryCardContainer.layoutParams
      layoutParams.width =  // set layout width based on number of stories
        if (itemCount > 1) {  // this checks for the number of promoted stories?
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup.LayoutParams.MATCH_PARENT
          } else {
            // set a different width only if it's landscape & there are multiple promoted stories
            (activity as Context).resources.getDimensionPixelSize(R.dimen.promoted_story_card_width)
          }
        } else {
          ViewGroup.LayoutParams.MATCH_PARENT
        }
      binding.promotedStoryCardContainer.layoutParams = layoutParams
    }
  }
}
