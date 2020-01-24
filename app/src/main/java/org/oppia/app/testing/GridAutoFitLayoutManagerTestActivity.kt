package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.recyclerview.GridAutoFitLayoutManager

/** Activity that controls profile creation and selection. */
class GridAutoFitLayoutManagerTestActivity : AppCompatActivity() {

  private lateinit var adapter: DummyGridAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.grid_auto_fit_layout_manager_test_activity)
    val recyclerViewWidth =
      checkNotNull(intent?.getIntExtra(GRID_AUTOFIT_TEST_ACTIVITY_RECYCLERVIEW_WIDTH_ARGUMENT_KEY, 0)) {
        "Expected recyclerViewWidth to be included in intent for GridAutoFitLayoutManagerTestActivity."
      }
    val columnWidth = checkNotNull(intent?.getIntExtra(GRID_AUTOFIT_TEST_ACTIVITY_CELL_WIDTH_ARGUMENT_KEY, 0)) {
      "Expected columnWidth to be included in intent for GridAutoFitLayoutManagerTestActivity."
    }
    val recyclerView = findViewById(R.id.grid_recycler_view) as RecyclerView
    recyclerView.setHasFixedSize(true)

    adapter = DummyGridAdapter()
    val params = recyclerView.getLayoutParams()
    params.width = recyclerViewWidth
    recyclerView.setLayoutParams(params)
    recyclerView.setAdapter(adapter)
    val layoutManager = GridAutoFitLayoutManager(this, columnWidth = columnWidth)// assume cell width of 500px
    recyclerView.setLayoutManager(layoutManager)
    recyclerView.setHasFixedSize(true)

  }

  companion object {

    internal const val GRID_AUTOFIT_TEST_ACTIVITY_RECYCLERVIEW_WIDTH_ARGUMENT_KEY =
      "GridAutoFitLayoutManagerTestActivity.recyclerView.width"
    internal const val GRID_AUTOFIT_TEST_ACTIVITY_CELL_WIDTH_ARGUMENT_KEY =
      "GridAutoFitLayoutManagerTestActivity.cell.width"

    /** Returns a new [Intent] to route to [GridAutoFitLayoutManagerTestActivity] for a specified topic ID. */
    fun createGridAutoFitLayoutManagerTestActivityIntent(
      context: Context,
      recyclerViewWidth: Int,
      columnWidth: Int
    ): Intent {
      val intent = Intent(context, GridAutoFitLayoutManagerTestActivity::class.java)
      intent.putExtra(GRID_AUTOFIT_TEST_ACTIVITY_RECYCLERVIEW_WIDTH_ARGUMENT_KEY, recyclerViewWidth)
      intent.putExtra(GRID_AUTOFIT_TEST_ACTIVITY_CELL_WIDTH_ARGUMENT_KEY, columnWidth)
      return intent
    }
  }
}
