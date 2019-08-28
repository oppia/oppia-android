package org.oppia.app.topic_index.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.backend.model.TopicIndexModel
import org.oppia.app.R
import org.oppia.app.topic_index.TopicIndexFragment

import java.util.ArrayList

/** TopicIndexAdapter binds the summary data to the list of Topics */
class TopicIndexAdapter(val topicSummaryList: List<TopicIndexModel>, internal var context: TopicIndexFragment) :
  RecyclerView.Adapter<TopicIndexAdapter.MyViewHolder>() {

  //the class is hodling the list view
  inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    internal var tvTitle: TextView
    internal var tvCategory: TextView
    internal var tvDescription: TextView
    internal var tvNumOfLessons: TextView
    internal var llSubContainer: RelativeLayout
    internal var llContainer: LinearLayout
    internal var cardView: CardView

    init {

      tvTitle = view.findViewById(R.id.tvTitle)
      tvCategory = view.findViewById(R.id.tvCategory)
      tvDescription = view.findViewById(R.id.tvDescription)
      tvNumOfLessons = view.findViewById(R.id.tvNumOfLessons)

      cardView = view.findViewById(R.id.cardView)
      llContainer = view.findViewById(R.id.llContainer)
      llSubContainer = view.findViewById(R.id.llSubContainer)

    }
  }
  //this method is returning the view for each item in the list
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.topic_index_items, parent, false)

    return MyViewHolder(itemView)
  }
  //this method is binding the data on the list
  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    holder.tvTitle.text = topicSummaryList[position].title
    holder.tvDescription.text = topicSummaryList[position].objective
    holder.tvNumOfLessons.text =topicSummaryList[position].num_of_lessons!! + context.getString(R.string.lessons)
    holder.tvCategory.text = topicSummaryList[position].category

  }
  //this method is giving the size of the list
  override fun getItemCount(): Int {
    return topicSummaryList.size
  }

}
