package org.oppia.app.recyclerview;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableList;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Holds all the custom binding adapters that bind to [RecyclerView] adapters.
 */
public final class RecyclerViewBindingAdapter {
  /**
   * Binds the specified generic data to the adapter of the [RecyclerView]. This is called by
   * Android's data binding framework and should not be used directly. For reference:
   * https://android.jlelse.eu/1bd08b4796b4.
   */
  @BindingAdapter("data")
  public static <T> void bindToRecyclerViewAdapterWithLiveData(
      @NonNull RecyclerView recyclerView,
      @NonNull LiveData<List<T>> liveData
  ) {
    List<T> data = liveData.getValue();
    bindToRecyclerViewAdapter(recyclerView, data);
  }

  /**
   * Binds the specified generic data to the adapter of the [RecyclerView]. This is called by
   * Android's data binding framework and should not be used directly. For reference:
   * https://android.jlelse.eu/1bd08b4796b4.
   */
  @BindingAdapter("list")
  public static <T> void bindToRecyclerViewAdapterWithoutLiveData(
      @NonNull RecyclerView recyclerView,
      List<T> itemList
  ) {
    if (!(itemList == null) && !(itemList.isEmpty())) {
      bindToRecyclerViewAdapter(recyclerView, itemList);
    }
  }

  /**
   * A variant of [bindToRecyclerViewAdapterWithLiveData] that instead uses an observable list.
   */
  @BindingAdapter("data")
  public static <T> void bindToRecyclerViewAdapterWithObservableList(
      @NonNull RecyclerView recyclerView,
      @NonNull ObservableList<T> dataList
  ) {
    bindToRecyclerViewAdapter(recyclerView, dataList);
  }

  private static <T> void bindToRecyclerViewAdapter(
      @NonNull RecyclerView recyclerView,
      @NonNull List<T> dataList
  ) {
    BindableAdapter<T> adapter = (BindableAdapter<T>) recyclerView.getAdapter();
    if (adapter == null) {
      throw new IllegalArgumentException(
          "Cannot bind data to a RecyclerView missing its adapter."
      );
    }
    if (!(recyclerView.getAdapter() instanceof BindableAdapter)) {
      throw new IllegalArgumentException(
          "Can only bind data to a BindableAdapter."
      );
    }
    adapter.setDataUnchecked(dataList);
  }

  @BindingAdapter("itemDecorator")
  public static void addItemDecorator(
      @NonNull RecyclerView recyclerView,
      @NonNull Drawable drawable
  ) {
    recyclerView.addItemDecoration(new DividerItemDecorator(drawable));
  }
}
