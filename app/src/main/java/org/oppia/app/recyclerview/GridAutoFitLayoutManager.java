package org.oppia.app.recyclerview;

import android.content.Context;
import android.util.TypedValue;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This is used to compute the number of columns based on the predicted size of the recycler view (which should be the
 * width of the parent given the match_parents) divided by the expected column size.
 * for reference https://github.com/pushpalroy/talkie/blob/master/app/src/main/java/com/pushpal/talkie/model/util/GridAutoFitLayoutManager.java.
 */
public class GridAutoFitLayoutManager extends GridLayoutManager {
  private int mColumnWidth;
  private boolean mColumnWidthChanged = true;

  public GridAutoFitLayoutManager(Context context, int columnWidth) {
    /* Initially set spanCount to 1, will be changed automatically later. */
    super(context, 1);
    setColumnWidth(checkedColumnWidth(context, columnWidth));
  }

  public GridAutoFitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout) { /* Initially set spanCount to 1, will be changed automatically later. */
    super(context, 1, orientation, reverseLayout);
    setColumnWidth(checkedColumnWidth(context, columnWidth));
  }

  private int checkedColumnWidth(Context context, int columnWidth) {
    if (columnWidth <= 0) { /* Set default columnWidth value (48dp here). It is better to move this constant to static constant on top, but we need context to convert it to dp, so can't really do so. */
      columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
    }
    return columnWidth;
  }

  private void setColumnWidth(int newColumnWidth) {
    if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
      mColumnWidth = newColumnWidth;
      mColumnWidthChanged = true;
    }
  }

  @Override
  public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (mColumnWidthChanged && mColumnWidth > 0) {
      int totalSpace;
      if (getOrientation() == RecyclerView.VERTICAL) {
        totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
      } else {
        totalSpace = getHeight() - getPaddingTop() - getPaddingBottom();
      }
      int spanCount = Math.max(1, totalSpace / mColumnWidth);
      setSpanCount(spanCount);
      mColumnWidthChanged = false;
    }
    super.onLayoutChildren(recycler, state);
  }
}