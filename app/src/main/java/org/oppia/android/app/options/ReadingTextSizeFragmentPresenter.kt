package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.ReadingTextSizeFragmentBinding
import org.oppia.android.databinding.TextSizeItemsBinding
import javax.inject.Inject

/** The presenter for [ReadingTextSizeFragment]. */
class ReadingTextSizeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val readingTextSizeSelectionViewModel: ReadingTextSizeSelectionViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    readingTextSize: ReadingTextSize
  ): View? {
    val binding = ReadingTextSizeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    updateTextSize(readingTextSize)

    binding.viewModel = readingTextSizeSelectionViewModel
    readingTextSizeSelectionViewModel.selectedTextSize = readingTextSize
    binding.textSizeRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  fun getTextSizeSelected(): ReadingTextSize? = readingTextSizeSelectionViewModel.selectedTextSize

  private fun createRecyclerViewAdapter(): BindableAdapter<TextSizeItemViewModel> {
    return singleTypeBuilderFactory.create<TextSizeItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TextSizeItemsBinding::inflate,
        setViewModel = TextSizeItemsBinding::setViewModel
      )
      .build()
  }

  private fun updateTextSize(textSize: ReadingTextSize) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateReadingTextSize(textSize)
      is ReadingTextSizeActivity ->
        parentActivity.readingTextSizeActivityPresenter.setSelectedReadingTextSize(textSize)
    }
  }

  fun onTextSizeSelected(selectedTextSize: ReadingTextSize) {
    readingTextSizeSelectionViewModel.selectedTextSize = selectedTextSize
    updateTextSize(selectedTextSize)
  }
}
