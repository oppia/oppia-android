package org.oppia.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.grid_test_item_list.view.*
import org.oppia.app.R

class DummyGridAdapter(
  private val gridTestItems: List<GridTestItem>
) : RecyclerView.Adapter<DummyGridAdapter.MainViewHolder>() {

  override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MainViewHolder =
    MainViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.grid_test_item_list, p0, false))

  override fun onBindViewHolder(p0: MainViewHolder, p1: Int) {
    p0.bind(gridTestItems.get(0))
  }

  override fun getItemCount() = 20

  inner class MainViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(gridTestItem: GridTestItem) {
      view.tvName.text = gridTestItem.name
      view.tvDescription.text = gridTestItem.description
    }
  }
}