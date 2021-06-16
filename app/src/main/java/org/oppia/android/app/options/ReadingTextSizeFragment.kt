package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE =
  "READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE"
private const val SELECTED_READING_TEXT_SIZE_SAVED_KEY =
  "ReadingTextSizeFragment.selected_text_size"

/** The fragment to change the text size of the reading content in the app. */
class ReadingTextSizeFragment : InjectableFragment(), TextSizeRadioButtonListener {
  @Inject
  lateinit var readingTextSizeFragmentPresenter: ReadingTextSizeFragmentPresenter

  companion object {
    fun newInstance(readingTextSize: String): ReadingTextSizeFragment {
      val args = Bundle()
      args.putString(KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE, readingTextSize)
      val fragment = ReadingTextSizeFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to ReadingTextSizeFragment" }
    val readingTextSize = if (savedInstanceState == null) {
      args.get(KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE) as String
    } else {
      savedInstanceState.get(SELECTED_READING_TEXT_SIZE_SAVED_KEY) as String
    }
    return readingTextSizeFragmentPresenter.handleOnCreateView(inflater, container, readingTextSize)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      SELECTED_READING_TEXT_SIZE_SAVED_KEY,
      readingTextSizeFragmentPresenter.getTextSizeSelected()
    )
  }

  override fun onTextSizeSelected(selectedTextSize: String) {
    readingTextSizeFragmentPresenter.onTextSizeSelected(selectedTextSize)
  }
}
