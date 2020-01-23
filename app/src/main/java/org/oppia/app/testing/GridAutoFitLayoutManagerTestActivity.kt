package org.oppia.app.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.recyclerview.GridAutoFitLayoutManager

/** Activity that controls profile creation and selection. */
class GridAutoFitLayoutManagerTestActivity : AppCompatActivity() {

  private lateinit var adapter: DummyGridAdapter
  private val dummyGridItems = mutableListOf(GridTestItem())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.grid_auto_fit_layout_manager_test_activity)


    // bind RecyclerView
    val recyclerView = findViewById<RecyclerView>(R.id.grid_recycler_view)
    recyclerView.setHasFixedSize(true)
    val layoutManager = GridAutoFitLayoutManager(this, columnWidth = 300)// assume cell width of 500px
    recyclerView.setLayoutManager(layoutManager)
    recyclerView.setHasFixedSize(true)
    adapter = DummyGridAdapter(dummyGridItems)
    recyclerView.setAdapter(adapter)

  }
}
