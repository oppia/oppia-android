package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.ReadingTextSizeFragmentBinding
import org.oppia.android.databinding.TextSizeItemsBinding
import javax.inject.Inject

/** The presenter for [ReadingTextSizeFragment]. */
class ReadingTextSizeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val readingTextSizeSelectionViewModel: ReadingTextSizeSelectionViewModel,
  resourceHandler: AppLanguageResourceHandler,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private var fontSize: String = resourceHandler.getStringInLocale(
    R.string.reading_text_size_medium
  )

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    readingTextSize: String
  ): View? {
    val binding = ReadingTextSizeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    fontSize = readingTextSize
    updateTextSize(fontSize)

    binding.viewModel = readingTextSizeSelectionViewModel
    readingTextSizeSelectionViewModel.selectedTextSize.value = fontSize
    binding.textSizeRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  fun getTextSizeSelected(): String? {
    return readingTextSizeSelectionViewModel.selectedTextSize.value
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TextSizeItemViewModel> {
    return singleTypeBuilderFactory.create<TextSizeItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TextSizeItemsBinding::inflate,
        setViewModel = TextSizeItemsBinding::setViewModel
      ).setLifecycleOwner(fragment)
      .build()
  }

  private fun updateTextSize(textSize: String) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateReadingTextSize(textSize)
      is ReadingTextSizeActivity ->
        parentActivity.readingTextSizeActivityPresenter.setSelectedReadingTextSize(textSize)
    }
  }

  fun onTextSizeSelected(selectedTextSize: String) {
    readingTextSizeSelectionViewModel.selectedTextSize.value = selectedTextSize
    updateTextSize(selectedTextSize)
  }
}
