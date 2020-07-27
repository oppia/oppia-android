package org.oppia.app.recyclerview;

import android.graphics.drawable.Drawable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableList;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecyclerViewBindingAdapter {
    /**
     * Binds the specified generic data to the adapter of the [RecyclerView]. This is called by
     * Android's data binding framework and should not be used directly. For reference:
     * https://android.jlelse.eu/1bd08b4796b4.
     */
    @BindingAdapter("data")
    public static <T> void bindToRecyclerViewAdapterWithLiveData(
        RecyclerView recyclerView,
        @NotNull LiveData<List<T>> liveData
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
        RecyclerView recyclerView,
        List<T> itemList
    ) {
        if (!(itemList == null) || !(itemList.isEmpty())) {
            bindToRecyclerViewAdapter(recyclerView, itemList);
        }
    }

/** A variant of [bindToRecyclerViewAdapterWithLiveData] that instead uses an observable list. */
    @BindingAdapter("data")
    public static <T> void bindToRecyclerViewAdapterWithObservableList(
        RecyclerView recyclerView,
        ObservableList<T> dataList
    ) {
        bindToRecyclerViewAdapter(recyclerView, dataList);
    }

    private static <T> void bindToRecyclerViewAdapter(@NotNull RecyclerView recyclerView, List<T> dataList) {
        //val adapter = recyclerView.adapter;
        if (recyclerView.getAdapter() == null) {
            throw new IllegalArgumentException(
                "Cannot bind data to a RecyclerView missing its adapter."
            );
        }
        if (!(recyclerView.getAdapter() instanceof BindableAdapter)) {
            throw new IllegalArgumentException(
                "Can only bind data to a BindableAdapter."
            );
        }
        ((BindableAdapter<T>) recyclerView.getAdapter()).setDataUnchecked(dataList);
    }

    @BindingAdapter("itemDecorator")
    public static void addItemDecorator(@NotNull RecyclerView recyclerView, Drawable drawable) {
        recyclerView.addItemDecoration(new DividerItemDecorator(drawable));
    }
}
