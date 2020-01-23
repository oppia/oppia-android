package org.oppia.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R

class DummyGridAdapter: RecyclerView.Adapter<DummyGridAdapter.MainViewHolder>() {

  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MainViewHolder =
    MainViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.grid_test_item_list, viewGroup, false))

  override fun onBindViewHolder(mainViewHolder: MainViewHolder, position: Int) {
  }

  override fun getItemCount() = 20

  inner class MainViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}
}