package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DragDropTestFragmentBinding
import javax.inject.Inject

class DragDropTestFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  var dataList = mutableListOf("Item 1", "Item 2", "Item 3", "Item 4")

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = DragDropTestFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    binding.dragDropRecyclerView.apply {
      adapter = createBindableAdapter()
      (adapter as BindableAdapter<*>).setDataUnchecked(dataList)
    }
    return binding.root
  }

  private fun createBindableAdapter(): BindableAdapter<String> {
    return singleTypeBuilderFactory
      .create<String>()
      .registerViewBinder(
        inflateView = this::inflateTextViewForStringWithoutDataBinding,
        bindView = this::bindTextViewForStringWithoutDataBinding
      )
      .build()
  }

  private fun bindTextViewForStringWithoutDataBinding(textView: TextView, data: String) {
    textView.text = data
  }

  private fun inflateTextViewForStringWithoutDataBinding(viewGroup: ViewGroup): TextView {
    val inflater = LayoutInflater.from(fragment.activity)
    return inflater.inflate(
      R.layout.test_text_view_for_string_no_data_binding, viewGroup, /* attachToRoot= */ false
    ) as TextView
  }
}
